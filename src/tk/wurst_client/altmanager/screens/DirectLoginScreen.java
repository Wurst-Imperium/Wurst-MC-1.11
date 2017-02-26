/*
 * Copyright © 2014 - 2017 | Wurst-Imperium | All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.altmanager.screens;

import net.minecraft.client.gui.GuiScreen;
import tk.wurst_client.altmanager.LoginManager;
import tk.wurst_client.gui.main.GuiWurstMainMenu;

public final class DirectLoginScreen extends AltEditorScreen
{
	public DirectLoginScreen(GuiScreen prevScreen)
	{
		super(prevScreen);
	}
	
	@Override
	protected String getTitle()
	{
		return "Direct Login";
	}
	
	@Override
	protected String getDoneButtonText()
	{
		return "Login";
	}
	
	@Override
	protected void pressDoneButton()
	{
		if(getPassword().isEmpty())
		{
			message = "";
			LoginManager.changeCrackedName(getEmail());
			
		}else
			message = LoginManager.login(getEmail(), getPassword());
		
		if(message.isEmpty())
			mc.displayGuiScreen(new GuiWurstMainMenu());
		else
			doErrorEffect();
	}
}
