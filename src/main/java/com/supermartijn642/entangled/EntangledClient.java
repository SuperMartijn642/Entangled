package com.supermartijn642.entangled;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.render.RenderUtils;
import com.supermartijn642.core.render.RenderWorldEvent;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

import java.util.Map;

/**
 * Created 3/16/2020 by SuperMartijn642
 */
public class EntangledClient implements ClientModInitializer {

    @Override
    public void onInitializeClient(){
        RenderWorldEvent.EVENT.register(EntangledClient::onDrawPlayerEvent);
        WorldRenderEvents.BLOCK_OUTLINE.register(EntangledClient::onBlockHighlight);

        BlockEntityRendererRegistry.register(Entangled.tile, context -> new EntangledBlockTileRenderer());
        BuiltinItemRendererRegistry.INSTANCE.register(Entangled.block, new EntangledBlockItemStackTileEntityRenderer());
    }

    public static void onModelBake(Map<ResourceLocation,BakedModel> modelRegistry){
        // replace the entangled block item model
        ResourceLocation location = new ModelResourceLocation(new ResourceLocation("entangled", "block"), "inventory");
        BakedModel model = modelRegistry.get(location);
        if(model != null)
            modelRegistry.put(location, new EntangledBlockBakedItemModel(model));
    }

    public static void onDrawPlayerEvent(RenderWorldEvent e){
        ItemStack stack = ClientUtils.getPlayer().getItemInHand(InteractionHand.MAIN_HAND);
        Level world = ClientUtils.getWorld();

        if(stack.getItem() instanceof BlockItem && ((BlockItem)stack.getItem()).getBlock() == Entangled.block && stack.hasTag() && stack.getOrCreateTag().contains("tileData")){
            CompoundTag compound = stack.getOrCreateTag().getCompound("tileData");
            if(compound.getBoolean("bound") && compound.getString("dimension").equals(world.dimension().location().toString())){
                BlockPos pos = new BlockPos(compound.getInt("boundx"), compound.getInt("boundy"), compound.getInt("boundz"));

                e.getPoseStack().pushPose();
                Vec3 camera = RenderUtils.getCameraPosition();
                e.getPoseStack().translate(-camera.x, -camera.y, -camera.z);
                e.getPoseStack().translate(pos.getX(), pos.getY(), pos.getZ());

                RenderUtils.disableDepthTest();
                RenderUtils.renderShape(e.getPoseStack(), world.getBlockState(pos).getOcclusionShape(world, pos), 86 / 255f, 0 / 255f, 156 / 255f);
                RenderUtils.renderShapeSides(e.getPoseStack(), world.getBlockState(pos).getOcclusionShape(world, pos), 86 / 255f, 0 / 255f, 156 / 255f, 30 / 255f);
                RenderUtils.resetState();

                e.getPoseStack().popPose();
            }
        }else if(stack.getItem() == Entangled.item){
            CompoundTag compound = stack.getOrCreateTag();
            if(compound.getBoolean("bound") && compound.getString("dimension").equals(world.dimension().location().toString())){
                BlockPos pos = new BlockPos(compound.getInt("boundx"), compound.getInt("boundy"), compound.getInt("boundz"));

                e.getPoseStack().pushPose();
                Vec3 camera = RenderUtils.getCameraPosition();
                e.getPoseStack().translate(-camera.x, -camera.y, -camera.z);
                e.getPoseStack().translate(pos.getX(), pos.getY(), pos.getZ());

                RenderUtils.disableDepthTest();
                RenderUtils.renderShape(e.getPoseStack(), world.getBlockState(pos).getOcclusionShape(world, pos), 235 / 255f, 210 / 255f, 52 / 255f);
                RenderUtils.renderShapeSides(e.getPoseStack(), world.getBlockState(pos).getOcclusionShape(world, pos), 235 / 255f, 210 / 255f, 52 / 255f, 30 / 255f);
                RenderUtils.resetState();

                e.getPoseStack().popPose();
            }
        }
    }

    public static boolean onBlockHighlight(WorldRenderContext renderContext, WorldRenderContext.BlockOutlineContext blockOutlineContext){
        if(blockOutlineContext.blockPos() == null || !EntangledConfig.renderBlockHighlight.get())
            return true;

        Level world = Minecraft.getInstance().level;
        BlockEntity tile = world.getBlockEntity(blockOutlineContext.blockPos());
        if(tile instanceof EntangledBlockTile && ((EntangledBlockTile)tile).isBound() && ((EntangledBlockTile)tile).getBoundDimension() == world.dimension()){
            BlockPos pos = ((EntangledBlockTile)tile).getBoundBlockPos();

            renderContext.matrixStack().pushPose();
            Vec3 camera = RenderUtils.getCameraPosition();
            renderContext.matrixStack().translate(-camera.x, -camera.y, -camera.z);
            renderContext.matrixStack().translate(pos.getX(), pos.getY(), pos.getZ());

            RenderUtils.disableDepthTest();
            RenderUtils.renderShape(renderContext.matrixStack(), world.getBlockState(pos).getOcclusionShape(world, pos), 86 / 255f, 0 / 255f, 156 / 255f);
            RenderUtils.renderShapeSides(renderContext.matrixStack(), world.getBlockState(pos).getOcclusionShape(world, pos), 86 / 255f, 0 / 255f, 156 / 255f, 30 / 255f);
            RenderUtils.resetState();

            renderContext.matrixStack().popPose();
        }

        return true;
    }
}
