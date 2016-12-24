/*
 * Copyright © 2014 - 2016 | Wurst-Imperium | All rights reserved.
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.features.mods;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraft.client.gui.GuiIngameMenu;
import tk.wurst_client.navigator.gui.NavigatorScreen;

@Mod.Info(
	description = "Allows you to walk while viewing a menu (e.g. the inventory\n"
		+ "menu).",
	name = "MenuWalk",
	tags = "InventoryWalk, menu walk, inventory walk",
	help = "Mods/MenuWalk")
@Mod.Bypasses
public class MenuWalkMod extends Mod
{
	public boolean shouldAllowWalking()
	{
		// check if mod is active
		if(!isActive())
			return false;
		
		// check if there is a player to move
		if(mc.player == null)
			return false;
		
		// check if player is viewing chat
		if(mc.currentScreen instanceof GuiChat
			|| mc.currentScreen instanceof GuiIngameMenu
			|| mc.currentScreen instanceof GuiGameOver
			|| mc.currentScreen instanceof NavigatorScreen)
			return false;
		
		// check if inventory key is pressed and if escape key is pressed
		if(Keyboard.isKeyDown(mc.gameSettings.keyBindInventory.getKeyCode()) || Keyboard.isKeyDown(Keyboard.KEY_ESCAPE))

			return false;
		
		return true;
	}
}
