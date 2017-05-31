/*
 * Copyright © 2014 - 2017 | Wurst-Imperium | All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.wurstclient.tabgui;

import java.util.ArrayList;

import net.wurstclient.features.Feature;

public final class Tab
{
	private final String name;
	private final ArrayList<Feature> features = new ArrayList<>();
	
	public Tab(String name)
	{
		this.name = name;
	}
	
	public void add(Feature feature)
	{
		features.add(feature);
	}
	
	public String getName()
	{
		return name;
	}
}
