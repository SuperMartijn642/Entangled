package com.supermartijn642.entangled;

import com.mojang.blaze3d.vertex.PoseStack;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.block.BaseBlock;
import com.supermartijn642.core.block.BlockShape;
import com.supermartijn642.core.registry.ClientRegistrationHandler;
import com.supermartijn642.core.render.RenderUtils;
import com.supermartijn642.core.render.RenderWorldEvent;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldExtractionContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.state.BlockOutlineRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Created 3/16/2020 by SuperMartijn642
 */
public class EntangledClient implements ClientModInitializer {

    private static final RenderStateDataKey<BlockHighlightState> BLOCK_HIGHLIGHT_DATA = RenderStateDataKey.create(() -> "entangled:bound_block_highlight");
    private static final PoseStack POSE_STACK = new PoseStack();

    @Override
    public void onInitializeClient(){
        RenderWorldEvent.EVENT.register(EntangledClient::onDrawPlayerEvent);
        WorldRenderEvents.AFTER_BLOCK_OUTLINE_EXTRACTION.register(EntangledClient::onBlockHighlightExtract);
        WorldRenderEvents.BEFORE_BLOCK_OUTLINE.register(EntangledClient::onBlockHighlightDraw);

        register();
    }

    public static void register(){
        ClientRegistrationHandler handler = ClientRegistrationHandler.get("entangled");

        // Entangled block renderer
        handler.registerCustomBlockEntityRenderer(() -> Entangled.tile, EntangledBlockEntityRenderer::new);
        // Entangled block item model
        handler.registerItemModelType("block", EntangledBlockItemModel.CODEC);
        // Entangled block render type
        handler.registerBlockModelCutoutRenderType(() -> Entangled.block);
    }

    public static void onDrawPlayerEvent(RenderWorldEvent e){
        ItemStack stack = ClientUtils.getPlayer().getItemInHand(InteractionHand.MAIN_HAND);
        Level world = ClientUtils.getWorld();

        if(stack.getItem() instanceof BlockItem && ((BlockItem)stack.getItem()).getBlock() == Entangled.block && stack.get(BaseBlock.TILE_DATA) != null){
            CompoundTag compound = stack.get(BaseBlock.TILE_DATA);
            if(compound.getBooleanOr("bound", false) && compound.getStringOr("dimension", "").equals(world.dimension().location().toString())){
                BlockPos pos = new BlockPos(compound.getIntOr("boundx", 0), compound.getIntOr("boundy", 0), compound.getIntOr("boundz", 0));

                e.getPoseStack().pushPose();
                Vec3 camera = RenderUtils.getCameraPosition();
                e.getPoseStack().translate(-camera.x, -camera.y, -camera.z);
                e.getPoseStack().translate(pos.getX(), pos.getY(), pos.getZ());

                RenderUtils.renderShape(e.getPoseStack(), world.getBlockState(pos).getOcclusionShape(), 86 / 255f, 0 / 255f, 156 / 255f, false);
                RenderUtils.renderShapeSides(e.getPoseStack(), world.getBlockState(pos).getOcclusionShape(), 86 / 255f, 0 / 255f, 156 / 255f, 30 / 255f, false);

                e.getPoseStack().popPose();
            }
        }else if(stack.getItem() == Entangled.item){
            EntangledBinderItem.BinderTarget target = stack.get(EntangledBinderItem.BINDER_TARGET);
            if(target != null && target.dimension().equals(world.dimension().location())){
                BlockPos pos = target.pos();

                e.getPoseStack().pushPose();
                Vec3 camera = RenderUtils.getCameraPosition();
                e.getPoseStack().translate(-camera.x, -camera.y, -camera.z);
                e.getPoseStack().translate(pos.getX(), pos.getY(), pos.getZ());

                RenderUtils.renderShape(e.getPoseStack(), world.getBlockState(pos).getOcclusionShape(), 235 / 255f, 210 / 255f, 52 / 255f, false);
                RenderUtils.renderShapeSides(e.getPoseStack(), world.getBlockState(pos).getOcclusionShape(), 235 / 255f, 210 / 255f, 52 / 255f, 30 / 255f, false);

                e.getPoseStack().popPose();
            }
        }
    }

    private static void onBlockHighlightExtract(WorldExtractionContext context, HitResult result){
        BlockHighlightState state = context.worldState().getData(BLOCK_HIGHLIGHT_DATA);
        if(state == null){
            state = new BlockHighlightState();
            context.worldState().setData(BLOCK_HIGHLIGHT_DATA, state);
        }
        state.shouldRender = false;
        if(!EntangledConfig.renderBlockHighlight.get())
            return;

        if(result instanceof BlockHitResult){
            BlockPos pos = ((BlockHitResult)result).getBlockPos();
            //noinspection resource
            ClientLevel level = context.world();
            BlockEntity entity = level.getBlockEntity(pos);
            if(entity instanceof EntangledBlockEntity && ((EntangledBlockEntity)entity).isBound() && ((EntangledBlockEntity)entity).getBoundDimensionIdentifier() == level.dimension()){
                BlockPos boundPos = ((EntangledBlockEntity)entity).getBoundBlockPos();
                VoxelShape shape = level.getBlockState(boundPos).getOcclusionShape();
                if(!shape.isEmpty()){
                    state.shouldRender = true;
                    state.pos = boundPos;
                    state.shape = BlockShape.create(shape);
                }
            }
        }
    }

    private static boolean onBlockHighlightDraw(WorldRenderContext context, BlockOutlineRenderState outlineRenderState){
        BlockHighlightState state = context.worldState().getData(BLOCK_HIGHLIGHT_DATA);
        if(state == null || !state.shouldRender)
            return true;

        POSE_STACK.pushPose();
        Vec3 playerPos = context.worldState().cameraRenderState.pos;
        POSE_STACK.translate(-playerPos.x, -playerPos.y, -playerPos.z);
        POSE_STACK.translate(state.pos.getX(), state.pos.getY(), state.pos.getZ());

        RenderUtils.renderShape(POSE_STACK, state.shape, 86 / 255f, 0 / 255f, 156 / 255f, false);
        RenderUtils.renderShapeSides(POSE_STACK, state.shape, 86 / 255f, 0 / 255f, 156 / 255f, 30 / 255f, false);

        POSE_STACK.popPose();
        return true;
    }

    private static class BlockHighlightState {
        boolean shouldRender;
        BlockPos pos;
        BlockShape shape;
    }
}
