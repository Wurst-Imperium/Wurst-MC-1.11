/*
 * Copyright © 2014 - 2016 | Wurst-Imperium | All rights reserved.
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.commands;

import net.minecraft.util.math.BlockPos;
import tk.wurst_client.ai.PathFinder;
import tk.wurst_client.commands.Cmd.Info;
import tk.wurst_client.events.listeners.UpdateListener;
import tk.wurst_client.utils.EntityUtils.TargetSettings;

@Info(description = "Walks or flies you to a specific location.",
	name = "goto",
	syntax = {"<x> <y> <z>", "<entity>", "-path"},
	help = "Commands/goto")
public class GoToCmd extends Cmd implements UpdateListener
{
	private PathFinder pathFinder;
	private boolean enabled;
	
	private TargetSettings targetSettings = new TargetSettings()
	{
		@Override
		public boolean targetFriends()
		{
			return true;
		}
		
		@Override
		public boolean targetBehindWalls()
		{
			return true;
		};
	};
	
	@Override
	public void execute(String[] args) throws Error
	{
		// disable if enabled
		if(enabled)
		{
			disable();
			
			if(args.length == 0)
				return;
		}
		
		// set PathFinder
		if(args.length == 1 && args[0].equals("-path"))
		{
			BlockPos goal = wurst.commands.pathCmd.getLastGoal();
			if(goal != null)
				pathFinder = new PathFinder(goal);
			else
				error("No previous position on .path.");
		}else
		{
			int[] goal = argsToPos(targetSettings, args);
			pathFinder =
				new PathFinder(new BlockPos(goal[0], goal[1], goal[2]));
		}
		
		// start
		enabled = true;
		wurst.events.add(UpdateListener.class, this);
		System.out.println("Finding path...");
	}
	
	@Override
	public void onUpdate()
	{
		// find path
		if(!pathFinder.isPathFound())
		{
			pathFinder.lockControls();
			pathFinder.process(1024);
			
			if(!pathFinder.isPathFound())
			{
				if(pathFinder.getQueueSize() == 0)
				{
					wurst.chat.error("Could not find a path.");
					disable();
				}
				
				return;
			}
			
			pathFinder.formatPath();
			System.out.println("Done");
		}
		
		if(!pathFinder.isPathStillValid())
		{
			System.out.println("Updating path...");
			pathFinder = new PathFinder(pathFinder.getGoal());
			return;
		}
		
		pathFinder.goToGoal();
		
		if(pathFinder.isGoalReached())
			disable();
	}
	
	private void disable()
	{
		wurst.events.remove(UpdateListener.class, this);
		pathFinder.releaseControls();
		enabled = false;
	}
	
	public boolean isActive()
	{
		return enabled;
	}
}
