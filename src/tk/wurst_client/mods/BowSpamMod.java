/*
 * Copyright © 2014 - 2016 | Wurst-Imperium | All rights reserved.
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.mods;

import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import tk.wurst_client.events.listeners.UpdateListener;
import tk.wurst_client.mods.Mod.Bypasses;
import tk.wurst_client.mods.Mod.Info;
import tk.wurst_client.navigator.NavigatorItem;
import tk.wurst_client.navigator.settings.CheckboxSetting;
import tk.wurst_client.navigator.settings.SliderSetting;
import tk.wurst_client.navigator.settings.SliderSetting.ValueDisplay;

@Info(description = "Automatically shoots a fully drawn bow.",
	name = "BowSpam",
	tags = "bow spam, bow, spam",
	help = "Mods/BowSpam")
@Bypasses
public class BowSpamMod extends Mod implements UpdateListener
{
	private int counter = 0;
	private int time = 0;
	
	public SliderSetting release = new SliderSetting("Release time (seconds)",
		2, 0.3, 2, 0.05, ValueDisplay.DECIMAL)
	{
		@Override
		public void update()
		{
			time = ((Double)(release.getValue() * 20)).intValue();
		}
	};
	public CheckboxSetting full =
		new CheckboxSetting("Shoots only whel fully charged.", true)
		{
			@Override
			public void update()
			{
				if(isChecked())
				{
					release.lockToValue(1.0D);
				}else
				{
					release.unlock();
				}
			};
		};
	
	@Override
	public void initSettings()
	{
		settings.add(full);
		settings.add(release);
	}
	
	@Override
	public NavigatorItem[] getSeeAlso()
	{
		return new NavigatorItem[]{wurst.mods.bowAimbotMod,
			wurst.mods.fastBowMod};
	}
	
	@Override
	public void onEnable()
	{
		wurst.events.add(UpdateListener.class, this);
	}
	
	@Override
	public void onUpdate()
	{
		
		ItemStack item = mc.player.inventory.getCurrentItem();
		
		// check if bow just fired, if so, continue holding right click
		if(counter == -1)
		{
			mc.gameSettings.keyBindUseItem.pressed = true;
		}
		
		// check if using item and if the item is a bow, if not, reset counter
		if(!mc.gameSettings.keyBindUseItem.pressed || item.isEmpty()
			|| !(item.getItem() instanceof ItemBow))
		{
			counter = 0;
			return;
		}
		
		++counter;
		
		// check if bow is fully drawn (or drawn the set time), if so, fire it
		if(counter >= time)
		{
			counter = -1;
			mc.gameSettings.keyBindUseItem.pressed = false;
			
		}
	}
	
	@Override
	public void onDisable()
	{
		wurst.events.remove(UpdateListener.class, this);
	}
}
