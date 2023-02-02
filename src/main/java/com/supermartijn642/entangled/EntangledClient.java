package com.supermartijn642.entangled;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.registry.ClientRegistrationHandler;
import com.supermartijn642.core.render.CustomRendererBakedModelWrapper;
import com.supermartijn642.core.render.RenderUtils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(Side.CLIENT)
public class EntangledClient {

    public static void register(){
        ClientRegistrationHandler handler = ClientRegistrationHandler.get("entangled");

        // Entangled block renderer
        handler.registerCustomBlockEntityRenderer(() -> Entangled.tile, EntangledBlockEntityRenderer::new);
        handler.registerCustomItemRenderer(() -> Item.getItemFromBlock(Entangled.block), EntangledBlockItemRenderer::new);
        // Entangled block item model
        handler.registerModelOverwrite("entangled", "block", "inventory", CustomRendererBakedModelWrapper::wrap);
    }

    @Mod.EventBusSubscriber(Side.CLIENT)
    public static class Events {

        @SubscribeEvent
        public static void onDrawPlayerEvent(RenderWorldLastEvent e){
            ItemStack stack = ClientUtils.getPlayer().getHeldItem(EnumHand.MAIN_HAND);
            World world = ClientUtils.getWorld();

            if(stack.getItem() instanceof ItemBlock && ((ItemBlock)stack.getItem()).getBlock() == Entangled.block && stack.hasTagCompound() && stack.getTagCompound().hasKey("tileData")){
                NBTTagCompound compound = stack.getTagCompound().getCompoundTag("tileData");
                if(compound.getBoolean("bound") && compound.getInteger("dimension") == world.provider.getDimensionType().getId()){
                    BlockPos pos = new BlockPos(compound.getInteger("boundx"), compound.getInteger("boundy"), compound.getInteger("boundz"));

                    GlStateManager.pushMatrix();
                    Vec3d camera = RenderUtils.getCameraPosition();
                    GlStateManager.translate(-camera.x, -camera.y, -camera.z);

                    RenderUtils.renderBox(world.getBlockState(pos).getSelectedBoundingBox(world, pos), 86 / 255f, 0 / 255f, 156 / 255f, false);
                    RenderUtils.renderBoxSides(world.getBlockState(pos).getSelectedBoundingBox(world, pos), 86 / 255f, 0 / 255f, 156 / 255f, 30 / 255f, false);

                    GlStateManager.popMatrix();
                }
            }else if(stack.getItem() == Entangled.item && stack.hasTagCompound()){
                NBTTagCompound compound = stack.getTagCompound();
                if(compound.getBoolean("bound") && compound.getInteger("dimension") == world.provider.getDimensionType().getId()){
                    BlockPos pos = new BlockPos(compound.getInteger("boundx"), compound.getInteger("boundy"), compound.getInteger("boundz"));

                    GlStateManager.pushMatrix();
                    Vec3d camera = RenderUtils.getCameraPosition();
                    GlStateManager.translate(-camera.x, -camera.y, -camera.z);

                    RenderUtils.renderBox(world.getBlockState(pos).getSelectedBoundingBox(world, pos), 235 / 255f, 210 / 255f, 52 / 255f, false);
                    RenderUtils.renderBoxSides(world.getBlockState(pos).getSelectedBoundingBox(world, pos), 235 / 255f, 210 / 255f, 52 / 255f, 30 / 255f, false);

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
            if(tile instanceof EntangledBlockEntity && ((EntangledBlockEntity)tile).isBound() && ((EntangledBlockEntity)tile).getBoundDimensionIdentifier() == world.provider.getDimensionType().getId()){
                BlockPos pos = ((EntangledBlockEntity)tile).getBoundBlockPos();

                GlStateManager.pushMatrix();
                Vec3d camera = RenderUtils.getCameraPosition();
                GlStateManager.translate(-camera.x, -camera.y, -camera.z);

                RenderUtils.renderBox(world.getBlockState(pos).getSelectedBoundingBox(world, pos), 86 / 255f, 0 / 255f, 156 / 255f, false);
                RenderUtils.renderBoxSides(world.getBlockState(pos).getSelectedBoundingBox(world, pos), 86 / 255f, 0 / 255f, 156 / 255f, 30 / 255f, false);

                GlStateManager.popMatrix();
            }
        }
    }

}
