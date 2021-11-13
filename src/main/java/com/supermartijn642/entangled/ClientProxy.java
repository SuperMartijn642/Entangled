package com.supermartijn642.entangled;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.render.RenderUtils;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientProxy {

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> e){
        ClientRegistry.bindTileEntitySpecialRenderer(EntangledBlockTile.class, new EntangledBlockTileRenderer());
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void registerItems(RegistryEvent.Register<Item> e){
        Item.getItemFromBlock(Entangled.block).setTileEntityItemStackRenderer(new EntangledBlockItemStackTileEntityRenderer());
    }

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent e){
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(Entangled.block), 0, new ModelResourceLocation(Entangled.block.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(Entangled.item, 0, new ModelResourceLocation(Entangled.item.getRegistryName(), "inventory"));
    }

    @SubscribeEvent
    public static void onModelBake(ModelBakeEvent e){
        // replace the entangled block item model
        ModelResourceLocation location = new ModelResourceLocation(new ResourceLocation("entangled", "block"), "inventory");
        IBakedModel model = e.getModelRegistry().getObject(location);
        if(model != null)
            e.getModelRegistry().putObject(location, new EntangledBlockBakedItemModel(model));
    }

    @Mod.EventBusSubscriber(Side.CLIENT)
    public static class Events {

        @SubscribeEvent
        public static void onDrawPlayerEvent(RenderWorldLastEvent e){
            ItemStack stack = ClientUtils.getPlayer().getHeldItem(EnumHand.MAIN_HAND);
            World world = ClientUtils.getMinecraft().world;

            if(stack.getItem() instanceof ItemBlock && ((ItemBlock)stack.getItem()).getBlock() == Entangled.block && stack.hasTagCompound() && stack.getTagCompound().hasKey("tileData")){
                NBTTagCompound compound = stack.getTagCompound().getCompoundTag("tileData");
                if(compound.getBoolean("bound") && compound.getInteger("dimension") == world.provider.getDimensionType().getId()){
                    BlockPos pos = new BlockPos(compound.getInteger("boundx"), compound.getInteger("boundy"), compound.getInteger("boundz"));

                    GlStateManager.pushMatrix();
                    Vec3d camera = RenderUtils.getCameraPosition();
                    GlStateManager.translate(-camera.x, -camera.y, -camera.z);

                    RenderUtils.disableDepthTest();
                    RenderUtils.renderBox(world.getBlockState(pos).getSelectedBoundingBox(world, pos), 86 / 255f, 0 / 255f, 156 / 255f);
                    RenderUtils.renderBoxSides(world.getBlockState(pos).getSelectedBoundingBox(world, pos), 86 / 255f, 0 / 255f, 156 / 255f, 30 / 255f);
                    RenderUtils.resetState();

                    GlStateManager.popMatrix();
                }
            }else if(stack.getItem() == Entangled.item && stack.hasTagCompound()){
                NBTTagCompound compound = stack.getTagCompound();
                if(compound.getBoolean("bound") && compound.getInteger("dimension") == world.provider.getDimensionType().getId()){
                    BlockPos pos = new BlockPos(compound.getInteger("boundx"), compound.getInteger("boundy"), compound.getInteger("boundz"));

                    GlStateManager.pushMatrix();
                    Vec3d camera = RenderUtils.getCameraPosition();
                    GlStateManager.translate(-camera.x, -camera.y, -camera.z);

                    RenderUtils.disableDepthTest();
                    RenderUtils.renderBox(world.getBlockState(pos).getSelectedBoundingBox(world, pos), 235 / 255f, 210 / 255f, 52 / 255f);
                    RenderUtils.renderBoxSides(world.getBlockState(pos).getSelectedBoundingBox(world, pos), 235 / 255f, 210 / 255f, 52 / 255f, 30 / 255f);
                    RenderUtils.resetState();

                    GlStateManager.popMatrix();
                }
            }
        }

        @SubscribeEvent
        public static void onBlockHighlight(DrawBlockHighlightEvent e){
            if(e.getTarget().typeOfHit != RayTraceResult.Type.BLOCK || e.getTarget().getBlockPos() == null || !EntangledConfig.renderBlockHighlight.get())
                return;

            World world = ClientUtils.getMinecraft().world;
            TileEntity tile = world.getTileEntity(e.getTarget().getBlockPos());
            if(tile instanceof EntangledBlockTile && ((EntangledBlockTile)tile).isBound() && ((EntangledBlockTile)tile).getBoundDimension() == world.provider.getDimensionType().getId()){
                BlockPos pos = ((EntangledBlockTile)tile).getBoundBlockPos();

                GlStateManager.pushMatrix();
                Vec3d camera = RenderUtils.getCameraPosition();
                GlStateManager.translate(-camera.x, -camera.y, -camera.z);

                RenderUtils.disableDepthTest();
                RenderUtils.renderBox(world.getBlockState(pos).getSelectedBoundingBox(world, pos), 86 / 255f, 0 / 255f, 156 / 255f);
                RenderUtils.renderBoxSides(world.getBlockState(pos).getSelectedBoundingBox(world, pos), 86 / 255f, 0 / 255f, 156 / 255f, 30 / 255f);
                RenderUtils.resetState();

                GlStateManager.popMatrix();
            }
        }
    }

}
