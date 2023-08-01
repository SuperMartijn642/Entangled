package com.supermartijn642.entangled;

import com.mojang.blaze3d.platform.GlStateManager;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.registry.Registries;
import com.supermartijn642.core.render.CustomBlockEntityRenderer;
import com.supermartijn642.core.render.RenderConfiguration;
import com.supermartijn642.core.render.RenderStateConfiguration;
import com.supermartijn642.core.render.TextureAtlases;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;

import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * Created 3/16/2020 by SuperMartijn642
 */
public class EntangledBlockEntityRenderer implements CustomBlockEntityRenderer<EntangledBlockEntity> {

    private static final Set<TileEntityType<?>> ERRORED_BLOCK_ENTITIES = Collections.synchronizedSet(new HashSet<>());
    private static final Set<BlockState> ERRORED_BLOCK_STATES = Collections.synchronizedSet(new HashSet<>());

    private static final RenderConfiguration BLOCK_RENDER_CONFIGURATION = RenderConfiguration.create(
        "entangled",
        "block_solid",
        DefaultVertexFormats.BLOCK,
        RenderConfiguration.PrimitiveType.QUADS,
        RenderStateConfiguration
            .builder()
            .disableLightmap()
            .useTexture(TextureAtlases.getBlocks(), false, false)
            .enableCulling()
            .useLessThanOrEqualDepthTest()
            .useTranslucentTransparency()
            .disableLighting()
            .build()
    );

    private static int depth = 0;

    @Override
    public void render(EntangledBlockEntity entity, float partialTicks, int combinedOverlay){
        if(!entity.isBound())
            return;

        TileEntity boundTile = entity.getLevel().getDimension().getType().getId() == entity.getBoundDimensionIdentifier() ? entity.getLevel().getBlockEntity(entity.getBoundBlockPos()) : null;
        BlockState boundState = entity.getBoundBlockState();

        boolean renderTile = boundTile != null && canRenderTileEntity(Registries.BLOCK_ENTITY_TYPES.getIdentifier(boundTile.getType())) && !ERRORED_BLOCK_ENTITIES.contains(boundTile.getType());
        boolean renderBlock = boundState != null && boundState.getRenderShape() == BlockRenderType.MODEL && canRenderBlock(Registries.BLOCKS.getIdentifier(boundState.getBlock())) && !ERRORED_BLOCK_STATES.contains(boundState);

        // get the bounding box
        AxisAlignedBB bounds = new AxisAlignedBB(0, 0, 0, 1, 1, 1);
        if(renderBlock && entity.getLevel().getDimension().getType().getId() == entity.getBoundDimensionIdentifier()){
            VoxelShape shape = boundState.getOcclusionShape(entity.getLevel(), entity.getBoundBlockPos());
            if(!shape.isEmpty())
                bounds = shape.bounds();
        }

        GlStateManager.pushMatrix();

        // rotate and scale
        GlStateManager.translated(0.5, 0.5, 0.5);
        if(EntangledConfig.rotateRenderedBlock.get()){
            float angleX = System.currentTimeMillis() % 10000 / 10000f * 360f;
            float angleY = System.currentTimeMillis() % 11000 / 11000f * 360f;
            float angleZ = System.currentTimeMillis() % 12000 / 12000f * 360f;
            GlStateManager.rotatef(angleX, 1, 0, 0);
            GlStateManager.rotatef(angleY, 0, 1, 0);
            GlStateManager.rotatef(angleZ, 0, 0, 1);
        }
        float scale = 0.4763f / (float)Math.sqrt((bounds.getXsize() * bounds.getXsize() + bounds.getYsize() * bounds.getYsize() + bounds.getZsize() * bounds.getZsize()) / 4);
        GlStateManager.scalef(scale, scale, scale);
        GlStateManager.translated(-bounds.getCenter().x, -bounds.getCenter().y, -bounds.getCenter().z);

        if(renderTile){
            if(!(boundTile instanceof EntangledBlockEntity) || depth < 10){
                depth++;
                try{
                    TileEntityRendererDispatcher.instance.render(boundTile, 0, 0, 0, partialTicks);
                }catch(Exception e){
                    ERRORED_BLOCK_ENTITIES.add(boundTile.getType());
                    Entangled.LOGGER.error("Encountered an exception whilst rendering block entity '" + Registries.BLOCK_ENTITY_TYPES.getIdentifier(boundTile.getType()) + "'! Please report to Entangled!", e);
                }
                depth--;
            }
        }
        if(renderBlock){
            BlockRenderLayer initialLayer = MinecraftForgeClient.getRenderLayer();
            try{
                for(BlockRenderLayer layer : BlockRenderLayer.values()){
                    if(boundState.getBlock().canRenderInLayer(boundState, layer)){
                        BufferBuilder buffer = BLOCK_RENDER_CONFIGURATION.begin();
                        buffer.offset(0, -1000, 0);

                        ForgeHooksClient.setRenderLayer(layer);
                        BlockRendererDispatcher blockRenderer = ClientUtils.getBlockRenderer();
                        IModelData data = blockRenderer.getBlockModel(boundState).getModelData(entity.getLevel(), entity.getBlockPos(), boundState, EmptyModelData.INSTANCE);
                        IBakedModel model = blockRenderer.getBlockModel(boundState);
                        blockRenderer.getModelRenderer().renderModel(entity.getLevel(), model, boundState, new BlockPos(0, 1000, 0), buffer, false, new Random(), 0, data);

                        buffer.offset(0, 0, 0);
                        BLOCK_RENDER_CONFIGURATION.end();
                    }
                }
            }catch(Exception e){
                ERRORED_BLOCK_STATES.add(boundState);
                Entangled.LOGGER.error("Encountered an exception whilst rendering block '" + boundState + "'! Please report to Entangled!", e);
            }
            ForgeHooksClient.setRenderLayer(initialLayer);
        }

        GlStateManager.popMatrix();
    }

    private static boolean canRenderBlock(ResourceLocation block){
        return !Entangled.RENDER_BLACKLISTED_MODS.contains(block.getNamespace()) && !Entangled.RENDER_BLACKLISTED_BLOCKS.contains(block);
    }

    private static boolean canRenderTileEntity(ResourceLocation tile){
        return !Entangled.RENDER_BLACKLISTED_MODS.contains(tile.getNamespace()) && !Entangled.RENDER_BLACKLISTED_TILE_ENTITIES.contains(tile);
    }
}
