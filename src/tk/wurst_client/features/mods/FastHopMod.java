/*
 * Copyright © 2014 - 2017 | Wurst-Imperium | All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tk.wurst_client.features.mods;
import tk.wurst_client.events.MoveEvent;

import tk.wurst_client.events.listeners.UpdateListener;

import tk.wurst_client.features.mods.Mod;

import tk.wurst_client.features.mods.Mod.*;

import net.minecraft.init.MobEffects;

@Bypasses(ghostMode = false, latestNCP = false, olderNCP = false, mineplex = false)

@Info(description = "Faster speed", name = "VanillaSpeed")

public class FastHop extends Mod implements UpdateListener

{

	private int janitorStage;

    private int stage;

	private double moveSpeed;

	@Override

  public void onEnable() 

	{

		this.stage = 1;

		wurst.events.add(UpdateListener.class, this);

	}

    public static double getBaseMoveSpeed() 

    {

        double baseSpeed = 0.6873d;

        if (mc.player.isPotionActive(MobEffects.SPEED)) 

        {

            final int amplifier = mc.player.getActivePotionEffect(MobEffects.SPEED).getAmplifier();

            baseSpeed *= 1.0 + 0.2 * (amplifier + 0.2);

        }

        return baseSpeed;

        }

  @Override

  public void onUpdate() 

	{

        if(mc.player.onGround && mc.player.moveForward != 0.0f || mc.player.onGround && mc.player.moveStrafing != 0.0f) 

        {

            mc.player.motionY = 0.4f;

        }

        this.onMove(0.8f, 0.12f, 0.02f);

	}

    public void onMove(float speed, float jSpeed, float janitorBaseSpeed) 

    {

    	if(this.stage == 1)

    	{

    		this.setMoveSpeed(0.1f);

    		++this.stage;    	}

    	if (this.stage == 2)

    	{

    		this.setMoveSpeed(1.8d);

    		--this.stage;

    	}

    }

	@Override

	public void onDisable() 

	{

		wurst.events.remove(UpdateListener.class, this);

	}

    public static void setMoveSpeed(final double speed) 

    {

        double forward = mc.player.movementInput.moveForward;

        double strafe = mc.player.movementInput.moveStrafe;

        float yaw = mc.player.rotationYaw;

        if (forward == 0.0 && strafe == 0.0) 

        {

        	mc.player.motionX = 0.0f;

        	mc.player.motionZ = 0.0f;

        }

        else 

        {

            if (forward != 0.0) 

            {

                if (strafe > 0.0) 

                {

                    yaw += ((forward > 0.0) ? -45 : 45);

                }

                else if (strafe < 0.0)

                {

                    yaw += ((forward > 0.0) ? 45 : -45);

                }

                strafe = 0.0;

                if (forward > 0.0)

                {

                    forward = 1.0;

                }

                else if (forward < 0.0) {

                    forward = -1.0;

                }

            }

            mc.player.motionX = forward * speed * Math.cos(Math.toRadians(yaw + 90.0f)) + strafe * speed * Math.sin(Math.toRadians(yaw + 90.0f));

            mc.player.motionZ = forward * speed * Math.sin(Math.toRadians(yaw + 90.0f)) - strafe * speed * Math.cos(Math.toRadians(yaw + 90.0f)); 

        }

    }    

}
