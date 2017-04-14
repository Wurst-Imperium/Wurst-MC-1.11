/*
 * Copyright © 2014 - 2016 | Wurst-Imperium | All rights reserved.
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.mods;

import net.minecraft.entity.projectile.EntityFishHook;
import tk.wurst_client.events.listeners.UpdateListener;
import tk.wurst_client.mods.Mod.Bypasses;
import tk.wurst_client.mods.Mod.Info;
import tk.wurst_client.utils.MiscUtils;

@Info(
	description = "Automatically catches fish.",
	name = "AutoFish",
	tags = "FishBot, auto fish, fish bot, fishing",
	help = "Mods/AutoFish")
@Bypasses
public class AutoFishMod extends Mod implements UpdateListener
{
	private boolean catching = false;
	private int lastY = 0;
	
	@Override
	public void onEnable()
	{
		wurst.events.add(UpdateListener.class, this);
	}
	
	@Override
	public void onUpdate()
	{
		if (mc.thePlayer.fishEntity != null && !catching) {
			try {
				if (mc.thePlayer.fishEntity != null && !catching) {
					if (lastY == 0) {
						lastY = (int)mc.thePlayer.fishEntity.posY;
					}
					if (lastY != (int)mc.thePlayer.fishEntity.posY) {
						lastY = (int)mc.thePlayer.fishEntity.posY;
						catching = true;
						new Thread() {
							@Override
							public void run() {
								mc.rightClickMouse();
								try {
									Thread.sleep(1000);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
								mc.rightClickMouse();
								try {
									Thread.sleep(1000);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
								lastY = 0;
								catching = false;
							}
						}.start();
					}
				}
			} catch (Exception ex) {
				// Probably if you had the mod on when closing mc and starting
				// it, it will crash when joining a world
				lastY = 0;
				catching = false;
			}
		}
	}
	
	@Override
	public void onDisable()
	{
		wurst.events.remove(UpdateListener.class, this);
	}
	
	private boolean isHooked(EntityFishHook hook)
	{
		return hook.motionX == 0.0D && hook.motionZ == 0.0D
			&& hook.motionY != 0.0D;
	}
}
