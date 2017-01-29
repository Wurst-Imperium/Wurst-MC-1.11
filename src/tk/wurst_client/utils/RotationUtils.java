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
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class RotationUtils
{
	private static final Minecraft mc = Minecraft.getMinecraft();
	
	private static boolean fakeRotation;
	private static float serverYaw;
	private static float serverPitch;
	
	public static Vec3d getEyesPos()
	{
		return new Vec3d(mc.player.posX,
			mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ);
	}
	
	public static Vec3d getClientLookVec()
	{
		float f = MathHelper
			.cos(-mc.player.rotationYaw * 0.017453292F - (float)Math.PI);
		float f1 = MathHelper
			.sin(-mc.player.rotationYaw * 0.017453292F - (float)Math.PI);
		float f2 = -MathHelper.cos(-mc.player.rotationPitch * 0.017453292F);
		float f3 = MathHelper.sin(-mc.player.rotationPitch * 0.017453292F);
		return new Vec3d(f1 * f2, f3, f * f2);
	}
	
	public static Vec3d getServerLookVec()
	{
		float f = MathHelper.cos(-serverYaw * 0.017453292F - (float)Math.PI);
		float f1 = MathHelper.sin(-serverYaw * 0.017453292F - (float)Math.PI);
		float f2 = -MathHelper.cos(-serverPitch * 0.017453292F);
		float f3 = MathHelper.sin(-serverPitch * 0.017453292F);
		return new Vec3d(f1 * f2, f3, f * f2);
	}
	
	private static float[] getNeededRotations(Vec3d vec)
	{
		Vec3d eyesPos = getEyesPos();
		
		double diffX = vec.xCoord - eyesPos.xCoord;
		double diffY = vec.yCoord - eyesPos.yCoord;
		double diffZ = vec.zCoord - eyesPos.zCoord;
		
		double diffXZ = MathHelper.sqrt(diffX * diffX + diffZ * diffZ);
		
		float yaw = (float)Math.toDegrees(Math.atan2(diffZ, diffX)) - 90F;
		float pitch = (float)-Math.toDegrees(Math.atan2(diffY, diffXZ));
		
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
		// use fake rotation in next packet
		fakeRotation = true;
		
		float[] rotations = getNeededRotations(vec);
		
		float oldYaw = serverYaw;
		float oldPitch = serverPitch;
		
		serverYaw = limitAngleChange(oldYaw, rotations[0], 30);
		serverPitch = rotations[1];
		
		return Math.abs(oldYaw - rotations[0])
			+ Math.abs(oldPitch - rotations[1]) < 1F;
	}
	
	public static void faceVectorPacketInstant(Vec3d vec)
	{
		float[] rotations = getNeededRotations(vec);
		
		mc.player.connection.sendPacket(new CPacketPlayer.Rotation(rotations[0],
			rotations[1], mc.player.onGround));
	}
	
	public static boolean faceVectorClient(Vec3d vec)
	{
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
		// get position & rotation
		Vec3d eyesPos = getEyesPos();
		Vec3d lookVec = getServerLookVec();
		
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
		// get position & rotation
		Vec3d eyesPos = getEyesPos();
		Vec3d lookVec = getServerLookVec();
		
		// try to face center of boundingBox
		AxisAlignedBB bb = entity.boundingBox;
		if(faceVectorPacket(bb.getCenter()))
			return true;
		
		// if not facing center, check if facing anything in boundingBox
		return bb.calculateIntercept(eyesPos,
			eyesPos.add(lookVec.scale(6))) != null;
	}
	
	public static boolean faceVectorForWalking(Vec3d vec)
	{
		float[] rotations = getNeededRotations(vec);
		
		float oldYaw = mc.player.prevRotationYaw;
		
		mc.player.rotationYaw = limitAngleChange(oldYaw, rotations[0], 30);
		mc.player.rotationPitch = 0;
		
		return Math.abs(oldYaw - rotations[0]) < 1F;
	}
	
	public static float getAngleToClientRotation(Vec3d vec)
	{
		float[] needed = getNeededRotations(vec);
		
		float diffYaw = mc.player.rotationYaw - needed[0];
		float diffPitch = mc.player.rotationPitch - needed[1];
		
		float angle =
			MathHelper.sqrt(diffYaw * diffYaw + diffPitch * diffPitch);
		
		return angle;
	}
	
	public static float getAngleToServerRotation(Vec3d vec)
	{
		float[] needed = getNeededRotations(vec);
		
		float diffYaw = serverYaw - needed[0];
		float diffPitch = serverPitch - needed[1];
		
		float angle =
			MathHelper.sqrt(diffYaw * diffYaw + diffPitch * diffPitch);
		
		return angle;
	}
	
	public static void updateServerRotation()
	{
		// disable fake rotation in next packet unless manually enabled again
		if(fakeRotation)
		{
			fakeRotation = false;
			return;
		}
		
		// slowly synchronize server rotation with client
		serverYaw = limitAngleChange(serverYaw, mc.player.rotationYaw, 30);
		serverPitch = mc.player.rotationPitch;
	}
	
	public static float getServerYaw()
	{
		return serverYaw;
	}
	
	public static float getServerPitch()
	{
		return serverPitch;
	}
}
