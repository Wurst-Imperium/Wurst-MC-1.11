/*
 * Copyright © 2014 - 2017 | Wurst-Imperium | All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class RotationUtils
{
	private static final Minecraft mc = Minecraft.getMinecraft();
	
	public static boolean lookChanged;
	public static float yaw;
	public static float pitch;
	
	public static boolean faceEntityClient(Entity entity)
	{
		float[] rotations = getRotationsNeeded(entity.boundingBox.getCenter());
		
		mc.player.rotationYaw =
			limitAngleChange(mc.player.prevRotationYaw, rotations[0], 30);
		mc.player.rotationPitch = rotations[1];
		
		return mc.player.rotationYaw == rotations[0];
	}
	
	public static boolean faceEntityPacket(Entity entity)
	{
		float[] rotations = getRotationsNeeded(entity.boundingBox.getCenter());
		
		yaw = limitAngleChange(yaw, rotations[0], 30);
		pitch = rotations[1];
		
		return yaw == rotations[0];
	}
	
	private static float[] getRotationsNeeded(Vec3d vec)
	{
		double diffX = vec.xCoord - mc.player.posX;
		double diffY = vec.yCoord - (mc.player.posY + mc.player.getEyeHeight());
		double diffZ = vec.zCoord - mc.player.posZ;
		
		double dist = MathHelper.sqrt(diffX * diffX + diffZ * diffZ);
		
		float yaw = (float)Math.toDegrees(Math.atan2(diffZ, diffX)) - 90F;
		float pitch = (float)-Math.toDegrees(Math.atan2(diffY, dist));
		
		return new float[]{MathHelper.wrapDegrees(yaw),
			MathHelper.wrapDegrees(pitch)};
	}
	
	public final static float limitAngleChange(float current, float intended,
		float maxChange)
	{
		float change = intended - current;
		
		change = MathHelper.clamp(change, -maxChange, maxChange);
		
		return current + change;
	}
	
	public static float getDistanceFromRotation(Vec3d vec)
	{
		float[] needed = RotationUtils.getRotationsNeeded(vec);
		
		float diffYaw = mc.player.rotationYaw - needed[0];
		float diffPitch = mc.player.rotationPitch - needed[1];
		
		float distance =
			MathHelper.sqrt(diffYaw * diffYaw + diffPitch * diffPitch);
		
		return distance;
	}
}
