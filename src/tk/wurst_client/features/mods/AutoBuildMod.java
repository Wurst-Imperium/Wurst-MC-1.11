/*
 * Copyright © 2014 - 2016 | Wurst-Imperium | All rights reserved.
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.features.mods;

import static org.lwjgl.opengl.GL11.glVertex3d;

import java.util.ArrayList;

import org.lwjgl.opengl.GL11;

import net.minecraft.block.material.Material;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import tk.wurst_client.events.listeners.RenderListener;
import tk.wurst_client.events.listeners.UpdateListener;
import tk.wurst_client.features.Feature;
import tk.wurst_client.features.special_features.YesCheatSpf.BypassLevel;
import tk.wurst_client.settings.ModeSetting;
import tk.wurst_client.utils.BlockUtils;

@Mod.Info(
	description = "Automatically builds the selected template whenever you place a block.\n"
		+ "Can fully bypass NoCheat+ while YesCheat+ is enabled.\n"
		+ "Templates can be customized. Press the \"Help\" button for details.",
	name = "AutoBuild",
	tags = "AutoBridge, AutoFloor, AutoNazi, AutoPenis, AutoPillar, AutoWall, AutoWurst, auto build",
	help = "Mods/AutoBuild")
@Mod.Bypasses
public class AutoBuildMod extends Mod implements UpdateListener, RenderListener
{
	public static ArrayList<String> names = new ArrayList<String>();
	public static ArrayList<int[][]> templates = new ArrayList<int[][]>();
	private int template = 1;
	
	private int blockIndex;
	private boolean building;
	
	private final ArrayList<BlockPos> positions = new ArrayList<>();
	
	@Override
	public String getRenderName()
	{
		return getName() + " [" + names.get(template) + "]";
	}
	
	public void initTemplateSetting()
	{
		settings.add(new ModeSetting("Template",
			names.toArray(new String[names.size()]), template)
		{
			@Override
			public void update()
			{
				template = getSelected();
			}
		});
	}
	
	@Override
	public Feature[] getSeeAlso()
	{
		return new Feature[]{wurst.mods.buildRandomMod,
			wurst.mods.fastPlaceMod};
	}
	
	@Override
	public void onEnable()
	{
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
			
			GL11.glBegin(GL11.GL_QUADS);
			{
				glVertex3d(0, 0, 0);
				glVertex3d(1, 0, 0);
				glVertex3d(1, 0, 1);
				glVertex3d(0, 0, 1);
				
				glVertex3d(0, 1, 0);
				glVertex3d(0, 1, 1);
				glVertex3d(1, 1, 1);
				glVertex3d(1, 1, 0);
				
				glVertex3d(0, 0, 0);
				glVertex3d(0, 1, 0);
				glVertex3d(1, 1, 0);
				glVertex3d(1, 0, 0);
				
				glVertex3d(1, 0, 0);
				glVertex3d(1, 1, 0);
				glVertex3d(1, 1, 1);
				glVertex3d(1, 0, 1);
				
				glVertex3d(0, 0, 1);
				glVertex3d(1, 0, 1);
				glVertex3d(1, 1, 1);
				glVertex3d(0, 1, 1);
				
				glVertex3d(0, 0, 0);
				glVertex3d(0, 0, 1);
				glVertex3d(0, 1, 1);
				glVertex3d(0, 1, 0);
			}
			GL11.glEnd();
			
			GL11.glPopMatrix();
			GL11.glDepthMask(true);
		}
		
		// black lines
		GL11.glColor4f(0F, 0F, 0F, 0.5F);
		for(int i = blockIndex; i < positions.size(); i++)
		{
			BlockPos pos = positions.get(i);
			
			GL11.glPushMatrix();
			GL11.glTranslated(pos.getX(), pos.getY(), pos.getZ());
			GL11.glTranslated(offset, offset, offset);
			GL11.glScaled(scale, scale, scale);
			
			GL11.glBegin(GL11.GL_LINES);
			{
				glVertex3d(0, 0, 0);
				glVertex3d(1, 0, 0);
				
				glVertex3d(1, 0, 0);
				glVertex3d(1, 0, 1);
				
				glVertex3d(1, 0, 1);
				glVertex3d(0, 0, 1);
				
				glVertex3d(0, 0, 1);
				glVertex3d(0, 0, 0);
				
				glVertex3d(0, 0, 0);
				glVertex3d(0, 1, 0);
				
				glVertex3d(1, 0, 0);
				glVertex3d(1, 1, 0);
				
				glVertex3d(1, 0, 1);
				glVertex3d(1, 1, 1);
				
				glVertex3d(0, 0, 1);
				glVertex3d(0, 1, 1);
				
				glVertex3d(0, 1, 0);
				glVertex3d(1, 1, 0);
				
				glVertex3d(1, 1, 0);
				glVertex3d(1, 1, 1);
				
				glVertex3d(1, 1, 1);
				glVertex3d(0, 1, 1);
				
				glVertex3d(0, 1, 1);
				glVertex3d(0, 1, 0);
			}
			GL11.glEnd();
			
			GL11.glPopMatrix();
		}
		
		GL11.glPopMatrix();
		
		// GL resets
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_BLEND);
	}
	
	@Override
	public void onUpdate()
	{
		boolean advanced = templates.get(template)[0].length == 4;
		
		// initialize on right click
		if(!building && mc.gameSettings.keyBindUseItem.pressed
			&& (mc.rightClickDelayTimer == 4
				|| wurst.mods.fastPlaceMod.isActive())
			&& mc.objectMouseOver != null
			&& mc.objectMouseOver.getBlockPos() != null && BlockUtils
				.getMaterial(mc.objectMouseOver.getBlockPos()) != Material.AIR)
		{
			// get start pos and facings
			BlockPos startPos = mc.objectMouseOver.getBlockPos()
				.offset(mc.objectMouseOver.sideHit);
			EnumFacing facing = mc.player.getHorizontalFacing();
			EnumFacing facing2 = facing.rotateYCCW();
			
			// set positions
			positions.clear();
			if(advanced)
			{
				startPos = startPos.down();
				for(int[] pos : templates.get(template))
				{
					EnumFacing direction = EnumFacing.getFront(pos[3]);
					
					if(direction.getHorizontalIndex() != -1)
						for(int i = 0; i < facing.getHorizontalIndex(); i++)
							direction = direction.rotateY();
						
					positions.add(startPos.up(pos[1]).offset(facing, pos[2])
						.offset(facing2, pos[0]).offset(direction));
				}
			}else
				for(int[] pos : templates.get(template))
					positions.add(startPos.up(pos[1]).offset(facing, pos[2])
						.offset(facing2, pos[0]));
				
			if(wurst.special.yesCheatSpf.getBypassLevel()
				.ordinal() < BypassLevel.ANTICHEAT.ordinal())
			{
				// build instantly
				for(BlockPos pos : positions)
					if(BlockUtils.getMaterial(pos) == Material.AIR)
						BlockUtils.placeBlockSimple(pos);
				mc.player.swingArm(EnumHand.MAIN_HAND);
				
				// set timer to 3 to prevent AutoBuild from instantly building
				// again next tick
				// TODO: add right click event to fix this problem
				mc.rightClickDelayTimer = 3;
			}else
			{
				// initialize building process
				blockIndex = 0;
				building = true;
				mc.rightClickDelayTimer = 4;
			}
			
			return;
		}
		
		// place next block
		if(building && blockIndex < positions.size()
			&& (mc.rightClickDelayTimer == 0
				|| wurst.mods.fastPlaceMod.isActive()))
		{
			BlockPos pos = positions.get(blockIndex);
			
			if(BlockUtils.getMaterial(pos) == Material.AIR)
				BlockUtils.placeBlockLegit(pos);
			else
			{
				blockIndex++;
				if(blockIndex == positions.size())
					building = false;
			}
		}
	}
	
	public int getTemplate()
	{
		return template;
	}
	
	public void setTemplate(int template)
	{
		((ModeSetting)settings.get(0)).setSelected(template);
	}
}
