package com.supermartijn642.entangled;

import com.mojang.blaze3d.vertex.PoseStack;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.registry.Registries;
import com.supermartijn642.core.render.CustomBlockEntityRenderer;
import it.unimi.dsi.fastutil.ints.IntList;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created 3/16/2020 by SuperMartijn642
 */
public class EntangledBlockEntityRenderer implements CustomBlockEntityRenderer<EntangledBlockEntity,EntangledBlockEntityRenderer.State> {

    private static final AABB FULL_BLOCK_BOUNDS = new AABB(0, 0, 0, 1, 1, 1);
    private static final Matrix4fc IDENTITY_MATRIX = new Matrix4f().identity();

    public static final TagKey<Block> BLACKLISTED_BLOCKS = TagKey.create(net.minecraft.core.registries.Registries.BLOCK, Identifier.fromNamespaceAndPath("entangled", "render_blacklist"));
    public static final TagKey<BlockEntityType<?>> BLACKLISTED_ENTITIES = TagKey.create(net.minecraft.core.registries.Registries.BLOCK_ENTITY_TYPE, Identifier.fromNamespaceAndPath("entangled", "render_blacklist"));
    private static final Set<BlockEntityType<?>> ERRORED_BLOCK_ENTITIES = Collections.synchronizedSet(new HashSet<>());
    private static final Set<BlockState> ERRORED_BLOCK_STATES = Collections.synchronizedSet(new HashSet<>());

    private static int depth = 0;

    @Override
    public State createStateHolder(){
        return new State();
    }

    @Override
    public void updateState(State state, EntangledBlockEntity entity, UpdateContext context){
        if(!entity.isBound()){
            state.isBound = false;
            return;
        }

        state.isBound = true;
        boolean isSameDimension = entity.getLevel().dimension() == entity.getBoundDimensionIdentifier();
        BlockPos boundPos = entity.getBoundBlockPos();
        BlockEntity boundEntity = isSameDimension ? entity.getLevel().getBlockEntity(boundPos) : null;
        BlockState boundState = entity.getBoundBlockState();

        boolean renderEntity = boundEntity != null
            && !BuiltInRegistries.BLOCK_ENTITY_TYPE.get(BLACKLISTED_ENTITIES).map(tag -> tag.contains(BuiltInRegistries.BLOCK_ENTITY_TYPE.get(ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK_ENTITY_TYPE, BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(boundEntity.getType()))).orElseThrow())).orElse(false) // Dude who the fuck makes this garbage system, I just want to check if something's in a tag
            && !ERRORED_BLOCK_ENTITIES.contains(boundEntity.getType());
        boolean renderBlock = boundState != null && boundState.getRenderShape() == RenderShape.MODEL
            && !boundState.is(BLACKLISTED_BLOCKS)
            && !ERRORED_BLOCK_STATES.contains(boundState);

        if(renderEntity && (!(boundEntity instanceof EntangledBlockEntity) || depth < 10)){
            depth++;
            var renderer = ClientUtils.getMinecraft().getBlockEntityRenderDispatcher().getRenderer(boundEntity);
            state.boundEntityRenderer = renderer;
            state.boundEntityType = boundEntity.getType();
            if(renderer != null){
                BlockEntityRenderState boundEntityRenderState = renderer.createRenderState();
                try{
                    renderer.extractRenderState(boundEntity, boundEntityRenderState, context.partialTicks(), context.cameraPos(), context.breakingOverlay());
                }catch(Exception e){
                    ERRORED_BLOCK_ENTITIES.add(boundEntity.getType());
                    Entangled.LOGGER.error("Encountered an exception whilst extracting block entity render state for '{}'! Please report to Entangled!", Registries.BLOCK_ENTITY_TYPES.getIdentifier(boundEntity.getType()), e);
                }
                state.boundEntityRenderState = boundEntityRenderState;
            }
            depth--;
        }else
            state.boundEntityRenderer = null;
        if(renderBlock){
            state.renderBlock = true;
            state.boundBlockRenderState.clear();
            BlockAndTintGetter boundLevel = isSameDimension && entity.getLevel() instanceof BlockAndTintGetter clientLevel ? clientLevel : BlockAndTintGetter.EMPTY;
            try{
                BlockStateModel model = ClientUtils.getMinecraft().getModelManager().getBlockStateModelSet().get(boundState);
                QuadEmitter emitter = state.boundBlockRenderState.setupMesh(IDENTITY_MATRIX, model.hasMaterialFlag(BakedQuad.FLAG_TRANSLUCENT));
                RandomSource random = context.randomSource(boundState.getSeed(boundPos));
                model.emitQuads(emitter, boundLevel, boundPos, boundState, random, _ -> false);
            }catch(Exception e){
                ERRORED_BLOCK_STATES.add(state.boundBlock);
                Entangled.LOGGER.error("Encountered an exception whilst rendering block '{}'! Please report to Entangled!", boundState, e);
            }

            IntList tintLayers = state.boundBlockRenderState.tintLayers();
            for(BlockTintSource tintSource : ClientUtils.getMinecraft().getBlockColors().getTintSources(boundState))
                tintLayers.add(tintSource.colorInWorld(boundState, boundLevel, boundPos));
        }else
            state.renderBlock = false;

        // Add bounding box
        if(renderBlock){
            VoxelShape shape = boundState.getOcclusionShape();
            if(!shape.isEmpty())
                state.boundBlockBounds = shape.bounds();
            else
                state.boundBlockBounds = FULL_BLOCK_BOUNDS;
        }
    }

    @Override
    public void submit(SubmitNodeCollector output, State state, RenderContext context){
        if(!state.isBound)
            return;
        PoseStack poseStack = context.poseStack();

        // rotate and scale
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        if(EntangledConfig.rotateRenderedBlock.get()){
            float angleX = System.currentTimeMillis() % 10000 / 10000f * 2 * (float)Math.PI;
            float angleY = System.currentTimeMillis() % 11000 / 11000f * 2 * (float)Math.PI;
            float angleZ = System.currentTimeMillis() % 12000 / 12000f * 2 * (float)Math.PI;
            poseStack.mulPose(new Quaternionf().setAngleAxis(angleX, 1, 0, 0));
            poseStack.mulPose(new Quaternionf().setAngleAxis(angleY, 0, 1, 0));
            poseStack.mulPose(new Quaternionf().setAngleAxis(angleZ, 0, 0, 1));
        }
        if(state.boundBlockBounds != null){
            AABB bounds = state.boundBlockBounds;
            float scale = 0.4763f / (float)Math.sqrt((bounds.getXsize() * bounds.getXsize() + bounds.getYsize() * bounds.getYsize() + bounds.getZsize() * bounds.getZsize()) / 4);
            poseStack.scale(scale, scale, scale);
            poseStack.translate(-bounds.getCenter().x, -bounds.getCenter().y, -bounds.getCenter().z);
        }else{
            poseStack.scale(0.55f, 0.55f, 0.55f);
            poseStack.translate(-0.5, -0.5, -0.5);
        }

        // Render block
        if(state.renderBlock){
            ModelFeatureRenderer.CrumblingOverlay breakingOverlay = context.breakingOverlay();
            state.boundBlockRenderState.submit(
                poseStack,
                output,
                context.packedLight(),
                breakingOverlay == null ? OverlayTexture.NO_OVERLAY : breakingOverlay.progress(),
                0
            );
        }
        // Render block entity
        if(state.boundEntityRenderer != null){
            try{
                state.boundEntityRenderer.submit(
                    state.boundEntityRenderState,
                    poseStack,
                    output,
                    context.cameraRenderState()
                );
            }catch(Exception e){
                ERRORED_BLOCK_ENTITIES.add(state.boundEntityType);
                Entangled.LOGGER.error("Encountered an exception whilst rendering block entity '{}'! Please report to Entangled!", Registries.BLOCK_ENTITY_TYPES.getIdentifier(state.boundEntityType), e);
            }
        }

        poseStack.popPose();
    }

    public static class State {

        public boolean isBound;
        public BlockState boundBlock;
        public boolean renderBlock;
        public final BlockModelRenderState boundBlockRenderState = new BlockModelRenderState();
        public AABB boundBlockBounds;
        public BlockEntityType<?> boundEntityType;
        public BlockEntityRenderer<?,BlockEntityRenderState> boundEntityRenderer;
        public BlockEntityRenderState boundEntityRenderState;
    }
}
