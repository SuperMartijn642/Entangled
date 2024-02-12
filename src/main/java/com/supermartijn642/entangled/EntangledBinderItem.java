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
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class EntangledBinderItem extends BaseItem {

    public static boolean isBound(ItemStack stack){
        if(!stack.hasTagCompound())
            return false;
        NBTTagCompound data = stack.getTagCompound();
        if(!data.getBoolean("bound") || !data.hasKey("dimension", Constants.NBT.TAG_INT)
            || !data.hasKey("boundx", Constants.NBT.TAG_INT) || !data.hasKey("boundy", Constants.NBT.TAG_INT)
            || !data.hasKey("boundz", Constants.NBT.TAG_INT))
            return false;
        return true;
    }

    public static BlockPos getBoundPosition(ItemStack stack){
        NBTTagCompound data = stack.getTagCompound();
        return new BlockPos(data.getInteger("boundx"), data.getInteger("boundy"), data.getInteger("boundz"));
    }

    public static int getBoundDimension(ItemStack stack){
        return stack.getTagCompound().getInteger("dimension");
    }

    public EntangledBinderItem(){
        super(ItemProperties.create().maxStackSize(1).group(CreativeItemGroup.getMisc()));
    }

    @Override
    public InteractionFeedback interactWithBlock(ItemStack stack, EntityPlayer player, EnumHand hand, World level, BlockPos hitPos, EnumFacing hitSide, Vec3d hitLocation){
        // Check if already bound to the clicked position
        NBTTagCompound compound = stack.getTagCompound() == null ? new NBTTagCompound() : stack.getTagCompound();
        if(compound.getBoolean("bound")
            && compound.getInteger("dimension") == level.provider.getDimension()
            && compound.getInteger("boundx") == hitPos.getX()
            && compound.getInteger("boundy") == hitPos.getY()
            && compound.getInteger("boundz") == hitPos.getZ())
            return InteractionFeedback.CONSUME;

        // Bind to clicked position
        if(!level.isRemote){
            compound.setBoolean("bound", true);
            compound.setInteger("dimension", level.provider.getDimension());
            compound.setInteger("boundx", hitPos.getX());
            compound.setInteger("boundy", hitPos.getY());
            compound.setInteger("boundz", hitPos.getZ());
            compound.setInteger("blockstate", Block.getStateId(level.getBlockState(hitPos)));
            stack.setTagCompound(compound);
            player.sendStatusMessage(TextComponents.translation("entangled.entangled_binder.select").color(TextFormatting.YELLOW).get(), true);
        }
        return InteractionFeedback.SUCCESS;
    }

    @Override
    public ItemUseResult interact(ItemStack stack, EntityPlayer player, EnumHand hand, World level){
        NBTTagCompound compound = stack.getTagCompound();
        if(player.isSneaking() && compound != null && compound.getBoolean("bound")){
            if(!level.isRemote){
                stack.removeSubCompound("bound");
                stack.removeSubCompound("dimension");
                stack.removeSubCompound("boundx");
                stack.removeSubCompound("boundy");
                stack.removeSubCompound("boundz");
                stack.removeSubCompound("blockstate");
                player.sendStatusMessage(TextComponents.translation("entangled.entangled_binder.clear").color(TextFormatting.YELLOW).get(), true);
            }
            return ItemUseResult.consume(stack);
        }
        return super.interact(stack, player, hand, level);
    }

    @Override
    protected void appendItemInformation(ItemStack stack, @Nullable IBlockAccess level, Consumer<ITextComponent> info, boolean advanced){
        info.accept(TextComponents.translation("entangled.entangled_binder.info").color(TextFormatting.AQUA).get());

        if(isBound(stack)){
            NBTTagCompound tag = stack.getTagCompound();
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
