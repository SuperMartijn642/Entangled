package com.supermartijn642.entangled;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.block.BaseBlock;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

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
            entity.setLevel(ClientUtils.getWorld());
            entity.readData(data);
            BlockEntityRenderer<EntangledBlockEntity> renderer = ClientUtils.getMinecraft().getBlockEntityRenderDispatcher().getRenderer(entity);
            poseStack.pushPose();
//            poseStack.scale(0.5f, 0.5f, 0.5f);
            //noinspection DataFlowIssue
            renderer.render(entity, ClientUtils.getPartialTicks(), poseStack, bufferSource, combinedLight, combinedOverlay);
            poseStack.popPose();
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
        BakedModel unbound = context.bake(ResourceLocation.fromNamespaceAndPath("entangled", "block/unbound"));
        BakedModel bound = context.bake(ResourceLocation.fromNamespaceAndPath("entangled", "block/bound"));
        return (renderState, stack, modelResolver, displayContext, level, entity, someRandomId) -> {
            CompoundTag data = stack.get(BaseBlock.TILE_DATA);
            RenderType renderType = ItemBlockRenderTypes.getRenderType(stack);
            // If the block is not bound, just render the unbound model
            if(data == null || !data.getBoolean("bound")){
                renderState.newLayer().setupBlockModel(unbound, renderType);
                return;
            }

            // Render the bound model
            renderState.newLayer().setupBlockModel(bound, renderType);
            // Render the block entity
            renderState.newLayer().setupSpecialModel(ENTITY_RENDERER, data, bound);
        };
    }

    @Override
    public void resolveDependencies(Resolver resolver){
        resolver.resolve(ResourceLocation.fromNamespaceAndPath("entangled", "block/unbound"));
        resolver.resolve(ResourceLocation.fromNamespaceAndPath("entangled", "block/bound"));
    }
}
