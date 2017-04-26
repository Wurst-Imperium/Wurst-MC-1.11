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
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.SliderSetting.ValueDisplay;

@SearchTags({"high jump"})
@HelpPage("Mods/HighJump")
@Mod.Bypasses(ghostMode = false,
	latestNCP = false,
	olderNCP = false,
	antiCheat = false,
	mineplex = false)
public final class HighJumpMod extends Mod
{
	public final SliderSetting height =
		new SliderSetting("Height", 6, 1, 100, 1, ValueDisplay.INTEGER);
	
	public HighJumpMod()
	{
		super("HighJump", "Makes you jump much higher.");
	}
	
	@Override
	public void initSettings()
	{
		settings.add(height);
	}
}
