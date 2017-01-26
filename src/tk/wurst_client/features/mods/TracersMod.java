/*
 * Copyright © 2014 - 2017 | Wurst-Imperium | All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.features.mods;

import org.lwjgl.opengl.GL11;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;
import tk.wurst_client.events.listeners.RenderListener;
import tk.wurst_client.features.Feature;
import tk.wurst_client.utils.RotationUtils;

@Mod.Info(description = "Draws lines to players around you.",
	name = "Tracers",
	help = "Mods/Tracers")
@Mod.Bypasses
public class TracersMod extends Mod implements RenderListener
{
	@Override
	public Feature[] getSeeAlso()
	{
		return new Feature[]{wurst.mods.playerEspMod,
			wurst.mods.playerFinderMod};
	}
	
	@Override
	public void onEnable()
	{
		wurst.events.add(RenderListener.class, this);
	}
	
	@Override
	public void onDisable()
	{
		wurst.events.remove(RenderListener.class, this);
	}
	
	@Override
	public void onRender()
	{
		// GL settings
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glEnable(GL11.GL_LINE_SMOOTH);
		GL11.glLineWidth(2);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		
		GL11.glPushMatrix();
		GL11.glTranslated(-mc.getRenderManager().renderPosX,
			-mc.getRenderManager().renderPosY,
			-mc.getRenderManager().renderPosZ);
		
		// set start position
		Vec3d start = RotationUtils.getClientLookVec()
			.addVector(0, mc.player.getEyeHeight(), 0)
			.addVector(mc.getRenderManager().renderPosX,
				mc.getRenderManager().renderPosY,
				mc.getRenderManager().renderPosZ);
		
		GL11.glBegin(GL11.GL_LINES);
		for(EntityPlayer entity : mc.world.playerEntities)
		{
			if(entity == mc.player)
				continue;
			
			// set end position
			Vec3d end = entity.boundingBox.getCenter();
			
			// set color
			if(wurst.friends.contains(entity.getName()))
				GL11.glColor4f(0, 0, 1, 0.5F);
			else
			{
				float factor = mc.player.getDistanceToEntity(entity) / 40F;
				if(factor > 1)
					factor = 1;
				
				GL11.glColor4f(2 - (factor * 2F), factor * 2F, 0, 0.5F);
			}
			
			// draw line
			GL11.glVertex3d(start.xCoord, start.yCoord, start.zCoord);
			GL11.glVertex3d(end.xCoord, end.yCoord, end.zCoord);
		}
		GL11.glEnd();
		
		GL11.glPopMatrix();
		
		// GL resets
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_LINE_SMOOTH);
	}
}
