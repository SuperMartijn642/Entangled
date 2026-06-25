package com.supermartijn642.entangled;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.block.BaseBlock;
import com.supermartijn642.core.util.Pair;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4fc;
import org.joml.Vector3fc;

import java.util.List;
import java.util.function.Consumer;

/**
 * Created 23/12/2024 by SuperMartijn642
 */
public class EntangledBlockItemModel implements ItemModel.Unbaked {

    public static final MapCodec<EntangledBlockItemModel> CODEC = MapCodec.unit(new EntangledBlockItemModel());
    private static final CameraRenderState DUMMY_CAMERA_RENDER_STATE = new CameraRenderState();
    private static final SpecialModelRenderer<CompoundTag> ENTITY_RENDERER = new SpecialModelRenderer<>() {
        static EntangledBlockEntity entity;
        static BlockEntityRenderState entityRenderState;

        @Override
        public void submit(CompoundTag data, PoseStack poseStack, SubmitNodeCollector output, int combinedLight, int combinedOverlay, boolean hasFoil, int k){
            if(entity == null)
                entity = new EntangledBlockEntity(BlockPos.ZERO, Entangled.block.defaultBlockState());
            Level level = ClientUtils.getWorld();
            entity.setLevel(level);
            entity.readData(TagValueInput.create(new ProblemReporter.ScopedCollector(entity.problemPath(), Entangled.LOGGER), level.registryAccess(), data));
            BlockEntityRenderer<EntangledBlockEntity,BlockEntityRenderState> renderer = ClientUtils.getMinecraft().getBlockEntityRenderDispatcher().getRenderer(entity);
            if(renderer == null)
                return;
            if(entityRenderState == null)
                entityRenderState = renderer.createRenderState();
            renderer.extractRenderState(entity, entityRenderState, ClientUtils.getPartialTicks(), Vec3.ZERO, null);
            entityRenderState.lightCoords = combinedLight;
            entityRenderState.breakProgress = null;
            renderer.submit(entityRenderState, poseStack, output, DUMMY_CAMERA_RENDER_STATE);
        }

        @Override
        public void getExtents(Consumer<Vector3fc> set){
        }

        @Override
        public @Nullable CompoundTag extractArgument(ItemStack stack){
            return null;
        }
    };

    @Override
    public MapCodec<? extends ItemModel.Unbaked> type(){
        return CODEC;
    }

    @Override
    public ItemModel bake(ItemModel.BakingContext context, Matrix4fc transformation){
        Pair<List<BakedQuad>,ModelRenderProperties> unbound = bakeModel(context, Identifier.fromNamespaceAndPath("entangled", "block/unbound"));
        Pair<List<BakedQuad>,ModelRenderProperties> bound = bakeModel(context, Identifier.fromNamespaceAndPath("entangled", "block/bound"));
        Vector3fc[] unboundExtents = CuboidItemModelWrapper.computeExtents(unbound.left());
        Vector3fc[] boundExtents = CuboidItemModelWrapper.computeExtents(bound.left());
        return (renderState, stack, modelResolver, displayContext, level, entity, someRandomId) -> {
            renderState.appendModelIdentityElement(this);
            CompoundTag data = stack.get(BaseBlock.TILE_DATA);
            // If the block is not bound, just render the unbound model
            if(data == null || !data.getBooleanOr("bound", false)){
                ItemStackRenderState.LayerRenderState layer = renderState.newLayer();
                layer.setExtents(() -> unboundExtents);
                layer.setLocalTransform(transformation);
                unbound.right().applyToLayer(layer, displayContext);
                layer.prepareQuadList().addAll(unbound.left());
                renderState.appendModelIdentityElement(false);
                return;
            }

            // Render the bound model
            ItemStackRenderState.LayerRenderState layer = renderState.newLayer();
            layer.setExtents(() -> boundExtents);
            layer.setLocalTransform(transformation);
            bound.right().applyToLayer(layer, displayContext);
            layer.prepareQuadList().addAll(bound.left());
            // Render the block entity
            layer = renderState.newLayer();
            layer.setLocalTransform(transformation);
            layer.setItemTransform(bound.right().transforms().getTransform(displayContext));
            layer.setupSpecialModel(ENTITY_RENDERER, data);

            BlockState state = Block.stateById(data.getIntOr("blockstate", 0));
            renderState.appendModelIdentityElement(state);
            renderState.setAnimated();
        };
    }

    @Override
    public void resolveDependencies(Resolver resolver){
        resolver.markDependency(Identifier.fromNamespaceAndPath("entangled", "block/unbound"));
        resolver.markDependency(Identifier.fromNamespaceAndPath("entangled", "block/bound"));
    }

    private static Pair<List<BakedQuad>,ModelRenderProperties> bakeModel(ItemModel.BakingContext context, Identifier location){
        ModelBaker modelBaker = context.blockModelBaker();
        ResolvedModel model = modelBaker.getModel(location);
        TextureSlots textureSlots = model.getTopTextureSlots();
        List<BakedQuad> quads = model.bakeTopGeometry(textureSlots, modelBaker, BlockModelRotation.IDENTITY).getAll();
        CuboidItemModelWrapper.validateAtlasUsage(quads);
        ModelRenderProperties properties = ModelRenderProperties.fromResolvedModel(modelBaker, model, textureSlots);
        return Pair.of(quads, properties);
    }
}
