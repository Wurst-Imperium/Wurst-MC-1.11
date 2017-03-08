/*
 * Copyright © 2014 - 2017 | Wurst-Imperium | All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.wurstclient.update;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import net.wurstclient.WurstClient;

public class Updater
{
	private boolean outdated;
	private JsonArray json;
	
	private String latestVersionString;
	private int latestVersionId;
	
	public void checkForUpdate()
	{
		Version currentVersion = new Version(WurstClient.VERSION);
		Version latestVersion = null;
		
		try
		{
			json = fetchJson(
				"https://api.github.com/repos/Wurst-Imperium/Wurst-MC-"
					+ WurstClient.MINECRAFT_VERSION + "/releases")
						.getAsJsonArray();
			
			for(JsonElement release : json)
				if(currentVersion.isPreRelease() || !release.getAsJsonObject()
					.get("prerelease").getAsBoolean())
				{
					latestVersionString = release.getAsJsonObject()
						.get("tag_name").getAsString().substring(1);
					latestVersionId =
						release.getAsJsonObject().get("id").getAsInt();
					latestVersion = new Version(latestVersionString);
					break;
				}
			
			if(latestVersion == null)
				throw new NullPointerException("Latest version is missing!");
			
		}catch(Exception e)
		{
			System.err.println("[Updater] An error occurred!");
			e.printStackTrace();
			return;
		}
		
		System.out.println("[Updater] Current version: " + currentVersion);
		System.out.println("[Updater] Latest version: " + latestVersion);
		outdated = currentVersion.shouldUpdateTo(latestVersion);
	}
	
	private JsonElement fetchJson(String url) throws IOException
	{
		URI u = URI.create(url);
		try(InputStream in = u.toURL().openStream())
		{
			return new JsonParser()
				.parse(new BufferedReader(new InputStreamReader(in)));
		}
	}
	
	public void update()
	{
		new Thread(() -> {
			try
			{
				Path path = Paths
					.get(Updater.class.getProtectionDomain().getCodeSource()
						.getLocation().toURI())
					.getParent().resolve("Wurst-updater.jar");
				
				try(InputStream in =
					getClass().getClassLoader().getResourceAsStream(
						"assets/minecraft/wurst/Wurst-updater.jar"))
				{
					Files.copy(in, path);
				}
				
				ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", "java",
					"-jar", path.toString(), "update", "" + latestVersionId,
					path.getParent().toString(),
					"Wurst-Imperium/Wurst-MC-" + WurstClient.MINECRAFT_VERSION);
				pb.redirectErrorStream(true);
				Process p = pb.start();
				
				try(BufferedReader reader = new BufferedReader(
					new InputStreamReader(p.getInputStream())))
				{
					for(String line; (line = reader.readLine()) != null;)
						System.out.println(line);
				}
				
			}catch(Exception e)
			{
				System.err.println("Could not update!");
				e.printStackTrace();
			}
		}).start();
	}
	
	public boolean isOutdated()
	{
		return outdated;
	}
	
	public String getLatestVersion()
	{
		return latestVersionString;
	}
}
