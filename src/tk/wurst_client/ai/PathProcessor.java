/*
 * Copyright © 2014 - 2016 | Wurst-Imperium | All rights reserved.
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.ai;

import java.util.ArrayList;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import tk.wurst_client.WurstClient;
import tk.wurst_client.utils.BlockUtils;

public abstract class PathProcessor
{
	protected final WurstClient wurst = WurstClient.INSTANCE;
	protected final Minecraft mc = Minecraft.getMinecraft();
	
	protected final ArrayList<PathPos> path;
	protected int index;
	protected boolean done;
	protected boolean failed;
	
	private final KeyBinding[] controls = new KeyBinding[]{
		mc.gameSettings.keyBindForward, mc.gameSettings.keyBindBack,
		mc.gameSettings.keyBindRight, mc.gameSettings.keyBindLeft,
		mc.gameSettings.keyBindJump, mc.gameSettings.keyBindSneak};
	
	public PathProcessor(ArrayList<PathPos> path)
	{
		if(path.isEmpty())
			throw new IllegalStateException("There is no path!");
		
		this.path = path;
	}
	
	public abstract void process();
	
	public void stop()
	{
		releaseControls();
	}
	
	public void lockControls()
	{
		for(KeyBinding key : controls)
			key.pressed = false;
		mc.player.rotationPitch = 10;
		if(index < path.size())
			BlockUtils.faceBlockClientHorizontally(path.get(index));
		mc.player.setSprinting(false);
	}
	
	public final void releaseControls()
	{
		// reset keys
		for(KeyBinding key : controls)
			key.pressed = Keyboard.isKeyDown(key.getKeyCode());
	}
	
	public int getIndex()
	{
		return index;
	}
	
	public final boolean isDone()
	{
		return done;
	}
	
	public final boolean isFailed()
	{
		return failed;
	}
}
