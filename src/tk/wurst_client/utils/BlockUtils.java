/*
 * Copyright © 2014 - 2017 | Wurst-Imperium | All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.utils;

import java.util.ArrayDeque;
import java.util.HashSet;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public final class BlockUtils
{
	private static final Minecraft mc = Minecraft.getMinecraft();
	
	public static IBlockState getState(BlockPos pos)
	{
		return mc.world.getBlockState(pos);
	}
	
	public static Block getBlock(BlockPos pos)
	{
		return getState(pos).getBlock();
	}
	
	public static int getId(BlockPos pos)
	{
		return Block.getIdFromBlock(getBlock(pos));
	}
	
	public static Material getMaterial(BlockPos pos)
	{
		return getState(pos).getMaterial();
	}
	
	public static boolean canBeClicked(BlockPos pos)
	{
		return getBlock(pos).canCollideCheck(getState(pos), false);
	}
	
	public static float getHardness(BlockPos pos)
	{
		return getState(pos).getPlayerRelativeBlockHardness(mc.player, mc.world,
			pos);
	}
	
	private static void processRightClickBlock(BlockPos pos, EnumFacing side,
		Vec3d hitVec)
	{
		mc.playerController.processRightClickBlock(mc.player, mc.world, pos,
			side, hitVec, EnumHand.MAIN_HAND);
	}
	
	private static void swingArmClient()
	{
		mc.player.swingArm(EnumHand.MAIN_HAND);
	}
	
	private static void swingArmPacket()
	{
		mc.player.connection
			.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
	}
	
	public static boolean placeBlockLegit(BlockPos pos)
	{
		Vec3d eyesPos = RotationUtils.getEyesPos();
		
		for(EnumFacing side : EnumFacing.values())
		{
			BlockPos neighbor = pos.offset(side);
			
			Vec3d posVec = new Vec3d(pos).addVector(0.5, 0.5, 0.5);
			Vec3d hitVec =
				posVec.add(new Vec3d(side.getDirectionVec()).scale(0.5));
			
			// check if side is visible (facing away from player)
			if(eyesPos.squareDistanceTo(posVec) >= eyesPos
				.squareDistanceTo(hitVec))
				continue;
			
			// check if neighbor can be right clicked
			if(!canBeClicked(neighbor))
				continue;
			
			// check if hitVec is within range (4.25 blocks)
			if(eyesPos.squareDistanceTo(hitVec) > 18.0625)
				continue;
			
			// check line of sight
			if(mc.world.rayTraceBlocks(eyesPos, hitVec, false, true,
				false) != null)
				continue;
			
			// face block
			if(!RotationUtils.faceVectorPacket(hitVec))
				return true;
			
			// place block
			processRightClickBlock(neighbor, side.getOpposite(), hitVec);
			swingArmClient();
			mc.rightClickDelayTimer = 4;
			
			return true;
		}
		
		return false;
	}
	
	public static boolean placeBlockSimple(BlockPos pos)
	{
		Vec3d eyesPos = RotationUtils.getEyesPos();
		
		for(EnumFacing side : EnumFacing.values())
		{
			BlockPos neighbor = pos.offset(side);
			EnumFacing side2 = side.getOpposite();
			
			// check if neighbor can be right clicked
			if(!canBeClicked(neighbor))
				continue;
			
			Vec3d hitVec = new Vec3d(neighbor).addVector(0.5, 0.5, 0.5)
				.add(new Vec3d(side2.getDirectionVec()).scale(0.5));
			
			// check if hitVec is within range (6 blocks)
			if(eyesPos.squareDistanceTo(hitVec) > 36)
				continue;
			
			// place block
			processRightClickBlock(neighbor, side2, hitVec);
			
			return true;
		}
		
		return false;
	}
	
	public static boolean breakBlockLegit(BlockPos pos)
	{
		Vec3d eyesPos = RotationUtils.getEyesPos();
		
		for(EnumFacing side : EnumFacing.values())
		{
			Vec3d posVec = new Vec3d(pos).addVector(0.5, 0.5, 0.5);
			Vec3d hitVec =
				posVec.add(new Vec3d(side.getDirectionVec()).scale(0.5));
			
			// check if hitVec is within range (4.25 blocks)
			if(eyesPos.squareDistanceTo(hitVec) > 18.0625)
				continue;
			
			// check if side is facing towards player
			if(eyesPos.squareDistanceTo(posVec) <= eyesPos
				.squareDistanceTo(hitVec))
				continue;
			
			// check line of sight
			if(mc.world.rayTraceBlocks(eyesPos, hitVec, false, true,
				false) != null)
				continue;
			
			// face block
			if(!RotationUtils.faceVectorPacket(hitVec))
				return true;
			
			// damage block
			if(!mc.playerController.onPlayerDamageBlock(pos, side))
				return false;
			
			// swing arm
			swingArmPacket();
			
			return true;
		}
		
		return false;
	}
	
	public static boolean breakBlockSimple(BlockPos pos)
	{
		Vec3d eyesPos = RotationUtils.getEyesPos();
		
		for(EnumFacing side : EnumFacing.values())
		{
			Vec3d posVec = new Vec3d(pos).addVector(0.5, 0.5, 0.5);
			Vec3d hitVec =
				posVec.add(new Vec3d(side.getDirectionVec()).scale(0.5));
			
			// check if hitVec is within range (6 blocks)
			if(eyesPos.squareDistanceTo(hitVec) > 36)
				continue;
			
			// check if side is facing towards player
			if(eyesPos.squareDistanceTo(posVec) <= eyesPos
				.squareDistanceTo(hitVec))
				continue;
			
			// face block
			RotationUtils.faceVectorPacket(hitVec);
			
			// damage block
			if(!mc.playerController.onPlayerDamageBlock(pos, side))
				return false;
			
			// swing arm
			swingArmPacket();
			
			return true;
		}
		
		return false;
	}
	
	public static boolean rightClickBlockLegit(BlockPos pos)
	{
		Vec3d eyesPos = RotationUtils.getEyesPos();
		
		for(EnumFacing side : EnumFacing.values())
		{
			Vec3d posVec = new Vec3d(pos).addVector(0.5, 0.5, 0.5);
			Vec3d hitVec =
				posVec.add(new Vec3d(side.getDirectionVec()).scale(0.5));
			
			// check if hitVec is within range (4.25 blocks)
			if(eyesPos.squareDistanceTo(hitVec) > 18.0625)
				continue;
			
			// check if side is facing towards player
			if(eyesPos.squareDistanceTo(posVec) <= eyesPos
				.squareDistanceTo(hitVec))
				continue;
			
			// check line of sight
			if(mc.world.rayTraceBlocks(eyesPos, hitVec, false, true,
				false) != null)
				continue;
			
			// face block
			if(!RotationUtils.faceVectorPacket(hitVec))
				return true;
			
			// place block
			processRightClickBlock(pos, side, hitVec);
			swingArmClient();
			mc.rightClickDelayTimer = 4;
			
			return true;
		}
		
		return false;
	}
	
	public static BlockPos findClosestValidBlock(double range,
		boolean ignoreVisibility, BlockValidator validator)
	{
		// initialize queue
		ArrayDeque<BlockPos> queue = new ArrayDeque<>();
		HashSet<BlockPos> visited = new HashSet<>();
		
		// prepare range check
		Vec3d eyesPos = RotationUtils.getEyesPos();
		double rangeSq = Math.pow(range + 0.5, 2);
		
		// add start pos
		queue.add(new BlockPos(mc.player).up());
		
		// find block using breadth first search
		while(!queue.isEmpty())
		{
			BlockPos current = queue.pop();
			
			// check range
			if(eyesPos.squareDistanceTo(
				new Vec3d(current).addVector(0.5, 0.5, 0.5)) > rangeSq)
				continue;
			
			boolean canBeClicked = canBeClicked(current);
			
			// check if block is valid
			if(canBeClicked && validator.isValid(current))
				return current;
			
			if(ignoreVisibility || !canBeClicked)
			{
				// add neighbors
				for(EnumFacing facing : EnumFacing.values())
				{
					BlockPos next = current.offset(facing);
					
					if(visited.contains(next))
						continue;
					
					queue.add(next);
					visited.add(next);
				}
			}
		}
		
		return null;
	}
	
	public static interface BlockValidator
	{
		public boolean isValid(BlockPos pos);
	}
	
	@Deprecated
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
	
	@Deprecated
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
	
	@Deprecated
	public static void faceBlockClientHorizontally(BlockPos blockPos)
	{
		double diffX = blockPos.getX() + 0.5 - mc.player.posX;
		double diffZ = blockPos.getZ() + 0.5 - mc.player.posZ;
		float yaw =
			(float)(Math.atan2(diffZ, diffX) * 180.0D / Math.PI) - 90.0F;
		mc.player.rotationYaw = mc.player.rotationYaw
			+ MathHelper.wrapDegrees(yaw - mc.player.rotationYaw);
	}
	
	@Deprecated
	public static float getPlayerBlockDistance(BlockPos blockPos)
	{
		return getPlayerBlockDistance(blockPos.getX(), blockPos.getY(),
			blockPos.getZ());
	}
	
	@Deprecated
	private static float getPlayerBlockDistance(double posX, double posY,
		double posZ)
	{
		float xDiff = (float)(mc.player.posX - posX);
		float yDiff = (float)(mc.player.posY - posY);
		float zDiff = (float)(mc.player.posZ - posZ);
		return getBlockDistance(xDiff, yDiff, zDiff);
	}
	
	@Deprecated
	public static float getBlockDistance(float xDiff, float yDiff, float zDiff)
	{
		return MathHelper.sqrt(
			(xDiff - 0.5F) * (xDiff - 0.5F) + (yDiff - 0.5F) * (yDiff - 0.5F)
				+ (zDiff - 0.5F) * (zDiff - 0.5F));
	}
}
