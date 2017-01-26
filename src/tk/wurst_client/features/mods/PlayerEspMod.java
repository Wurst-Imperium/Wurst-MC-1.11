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
import net.minecraft.util.math.AxisAlignedBB;
import tk.wurst_client.events.listeners.RenderListener;
import tk.wurst_client.features.Feature;
import tk.wurst_client.utils.RenderUtils;

@Mod.Info(description = "Allows you to see players through walls.",
	name = "PlayerESP",
	tags = "player esp",
	help = "Mods/PlayerESP")
@Mod.Bypasses
public class PlayerEspMod extends Mod implements RenderListener
{
	private static final AxisAlignedBB PLAYER_BOX =
		new AxisAlignedBB(-0.35, 0, -0.35, 0.35, 1.9, 0.35);
	
	@Override
	public Feature[] getSeeAlso()
	{
		return new Feature[]{wurst.mods.tracersMod, wurst.mods.playerFinderMod,
			wurst.mods.mobEspMod, wurst.mods.prophuntEspMod};
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
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		
		GL11.glPushMatrix();
		GL11.glTranslated(-mc.getRenderManager().renderPosX,
			-mc.getRenderManager().renderPosY,
			-mc.getRenderManager().renderPosZ);
		
		// draw boxes
		for(EntityPlayer entity : mc.world.playerEntities)
		{
			if(entity == mc.player)
				continue;
			
			// set color
			if(wurst.friends.contains(entity.getName()))
				GL11.glColor4f(0, 0, 1, 0.5F);
			else
			{
				float factor = mc.player.getDistanceToEntity(entity) / 20F;
				if(factor > 2)
					factor = 2;
				GL11.glColor4f(2 - factor, factor, 0, 0.5F);
			}
			
			// set position
			GL11.glPushMatrix();
			GL11.glTranslated(entity.posX, entity.posY, entity.posZ);
			
			// draw box
			RenderUtils.drawOutlinedBox(PLAYER_BOX);
			
			GL11.glPopMatrix();
		}
		
		GL11.glPopMatrix();
		
		// GL resets
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_LINE_SMOOTH);
	}
}
