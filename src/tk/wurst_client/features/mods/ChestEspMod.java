/*
 * Copyright © 2014 - 2016 | Wurst-Imperium | All rights reserved.
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.features.mods;

import java.util.ArrayDeque;

import net.minecraft.block.BlockChest;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecartChest;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraft.util.math.BlockPos;
import tk.wurst_client.events.listeners.RenderListener;
import tk.wurst_client.features.Feature;
import tk.wurst_client.utils.ChatUtils;
import tk.wurst_client.utils.RenderUtils;

@Mod.Info(description = "Allows you to see chests through walls.",
	name = "ChestESP",
	tags = "ChestFinder, chest esp, chest finder",
	help = "Mods/ChestESP")
@Mod.Bypasses
public class ChestEspMod extends Mod implements RenderListener
{
	private int maxChests = 1000;
	public boolean shouldInform = true;
	private TileEntityChest openChest;
	private ArrayDeque<TileEntityChest> emptyChests = new ArrayDeque<>();
	private ArrayDeque<TileEntityChest> nonEmptyChests = new ArrayDeque<>();
	
	@Override
	public Feature[] getSeeAlso()
	{
		return new Feature[]{wurst.mods.itemEspMod, wurst.mods.searchMod,
			wurst.mods.xRayMod};
	}
	
	@Override
	public void onEnable()
	{
		shouldInform = true;
		emptyChests.clear();
		nonEmptyChests.clear();
		wurst.events.add(RenderListener.class, this);
	}
	
	@Override
	public void onDisable()
	{
		wurst.events.remove(RenderListener.class, this);
	}
	
	@Override
	public void onRender()
	{
		int chests = 0;
		
		for(int i = 0; i < mc.world.loadedTileEntityList.size(); i++)
		{
			TileEntity tileEntity = mc.world.loadedTileEntityList.get(i);
			if(chests >= maxChests)
				break;
			if(tileEntity instanceof TileEntityChest)
			{
				chests++;
				TileEntityChest chest = ((TileEntityChest)tileEntity);
				boolean trapped = chest.getChestType() == BlockChest.Type.TRAP;
				
				if(emptyChests.contains(tileEntity))
					RenderUtils.blockEspBox(chest.getPos(), 0.25, 0.25, 0.25);
				else if(nonEmptyChests.contains(tileEntity))
					if(trapped)
						RenderUtils.blockEspBox(chest.getPos(), 0.5, 0.25, 0);
					else
						RenderUtils.blockEspBox(chest.getPos(), 0, 0.5, 0);
				else if(trapped)
					RenderUtils.blockEsp(chest.getPos(), 1, 0.5, 0);
				else
					RenderUtils.blockEsp(chest.getPos(), 0, 1, 0);
				
				if(trapped)
					RenderUtils.blockEspFrame(chest.getPos(), 1, 0.5, 0);
				else
					RenderUtils.blockEspFrame(chest.getPos(), 0, 1, 0);
			}else if(tileEntity instanceof TileEntityEnderChest)
			{
				chests++;
				RenderUtils.blockEsp(
					((TileEntityEnderChest)tileEntity).getPos(), 0, 1, 1);
			}
		}
		
		for(int i = 0; i < mc.world.loadedEntityList.size(); i++)
		{
			Entity entity = mc.world.loadedEntityList.get(i);
			if(chests >= maxChests)
				break;
			if(entity instanceof EntityMinecartChest)
			{
				chests++;
				RenderUtils
					.blockEsp(((EntityMinecartChest)entity).getPosition());
			}
		}
		
		if(chests >= maxChests && shouldInform)
		{
			ChatUtils.warning(getName() + " found §lA LOT§r of chests.");
			ChatUtils.message("To prevent lag, it will only show the first "
				+ maxChests + " chests.");
			shouldInform = false;
		}else if(chests < maxChests)
			shouldInform = true;
	}
	
	public void openChest(BlockPos pos)
	{
		TileEntity tileEntity = mc.world.getTileEntity(pos);
		if(tileEntity instanceof TileEntityChest)
			openChest = (TileEntityChest)tileEntity;
	}
	
	public void closeChest(Container inventorySlots)
	{
		if(openChest == null)
			return;
		
		boolean empty = true;
		for(int i = 0; i < inventorySlots.inventorySlots.size() - 36; i++)
			if(((Slot)inventorySlots.inventorySlots.get(i)).getStack() != null)
			{
				empty = false;
				break;
			}
		
		if(empty)
		{
			if(!emptyChests.contains(openChest))
				emptyChests.addLast(openChest);
			
			if(emptyChests.size() >= 64)
				emptyChests.removeFirst();
			
			nonEmptyChests.remove(openChest);
		}else
		{
			if(!nonEmptyChests.contains(openChest))
				nonEmptyChests.addLast(openChest);
			
			if(nonEmptyChests.size() >= 64)
				nonEmptyChests.removeFirst();
			
			emptyChests.remove(openChest);
		}
		
		openChest = null;
	}
}
