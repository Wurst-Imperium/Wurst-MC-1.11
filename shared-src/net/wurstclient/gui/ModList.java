/*
 * Copyright © 2014 - 2017 | Wurst-Imperium | All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.wurstclient.gui;

import java.util.ArrayList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.wurstclient.WurstClient;
import net.wurstclient.features.Mod;
import net.wurstclient.features.mods.NavigatorMod;
import net.wurstclient.font.Fonts;

public final class ModList
{
	private int posY;
	
	public void render()
	{
		if(WurstClient.INSTANCE.special.modListSpf.getMode() == 2)
			return;
		
		posY = 19;
		
		// YesCheat+ mode indicator
		if(WurstClient.INSTANCE.special.yesCheatSpf.modeIndicator.isChecked())
			drawString("YesCheat+: " + WurstClient.INSTANCE.special.yesCheatSpf
				.getProfile().getName());
		
		// get render names of active mods
		ArrayList<String> activeModNames = new ArrayList<>();
		for(Mod mod : WurstClient.INSTANCE.mods.getAllMods())
		{
			if(mod instanceof NavigatorMod)
				continue;
			
			if(mod.isActive())
				activeModNames.add(mod.getRenderName());
		}
		
		ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
		int modListHeight = posY + activeModNames.size() * 9;
		
		if(WurstClient.INSTANCE.special.modListSpf.getMode() == 0
			&& modListHeight <= sr.getScaledHeight())
			
			// draw mod list
			for(String name : activeModNames)
				drawString(name);
			
		// draw counter
		else if(activeModNames.size() == 1)
			drawString("1 mod active");
		else
			drawString(activeModNames.size() + " mods active");
	}
	
	private void drawString(String string)
	{
		Fonts.segoe18.drawString(string, 3, posY + 1, 0xff000000);
		Fonts.segoe18.drawString(string, 2, posY, 0xffffffff);
		posY += 9;
	}
}
