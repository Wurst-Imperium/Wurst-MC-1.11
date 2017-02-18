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
import java.nio.file.Files;
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
import tk.wurst_client.alts.Alt;
import tk.wurst_client.alts.Encryption;
import tk.wurst_client.features.mods.XRayMod;
import tk.wurst_client.gui.alts.GuiAltList;
import tk.wurst_client.navigator.Navigator;
import tk.wurst_client.options.FriendsList;
import tk.wurst_client.settings.Setting;
import tk.wurst_client.utils.JsonUtils;
import tk.wurst_client.utils.XRayUtils;

public class FileManager
{
	public final File alts = new File(WurstFolders.MAIN, "alts.json");
	public final File friends = new File(WurstFolders.MAIN, "friends.json");
	public final File navigatorData =
		new File(WurstFolders.MAIN, "navigator.json");
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
		
		if(!navigatorData.exists())
			saveNavigatorData();
		else
			loadNavigatorData();
		if(!alts.exists())
			saveAlts();
		else
			loadAlts();
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
	
	public void saveNavigatorData()
	{
		try
		{
			JsonObject json = new JsonObject();
			
			Navigator navigator = WurstClient.INSTANCE.navigator;
			navigator.forEach((item) -> {
				JsonObject jsonFeature = new JsonObject();
				
				long preference = navigator.getPreference(item.getName());
				if(preference != 0L)
					jsonFeature.addProperty("preference", preference);
				
				if(!item.getSettings().isEmpty())
				{
					JsonObject jsonSettings = new JsonObject();
					for(Setting setting : item.getSettings())
						try
						{
							setting.save(jsonSettings);
						}catch(Exception e)
						{
							e.printStackTrace();
						}
					jsonFeature.add("settings", jsonSettings);
				}
				
				if(!jsonFeature.entrySet().isEmpty())
					json.add(item.getName(), jsonFeature);
			});
			
			PrintWriter save = new PrintWriter(new FileWriter(navigatorData));
			save.println(JsonUtils.prettyGson.toJson(json));
			save.close();
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void loadNavigatorData()
	{
		try
		{
			BufferedReader load =
				new BufferedReader(new FileReader(navigatorData));
			JsonObject json = (JsonObject)JsonUtils.jsonParser.parse(load);
			load.close();
			
			Navigator navigator = WurstClient.INSTANCE.navigator;
			navigator.forEach((item) -> {
				String itemName = item.getName();
				if(!json.has(itemName))
					return;
				JsonObject jsonFeature = json.get(itemName).getAsJsonObject();
				
				if(jsonFeature.has("preference"))
					navigator.setPreference(itemName,
						jsonFeature.get("preference").getAsLong());
				
				if(jsonFeature.has("settings"))
				{
					JsonObject jsonSettings =
						jsonFeature.get("settings").getAsJsonObject();
					for(Setting setting : item.getSettings())
						try
						{
							setting.load(jsonSettings);
						}catch(Exception e)
						{
							e.printStackTrace();
						}
				}
			});
		}catch(Exception e)
		{
			e.printStackTrace();
		}
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
	
	public void saveAlts()
	{
		try
		{
			JsonObject json = new JsonObject();
			for(Alt alt : GuiAltList.alts)
			{
				JsonObject jsonAlt = new JsonObject();
				jsonAlt.addProperty("password", alt.getPassword());
				jsonAlt.addProperty("name", alt.getName());
				jsonAlt.addProperty("starred", alt.isStarred());
				json.add(alt.getEmail(), jsonAlt);
			}
			Files.write(alts.toPath(),
				Encryption.encrypt(JsonUtils.prettyGson.toJson(json))
					.getBytes(Encryption.CHARSET));
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void loadAlts()
	{
		try
		{
			JsonObject json = (JsonObject)JsonUtils.jsonParser.parse(
				Encryption.decrypt(new String(Files.readAllBytes(alts.toPath()),
					Encryption.CHARSET)));
			GuiAltList.alts.clear();
			Iterator<Entry<String, JsonElement>> itr =
				json.entrySet().iterator();
			while(itr.hasNext())
			{
				Entry<String, JsonElement> entry = itr.next();
				JsonObject jsonAlt = entry.getValue().getAsJsonObject();
				
				String email = entry.getKey();
				String password = jsonAlt.get("password") == null ? ""
					: jsonAlt.get("password").getAsString();
				String name = jsonAlt.get("name") == null ? ""
					: jsonAlt.get("name").getAsString();
				boolean starred = jsonAlt.get("starred") == null ? false
					: jsonAlt.get("starred").getAsBoolean();
				
				GuiAltList.alts.add(new Alt(email, password, name, starred));
			}
			GuiAltList.sortAlts();
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
