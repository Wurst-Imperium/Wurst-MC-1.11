/*
 * Copyright © 2014 - 2017 | Wurst-Imperium | All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.wurstclient.gui;

import static org.lwjgl.opengl.GL11.*;

import java.awt.Color;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.ResourceLocation;
import net.wurstclient.WurstClient;
import net.wurstclient.compatibility.WMinecraft;
import net.wurstclient.events.GUIRenderEvent;
import net.wurstclient.font.Fonts;
import net.wurstclient.utils.RenderUtils;

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
		glEnable(GL_BLEND);
		glDisable(GL_CULL_FACE);
		glDisable(GL_TEXTURE_2D);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		RenderUtils.setColor(new Color(255, 255, 255, 128));
		
		// get version string
		String version = "v" + WurstClient.VERSION + " MC" + WMinecraft.VERSION;
		if(WMinecraft.OPTIFINE)
			version += " OF";
		if(WurstClient.INSTANCE.updater.isOutdated())
			version += " (outdated)";
		
		// draw version background
		glBegin(GL_QUADS);
		{
			glVertex2d(0, 6);
			glVertex2d(Fonts.segoe22.getStringWidth(version) + 78, 6);
			glVertex2d(Fonts.segoe22.getStringWidth(version) + 78, 18);
			glVertex2d(0, 18);
		}
		glEnd();
		
		// draw version string
		glEnable(GL_TEXTURE_2D);
		glEnable(GL_CULL_FACE);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDepthMask(false);
		Fonts.segoe22.drawString(version, 74, 4, 0xFF000000);
		
		// mod list
		modList.render();
		
		// Wurst logo
		OpenGlHelper.glBlendFunc(770, 771, 1, 0);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(wurstLogo);
		int x = 0;
		int y = 3;
		int w = 72;
		int h = 18;
		float fw = 72;
		float fh = 18;
		float u = 0;
		float v = 0;
		Gui.drawModalRectWithCustomSizedTexture(x, y, u, v, w, h, fw, fh);
		
		// GUI render event
		WurstClient.INSTANCE.events.fire(GUIRenderEvent.INSTANCE);
		
		// GL resets
		GL11.glDepthMask(true);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
	}
}
