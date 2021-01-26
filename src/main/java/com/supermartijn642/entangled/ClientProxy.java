package com.supermartijn642.entangled;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.I18n;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.DrawHighlightEvent;
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
        ClientRegistry.bindTileEntityRenderer(Entangled.tile, EntangledBlockTileRenderer::new);
    }

    public static String translate(String key, Object... arguments){
        return I18n.format(key, arguments);
    }

    @Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class Events {
        @SubscribeEvent
        public static void onBlockHighlight(DrawHighlightEvent.HighlightBlock e){
            if(!EntangledConfig.renderBlockHighlight.get())
                return;

            World world = Minecraft.getInstance().world;
            TileEntity tile = world.getTileEntity(e.getTarget().getPos());
            if(tile instanceof EntangledBlockTile && ((EntangledBlockTile)tile).isBound() && ((EntangledBlockTile)tile).getBoundDimension() == world.func_234923_W_()){
                MatrixStack matrixStack = e.getMatrix();
                matrixStack.push();
                Vector3d playerPos = e.getInfo().getProjectedView();
                matrixStack.translate(-playerPos.x, -playerPos.y, -playerPos.z);
                IVertexBuilder builder = e.getBuffers().getBuffer(RenderType.getLines());

                BlockPos pos = ((EntangledBlockTile)tile).getBoundBlockPos();
                VoxelShape shape = world.getBlockState(pos).getRenderShape(world, pos);
                drawShape(e.getMatrix(), builder, shape, pos.getX(), pos.getY(), pos.getZ(), 86 / 255f, 0 / 255f, 156 / 255f, 1);

                matrixStack.pop();
            }
        }

        private static void drawShape(MatrixStack matrixStackIn, IVertexBuilder bufferIn, VoxelShape shapeIn, double xIn, double yIn, double zIn, float red, float green, float blue, float alpha){
            Matrix4f matrix4f = matrixStackIn.getLast().getMatrix();
            shapeIn.forEachEdge((p_230013_12_, p_230013_14_, p_230013_16_, p_230013_18_, p_230013_20_, p_230013_22_) -> {
                bufferIn.pos(matrix4f, (float)(p_230013_12_ + xIn), (float)(p_230013_14_ + yIn), (float)(p_230013_16_ + zIn)).color(red, green, blue, alpha).endVertex();
                bufferIn.pos(matrix4f, (float)(p_230013_18_ + xIn), (float)(p_230013_20_ + yIn), (float)(p_230013_22_ + zIn)).color(red, green, blue, alpha).endVertex();
            });
        }
    }

}
