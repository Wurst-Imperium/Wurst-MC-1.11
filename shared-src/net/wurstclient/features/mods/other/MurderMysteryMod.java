/*
 * Copyright © 2014 - 2017 | Wurst-Imperium | All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.wurstclient.features.mods.other;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemSword;
import net.minecraft.util.math.AxisAlignedBB;
import net.wurstclient.compatibility.WMinecraft;
import net.wurstclient.events.listeners.RenderListener;
import net.wurstclient.events.listeners.UpdateListener;
import net.wurstclient.features.Mod;
import net.wurstclient.features.SearchTags;
import net.wurstclient.utils.ChatUtils;
import net.wurstclient.utils.RenderUtils;

@SearchTags({"hypixel"})
@Mod.Bypasses
public final class MurderMysteryMod extends Mod
	implements UpdateListener, RenderListener
{
	private static final AxisAlignedBB PLAYER_BOX =
		new AxisAlignedBB(-0.35, 0, -0.35, 0.35, 1.9, 0.35);
	private String murderer = "";
	private boolean renderNotice = false;

	public MurderMysteryMod()
	{
		super("MurderMystery",
			"Tells you who is the murderer in Murder Mystery."
				+ "\nJust keep the mod enabled, the murderer will updated once the new murderer is found.");
		// setCategory(Category.OTHER);
	}

	@Override
	public String getRenderName()
	{
		if(murderer.equals(""))
			return "MurderMystery [Scanning]";
		else
			return "MurderMystery [Murderer: " + murderer + "]";
	}

	@Override
	public void onDisable()
	{
		wurst.events.remove(UpdateListener.class, this);
		wurst.events.remove(RenderListener.class, this);
	}

	@Override
	public void onEnable()
	{
		murderer = "";
		renderNotice = false;
		wurst.events.add(UpdateListener.class, this);
		wurst.events.add(RenderListener.class, this);
		if(isUserNotMurderer())
			whoHasSword();
	}

	@Override
	public void onRender(float partialTicks)
	{
		if(murderer.equals("") || murderer.equals("YOU!"))
			return;

		// GL settings
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glEnable(GL11.GL_LINE_SMOOTH);
		GL11.glLineWidth(6);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_DEPTH_TEST);

		GL11.glPushMatrix();
		GL11.glTranslated(-mc.getRenderManager().renderPosX,
			-mc.getRenderManager().renderPosY,
			-mc.getRenderManager().renderPosZ);

		// draw boxes
		for(EntityPlayer entity : WMinecraft.getWorld().playerEntities)
		{
			if(!entity.getName().equals(murderer))
				continue;

			// set color
			GL11.glColor4f(1F, 0, 1, 0.2F);

			// set position
			GL11.glPushMatrix();
			GL11.glTranslated(
				entity.prevPosX
					+ (entity.posX - entity.prevPosX) * partialTicks,
				entity.prevPosY
					+ (entity.posY - entity.prevPosY) * partialTicks,
				entity.prevPosZ
					+ (entity.posZ - entity.prevPosZ) * partialTicks);

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

	@Override
	public void onUpdate()
	{
		updateMS();
		if(!hasTimePassedM(250))
			return;
		if(isUserNotMurderer())
			whoHasSword();
		updateLastMS();
	}

	private boolean isUserNotMurderer()
	{
		if(murderer.equals("YOU!"))
			if(WMinecraft.getPlayer().inventory.getStackInSlot(1)
				.getItem() instanceof ItemSword)
				return false;
			else
				return true;
		if(WMinecraft.getPlayer().inventory.getStackInSlot(1)
			.getItem() instanceof ItemSword)
		{
			murderer = "YOU!";
			ChatUtils.message("You are the murderer. Good luck!");
			return false;
		}
		return true;
	}

	private void whoHasSword()
	{
		for(Object entity : WMinecraft.getWorld().loadedEntityList)
			if(entity instanceof EntityOtherPlayerMP)
			{
				EntityOtherPlayerMP player = (EntityOtherPlayerMP)entity;
				if(player.inventory.getCurrentItem()
					.getItem() instanceof ItemSword)
				{
					if(player.getDisplayName().getUnformattedText()
						.contains("[NPC]"))
						continue;
					if(murderer.equals(player.getName()))
						break;
					murderer = player.getName();
					ChatUtils
						.message("The murderer in this game is: §a" + murderer);
					if(renderNotice == false)
					{
						ChatUtils.message(
							"Wurst will show you the murderer until you disable this mod.");
						renderNotice = true;
					}
					break;
				}
			}
	}
}
