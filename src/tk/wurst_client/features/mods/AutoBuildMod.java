/*
 * Copyright © 2014 - 2017 | Wurst-Imperium | All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.features.mods;

import java.util.ArrayList;
import java.util.TreeMap;

import org.lwjgl.opengl.GL11;

import net.minecraft.block.material.Material;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import tk.wurst_client.ai.AutoBuildAI;
import tk.wurst_client.events.RightClickEvent;
import tk.wurst_client.events.listeners.RenderListener;
import tk.wurst_client.events.listeners.RightClickListener;
import tk.wurst_client.events.listeners.UpdateListener;
import tk.wurst_client.features.Feature;
import tk.wurst_client.features.special_features.YesCheatSpf.BypassLevel;
import tk.wurst_client.settings.CheckboxSetting;
import tk.wurst_client.settings.ModeSetting;
import tk.wurst_client.utils.BlockUtils;
import tk.wurst_client.utils.RenderUtils;
import tk.wurst_client.utils.RotationUtils;

@Mod.Info(
	description = "Automatically builds the selected template whenever you place a block.\n"
		+ "Can fully bypass NoCheat+ while YesCheat+ is enabled.\n"
		+ "Templates can be customized. Press the \"Help\" button for details.",
	name = "AutoBuild",
	tags = "AutoBridge, AutoFloor, AutoNazi, AutoPenis, AutoPillar, AutoWall, AutoWurst, auto build",
	help = "Mods/AutoBuild")
@Mod.Bypasses
@Mod.DontSaveState
public final class AutoBuildMod extends Mod
	implements RightClickListener, UpdateListener, RenderListener
{
	public ModeSetting mode =
		new ModeSetting("Mode", new String[]{"Fast", "Legit"}, 0);
	public CheckboxSetting useAi =
		new CheckboxSetting("Use AI (experimental)", false)
		{
			@Override
			public void update()
			{
				if(!isChecked() && ai != null)
				{
					ai.stop();
					ai = null;
				}
			}
		};
	public ModeSetting template;
	
	private int[][][] templates;
	private int blockIndex;
	private final ArrayList<BlockPos> positions = new ArrayList<>();
	
	private AutoBuildAI ai;
	
	@Override
	public String getRenderName()
	{
		String name = getName() + " [" + template.getSelectedMode() + "]";
		
		if(blockIndex > 0)
			name +=
				" " + (int)((float)blockIndex / (float)positions.size() * 100)
					+ "%";
		
		return name;
	}
	
	public void setTemplates(TreeMap<String, int[][]> templates)
	{
		settings.clear();
		settings.add(mode);
		settings.add(useAi);
		
		this.templates =
			templates.values().toArray(new int[templates.size()][][]);
		
		int selected;
		if(template != null && template.getSelected() < templates.size())
			selected = template.getSelected();
		else
			selected = 0;
		
		template = new ModeSetting("Template",
			templates.keySet().toArray(new String[templates.size()]), selected);
		
		settings.add(template);
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
		wurst.events.add(RightClickListener.class, this);
	}
	
	@Override
	public void onDisable()
	{
		wurst.events.remove(RightClickListener.class, this);
		wurst.events.remove(UpdateListener.class, this);
		wurst.events.remove(RenderListener.class, this);
		
		blockIndex = 0;
		
		if(ai != null)
		{
			ai.stop();
			ai = null;
		}
	}
	
	@Override
	public void onRightClick(RightClickEvent event)
	{
		// check hitResult
		if(mc.objectMouseOver == null
			|| mc.objectMouseOver.typeOfHit != RayTraceResult.Type.BLOCK
			|| mc.objectMouseOver.getBlockPos() == null || BlockUtils
				.getMaterial(mc.objectMouseOver.getBlockPos()) == Material.AIR)
			return;
		
		// get start pos and facings
		BlockPos startPos =
			mc.objectMouseOver.getBlockPos().offset(mc.objectMouseOver.sideHit);
		EnumFacing front = mc.player.getHorizontalFacing();
		EnumFacing left = front.rotateYCCW();
		
		// set positions
		positions.clear();
		for(int[] pos : templates[template.getSelected()])
			positions.add(
				startPos.up(pos[1]).offset(front, pos[2]).offset(left, pos[0]));
		
		if(mode.getSelected() == 0 && positions.size() <= 64)
		{
			// build instantly
			for(BlockPos pos : positions)
				if(BlockUtils.getMaterial(pos) == Material.AIR)
					BlockUtils.placeBlockSimple(pos);
				
		}else
		{
			// initialize building process
			wurst.events.add(UpdateListener.class, this);
			wurst.events.add(RenderListener.class, this);
			wurst.events.remove(RightClickListener.class, this);
		}
	}
	
	@Override
	public void onUpdate()
	{
		// get next block
		BlockPos pos = positions.get(blockIndex);
		
		// skip already placed blocks
		while(BlockUtils.getMaterial(pos) != Material.AIR)
		{
			blockIndex++;
			
			// stop if done
			if(blockIndex == positions.size())
			{
				wurst.events.remove(UpdateListener.class, this);
				wurst.events.remove(RenderListener.class, this);
				wurst.events.add(RightClickListener.class, this);
				
				blockIndex = 0;
				
				if(ai != null)
				{
					ai.stop();
					ai = null;
				}
				
				return;
			}else
				pos = positions.get(blockIndex);
		}
		
		// move automatically
		if(useAi.isChecked())
		{
			BlockPos playerPos = new BlockPos(mc.player);
			Vec3d eyesPos = RotationUtils.getEyesPos();
			if(playerPos.equals(pos) || playerPos.equals(pos.down())
				|| eyesPos.squareDistanceTo(new Vec3d(pos).addVector(0.5, 0.5,
					0.5)) > (mode.getSelected() == 0 ? 30.25 : 14.0625))
			{
				if(ai != null && (ai.isFailed() || ai.isDone()
					|| !ai.getGoal().equals(pos)))
				{
					ai.stop();
					ai = null;
				}
				
				if(ai == null)
					ai = new AutoBuildAI(pos);
				
				ai.update();
				
			}else if(ai != null)
			{
				ai.stop();
				ai = null;
			}
		}
		
		// fast mode
		if(mode.getSelected() == 0)
			// place next 64 blocks
			for(int i = blockIndex; i < positions.size()
				&& i < blockIndex + 64; i++)
			{
				pos = positions.get(i);
				if(BlockUtils.getMaterial(pos) == Material.AIR)
					BlockUtils.placeBlockSimple(pos);
			}
		else if(mode.getSelected() == 1)
		{
			// wait for right click timer
			if(mc.rightClickDelayTimer > 0)
				return;
			
			// place next block
			BlockUtils.placeBlockLegit(pos);
		}
	}
	
	@Override
	public void onRender(float partialTicks)
	{
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
		
		int greenBoxes = mode.getSelected() == 0 ? 64 : 1;
		
		// green boxes
		for(int i = blockIndex; i < positions.size()
			&& i < blockIndex + greenBoxes; i++)
		{
			BlockPos pos = positions.get(i);
			
			GL11.glPushMatrix();
			GL11.glTranslated(pos.getX(), pos.getY(), pos.getZ());
			GL11.glTranslated(offset, offset, offset);
			GL11.glScaled(scale, scale, scale);
			
			GL11.glDepthMask(false);
			GL11.glColor4f(0F, 1F, 0F, 0.15F);
			RenderUtils.drawSolidBox();
			GL11.glDepthMask(true);
			
			GL11.glColor4f(0F, 0F, 0F, 0.5F);
			RenderUtils.drawOutlinedBox();
			
			GL11.glPopMatrix();
		}
		
		// black outlines
		for(int i = blockIndex + greenBoxes; i < positions.size()
			&& i < blockIndex + 1024; i++)
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
		GL11.glDisable(GL11.GL_LINE_SMOOTH);
	}
	
	@Override
	public void onYesCheatUpdate(BypassLevel bypassLevel)
	{
		if(bypassLevel.ordinal() >= BypassLevel.ANTICHEAT.ordinal())
			mode.lock(1);
		else
			mode.unlock();
	}
}
