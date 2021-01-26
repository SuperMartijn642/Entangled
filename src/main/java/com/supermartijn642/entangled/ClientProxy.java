package com.supermartijn642.entangled;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientProxy {

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> e){
        ClientRegistry.bindTileEntitySpecialRenderer(EntangledBlockTile.class, new EntangledBlockTileRenderer());
    }

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent e){
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(Entangled.block), 0, new ModelResourceLocation(Entangled.block.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(Entangled.item, 0, new ModelResourceLocation(Entangled.item.getRegistryName(), "inventory"));
    }

    public static String translate(String translationKey, Object... arguments){
        return I18n.format(translationKey, arguments);
    }

    @Mod.EventBusSubscriber(Side.CLIENT)
    public static class Events {
        @SubscribeEvent
        public static void onBlockHighlight(DrawBlockHighlightEvent e){
            if(!EntangledConfig.renderBlockHighlight.get())
                return;

            World world = Minecraft.getMinecraft().world;
            BlockPos generatorPos = e.getTarget().getBlockPos();
            // apparent this can be null
            if(generatorPos != null){
                TileEntity tile = world.getTileEntity(e.getTarget().getBlockPos());
                if(tile instanceof EntangledBlockTile && ((EntangledBlockTile)tile).isBound()){
                    GlStateManager.pushMatrix();
                    GlStateManager.enableBlend();
                    GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                    GlStateManager.glLineWidth(2.0F);
                    GlStateManager.disableTexture2D();
                    GlStateManager.depthMask(false);
                    EntityPlayer player = e.getPlayer();
                    float partialTicks = e.getPartialTicks();
                    double d3 = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double)partialTicks;
                    double d4 = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double)partialTicks;
                    double d5 = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double)partialTicks;
                    GlStateManager.translate(-d3, -d4, -d5);

                    BlockPos pos = ((EntangledBlockTile)tile).getBoundBlockPos();
                    AxisAlignedBB shape = world.getBlockState(pos).getSelectedBoundingBox(world, pos);
                    drawShape(shape, pos.getX(), pos.getY(), pos.getZ(), 86 / 255f, 0 / 255f, 156 / 255f, 1);

                    GlStateManager.depthMask(true);
                    GlStateManager.enableTexture2D();
                    GlStateManager.disableBlend();
                    GlStateManager.popMatrix();
                }
            }
        }

        private static void drawShape(AxisAlignedBB shapeIn, double xIn, double yIn, double zIn, float red, float green, float blue, float alpha){
            RenderGlobal.drawSelectionBoundingBox(shapeIn, red, green, blue, alpha);
        }
    }

}
