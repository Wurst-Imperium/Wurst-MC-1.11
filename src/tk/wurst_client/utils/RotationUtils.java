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
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class RotationUtils
{
	private static final Minecraft mc = Minecraft.getMinecraft();
	
	public static boolean lookChanged;
	public static float yaw;
	public static float pitch;
	
	private static float[] getNeededRotations(Vec3d vec)
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
	
	public static float limitAngleChange(float current, float intended,
		float maxChange)
	{
		float change = MathHelper.wrapDegrees(intended - current);
		
		change = MathHelper.clamp(change, -maxChange, maxChange);
		
		return MathHelper.wrapDegrees(current + change);
	}
	
	public static boolean faceVectorPacket(Vec3d vec)
	{
		lookChanged = true;
		
		float[] rotations = getNeededRotations(vec);
		
		float oldYaw = yaw;
		float oldPitch = pitch;
		
		yaw = limitAngleChange(oldYaw, rotations[0], 30);
		pitch = rotations[1];
		
		return Math.abs(oldYaw - rotations[0])
			+ Math.abs(oldPitch - rotations[1]) < 1F;
	}
	
	public static boolean faceVectorClient(Vec3d vec)
	{
		lookChanged = false;
		
		float[] rotations = getNeededRotations(vec);
		
		float oldYaw = mc.player.prevRotationYaw;
		float oldPitch = mc.player.prevRotationPitch;
		
		mc.player.rotationYaw = limitAngleChange(oldYaw, rotations[0], 30);
		mc.player.rotationPitch = rotations[1];
		
		return Math.abs(oldYaw - rotations[0])
			+ Math.abs(oldPitch - rotations[1]) < 1F;
	}
	
	public static boolean faceEntityClient(Entity entity)
	{
		// get eyesPos
		Vec3d eyesPos = new Vec3d(mc.player.posX,
			mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ);
		
		// get lookVec
		float f = MathHelper.cos(-yaw * 0.017453292F - (float)Math.PI);
		float f1 = MathHelper.sin(-yaw * 0.017453292F - (float)Math.PI);
		float f2 = -MathHelper.cos(-pitch * 0.017453292F);
		float f3 = MathHelper.sin(-pitch * 0.017453292F);
		Vec3d lookVec = new Vec3d(f1 * f2, f3, f * f2);
		
		// try to face center of boundingBox
		AxisAlignedBB bb = entity.boundingBox;
		if(faceVectorClient(bb.getCenter()))
			return true;
		
		// if not facing center, check if facing anything in boundingBox
		return bb.calculateIntercept(eyesPos,
			eyesPos.add(lookVec.scale(6))) != null;
	}
	
	public static boolean faceEntityPacket(Entity entity)
	{
		// get eyesPos
		Vec3d eyesPos = new Vec3d(mc.player.posX,
			mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ);
		
		// get lookVec
		float f = MathHelper.cos(-yaw * 0.017453292F - (float)Math.PI);
		float f1 = MathHelper.sin(-yaw * 0.017453292F - (float)Math.PI);
		float f2 = -MathHelper.cos(-pitch * 0.017453292F);
		float f3 = MathHelper.sin(-pitch * 0.017453292F);
		Vec3d lookVec = new Vec3d(f1 * f2, f3, f * f2);
		
		// try to face center of boundingBox
		AxisAlignedBB bb = entity.boundingBox;
		if(faceVectorPacket(bb.getCenter()))
			return true;
		
		// if not facing center, check if facing anything in boundingBox
		return bb.calculateIntercept(eyesPos,
			eyesPos.add(lookVec.scale(6))) != null;
	}
	
	public static float getDistanceFromRotation(Vec3d vec)
	{
		float[] needed = getNeededRotations(vec);
		
		float diffYaw = mc.player.rotationYaw - needed[0];
		float diffPitch = mc.player.rotationPitch - needed[1];
		
		float distance =
			MathHelper.sqrt(diffYaw * diffYaw + diffPitch * diffPitch);
		
		return distance;
	}
}
