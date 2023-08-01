package com.supermartijn642.entangled;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.registry.Registries;
import com.supermartijn642.core.render.CustomBlockEntityRenderer;
import com.supermartijn642.core.render.RenderConfiguration;
import com.supermartijn642.core.render.RenderStateConfiguration;
import com.supermartijn642.core.render.TextureAtlases;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.MinecraftForgeClient;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created 3/16/2020 by SuperMartijn642
 */
public class EntangledBlockEntityRenderer implements CustomBlockEntityRenderer<EntangledBlockEntity> {

    private static final Set<Class<? extends TileEntity>> ERRORED_BLOCK_ENTITIES = Collections.synchronizedSet(new HashSet<>());
    private static final Set<IBlockState> ERRORED_BLOCK_STATES = Collections.synchronizedSet(new HashSet<>());

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
    public void render(EntangledBlockEntity entity, float partialTicks, int destroyStage, float alpha){
        if(!entity.isBound())
            return;

        TileEntity boundTile = entity.getWorld().provider.getDimensionType().getId() == entity.getBoundDimensionIdentifier() ? entity.getWorld().getTileEntity(entity.getBoundBlockPos()) : null;
        IBlockState boundState = entity.getBoundBlockState();

        boolean renderTile = boundTile != null && canRenderTileEntity(Registries.BLOCK_ENTITY_CLASSES.getIdentifier(boundTile.getClass())) && !ERRORED_BLOCK_ENTITIES.contains(boundTile.getClass());
        boolean renderBlock = boundState != null && boundState.getRenderType() == EnumBlockRenderType.MODEL && canRenderBlock(Registries.BLOCKS.getIdentifier(boundState.getBlock())) && !ERRORED_BLOCK_STATES.contains(boundState);

        // get the bounding box
        AxisAlignedBB bounds = new AxisAlignedBB(0, 0, 0, 1, 1, 1);
        if(renderBlock && entity.getWorld().provider.getDimensionType().getId() == entity.getBoundDimensionIdentifier()){
            AxisAlignedBB shape = boundState.getBoundingBox(entity.getWorld(), entity.getBoundBlockPos());
            if(shape.minX != shape.maxX || shape.minY != shape.maxY || shape.minZ != shape.maxZ)
                bounds = shape;
        }

        GlStateManager.pushMatrix();

        // rotate and scale
        GlStateManager.translate(0.5, 0.5, 0.5);
        if(EntangledConfig.rotateRenderedBlock.get()){
            float angleX = System.currentTimeMillis() % 10000 / 10000f * 360f;
            float angleY = System.currentTimeMillis() % 11000 / 11000f * 360f;
            float angleZ = System.currentTimeMillis() % 12000 / 12000f * 360f;
            GlStateManager.rotate(angleX, 1, 0, 0);
            GlStateManager.rotate(angleY, 0, 1, 0);
            GlStateManager.rotate(angleZ, 0, 0, 1);
        }
        double sizeX = bounds.maxX - bounds.minX, sizeY = bounds.maxY - bounds.minY, sizeZ = bounds.maxZ - bounds.minZ;
        float scale = 0.4763f / (float)Math.sqrt((sizeX * sizeX + sizeY * sizeY + sizeZ * sizeZ) / 4);
        GlStateManager.scale(scale, scale, scale);
        GlStateManager.translate(-bounds.getCenter().x, -bounds.getCenter().y, -bounds.getCenter().z);

        if(renderTile){
            if(!(boundTile instanceof EntangledBlockEntity) || depth < 10){
                depth++;
                try{
                    TileEntityRendererDispatcher.instance.render(boundTile, 0, 0, 0, partialTicks);
                }catch(Exception e){
                    ERRORED_BLOCK_ENTITIES.add(boundTile.getClass());
                    Entangled.LOGGER.error("Encountered an exception whilst rendering block entity '" + boundTile.getClass() + "'! Please report to Entangled!", e);
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
                        buffer.setTranslation(0, -1000, 0);

                        ForgeHooksClient.setRenderLayer(layer);
                        BlockRendererDispatcher blockRenderer = ClientUtils.getBlockRenderer();
                        IBakedModel model = blockRenderer.getModelForState(boundState);
                        blockRenderer.getBlockModelRenderer().renderModel(entity.getWorld(), model, boundState, new BlockPos(0, 1000, 0), buffer, false);

                        buffer.setTranslation(0, 0, 0);
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
        return !Entangled.RENDER_BLACKLISTED_MODS.contains(block.getResourceDomain()) && !Entangled.RENDER_BLACKLISTED_BLOCKS.contains(block);
    }

    private static boolean canRenderTileEntity(ResourceLocation tile){
        return !Entangled.RENDER_BLACKLISTED_MODS.contains(tile.getResourceDomain()) && !Entangled.RENDER_BLACKLISTED_TILE_ENTITIES.contains(tile);
    }
}
