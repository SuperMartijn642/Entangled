package com.supermartijn642.entangled;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.item.BaseItem;
import com.supermartijn642.core.item.CreativeItemGroup;
import com.supermartijn642.core.item.ItemProperties;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Created 2/6/2020 by SuperMartijn642
 */
public class EntangledBinderItem extends BaseItem {

    public static final DataComponentType<BinderTarget> BINDER_TARGET = DataComponentType.<BinderTarget>builder()
        .persistent(RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("dimension").forGetter(BinderTarget::dimension),
            BlockPos.CODEC.fieldOf("pos").forGetter(BinderTarget::pos),
            BlockState.CODEC.optionalFieldOf("state").forGetter(BinderTarget::state)
        ).apply(instance, BinderTarget::new)))
        .networkSynchronized(StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, BinderTarget::dimension,
            BlockPos.STREAM_CODEC, BinderTarget::pos,
            ByteBufCodecs.INT, t -> t.state().map(Block::getId).orElse(-1),
            (dim, pos, id) -> new BinderTarget(dim, pos, id >= 0 ? Optional.ofNullable(Block.BLOCK_STATE_REGISTRY.byId(id)) : Optional.empty())
        )).build();

    public static boolean isBound(ItemStack stack){
        return stack.has(BINDER_TARGET);
    }

    public static BlockPos getBoundPosition(ItemStack stack){
        //noinspection DataFlowIssue
        return stack.get(BINDER_TARGET).pos;
    }

    public static ResourceLocation getBoundDimension(ItemStack stack){
        //noinspection DataFlowIssue
        return stack.get(BINDER_TARGET).dimension;
    }

    public EntangledBinderItem(){
        super(ItemProperties.create().maxStackSize(1).group(CreativeItemGroup.getToolsAndUtilities()));
    }

    @Override
    public InteractionFeedback interactWithBlock(ItemStack stack, Player player, InteractionHand hand, Level level, BlockPos hitPos, Direction hitSide, Vec3 hitLocation){
        // Check if already bound to the clicked position
        BinderTarget target = stack.get(BINDER_TARGET);
        if(target != null && target.dimension.equals(level.dimension().location()) && target.pos.equals(hitPos))
            return InteractionFeedback.CONSUME;

        // Bind to clicked position
        if(!level.isClientSide){
            stack.set(BINDER_TARGET, new BinderTarget(level.dimension().location(), hitPos, Optional.of(level.getBlockState(hitPos))));
            player.displayClientMessage(TextComponents.translation("entangled.entangled_binder.select").color(ChatFormatting.YELLOW).get(), true);
        }
        return InteractionFeedback.SUCCESS;
    }

    @Override
    public ItemUseResult interact(ItemStack stack, Player player, InteractionHand hand, Level level){
        if(player.isCrouching() && stack.has(BINDER_TARGET)){
            if(!level.isClientSide){
                stack.remove(BINDER_TARGET);
                player.displayClientMessage(TextComponents.translation("entangled.entangled_binder.clear").color(ChatFormatting.YELLOW).get(), true);
            }
            return ItemUseResult.consume(stack);
        }
        return super.interact(stack, player, hand, level);
    }

    @Override
    protected void appendItemInformation(ItemStack stack, Consumer<Component> info, boolean advanced){
        info.accept(TextComponents.translation("entangled.entangled_binder.info").color(ChatFormatting.AQUA).get());

        BinderTarget target = stack.get(BINDER_TARGET);
        if(target != null){
            Component dimension = TextComponents.dimension(ResourceKey.create(Registries.DIMENSION, target.dimension)).color(ChatFormatting.GOLD).get();
            Component xText = TextComponents.number(target.pos.getX()).color(ChatFormatting.GOLD).get();
            Component yText = TextComponents.number(target.pos.getY()).color(ChatFormatting.GOLD).get();
            Component zText = TextComponents.number(target.pos.getZ()).color(ChatFormatting.GOLD).get();
            if(target.state.isPresent()){
                Component name = TextComponents.blockState(target.state.get()).color(ChatFormatting.GOLD).get();
                info.accept(TextComponents.translation("entangled.entangled_binder.info.target.known", name, xText, yText, zText, dimension).color(ChatFormatting.YELLOW).get());
            }else
                info.accept(TextComponents.translation("entangled.entangled_binder.info.target.unknown", xText, yText, zText, dimension).color(ChatFormatting.YELLOW).get());
        }
    }

    public record BinderTarget(ResourceLocation dimension, BlockPos pos, Optional<BlockState> state) {
    }
}
