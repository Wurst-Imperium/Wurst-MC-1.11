/*
 * Copyright © 2014 - 2016 | Wurst-Imperium | All rights reserved.
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class BlockUtils
{
	public static void faceBlockClient(BlockPos blockPos)
	{
		double diffX =
			blockPos.getX() + 0.5 - Minecraft.getMinecraft().player.posX;
		double diffY =
			blockPos.getY() + 0.5 - (Minecraft.getMinecraft().player.posY
				+ Minecraft.getMinecraft().player.getEyeHeight());
		double diffZ =
			blockPos.getZ() + 0.5 - Minecraft.getMinecraft().player.posZ;
		double dist = MathHelper.sqrt(diffX * diffX + diffZ * diffZ);
		float yaw =
			(float)(Math.atan2(diffZ, diffX) * 180.0D / Math.PI) - 90.0F;
		float pitch = (float)-(Math.atan2(diffY, dist) * 180.0D / Math.PI);
		Minecraft.getMinecraft().player.rotationYaw =
			Minecraft.getMinecraft().player.rotationYaw + MathHelper
				.wrapDegrees(yaw - Minecraft.getMinecraft().player.rotationYaw);
		Minecraft.getMinecraft().player.rotationPitch =
			Minecraft.getMinecraft().player.rotationPitch
				+ MathHelper.wrapDegrees(
					pitch - Minecraft.getMinecraft().player.rotationPitch);
	}
	
	public static void faceBlockPacket(BlockPos blockPos)
	{
		double diffX =
			blockPos.getX() + 0.5 - Minecraft.getMinecraft().player.posX;
		double diffY =
			blockPos.getY() + 0.5 - (Minecraft.getMinecraft().player.posY
				+ Minecraft.getMinecraft().player.getEyeHeight());
		double diffZ =
			blockPos.getZ() + 0.5 - Minecraft.getMinecraft().player.posZ;
		double dist = MathHelper.sqrt(diffX * diffX + diffZ * diffZ);
		float yaw =
			(float)(Math.atan2(diffZ, diffX) * 180.0D / Math.PI) - 90.0F;
		float pitch = (float)-(Math.atan2(diffY, dist) * 180.0D / Math.PI);
		Minecraft.getMinecraft().player.connection
			.sendPacket(new CPacketPlayer.Rotation(
				Minecraft.getMinecraft().player.rotationYaw
					+ MathHelper.wrapDegrees(
						yaw - Minecraft.getMinecraft().player.rotationYaw),
				Minecraft.getMinecraft().player.rotationPitch
					+ MathHelper.wrapDegrees(
						pitch - Minecraft.getMinecraft().player.rotationPitch),
				Minecraft.getMinecraft().player.onGround));
	}
	
	public static void faceBlockClientHorizontally(BlockPos blockPos)
	{
		double diffX =
			blockPos.getX() + 0.5 - Minecraft.getMinecraft().player.posX;
		double diffZ =
			blockPos.getZ() + 0.5 - Minecraft.getMinecraft().player.posZ;
		float yaw =
			(float)(Math.atan2(diffZ, diffX) * 180.0D / Math.PI) - 90.0F;
		Minecraft.getMinecraft().player.rotationYaw =
			Minecraft.getMinecraft().player.rotationYaw + MathHelper
				.wrapDegrees(yaw - Minecraft.getMinecraft().player.rotationYaw);
	}
	
	public static float getPlayerBlockDistance(BlockPos blockPos)
	{
		return getPlayerBlockDistance(blockPos.getX(), blockPos.getY(),
			blockPos.getZ());
	}
	
	public static float getPlayerBlockDistance(double posX, double posY,
		double posZ)
	{
		float xDiff = (float)(Minecraft.getMinecraft().player.posX - posX);
		float yDiff = (float)(Minecraft.getMinecraft().player.posY - posY);
		float zDiff = (float)(Minecraft.getMinecraft().player.posZ - posZ);
		return getBlockDistance(xDiff, yDiff, zDiff);
	}
	
	public static float getBlockDistance(float xDiff, float yDiff, float zDiff)
	{
		return MathHelper.sqrt(
			(xDiff - 0.5F) * (xDiff - 0.5F) + (yDiff - 0.5F) * (yDiff - 0.5F)
				+ (zDiff - 0.5F) * (zDiff - 0.5F));
	}
	
	public static float getHorizontalPlayerBlockDistance(BlockPos blockPos)
	{
		float xDiff =
			(float)(Minecraft.getMinecraft().player.posX - blockPos.getX());
		float zDiff =
			(float)(Minecraft.getMinecraft().player.posZ - blockPos.getZ());
		return MathHelper.sqrt(
			(xDiff - 0.5F) * (xDiff - 0.5F) + (zDiff - 0.5F) * (zDiff - 0.5F));
	}
	
	/**
	 * Determines whether a block is in a specified cuboid.
	 * @param bound1 The first corner of the cuboid.
	 * @param bound2 The second corner of the cuboid.
	 * @param pos The position to check.
	 * @return true if the block is within the bounds
	 */
	public static  boolean inBounds(BlockPos bound1, BlockPos bound2, BlockPos pos)
	{
		return (isBetween(bound1.getX(), pos.getX(), bound2.getX())
		&& isBetween(bound1.getY(), pos.getY(), bound2.getY())
		&& isBetween(bound1.getZ(), pos.getZ(), bound2.getZ())
		);
	}
	
	/**
	 * Simple helper
	 * @param i1
	 * @param middle The middle number
	 * @param i2
	 * @return
	 */
	public static boolean isBetween(int i1, int middle, int i2)
	{
		return ((i1 >= i2) ? (i1 >= middle && middle >= i2) : (i1 <= middle && middle <= i2));
	}
}
