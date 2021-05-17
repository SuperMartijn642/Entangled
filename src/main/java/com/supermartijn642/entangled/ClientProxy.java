package com.supermartijn642.entangled;

import com.mojang.blaze3d.platform.GlStateManager;
import com.supermartijn642.core.ClientUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
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

    @SubscribeEvent
    public static void onModelBake(ModelBakeEvent e){
        // replace the entangled block item model
        ResourceLocation location = new ModelResourceLocation(new ResourceLocation("entangled", "block"), "inventory");
        IBakedModel model = e.getModelRegistry().get(location);
        if(model != null)
            e.getModelRegistry().put(location, new EntangledBlockBakedItemModel(model));
    }

    @Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class Events {

        @SubscribeEvent
        public static void onDrawPlayerEvent(RenderWorldLastEvent e){
            ItemStack stack = ClientUtils.getPlayer().getHeldItem(Hand.MAIN_HAND);
            if(stack.getItem() instanceof BlockItem && ((BlockItem)stack.getItem()).getBlock() == Entangled.block && stack.hasTag() && stack.getOrCreateTag().contains("tileData")){
                CompoundNBT compound = stack.getOrCreateTag().getCompound("tileData");
                World world = ClientUtils.getMinecraft().world;
                if(compound.getBoolean("bound") && compound.getInt("dimension") == world.getDimension().getType().getId()){
                    BlockPos pos = new BlockPos(compound.getInt("boundx"), compound.getInt("boundy"), compound.getInt("boundz"));
                    renderHighlight(ClientUtils.getMinecraft().getRenderManager().info, world, pos, 86 / 255f, 0 / 255f, 156 / 255f);
                }
            }else if(stack.getItem() == Entangled.item){
                CompoundNBT compound = stack.getOrCreateTag();
                World world = ClientUtils.getMinecraft().world;
                if(compound.getBoolean("bound") && compound.getInt("dimension") == world.getDimension().getType().getId()){
                    BlockPos pos = new BlockPos(compound.getInt("boundx"), compound.getInt("boundy"), compound.getInt("boundz"));
                    renderHighlight(ClientUtils.getMinecraft().getRenderManager().info, world, pos, 235 / 255f, 210 / 255f, 52 / 255f);
                }
            }
        }

        @SubscribeEvent
        public static void onBlockHighlight(DrawBlockHighlightEvent.HighlightBlock e){
            if(!EntangledConfig.renderBlockHighlight.get())
                return;

            World world = Minecraft.getInstance().world;
            TileEntity tile = world.getTileEntity(e.getTarget().getPos());
            if(tile instanceof EntangledBlockTile && ((EntangledBlockTile)tile).isBound() && ((EntangledBlockTile)tile).getBoundDimension() == world.getDimension().getType().getId())
                renderHighlight(e.getInfo(), world, ((EntangledBlockTile)tile).getBoundBlockPos(), 86 / 255f, 0 / 255f, 156 / 255f);
        }

        private static void renderHighlight(ActiveRenderInfo info, World world, BlockPos pos, float red, float green, float blue){
            GlStateManager.pushMatrix();
            GlStateManager.disableTexture();
            GlStateManager.disableLighting();
            GlStateManager.disableBlend();
            GlStateManager.disableDepthTest();
            Vec3d playerPos = info.getProjectedView();
            GlStateManager.translated(-playerPos.x, -playerPos.y, -playerPos.z);

            VoxelShape shape = world.getBlockState(pos).getRenderShape(world, pos);
            drawShape(shape, pos.getX(), pos.getY(), pos.getZ(), red, green, blue, 1);

            GlStateManager.popMatrix();
            GlStateManager.enableTexture();
            GlStateManager.enableDepthTest();
        }

        private static void drawShape(VoxelShape shapeIn, double xIn, double yIn, double zIn, float red, float green, float blue, float alpha){
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuffer();
            bufferbuilder.begin(1, DefaultVertexFormats.POSITION_COLOR);
            shapeIn.forEachEdge((x1, y1, z1, x2, y2, z2) -> {
                bufferbuilder.pos(x1 + xIn, y1 + yIn, z1 + zIn).color(red, green, blue, alpha).endVertex();
                bufferbuilder.pos(x2 + xIn, y2 + yIn, z2 + zIn).color(red, green, blue, alpha).endVertex();
            });
            tessellator.draw();
        }
    }

}
