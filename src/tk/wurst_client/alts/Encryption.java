/*
 * Copyright © 2014 - 2017 | Wurst-Imperium | All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.alts;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import net.minecraft.crash.CrashReport;
import net.minecraft.util.ReportedException;
import tk.wurst_client.files.WurstFolders;

public final class Encryption
{
	private static final String CHARSET = "UTF-8";
	
	private final Cipher encryptCipher;
	private final Cipher decryptCipher;
	
	public Encryption()
	{
		if(System.getProperty("user.home") == null)
			throw new NullPointerException("user.home property is missing!");
		
		KeyPair rsaKeyPair =
			getRsaKeyPair(new File(WurstFolders.RSA, "wurst_rsa.pub"),
				new File(WurstFolders.RSA, "wurst_rsa"));
		
		SecretKey aesKey =
			getAesKey(new File(WurstFolders.MAIN, "key"), rsaKeyPair);
		
		try
		{
			encryptCipher = Cipher.getInstance("AES/CFB8/NoPadding");
			encryptCipher.init(Cipher.ENCRYPT_MODE, aesKey,
				new IvParameterSpec(aesKey.getEncoded()));
			
			decryptCipher = Cipher.getInstance("AES/CFB8/NoPadding");
			decryptCipher.init(Cipher.DECRYPT_MODE, aesKey,
				new IvParameterSpec(aesKey.getEncoded()));
			
		}catch(GeneralSecurityException e)
		{
			throw new ReportedException(
				CrashReport.makeCrashReport(e, "Creating AES ciphers"));
		}
	}
	
	public void saveEncryptedFile(File file, String content) throws IOException
	{
		Files.write(file.toPath(), encrypt(content.getBytes(CHARSET)));
	}
	
	public String loadEncryptedFile(File file) throws IOException
	{
		return new String(decrypt(Files.readAllBytes(file.toPath())), CHARSET);
	}
	
	public byte[] encrypt(byte[] bytes)
	{
		try
		{
			return Base64.getEncoder().encode(encryptCipher.doFinal(bytes));
		}catch(GeneralSecurityException e)
		{
			throw new ReportedException(
				CrashReport.makeCrashReport(e, "Encrypting bytes"));
		}
	}
	
	public byte[] decrypt(byte[] bytes)
	{
		try
		{
			return decryptCipher.doFinal(Base64.getDecoder().decode(bytes));
		}catch(GeneralSecurityException e)
		{
			throw new ReportedException(
				CrashReport.makeCrashReport(e, "Decrypting bytes"));
		}
	}
	
	private KeyPair getRsaKeyPair(File publicFile, File privateFile)
	{
		if(!privateFile.exists() || !publicFile.exists())
			return createRsaKeys(publicFile, privateFile);
		
		try
		{
			return loadRsaKeys(publicFile, privateFile);
		}catch(GeneralSecurityException | ReflectiveOperationException
			| IOException e)
		{
			System.err.println("Couldn't load RSA keypair!");
			e.printStackTrace();
			
			return createRsaKeys(publicFile, privateFile);
		}
	}
	
	private SecretKey getAesKey(File file, KeyPair keyPair)
	{
		if(!file.exists())
			return createAesKey(file, keyPair);
		
		try
		{
			return loadAesKey(file, keyPair);
		}catch(GeneralSecurityException | IOException e)
		{
			System.err.println("Couldn't load AES key!");
			e.printStackTrace();
			
			return createAesKey(file, keyPair);
		}
	}
	
	private KeyPair createRsaKeys(File publicFile, File privateFile)
	{
		try
		{
			System.out.println("Generating RSA keypair.");
			
			// generate keypair
			KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
			generator.initialize(1024);
			KeyPair pair = generator.generateKeyPair();
			
			KeyFactory factory = KeyFactory.getInstance("RSA");
			
			// save public key
			try(ObjectOutputStream stream =
				new ObjectOutputStream(new FileOutputStream(publicFile)))
			{
				RSAPublicKeySpec keySpec = factory.getKeySpec(pair.getPublic(),
					RSAPublicKeySpec.class);
				
				stream.writeObject(keySpec.getModulus());
				stream.writeObject(keySpec.getPublicExponent());
			}
			
			// save private key
			try(ObjectOutputStream stream =
				new ObjectOutputStream(new FileOutputStream(privateFile)))
			{
				RSAPrivateKeySpec keySpec = factory
					.getKeySpec(pair.getPrivate(), RSAPrivateKeySpec.class);
				
				stream.writeObject(keySpec.getModulus());
				stream.writeObject(keySpec.getPrivateExponent());
			}
			
			return pair;
			
		}catch(GeneralSecurityException | IOException e)
		{
			throw new ReportedException(
				CrashReport.makeCrashReport(e, "Creating RSA keypair"));
		}
	}
	
	private SecretKey createAesKey(File file, KeyPair keyPair)
	{
		try
		{
			System.out.println("Generating AES key.");
			
			// generate key
			KeyGenerator keygen = KeyGenerator.getInstance("AES");
			keygen.init(128);
			SecretKey key = keygen.generateKey();
			
			// save key
			Cipher rsaCipher = Cipher.getInstance("RSA");
			rsaCipher.init(Cipher.ENCRYPT_MODE, keyPair.getPublic());
			Files.write(file.toPath(), rsaCipher.doFinal(key.getEncoded()));
			
			return key;
			
		}catch(GeneralSecurityException | IOException e)
		{
			throw new ReportedException(
				CrashReport.makeCrashReport(e, "Creating AES key"));
		}
	}
	
	private KeyPair loadRsaKeys(File publicFile, File privateFile)
		throws GeneralSecurityException, ReflectiveOperationException,
		IOException
	{
		KeyFactory factory = KeyFactory.getInstance("RSA");
		
		// load public key
		PublicKey publicKey;
		try(ObjectInputStream publicLoad =
			new ObjectInputStream(new FileInputStream(publicFile)))
		{
			publicKey = factory.generatePublic(
				new RSAPublicKeySpec((BigInteger)publicLoad.readObject(),
					(BigInteger)publicLoad.readObject()));
		}
		
		// load private key
		PrivateKey privateKey;
		try(ObjectInputStream privateLoad =
			new ObjectInputStream(new FileInputStream(privateFile)))
		{
			privateKey = factory.generatePrivate(
				new RSAPrivateKeySpec((BigInteger)privateLoad.readObject(),
					(BigInteger)privateLoad.readObject()));
		}
		
		return new KeyPair(publicKey, privateKey);
	}
	
	private SecretKey loadAesKey(File file, KeyPair keyPair)
		throws GeneralSecurityException, IOException
	{
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
		
		return new SecretKeySpec(
			cipher.doFinal(Files.readAllBytes(file.toPath())), "AES");
	}
}
