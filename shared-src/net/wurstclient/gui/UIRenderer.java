/*
 * Copyright © 2014 - 2017 | Wurst-Imperium | All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.wurstclient.gui;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.ResourceLocation;
import net.wurstclient.WurstClient;
import net.wurstclient.compatibility.WMinecraft;
import net.wurstclient.events.GUIRenderEvent;
import net.wurstclient.font.Fonts;

public class UIRenderer
{
	private static final ResourceLocation wurstLogo =
		new ResourceLocation("wurst/wurst_128.png");
	private static final ModList modList = new ModList();
	
	public static void renderUI(float zLevel)
	{
		if(!WurstClient.INSTANCE.isEnabled())
			return;
		
		// GL settings
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glColor4f(1, 1, 1, 0.5F);
		
		// get version string
		String version = "v" + WurstClient.VERSION + " MC" + WMinecraft.VERSION;
		if(WMinecraft.OPTIFINE)
			version += " OF";
		if(WurstClient.INSTANCE.updater.isOutdated())
			version += " (outdated)";
		
		// draw version background
		GL11.glBegin(GL11.GL_QUADS);
		{
			GL11.glVertex2d(0, 6);
			GL11.glVertex2d(Fonts.segoe22.getStringWidth(version) + 76, 6);
			GL11.glVertex2d(Fonts.segoe22.getStringWidth(version) + 76, 18);
			GL11.glVertex2d(0, 18);
		}
		GL11.glEnd();
		
		// draw version string
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDepthMask(false);
		Fonts.segoe22.drawString(version, 74, 4, 0xFF000000);
		
		// Wurst logo
		GL11.glColor4f(1, 1, 1, 1);
		Minecraft.getMinecraft().getTextureManager().bindTexture(wurstLogo);
		Gui.drawModalRectWithCustomSizedTexture(0, 3, 0, 0, 72, 18, 72, 18);
		
		// mod list
		modList.render();
		
		// GUI render event
		WurstClient.INSTANCE.events.fire(GUIRenderEvent.INSTANCE);
		
		// GL resets
		GL11.glDepthMask(true);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glColor4f(1, 1, 1, 1);
	}
}
