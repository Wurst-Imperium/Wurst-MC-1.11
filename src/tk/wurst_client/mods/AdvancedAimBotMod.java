/*
 * Copyright © 2014 - 2016 | Wurst-Imperium | All rights reserved.
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.mods;

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import tk.wurst_client.events.listeners.UpdateListener;
import tk.wurst_client.mods.Mod.Bypasses;
import tk.wurst_client.mods.Mod.Info;
import tk.wurst_client.navigator.NavigatorItem;
import tk.wurst_client.navigator.settings.CheckboxSetting;
import tk.wurst_client.navigator.settings.ModeSetting;
import tk.wurst_client.navigator.settings.SliderSetting;
import tk.wurst_client.navigator.settings.SliderSetting.ValueDisplay;
import tk.wurst_client.utils.EntityUtils;
import tk.wurst_client.utils.EntityUtils.TargetSettings;

@Info(description = "Automaticly aims you at an entity.\n\n",
	name = "AdvancedAimBot",
	tags = "AdvancedAimBot, advanced, aimbot,",
	help = "Mods/AdvancedAimBot")
@Bypasses(antiCheat = true, olderNCP = true, latestNCP = true, ghostMode = true)
public class AdvancedAimBotMod extends Mod implements UpdateListener
{
	public Entity target;
	//public KeyBinding aim = new KeyBinding("AimBot Key", 0, "Misc");
	
	public CheckboxSetting track =
		new CheckboxSetting("Tracks the closest valid entity.", false);
	/*public CheckboxSetting mouse =
		new CheckboxSetting("Tacks entity closest to the mouse,\n"
			+ "instead of closest to the player.", false);*/
	public final ModeSetting mode = new ModeSetting("Mode", new String[]{
		"Distance", "Mouse", "Weighted"}, 0);
	public CheckboxSetting right =
		new CheckboxSetting("Aims on right click.", false);
	public CheckboxSetting left =
		new CheckboxSetting("Aims on left click.", false);
	public final SliderSetting range =
		new SliderSetting("Range", 32, 0.05, 128, 0.05, ValueDisplay.DECIMAL);
	public SliderSetting fov =
		new SliderSetting("FOV", 360, 30, 360, 10, ValueDisplay.DEGREES);
	public TargetSettings targetSettings = new TargetSettings()
	{
		@Override
		public float getRange()
		{
			return range.getValueF();
		}
		
		@Override
		public float getFOV()
		{
			return fov.getValueF();
		}
	};
	
	@Override
	public void initSettings()
	{
		settings.add(left);
		settings.add(right);
		settings.add(track);
		//settings.add(mouse);
		settings.add(mode);
		settings.add(range);
		settings.add(fov);
	}
	
	@Override
	public NavigatorItem[] getSeeAlso()
	{
		return new NavigatorItem[]{wurst.mods.advancedTriggerBotMod};
	}
	
	@Override
	public void onEnable()
	{
		wurst.events.add(UpdateListener.class, this);
	}
	
	@Override
	public void onUpdate()
	{
		if(wurst.mods.advancedTriggerBotMod.isEnabled())
		{
			wurst.mods.advancedTriggerBotMod.range.lockToValue(this.range.getValue());
		}else
		{
			wurst.mods.advancedTriggerBotMod.range.unlock();;
		}
		
		
		switch(mode.getSelected())
		{
			case 0:
				{
					target = EntityUtils.getClosestEntity(targetSettings);
				}
			case 1:
				{
					target = EntityUtils.getClosestEntityToMouse(targetSettings);
				}
			case 2:
				{
					target = EntityUtils.getClosestEntityWeighted(targetSettings);
				}
			default:
				{
					target = null;
				}
		}
		/*
		if(mouse.isChecked())
		{
			target = EntityUtils.getClosestEntityToMouse(targetSettings);
		}else
		{
			target = EntityUtils.getClosestEntity(targetSettings);
		}*/
		
		if((mc.gameSettings.keyBindAttack.pressed && left.isChecked())
			|| (mc.gameSettings.keyBindUseItem.pressed && right.isChecked())
			|| track.isChecked())
		/* || (aim.pressed) */
		{
			if(target == null)
				return;
			if(mc.player.getDistanceToEntity(target) > range.getValueF())
				return;
			if(wurst.mods.advancedTriggerBotMod.isEnabled())
				wurst.mods.advancedTriggerBotMod.lookingEntity = target;
			if(!EntityUtils.faceEntityClient(target))
				return;
		}
		
	}
	
	@Override
	public void onDisable()
	{
		wurst.events.remove(UpdateListener.class, this);
	}
	
}
