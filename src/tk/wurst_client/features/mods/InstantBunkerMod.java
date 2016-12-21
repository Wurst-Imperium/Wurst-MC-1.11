/*
 * Copyright © 2014 - 2016 | Wurst-Imperium | All rights reserved.
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.features.mods;

import java.util.ArrayList;

import org.lwjgl.opengl.GL11;

import net.minecraft.block.material.Material;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import tk.wurst_client.events.listeners.RenderListener;
import tk.wurst_client.events.listeners.UpdateListener;
import tk.wurst_client.features.special_features.YesCheatSpf.BypassLevel;
import tk.wurst_client.utils.BlockUtils;
import tk.wurst_client.utils.RenderUtils;

@Mod.Info(description = "Instantly builds a small bunker around you.",
	name = "InstantBunker",
	tags = "instant bunker",
	help = "Mods/InstantBunker")
@Mod.Bypasses
public class InstantBunkerMod extends Mod
	implements UpdateListener, RenderListener
{
	// Bottom = 0, Top = 1, Front = 2, Back = 3, Right = 4, Left = 5.
	private int[][] template = {{0, 1, 2, 1}, {1, 1, 2, 1}, {-1, 1, 2, 1},
		{2, 1, 2, 1}, {-2, 1, 2, 1}, {2, 1, 1, 1}, {-2, 1, 1, 1}, {2, 1, 0, 1},
		{-2, 1, 0, 1}, {2, 1, -1, 1}, {-2, 1, -1, 1}, {0, 1, -2, 1},
		{1, 1, -2, 1}, {-1, 1, -2, 1}, {2, 1, -2, 1}, {-2, 1, -2, 1},
		{0, 2, 2, 1}, {1, 2, 2, 1}, {-1, 2, 2, 1}, {2, 2, 2, 1}, {-2, 2, 2, 1},
		{2, 2, 1, 1}, {-2, 2, 1, 1}, {2, 2, 0, 1}, {-2, 2, 0, 1}, {2, 2, -1, 1},
		{-2, 2, -1, 1}, {0, 2, -2, 1}, {1, 2, -2, 1}, {-1, 2, -2, 1},
		{2, 2, -2, 1}, {-2, 2, -2, 1}, {0, 3, 2, 1}, {1, 3, 2, 1},
		{-1, 3, 2, 1}, {2, 3, 2, 1}, {-2, 3, 2, 1}, {2, 3, 1, 1}, {-2, 3, 1, 1},
		{2, 3, 0, 1}, {-2, 3, 0, 1}, {2, 3, -1, 1}, {-2, 3, -1, 1},
		{0, 3, -2, 1}, {1, 3, -2, 1}, {-1, 3, -2, 1}, {2, 3, -2, 1},
		{-2, 3, -2, 1}, {0, 4, 2, 2}, {1, 4, 2, 2}, {-1, 4, 2, 2},
		{0, 4, -2, 3}, {1, 4, -2, 3}, {-1, 4, -2, 3}, {2, 4, 0, 4},
		{-2, 4, 0, 5}, {0, 4, 1, 2},};
	
	private int blockIndex;
	private boolean building;
	private final ArrayList<BlockPos> positions = new ArrayList<>();
	
	@Override
	public void onEnable()
	{
		boolean advanced = template[0].length == 4;
		
		// initialize
		// get start pos and facings
		BlockPos startPos = new BlockPos(mc.player).down();
		EnumFacing facing = mc.player.getHorizontalFacing();
		EnumFacing facing2 = facing.rotateYCCW();
		
		// set positions
		positions.clear();
		if(advanced)
		{
			startPos = startPos.down();
			for(int[] pos : template)
			{
				EnumFacing direction = EnumFacing.getFront(pos[3]);
				
				if(direction.getHorizontalIndex() != -1)
					for(int i = 0; i < facing.getHorizontalIndex(); i++)
						direction = direction.rotateY();
					
				positions.add(startPos.up(pos[1]).offset(facing, pos[2])
					.offset(facing2, pos[0]).offset(direction));
			}
		}else
			for(int[] pos : template)
				positions.add(startPos.up(pos[1]).offset(facing, pos[2])
					.offset(facing2, pos[0]));
			
		if(wurst.special.yesCheatSpf.getBypassLevel()
			.ordinal() >= BypassLevel.ANTICHEAT.ordinal())
		{
			// initialize building process
			blockIndex = 0;
			building = true;
			mc.rightClickDelayTimer = 4;
		}
		
		wurst.events.add(UpdateListener.class, this);
		wurst.events.add(RenderListener.class, this);
	}
	
	@Override
	public void onDisable()
	{
		wurst.events.remove(UpdateListener.class, this);
		wurst.events.remove(RenderListener.class, this);
		building = false;
	}
	
	@Override
	public void onUpdate()
	{
		// build instantly
		if(!building)
		{
			for(BlockPos pos : positions)
				if(BlockUtils.getMaterial(pos) == Material.AIR)
					BlockUtils.placeBlockSimple(pos);
			mc.player.swingArm(EnumHand.MAIN_HAND);
			setEnabled(false);
			return;
		}
		
		// place next block
		if(blockIndex < positions.size() && (mc.rightClickDelayTimer == 0
			|| wurst.mods.fastPlaceMod.isActive()))
		{
			BlockPos pos = positions.get(blockIndex);
			
			if(BlockUtils.getMaterial(pos) == Material.AIR)
				BlockUtils.placeBlockLegit(pos);
			else
			{
				blockIndex++;
				if(blockIndex == positions.size())
				{
					building = false;
					setEnabled(false);
				}
			}
		}
	}
	
	@Override
	public void onRender()
	{
		if(!building || blockIndex >= positions.size())
			return;
		
		// scale and offset
		double scale = 1D * 7D / 8D;
		double offset = (1D - scale) / 2D;
		
		// GL settings
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glEnable(GL11.GL_LINE_SMOOTH);
		GL11.glLineWidth(2F);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_CULL_FACE);
		
		GL11.glPushMatrix();
		GL11.glTranslated(-mc.getRenderManager().renderPosX,
			-mc.getRenderManager().renderPosY,
			-mc.getRenderManager().renderPosZ);
		
		// green box
		{
			GL11.glDepthMask(false);
			GL11.glColor4f(0F, 1F, 0F, 0.15F);
			BlockPos pos = positions.get(blockIndex);
			
			GL11.glPushMatrix();
			GL11.glTranslated(pos.getX(), pos.getY(), pos.getZ());
			GL11.glTranslated(offset, offset, offset);
			GL11.glScaled(scale, scale, scale);
			
			RenderUtils.drawSolidBox();
			
			GL11.glPopMatrix();
			GL11.glDepthMask(true);
		}
		
		// black outlines
		GL11.glColor4f(0F, 0F, 0F, 0.5F);
		for(int i = blockIndex; i < positions.size(); i++)
		{
			BlockPos pos = positions.get(i);
			
			GL11.glPushMatrix();
			GL11.glTranslated(pos.getX(), pos.getY(), pos.getZ());
			GL11.glTranslated(offset, offset, offset);
			GL11.glScaled(scale, scale, scale);
			
			RenderUtils.drawOutlinedBox();
			
			GL11.glPopMatrix();
		}
		
		GL11.glPopMatrix();
		
		// GL resets
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_BLEND);
	}
}
