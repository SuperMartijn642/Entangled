package com.supermartijn642.entangled;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * Created 3/16/2020 by SuperMartijn642
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientProxy {

    @SubscribeEvent
    public static void setup(FMLClientSetupEvent e){
        ClientRegistry.bindTileEntitySpecialRenderer(EntangledBlockTile.class, new EntangledBlockTileRenderer());
    }

    public static String translate(String key, Object... arguments){
        return I18n.format(key, arguments);
    }

    @Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class Events {
        @SubscribeEvent
        public static void onBlockHighlight(DrawBlockHighlightEvent.HighlightBlock e){
            if(!EntangledConfig.renderBlockHighlight.get())
                return;

            World world = Minecraft.getInstance().world;
            TileEntity tile = world.getTileEntity(e.getTarget().getPos());
            if(tile instanceof EntangledBlockTile && ((EntangledBlockTile)tile).isBound() && ((EntangledBlockTile)tile).getBoundDimension() == world.getDimension().getType().getId()){
                GlStateManager.pushMatrix();
                GlStateManager.disableTexture();
                GlStateManager.disableLighting();
                GlStateManager.disableBlend();
                Vec3d playerPos = e.getInfo().getProjectedView();
                GlStateManager.translated(-playerPos.x, -playerPos.y, -playerPos.z);

                BlockPos pos = ((EntangledBlockTile)tile).getBoundBlockPos();
                VoxelShape shape = world.getBlockState(pos).getRenderShape(world, pos);
                drawShape(shape, pos.getX(), pos.getY(), pos.getZ(), 86 / 255f, 0 / 255f, 156 / 255f, 1);


                GlStateManager.popMatrix();
            }
        }

        private static void drawShape(VoxelShape shapeIn, double xIn, double yIn, double zIn, float red, float green, float blue, float alpha){
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuffer();
            bufferbuilder.begin(1, DefaultVertexFormats.POSITION_COLOR);
            shapeIn.forEachEdge((p_195468_11_, p_195468_13_, p_195468_15_, p_195468_17_, p_195468_19_, p_195468_21_) -> {
                bufferbuilder.pos(p_195468_11_ + xIn, p_195468_13_ + yIn, p_195468_15_ + zIn).color(red, green, blue, alpha).endVertex();
                bufferbuilder.pos(p_195468_17_ + xIn, p_195468_19_ + yIn, p_195468_21_ + zIn).color(red, green, blue, alpha).endVertex();
            });
            tessellator.draw();
        }
    }

}
