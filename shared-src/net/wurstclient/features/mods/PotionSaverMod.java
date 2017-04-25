/*
 * Copyright © 2014 - 2017 | Wurst-Imperium | All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.wurstclient.features.mods;

import net.wurstclient.features.Mod;

@Mod.Info(tags = "potion saver", help = "Mods/PotionSaver")
@Mod.Bypasses
public final class PotionSaverMod extends Mod
{
	public PotionSaverMod()
	{
		super("PotionSaver",
			"Freezes all potion effects while you are standing still.");
	}
}
