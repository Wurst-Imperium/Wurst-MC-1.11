/*
 * Copyright © 2014 - 2017 | Wurst-Imperium | All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.wurstclient.keybinds;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.lwjgl.input.Keyboard;

import net.wurstclient.WurstClient;
import net.wurstclient.compatibility.WMinecraft;
import net.wurstclient.files.ConfigFiles;

public class KeybindManager
{
	private final TreeMap<String, TreeSet<String>> map = new TreeMap<>();
	
	public void loadDefaults()
	{
		map.clear();
		bind("B", ".t fastbreak", ".t fastplace");
		bind("C", ".t fullbright");
		bind("G", ".t flight");
		bind("GRAVE", ".t speednuker");
		bind("H", ".t /home");
		bind("J", ".t jesus");
		bind("K", ".t multiaura");
		bind("L", ".t nuker");
		bind("LCONTROL", ".t navigator");
		bind("R", ".t killaura");
		bind("RSHIFT", ".t navigator");
		bind("U", ".t freecam");
		bind("X", ".t x-ray");
		bind("Z", ".t sneak");
	}
	
	public void bind(String key, String... commands)
	{
		bind(key, Arrays.asList(commands));
	}
	
	public void bind(String key, Collection<String> commands)
	{
		map.put(key, new TreeSet<>(commands));
	}
	
	public void unbind(String key)
	{
		map.remove(key);
	}
	
	public void addBind(String key, String command)
	{
		TreeSet<String> commands = map.get(key);
		if(commands != null)
			commands.add(command);
		else
			bind(key, command);
	}
	
	public void removeBind(String key, String command)
	{
		TreeSet<String> commands = map.get(key);
		if(commands == null)
			return;
		
		commands.remove(command);
		if(commands.isEmpty())
			map.remove(key);
	}
	
	public void forceAddGuiKeybind()
	{
		for(TreeSet<String> value : map.values())
			if(value.contains(".t navigator"))
				return;
			
		addBind("LCONTROL", ".t navigator");
		ConfigFiles.KEYBINDS.save();
	}
	
	public int size()
	{
		return map.size();
	}
	
	public TreeSet<String> get(Object key)
	{
		return map.get(key);
	}
	
	public void clear()
	{
		map.clear();
	}
	
	public Set<Entry<String, TreeSet<String>>> entrySet()
	{
		return map.entrySet();
	}
	
	public Set<String> keySet()
	{
		return map.keySet();
	}
	
	public void onKeyPress()
	{
		if(!WurstClient.INSTANCE.isEnabled())
			return;
		
		int key = Keyboard.getEventKey();
		if(key == 0)
			return;
		
		TreeSet<String> commands = map.get(Keyboard.getKeyName(key));
		if(commands == null)
			return;
		
		commands.forEach(
			cmd -> WMinecraft.getPlayer().sendAutomaticChatMessage(cmd));
	}
}
