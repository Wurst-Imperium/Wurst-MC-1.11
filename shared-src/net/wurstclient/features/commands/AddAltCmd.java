/*
 * Copyright � 2014 - 2017 | Wurst-Imperium | All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.wurstclient.features.commands;

import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.util.StringUtils;
import net.wurstclient.altmanager.Alt;
import net.wurstclient.altmanager.screens.GuiAltList;
import net.wurstclient.compatibility.WMinecraft;
import net.wurstclient.features.Cmd;
import net.wurstclient.files.ConfigFiles;
import net.wurstclient.utils.ChatUtils;

@Cmd.Info(help = "Commands/addalt")
public final class AddAltCmd extends Cmd
{
	public AddAltCmd()
	{
		super("addalt",
			"Adds a player or all players on a server to your alt list.",
			"<player>", "all");
	}
	
	@Override
	public void execute(String[] args) throws CmdError
	{
		if(args.length != 1)
			syntaxError();
		
		if(args[0].equals("all"))
		{
			int alts = 0;
			for(NetworkPlayerInfo info : WMinecraft.getConnection()
				.getPlayerInfoMap())
			{
				String name =
					StringUtils.stripControlCodes(info.getPlayerNameForReal());
				
				if(name.equals(WMinecraft.getPlayer().getName())
					|| name.equals("Alexander01998")
					|| GuiAltList.alts.contains(new Alt(name, null, null)))
					continue;
				
				GuiAltList.alts.add(new Alt(name, null, null));
				alts++;
			}
			
			if(alts == 1)
				ChatUtils.message("Added 1 alt.");
			else
				ChatUtils.message("Added " + alts + " alts.");
			
			GuiAltList.sortAlts();
			ConfigFiles.ALTS.save();
			
		}else if(!args[0].equals("Alexander01998"))
		{
			GuiAltList.alts.add(new Alt(args[0], null, null));
			
			GuiAltList.sortAlts();
			ConfigFiles.ALTS.save();
			
			ChatUtils.message("Added 1 alt.");
		}
	}
}
