/*
 * Copyright © 2014 - 2017 | Wurst-Imperium | All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.features.mods;

import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketEntityAction.Action;
import tk.wurst_client.events.listeners.PostUpdateListener;
import tk.wurst_client.events.listeners.UpdateListener;
import tk.wurst_client.features.special_features.YesCheatSpf.BypassLevel;

@Mod.Info(description = "Automatically sneaks all the time.",
	name = "Sneak",
	tags = "AutoSneaking",
	help = "Mods/Sneak")
@Mod.Bypasses(ghostMode = false)
public class SneakMod extends Mod implements UpdateListener, PostUpdateListener
{
	@Override
	public void onEnable()
	{
		wurst.events.add(UpdateListener.class, this);
		wurst.events.add(PostUpdateListener.class, this);
	}
	
	@Override
	public void onDisable()
	{
		wurst.events.remove(UpdateListener.class, this);
		wurst.events.remove(PostUpdateListener.class, this);
		
		mc.gameSettings.keyBindSneak.pressed = false;
		mc.player.connection.sendPacket(
			new CPacketEntityAction(mc.player, Action.STOP_SNEAKING));
	}
	
	@Override
	public void onUpdate()
	{
		if(wurst.special.yesCheatSpf.getBypassLevel()
			.ordinal() >= BypassLevel.OLDER_NCP.ordinal())
		{
			mc.player.connection.sendPacket(
				new CPacketEntityAction(mc.player, Action.START_SNEAKING));
			mc.player.connection.sendPacket(
				new CPacketEntityAction(mc.player, Action.STOP_SNEAKING));
		}else
			mc.player.connection.sendPacket(
				new CPacketEntityAction(mc.player, Action.START_SNEAKING));
	}
	
	@Override
	public void afterUpdate()
	{
		mc.player.connection.sendPacket(
			new CPacketEntityAction(mc.player, Action.STOP_SNEAKING));
		mc.player.connection.sendPacket(
			new CPacketEntityAction(mc.player, Action.START_SNEAKING));
	}
}
