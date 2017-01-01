/*
 * Copyright © 2014 - 2017 | Wurst-Imperium | All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.features.mods;

import net.minecraft.entity.projectile.EntityFishHook;
import tk.wurst_client.events.listeners.UpdateListener;

@Mod.Info(description = "Automatically catches fish.",
	name = "AutoFish",
	tags = "FishBot, auto fish, fish bot, fishing",
	help = "Mods/AutoFish")
@Mod.Bypasses
public class AutoFishMod extends Mod implements UpdateListener
{
	private int timer;
	
	@Override
	public void onEnable()
	{
		wurst.events.add(UpdateListener.class, this);
	}
	
	@Override
	public void onDisable()
	{
		wurst.events.remove(UpdateListener.class, this);
		timer = 0;
	}
	
	@Override
	public void onUpdate()
	{
		switch(timer)
		{
			case 0:
				EntityFishHook hook = mc.player.fishEntity;
				if(hook != null && hook.motionX == 0.0 && hook.motionZ == 0.0
					&& hook.motionY <= -0.24)
				{
					mc.rightClickMouse();
					timer = 20;
				}
				break;
			
			case 1:
				mc.rightClickMouse();
			default:
				timer--;
				break;
		}
	}
}
