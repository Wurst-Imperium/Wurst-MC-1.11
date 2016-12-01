/*
 * Copyright © 2014 - 2016 | Wurst-Imperium | All rights reserved.
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.mods;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerDigging.Action;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import tk.wurst_client.events.listeners.LeftClickListener;
import tk.wurst_client.events.listeners.RightClickListener;
import tk.wurst_client.events.listeners.UpdateListener;
import tk.wurst_client.mods.Mod.Bypasses;
import tk.wurst_client.mods.Mod.Info;
import tk.wurst_client.navigator.NavigatorItem;
import tk.wurst_client.navigator.settings.CheckboxSetting;
import tk.wurst_client.navigator.settings.ModeSetting;
import tk.wurst_client.navigator.settings.SliderSetting;
import tk.wurst_client.navigator.settings.SliderSetting.ValueDisplay;
import tk.wurst_client.special.YesCheatSpf.BypassLevel;
import tk.wurst_client.utils.BlockUtils;

@Info(
	description = "Increases the distance at which you\n"
		+ "can place and destroy blocks.\n\n",
	name = "BlockReach",
	tags = "BlockReach, reach, distance",
	help = "Mods/BlockReach")
@Bypasses(
	antiCheat = false,
	olderNCP = false,
	latestNCP = false,
	ghostMode = false)
public class BlockReachMod extends Mod implements UpdateListener
{
	public float reach = 4.5F;
	private boolean flag = false;
	
	public CheckboxSetting auto = new CheckboxSetting("Set reach to 100 blocks.\n\n", false)
	{
		@Override
		public void update()
		{
			if(isChecked())
			{
				flag = true;
			}
			else
			{
				flag = false;
			}
		};
	};
	public final SliderSetting distance = new SliderSetting("Distance", 10, 1, 10,
		0.05, ValueDisplay.DECIMAL);
	
	@Override
	public void initSettings()
	{
		settings.add(auto);
		settings.add(distance);
	}
	
	@Override
	public String getRenderName()
	{
		return "BlockReach";
	}
	
	@Override
	public NavigatorItem[] getSeeAlso()
	{
		return new NavigatorItem[]{};
	}
	
	@Override
	public void onEnable()
	{
		wurst.events.add(UpdateListener.class, this);
	}
	
	@Override
	public void onUpdate()
	{
		reach = !flag ? distance.getValueF() : 100.0F;
	}
	
	@Override
	public void onDisable()
	{
		wurst.events.remove(UpdateListener.class, this);
	}
	
}
