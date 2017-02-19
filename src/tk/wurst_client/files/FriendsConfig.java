/*
 * Copyright © 2014 - 2017 | Wurst-Imperium | All rights reserved.
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.files;

import java.io.File;

import com.google.gson.JsonElement;

import tk.wurst_client.WurstClient;
import tk.wurst_client.options.FriendsList;
import tk.wurst_client.utils.JsonUtils;

public final class FriendsConfig extends Config
{
	public FriendsConfig()
	{
		super(new File(WurstFolders.MAIN, "friends.json"));
	}
	
	@Override
	protected void loadFromJson(JsonElement json)
	{
		WurstClient.INSTANCE.friends =
			JsonUtils.gson.fromJson(json, FriendsList.class);
	}
	
	@Override
	protected JsonElement saveToJson()
	{
		return JsonUtils.gson.toJsonTree(WurstClient.INSTANCE.friends);
	}
	
}
