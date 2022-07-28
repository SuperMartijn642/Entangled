package com.supermartijn642.entangled;

import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.item.BaseItem;
import com.supermartijn642.core.item.ItemProperties;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * Created 2/6/2020 by SuperMartijn642
 */
public class EntangledBinderItem extends BaseItem {

    public EntangledBinderItem(){
        super(ItemProperties.create().maxStackSize(1).group(CreativeModeTab.TAB_MISC));
    }

    @Override
    public InteractionFeedback interactWithBlock(ItemStack stack, Player player, InteractionHand hand, Level level, BlockPos hitPos, Direction hitSide, Vec3 hitLocation){
        if(level.isClientSide || player == null)
            return InteractionFeedback.SUCCESS;
        CompoundTag compound = stack.getTag() == null ? new CompoundTag() : stack.getTag();
        if(compound.getBoolean("bound") && compound.getString("dimension").equals(level.dimension().location().toString()) &&
            compound.getInt("boundx") == hitPos.getX() &&
            compound.getInt("boundy") == hitPos.getY() && compound.getInt("boundz") == hitPos.getZ())
            return InteractionFeedback.CONSUME;
        compound.putBoolean("bound", true);
        compound.putString("dimension", level.dimension().location().toString());
        compound.putInt("boundx", hitPos.getX());
        compound.putInt("boundy", hitPos.getY());
        compound.putInt("boundz", hitPos.getZ());
        compound.putInt("blockstate", Block.getId(level.getBlockState(hitPos)));
        stack.setTag(compound);
        player.displayClientMessage(TextComponents.translation("entangled.entangled_binder.select").color(ChatFormatting.YELLOW).get(), true);
        return InteractionFeedback.SUCCESS;
    }

    @Override
    public ItemUseResult interact(ItemStack stack, Player player, InteractionHand hand, Level level){
        if(player.level.isClientSide)
            return super.interact(stack, player, hand, level);
        CompoundTag compound = player.getItemInHand(hand).getTag();
        if(player.isCrouching() && compound != null && compound.getBoolean("bound")){
            compound.putBoolean("bound", false);
            player.getItemInHand(hand).setTag(compound);
            player.displayClientMessage(TextComponents.translation("entangled.entangled_binder.clear").color(ChatFormatting.YELLOW).get(), true);
            return ItemUseResult.consume(stack);
        }
        return super.interact(stack, player, hand, level);
    }

    @Override
    protected void appendItemInformation(ItemStack stack, @Nullable BlockGetter level, Consumer<Component> info, boolean advanced){
        info.accept(TextComponents.translation("entangled.entangled_binder.info").color(ChatFormatting.AQUA).get());

        CompoundTag tag = stack.getOrCreateTag();
        if(tag.contains("bound") && tag.getBoolean("bound")){
            int x = tag.getInt("boundx"), y = tag.getInt("boundy"), z = tag.getInt("boundz");
            Component dimension = TextComponents.dimension(ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(tag.getString("dimension")))).color(ChatFormatting.GOLD).get();
            Component xText = TextComponents.string(Integer.toString(x)).color(ChatFormatting.GOLD).get();
            Component yText = TextComponents.string(Integer.toString(y)).color(ChatFormatting.GOLD).get();
            Component zText = TextComponents.string(Integer.toString(z)).color(ChatFormatting.GOLD).get();
            if(tag.contains("blockstate")){
                Component name = TextComponents.blockState(Block.stateById(tag.getInt("blockstate"))).color(ChatFormatting.GOLD).get();
                info.accept(TextComponents.translation("entangled.entangled_binder.info.target.known", name, xText, yText, zText, dimension).color(ChatFormatting.YELLOW).get());
            }else
                info.accept(TextComponents.translation("entangled.entangled_binder.info.target.unknown", xText, yText, zText, dimension).color(ChatFormatting.YELLOW).get());
        }
    }
}
