package com.supermartijn642.entangled;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.DrawHighlightEvent;
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
        ClientRegistry.bindTileEntityRenderer(Entangled.tile, EntangledBlockTileRenderer::new);
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
            World world = ClientUtils.getWorld();

            if(stack.getItem() instanceof BlockItem && ((BlockItem)stack.getItem()).getBlock() == Entangled.block && stack.hasTag() && stack.getOrCreateTag().contains("tileData")){
                CompoundNBT compound = stack.getOrCreateTag().getCompound("tileData");
                if(compound.getBoolean("bound") && compound.getString("dimension").equals(world.getDimensionKey().getLocation().toString())){
                    BlockPos pos = new BlockPos(compound.getInt("boundx"), compound.getInt("boundy"), compound.getInt("boundz"));
                    RenderUtils.disableDepthTest();
                    RenderUtils.renderShape(e.getMatrixStack(), world.getBlockState(pos).getRenderShape(world, pos), pos, 86 / 255f, 0 / 255f, 156 / 255f);
                    RenderUtils.enableDepthTest();
                }
            }else if(stack.getItem() == Entangled.item){
                CompoundNBT compound = stack.getOrCreateTag();
                if(compound.getBoolean("bound") && compound.getString("dimension").equals(world.getDimensionKey().getLocation().toString())){
                    BlockPos pos = new BlockPos(compound.getInt("boundx"), compound.getInt("boundy"), compound.getInt("boundz"));
                    RenderUtils.disableDepthTest();
                    RenderUtils.renderShape(e.getMatrixStack(), world.getBlockState(pos).getRenderShape(world, pos), pos, 235 / 255f, 210 / 255f, 52 / 255f);
                    RenderUtils.enableDepthTest();
                }
            }
        }

        @SubscribeEvent
        public static void onBlockHighlight(DrawHighlightEvent.HighlightBlock e){
            if(e.getTarget().getType() != RayTraceResult.Type.BLOCK || e.getTarget().getPos() == null || !EntangledConfig.renderBlockHighlight.get())
                return;

            World world = Minecraft.getInstance().world;
            TileEntity tile = world.getTileEntity(e.getTarget().getPos());
            if(tile instanceof EntangledBlockTile && ((EntangledBlockTile)tile).isBound() && ((EntangledBlockTile)tile).getBoundDimension() == world.getDimensionKey()){
                BlockPos pos = ((EntangledBlockTile)tile).getBoundBlockPos();
                RenderUtils.disableDepthTest();
                RenderUtils.renderShape(e.getMatrix(), world.getBlockState(pos).getRenderShape(world, pos), pos, 86 / 255f, 0 / 255f, 156 / 255f);
                RenderUtils.enableDepthTest();
            }
        }
    }

}
