/*
 * Copyright © 2014 - 2017 | Wurst-Imperium | All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.features.mods;

import net.minecraft.entity.Entity;
import net.minecraft.util.EnumHand;
import tk.wurst_client.events.listeners.UpdateListener;
import tk.wurst_client.features.Feature;
import tk.wurst_client.features.special_features.TargetSpf;
import tk.wurst_client.features.special_features.YesCheatSpf.BypassLevel;
import tk.wurst_client.settings.CheckboxSetting;
import tk.wurst_client.settings.ColorsSetting;
import tk.wurst_client.settings.SliderSetting;
import tk.wurst_client.settings.SliderSetting.ValueDisplay;
import tk.wurst_client.utils.EntityUtils;
import tk.wurst_client.utils.EntityUtils.TargetSettings;

@Mod.Info(
	description = "Automatically attacks entities around you.\n"
		+ "Can be configured in various ways to attack only some entities and ignore others.",
	name = "Killaura",
	tags = "kill aura",
	help = "Mods/Killaura")
@Mod.Bypasses
public class KillauraMod extends Mod implements UpdateListener
{
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
	public SliderSetting fov =
		new SliderSetting("FOV", 360, 30, 360, 10, ValueDisplay.DEGREES);
	public CheckboxSetting hitThroughWalls =
		new CheckboxSetting("Hit through walls", false);
	
	public CheckboxSetting useTarget =
		new CheckboxSetting("Use Target settings", true)
		{
			@Override
			public void update()
			{
				if(isChecked())
				{
					TargetSpf target = wurst.special.targetSpf;
					players.lock(target.players.isChecked());
					animals.lock(target.animals.isChecked());
					monsters.lock(target.monsters.isChecked());
					golems.lock(target.golems.isChecked());
					sleepingPlayers.lock(target.sleepingPlayers.isChecked());
					invisiblePlayers.lock(target.invisiblePlayers.isChecked());
					invisibleMobs.lock(target.invisibleMobs.isChecked());
					teams.lock(target.teams.isChecked());
					teamColors.lock(target.teamColors.getSelected());
				}else
				{
					players.unlock();
					animals.unlock();
					monsters.unlock();
					golems.unlock();
					sleepingPlayers.unlock();
					invisiblePlayers.unlock();
					invisibleMobs.unlock();
					teams.unlock();
					teamColors.unlock();
				}
			};
		};
	public final CheckboxSetting players =
		new CheckboxSetting("Attack players", true);
	public final CheckboxSetting animals =
		new CheckboxSetting("Attack animals", true);
	public final CheckboxSetting monsters =
		new CheckboxSetting("Attack monsters", true);
	public final CheckboxSetting golems =
		new CheckboxSetting("Attack golems", true);
	
	public final CheckboxSetting sleepingPlayers =
		new CheckboxSetting("Attack sleeping players", false);
	public final CheckboxSetting invisiblePlayers =
		new CheckboxSetting("Attack invisible players", false);
	public final CheckboxSetting invisibleMobs =
		new CheckboxSetting("Attack invisible mobs", false);
	
	public final CheckboxSetting teams = new CheckboxSetting("Teams", false);
	public final ColorsSetting teamColors = new ColorsSetting("Team Colors",
		new boolean[]{true, true, true, true, true, true, true, true, true,
			true, true, true, true, true, true, true});
	
	private TargetSettings targetSettings = new TargetSettings()
	{
		@Override
		public boolean targetBehindWalls()
		{
			return hitThroughWalls.isChecked();
		}
		
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
		
		@Override
		public boolean targetPlayers()
		{
			return players.isChecked();
		}
		
		@Override
		public boolean targetAnimals()
		{
			return animals.isChecked();
		}
		
		@Override
		public boolean targetMonsters()
		{
			return monsters.isChecked();
		}
		
		@Override
		public boolean targetGolems()
		{
			return golems.isChecked();
		}
		
		@Override
		public boolean targetSleepingPlayers()
		{
			return sleepingPlayers.isChecked();
		}
		
		@Override
		public boolean targetInvisiblePlayers()
		{
			return invisiblePlayers.isChecked();
		}
		
		@Override
		public boolean targetInvisibleMobs()
		{
			return invisibleMobs.isChecked();
		}
		
		@Override
		public boolean targetTeams()
		{
			return teams.isChecked();
		}
		
		@Override
		public boolean[] getTeamColors()
		{
			return teamColors.getSelected();
		}
	};
	
	@Override
	public void initSettings()
	{
		settings.add(useCooldown);
		settings.add(speed);
		settings.add(range);
		settings.add(fov);
		settings.add(hitThroughWalls);
		settings.add(useTarget);
		settings.add(players);
		settings.add(animals);
		settings.add(monsters);
		settings.add(golems);
		settings.add(sleepingPlayers);
		settings.add(invisiblePlayers);
		settings.add(invisibleMobs);
		settings.add(teams);
		settings.add(teamColors);
	}
	
	@Override
	public Feature[] getSeeAlso()
	{
		return new Feature[]{wurst.special.targetSpf,
			wurst.mods.killauraLegitMod, wurst.mods.multiAuraMod,
			wurst.mods.clickAuraMod, wurst.mods.tpAuraMod,
			wurst.mods.triggerBotMod, wurst.mods.criticalsMod};
	}
	
	@Override
	public void onEnable()
	{
		// TODO: Clean up this mess!
		if(wurst.mods.killauraLegitMod.isEnabled())
			wurst.mods.killauraLegitMod.setEnabled(false);
		if(wurst.mods.multiAuraMod.isEnabled())
			wurst.mods.multiAuraMod.setEnabled(false);
		if(wurst.mods.clickAuraMod.isEnabled())
			wurst.mods.clickAuraMod.setEnabled(false);
		if(wurst.mods.tpAuraMod.isEnabled())
			wurst.mods.tpAuraMod.setEnabled(false);
		if(wurst.mods.triggerBotMod.isEnabled())
			wurst.mods.triggerBotMod.setEnabled(false);
		wurst.events.add(UpdateListener.class, this);
	}
	
	@Override
	public void onDisable()
	{
		wurst.events.remove(UpdateListener.class, this);
		EntityUtils.lookChanged = false;
	}
	
	@Override
	public void onUpdate()
	{
		// update timer
		updateMS();
		
		// check timer / cooldown
		if(useCooldown.isChecked() ? mc.player.getCooledAttackStrength(0F) < 1F
			: !hasTimePassedS(speed.getValueF()))
			return;
		
		// set entity
		Entity entity = EntityUtils.getClosestEntity(targetSettings);
		
		// head rotation
		EntityUtils.lookChanged = entity != null;
		if(!EntityUtils.lookChanged)
			return;
		
		// AutoSword
		wurst.mods.autoSwordMod.setSlot();
		
		// Criticals
		wurst.mods.criticalsMod.doCritical();
		
		// BlockHit
		wurst.mods.blockHitMod.doBlock();
		
		// face entity
		if(!EntityUtils.faceEntityPacket(entity))
			return;
		
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
				hitThroughWalls.unlock();
				break;
			case ANTICHEAT:
			case OLDER_NCP:
			case LATEST_NCP:
				speed.lockToMax(12);
				range.lockToMax(4.25);
				hitThroughWalls.unlock();
				break;
			case GHOST_MODE:
				speed.lockToMax(12);
				range.lockToMax(4.25);
				hitThroughWalls.lock(false);
				break;
		}
	}
}
