/*
 * Copyright © 2014 - 2017 | Wurst-Imperium | All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.wurstclient.gui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.wurstclient.WurstClient;
import net.wurstclient.events.listeners.UpdateListener;
import net.wurstclient.features.Mod;
import net.wurstclient.features.mods.NavigatorMod;
import net.wurstclient.font.Fonts;

public final class ModList implements UpdateListener
{
	private final ArrayList<Entry> activeMods = new ArrayList<>();
	private int posY;
	
	public ModList()
	{
		for(Mod mod : WurstClient.INSTANCE.mods.getAllMods())
		{
			if(mod instanceof NavigatorMod)
				continue;
			
			if(mod.isActive())
				activeMods.add(new Entry(mod, 0));
		}
		
		WurstClient.INSTANCE.events.add(UpdateListener.class, this);
	}
	
	public void render(float partialTicks)
	{
		if(WurstClient.INSTANCE.special.modListSpf.getMode() == 2)
			return;
		
		posY = 19;
		
		// YesCheat+ mode indicator
		if(WurstClient.INSTANCE.special.yesCheatSpf.modeIndicator.isChecked())
			drawString("YesCheat+: " + WurstClient.INSTANCE.special.yesCheatSpf
				.getProfile().getName());
		
		ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
		int modListHeight = posY + activeMods.size() * 9;
		
		if(WurstClient.INSTANCE.special.modListSpf.getMode() == 0
			&& modListHeight <= sr.getScaledHeight())
			
			// draw mod list
			if(WurstClient.INSTANCE.special.modListSpf.isAnimations())
				for(Entry e : activeMods)
					drawWithOffset(e, partialTicks);
			else
				for(Entry e : activeMods)
					drawString(e.mod.getRenderName());
				
		// draw counter
		else if(activeMods.size() == 1)
			drawString("1 mod active");
		else
			drawString(activeMods.size() + " mods active");
	}
	
	public void updateState(Mod mod)
	{
		if(mod.isActive())
		{
			for(Entry e : activeMods)
				if(e.mod == mod)
					return;
			activeMods.add(new Entry(mod, 4));
			activeMods.sort(Comparator.comparing(e -> e.mod.getName()));
			
		}else if(!WurstClient.INSTANCE.special.modListSpf.isAnimations())
			activeMods.removeIf(e -> e.mod == mod);
	}
	
	@Override
	public void onUpdate()
	{
		if(!WurstClient.INSTANCE.special.modListSpf.isAnimations())
			return;
		
		for(Iterator<Entry> itr = activeMods.iterator(); itr.hasNext();)
		{
			Entry e = itr.next();
			
			if(e.mod.isActive())
			{
				e.prevOffset = e.offset;
				if(e.offset > 0)
					e.offset--;
			}else if(!e.mod.isActive() && e.offset < 4)
			{
				e.prevOffset = e.offset;
				e.offset++;
			}else if(!e.mod.isActive() && e.offset == 4)
				itr.remove();
		}
	}
	
	private void drawString(String s)
	{
		Fonts.segoe18.drawString(s, 3, posY + 1, 0xff000000);
		Fonts.segoe18.drawString(s, 2, posY, 0xffffffff);
		posY += 9;
	}
	
	private void drawWithOffset(Entry e, float partialTicks)
	{
		String s = e.mod.getRenderName();
		float offset =
			e.offset * partialTicks + e.prevOffset * (1 - partialTicks);
		float posX = 2 - 5 * offset;
		int alpha = (int)(255 * (1 - offset / 4)) << 24;
		
		Fonts.segoe18.drawString(s, posX + 1, posY + 1, 0x04000000 | alpha);
		Fonts.segoe18.drawString(s, posX, posY, 0x04ffffff | alpha);
		posY += 9;
	}
	
	private static final class Entry
	{
		private final Mod mod;
		private int offset;
		private int prevOffset;
		
		public Entry(Mod mod, int offset)
		{
			this.mod = mod;
			this.offset = offset;
			prevOffset = offset;
		}
	}
}
