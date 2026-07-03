package com.supermartijn642.entangled;

import com.mojang.blaze3d.vertex.PoseStack;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.block.BaseBlock;
import com.supermartijn642.core.block.BlockShape;
import com.supermartijn642.core.registry.ClientRegistrationHandler;
import com.supermartijn642.core.render.RenderUtils;
import com.supermartijn642.core.render.RenderWorldEvent;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.state.level.BlockOutlineRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHighlightEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Created 3/16/2020 by SuperMartijn642
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class EntangledClient {

    private static final PoseStack POSE_STACK = new PoseStack();

    public static void register(){
        ClientRegistrationHandler handler = ClientRegistrationHandler.get("entangled");

        // Entangled block renderer
        handler.registerCustomBlockEntityRenderer(() -> Entangled.tile, EntangledBlockEntityRenderer::new);
        // Entangled block item model
        handler.registerItemModelType("block", EntangledBlockItemModel.CODEC);
    }

    @SubscribeEvent
    public static void onDrawPlayerEvent(RenderWorldEvent e){
        ItemStack stack = ClientUtils.getPlayer().getItemInHand(InteractionHand.MAIN_HAND);
        Level world = ClientUtils.getWorld();

        if(stack.getItem() instanceof BlockItem && ((BlockItem)stack.getItem()).getBlock() == Entangled.block && stack.get(BaseBlock.TILE_DATA) != null){
            CompoundTag compound = stack.get(BaseBlock.TILE_DATA);
            if(compound.getBooleanOr("bound", false) && compound.getStringOr("dimension", "").equals(world.dimension().identifier().toString())){
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
            if(target != null && target.dimension().equals(world.dimension().identifier())){
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

    @SubscribeEvent
    private static void onBlockHighlightExtract(RenderHighlightEvent.Block event){
        if(!EntangledConfig.renderBlockHighlight.get())
            return;

        BlockPos pos = event.getTarget().getBlockPos();
        Level level = ClientUtils.getWorld();
        BlockEntity entity = level.getBlockEntity(pos);
        if(entity instanceof EntangledBlockEntity && ((EntangledBlockEntity)entity).isBound() && ((EntangledBlockEntity)entity).getBoundDimensionIdentifier() == level.dimension()){
            BlockPos boundPos = ((EntangledBlockEntity)entity).getBoundBlockPos();
            VoxelShape shape = level.getBlockState(boundPos).getOcclusionShape();
            if(!shape.isEmpty()){
                BlockHighlightState state = new BlockHighlightState();
                state.shouldRender = true;
                state.pos = boundPos;
                state.shape = BlockShape.create(shape);
                BlockState blockState = level.getBlockState(pos);
                //noinspection deprecation
                BlockOutlineRenderState outlineRenderState = new BlockOutlineRenderState(
                    pos,
                    ClientUtils.getMinecraft().getModelManager().getBlockStateModelSet().get(blockState).hasMaterialFlag(BakedQuad.FLAG_TRANSLUCENT),
                    ClientUtils.getMinecraft().options.highContrastBlockOutline().get(),
                    blockState.getShape(level, pos, CollisionContext.of(event.getCamera().entity()))
                );
                LevelRenderer levelRenderer = event.getLevelRenderer();
                event.setCustomRenderer((source, stack, translucent, levelRenderState) -> onBlockHighlightDraw(outlineRenderState, source, stack, translucent, levelRenderState, levelRenderer, state));
            }
        }
    }

    private static boolean onBlockHighlightDraw(BlockOutlineRenderState outlineRenderState, MultiBufferSource.BufferSource bufferSource, PoseStack poseStack, boolean translucentPass, LevelRenderState levelRenderState, LevelRenderer levelRenderer, BlockHighlightState state){
        if(state == null || !state.shouldRender)
            return true;

        POSE_STACK.pushPose();
        Vec3 playerPos = levelRenderState.cameraRenderState.pos;
        POSE_STACK.translate(-playerPos.x, -playerPos.y, -playerPos.z);
        POSE_STACK.translate(state.pos.getX(), state.pos.getY(), state.pos.getZ());

        RenderUtils.renderShape(POSE_STACK, state.shape, 86 / 255f, 0 / 255f, 156 / 255f, false);
        RenderUtils.renderShapeSides(POSE_STACK, state.shape, 86 / 255f, 0 / 255f, 156 / 255f, 30 / 255f, false);

        POSE_STACK.popPose();

        // Render original outline
        BlockOutlineRenderState temp = levelRenderState.blockOutlineRenderState;
        levelRenderState.blockOutlineRenderState = outlineRenderState;
        levelRenderer.renderBlockOutline(bufferSource, poseStack, translucentPass, levelRenderState);
        levelRenderState.blockOutlineRenderState = temp;
        return true;
    }

    private static class BlockHighlightState {
        boolean shouldRender;
        BlockPos pos;
        BlockShape shape;
    }
}
