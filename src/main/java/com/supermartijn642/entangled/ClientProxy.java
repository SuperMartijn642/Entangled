package com.supermartijn642.entangled;

import com.supermartijn642.core.ClientUtils;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
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

    public static String translate(String translationKey, Object... arguments){
        return I18n.format(translationKey, arguments);
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
            if(stack.getItem() instanceof ItemBlock && ((ItemBlock)stack.getItem()).getBlock() == Entangled.block && stack.hasTagCompound() && stack.getTagCompound().hasKey("tileData")){
                NBTTagCompound compound = stack.getTagCompound().getCompoundTag("tileData");
                World world = ClientUtils.getMinecraft().world;
                if(compound.getBoolean("bound") && compound.getInteger("dimension") == world.provider.getDimensionType().getId()){
                    BlockPos pos = new BlockPos(compound.getInteger("boundx"), compound.getInteger("boundy"), compound.getInteger("boundz"));
                    renderHighlight(world, pos, 86 / 255f, 0 / 255f, 156 / 255f);
                }
            }else if(stack.getItem() == Entangled.item && stack.hasTagCompound()){
                NBTTagCompound compound = stack.getTagCompound();
                World world = ClientUtils.getMinecraft().world;
                if(compound.getBoolean("bound") && compound.getInteger("dimension") == world.provider.getDimensionType().getId()){
                    BlockPos pos = new BlockPos(compound.getInteger("boundx"), compound.getInteger("boundy"), compound.getInteger("boundz"));
                    renderHighlight(world, pos, 235 / 255f, 210 / 255f, 52 / 255f);
                }
            }
        }

        @SubscribeEvent
        public static void onBlockHighlight(DrawBlockHighlightEvent e){
            // RayTraceResult#getBlockPos() can definitely be null
            if(!EntangledConfig.renderBlockHighlight.get() || e.getTarget().getBlockPos() == null)
                return;

            World world = ClientUtils.getMinecraft().world;
            TileEntity tile = world.getTileEntity(e.getTarget().getBlockPos());
            if(tile instanceof EntangledBlockTile && ((EntangledBlockTile)tile).isBound() && ((EntangledBlockTile)tile).getBoundDimension() == world.provider.getDimensionType().getId())
                renderHighlight(world, ((EntangledBlockTile)tile).getBoundBlockPos(), 86 / 255f, 0 / 255f, 156 / 255f);
        }

        private static void renderHighlight(World world, BlockPos pos, float red, float green, float blue){
            GlStateManager.pushMatrix();
            GlStateManager.disableTexture2D();
            GlStateManager.disableLighting();
            GlStateManager.disableBlend();
            GlStateManager.disableDepth();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

            RenderManager manager = ClientUtils.getMinecraft().getRenderManager();
            GlStateManager.translate(-manager.viewerPosX, -manager.viewerPosY, -manager.viewerPosZ);

            AxisAlignedBB shape = world.getBlockState(pos).getSelectedBoundingBox(world, pos);
            drawShape(shape, red, green, blue, 1);

            GlStateManager.popMatrix();
            GlStateManager.enableTexture2D();
            GlStateManager.enableDepth();
        }

        private static void drawShape(AxisAlignedBB shapeIn, float red, float green, float blue, float alpha){
            RenderGlobal.drawSelectionBoundingBox(shapeIn, red, green, blue, alpha);
        }
    }

}
