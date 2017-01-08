/*
 * Copyright © 2014 - 2017 | Wurst-Imperium | All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.features.mods;

import java.util.ArrayList;

import net.minecraft.block.BlockFenceGate;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.BlockPos;
import tk.wurst_client.events.listeners.UpdateListener;
import tk.wurst_client.features.special_features.YesCheatSpf.BypassLevel;
import tk.wurst_client.settings.SliderSetting;
import tk.wurst_client.settings.SliderSetting.ValueDisplay;

@Mod.Info(description = "Allows you to step up full blocks.",
	name = "Step",
	help = "Mods/Step")
@Mod.Bypasses
public class StepMod extends Mod implements UpdateListener
{
	public SliderSetting height =
		new SliderSetting("Height", 1, 1, 100, 1, ValueDisplay.INTEGER);
	
	@Override
	public void initSettings()
	{
		settings.add(height);
	}
	
	@Override
	public void onEnable()
	{
		wurst.events.add(UpdateListener.class, this);
	}
	
	@Override
	public void onDisable()
	{
		wurst.events.remove(UpdateListener.class, this);
		mc.player.stepHeight = 0.5F;
	}
	
	@Override
	public void onUpdate()
	{
		if(wurst.special.yesCheatSpf.getBypassLevel()
			.ordinal() >= BypassLevel.ANTICHEAT.ordinal())
		{
			mc.player.stepHeight = 0.5F;
			if(mc.player.onGround && !mc.player.isOnLadder()
				&& (mc.player.movementInput.moveForward != 0.0F
					|| mc.player.movementInput.moveStrafe != 0.0F)
				&& canStep() && !mc.player.movementInput.jump
				&& mc.player.isCollidedHorizontally)
			{
				mc.getConnection()
					.sendPacket(new CPacketPlayer.Position(mc.player.posX,
						mc.player.posY + 0.42D, mc.player.posZ,
						mc.player.onGround));
				mc.getConnection()
					.sendPacket(new CPacketPlayer.Position(mc.player.posX,
						mc.player.posY + 0.753D, mc.player.posZ,
						mc.player.onGround));
				mc.player.setPosition(mc.player.posX, mc.player.posY + 1D,
					mc.player.posZ);
			}
		}else
			mc.player.stepHeight = height.getValueF();
	}
	
	@Override
	public void onYesCheatUpdate(BypassLevel bypassLevel)
	{
		switch(bypassLevel)
		{
			default:
			case OFF:
			case MINEPLEX:
				height.unlock();
				break;
			case ANTICHEAT:
			case OLDER_NCP:
			case LATEST_NCP:
			case GHOST_MODE:
				height.lockToValue(1);
				break;
		}
	}
	
	@SuppressWarnings("deprecation")
	private boolean canStep()
	{
		ArrayList<BlockPos> collisionBlocks = new ArrayList<>();
		
		EntityPlayerSP player = mc.player;
		BlockPos pos1 =
			new BlockPos(player.getEntityBoundingBox().minX - 0.001D,
				player.getEntityBoundingBox().minY - 0.001D,
				player.getEntityBoundingBox().minZ - 0.001D);
		BlockPos pos2 =
			new BlockPos(player.getEntityBoundingBox().maxX + 0.001D,
				player.getEntityBoundingBox().maxY + 0.001D,
				player.getEntityBoundingBox().maxZ + 0.001D);
		
		if(player.world.isAreaLoaded(pos1, pos2))
			for(int x = pos1.getX(); x <= pos2.getX(); x++)
				for(int y = pos1.getY(); y <= pos2.getY(); y++)
					for(int z = pos1.getZ(); z <= pos2.getZ(); z++)
						if(y > player.posY - 1.0D && y <= player.posY)
							collisionBlocks.add(new BlockPos(x, y, z));
						
		BlockPos belowPlayerPos =
			new BlockPos(player.posX, player.posY - 1.0D, player.posZ);
		for(BlockPos collisionBlock : collisionBlocks)
			if(!(player.world.getBlockState(collisionBlock.add(0, 1, 0))
				.getBlock() instanceof BlockFenceGate))
				if(player.world.getBlockState(collisionBlock.add(0, 1, 0))
					.getBlock().getCollisionBoundingBox(
						mc.world.getBlockState(collisionBlock), mc.world,
						belowPlayerPos) != null)
					return false;
				
		return true;
	}
}
