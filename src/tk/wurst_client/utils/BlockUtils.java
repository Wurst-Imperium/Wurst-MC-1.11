/*
 * Copyright © 2014 - 2016 | Wurst-Imperium | All rights reserved.
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.utils;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public final class BlockUtils
{
	private static final Minecraft mc = Minecraft.getMinecraft();
	
	public static Block getBlock(BlockPos pos)
	{
		return mc.world.getBlockState(pos).getBlock();
	}
	
	public static Material getMaterial(BlockPos pos)
	{
		return mc.world.getBlockState(pos).getMaterial();
	}
	
	public static void faceBlockClient(BlockPos blockPos)
	{
		double diffX = blockPos.getX() + 0.5 - mc.player.posX;
		double diffY =
			blockPos.getY() + 0.5 - (mc.player.posY + mc.player.getEyeHeight());
		double diffZ = blockPos.getZ() + 0.5 - mc.player.posZ;
		double dist = MathHelper.sqrt(diffX * diffX + diffZ * diffZ);
		float yaw =
			(float)(Math.atan2(diffZ, diffX) * 180.0D / Math.PI) - 90.0F;
		float pitch = (float)-(Math.atan2(diffY, dist) * 180.0D / Math.PI);
		mc.player.rotationYaw = mc.player.rotationYaw
			+ MathHelper.wrapDegrees(yaw - mc.player.rotationYaw);
		mc.player.rotationPitch = mc.player.rotationPitch
			+ MathHelper.wrapDegrees(pitch - mc.player.rotationPitch);
	}
	
	public static void faceBlockPacket(BlockPos blockPos)
	{
		double diffX = blockPos.getX() + 0.5 - mc.player.posX;
		double diffY =
			blockPos.getY() + 0.5 - (mc.player.posY + mc.player.getEyeHeight());
		double diffZ = blockPos.getZ() + 0.5 - mc.player.posZ;
		double dist = MathHelper.sqrt(diffX * diffX + diffZ * diffZ);
		float yaw =
			(float)(Math.atan2(diffZ, diffX) * 180.0D / Math.PI) - 90.0F;
		float pitch = (float)-(Math.atan2(diffY, dist) * 180.0D / Math.PI);
		mc.player.connection.sendPacket(new CPacketPlayer.Rotation(
			mc.player.rotationYaw
				+ MathHelper.wrapDegrees(yaw - mc.player.rotationYaw),
			mc.player.rotationPitch
				+ MathHelper.wrapDegrees(pitch - mc.player.rotationPitch),
			mc.player.onGround));
	}
	
	public static void faceBlockClientHorizontally(BlockPos blockPos)
	{
		double diffX = blockPos.getX() + 0.5 - mc.player.posX;
		double diffZ = blockPos.getZ() + 0.5 - mc.player.posZ;
		float yaw =
			(float)(Math.atan2(diffZ, diffX) * 180.0D / Math.PI) - 90.0F;
		mc.player.rotationYaw = mc.player.rotationYaw
			+ MathHelper.wrapDegrees(yaw - mc.player.rotationYaw);
	}
	
	public static float getPlayerBlockDistance(BlockPos blockPos)
	{
		return getPlayerBlockDistance(blockPos.getX(), blockPos.getY(),
			blockPos.getZ());
	}
	
	public static float getPlayerBlockDistance(double posX, double posY,
		double posZ)
	{
		float xDiff = (float)(mc.player.posX - posX);
		float yDiff = (float)(mc.player.posY - posY);
		float zDiff = (float)(mc.player.posZ - posZ);
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
		float xDiff = (float)(mc.player.posX - blockPos.getX());
		float zDiff = (float)(mc.player.posZ - blockPos.getZ());
		return MathHelper.sqrt(
			(xDiff - 0.5F) * (xDiff - 0.5F) + (zDiff - 0.5F) * (zDiff - 0.5F));
	}
}
