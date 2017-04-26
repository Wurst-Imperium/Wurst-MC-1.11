/*
 * Copyright © 2014 - 2017 | Wurst-Imperium | All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.wurstclient.features.mods;

import net.wurstclient.features.HelpPage;
import net.wurstclient.features.Mod;
import net.wurstclient.features.SearchTags;

@SearchTags({"health tags"})
@HelpPage("Mods/HealthTags")
@Mod.Bypasses
public final class HealthTagsMod extends Mod
{
	public HealthTagsMod()
	{
		super("HealthTags", "Adds the health of players to their nametags.");
	}
}
