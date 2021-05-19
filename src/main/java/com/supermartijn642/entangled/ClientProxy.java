package com.supermartijn642.entangled;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.supermartijn642.core.ClientUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.DrawHighlightEvent;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.lwjgl.opengl.GL11;

import java.util.OptionalDouble;

/**
 * Created 3/16/2020 by SuperMartijn642
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientProxy {

    protected static final RenderState.LayerState VIEW_OFFSET_Z_LAYERING = new RenderState.LayerState("view_offset_z_layering", () -> {
        RenderSystem.pushMatrix();
        RenderSystem.scalef(0.99975586F, 0.99975586F, 0.99975586F);
    }, RenderSystem::popMatrix);
    protected static final RenderState.TransparencyState TRANSLUCENT_TRANSPARENCY = new RenderState.TransparencyState("translucent_transparency", () -> {
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
    }, () -> {
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    });
    private static final RenderType HIGHLIGHT_RENDER_TYPE = RenderType.makeType(
        "entangled:highlight",
        DefaultVertexFormats.POSITION_COLOR,
        GL11.GL_LINES,
        128,
        RenderType.State.getBuilder().line(new RenderState.LineState(OptionalDouble.of(1))).transparency(TRANSLUCENT_TRANSPARENCY).layer(VIEW_OFFSET_Z_LAYERING).writeMask(new RenderState.WriteMaskState(true, false)).depthTest(new RenderState.DepthTestState("no_depth",GL11.GL_ALWAYS)).build(false));
    private static final IRenderTypeBuffer.Impl HIGHLIGHT_BUFFER = IRenderTypeBuffer.getImpl(new BufferBuilder(128));

    @SubscribeEvent
    public static void setup(FMLClientSetupEvent e){
        ClientRegistry.bindTileEntityRenderer(Entangled.tile, EntangledBlockTileRenderer::new);
    }

    public static String translate(String key, Object... arguments){
        return I18n.format(key, arguments);
    }

    @SubscribeEvent
    public static void onModelBake(ModelBakeEvent e){
        // replace the entangled block item model
        ResourceLocation location = new ModelResourceLocation(new ResourceLocation("entangled", "block"), "inventory");
        IBakedModel model = e.getModelRegistry().get(location);
        if(model != null)
            e.getModelRegistry().put(location, new EntangledBlockBakedItemModel(model));
    }

    @Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class Events {

        @SubscribeEvent
        public static void onDrawPlayerEvent(RenderWorldLastEvent e){
            ItemStack stack = ClientUtils.getPlayer().getHeldItem(Hand.MAIN_HAND);
            if(stack.getItem() instanceof BlockItem && ((BlockItem)stack.getItem()).getBlock() == Entangled.block && stack.hasTag() && stack.getOrCreateTag().contains("tileData")){
                CompoundNBT compound = stack.getOrCreateTag().getCompound("tileData");
                World world = ClientUtils.getMinecraft().world;
                if(compound.getBoolean("bound") && compound.getString("dimension").equals(world.getDimensionKey().getLocation().toString())){
                    BlockPos pos = new BlockPos(compound.getInt("boundx"), compound.getInt("boundy"), compound.getInt("boundz"));
                    renderHighlight(e.getMatrixStack(), ClientUtils.getMinecraft().getRenderManager().info, world, pos, 86 / 255f, 0 / 255f, 156 / 255f);
                }
            }else if(stack.getItem() == Entangled.item){
                CompoundNBT compound = stack.getOrCreateTag();
                World world = ClientUtils.getMinecraft().world;
                if(compound.getBoolean("bound") && compound.getString("dimension").equals(world.getDimensionKey().getLocation().toString())){
                    BlockPos pos = new BlockPos(compound.getInt("boundx"), compound.getInt("boundy"), compound.getInt("boundz"));
                    renderHighlight(e.getMatrixStack(), ClientUtils.getMinecraft().getRenderManager().info, world, pos, 235 / 255f, 210 / 255f, 52 / 255f);
                }
            }
        }

        @SubscribeEvent
        public static void onBlockHighlight(DrawHighlightEvent.HighlightBlock e){
            // RayTraceResult#getBlockPos() can definitely be null
            if(!EntangledConfig.renderBlockHighlight.get() || e.getTarget().getPos() == null)
                return;

            World world = Minecraft.getInstance().world;
            TileEntity tile = world.getTileEntity(e.getTarget().getPos());
            if(tile instanceof EntangledBlockTile && ((EntangledBlockTile)tile).isBound() && ((EntangledBlockTile)tile).getBoundDimension() == world.getDimensionKey())
                renderHighlight(e.getMatrix(), e.getInfo(), world, ((EntangledBlockTile)tile).getBoundBlockPos(), 86 / 255f, 0 / 255f, 156 / 255f);
        }

        private static void renderHighlight(MatrixStack matrixStack, ActiveRenderInfo info, World world, BlockPos pos, float red, float green, float blue){
            matrixStack.push();
            Vector3d playerPos = info.getProjectedView();
            matrixStack.translate(-playerPos.x, -playerPos.y, -playerPos.z);
            IVertexBuilder builder = HIGHLIGHT_BUFFER.getBuffer(HIGHLIGHT_RENDER_TYPE);

            VoxelShape shape = world.getBlockState(pos).getRenderShape(world, pos);
            matrixStack.translate(pos.getX(), pos.getY(), pos.getZ() + 1);
            drawShape(matrixStack, builder, shape, 0,0,-1, red, green, blue, 1);

            matrixStack.pop();
            RenderSystem.disableDepthTest();
            HIGHLIGHT_BUFFER.finish();
        }

        private static void drawShape(MatrixStack matrixStackIn, IVertexBuilder bufferIn, VoxelShape shapeIn, double xIn, double yIn, double zIn, float red, float green, float blue, float alpha){
            Matrix4f matrix4f = matrixStackIn.getLast().getMatrix();
            shapeIn.forEachEdge((x1, y1, z1, x2, y2, z2) -> {
                bufferIn.pos(matrix4f, (float)(x1 + xIn), (float)(y1 + yIn), (float)(z1 + zIn)).color(red, green, blue, alpha).endVertex();
                bufferIn.pos(matrix4f, (float)(x2 + xIn), (float)(y2 + yIn), (float)(z2 + zIn)).color(red, green, blue, alpha).endVertex();
            });
        }
    }

}
