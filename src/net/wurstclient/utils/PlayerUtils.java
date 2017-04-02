/*
 * Copyright © 2014 - 2017 | Wurst-Imperium | All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.wurstclient.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.wurstclient.compatibility.WMinecraft;

public class PlayerUtils
{
	private static final Minecraft mc = Minecraft.getMinecraft();
	
	public static void swingArmClient()
	{
		WMinecraft.getPlayer().swingArm(EnumHand.MAIN_HAND);
	}
	
	public static void swingArmPacket()
	{
		WMinecraft.getPlayer().connection
			.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
	}
	
	public static float getCooldown()
	{
		return WMinecraft.getPlayer().getCooledAttackStrength(0);
	}
	
	public static void processRightClick()
	{
		mc.playerController.processRightClick(WMinecraft.getPlayer(),
			WMinecraft.getWorld(), EnumHand.MAIN_HAND);
	}
	
	static void processRightClickBlock(BlockPos pos, EnumFacing side,
		Vec3d hitVec)
	{
		mc.playerController.processRightClickBlock(WMinecraft.getPlayer(),
			WMinecraft.getWorld(), pos, side, hitVec, EnumHand.MAIN_HAND);
	}
	
	public static void addPotionEffect(Potion potion)
	{
		WMinecraft.getPlayer()
			.addPotionEffect(new PotionEffect(potion, 10801220));
	}
	
	public static void removePotionEffect(Potion potion)
	{
		WMinecraft.getPlayer().removePotionEffect(potion);
	}
}
