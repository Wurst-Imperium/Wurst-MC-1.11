/*
 * Copyright © 2014 - 2017 | Wurst-Imperium | All rights reserved.
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.files;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;

import net.minecraft.client.Minecraft;

public final class WurstFolders
{
	public static final File MAIN =
		new File(Minecraft.getMinecraft().mcDataDir, "wurst");
	
	public static final File AUTOBUILD = new File(MAIN, "autobuild");
	public static final File SKINS = new File(MAIN, "skins");
	public static final File SERVERLISTS = new File(MAIN, "serverlists");
	public static final File SPAM = new File(MAIN, "spam");
	public static final File SCRIPTS = new File(SPAM, "autorun");
	public static final File RSA =
		new File(System.getProperty("user.home"), ".ssh");
	
	public static void createFolders()
		throws ReflectiveOperationException, IOException
	{
		if(System.getProperty("user.home") == null)
			throw new IOException("user.home property is missing!");
		
		for(Field field : WurstFolders.class.getFields())
		{
			File dir = ((File)field.get(null));
			
			if(dir.isDirectory())
				continue;
			
			if(!dir.mkdir())
				throw new IOException(
					"Could not create directory: " + dir.getName());
		}
	}
}
