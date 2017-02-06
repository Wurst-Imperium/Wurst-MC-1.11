/*
 * Copyright © 2014 - 2017 | Wurst-Imperium | All rights reserved.
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class PlayerUtils
{
	private static final Minecraft mc = Minecraft.getMinecraft();
	
	public static void swingArmClient()
	{
		mc.player.swingArm(EnumHand.MAIN_HAND);
	}
	
	public static void swingArmPacket()
	{
		mc.player.connection
			.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
	}
	
	static void processRightClickBlock(BlockPos pos, EnumFacing side,
		Vec3d hitVec)
	{
		mc.playerController.processRightClickBlock(mc.player, mc.world, pos,
			side, hitVec, EnumHand.MAIN_HAND);
	}
}
