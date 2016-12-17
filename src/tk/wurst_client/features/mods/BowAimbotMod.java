/*
 * Copyright © 2014 - 2016 | Wurst-Imperium | All rights reserved.
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.features.mods;

import static org.lwjgl.opengl.GL11.*;

import java.awt.Color;

import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import tk.wurst_client.events.listeners.GUIRenderListener;
import tk.wurst_client.events.listeners.RenderListener;
import tk.wurst_client.events.listeners.UpdateListener;
import tk.wurst_client.features.Feature;
import tk.wurst_client.font.Fonts;
import tk.wurst_client.utils.EntityUtils;
import tk.wurst_client.utils.EntityUtils.TargetSettings;
import tk.wurst_client.utils.RenderUtils;

@Mod.Info(
	description = "Automatically aims your bow at the closest entity.\n"
		+ "Tip: This works with FastBow.",
	name = "BowAimbot",
	tags = "bow aimbot",
	help = "Mods/BowAimbot")
@Mod.Bypasses
public class BowAimbotMod extends Mod
	implements UpdateListener, RenderListener, GUIRenderListener
{
	private Entity target;
	private float velocity;
	
	private TargetSettings targetSettings = new TargetSettings();
	
	@Override
	public Feature[] getSeeAlso()
	{
		return new Feature[]{wurst.mods.fastBowMod};
	}
	
	@Override
	public void onEnable()
	{
		wurst.events.add(GUIRenderListener.class, this);
		wurst.events.add(RenderListener.class, this);
		wurst.events.add(UpdateListener.class, this);
	}
	
	@Override
	public void onDisable()
	{
		wurst.events.remove(GUIRenderListener.class, this);
		wurst.events.remove(RenderListener.class, this);
		wurst.events.remove(UpdateListener.class, this);
	}
	
	@Override
	public void onRender()
	{
		if(target == null)
			return;
		RenderUtils.entityESPBox(target, 3);
	}
	
	@Override
	public void onRenderGUI()
	{
		if(target == null || velocity < 0.1)
			return;
		
		glEnable(GL_BLEND);
		glDisable(GL_CULL_FACE);
		glDisable(GL_TEXTURE_2D);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		RenderUtils.setColor(new Color(8, 8, 8, 128));
		ScaledResolution sr = new ScaledResolution(mc);
		int width = sr.getScaledWidth();
		int height = sr.getScaledHeight();
		String targetLocked = "Target locked";
		glBegin(GL_QUADS);
		{
			glVertex2d(width / 2 + 1, height / 2 + 1);
			glVertex2d(
				width / 2 + Fonts.segoe15.getStringWidth(targetLocked) + 4,
				height / 2 + 1);
			glVertex2d(
				width / 2 + Fonts.segoe15.getStringWidth(targetLocked) + 4,
				height / 2 + Fonts.segoe15.FONT_HEIGHT + 2);
			glVertex2d(width / 2 + 1,
				height / 2 + Fonts.segoe15.FONT_HEIGHT + 2);
		}
		glEnd();
		glEnable(GL_TEXTURE_2D);
		Fonts.segoe15.drawStringWithShadow(targetLocked, width / 2 + 2,
			height / 2, 0xffffffff);
		glEnable(GL_CULL_FACE);
		glDisable(GL_BLEND);
	}
	
	@Override
	public void onUpdate()
	{
		// reset target
		target = null;
		
		// check if using item
		if(!mc.gameSettings.keyBindUseItem.pressed)
			return;
		
		// check if item is bow
		ItemStack item = mc.player.inventory.getCurrentItem();
		if(item == null || !(item.getItem() instanceof ItemBow))
			return;
		
		// set target
		target = EntityUtils.getClosestEntity(targetSettings);
		if(target == null)
			return;
		
		// set velocity
		velocity = mc.player.getItemInUseMaxCount() / 20;
		velocity = (velocity * velocity + velocity * 2) / 3;
		if(velocity > 1)
			velocity = 1;
		
		// adjust for FastBow
		if(wurst.mods.fastBowMod.isActive())
			velocity = 1;
		
		// abort if velocity is too low
		if(velocity < 0.1)
		{
			if(target instanceof EntityLivingBase)
				EntityUtils.faceEntityClient((EntityLivingBase)target);
			return;
		}
		
		// set position to aim at
		double posX =
			target.posX + (target.posX - target.prevPosX) * 5 - mc.player.posX;
		double posY = target.posY + (target.posY - target.prevPosY) * 5
			+ target.getEyeHeight() - 0.15 - mc.player.posY
			- mc.player.getEyeHeight();
		double posZ =
			target.posZ + (target.posZ - target.prevPosZ) * 5 - mc.player.posZ;
		
		// set yaw
		mc.player.rotationYaw =
			(float)(Math.atan2(posZ, posX) * 180 / Math.PI) - 90;
		
		// set pitch
		double distanceXZ = Math.sqrt(posX * posX + posZ * posZ);
		float g = 0.006F;
		mc.player.rotationPitch =
			(float)-Math
				.toDegrees(
					Math.atan((velocity * velocity
						- Math.sqrt(
							(float)(velocity * velocity * velocity
								* velocity - g * (g * (distanceXZ * distanceXZ)
									+ 2 * posY * (velocity * velocity)))))
						/ (g * distanceXZ)));
	}
}
