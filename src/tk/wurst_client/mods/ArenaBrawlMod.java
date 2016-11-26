/*
 * Copyright © 2014 - 2016 | Wurst-Imperium | All rights reserved.
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.mods;

import java.awt.Color;
import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import tk.wurst_client.events.ChatInputEvent;
import tk.wurst_client.events.listeners.ChatInputListener;
import tk.wurst_client.events.listeners.DeathListener;
import tk.wurst_client.events.listeners.RenderListener;
import tk.wurst_client.events.listeners.UpdateListener;
import tk.wurst_client.mods.Mod.Bypasses;
import tk.wurst_client.mods.Mod.Info;
import tk.wurst_client.navigator.settings.SliderSetting;
import tk.wurst_client.navigator.settings.SliderSetting.ValueDisplay;
import tk.wurst_client.utils.BlockUtils;
import tk.wurst_client.utils.EntityUtils;
import tk.wurst_client.utils.RenderUtils;
import tk.wurst_client.utils.EntityUtils.TargetSettings;

@Info(description = "Makes ArenaBrawl on mc.hypixel.net a lot easier.\n"
	+ "This is a collection of mods that have been optimized\n"
	+ "for ArenaBrawl. It will bypass everything that Hypixel\n"
	+ "has to offer.", name = "ArenaBrawl", help = "Mods/ArenaBrawl")
@Bypasses(ghostMode = false)
public class ArenaBrawlMod extends Mod
	implements ChatInputListener, DeathListener, RenderListener, UpdateListener
{
	private Entity friend;
	public static float range = 4.25F;
	public static ArrayList<String> scoreboard = new ArrayList<String>();
	private ArrayList<int[]> matchingBlocks = new ArrayList<int[]>();
	private ArrayList<int[]> enemyTotems = new ArrayList<int[]>();
	private ArrayList<int[]> friendTotems = new ArrayList<int[]>();
	private String friendsName;
	private boolean frame;
	private int target;
	private TargetType targetType;
	private Entity entityTarget;
	private int[] blockTarget;
	private long lastAttack = 0L;
	public int level = 40;
	
	private TargetSettings targetSettingsAll = new TargetSettings()
	{
		@Override
		public boolean targetFriends()
		{
			return true;
		}
		
		@Override
		public boolean targetBehindWalls()
		{
			return true;
		};
		
		@Override
		public boolean targetPlayers()
		{
			return true;
		}
		
		@Override
		public boolean targetAnimals()
		{
			return false;
		}
		
		@Override
		public boolean targetMonsters()
		{
			return false;
		}
		
		@Override
		public boolean targetGolems()
		{
			return false;
		}
		
		@Override
		public boolean targetSleepingPlayers()
		{
			return false;
		}
		
		@Override
		public boolean targetInvisiblePlayers()
		{
			return false;
		}
		
		@Override
		public boolean targetInvisibleMobs()
		{
			return false;
		}
		
		@Override
		public boolean targetTeams()
		{
			return false;
		}
	};
	
	private TargetSettings targetSettingsVisible = new TargetSettings()
	{
		@Override
		public boolean targetFriends()
		{
			return true;
		}
		
		@Override
		public boolean targetBehindWalls()
		{
			return false;
		};
		
		@Override
		public boolean targetPlayers()
		{
			return true;
		}
		
		@Override
		public boolean targetAnimals()
		{
			return false;
		}
		
		@Override
		public boolean targetMonsters()
		{
			return false;
		}
		
		@Override
		public boolean targetGolems()
		{
			return false;
		}
		
		@Override
		public boolean targetSleepingPlayers()
		{
			return false;
		}
		
		@Override
		public boolean targetInvisiblePlayers()
		{
			return false;
		}
		
		@Override
		public boolean targetInvisibleMobs()
		{
			return false;
		}
		
		@Override
		public boolean targetTeams()
		{
			return false;
		}
	};
	
	@Override
	public String getRenderName()
	{
		if(friendsName != null)
			return "ArenaBrawl with " + friendsName;
		else
			return "ArenaBrawl";
	}
	
	@Override
	public void initSettings()
	{
		settings.add(
			new SliderSetting("Level", level, 20, 100, 10, ValueDisplay.INTEGER)
			{
				@Override
				public void update()
				{
					level = (int)getValue();
				}
			});
	}
	
	@Override
	public void onEnable()
	{
		reset();
		wurst.events.add(ChatInputListener.class, this);
		wurst.events.add(DeathListener.class, this);
		wurst.events.add(RenderListener.class, this);
		wurst.events.add(UpdateListener.class, this);
	}
	
	@Override
	public void onRender()
	{
		if(targetType == TargetType.BLOCK_E)
		{
			double x = blockTarget[0];
			double y = blockTarget[1];
			double z = blockTarget[2];
			RenderUtils.box(x, y, z, x + 1, y + 2, z + 1, 1F, 0F, 0F, 0.25F);
		}else if(targetType == TargetType.BLOCK_F)
		{
			double x = blockTarget[0];
			double y = blockTarget[1];
			double z = blockTarget[2];
			RenderUtils.box(x, y, z, x + 1, y + 2, z + 1, 0F, 1F, 0F, 0.25F);
		}else if(targetType == TargetType.ENTITY_E && entityTarget != null)
		{
			double x = entityTarget.posX;
			double y = entityTarget.posY;
			double z = entityTarget.posZ;
			RenderUtils.box(x - 0.35, y, z - 0.35, x + 0.35, y + 1.9, z + 0.35,
				1F, 0F, 0F, 0.25F);
		}else if(targetType == TargetType.ENTITY_F && entityTarget != null)
		{
			double x = entityTarget.posX;
			double y = entityTarget.posY;
			double z = entityTarget.posZ;
			RenderUtils.box(x - 0.35, y, z - 0.35, x + 0.35, y + 1.9, z + 0.35,
				0F, 1F, 0F, 0.25F);
		}
		Entity enemy1 =
			EntityUtils.getEntityWithName(formatSBName(5), targetSettingsAll);
		if(enemy1 != null)
		{
			RenderUtils.entityESPBox(enemy1, RenderUtils.target);
			RenderUtils.tracerLine(enemy1, RenderUtils.target);
		}
		Entity enemy2 =
			EntityUtils.getEntityWithName(formatSBName(4), targetSettingsAll);
		if(enemy2 != null)
		{
			RenderUtils.entityESPBox(enemy2, RenderUtils.target);
			RenderUtils.tracerLine(enemy2, RenderUtils.target);
		}
		if(friend != null)
		{
			RenderUtils.entityESPBox(friend, RenderUtils.team);
			RenderUtils.tracerLine(friend, RenderUtils.team);
		}
		if(!enemyTotems.isEmpty())
			for(int[] totem : enemyTotems)
			{
				double x = totem[0];
				double y = totem[1];
				double z = totem[2];
				RenderUtils.frame(x, y, z, x + 1, y + 2, z + 1,
					new Color(255, 0, 0, 128));
				RenderUtils.tracerLine((int)x, (int)y, (int)z,
					new Color(255, 0, 0, 128));
			}
		if(!friendTotems.isEmpty())
			for(int[] totem : friendTotems)
			{
				double x = totem[0];
				double y = totem[1];
				double z = totem[2];
				RenderUtils.frame(x, y, z, x + 1, y + 2, z + 1,
					new Color(0, 255, 0, 128));
			}
	}
	
	@Override
	public void onUpdate()
	{
		if(scoreboard != null
			&& (scoreboard.size() == 13 || scoreboard.size() == 11))
		{// If you are in the lobby:
			wurst.chat.message("You need to be in a 2v2 arena.");
			setEnabled(false);
			return;
		}
		if(scoreboard == null)
			return;
		if(!frame && scoreboard.size() == 8)
			try
			{
				setupFrame();
			}catch(Exception e)
			{
				e.printStackTrace();
				frame = false;
				return;
			}
		if(friend == null || friend.isDead)
			friend =
				EntityUtils.getEntityWithName(friendsName, targetSettingsAll);
		updateMS();
		try
		{
			scanTotems();
			getTarget();
			if(!mc.player.isCollidedHorizontally
				&& mc.player.moveForward > 0 && !mc.player.isSneaking())
			{// Built-in AutoSprint and BunnyHop:
				mc.player.setSprinting(true);
				if(mc.player.onGround && mc.player.isSprinting())
					mc.player.jump();
			}
			if(targetType == TargetType.BLOCK_E)
			{
				float distX = (float)(blockTarget[0] - mc.player.posX);
				float distY = (float)(blockTarget[1] - mc.player.posY);
				float distZ = (float)(blockTarget[2] - mc.player.posZ);
				if(BlockUtils.getBlockDistance(distX, distY, distZ) <= 4.25)
				{// If the target is an enemy totem in range:
					faceTarget();
					attackTarget();
				}else
				{
					KeyBinding.setKeyBindState(
						mc.gameSettings.keyBindAttack.getKeyCode(), false);
					KeyBinding.setKeyBindState(
						mc.gameSettings.keyBindUseItem.getKeyCode(), false);
				}
			}else if(targetType == TargetType.ENTITY_E)
			{
				if(mc.player.getDistanceToEntity(entityTarget) <= 4.25)
				{// If the target is an enemy in range:
					faceTarget();
					attackTarget();
				}else
				{
					KeyBinding.setKeyBindState(
						mc.gameSettings.keyBindAttack.getKeyCode(), false);
					KeyBinding.setKeyBindState(
						mc.gameSettings.keyBindUseItem.getKeyCode(), false);
				}
			}else
			{
				KeyBinding.setKeyBindState(
					mc.gameSettings.keyBindAttack.getKeyCode(), false);
				KeyBinding.setKeyBindState(
					mc.gameSettings.keyBindUseItem.getKeyCode(), false);
			}
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	@Override
	public void onDisable()
	{
		wurst.events.remove(ChatInputListener.class, this);
		wurst.events.remove(DeathListener.class, this);
		wurst.events.remove(RenderListener.class, this);
		wurst.events.remove(UpdateListener.class, this);
		mc.gameSettings.keyBindForward.pressed = false;
		if(friendsName != null)
			wurst.chat.message(
				"No longer playing ArenaBrawl with " + friendsName + ".");
		reset();
	}
	
	@Override
	public void onReceivedMessage(ChatInputEvent event)
	{
		String message = event.getComponent().getUnformattedText();
		if(message.startsWith("[Arena]: ")
			&& message.endsWith(" has won the game!"))
		{
			event.cancel();
			wurst.chat.message(message.substring(9));
			setEnabled(false);
		}
	}
	
	@Override
	public void onDeath()
	{
		mc.player.respawnPlayer();
		mc.displayGuiScreen((GuiScreen)null);
		wurst.chat.message("You died.");
		setEnabled(false);
	}
	
	private void setupFrame()
	{
		friendsName = formatSBName(0);
		wurst.chat.message("Now playing ArenaBrawl with " + friendsName + ".");
		frame = true;
	}
	
	private String formatSBName(int index)
	{
		try
		{
			return scoreboard.get(index).split(" ")[0].substring(2,
				scoreboard.get(index).split(" ")[0].length() - 2);
		}catch(Exception e)
		{
			return null;
		}
	}
	
	private void scanTotems()
	{
		matchingBlocks.clear();
		for(int y = 3; y >= -3; y--)
			for(int x = 50; x >= -50; x--)
				for(int z = 50; z >= -50; z--)
				{
					int posX = (int)(mc.player.posX + x);
					int posY = (int)(mc.player.posY + y);
					int posZ = (int)(mc.player.posZ + z);
					if(Block.getIdFromBlock(mc.world
						.getBlockState(new BlockPos(posX, posY, posZ))
						.getBlock()) == Block
							.getIdFromBlock(Block.getBlockFromName("wool")))
						matchingBlocks.add(new int[]{posX, posY, posZ});
				}
		enemyTotems.clear();
		for(int i = 0; i < matchingBlocks.size(); i++)
		{
			IBlockState blockState =
				mc.world.getBlockState(new BlockPos(matchingBlocks.get(i)[0],
					matchingBlocks.get(i)[1] + 1, matchingBlocks.get(i)[2]));
			if(blockState.getBlock().getMetaFromState(blockState) == 14// red
				&& Block.getIdFromBlock(blockState.getBlock()) != 0)
				enemyTotems.add(new int[]{matchingBlocks.get(i)[0],
					matchingBlocks.get(i)[1] + 1, matchingBlocks.get(i)[2]});
		}
		friendTotems.clear();
		for(int i = 0; i < matchingBlocks.size(); i++)
		{
			IBlockState blockState =
				mc.world.getBlockState(new BlockPos(matchingBlocks.get(i)[0],
					matchingBlocks.get(i)[1] + 1, matchingBlocks.get(i)[2]));
			if(blockState.getBlock().getMetaFromState(blockState) == 5// lime
				&& Block.getIdFromBlock(blockState.getBlock()) != 0)
				friendTotems.add(new int[]{matchingBlocks.get(i)[0],
					matchingBlocks.get(i)[1] + 1, matchingBlocks.get(i)[2]});
		}
	}
	
	private void getTarget()
	{
		blockTarget = null;
		entityTarget = null;
		target = -1;
		targetType = null;
		if(!enemyTotems.isEmpty())
		{// If there is an enemy totem:
			int[] closestTotem = null;
			float dist = 999999999;
			for(int[] totem : enemyTotems)
			{
				float distX = (float)(totem[0] - mc.player.posX);
				float distY = (float)(totem[1] - mc.player.posY);
				float distZ = (float)(totem[2] - mc.player.posZ);
				dist = BlockUtils.getBlockDistance(distX, distY, distZ);
				if(closestTotem == null)
					closestTotem = totem;
				else
				{
					float distXC = (float)(closestTotem[0] - mc.player.posX);
					float distYC = (float)(closestTotem[1] - mc.player.posY);
					float distZC = (float)(closestTotem[2] - mc.player.posZ);
					float distC =
						BlockUtils.getBlockDistance(distXC, distYC, distZC);
					if(dist < distC)
						closestTotem = totem;
				}
			}
			target = 8
				+ (friendTotems.size() + enemyTotems.indexOf(closestTotem) + 1)
					* 2;
			targetType = TargetType.BLOCK_E;
			blockTarget = closestTotem;
			if(dist <= 4.25)
				return;
		}
		Entity enemy1 = EntityUtils.getEntityWithName(formatSBName(5),
			targetSettingsVisible);
		Entity enemy2 = EntityUtils.getEntityWithName(formatSBName(4),
			targetSettingsVisible);
		if(enemy1 != null || enemy2 != null)
		{// If one of the enemies can be seen:
			if(enemy2 == null)
			{
				entityTarget = enemy1;
				target = 6;
			}else if(enemy1 == null)
			{
				entityTarget = enemy2;
				target = 8;
			}else if(mc.player.getDistanceToEntity(enemy1) <= mc.player
				.getDistanceToEntity(enemy2))
			{
				entityTarget = enemy1;
				target = 6;
			}else
			{
				entityTarget = enemy2;
				target = 8;
			}
			targetType = TargetType.ENTITY_E;
			if(mc.player.getDistanceToEntity(entityTarget) <= 4.25)
				return;
		}// Enemies have a lower priority than enemy totems.
		if(!friendTotems.isEmpty())
		{// If there is a friend totem:
			int[] closestTotem = null;
			float dist = 999999999;
			for(int[] totem : friendTotems)
			{
				float distX = (float)(totem[0] - mc.player.posX);
				float distY = (float)(totem[1] - mc.player.posY);
				float distZ = (float)(totem[2] - mc.player.posZ);
				dist = BlockUtils.getBlockDistance(distX, distY, distZ);
				if(closestTotem == null)
					closestTotem = totem;
				else
				{
					float distXC = (float)(closestTotem[0] - mc.player.posX);
					float distYC = (float)(closestTotem[1] - mc.player.posY);
					float distZC = (float)(closestTotem[2] - mc.player.posZ);
					float distC =
						BlockUtils.getBlockDistance(distXC, distYC, distZC);
					if(dist < distC)
						closestTotem = totem;
				}
			}
			target = 8 + (friendTotems.indexOf(closestTotem) + 1) * 2;
			targetType = TargetType.BLOCK_F;
			blockTarget = closestTotem;
			return;
		}// Friend totems have a lower priority than enemies in range, but a
			// higher priority than enemies out of range.
		if(target == -1)
		{// If there is no other target:
			entityTarget = friend;
			target = 4;
			targetType = TargetType.ENTITY_F;
			return;
		}// The friend has the lowest priority.
	}
	
	private enum TargetType
	{
		BLOCK_E,
		BLOCK_F,
		ENTITY_E,
		ENTITY_F;
	}
	
	private void faceTarget()
	{
		if(targetType == TargetType.BLOCK_E)
			BlockUtils.faceBlockClient(
				new BlockPos(blockTarget[0], blockTarget[1], blockTarget[2]));
		else if(targetType == TargetType.ENTITY_E
			|| targetType == TargetType.ENTITY_F)
			EntityUtils.faceEntityClient(entityTarget);
	}
	
	private void attackTarget()
	{
		if(targetType == TargetType.BLOCK_E)
		{// Attacks the totem with the sword:
			if(System.currentTimeMillis() >= lastAttack + 50)
			{
				mc.gameSettings.keyBindAttack.pressed =
					!mc.gameSettings.keyBindAttack.pressed;
				lastAttack = System.currentTimeMillis();
				mc.gameSettings.keyBindUseItem.pressed = false;
			}
		}else if(targetType == TargetType.ENTITY_E)
			if(System.currentTimeMillis() >= lastAttack + 100)
			{
				if(mc.player.experienceLevel >= level)
					mc.gameSettings.keyBindUseItem.pressed =
						!mc.gameSettings.keyBindUseItem.pressed;
				else
				{
					mc.gameSettings.keyBindUseItem.pressed = false;
					mc.player.swingArm(EnumHand.MAIN_HAND);
					mc.playerController.attackEntity(mc.player,
						entityTarget);
				}
				lastAttack = System.currentTimeMillis();
			}
	}
	
	private void reset()
	{
		mc.gameSettings.keyBindUseItem.pressed = false;
		matchingBlocks.clear();
		enemyTotems.clear();
		friendTotems.clear();
		frame = false;
		friend = null;
		entityTarget = null;
		blockTarget = null;
		targetType = null;
		friendsName = null;
	}
}
