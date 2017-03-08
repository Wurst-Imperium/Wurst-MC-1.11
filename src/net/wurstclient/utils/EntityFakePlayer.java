/*
 * Copyright © 2014 - 2017 | Wurst-Imperium | All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.wurstclient.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;

public class EntityFakePlayer extends EntityOtherPlayerMP
{
	private static Minecraft mc = Minecraft.getMinecraft();
	
	public EntityFakePlayer()
	{
		super(mc.world, mc.player.getGameProfile());
		copyLocationAndAnglesFrom(mc.player);
		
		// fix inventory
		clonePlayer(mc.player, true);
		
		// fix rotation
		rotationYawHead = mc.player.rotationYawHead;
		renderYawOffset = mc.player.renderYawOffset;
		
		// fix cape movement
		chasingPosX = posX;
		chasingPosY = posY;
		chasingPosZ = posZ;
		
		// spawn
		mc.world.addEntityToWorld(getEntityId(), this);
	}
	
	public void resetPlayerPosition()
	{
		mc.player.setPositionAndRotation(posX, posY, posZ, rotationYaw,
			rotationPitch);
	}
	
	public void despawn()
	{
		mc.world.removeEntityFromWorld(getEntityId());
	}
}
