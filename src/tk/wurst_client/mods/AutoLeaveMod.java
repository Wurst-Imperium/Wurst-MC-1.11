/*
 * Copyright © 2014 - 2016 | Wurst-Imperium | All rights reserved.
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.mods;

import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.util.EnumHand;
import tk.wurst_client.events.listeners.UpdateListener;
import tk.wurst_client.navigator.NavigatorItem;
import tk.wurst_client.navigator.settings.ModeSetting;

@Mod.Info(
	description = "Automatically leaves the server when your health is low.\n"
		+ "The Chars, TP and SelfHurt modes can bypass CombatLog and similar plugins.",
	name = "AutoLeave",
	tags = "AutoDisconnect, auto leave, auto disconnect",
	help = "Mods/AutoLeave")
@Mod.Bypasses
public class AutoLeaveMod extends Mod implements UpdateListener
{
	private int mode = 0;
	private String[] modes = new String[]{"Quit", "Chars", "TP", "SelfHurt"};
	
	@Override
	public NavigatorItem[] getSeeAlso()
	{
		return new NavigatorItem[]{wurst.commands.leaveCmd};
	}
	
	@Override
	public String getRenderName()
	{
		String name = getName() + "[" + modes[mode] + "]";
		return name;
	}
	
	@Override
	public void initSettings()
	{
		settings.add(new ModeSetting("Mode", modes, mode)
		{
			@Override
			public void update()
			{
				mode = getSelected();
			}
		});
	}
	
	@Override
	public void onEnable()
	{
		wurst.events.add(UpdateListener.class, this);
	}
	
	@Override
	public void onUpdate()
	{
		if(mc.player.getHealth() <= 8.0
			&& !mc.player.capabilities.isCreativeMode
			&& (!mc.isIntegratedServerRunning() || Minecraft.getMinecraft().player.connection
				.getPlayerInfoMap().size() > 1))
		{
			switch(mode)
			{
				case 0:
					mc.world.sendQuittingDisconnectingPacket();
					break;
				case 1:
					mc.player.connection
						.sendPacket(new CPacketChatMessage("§"));
					break;
				case 2:
					mc.player.connection
						.sendPacket(new CPacketPlayer.Position(
							3.1e7d, 100, 3.1e7d, false));
					break;
				case 3:
					mc.player.connection
						.sendPacket(new CPacketUseEntity(mc.player,
							EnumHand.MAIN_HAND));
					break;
				default:
					break;
			}
			setEnabled(false);
		}
	}
	
	@Override
	public void onDisable()
	{
		wurst.events.remove(UpdateListener.class, this);
	}
	
	public int getMode()
	{
		return mode;
	}
	
	public void setMode(int mode)
	{
		((ModeSetting)settings.get(1)).setSelected(mode);
	}
	
	public String[] getModes()
	{
		return modes;
	}
}
