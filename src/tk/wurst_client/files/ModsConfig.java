/*
 * Copyright © 2014 - 2017 | Wurst-Imperium | All rights reserved.
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.files;

import java.util.HashSet;
import java.util.Map.Entry;

import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import tk.wurst_client.WurstClient;
import tk.wurst_client.features.mods.*;

public final class ModsConfig extends Config
{
	// TODO: @DontSaveState annotation on the affected mods
	private final HashSet<String> blacklist =
		Sets.newHashSet(AntiAfkMod.class.getName(), BlinkMod.class.getName(),
			AutoBuildMod.class.getName(), AutoSignMod.class.getName(),
			FightBotMod.class.getName(), FollowMod.class.getName(),
			ForceOpMod.class.getName(), FreecamMod.class.getName(),
			InvisibilityMod.class.getName(), LsdMod.class.getName(),
			MassTpaMod.class.getName(), NavigatorMod.class.getName(),
			ProtectMod.class.getName(), RemoteViewMod.class.getName(),
			SpammerMod.class.getName());
	
	public ModsConfig()
	{
		super("modules.json");
	}
	
	@Override
	protected void loadFromJson(JsonElement json)
	{
		for(Entry<String, JsonElement> entry : json.getAsJsonObject()
			.entrySet())
		{
			Mod mod = WurstClient.INSTANCE.mods.getModByName(entry.getKey());
			
			if(mod == null || isModBlacklisted(mod))
				continue;
			
			JsonObject jsonMod = entry.getValue().getAsJsonObject();
			
			if(jsonMod.get("enabled").getAsBoolean())
				mod.enableOnStartup();
		}
	}
	
	@Override
	protected JsonElement saveToJson()
	{
		JsonObject json = new JsonObject();
		
		for(Mod mod : WurstClient.INSTANCE.mods.getAllMods())
		{
			JsonObject jsonMod = new JsonObject();
			jsonMod.addProperty("enabled", mod.isEnabled());
			json.add(mod.getName(), jsonMod);
		}
		
		return json;
	}
	
	public boolean isModBlacklisted(Mod mod)
	{
		return blacklist.contains(mod.getClass().getName());
	}
}
