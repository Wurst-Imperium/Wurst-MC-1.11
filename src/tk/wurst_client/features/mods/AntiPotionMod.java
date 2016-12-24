/*
 * Copyright © 2014 - 2016 | Wurst-Imperium | All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.features.mods;

import net.minecraft.init.MobEffects;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.potion.Potion;
import tk.wurst_client.events.listeners.UpdateListener;

@Mod.Info(description = "Blocks bad potion effects.",
	name = "AntiPotion",
	tags = "NoPotion, Zoot, anti potions, no potions",
	help = "Mods/AntiPotion")
@Mod.Bypasses(ghostMode = false, latestNCP = false, olderNCP = false)
public class AntiPotionMod extends Mod implements UpdateListener
{
	private final Potion[] blockedEffects = new Potion[]{MobEffects.HUNGER,
		MobEffects.SLOWNESS, MobEffects.MINING_FATIGUE,
		MobEffects.INSTANT_DAMAGE, MobEffects.NAUSEA, MobEffects.BLINDNESS,
		MobEffects.WEAKNESS, MobEffects.WITHER, MobEffects.POISON};
	
	@Override
	public void onEnable()
	{
		wurst.events.add(UpdateListener.class, this);
	}
	
	@Override
	public void onDisable()
	{
		wurst.events.remove(UpdateListener.class, this);
	}
	
	@Override
	public void onUpdate()
	{
		if(!mc.player.capabilities.isCreativeMode && mc.player.onGround
			&& hasBadEffect())
			for(int i = 0; i < 1000; i++)
				mc.player.connection.sendPacket(new CPacketPlayer());
	}
	
	private boolean hasBadEffect()
	{
		if(mc.player.getActivePotionEffects().isEmpty())
			return false;
		
		for(Potion effect : blockedEffects)
			if(mc.player.isPotionActive(effect))
				return true;
			
		return false;
	}
}
