/*
 * Copyright © 2014 - 2017 | Wurst-Imperium | All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.features.mods;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumHand;
import tk.wurst_client.events.listeners.UpdateListener;
import tk.wurst_client.features.Feature;
import tk.wurst_client.features.special_features.YesCheatSpf.BypassLevel;
import tk.wurst_client.settings.CheckboxSetting;
import tk.wurst_client.settings.SliderSetting;
import tk.wurst_client.settings.SliderSetting.ValueDisplay;
import tk.wurst_client.utils.EntityUtils;
import tk.wurst_client.utils.EntityUtils.TargetSettings;
import tk.wurst_client.utils.RotationUtils;

@Mod.Info(
	description = "A bot that automatically fights for you.\n"
		+ "It walks around and kills everything.\n" + "Good for MobArena.",
	name = "FightBot",
	tags = "fight bot",
	help = "Mods/FightBot")
@Mod.Bypasses(ghostMode = false)
public class FightBotMod extends Mod implements UpdateListener
{
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
	public SliderSetting speed =
		new SliderSetting("Speed", 20, 0.1, 20, 0.1, ValueDisplay.DECIMAL);
	public SliderSetting range =
		new SliderSetting("Range", 6, 1, 6, 0.05, ValueDisplay.DECIMAL);
	public SliderSetting distance =
		new SliderSetting("Distance", 3, 1, 6, 0.05, ValueDisplay.DECIMAL);
	
	private TargetSettings followSettings = new TargetSettings();
	private TargetSettings attackSettings = new TargetSettings()
	{
		@Override
		public float getRange()
		{
			return range.getValueF();
		};
	};
	
	@Override
	public void initSettings()
	{
		settings.add(useKillaura);
		settings.add(useCooldown);
		settings.add(speed);
		settings.add(range);
		settings.add(distance);
	}
	
	@Override
	public Feature[] getSeeAlso()
	{
		return new Feature[]{wurst.mods.killauraMod, wurst.special.targetSpf,
			wurst.special.yesCheatSpf};
	}
	
	@Override
	public void onEnable()
	{
		wurst.events.add(UpdateListener.class, this);
	}
	
	@Override
	public void onDisable()
	{
		// remove listener
		wurst.events.remove(UpdateListener.class, this);
		
		// reset keys
		resetKeys();
	}
	
	@Override
	public void onUpdate()
	{
		// update timer
		updateMS();
		
		// reset keys
		resetKeys();
		
		// set entity
		Entity entity = EntityUtils.getClosestEntity(followSettings);
		if(entity == null)
			return;
		
		// jump if necessary
		if(mc.player.isCollidedHorizontally)
			mc.gameSettings.keyBindJump.pressed = true;
		
		// swim up if necessary
		if(mc.player.isInWater() && mc.player.posY < entity.posY)
			mc.gameSettings.keyBindJump.pressed = true;
		
		// control height if flying
		if(!mc.player.onGround
			&& (mc.player.capabilities.isFlying
				|| wurst.mods.flightMod.isActive())
			&& Math.sqrt(Math.pow(mc.player.posX - entity.posX, 2)
				+ Math.pow(mc.player.posZ - entity.posZ, 2)) <= range
					.getValue())
			if(mc.player.posY > entity.posY + 1D)
				mc.gameSettings.keyBindSneak.pressed = true;
			else if(mc.player.posY < entity.posY - 1D)
				mc.gameSettings.keyBindJump.pressed = true;
			
		// follow entity
		mc.gameSettings.keyBindForward.pressed =
			mc.player.getDistanceToEntity(entity) > distance.getValueF();
		if(!RotationUtils.faceEntityClient(entity))
			return;
		
		// check timer / cooldown
		if(useCooldown.isChecked() ? mc.player.getCooledAttackStrength(0F) < 1F
			: !hasTimePassedS(speed.getValueF()))
			return;
		
		// check range
		if(!EntityUtils.isCorrectEntity(entity, attackSettings))
			return;
		
		// AutoSword
		wurst.mods.autoSwordMod.setSlot();
		
		// Criticals
		wurst.mods.criticalsMod.doCritical();
		
		// attack entity
		mc.playerController.attackEntity(mc.player, entity);
		mc.player.swingArm(EnumHand.MAIN_HAND);
		
		// reset timer
		updateLastMS();
	}
	
	@Override
	public void onYesCheatUpdate(BypassLevel bypassLevel)
	{
		switch(bypassLevel)
		{
			default:
			case OFF:
			case MINEPLEX:
				speed.unlock();
				range.unlock();
				distance.unlock();
				break;
			case ANTICHEAT:
			case OLDER_NCP:
			case LATEST_NCP:
			case GHOST_MODE:
				speed.lockToMax(12);
				range.lockToMax(4.25);
				distance.lockToMax(4.25);
				break;
		}
	}
	
	private void resetKeys()
	{
		// get keys
		GameSettings gs = mc.gameSettings;
		KeyBinding[] keys = new KeyBinding[]{gs.keyBindForward, gs.keyBindJump,
			gs.keyBindSneak};
		
		// reset keys
		for(KeyBinding key : keys)
			key.pressed = Keyboard.isKeyDown(key.getKeyCode());
	}
}
