/*
 * Copyright © 2014 - 2017 | Wurst-Imperium | All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.features.mods;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;

import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.util.ReportedException;
import tk.wurst_client.features.Feature;
import tk.wurst_client.features.special_features.YesCheatSpf.BypassLevel;
import tk.wurst_client.files.ConfigFiles;
import tk.wurst_client.navigator.PossibleKeybind;
import tk.wurst_client.settings.Setting;

public abstract class Mod extends Feature
{
	private final String name = getClass().getAnnotation(Info.class).name();
	private final String description =
		getClass().getAnnotation(Info.class).description();
	private final String tags = getClass().getAnnotation(Info.class).tags();
	private final String help = getClass().getAnnotation(Info.class).help();
	private final Bypasses bypasses = getClass().getAnnotation(Bypasses.class);
	private boolean enabled;
	private boolean blocked;
	private boolean active;
	protected ArrayList<Setting> settings = new ArrayList<>();
	private long currentMS = 0L;
	protected long lastMS = -1L;
	
	@Retention(RetentionPolicy.RUNTIME)
	public @interface Info
	{
		String name();
		
		String description();
		
		String tags() default "";
		
		String help() default "";
	}
	
	@Retention(RetentionPolicy.RUNTIME)
	public @interface Bypasses
	{
		boolean mineplex() default true;
		
		boolean antiCheat() default true;
		
		boolean olderNCP() default true;
		
		boolean latestNCP() default true;
		
		boolean ghostMode() default true;
	}
	
	@Override
	public final String getName()
	{
		return name;
	}
	
	@Override
	public final String getType()
	{
		return "Mod";
	}
	
	public String getRenderName()
	{
		return name;
	}
	
	@Override
	public final String getDescription()
	{
		return description;
	}
	
	@Override
	public final String getTags()
	{
		return tags;
	}
	
	@Override
	public final ArrayList<Setting> getSettings()
	{
		return settings;
	}
	
	@Override
	public final ArrayList<PossibleKeybind> getPossibleKeybinds()
	{
		// mod keybinds
		String dotT = ".t " + name.toLowerCase();
		ArrayList<PossibleKeybind> possibleKeybinds = new ArrayList<>(
			Arrays.asList(new PossibleKeybind(dotT, "Toggle " + name),
				new PossibleKeybind(dotT + " on", "Enable " + name),
				new PossibleKeybind(dotT + " off", "Disable " + name)));
		
		// settings keybinds
		for(Setting setting : settings)
			possibleKeybinds.addAll(setting.getPossibleKeybinds(name));
		
		return possibleKeybinds;
	}
	
	@Override
	public final String getPrimaryAction()
	{
		return enabled ? "Disable" : "Enable";
	}
	
	@Override
	public final void doPrimaryAction()
	{
		toggle();
	}
	
	@Override
	public final String getHelpPage()
	{
		return help;
	}
	
	public Bypasses getBypasses()
	{
		return bypasses;
	}
	
	@Override
	public Feature[] getSeeAlso()
	{
		return new Feature[0];
	}
	
	@Override
	public final boolean isEnabled()
	{
		return enabled;
	}
	
	public final boolean isActive()
	{
		return active;
	}
	
	public final void setEnabled(boolean enabled)
	{
		if(this.enabled == enabled)
			return;
		
		this.enabled = enabled;
		
		active = enabled && !blocked;
		
		if(blocked && enabled)
			return;
		
		try
		{
			onToggle();
			
			if(enabled)
				onEnable();
			else
				onDisable();
			
		}catch(Throwable e)
		{
			CrashReport report =
				CrashReport.makeCrashReport(e, "Toggling Wurst mod");
			
			CrashReportCategory category = report.makeCategory("Affected mod");
			category.setDetail("Mod name", () -> name);
			category.setDetail("Attempted action",
				() -> enabled ? "Enable" : "Disable");
			
			throw new ReportedException(report);
		}
		
		if(!ConfigFiles.MODS.isModBlacklisted(this))
			ConfigFiles.MODS.save();
	}
	
	public final void enableOnStartup()
	{
		enabled = true;
		active = enabled && !blocked;
		
		try
		{
			onToggle();
			onEnable();
		}catch(Throwable e)
		{
			CrashReport report =
				CrashReport.makeCrashReport(e, "Toggling Wurst mod");
			
			CrashReportCategory category = report.makeCategory("Affected mod");
			category.setDetail("Mod name", () -> name);
			category.setDetail("Attempted action", () -> "Enable on startup");
			
			throw new ReportedException(report);
		}
	}
	
	public final void toggle()
	{
		setEnabled(!isEnabled());
	}
	
	@Override
	public boolean isBlocked()
	{
		return blocked;
	}
	
	public void setBlocked(boolean blocked)
	{
		this.blocked = blocked;
		active = enabled && !blocked;
		if(enabled)
			try
			{
				onToggle();
				if(blocked)
					onDisable();
				else
					onEnable();
			}catch(Throwable e)
			{
				CrashReport report =
					CrashReport.makeCrashReport(e, "Toggling Wurst mod");
				
				CrashReportCategory category =
					report.makeCategory("Affected mod");
				category.setDetail("Mod name", () -> name);
				category.setDetail("Attempted action",
					() -> blocked ? "Block" : "Unblock");
				
				throw new ReportedException(report);
			}
	}
	
	public final void updateMS()
	{
		currentMS = System.currentTimeMillis();
	}
	
	public final void updateLastMS()
	{
		lastMS = System.currentTimeMillis();
	}
	
	public final boolean hasTimePassedM(long MS)
	{
		return currentMS >= lastMS + MS;
	}
	
	public final boolean hasTimePassedS(float speed)
	{
		return currentMS >= lastMS + (long)(1000 / speed);
	}
	
	public void onToggle()
	{}
	
	public void onEnable()
	{}
	
	public void onDisable()
	{}
	
	public void initSettings()
	{}
	
	public void onYesCheatUpdate(BypassLevel bypassLevel)
	{}
}
