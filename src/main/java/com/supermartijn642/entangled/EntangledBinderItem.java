package com.supermartijn642.entangled;

import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.item.BaseItem;
import com.supermartijn642.core.item.CreativeItemGroup;
import com.supermartijn642.core.item.ItemProperties;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * Created 2/6/2020 by SuperMartijn642
 */
public class EntangledBinderItem extends BaseItem {

    public EntangledBinderItem(){
        super(ItemProperties.create().maxStackSize(1).group(CreativeItemGroup.getMisc()));
    }

    @Override
    public InteractionFeedback interactWithBlock(ItemStack stack, PlayerEntity player, Hand hand, World level, BlockPos hitPos, Direction hitSide, Vector3d hitLocation){
        if(level.isClientSide || player == null)
            return InteractionFeedback.SUCCESS;
        CompoundNBT compound = stack.getTag() == null ? new CompoundNBT() : stack.getTag();
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
        player.displayClientMessage(TextComponents.translation("entangled.entangled_binder.select").color(TextFormatting.YELLOW).get(), true);
        return InteractionFeedback.SUCCESS;
    }

    @Override
    public ItemUseResult interact(ItemStack stack, PlayerEntity player, Hand hand, World level){
        if(player.level.isClientSide)
            return super.interact(stack, player, hand, level);
        CompoundNBT compound = player.getItemInHand(hand).getTag();
        if(player.isCrouching() && compound != null && compound.getBoolean("bound")){
            compound.putBoolean("bound", false);
            player.getItemInHand(hand).setTag(compound);
            player.displayClientMessage(TextComponents.translation("entangled.entangled_binder.clear").color(TextFormatting.YELLOW).get(), true);
            return ItemUseResult.consume(stack);
        }
        return super.interact(stack, player, hand, level);
    }

    @Override
    protected void appendItemInformation(ItemStack stack, @Nullable IBlockReader level, Consumer<ITextComponent> info, boolean advanced){
        info.accept(TextComponents.translation("entangled.entangled_binder.info").color(TextFormatting.AQUA).get());

        CompoundNBT tag = stack.getOrCreateTag();
        if(tag.contains("bound") && tag.getBoolean("bound")){
            int x = tag.getInt("boundx"), y = tag.getInt("boundy"), z = tag.getInt("boundz");
            ITextComponent dimension = TextComponents.dimension(RegistryKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(tag.getString("dimension")))).color(TextFormatting.GOLD).get();
            ITextComponent xText = TextComponents.string(Integer.toString(x)).color(TextFormatting.GOLD).get();
            ITextComponent yText = TextComponents.string(Integer.toString(y)).color(TextFormatting.GOLD).get();
            ITextComponent zText = TextComponents.string(Integer.toString(z)).color(TextFormatting.GOLD).get();
            if(tag.contains("blockstate")){
                ITextComponent name = TextComponents.blockState(Block.stateById(tag.getInt("blockstate"))).color(TextFormatting.GOLD).get();
                info.accept(TextComponents.translation("entangled.entangled_binder.info.target.known", name, xText, yText, zText, dimension).color(TextFormatting.YELLOW).get());
            }else
                info.accept(TextComponents.translation("entangled.entangled_binder.info.target.unknown", xText, yText, zText, dimension).color(TextFormatting.YELLOW).get());
        }
    }
}
