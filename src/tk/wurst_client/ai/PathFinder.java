/*
 * Copyright © 2014 - 2016 | Wurst-Imperium | All rights reserved.
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import tk.wurst_client.WurstClient;

public class PathFinder
{
	private final WurstClient wurst = WurstClient.INSTANCE;
	private final Minecraft mc = Minecraft.getMinecraft();
	
	private final boolean invulnerable = mc.player.capabilities.isCreativeMode;
	private final boolean creativeFlying = mc.player.capabilities.isFlying;
	private final boolean flying =
		creativeFlying || wurst.mods.flightMod.isActive();
	private final boolean immuneToFallDamage =
		invulnerable || wurst.mods.noFallMod.isActive();
	private final boolean noSlowdownActive =
		wurst.mods.noSlowdownMod.isActive();
	private final boolean jesus = wurst.mods.jesusMod.isActive();
	private final boolean spider = wurst.mods.spiderMod.isActive();
	
	private final PathPos start;
	private final BlockPos goal;
	private PathPos current;
	
	private final HashMap<PathPos, Float> costMap = new HashMap<>();
	private final HashMap<PathPos, PathPos> prevPosMap = new HashMap<>();
	private final PathQueue queue = new PathQueue();
	
	private boolean done;
	private int iterations;
	private final ArrayList<PathPos> path = new ArrayList<>();
	
	public PathFinder(BlockPos goal)
	{
		start = new PathPos(new BlockPos(mc.player));
		this.goal = goal;
		
		costMap.put(start, 0F);
		queue.add(start, getHeuristic(start));
	}
	
	public void process(int limit)
	{
		if(done)
			throw new IllegalStateException("Path was already found!");
		
		int i = 0;
		for(; i < limit && !isFailed(); i++)
		{
			// get next position from queue
			current = queue.poll();
			
			// check if path is found
			done = goal.equals(current);
			if(done)
				return;
			
			// add neighbors to queue
			for(PathPos next : getNeighbors(current))
			{
				// check cost
				float newCost = costMap.get(current) + getCost(current, next);
				if(costMap.containsKey(next) && costMap.get(next) <= newCost)
					continue;
				
				// add to queue
				costMap.put(next, newCost);
				prevPosMap.put(next, current);
				queue.add(next, newCost + getHeuristic(next));
			}
		}
		iterations += i;
	}
	
	private ArrayList<PathPos> getNeighbors(PathPos pos)
	{
		ArrayList<PathPos> neighbors = new ArrayList<>();
		
		// abort if too far away
		if(Math.abs(start.getX() - pos.getX()) > 256
			|| Math.abs(start.getZ() - pos.getZ()) > 256)
			return neighbors;
		
		// get all neighbors
		BlockPos north = pos.north();
		BlockPos east = pos.east();
		BlockPos south = pos.south();
		BlockPos west = pos.west();
		
		BlockPos northEast = north.east();
		BlockPos southEast = south.east();
		BlockPos southWest = south.west();
		BlockPos northWest = north.west();
		
		BlockPos up = pos.up();
		BlockPos down = pos.down();
		
		// flying
		boolean flying = canFlyAt(pos);
		// walking
		boolean onGround = canBeSolid(down);
		
		// player can move sideways if flying, standing on the ground, jumping,
		// or inside of a block that allows sideways movement (ladders, webs,
		// etc.)
		if(flying || onGround || pos.isJumping()
			|| canMoveSidewaysInMidairAt(pos) || canClimbUpAt(pos.down()))
		{
			// north
			boolean basicCheckNorth = canGoThrough(north)
				&& canGoThrough(north.up()) && canGoAbove(north.down());
			if(basicCheckNorth && (flying || canGoThrough(north.down())
				|| canSafelyStandOn(north.down())))
				neighbors.add(new PathPos(north));
			
			// east
			boolean basicCheckEast = canGoThrough(east)
				&& canGoThrough(east.up()) && canGoAbove(east.down());
			if(basicCheckEast && (flying || canGoThrough(east.down())
				|| canSafelyStandOn(east.down())))
				neighbors.add(new PathPos(east));
			
			// south
			boolean basicCheckSouth = canGoThrough(south)
				&& canGoThrough(south.up()) && canGoAbove(south.down());
			if(basicCheckSouth && (flying || canGoThrough(south.down())
				|| canSafelyStandOn(south.down())))
				neighbors.add(new PathPos(south));
			
			// west
			boolean basicCheckWest = canGoThrough(west)
				&& canGoThrough(west.up()) && canGoAbove(west.down());
			if(basicCheckWest && (flying || canGoThrough(west.down())
				|| canSafelyStandOn(west.down())))
				neighbors.add(new PathPos(west));
			
			// north-east
			if(basicCheckNorth && basicCheckEast && canGoThrough(northEast)
				&& canGoThrough(northEast.up()) && canGoAbove(northEast.down())
				&& (flying || canGoThrough(northEast.down())
					|| canSafelyStandOn(northEast.down())))
				neighbors.add(new PathPos(northEast));
			
			// south-east
			if(basicCheckSouth && basicCheckEast && canGoThrough(southEast)
				&& canGoThrough(southEast.up()) && canGoAbove(southEast.down())
				&& (flying || canGoThrough(southEast.down())
					|| canSafelyStandOn(southEast.down())))
				neighbors.add(new PathPos(southEast));
			
			// south-west
			if(basicCheckSouth && basicCheckWest && canGoThrough(southWest)
				&& canGoThrough(southWest.up()) && canGoAbove(southWest.down())
				&& (flying || canGoThrough(southWest.down())
					|| canSafelyStandOn(southWest.down())))
				neighbors.add(new PathPos(southWest));
			
			// north-west
			if(basicCheckNorth && basicCheckWest && canGoThrough(northWest)
				&& canGoThrough(northWest.up()) && canGoAbove(northWest.down())
				&& (flying || canGoThrough(northWest.down())
					|| canSafelyStandOn(northWest.down())))
				neighbors.add(new PathPos(northWest));
		}
		
		// up
		if(pos.getY() < 256 && canGoThrough(up.up())
			&& (flying || onGround || canClimbUpAt(pos)))
			neighbors.add(new PathPos(up, onGround));
		
		// down
		if(pos.getY() > 0 && canGoThrough(down)
			&& (flying || canFallBelow(pos)))
			neighbors.add(new PathPos(down));
		
		return neighbors;
	}
	
	private boolean canBeSolid(BlockPos pos)
	{
		Material material = getMaterial(pos);
		Block block = getBlock(pos);
		return (material.blocksMovement() && !(block instanceof BlockSign))
			|| block instanceof BlockLadder || (jesus
				&& (material == Material.WATER || material == Material.LAVA));
	}
	
	private boolean canGoThrough(BlockPos pos)
	{
		// check if loaded
		if(!mc.world.isBlockLoaded(pos, false))
			return false;
		
		// check if solid
		Material material = getMaterial(pos);
		Block block = getBlock(pos);
		if(material.blocksMovement() && !(block instanceof BlockSign))
			return false;
		
		// check if trapped
		if(block instanceof BlockTripWire
			|| block instanceof BlockPressurePlate)
			return false;
		
		// check if safe
		if(!invulnerable
			&& (material == Material.LAVA || material == Material.FIRE))
			return false;
		
		return true;
	}
	
	private boolean canGoAbove(BlockPos pos)
	{
		// check for fences, etc.
		Block block = getBlock(pos);
		if(block instanceof BlockFence || block instanceof BlockWall)
			return false;
		
		return true;
	}
	
	private boolean canSafelyStandOn(BlockPos pos)
	{
		// check if solid
		Material material = getMaterial(pos);
		if(!canBeSolid(pos))
			return false;
		
		// check if safe
		if(!invulnerable
			&& (material == Material.CACTUS || material == Material.LAVA))
			return false;
		
		return true;
	}
	
	private boolean canFallBelow(PathPos pos)
	{
		// check if player can keep falling
		BlockPos down2 = pos.down(2);
		if(canGoThrough(down2))
			return true;
		
		// check if player can stand below
		if(!canSafelyStandOn(down2))
			return false;
		
		// check if fall damage is off
		if(immuneToFallDamage)
			return true;
		
		// check if fall ends with slime block
		if(getBlock(down2) instanceof BlockSlime)
			return true;
		
		// check fall damage
		BlockPos prevPos = pos;
		for(int i = 0; i <= 3; i++)
		{
			// check if prevPos does not exist, meaning that the pathfinding
			// started during the fall and fall damage should be ignored because
			// it cannot be prevented
			if(prevPos == null)
				return true;
				
			// check if point is not part of this fall, meaning that the fall is
			// too short to cause any damage
			if(!pos.up(i).equals(prevPos))
				return true;
			
			// check if block resets fall damage
			Block prevBlock = getBlock(prevPos);
			if(prevBlock instanceof BlockLiquid
				|| prevBlock instanceof BlockLadder
				|| prevBlock instanceof BlockVine
				|| prevBlock instanceof BlockWeb)
				return true;
			
			prevPos = prevPosMap.get(prevPos);
		}
		
		return false;
	}
	
	private boolean canFlyAt(BlockPos pos)
	{
		return flying
			|| !noSlowdownActive && getMaterial(pos) == Material.WATER;
	}
	
	private boolean canClimbUpAt(BlockPos pos)
	{
		// check if this block works for climbing
		Block block = getBlock(pos);
		if(!spider && !(block instanceof BlockLadder)
			&& !(block instanceof BlockVine))
			return false;
		
		// check if any adjacent block is solid
		BlockPos up = pos.up();
		if(!canBeSolid(pos.north()) && !canBeSolid(pos.east())
			&& !canBeSolid(pos.south()) && !canBeSolid(pos.west())
			&& !canBeSolid(up.north()) && !canBeSolid(up.east())
			&& !canBeSolid(up.south()) && !canBeSolid(up.west()))
			return false;
		
		return true;
	}
	
	private boolean canMoveSidewaysInMidairAt(BlockPos pos)
	{
		// check feet
		Block blockFeet = getBlock(pos);
		if(blockFeet instanceof BlockLiquid || blockFeet instanceof BlockLadder
			|| blockFeet instanceof BlockVine || blockFeet instanceof BlockWeb)
			return true;
		
		// check head
		Block blockHead = getBlock(pos.up());
		if(blockHead instanceof BlockLiquid || blockHead instanceof BlockWeb)
			return true;
		
		return false;
	}
	
	private float getCost(BlockPos current, BlockPos next)
	{
		float cost = 1F;
		
		// diagonal movement
		if(current.getX() != next.getX() && current.getZ() != next.getZ())
			cost *= 1.4142135623730951F;
		
		// liquids
		Material nextMaterial = getMaterial(next);
		if(nextMaterial == Material.WATER && !noSlowdownActive)
			cost *= 1.3164437838225804F;
		else if(nextMaterial == Material.LAVA)
			cost *= 4.539515393656079F;
		
		// soul sand
		if(!canFlyAt(next) && getBlock(next.down()) instanceof BlockSoulSand)
			cost *= 2.5F;
		
		return cost;
	}
	
	private float getHeuristic(BlockPos pos)
	{
		float dx = Math.abs(pos.getX() - goal.getX());
		float dy = Math.abs(pos.getY() - goal.getY());
		float dz = Math.abs(pos.getZ() - goal.getZ());
		return 1.001F
			* ((dx + dy + dz) - 0.5857864376269049F * Math.min(dx, dz));
	}
	
	private Block getBlock(BlockPos pos)
	{
		return mc.world.getBlockState(pos).getBlock();
	}
	
	private Material getMaterial(BlockPos pos)
	{
		return mc.world.getBlockState(pos).getMaterial();
	}
	
	public PathPos getCurrentPos()
	{
		return current;
	}
	
	public BlockPos getGoal()
	{
		return goal;
	}
	
	public Set<PathPos> getProcessedBlocks()
	{
		return prevPosMap.keySet();
	}
	
	public PathPos[] getQueuedBlocks()
	{
		return queue.toArray();
	}
	
	public int getQueueSize()
	{
		return queue.size();
	}
	
	public float getCost(BlockPos pos)
	{
		return costMap.get(pos);
	}
	
	public BlockPos getPrevPos(BlockPos pos)
	{
		return prevPosMap.get(pos);
	}
	
	public boolean isDone()
	{
		return done;
	}
	
	public boolean isFailed()
	{
		return queue.isEmpty() || iterations >= 1024 * 200;
	}
	
	public ArrayList<PathPos> formatPath()
	{
		if(!done)
			throw new IllegalStateException("No path found!");
		if(!path.isEmpty())
			throw new IllegalStateException("Path was already formatted!");
		
		// get positions
		PathPos pos = current;
		while(pos != null)
		{
			path.add(pos);
			pos = prevPosMap.get(pos);
		}
		
		// reverse path
		Collections.reverse(path);
		
		return path;
	}
	
	public boolean isPathStillValid(int index)
	{
		if(path.isEmpty())
			throw new IllegalStateException("Path is not formatted!");
		
		// check player abilities
		if(invulnerable != mc.player.capabilities.isCreativeMode
			|| flying != (creativeFlying || wurst.mods.flightMod.isActive())
			|| immuneToFallDamage != (invulnerable
				|| wurst.mods.noFallMod.isActive())
			|| noSlowdownActive != wurst.mods.noSlowdownMod.isActive()
			|| jesus != wurst.mods.jesusMod.isActive()
			|| spider != wurst.mods.spiderMod.isActive())
			return false;
		
		// check path
		for(int i = Math.max(1, index); i < path.size(); i++)
			if(!getNeighbors(path.get(i - 1)).contains(path.get(i)))
				return false;
			
		return true;
	}
	
	public PathProcessor getProcessor()
	{
		if(flying)
			return new FlyPathProcessor(path, creativeFlying);
		
		return new WalkPathProcessor(path);
	}
}
