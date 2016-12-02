/*
 * Copyright © 2014 - 2016 | Wurst-Imperium | All rights reserved.
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.ai;

import net.minecraft.entity.Entity;

public class FollowAI
{
	private EntityPathFinder pathFinder;
	private PathProcessor processor;
	
	private boolean done;
	private boolean failed;
	
	public FollowAI(Entity entity, float range)
	{
		pathFinder = new EntityPathFinder(entity, range);
		pathFinder.setThinkTime(50);
	}
	
	public void update()
	{
		// find path
		if(!pathFinder.isDone())
		{
			if(processor != null)
				processor.lockControls();
			
			pathFinder.think();
			
			if(!pathFinder.isDone())
			{
				if(pathFinder.isFailed())
					failed = true;
				
				return;
			}
			
			pathFinder.formatPath();
			
			// set processor
			processor = pathFinder.getProcessor();
		}
		
		// check path
		if(processor != null
			&& !pathFinder.isPathStillValid(processor.getIndex()))
		{
			pathFinder = new EntityPathFinder(pathFinder);
			return;
		}
		
		// process path
		processor.process();
		
		if(processor.isFailed())
			failed = true;
		
		if(processor.isDone())
			done = true;
	}
	
	public void stop()
	{
		if(processor != null)
			processor.stop();
	}
	
	public final boolean isDone()
	{
		return done;
	}
	
	public final boolean isFailed()
	{
		return failed;
	}
}
