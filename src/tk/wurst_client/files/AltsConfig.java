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
import java.nio.file.Files;
import java.util.Map.Entry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import tk.wurst_client.alts.Alt;
import tk.wurst_client.alts.Encryption;
import tk.wurst_client.gui.alts.GuiAltList;
import tk.wurst_client.utils.JsonUtils;

public final class AltsConfig extends Config
{
	public AltsConfig()
	{
		super("alts.json");
	}
	
	@Override
	protected JsonElement readFile(File file) throws IOException
	{
		return JsonUtils.jsonParser.parse(Encryption.decrypt(
			new String(Files.readAllBytes(file.toPath()), Encryption.CHARSET)));
	}
	
	@Override
	protected void writeFile(File file, JsonElement json) throws IOException
	{
		Files.write(file.toPath(),
			Encryption.encrypt(JsonUtils.prettyGson.toJson(json))
				.getBytes(Encryption.CHARSET));
	}
	
	@Override
	protected void loadFromJson(JsonElement json)
	{
		GuiAltList.alts.clear();
		
		for(Entry<String, JsonElement> entry : json.getAsJsonObject()
			.entrySet())
		{
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
	}
	
	@Override
	protected JsonElement saveToJson()
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
		
		return json;
	}
}
