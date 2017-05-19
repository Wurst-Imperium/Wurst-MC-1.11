/*
 * Copyright © 2014 - 2017 | Wurst-Imperium | All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.wurstclient.features.mods;

import net.wurstclient.features.Mod;
import net.wurstclient.features.SearchTags;
import net.wurstclient.settings.CheckboxSetting;

@SearchTags({"ChestStealer", "auto steal", "chest stealer"})
@Mod.Bypasses
public final class AutoStealMod extends Mod
{
	private final CheckboxSetting buttons =
		new CheckboxSetting("Steal/Store buttons", true);
	
	public AutoStealMod()
	{
		super("AutoSteal",
			"Automatically steals everything from all chests you open.");
	}
	
	@Override
	public void initSettings()
	{
		settings.add(buttons);
	}
	
	public boolean areButtonsVisible()
	{
		return buttons.isChecked();
	}
}
