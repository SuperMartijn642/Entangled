package com.supermartijn642.entangled;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.block.BaseBlock;
import com.supermartijn642.core.registry.ClientRegistrationHandler;
import com.supermartijn642.core.render.CustomRendererBakedModelWrapper;
import com.supermartijn642.core.render.RenderUtils;
import com.supermartijn642.core.render.RenderWorldEvent;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

/**
 * Created 3/16/2020 by SuperMartijn642
 */
public class EntangledClient implements ClientModInitializer {

    @Override
    public void onInitializeClient(){
        RenderWorldEvent.EVENT.register(EntangledClient::onDrawPlayerEvent);
        WorldRenderEvents.BLOCK_OUTLINE.register(EntangledClient::onBlockHighlight);

        register();
    }

    public static void register(){
        ClientRegistrationHandler handler = ClientRegistrationHandler.get("entangled");

        // Entangled block renderer
        handler.registerCustomBlockEntityRenderer(() -> Entangled.tile, EntangledBlockEntityRenderer::new);
        handler.registerCustomItemRenderer(() -> Entangled.block.asItem(), EntangledBlockItemRenderer::new);
        // Entangled block item model
        handler.registerModelOverwrite("entangled", "block", "inventory", CustomRendererBakedModelWrapper::wrap);
        // Entangled block render type
        handler.registerBlockModelCutoutRenderType(() -> Entangled.block);
    }

    public static void onDrawPlayerEvent(RenderWorldEvent e){
        ItemStack stack = ClientUtils.getPlayer().getItemInHand(InteractionHand.MAIN_HAND);
        Level world = ClientUtils.getWorld();

        if(stack.getItem() instanceof BlockItem && ((BlockItem)stack.getItem()).getBlock() == Entangled.block && stack.get(BaseBlock.TILE_DATA) != null){
            CompoundTag compound = stack.get(BaseBlock.TILE_DATA);
            if(compound.getBoolean("bound") && compound.getString("dimension").equals(world.dimension().location().toString())){
                BlockPos pos = new BlockPos(compound.getInt("boundx"), compound.getInt("boundy"), compound.getInt("boundz"));

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

    public static boolean onBlockHighlight(WorldRenderContext renderContext, WorldRenderContext.BlockOutlineContext blockOutlineContext){
        if(blockOutlineContext.blockPos() == null || !EntangledConfig.renderBlockHighlight.get())
            return true;

        Level world = Minecraft.getInstance().level;
        BlockEntity tile = world.getBlockEntity(blockOutlineContext.blockPos());
        if(tile instanceof EntangledBlockEntity && ((EntangledBlockEntity)tile).isBound() && ((EntangledBlockEntity)tile).getBoundDimensionIdentifier() == world.dimension()){
            BlockPos pos = ((EntangledBlockEntity)tile).getBoundBlockPos();

            renderContext.matrixStack().pushPose();
            Vec3 camera = RenderUtils.getCameraPosition();
            renderContext.matrixStack().translate(-camera.x, -camera.y, -camera.z);
            renderContext.matrixStack().translate(pos.getX(), pos.getY(), pos.getZ());

//            RenderUtils.renderShape(renderContext.matrixStack(), world.getBlockState(pos).getOcclusionShape(world, pos), 86 / 255f, 0 / 255f, 156 / 255f, false);
//            RenderUtils.renderShapeSides(renderContext.matrixStack(), world.getBlockState(pos).getOcclusionShape(world, pos), 86 / 255f, 0 / 255f, 156 / 255f, 30 / 255f, false);
// TODO
            renderContext.matrixStack().popPose();
        }

        return true;
    }
}
