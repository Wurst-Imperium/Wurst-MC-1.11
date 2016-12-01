/*
 * Copyright © 2014 - 2016 | Wurst-Imperium | All rights reserved.
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.commands;

import tk.wurst_client.commands.Cmd.Info;
import tk.wurst_client.mods.BoundedNukerMod;

@Info(description = "Manages bounds for the BoundableNuker.",
	name = "bound",
	syntax = {"", "clear", "clear1, clear2", "here1, here2",
		"looking1, looking2"},
	help = "Commands/bound")
public class BoundCmd extends Cmd
{
	@Override
	public void execute(String[] args) throws Error
	{
		if(args.length == 0)
		{
			syntaxError();
		}
		BoundedNukerMod nuker = wurst.mods.boundedNukerMod;
		String par = args[0];
		if(args.length == 2)
		{
			par = par + args[1];
		}else if(args.length != 1)
		{
			syntaxError();
		}
		switch(par)
		{
			case "clear":
				nuker.resetBounds();
			case "clear1":
				nuker.setBound(null, 1);
			case "clear2":
				nuker.setBound(null, 2);
			case "here1":
				nuker.setBound(mc.player.getPosition(), 1);
			case "here2":
				nuker.setBound(mc.player.getPosition(), 2);
			case "looking1":
				nuker.setBound(mc.objectMouseOver.getBlockPos(), 1);
			case "looking2":
				nuker.setBound(mc.objectMouseOver.getBlockPos(), 2);
			default:
				syntaxError();
		}
		
	}
	
}
