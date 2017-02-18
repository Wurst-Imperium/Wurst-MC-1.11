/*
 * Copyright © 2014 - 2017 | Wurst-Imperium | All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.files;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.crash.CrashReport;
import net.minecraft.util.ReportedException;
import tk.wurst_client.WurstClient;
import tk.wurst_client.features.mods.XRayMod;
import tk.wurst_client.options.FriendsList;
import tk.wurst_client.utils.JsonUtils;
import tk.wurst_client.utils.XRayUtils;

public class FileManager
{
	public final File friends = new File(WurstFolders.MAIN, "friends.json");
	public final File autoMaximize =
		new File(WurstFolders.MAIN, "automaximize.json");
	public final File xray = new File(WurstFolders.MAIN, "xray.json");
	
	public void init()
	{
		// create folders
		try
		{
			WurstFolders.createFolders();
		}catch(ReflectiveOperationException | IOException e)
		{
			throw new ReportedException(
				CrashReport.makeCrashReport(e, "Creating Wurst folders"));
		}
		
		if(!friends.exists())
			saveFriends();
		else
			loadFriends();
		if(!xray.exists())
		{
			XRayUtils.initXRayBlocks();
			saveXRayBlocks();
		}else
			loadXRayBlocks();
		
		loadAutoBuildTemplates();
	}
	
	public boolean loadAutoMaximize()
	{
		boolean autoMaximizeEnabled = false;
		if(!autoMaximize.exists())
			saveAutoMaximize(true);
		try
		{
			BufferedReader load =
				new BufferedReader(new FileReader(autoMaximize));
			autoMaximizeEnabled = JsonUtils.gson.fromJson(load, Boolean.class)
				&& !Minecraft.IS_RUNNING_ON_MAC;
			load.close();
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		return autoMaximizeEnabled;
	}
	
	public void saveAutoMaximize(boolean autoMaximizeEnabled)
	{
		try
		{
			if(!autoMaximize.getParentFile().exists())
				autoMaximize.getParentFile().mkdirs();
			PrintWriter save = new PrintWriter(new FileWriter(autoMaximize));
			save.println(JsonUtils.prettyGson.toJson(autoMaximizeEnabled));
			save.close();
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void saveFriends()
	{
		try
		{
			PrintWriter save = new PrintWriter(new FileWriter(friends));
			save.println(
				JsonUtils.prettyGson.toJson(WurstClient.INSTANCE.friends));
			save.close();
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void loadFriends()
	{
		try
		{
			BufferedReader load = new BufferedReader(new FileReader(friends));
			WurstClient.INSTANCE.friends =
				JsonUtils.gson.fromJson(load, FriendsList.class);
			load.close();
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void saveXRayBlocks()
	{
		try
		{
			XRayUtils.sortBlocks();
			JsonArray json = new JsonArray();
			for(int i = 0; i < XRayMod.xrayBlocks.size(); i++)
				json.add(JsonUtils.prettyGson.toJsonTree(
					Block.getIdFromBlock(XRayMod.xrayBlocks.get(i))));
			PrintWriter save = new PrintWriter(new FileWriter(xray));
			save.println(JsonUtils.prettyGson.toJson(json));
			save.close();
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void loadXRayBlocks()
	{
		try
		{
			BufferedReader load = new BufferedReader(new FileReader(xray));
			JsonArray json = JsonUtils.jsonParser.parse(load).getAsJsonArray();
			load.close();
			Iterator<JsonElement> itr = json.iterator();
			while(itr.hasNext())
				try
				{
					String jsonBlock = itr.next().getAsString();
					XRayMod.xrayBlocks.add(Block.getBlockFromName(jsonBlock));
				}catch(Exception e)
				{
					e.printStackTrace();
				}
			XRayUtils.sortBlocks();
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void createDefaultAutoBuildTemplates()
	{
		try
		{
			String[] comment =
				{"Copyright © 2014 - 2017 | Wurst-Imperium | All rights reserved.",
					"This Source Code Form is subject to the terms of the Mozilla Public",
					"License, v. 2.0. If a copy of the MPL was not distributed with this",
					"file, You can obtain one at http://mozilla.org/MPL/2.0/."};
			Iterator<Entry<String, int[][]>> itr =
				new DefaultAutoBuildTemplates().entrySet().iterator();
			while(itr.hasNext())
			{
				Entry<String, int[][]> entry = itr.next();
				JsonObject json = new JsonObject();
				json.add("__comment",
					JsonUtils.prettyGson.toJsonTree(comment, String[].class));
				json.add("blocks", JsonUtils.prettyGson
					.toJsonTree(entry.getValue(), int[][].class));
				PrintWriter save = new PrintWriter(new FileWriter(new File(
					WurstFolders.AUTOBUILD, entry.getKey() + ".json")));
				save.println(JsonUtils.prettyGson.toJson(json));
				save.close();
			}
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void loadAutoBuildTemplates()
	{
		File[] files = WurstFolders.AUTOBUILD.listFiles();
		
		boolean foundOldTemplates = false;
		TreeMap<String, int[][]> templates = new TreeMap<>();
		for(File file : files)
			try
			{
				// read file
				FileReader reader = new FileReader(file);
				JsonObject json =
					JsonUtils.jsonParser.parse(reader).getAsJsonObject();
				reader.close();
				
				// get blocks
				int[][] blocks =
					JsonUtils.gson.fromJson(json.get("blocks"), int[][].class);
				
				// delete file if old template is found
				if(blocks[0].length == 4)
				{
					foundOldTemplates = true;
					file.delete();
					continue;
				}
				
				// add template
				templates.put(file.getName().substring(0,
					file.getName().lastIndexOf(".json")), blocks);
			}catch(Exception e)
			{
				System.err
					.println("Failed to load template: " + file.getName());
				e.printStackTrace();
			}
			
		// if directory is empty or contains old templates,
		// add default templates and try again
		if(foundOldTemplates || WurstFolders.AUTOBUILD.listFiles().length == 0)
		{
			createDefaultAutoBuildTemplates();
			loadAutoBuildTemplates();
			return;
		}
		
		if(templates.isEmpty())
			throw new JsonParseException(
				"Couldn't load any AutoBuild templates.");
		
		WurstClient.INSTANCE.mods.autoBuildMod.setTemplates(templates);
	}
}
