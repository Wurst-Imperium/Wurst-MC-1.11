/*
 * Copyright © 2014 - 2017 | Wurst-Imperium | All rights reserved.
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.files;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.JsonElement;

import tk.wurst_client.utils.JsonUtils;

public abstract class Config
{
	private final Path path;
	
	public Config(String name)
	{
		this.path = WurstFolders.MAIN.resolve(name);
	}
	
	public final void initialize()
	{
		if(Files.exists(path))
			load();
		else
			save();
	}
	
	public final void load()
	{
		try
		{
			loadFromJson(readFile(path));
		}catch(Exception e)
		{
			System.out.println("Failed to load " + path.getFileName());
			e.printStackTrace();
		}
	}
	
	public final void save()
	{
		try
		{
			writeFile(path, saveToJson());
		}catch(Exception e)
		{
			System.out.println("Failed to save " + path.getFileName());
			e.printStackTrace();
		}
	}
	
	protected JsonElement readFile(Path path) throws IOException
	{
		return JsonUtils.jsonParser.parse(Files.newBufferedReader(path));
	}
	
	protected void writeFile(Path path, JsonElement json) throws IOException
	{
		JsonUtils.prettyGson.toJson(json, Files.newBufferedWriter(path));
	}
	
	protected abstract void loadFromJson(JsonElement json);
	
	protected abstract JsonElement saveToJson();
}
