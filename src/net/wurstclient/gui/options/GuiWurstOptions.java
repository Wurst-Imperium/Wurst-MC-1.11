/*
 * Copyright © 2014 - 2017 | Wurst-Imperium | All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.wurstclient.gui.options;

import java.util.Arrays;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.wurstclient.WurstClient;
import net.wurstclient.files.ConfigFiles;
import net.wurstclient.gui.options.keybinds.GuiKeybindManager;
import net.wurstclient.gui.options.xray.GuiXRayBlocksManager;
import net.wurstclient.gui.options.zoom.GuiZoomManager;
import net.wurstclient.options.OptionsManager.GoogleAnalytics;
import net.wurstclient.utils.MiscUtils;

public class GuiWurstOptions extends GuiScreen
{
	private GuiScreen prevScreen;
	private String[] modListModes = {"Auto", "Count", "Hidden"};
	private String[] toolTips = {"",
		"Add/remove friends by clicking them with\n"
			+ "the middle mouse button.",
		"How the mod list under the Wurst logo\n" + "should be displayed.\n"
			+ "§lModes:§r\n" + "§nAuto§r: Renders the whole list if it fits\n"
			+ "onto the screen.\n"
			+ "§nCount§r: Only renders the number of active\n" + "mods.\n"
			+ "§nHidden§r: Renders nothing.",
		"Automatically maximizes the Minecraft window.\n"
			+ "Windows & Linux only!",
		"Whether or not the Wurst News should be\n" + "shown in the main menu.",
		"Sends anonymous usage statistics that\n"
			+ "help us improve the Wurst Client.",
		"Keybinds allow you to toggle any mod\n"
			+ "or command by simply pressing a\n" + "button.",
		"Manager for the blocks that X-Ray will\n" + "show.",
		"The Zoom Manager allows you to\n" + "change the zoom key, how far it\n"
			+ "will zoom in and more.",
		"", "", "", "", "", "", ""};
	private boolean autoMaximize =
		WurstClient.INSTANCE.files.loadAutoMaximize();
	
	public GuiWurstOptions(GuiScreen par1GuiScreen)
	{
		prevScreen = par1GuiScreen;
	}
	
	@Override
	public void initGui()
	{
		buttonList.clear();
		
		buttonList.add(new GuiButton(0, width / 2 - 100, height / 4 + 144 - 16,
			200, 20, "Back"));
		buttonList.add(
			new GuiButton(1, width / 2 - 154, height / 4 + 24 - 16, 100, 20,
				"Click Friends: "
					+ (WurstClient.INSTANCE.options.middleClickFriends ? "ON"
						: "OFF")));
		buttonList.add(new GuiButton(2, width / 2 - 154, height / 4 + 48 - 16,
			100, 20, "Mod List: "
				+ modListModes[WurstClient.INSTANCE.options.modListMode]));
		buttonList.add(new GuiButton(3, width / 2 - 154, height / 4 + 72 - 16,
			100, 20, "AutoMaximize: " + (autoMaximize ? "ON" : "OFF")));
		buttonList.add(new GuiButton(4, width / 2 - 154, height / 4 + 96 - 16,
			100, 20, "Wurst News: "
				+ (WurstClient.INSTANCE.options.wurstNews ? "ON" : "OFF")));
		buttonList.add(
			new GuiButton(5, width / 2 - 154, height / 4 + 120 - 16, 100, 20,
				"Analytics: "
					+ (WurstClient.INSTANCE.options.google_analytics.enabled
						? "ON" : "OFF")));
		buttonList.add(new GuiButton(6, width / 2 - 50, height / 4 + 24 - 16,
			100, 20, "Keybinds"));
		buttonList.add(new GuiButton(7, width / 2 - 50, height / 4 + 48 - 16,
			100, 20, "X-Ray Blocks"));
		buttonList.add(new GuiButton(8, width / 2 - 50, height / 4 + 72 - 16,
			100, 20, "Zoom"));
		// this.buttonList.add(new GuiButton(9, this.width / 2 - 50, this.height
		// / 4 + 96 - 16, 100, 20, "???"));
		// this.buttonList.add(new GuiButton(10, this.width / 2 - 50,
		// this.height / 4 + 120 - 16, 100, 20, "???"));
		buttonList.add(new GuiButton(11, width / 2 + 54, height / 4 + 24 - 16,
			100, 20, "Official Website"));
		buttonList.add(new GuiButton(12, width / 2 + 54, height / 4 + 48 - 16,
			100, 20, "YouTube Channel"));
		buttonList.add(new GuiButton(13, width / 2 + 54, height / 4 + 72 - 16,
			100, 20, "Twitter Page"));
		buttonList.add(new GuiButton(14, width / 2 + 54, height / 4 + 96 - 16,
			100, 20, "Discord Server"));
		// buttonList.add(new GuiButton(15, width / 2 + 54, height / 4 + 120 -
		// 16, 100, 20, "???"));
		buttonList.get(3).enabled = !Minecraft.IS_RUNNING_ON_MAC;
	}
	
	@Override
	protected void actionPerformed(GuiButton button)
	{
		if(!button.enabled)
			return;
		
		if(button.id == 0)
			mc.displayGuiScreen(prevScreen);
		else if(button.id == 1)
		{// Click Friends
			WurstClient.INSTANCE.options.middleClickFriends =
				!WurstClient.INSTANCE.options.middleClickFriends;
			button.displayString = "Click Friends: "
				+ (WurstClient.INSTANCE.options.middleClickFriends ? "ON"
					: "OFF");
			ConfigFiles.OPTIONS.save();
			WurstClient.INSTANCE.analytics.trackEvent("options",
				"click friends",
				WurstClient.INSTANCE.options.middleClickFriends ? "ON" : "OFF");
		}else if(button.id == 2)
		{// Mod List
			WurstClient.INSTANCE.options.modListMode++;
			if(WurstClient.INSTANCE.options.modListMode > 2)
				WurstClient.INSTANCE.options.modListMode = 0;
			button.displayString = "Mod List: "
				+ modListModes[WurstClient.INSTANCE.options.modListMode];
			ConfigFiles.OPTIONS.save();
			WurstClient.INSTANCE.analytics.trackEvent("options", "mod list",
				modListModes[WurstClient.INSTANCE.options.modListMode]);
		}else if(button.id == 3)
		{// AutoMaximize
			autoMaximize = !autoMaximize;
			button.displayString =
				"AutoMaximize: " + (autoMaximize ? "ON" : "OFF");
			WurstClient.INSTANCE.files.saveAutoMaximize(autoMaximize);
			WurstClient.INSTANCE.analytics.trackEvent("options", "automaximize",
				autoMaximize ? "ON" : "OFF");
		}else if(button.id == 4)
		{// Wurst News
			WurstClient.INSTANCE.options.wurstNews =
				!WurstClient.INSTANCE.options.wurstNews;
			button.displayString = "Wurst News: "
				+ (WurstClient.INSTANCE.options.wurstNews ? "ON" : "OFF");
			ConfigFiles.OPTIONS.save();
			WurstClient.INSTANCE.analytics.trackEvent("options", "wurst news",
				WurstClient.INSTANCE.options.wurstNews ? "ON" : "OFF");
		}else if(button.id == 5)
		{// Analytics
			GoogleAnalytics analytics =
				WurstClient.INSTANCE.options.google_analytics;
			if(analytics.enabled)
				WurstClient.INSTANCE.analytics.trackEvent("options",
					"analytics", "disable");
			analytics.enabled = !analytics.enabled;
			if(analytics.enabled)
				WurstClient.INSTANCE.analytics.trackEvent("options",
					"analytics", "enable");
			button.displayString =
				"Analytics: " + (analytics.enabled ? "ON" : "OFF");
			ConfigFiles.OPTIONS.save();
		}else if(button.id == 6)
			// Keybind Manager
			mc.displayGuiScreen(new GuiKeybindManager(this));
		else if(button.id == 7)
			// X-Ray Block Manager
			mc.displayGuiScreen(new GuiXRayBlocksManager(this));
		else if(button.id == 8)
			// Zoom Manager
			mc.displayGuiScreen(new GuiZoomManager(this));
		else if(button.id == 9)
		{
			
		}else if(button.id == 10)
		{
			
		}else if(button.id == 11)
			MiscUtils.openLink("https://www.wurstclient.net/");
		else if(button.id == 12)
			MiscUtils.openLink("https://www.wurstclient.net/youtube/");
		else if(button.id == 13)
			MiscUtils.openLink("https://www.wurstclient.net/twitter/");
		else if(button.id == 14)
			MiscUtils.openLink("https://www.wurstclient.net/discord/");
		else if(button.id == 15)
		{
			
		}
	}
	
	@Override
	public void drawScreen(int par1, int par2, float par3)
	{
		drawDefaultBackground();
		drawCenteredString(fontRendererObj, "Wurst Options", width / 2, 40,
			0xffffff);
		drawCenteredString(fontRendererObj, "Settings", width / 2 - 104,
			height / 4 + 24 - 28, 0xcccccc);
		drawCenteredString(fontRendererObj, "Managers", width / 2,
			height / 4 + 24 - 28, 0xcccccc);
		drawCenteredString(fontRendererObj, "Links", width / 2 + 104,
			height / 4 + 24 - 28, 0xcccccc);
		super.drawScreen(par1, par2, par3);
		for(int i = 0; i < buttonList.size(); i++)
		{
			GuiButton button = buttonList.get(i);
			if(button.isMouseOver() && !toolTips[button.id].isEmpty())
			{
				drawHoveringText(Arrays.asList(toolTips[button.id].split("\n")),
					par1, par2);
				break;
			}
		}
	}
}
