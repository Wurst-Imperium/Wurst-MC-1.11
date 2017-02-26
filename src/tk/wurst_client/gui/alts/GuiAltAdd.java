/*
 * Copyright © 2014 - 2017 | Wurst-Imperium | All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.gui.alts;

import net.minecraft.client.gui.GuiScreen;
import tk.wurst_client.alts.Alt;
import tk.wurst_client.alts.LoginManager;
import tk.wurst_client.files.ConfigFiles;

public final class GuiAltAdd extends AltEditorScreen
{
	public GuiAltAdd(GuiScreen prevScreen)
	{
		super(prevScreen);
	}
	
	@Override
	protected String getTitle()
	{
		return "New Alt";
	}
	
	@Override
	protected String getDoneButtonText()
	{
		return "Add";
	}
	
	@Override
	protected void pressDoneButton()
	{
		if(getPassword().isEmpty())
		{
			// add cracked alt
			message = "";
			GuiAltList.alts.add(new Alt(getEmail(), null, null));
			
		}else
		{
			// add premium alt
			message = LoginManager.login(getEmail(), getPassword());
			
			if(message.isEmpty())
				GuiAltList.alts.add(new Alt(getEmail(), getPassword(),
					mc.session.getUsername()));
		}
		
		if(message.isEmpty())
		{
			GuiAltList.sortAlts();
			ConfigFiles.ALTS.save();
			mc.displayGuiScreen(prevScreen);
			
		}else
			doErrorEffect();
	}
}
