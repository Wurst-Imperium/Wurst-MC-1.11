/*
 * Copyright © 2014 - 2016 | Wurst-Imperium | All rights reserved.
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.features.mods;

import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.Entity;
import tk.wurst_client.events.listeners.UpdateListener;
import tk.wurst_client.utils.ChatUtils;
import tk.wurst_client.utils.EntityUtils;
import tk.wurst_client.utils.EntityUtils.TargetSettings;

@Mod.Info(
	description = "Allows you to see the world as someone else.\n"
		+ "Use the .rv command to make it target a specific entity.",
	name = "RemoteView",
	tags = "remote view",
	help = "Mods/RemoteView")
@Mod.Bypasses
public class RemoteViewMod extends Mod implements UpdateListener
{
	private Entity entity = null;
	
	private double oldX;
	private double oldY;
	private double oldZ;
	private float oldYaw;
	private float oldPitch;
	private boolean wasInvisible;
	
	private TargetSettings targetSettingsFind = new TargetSettings()
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
	
	private TargetSettings targetSettingsKeep = new TargetSettings()
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
		
		@Override
		public boolean targetInvisiblePlayers()
		{
			return true;
		}
		
		@Override
		public boolean targetInvisibleMobs()
		{
			return true;
		}
	};
	
	@Override
	public void onEnable()
	{
		// find entity if not already set
		if(entity == null)
		{
			entity = EntityUtils.getClosestEntity(targetSettingsFind);
			
			// check if entity was found
			if(entity == null)
			{
				ChatUtils.message("There is no nearby entity.");
				setEnabled(false);
				return;
			}
		}
		
		// save old data
		oldX = mc.player.posX;
		oldY = mc.player.posY;
		oldZ = mc.player.posZ;
		oldYaw = mc.player.rotationYaw;
		oldPitch = mc.player.rotationPitch;
		wasInvisible = entity.isInvisibleToPlayer(mc.player);
		
		// activate NoClip
		mc.player.noClip = true;
		
		// spawn fake player
		EntityOtherPlayerMP fakePlayer =
			new EntityOtherPlayerMP(mc.world, mc.player.getGameProfile());
		fakePlayer.clonePlayer(mc.player, true);
		fakePlayer.copyLocationAndAnglesFrom(mc.player);
		fakePlayer.rotationYawHead = mc.player.rotationYawHead;
		mc.world.addEntityToWorld(-69, fakePlayer);
		
		// success message
		ChatUtils.message("Now viewing " + entity.getName() + ".");
		
		// add listener
		wurst.events.add(UpdateListener.class, this);
	}
	
	@Override
	public void onDisable()
	{
		// remove listener
		wurst.events.remove(UpdateListener.class, this);
		
		// reset entity
		if(entity != null)
		{
			ChatUtils.message("No longer viewing " + entity.getName() + ".");
			entity.setInvisible(wasInvisible);
			entity = null;
		}
		
		// reset player
		mc.player.noClip = false;
		mc.player.setPositionAndRotation(oldX, oldY, oldZ, oldYaw, oldPitch);
		
		// remove fake player
		mc.world.removeEntityFromWorld(-69);
	}
	
	public void onToggledByCommand(String viewName)
	{
		// set entity
		if(!isEnabled() && viewName != null && !viewName.isEmpty())
			entity =
				EntityUtils.getEntityWithName(viewName, targetSettingsFind);
		
		// toggle RemoteView
		toggle();
	}
	
	@Override
	public void onUpdate()
	{
		// validate entity
		if(!EntityUtils.isCorrectEntity(entity, targetSettingsKeep))
		{
			setEnabled(false);
			return;
		}
		
		// update position, rotation, etc.
		mc.player.copyLocationAndAnglesFrom(entity);
		mc.player.motionX = 0;
		mc.player.motionY = 0;
		mc.player.motionZ = 0;
		
		// set entity invisible
		entity.setInvisible(true);
	}
}
