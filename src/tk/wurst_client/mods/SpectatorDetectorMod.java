/*
 * Copyright © 2014 - 2016 | Wurst-Imperium | All rights reserved.
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.mods;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.world.GameType;
import tk.wurst_client.events.listeners.UpdateListener;
import tk.wurst_client.mods.Mod.Bypasses;
import tk.wurst_client.mods.Mod.Info;
import tk.wurst_client.navigator.NavigatorItem;

@Info(
	description = "Tells you when a player is in spectator mode.\n"
		+ "Works in vanilla only.\n\n",
	name = "SpectatorDectector",
	tags = "SpectatorDetector, spectator dectector",
	help = "Mods/SpectatorDectector")
@Bypasses
public class SpectatorDetectorMod extends Mod implements UpdateListener
{
	private Map<String, String> spectators = new TreeMap<String, String>();
	
	@Override
	public void initSettings()
	{
		;
	}
	
	@Override
	public String getRenderName()
	{
		return "SpectatorDectector";
	}
	
	@Override
	public NavigatorItem[] getSeeAlso()
	{
		return new NavigatorItem[]{};
	}
	
	@Override
	public void onEnable()
	{
		wurst.events.add(UpdateListener.class, this);
	}
	
	@SuppressWarnings("static-access")
	@Override
	public void onUpdate()
	{
		List<NetworkPlayerInfo> list =
			mc.ingameGUI.overlayPlayerList.ENTRY_ORDERING
				.<NetworkPlayerInfo> sortedCopy(
					mc.player.connection.getPlayerInfoMap());
		
		for(NetworkPlayerInfo networkplayerinfo : list)
		{
			String name = networkplayerinfo.getPlayerNameForReal();
			if(!spectators.containsKey(name)
				&& networkplayerinfo.getGameType() == GameType.SPECTATOR)
			{
				spectators.put(name, name);
				wurst.chat.info("Player " + name + "is in spectator mode.");
			}else
			{
				if(spectators.containsKey(name))
				{
					spectators.remove(name);
				}
				/*
				 * try
				 * {
				 * spectators.remove(name);
				 * }
				 * catch(Exception e)
				 * {
				 * System.out.println(e);
				 * }
				 */
			}
		}
	}
	
	@Override
	public void onDisable()
	{
		wurst.events.remove(UpdateListener.class, this);
	}
	
}
