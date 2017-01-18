/*
 * Copyright © 2014 - 2017 | Wurst-Imperium | All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.utils;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashSet;

import com.google.common.collect.AbstractIterator;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerDigging.Action;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import tk.wurst_client.WurstClient;

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
	
	public static AxisAlignedBB getBoundingBox(BlockPos pos)
	{
		return getState(pos).getBoundingBox(mc.world, pos).offset(pos);
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
		Vec3d posVec = new Vec3d(pos).addVector(0.5, 0.5, 0.5);
		double distanceSqPosVec = eyesPos.squareDistanceTo(posVec);
		
		for(EnumFacing side : EnumFacing.values())
		{
			BlockPos neighbor = pos.offset(side);
			
			// check if neighbor can be right clicked
			if(!canBeClicked(neighbor))
				continue;
			
			Vec3d hitVec =
				posVec.add(new Vec3d(side.getDirectionVec()).scale(0.5));
			double distanceSqHitVec = eyesPos.squareDistanceTo(hitVec);
			
			// check if hitVec is within range (4.25 blocks)
			if(distanceSqHitVec > 18.0625)
				continue;
			
			// check if side is visible (facing away from player)
			if(distanceSqHitVec <= distanceSqPosVec)
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
		Vec3d posVec = new Vec3d(pos).addVector(0.5, 0.5, 0.5);
		
		for(EnumFacing side : EnumFacing.values())
		{
			BlockPos neighbor = pos.offset(side);
			
			// check if neighbor can be right clicked
			if(!canBeClicked(neighbor))
				continue;
			
			Vec3d hitVec =
				posVec.add(new Vec3d(side.getDirectionVec()).scale(0.5));
			
			// check if hitVec is within range (6 blocks)
			if(eyesPos.squareDistanceTo(hitVec) > 36)
				continue;
			
			// place block
			processRightClickBlock(neighbor, side.getOpposite(), hitVec);
			
			return true;
		}
		
		return false;
	}
	
	public static boolean breakBlockLegit(BlockPos pos)
	{
		Vec3d eyesPos = RotationUtils.getEyesPos();
		Vec3d posVec = new Vec3d(pos).addVector(0.5, 0.5, 0.5);
		double distanceSqPosVec = eyesPos.squareDistanceTo(posVec);
		
		for(EnumFacing side : EnumFacing.values())
		{
			Vec3d hitVec =
				posVec.add(new Vec3d(side.getDirectionVec()).scale(0.5));
			double distanceSqHitVec = eyesPos.squareDistanceTo(hitVec);
			
			// check if hitVec is within range (4.25 blocks)
			if(distanceSqHitVec > 18.0625)
				continue;
			
			// check if side is facing towards player
			if(distanceSqHitVec >= distanceSqPosVec)
				continue;
			
			// check line of sight
			if(mc.world.rayTraceBlocks(eyesPos, hitVec, false, true,
				false) != null)
				continue;
			
			// AutoTool
			WurstClient.INSTANCE.mods.autoToolMod.setSlot(pos);
			
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
	
	public static boolean breakBlockExtraLegit(BlockPos pos)
	{
		Vec3d eyesPos = RotationUtils.getEyesPos();
		Vec3d posVec = new Vec3d(pos).addVector(0.5, 0.5, 0.5);
		double distanceSqPosVec = eyesPos.squareDistanceTo(posVec);
		
		for(EnumFacing side : EnumFacing.values())
		{
			Vec3d hitVec =
				posVec.add(new Vec3d(side.getDirectionVec()).scale(0.5));
			double distanceSqHitVec = eyesPos.squareDistanceTo(hitVec);
			
			// check if hitVec is within range (4.25 blocks)
			if(distanceSqHitVec > 18.0625)
				continue;
			
			// check if side is facing towards player
			if(distanceSqHitVec >= distanceSqPosVec)
				continue;
			
			// check line of sight
			if(mc.world.rayTraceBlocks(eyesPos, hitVec, false, true,
				false) != null)
				continue;
			
			// AutoTool
			WurstClient.INSTANCE.mods.autoToolMod.setSlot(pos);
			
			// face block
			if(!RotationUtils.faceVectorClient(hitVec))
				return true;
				
			// if attack key is down but nothing happens, release it for one
			// tick
			if(mc.gameSettings.keyBindAttack.pressed
				&& !mc.playerController.getIsHittingBlock())
			{
				mc.gameSettings.keyBindAttack.pressed = false;
				return true;
			}
			
			// damage block
			mc.gameSettings.keyBindAttack.pressed = true;
			
			return true;
		}
		
		return false;
	}
	
	public static boolean breakBlockSimple(BlockPos pos)
	{
		Vec3d eyesPos = RotationUtils.getEyesPos();
		Vec3d posVec = new Vec3d(pos).addVector(0.5, 0.5, 0.5);
		double distanceSqPosVec = eyesPos.squareDistanceTo(posVec);
		
		for(EnumFacing side : EnumFacing.values())
		{
			Vec3d hitVec =
				posVec.add(new Vec3d(side.getDirectionVec()).scale(0.5));
			double distanceSqHitVec = eyesPos.squareDistanceTo(hitVec);
			
			// check if hitVec is within range (6 blocks)
			if(distanceSqHitVec > 36)
				continue;
			
			// check if side is facing towards player
			if(distanceSqHitVec >= distanceSqPosVec)
				continue;
			
			// AutoTool
			WurstClient.INSTANCE.mods.autoToolMod.setSlot(pos);
			
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
	
	public static void breakBlockPacketSpam(BlockPos pos)
	{
		Vec3d eyesPos = RotationUtils.getEyesPos();
		Vec3d posVec = new Vec3d(pos).addVector(0.5, 0.5, 0.5);
		double distanceSqPosVec = eyesPos.squareDistanceTo(posVec);
		
		for(EnumFacing side : EnumFacing.values())
		{
			Vec3d hitVec =
				posVec.add(new Vec3d(side.getDirectionVec()).scale(0.5));
			
			// check if side is facing towards player
			if(eyesPos.squareDistanceTo(hitVec) >= distanceSqPosVec)
				continue;
			
			// break block
			mc.player.connection.sendPacket(new CPacketPlayerDigging(
				Action.START_DESTROY_BLOCK, pos, side));
			mc.player.connection.sendPacket(
				new CPacketPlayerDigging(Action.STOP_DESTROY_BLOCK, pos, side));
			
			return;
		}
	}
	
	public static boolean rightClickBlockLegit(BlockPos pos)
	{
		Vec3d eyesPos = RotationUtils.getEyesPos();
		Vec3d posVec = new Vec3d(pos).addVector(0.5, 0.5, 0.5);
		double distanceSqPosVec = eyesPos.squareDistanceTo(posVec);
		
		for(EnumFacing side : EnumFacing.values())
		{
			Vec3d hitVec =
				posVec.add(new Vec3d(side.getDirectionVec()).scale(0.5));
			double distanceSqHitVec = eyesPos.squareDistanceTo(hitVec);
			
			// check if hitVec is within range (4.25 blocks)
			if(distanceSqHitVec > 18.0625)
				continue;
			
			// check if side is facing towards player
			if(distanceSqHitVec >= distanceSqPosVec)
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
	
	public static boolean rightClickBlockSimple(BlockPos pos)
	{
		Vec3d eyesPos = RotationUtils.getEyesPos();
		Vec3d posVec = new Vec3d(pos).addVector(0.5, 0.5, 0.5);
		double distanceSqPosVec = eyesPos.squareDistanceTo(posVec);
		
		for(EnumFacing side : EnumFacing.values())
		{
			Vec3d hitVec =
				posVec.add(new Vec3d(side.getDirectionVec()).scale(0.5));
			double distanceSqHitVec = eyesPos.squareDistanceTo(hitVec);
			
			// check if hitVec is within range (6 blocks)
			if(distanceSqHitVec > 36)
				continue;
			
			// check if side is facing towards player
			if(distanceSqHitVec >= distanceSqPosVec)
				continue;
			
			// place block
			processRightClickBlock(pos, side, hitVec);
			
			return true;
		}
		
		return false;
	}
	
	public static Iterable<BlockPos> getValidBlocksByDistance(double range,
		boolean ignoreVisibility, BlockValidator validator)
	{
		// prepare range check
		Vec3d eyesPos = RotationUtils.getEyesPos().subtract(0.5, 0.5, 0.5);
		double rangeSq = Math.pow(range + 0.5, 2);
		
		// set start pos
		BlockPos startPos = new BlockPos(RotationUtils.getEyesPos());
		
		return () -> new AbstractIterator<BlockPos>()
		{
			// initialize queue
			private ArrayDeque<BlockPos> queue =
				new ArrayDeque<>(Arrays.asList(startPos));
			private HashSet<BlockPos> visited = new HashSet<>();
			
			@Override
			protected BlockPos computeNext()
			{
				// find block using breadth first search
				while(!queue.isEmpty())
				{
					BlockPos current = queue.pop();
					
					// check range
					if(eyesPos.squareDistanceTo(new Vec3d(current)) > rangeSq)
						continue;
					
					boolean canBeClicked = canBeClicked(current);
					
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
					
					// check if block is valid
					if(canBeClicked && validator.isValid(current))
						return current;
				}
				
				return endOfData();
			}
		};
	}
	
	public static Iterable<BlockPos> getValidBlocksByDistanceReversed(
		double range, boolean ignoreVisibility, BlockValidator validator)
	{
		ArrayDeque<BlockPos> validBlocks = new ArrayDeque<>();
		
		BlockUtils.getValidBlocksByDistance(range, ignoreVisibility, validator)
			.forEach((p) -> validBlocks.push(p));
		
		return validBlocks;
	}
	
	public static Iterable<BlockPos> getValidBlocks(double range,
		BlockValidator validator)
	{
		// prepare range check
		Vec3d eyesPos = RotationUtils.getEyesPos().subtract(0.5, 0.5, 0.5);
		double rangeSq = Math.pow(range + 0.5, 2);
		
		return getValidBlocks((int)Math.ceil(range), (pos) -> {
			
			// check range
			if(eyesPos.squareDistanceTo(new Vec3d(pos)) > rangeSq)
				return false;
			
			// check if block is valid
			return validator.isValid(pos);
		});
	}
	
	public static Iterable<BlockPos> getValidBlocks(int blockRange,
		BlockValidator validator)
	{
		BlockPos playerPos = new BlockPos(RotationUtils.getEyesPos());
		
		BlockPos min = playerPos.add(-blockRange, -blockRange, -blockRange);
		BlockPos max = playerPos.add(blockRange, blockRange, blockRange);
		
		return () -> new AbstractIterator<BlockPos>()
		{
			private BlockPos last;
			
			private BlockPos computeNextUnchecked()
			{
				if(last == null)
				{
					last = min;
					return last;
				}
				
				int x = last.getX();
				int y = last.getY();
				int z = last.getZ();
				
				if(z < max.getZ())
					z++;
				else if(x < max.getX())
				{
					z = min.getZ();
					x++;
				}else if(y < max.getY())
				{
					z = min.getZ();
					x = min.getX();
					y++;
				}else
					return null;
				
				last = new BlockPos(x, y, z);
				return last;
			}
			
			@Override
			protected BlockPos computeNext()
			{
				BlockPos pos;
				while((pos = computeNextUnchecked()) != null)
				{
					// skip air blocks
					if(getMaterial(pos) == Material.AIR)
						continue;
					
					// check if block is valid
					if(!validator.isValid(pos))
						continue;
					
					return pos;
				}
				
				return endOfData();
			}
		};
	}
	
	public static interface BlockValidator
	{
		public boolean isValid(BlockPos pos);
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
