/*
 * Copyright © 2014 - 2017 | Wurst-Imperium | All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.utils;

import static org.lwjgl.opengl.GL11.*;

import java.awt.Color;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class RenderUtils
{
	private static final AxisAlignedBB DEFAULT_AABB =
		new AxisAlignedBB(0, 0, 0, 1, 1, 1);
	
	/**
	 * Renders a green ESP box with the size of a normal block at the specified
	 * BlockPos.
	 */
	public static void blockEsp(BlockPos blockPos)
	{
		blockEsp(blockPos, 0, 1, 0);
	}
	
	/**
	 * Renders an ESP box with the size of a normal block at the specified
	 * BlockPos.
	 */
	public static void blockEsp(BlockPos blockPos, double red, double green,
		double blue)
	{
		double x = blockPos.getX()
			- Minecraft.getMinecraft().getRenderManager().renderPosX;
		double y = blockPos.getY()
			- Minecraft.getMinecraft().getRenderManager().renderPosY;
		double z = blockPos.getZ()
			- Minecraft.getMinecraft().getRenderManager().renderPosZ;
		GL11.glBlendFunc(770, 771);
		GL11.glEnable(GL_BLEND);
		GL11.glLineWidth(1.0F);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL_DEPTH_TEST);
		GL11.glDepthMask(false);
		GL11.glColor4d(red, green, blue, 0.15);
		drawColorBox(new AxisAlignedBB(x, y, z, x + 1.0, y + 1.0, z + 1.0), 0F,
			0F, 0F, 0F);
		GL11.glColor4d(0, 0, 0, 0.5);
		drawSelectionBoundingBox(
			new AxisAlignedBB(x, y, z, x + 1.0, y + 1.0, z + 1.0));
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL_DEPTH_TEST);
		GL11.glDepthMask(true);
		GL11.glDisable(GL_BLEND);
	}
	
	public static void entityESPBox(Entity entity, int mode)
	{
		GL11.glBlendFunc(770, 771);
		GL11.glEnable(GL_BLEND);
		GL11.glLineWidth(2.0F);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL_DEPTH_TEST);
		GL11.glDepthMask(false);
		if(mode == 0)// Enemy
			GL11.glColor4d(
				1 - Minecraft.getMinecraft().player.getDistanceToEntity(entity)
					/ 40,
				Minecraft.getMinecraft().player.getDistanceToEntity(entity)
					/ 40,
				0, 0.5F);
		else if(mode == 1)// Friend
			GL11.glColor4d(0, 0, 1, 0.5F);
		else if(mode == 2)// Other
			GL11.glColor4d(1, 1, 0, 0.5F);
		else if(mode == 3)// Target
			GL11.glColor4d(1, 0, 0, 0.5F);
		else if(mode == 4)// Team
			GL11.glColor4d(0, 1, 0, 0.5F);
		RenderManager renderManager =
			Minecraft.getMinecraft().getRenderManager();
		drawSelectionBoundingBox(new AxisAlignedBB(
			entity.boundingBox.minX - 0.05 - entity.posX
				+ (entity.posX - renderManager.renderPosX),
			entity.boundingBox.minY - entity.posY
				+ (entity.posY - renderManager.renderPosY),
			entity.boundingBox.minZ - 0.05 - entity.posZ
				+ (entity.posZ - renderManager.renderPosZ),
			entity.boundingBox.maxX + 0.05 - entity.posX
				+ (entity.posX - renderManager.renderPosX),
			entity.boundingBox.maxY + 0.1 - entity.posY
				+ (entity.posY - renderManager.renderPosY),
			entity.boundingBox.maxZ + 0.05 - entity.posZ
				+ (entity.posZ - renderManager.renderPosZ)));
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL_DEPTH_TEST);
		GL11.glDepthMask(true);
		GL11.glDisable(GL_BLEND);
	}
	
	public static void nukerBox(BlockPos blockPos, float damage)
	{
		double x = blockPos.getX()
			- Minecraft.getMinecraft().getRenderManager().renderPosX;
		double y = blockPos.getY()
			- Minecraft.getMinecraft().getRenderManager().renderPosY;
		double z = blockPos.getZ()
			- Minecraft.getMinecraft().getRenderManager().renderPosZ;
		GL11.glBlendFunc(770, 771);
		GL11.glEnable(GL_BLEND);
		GL11.glLineWidth(1.0F);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL_DEPTH_TEST);
		GL11.glDepthMask(false);
		GL11.glColor4f(damage, 1 - damage, 0, 0.15F);
		drawColorBox(
			new AxisAlignedBB(x + 0.5 - damage / 2, y + 0.5 - damage / 2,
				z + 0.5 - damage / 2, x + 0.5 + damage / 2,
				y + 0.5 + damage / 2, z + 0.5 + damage / 2),
			damage, 1 - damage, 0, 0.15F);
		GL11.glColor4d(0, 0, 0, 0.5F);
		drawSelectionBoundingBox(new AxisAlignedBB(x + 0.5 - damage / 2,
			y + 0.5 - damage / 2, z + 0.5 - damage / 2, x + 0.5 + damage / 2,
			y + 0.5 + damage / 2, z + 0.5 + damage / 2));
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL_DEPTH_TEST);
		GL11.glDepthMask(true);
		GL11.glDisable(GL_BLEND);
	}
	
	public static void searchBox(BlockPos blockPos)
	{
		double x = blockPos.getX()
			- Minecraft.getMinecraft().getRenderManager().renderPosX;
		double y = blockPos.getY()
			- Minecraft.getMinecraft().getRenderManager().renderPosY;
		double z = blockPos.getZ()
			- Minecraft.getMinecraft().getRenderManager().renderPosZ;
		GL11.glBlendFunc(770, 771);
		GL11.glEnable(GL_BLEND);
		GL11.glLineWidth(1.0F);
		float sinus =
			1F - MathHelper.abs(MathHelper.sin(Minecraft.getSystemTime()
				% 10000L / 10000.0F * (float)Math.PI * 4.0F) * 1F);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL_DEPTH_TEST);
		GL11.glDepthMask(false);
		GL11.glColor4f(1F - sinus, sinus, 0F, 0.15F);
		drawColorBox(new AxisAlignedBB(x, y, z, x + 1.0, y + 1.0, z + 1.0),
			1F - sinus, sinus, 0F, 0.15F);
		GL11.glColor4d(0, 0, 0, 0.5);
		drawSelectionBoundingBox(
			new AxisAlignedBB(x, y, z, x + 1.0, y + 1.0, z + 1.0));
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL_DEPTH_TEST);
		GL11.glDepthMask(true);
		GL11.glDisable(GL_BLEND);
	}
	
	public static void drawColorBox(AxisAlignedBB axisalignedbb, float red,
		float green, float blue, float alpha)
	{
		Tessellator ts = Tessellator.getInstance();
		VertexBuffer vb = ts.getBuffer();
		vb.begin(7, DefaultVertexFormats.POSITION_TEX);// Starts X.
		vb.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ)
			.color(red, green, blue, alpha).endVertex();
		vb.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.minZ)
			.color(red, green, blue, alpha).endVertex();
		vb.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.minZ)
			.color(red, green, blue, alpha).endVertex();
		vb.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.minZ)
			.color(red, green, blue, alpha).endVertex();
		vb.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.maxZ)
			.color(red, green, blue, alpha).endVertex();
		vb.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.maxZ)
			.color(red, green, blue, alpha).endVertex();
		vb.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.maxZ)
			.color(red, green, blue, alpha).endVertex();
		vb.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.maxZ)
			.color(red, green, blue, alpha).endVertex();
		ts.draw();
		vb.begin(7, DefaultVertexFormats.POSITION_TEX);
		vb.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.minZ)
			.color(red, green, blue, alpha).endVertex();
		vb.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.minZ)
			.color(red, green, blue, alpha).endVertex();
		vb.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.minZ)
			.color(red, green, blue, alpha).endVertex();
		vb.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ)
			.color(red, green, blue, alpha).endVertex();
		vb.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.maxZ)
			.color(red, green, blue, alpha).endVertex();
		vb.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.maxZ)
			.color(red, green, blue, alpha).endVertex();
		vb.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.maxZ)
			.color(red, green, blue, alpha).endVertex();
		vb.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.maxZ)
			.color(red, green, blue, alpha).endVertex();
		ts.draw();// Ends X.
		vb.begin(7, DefaultVertexFormats.POSITION_TEX);// Starts Y.
		vb.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.minZ)
			.color(red, green, blue, alpha).endVertex();
		vb.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.minZ)
			.color(red, green, blue, alpha).endVertex();
		vb.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.maxZ)
			.color(red, green, blue, alpha).endVertex();
		vb.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.maxZ)
			.color(red, green, blue, alpha).endVertex();
		vb.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.minZ)
			.color(red, green, blue, alpha).endVertex();
		vb.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.maxZ)
			.color(red, green, blue, alpha).endVertex();
		vb.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.maxZ)
			.color(red, green, blue, alpha).endVertex();
		vb.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.minZ)
			.color(red, green, blue, alpha).endVertex();
		ts.draw();
		vb.begin(7, DefaultVertexFormats.POSITION_TEX);
		vb.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ)
			.color(red, green, blue, alpha).endVertex();
		vb.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.minZ)
			.color(red, green, blue, alpha).endVertex();
		vb.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.maxZ)
			.color(red, green, blue, alpha).endVertex();
		vb.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.maxZ)
			.color(red, green, blue, alpha).endVertex();
		vb.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ)
			.color(red, green, blue, alpha).endVertex();
		vb.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.maxZ)
			.color(red, green, blue, alpha).endVertex();
		vb.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.maxZ)
			.color(red, green, blue, alpha).endVertex();
		vb.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.minZ)
			.color(red, green, blue, alpha).endVertex();
		ts.draw();// Ends Y.
		vb.begin(7, DefaultVertexFormats.POSITION_TEX);// Starts Z.
		vb.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ)
			.color(red, green, blue, alpha).endVertex();
		vb.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.minZ)
			.color(red, green, blue, alpha).endVertex();
		vb.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.maxZ)
			.color(red, green, blue, alpha).endVertex();
		vb.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.maxZ)
			.color(red, green, blue, alpha).endVertex();
		vb.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.maxZ)
			.color(red, green, blue, alpha).endVertex();
		vb.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.maxZ)
			.color(red, green, blue, alpha).endVertex();
		vb.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.minZ)
			.color(red, green, blue, alpha).endVertex();
		vb.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.minZ)
			.color(red, green, blue, alpha).endVertex();
		ts.draw();
		vb.begin(7, DefaultVertexFormats.POSITION_TEX);
		vb.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.maxZ)
			.color(red, green, blue, alpha).endVertex();
		vb.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.maxZ)
			.color(red, green, blue, alpha).endVertex();
		vb.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.minZ)
			.color(red, green, blue, alpha).endVertex();
		vb.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ)
			.color(red, green, blue, alpha).endVertex();
		vb.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.minZ)
			.color(red, green, blue, alpha).endVertex();
		vb.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.minZ)
			.color(red, green, blue, alpha).endVertex();
		vb.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.maxZ)
			.color(red, green, blue, alpha).endVertex();
		vb.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.maxZ)
			.color(red, green, blue, alpha).endVertex();
		ts.draw();// Ends Z.
	}
	
	public static void tracerLine(Entity entity, int mode)
	{
		double x = entity.posX
			- Minecraft.getMinecraft().getRenderManager().renderPosX;
		double y = entity.posY + entity.height / 2
			- Minecraft.getMinecraft().getRenderManager().renderPosY;
		double z = entity.posZ
			- Minecraft.getMinecraft().getRenderManager().renderPosZ;
		glBlendFunc(770, 771);
		glEnable(GL_BLEND);
		glLineWidth(2.0F);
		glDisable(GL11.GL_TEXTURE_2D);
		glDisable(GL_DEPTH_TEST);
		glDepthMask(false);
		if(mode == 0)// Enemy
			GL11.glColor4d(
				1 - Minecraft.getMinecraft().player.getDistanceToEntity(entity)
					/ 40,
				Minecraft.getMinecraft().player.getDistanceToEntity(entity)
					/ 40,
				0, 0.5F);
		else if(mode == 1)// Friend
			GL11.glColor4d(0, 0, 1, 0.5F);
		else if(mode == 2)// Other
			GL11.glColor4d(1, 1, 0, 0.5F);
		else if(mode == 3)// Target
			GL11.glColor4d(1, 0, 0, 0.5F);
		else if(mode == 4)// Team
			GL11.glColor4d(0, 1, 0, 0.5F);
		
		Vec3d eyes = new Vec3d(0, 0, 1)
			.rotatePitch(-(float)Math
				.toRadians(Minecraft.getMinecraft().player.rotationPitch))
			.rotateYaw(-(float)Math
				.toRadians(Minecraft.getMinecraft().player.rotationYaw));
		
		glBegin(GL_LINES);
		{
			glVertex3d(eyes.xCoord,
				Minecraft.getMinecraft().player.getEyeHeight() + eyes.yCoord,
				eyes.zCoord);
			glVertex3d(x, y, z);
		}
		glEnd();
		glEnable(GL11.GL_TEXTURE_2D);
		glEnable(GL_DEPTH_TEST);
		glDepthMask(true);
		glDisable(GL_BLEND);
	}
	
	public static void tracerLine(double x, double y, double z, Color color)
	{
		x += 0.5 - Minecraft.getMinecraft().getRenderManager().renderPosX;
		y += 0.5 - Minecraft.getMinecraft().getRenderManager().renderPosY;
		z += 0.5 - Minecraft.getMinecraft().getRenderManager().renderPosZ;
		glBlendFunc(770, 771);
		glEnable(GL_BLEND);
		glLineWidth(2.0F);
		glDisable(GL11.GL_TEXTURE_2D);
		glDisable(GL_DEPTH_TEST);
		glDepthMask(false);
		setColor(color);
		
		Vec3d eyes = new Vec3d(0, 0, 1)
			.rotatePitch(-(float)Math
				.toRadians(Minecraft.getMinecraft().player.rotationPitch))
			.rotateYaw(-(float)Math
				.toRadians(Minecraft.getMinecraft().player.rotationYaw));
		
		glBegin(GL_LINES);
		{
			glVertex3d(eyes.xCoord,
				Minecraft.getMinecraft().player.getEyeHeight() + eyes.yCoord,
				eyes.zCoord);
			glVertex3d(x, y, z);
		}
		glEnd();
		
		glEnable(GL11.GL_TEXTURE_2D);
		glEnable(GL_DEPTH_TEST);
		glDepthMask(true);
		glDisable(GL_BLEND);
	}
	
	public static void drawSelectionBoundingBox(AxisAlignedBB boundingBox)
	{
		Tessellator tessellator = Tessellator.getInstance();
		VertexBuffer vertexbuffer = tessellator.getBuffer();
		vertexbuffer.begin(3, DefaultVertexFormats.POSITION);
		vertexbuffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ)
			.endVertex();
		vertexbuffer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ)
			.endVertex();
		vertexbuffer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ)
			.endVertex();
		vertexbuffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ)
			.endVertex();
		vertexbuffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ)
			.endVertex();
		tessellator.draw();
		vertexbuffer.begin(3, DefaultVertexFormats.POSITION);
		vertexbuffer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ)
			.endVertex();
		vertexbuffer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ)
			.endVertex();
		vertexbuffer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ)
			.endVertex();
		vertexbuffer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ)
			.endVertex();
		vertexbuffer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ)
			.endVertex();
		tessellator.draw();
		vertexbuffer.begin(1, DefaultVertexFormats.POSITION);
		vertexbuffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ)
			.endVertex();
		vertexbuffer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ)
			.endVertex();
		vertexbuffer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ)
			.endVertex();
		vertexbuffer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ)
			.endVertex();
		vertexbuffer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ)
			.endVertex();
		vertexbuffer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ)
			.endVertex();
		vertexbuffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ)
			.endVertex();
		vertexbuffer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ)
			.endVertex();
		tessellator.draw();
	}
	
	public static void scissorBox(int x, int y, int xend, int yend)
	{
		int width = xend - x;
		int height = yend - y;
		ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
		int factor = sr.getScaleFactor();
		int bottomY = Minecraft.getMinecraft().currentScreen.height - yend;
		glScissor(x * factor, bottomY * factor, width * factor,
			height * factor);
	}
	
	public static void setColor(Color c)
	{
		glColor4f(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f,
			c.getAlpha() / 255f);
	}
	
	public static void drawSolidBox()
	{
		drawSolidBox(DEFAULT_AABB);
	}
	
	public static void drawSolidBox(AxisAlignedBB bb)
	{
		glBegin(GL_QUADS);
		{
			glVertex3d(bb.minX, bb.minY, bb.minZ);
			glVertex3d(bb.maxX, bb.minY, bb.minZ);
			glVertex3d(bb.maxX, bb.minY, bb.maxZ);
			glVertex3d(bb.minX, bb.minY, bb.maxZ);
			
			glVertex3d(bb.minX, bb.maxY, bb.minZ);
			glVertex3d(bb.minX, bb.maxY, bb.maxZ);
			glVertex3d(bb.maxX, bb.maxY, bb.maxZ);
			glVertex3d(bb.maxX, bb.maxY, bb.minZ);
			
			glVertex3d(bb.minX, bb.minY, bb.minZ);
			glVertex3d(bb.minX, bb.maxY, bb.minZ);
			glVertex3d(bb.maxX, bb.maxY, bb.minZ);
			glVertex3d(bb.maxX, bb.minY, bb.minZ);
			
			glVertex3d(bb.maxX, bb.minY, bb.minZ);
			glVertex3d(bb.maxX, bb.maxY, bb.minZ);
			glVertex3d(bb.maxX, bb.maxY, bb.maxZ);
			glVertex3d(bb.maxX, bb.minY, bb.maxZ);
			
			glVertex3d(bb.minX, bb.minY, bb.maxZ);
			glVertex3d(bb.maxX, bb.minY, bb.maxZ);
			glVertex3d(bb.maxX, bb.maxY, bb.maxZ);
			glVertex3d(bb.minX, bb.maxY, bb.maxZ);
			
			glVertex3d(bb.minX, bb.minY, bb.minZ);
			glVertex3d(bb.minX, bb.minY, bb.maxZ);
			glVertex3d(bb.minX, bb.maxY, bb.maxZ);
			glVertex3d(bb.minX, bb.maxY, bb.minZ);
		}
		glEnd();
	}
	
	public static void drawOutlinedBox()
	{
		drawOutlinedBox(DEFAULT_AABB);
	}
	
	public static void drawOutlinedBox(AxisAlignedBB bb)
	{
		glBegin(GL_LINES);
		{
			glVertex3d(bb.minX, bb.minY, bb.minZ);
			glVertex3d(bb.maxX, bb.minY, bb.minZ);
			
			glVertex3d(bb.maxX, bb.minY, bb.minZ);
			glVertex3d(bb.maxX, bb.minY, bb.maxZ);
			
			glVertex3d(bb.maxX, bb.minY, bb.maxZ);
			glVertex3d(bb.minX, bb.minY, bb.maxZ);
			
			glVertex3d(bb.minX, bb.minY, bb.maxZ);
			glVertex3d(bb.minX, bb.minY, bb.minZ);
			
			glVertex3d(bb.minX, bb.minY, bb.minZ);
			glVertex3d(bb.minX, bb.maxY, bb.minZ);
			
			glVertex3d(bb.maxX, bb.minY, bb.minZ);
			glVertex3d(bb.maxX, bb.maxY, bb.minZ);
			
			glVertex3d(bb.maxX, bb.minY, bb.maxZ);
			glVertex3d(bb.maxX, bb.maxY, bb.maxZ);
			
			glVertex3d(bb.minX, bb.minY, bb.maxZ);
			glVertex3d(bb.minX, bb.maxY, bb.maxZ);
			
			glVertex3d(bb.minX, bb.maxY, bb.minZ);
			glVertex3d(bb.maxX, bb.maxY, bb.minZ);
			
			glVertex3d(bb.maxX, bb.maxY, bb.minZ);
			glVertex3d(bb.maxX, bb.maxY, bb.maxZ);
			
			glVertex3d(bb.maxX, bb.maxY, bb.maxZ);
			glVertex3d(bb.minX, bb.maxY, bb.maxZ);
			
			glVertex3d(bb.minX, bb.maxY, bb.maxZ);
			glVertex3d(bb.minX, bb.maxY, bb.minZ);
		}
		glEnd();
	}
	
	public static void drawCrossBox()
	{
		drawOutlinedBox(DEFAULT_AABB);
	}
	
	public static void drawCrossBox(AxisAlignedBB bb)
	{
		glBegin(GL_LINES);
		{
			glVertex3d(bb.minX, bb.minY, bb.minZ);
			glVertex3d(bb.maxX, bb.maxY, bb.minZ);
			
			glVertex3d(bb.maxX, bb.minY, bb.minZ);
			glVertex3d(bb.maxX, bb.maxY, bb.maxZ);
			
			glVertex3d(bb.maxX, bb.minY, bb.maxZ);
			glVertex3d(bb.minX, bb.maxY, bb.maxZ);
			
			glVertex3d(bb.minX, bb.minY, bb.maxZ);
			glVertex3d(bb.minX, bb.maxY, bb.minZ);
			
			glVertex3d(bb.maxX, bb.minY, bb.minZ);
			glVertex3d(bb.minX, bb.maxY, bb.minZ);
			
			glVertex3d(bb.maxX, bb.minY, bb.maxZ);
			glVertex3d(bb.maxX, bb.maxY, bb.minZ);
			
			glVertex3d(bb.minX, bb.minY, bb.maxZ);
			glVertex3d(bb.maxX, bb.maxY, bb.maxZ);
			
			glVertex3d(bb.minX, bb.minY, bb.minZ);
			glVertex3d(bb.minX, bb.maxY, bb.maxZ);
			
			glVertex3d(bb.minX, bb.maxY, bb.minZ);
			glVertex3d(bb.maxX, bb.maxY, bb.maxZ);
			
			glVertex3d(bb.maxX, bb.maxY, bb.minZ);
			glVertex3d(bb.minX, bb.maxY, bb.maxZ);
			
			glVertex3d(bb.maxX, bb.minY, bb.minZ);
			glVertex3d(bb.minX, bb.minY, bb.maxZ);
			
			glVertex3d(bb.maxX, bb.minY, bb.maxZ);
			glVertex3d(bb.minX, bb.minY, bb.minZ);
		}
		glEnd();
	}
}
