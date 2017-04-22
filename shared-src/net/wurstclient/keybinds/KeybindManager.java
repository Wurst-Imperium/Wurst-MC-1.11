/*
 * Copyright © 2014 - 2017 | Wurst-Imperium | All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.wurstclient.keybinds;

import java.util.Arrays;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class KeybindManager
{
	private final TreeMap<String, TreeSet<String>> map = new TreeMap<>();
	
	public void loadDefaults()
	{
		put("B", ".t fastbreak", ".t fastplace");
		put("C", ".t fullbright");
		put("G", ".t flight");
		put("GRAVE", ".t speednuker");
		put("H", ".t /home");
		put("J", ".t jesus");
		put("K", ".t multiaura");
		put("L", ".t nuker");
		put("LCONTROL", ".t navigator");
		put("R", ".t killaura");
		put("RSHIFT", ".t navigator");
		put("U", ".t freecam");
		put("X", ".t x-ray");
		put("Z", ".t sneak");
	}
	
	public void put(String key, String... commands)
	{
		map.put(key, new TreeSet<>(Arrays.asList(commands)));
	}

	public int size()
	{
		return map.size();
	}

	public boolean containsValue(Object value)
	{
		return map.containsValue(value);
	}

	public TreeSet<String> get(Object key)
	{
		return map.get(key);
	}

	public TreeSet<String> put(String key, TreeSet<String> value)
	{
		return map.put(key, value);
	}

	public TreeSet<String> remove(Object key)
	{
		return map.remove(key);
	}

	public void clear()
	{
		map.clear();
	}

	public Set<Entry<String, TreeSet<String>>> entrySet()
	{
		return map.entrySet();
	}
}
