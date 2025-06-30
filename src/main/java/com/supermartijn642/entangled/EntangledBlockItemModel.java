package com.supermartijn642.entangled;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.block.BaseBlock;
import com.supermartijn642.core.util.Pair;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.item.BlockModelWrapper;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.List;
import java.util.Set;

/**
 * Created 23/12/2024 by SuperMartijn642
 */
public class EntangledBlockItemModel implements ItemModel.Unbaked {

    public static final MapCodec<EntangledBlockItemModel> CODEC = MapCodec.unit(new EntangledBlockItemModel());
    private static final SpecialModelRenderer<CompoundTag> ENTITY_RENDERER = new SpecialModelRenderer<>() {
        static EntangledBlockEntity entity;

        @Override
        public void render(CompoundTag data, ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight, int combinedOverlay, boolean hasFoil){
            if(entity == null)
                entity = new EntangledBlockEntity(BlockPos.ZERO, Entangled.block.defaultBlockState());
            Level level = ClientUtils.getWorld();
            entity.setLevel(level);
            entity.readData(TagValueInput.create(new ProblemReporter.ScopedCollector(entity.problemPath(), Entangled.LOGGER), level.registryAccess(), data));
            BlockEntityRenderer<EntangledBlockEntity> renderer = ClientUtils.getMinecraft().getBlockEntityRenderDispatcher().getRenderer(entity);
            //noinspection DataFlowIssue
            renderer.render(entity, ClientUtils.getPartialTicks(), poseStack, bufferSource, combinedLight, combinedOverlay, Vec3.ZERO);
        }

        @Override
        public void getExtents(Set<Vector3f> set){
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
    public ItemModel bake(ItemModel.BakingContext context){
        Pair<List<BakedQuad>,ModelRenderProperties> unbound = bakeModel(context, ResourceLocation.fromNamespaceAndPath("entangled", "block/unbound"));
        Pair<List<BakedQuad>,ModelRenderProperties> bound = bakeModel(context, ResourceLocation.fromNamespaceAndPath("entangled", "block/bound"));
        Vector3f[] unboundExtents = BlockModelWrapper.computeExtents(unbound.left());
        Vector3f[] boundExtents = BlockModelWrapper.computeExtents(bound.left());
        return (renderState, stack, modelResolver, displayContext, level, entity, someRandomId) -> {
            CompoundTag data = stack.get(BaseBlock.TILE_DATA);
            RenderType renderType = ItemBlockRenderTypes.getRenderType(stack);
            // If the block is not bound, just render the unbound model
            if(data == null || !data.getBooleanOr("bound", false)){
                ItemStackRenderState.LayerRenderState layer = renderState.newLayer();
                layer.setExtents(() -> unboundExtents);
                layer.setRenderType(renderType);
                unbound.right().applyToLayer(layer, displayContext);
                layer.prepareQuadList().addAll(unbound.left());
                renderState.appendModelIdentityElement(Pair.of(this, false));
                return;
            }

            // Render the bound model
            ItemStackRenderState.LayerRenderState layer = renderState.newLayer();
            layer.setExtents(() -> boundExtents);
            layer.setRenderType(renderType);
            bound.right().applyToLayer(layer, displayContext);
            layer.prepareQuadList().addAll(bound.left());
            // Render the block entity
            layer = renderState.newLayer();
            layer.setTransform(bound.right().transforms().getTransform(displayContext));
            layer.setupSpecialModel(ENTITY_RENDERER, data);

            BlockState state = Block.stateById(data.getIntOr("blockstate", 0));
            renderState.appendModelIdentityElement(Pair.of(this, state));
            renderState.setAnimated();
        };
    }

    @Override
    public void resolveDependencies(Resolver resolver){
        resolver.markDependency(ResourceLocation.fromNamespaceAndPath("entangled", "block/unbound"));
        resolver.markDependency(ResourceLocation.fromNamespaceAndPath("entangled", "block/bound"));
    }

    private static Pair<List<BakedQuad>,ModelRenderProperties> bakeModel(ItemModel.BakingContext context, ResourceLocation location){
        ModelBaker modelBaker = context.blockModelBaker();
        ResolvedModel model = modelBaker.getModel(location);
        TextureSlots textureSlots = model.getTopTextureSlots();
        List<BakedQuad> quads = model.bakeTopGeometry(textureSlots, modelBaker, BlockModelRotation.X0_Y0).getAll();
        ModelRenderProperties properties = ModelRenderProperties.fromResolvedModel(modelBaker, model, textureSlots);
        return Pair.of(quads, properties);
    }
}
