/*
 * Copyright © 2014 - 2017 | Wurst-Imperium | All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.wurstclient.gui.options.keybinds;

import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlot;
import net.wurstclient.WurstClient;

public final class GuiKeybindList extends GuiSlot
{
	private int selectedSlot;
	private Minecraft mc;
	
	public GuiKeybindList(Minecraft mc, GuiScreen prevScreen)
	{
		super(mc, prevScreen.width, prevScreen.height, 36,
			prevScreen.height - 56, 30);
		this.mc = mc;
	}
	
	public int getSelectedSlot()
	{
		return selectedSlot;
	}
	
	@Override
	protected boolean isSelected(int index)
	{
		return selectedSlot == index;
	}
	
	@Override
	protected int getSize()
	{
		return WurstClient.INSTANCE.keybinds.size();
	}
	
	@Override
	protected void elementClicked(int index, boolean isDoubleClick, int mouseX,
		int mouseY)
	{
		selectedSlot = index;
	}
	
	@Override
	protected void drawBackground()
	{
		
	}
	
	@Override
	protected void drawSlot(int id, int x, int y, int slotHeight, int mouseX,
		int mouseY)
	{
		Entry entry = WurstClient.INSTANCE.keybinds.entrySet()
			.toArray(new Map.Entry[WurstClient.INSTANCE.keybinds.size()])[id];
		
		mc.fontRendererObj.drawString("Key: " + entry.getKey(), x + 3, y + 3,
			0xa0a0a0);
		mc.fontRendererObj.drawString("Command: " + entry.getValue(), x + 3,
			y + 15, 0xa0a0a0);
	}
}
