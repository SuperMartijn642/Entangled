package com.supermartijn642.entangled;

import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.item.BaseItem;
import com.supermartijn642.core.item.ItemProperties;
import com.supermartijn642.core.registry.RegistryUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
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

    public static boolean isBound(ItemStack stack){
        if(!stack.hasTag())
            return false;
        CompoundTag data = stack.getTag();
        if(!data.getBoolean("bound") || !data.contains("dimension", Tag.TAG_STRING)
            || !data.contains("boundx", Tag.TAG_INT) || !data.contains("boundy", Tag.TAG_INT)
            || !data.contains("boundz", Tag.TAG_INT))
            return false;
        String dimension = data.getString("dimension");
        return RegistryUtil.isValidIdentifier(dimension);
    }

    public static BlockPos getBoundPosition(ItemStack stack){
        CompoundTag data = stack.getTag();
        return new BlockPos(data.getInt("boundx"), data.getInt("boundy"), data.getInt("boundz"));
    }

    public static ResourceLocation getBoundDimension(ItemStack stack){
        return new ResourceLocation(stack.getTag().getString("dimension"));
    }

    public EntangledBinderItem(){
        super(ItemProperties.create().maxStackSize(1).group(CreativeModeTab.TAB_MISC));
    }

    @Override
    public InteractionFeedback interactWithBlock(ItemStack stack, Player player, InteractionHand hand, Level level, BlockPos hitPos, Direction hitSide, Vec3 hitLocation){
        // Check if already bound to the clicked position
        CompoundTag compound = stack.getTag() == null ? new CompoundTag() : stack.getTag();
        if(compound.getBoolean("bound") && compound.getString("dimension").equals(level.dimension().location().toString()) &&
            compound.getInt("boundx") == hitPos.getX() &&
            compound.getInt("boundy") == hitPos.getY() && compound.getInt("boundz") == hitPos.getZ())
            return InteractionFeedback.CONSUME;

        // Bind to clicked position
        if(!level.isClientSide){
            compound.putBoolean("bound", true);
            compound.putString("dimension", level.dimension().location().toString());
            compound.putInt("boundx", hitPos.getX());
            compound.putInt("boundy", hitPos.getY());
            compound.putInt("boundz", hitPos.getZ());
            compound.putInt("blockstate", Block.getId(level.getBlockState(hitPos)));
            stack.setTag(compound);
            player.displayClientMessage(TextComponents.translation("entangled.entangled_binder.select").color(ChatFormatting.YELLOW).get(), true);
        }
        return InteractionFeedback.SUCCESS;
    }

    @Override
    public ItemUseResult interact(ItemStack stack, Player player, InteractionHand hand, Level level){
        CompoundTag compound = stack.getTag();
        if(player.isCrouching() && compound != null && compound.getBoolean("bound")){
            if(!level.isClientSide){
                stack.removeTagKey("bound");
                stack.removeTagKey("dimension");
                stack.removeTagKey("boundx");
                stack.removeTagKey("boundy");
                stack.removeTagKey("boundz");
                stack.removeTagKey("blockstate");
                player.displayClientMessage(TextComponents.translation("entangled.entangled_binder.clear").color(ChatFormatting.YELLOW).get(), true);
            }
            return ItemUseResult.consume(stack);
        }
        return super.interact(stack, player, hand, level);
    }

    @Override
    protected void appendItemInformation(ItemStack stack, @Nullable BlockGetter level, Consumer<Component> info, boolean advanced){
        info.accept(TextComponents.translation("entangled.entangled_binder.info").color(ChatFormatting.AQUA).get());

        if(isBound(stack)){
            CompoundTag tag = stack.getOrCreateTag();
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
