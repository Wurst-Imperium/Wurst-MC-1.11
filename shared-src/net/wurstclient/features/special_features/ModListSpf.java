/*
 * Copyright © 2014 - 2017 | Wurst-Imperium | All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.wurstclient.features.special_features;

import net.wurstclient.features.Spf;
import net.wurstclient.settings.ModeSetting;

public final class ModListSpf extends Spf
{
	private final ModeSetting mode =
		new ModeSetting("Mode", new String[]{"Auto", "Count", "Hidden"}, 0);
	
	public ModListSpf()
	{
		super("ModList",
			"Shows a list of active mods on the screen.\n"
				+ "§lAuto§r mode renders the whole list if it fits onto the screen.\n"
				+ "§lCount§r mode only renders the number of active mods.\n"
				+ "§lHidden§r mode renders nothing.");
		
		settings.add(mode);
	}
	
	public int getMode()
	{
		return mode.getSelected();
	}
}
