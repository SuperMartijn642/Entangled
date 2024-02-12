package com.supermartijn642.entangled;

import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.item.BaseItem;
import com.supermartijn642.core.item.CreativeItemGroup;
import com.supermartijn642.core.item.ItemProperties;
import com.supermartijn642.core.registry.RegistryUtil;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * Created 2/6/2020 by SuperMartijn642
 */
public class EntangledBinderItem extends BaseItem {

    public static boolean isBound(ItemStack stack){
        if(!stack.hasTag())
            return false;
        CompoundNBT data = stack.getTag();
        if(!data.getBoolean("bound") || !data.contains("dimension", Constants.NBT.TAG_STRING)
            || !data.contains("boundx", Constants.NBT.TAG_INT) || !data.contains("boundy", Constants.NBT.TAG_INT)
            || !data.contains("boundz", Constants.NBT.TAG_INT))
            return false;
        String dimension = data.getString("dimension");
        return RegistryUtil.isValidIdentifier(dimension);
    }

    public static BlockPos getBoundPosition(ItemStack stack){
        CompoundNBT data = stack.getTag();
        return new BlockPos(data.getInt("boundx"), data.getInt("boundy"), data.getInt("boundz"));
    }

    public static ResourceLocation getBoundDimension(ItemStack stack){
        return new ResourceLocation(stack.getTag().getString("dimension"));
    }

    public EntangledBinderItem(){
        super(ItemProperties.create().maxStackSize(1).group(CreativeItemGroup.getMisc()));
    }

    @Override
    public InteractionFeedback interactWithBlock(ItemStack stack, PlayerEntity player, Hand hand, World level, BlockPos hitPos, Direction hitSide, Vec3d hitLocation){
        // Check if already bound to the clicked position
        CompoundNBT compound = stack.getTag() == null ? new CompoundNBT() : stack.getTag();
        if(compound.getBoolean("bound")
            && (
            compound.contains("dimension", Constants.NBT.TAG_STRING) ?
                DimensionType.getByName(new ResourceLocation(compound.getString("dimension"))) == level.getDimension().getType() :
                compound.getInt("dimension") == level.getDimension().getType().getId())
            && compound.getInt("boundx") == hitPos.getX()
            && compound.getInt("boundy") == hitPos.getY()
            && compound.getInt("boundz") == hitPos.getZ())
            return InteractionFeedback.CONSUME;

        // Bind to clicked position
        if(!level.isClientSide){
            compound.putBoolean("bound", true);
            compound.putString("dimension", DimensionType.getName(level.getDimension().getType()).toString());
            compound.putInt("boundx", hitPos.getX());
            compound.putInt("boundy", hitPos.getY());
            compound.putInt("boundz", hitPos.getZ());
            compound.putInt("blockstate", Block.getId(level.getBlockState(hitPos)));
            stack.setTag(compound);
            player.displayClientMessage(TextComponents.translation("entangled.entangled_binder.select").color(TextFormatting.YELLOW).get(), true);
        }
        return InteractionFeedback.SUCCESS;
    }

    @Override
    public ItemUseResult interact(ItemStack stack, PlayerEntity player, Hand hand, World level){
        CompoundNBT compound = stack.getTag();
        if(player.isSneaking() && compound != null && compound.getBoolean("bound")){
            if(!level.isClientSide){
                stack.removeTagKey("bound");
                stack.removeTagKey("dimension");
                stack.removeTagKey("boundx");
                stack.removeTagKey("boundy");
                stack.removeTagKey("boundz");
                stack.removeTagKey("blockstate");
                player.displayClientMessage(TextComponents.translation("entangled.entangled_binder.clear").color(TextFormatting.YELLOW).get(), true);
            }
            return ItemUseResult.consume(stack);
        }
        return super.interact(stack, player, hand, level);
    }

    @Override
    protected void appendItemInformation(ItemStack stack, @Nullable IBlockReader level, Consumer<ITextComponent> info, boolean advanced){
        info.accept(TextComponents.translation("entangled.entangled_binder.info").color(TextFormatting.AQUA).get());

        if(isBound(stack)){
            CompoundNBT tag = stack.getOrCreateTag();
            int x = tag.getInt("boundx"), y = tag.getInt("boundy"), z = tag.getInt("boundz");
            ITextComponent dimension = TextComponents.dimension(DimensionType.getByName(new ResourceLocation(tag.getString("dimension")))).color(TextFormatting.GOLD).get();
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
