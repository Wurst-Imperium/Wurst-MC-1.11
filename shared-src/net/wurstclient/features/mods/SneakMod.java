/*
 * Copyright © 2014 - 2017 | Wurst-Imperium | All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.wurstclient.features.mods;

import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketEntityAction.Action;
import net.wurstclient.compatibility.WConnection;
import net.wurstclient.compatibility.WMinecraft;
import net.wurstclient.events.listeners.PostUpdateListener;
import net.wurstclient.events.listeners.UpdateListener;
import net.wurstclient.features.HelpPage;
import net.wurstclient.features.Mod;
import net.wurstclient.features.SearchTags;
import net.wurstclient.features.special_features.YesCheatSpf.Profile;

@SearchTags({"AutoSneaking"})
@HelpPage("Mods/Sneak")
@Mod.Bypasses(ghostMode = false)
public final class SneakMod extends Mod
	implements UpdateListener, PostUpdateListener
{
	public SneakMod()
	{
		super("Sneak", "Automatically sneaks all the time.");
	}
	
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
		WConnection.sendPacket(new CPacketEntityAction(WMinecraft.getPlayer(),
			Action.STOP_SNEAKING));
	}
	
	@Override
	public void onUpdate()
	{
		if(wurst.special.yesCheatSpf.getProfile().ordinal() >= Profile.OLDER_NCP
			.ordinal())
		{
			WConnection.sendPacket(new CPacketEntityAction(
				WMinecraft.getPlayer(), Action.START_SNEAKING));
			WConnection.sendPacket(new CPacketEntityAction(
				WMinecraft.getPlayer(), Action.STOP_SNEAKING));
		}else
			WConnection.sendPacket(new CPacketEntityAction(
				WMinecraft.getPlayer(), Action.START_SNEAKING));
	}
	
	@Override
	public void afterUpdate()
	{
		WConnection.sendPacket(new CPacketEntityAction(WMinecraft.getPlayer(),
			Action.STOP_SNEAKING));
		WConnection.sendPacket(new CPacketEntityAction(WMinecraft.getPlayer(),
			Action.START_SNEAKING));
	}
}
