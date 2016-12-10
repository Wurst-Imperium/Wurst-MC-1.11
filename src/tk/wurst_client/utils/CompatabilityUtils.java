/*
 * Copyright © 2014 - 2016 | Wurst-Imperium | All rights reserved.
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.utils;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import tk.wurst_client.WurstClient;
import tk.wurst_client.mods.*;

public final class CompatabilityUtils
{
	private static final WurstClient wurst = WurstClient.INSTANCE;
	
	@SafeVarargs
	public static boolean disableModGroup(ModGroup group, Mod... exceptions)
	{
		try
		{
			List<Mod> toDisable = getGroup(group);
			for(Mod dontDisable : Lists.asList(null, exceptions))
				toDisable.remove(dontDisable);
			for(Mod mod : toDisable)
				mod.setEnabled(false);
		}catch(Exception e)
		{
			return false;
		}
		return true;
	}
	
	public static List<Mod> getGroup(ModGroup group)
	{
		List<Mod> list = Lists.newArrayList();
		if(group == ModGroup.NUKING)
		{
			list.add(wurst.mods.nukerMod);
			list.add(wurst.mods.speedNukerMod);
			list.add(wurst.mods.nukerLegitMod);
			list.add(wurst.mods.tunnellerMod);
			list.add(wurst.mods.speedTunnellerMod);
			list.add(wurst.mods.boundedNukerMod);
		}else if(group == ModGroup.KILLING)
		{
			list.add(wurst.mods.killauraMod);
		}
		return list;
	}
	
	public enum ModGroup
	{
		NUKING,
		KILLING;
	}
}
