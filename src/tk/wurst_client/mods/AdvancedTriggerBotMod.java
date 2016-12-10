/*
 * Copyright © 2014 - 2016 | Wurst-Imperium | All rights reserved.
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.mods;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import tk.wurst_client.events.listeners.UpdateListener;
import tk.wurst_client.mods.Mod.Bypasses;
import tk.wurst_client.mods.Mod.Info;
import tk.wurst_client.navigator.NavigatorItem;
import tk.wurst_client.navigator.settings.CheckboxSetting;
import tk.wurst_client.navigator.settings.SliderSetting;
import tk.wurst_client.navigator.settings.SliderSetting.ValueDisplay;
import tk.wurst_client.utils.EntityUtils;
import tk.wurst_client.utils.EntityUtils.TargetSettings;

@Info(
	description = "Automatically performs an action at an entity.\n"
		+ "Similar to an autosniper.\n\n",
	name = "AdvancedTriggerBot",
	tags = "advanced, trigger bot",
	help = "Mods/AdvancedTriggerBot")
@Bypasses
public class AdvancedTriggerBotMod extends Mod implements UpdateListener
{
	Entity lookingEntity;
	
	public CheckboxSetting useKillaura =
		new CheckboxSetting("Use Killaura settings", true)
		{
			@Override
			public void update()
			{
				if(isChecked())
				{
					KillauraMod killaura = wurst.mods.killauraMod;
					useCooldown.lock(killaura.useCooldown.isChecked());
					speed.lockToValue(killaura.speed.getValue());
					range.lockToValue(killaura.range.getValue());
				}else
				{
					useCooldown.unlock();
					speed.unlock();
					range.unlock();
				}
			};
		};
	public CheckboxSetting useCooldown =
		new CheckboxSetting("Use Attack Cooldown as Speed", true)
		{
			@Override
			public void update()
			{
				speed.setDisabled(isChecked());
			};
		};
	public CheckboxSetting right =
		new CheckboxSetting("Right clicks at entity.", true)
		{
		
		};
	public CheckboxSetting left =
		new CheckboxSetting("Left clicks at entity.", true)
		{
			@Override
			public void update()
			{
				if(!isChecked())
				{
					useKillaura.lock(false);
					useCooldown.lock(false);
					speed.setDisabled(true);
				}else
				{
					useKillaura.unlock();
					useCooldown.unlock();
					speed.setDisabled(false);
				}
			};
		};
	public SliderSetting speed =
		new SliderSetting("Speed", 20, 0.1, 20, 0.1, ValueDisplay.DECIMAL);
	public SliderSetting range =
		new SliderSetting("Range", 32, 0.05, 128, 0.05, ValueDisplay.DECIMAL);
	
	private TargetSettings targetSettings = new TargetSettings()
	{
		@Override
		public float getRange()
		{
			return range.getValueF();
		}
	};
	
	@Override
	public void initSettings()
	{
		settings.add(right);
		settings.add(left);
		settings.add(useKillaura);
		settings.add(useCooldown);
		settings.add(speed);
		settings.add(range);
	}
	
	@Override
	public NavigatorItem[] getSeeAlso()
	{
		return new NavigatorItem[]{wurst.mods.advancedAimBotMod};
	}
	
	@Override
	public void onEnable()
	{
		wurst.events.add(UpdateListener.class, this);
	}
	
	@Override
	public void onUpdate()
	{
		// update timer
		updateMS();
		
		/*
		
		// lookingEntity = mc.objectMouseOver.entityHit;
		
		// check entity
		lookingEntity = mc.player.rayTrace(range.getValue(),
			mc.timer.renderPartialTicks).entityHit;
		
		*/
		
		if(!EntityUtils.isCorrectEntity(lookingEntity, targetSettings))
			return;
		
		if(left.isChecked())
		{
			if(useCooldown.isChecked()
				? mc.player.getCooledAttackStrength(0F) < 1F
				: !(hasTimePassedS(speed.getValueF()) || speed.isLocked()))
				return;
			
			mc.clickMouse();
		}
		
		if(right.isChecked())
		{
			mc.rightClickMouse();
		}
		
		lookingEntity = null;
		
		// reset timer
		updateLastMS();
	}
	
	@Override
	public void onDisable()
	{
		wurst.events.remove(UpdateListener.class, this);
	}
	
}
