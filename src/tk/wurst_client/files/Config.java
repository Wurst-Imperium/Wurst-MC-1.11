/*
 * Copyright © 2014 - 2017 | Wurst-Imperium | All rights reserved.
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.files;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.google.gson.JsonElement;

import tk.wurst_client.utils.JsonUtils;

public abstract class Config
{
	private final File file;
	
	public Config(String name)
	{
		this.file = new File(WurstFolders.MAIN, name);
	}
	
	public final void initialize()
	{
		if(file.exists())
			load();
		else
			save();
	}
	
	public final void load()
	{
		try
		{
			loadFromJson(readFile(file));
		}catch(Exception e)
		{
			System.out.println("Failed to load " + file.getName());
			e.printStackTrace();
		}
	}
	
	public final void save()
	{
		try
		{
			writeFile(file, saveToJson());
		}catch(Exception e)
		{
			System.out.println("Failed to save " + file.getName());
			e.printStackTrace();
		}
	}
	
	protected JsonElement readFile(File file) throws IOException
	{
		try(FileReader reader = new FileReader(file))
		{
			return JsonUtils.jsonParser.parse(reader);
		}
	}
	
	protected void writeFile(File file, JsonElement json) throws IOException
	{
		try(FileWriter writer = new FileWriter(file))
		{
			JsonUtils.prettyGson.toJson(json, writer);
		}
	}
	
	protected abstract void loadFromJson(JsonElement json);
	
	protected abstract JsonElement saveToJson();
	
	public final File getFile()
	{
		return file;
	}
}
