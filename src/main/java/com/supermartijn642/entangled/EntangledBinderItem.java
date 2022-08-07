package com.supermartijn642.entangled;

import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.item.BaseItem;
import com.supermartijn642.core.item.CreativeItemGroup;
import com.supermartijn642.core.item.ItemProperties;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.DimensionType;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class EntangledBinderItem extends BaseItem {

    public EntangledBinderItem(){
        super(ItemProperties.create().maxStackSize(1).group(CreativeItemGroup.getMisc()));
    }

    @Override
    public InteractionFeedback interactWithBlock(ItemStack stack, EntityPlayer player, EnumHand hand, World level, BlockPos hitPos, EnumFacing hitSide, Vec3d hitLocation){
        if(level.isRemote)
            return InteractionFeedback.SUCCESS;
        NBTTagCompound compound = stack.getTagCompound() == null ? new NBTTagCompound() : stack.getTagCompound();
        if(compound.getBoolean("bound") && compound.getInteger("dimension") == level.provider.getDimension() &&
            compound.getInteger("boundx") == hitPos.getX() &&
            compound.getInteger("boundy") == hitPos.getY() && compound.getInteger("boundz") == hitPos.getZ())
            return InteractionFeedback.CONSUME;
        compound.setBoolean("bound", true);
        compound.setInteger("dimension", level.provider.getDimension());
        compound.setInteger("boundx", hitPos.getX());
        compound.setInteger("boundy", hitPos.getY());
        compound.setInteger("boundz", hitPos.getZ());
        compound.setInteger("blockstate", Block.getStateId(level.getBlockState(hitPos)));
        stack.setTagCompound(compound);
        player.sendStatusMessage(TextComponents.translation("entangled.entangled_binder.select").color(TextFormatting.YELLOW).get(), true);
        return InteractionFeedback.SUCCESS;
    }

    @Override
    public ItemUseResult interact(ItemStack stack, EntityPlayer player, EnumHand hand, World level){
        if(player.world.isRemote)
            return super.interact(stack, player, hand, level);
        NBTTagCompound compound = player.getHeldItem(hand).getTagCompound();
        if(player.isSneaking() && compound != null && compound.getBoolean("bound")){
            compound.setBoolean("bound", false);
            player.getHeldItem(hand).setTagCompound(compound);
            player.sendStatusMessage(TextComponents.translation("entangled.entangled_binder.clear").color(TextFormatting.YELLOW).get(), true);
            return ItemUseResult.consume(stack);
        }
        return super.interact(stack, player, hand, level);
    }

    @Override
    protected void appendItemInformation(ItemStack stack, @Nullable IBlockAccess level, Consumer<ITextComponent> info, boolean advanced){
        info.accept(TextComponents.translation("entangled.entangled_binder.info").color(TextFormatting.AQUA).get());

        NBTTagCompound tag = stack.getTagCompound();
        if(tag != null && tag.hasKey("bound") && tag.getBoolean("bound")){
            int x = tag.getInteger("boundx"), y = tag.getInteger("boundy"), z = tag.getInteger("boundz");
            ITextComponent dimension = TextComponents.dimension(DimensionType.getById(tag.getInteger("dimension"))).color(TextFormatting.GOLD).get();
            ITextComponent xText = TextComponents.string(Integer.toString(x)).color(TextFormatting.GOLD).get();
            ITextComponent yText = TextComponents.string(Integer.toString(y)).color(TextFormatting.GOLD).get();
            ITextComponent zText = TextComponents.string(Integer.toString(z)).color(TextFormatting.GOLD).get();
            if(tag.hasKey("blockstate")){
                ITextComponent name = TextComponents.blockState(Block.getStateById(tag.getInteger("blockstate"))).color(TextFormatting.GOLD).get();
                info.accept(TextComponents.translation("entangled.entangled_binder.info.target.known", name, xText, yText, zText, dimension).color(TextFormatting.YELLOW).get());
            }else
                info.accept(TextComponents.translation("entangled.entangled_binder.info.target.unknown", xText, yText, zText, dimension).color(TextFormatting.YELLOW).get());
        }
    }
}
