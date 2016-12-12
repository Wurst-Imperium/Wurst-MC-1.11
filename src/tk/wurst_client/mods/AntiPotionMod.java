/*
 * Copyright © 2014 - 2016 | Wurst-Imperium | All rights reserved.
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.mods;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.init.MobEffects;
import net.minecraft.network.play.client.CPacketPlayer;
import tk.wurst_client.events.listeners.UpdateListener;

@Mod.Info(
	description = "Blocks bad potion effects.",
	name = "AntiPotion",
	tags = "NoPotion, Zoot, anti potions, no potions",
	help = "Mods/AntiPotion")
@Mod.Bypasses(ghostMode = false, latestNCP = false, olderNCP = false)
public class AntiPotionMod extends Mod implements UpdateListener
{
	@Override
	public void onEnable()
	{
		wurst.events.add(UpdateListener.class, this);
	}
	
	@Override
	public void onUpdate()
	{
		EntityPlayerSP player = mc.player;
		if(!player.capabilities.isCreativeMode && player.onGround
			&& !player.getActivePotionEffects().isEmpty())
			if(player.isPotionActive(MobEffects.HUNGER)
				|| player.isPotionActive(MobEffects.SLOWNESS)
				|| player.isPotionActive(MobEffects.MINING_FATIGUE)
				|| player.isPotionActive(MobEffects.INSTANT_DAMAGE)
				|| player.isPotionActive(MobEffects.NAUSEA)
				|| player.isPotionActive(MobEffects.BLINDNESS)
				|| player.isPotionActive(MobEffects.WEAKNESS)
				|| player.isPotionActive(MobEffects.WITHER)
				|| player.isPotionActive(MobEffects.POISON))
				for(int i = 0; i < 1000; i++)
					player.connection.sendPacket(new CPacketPlayer());
	}
	
	@Override
	public void onDisable()
	{
		wurst.events.remove(UpdateListener.class, this);
	}
}
