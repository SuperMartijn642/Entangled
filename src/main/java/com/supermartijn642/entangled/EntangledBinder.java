package com.supermartijn642.entangled;

import com.supermartijn642.core.TextComponents;
import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

import java.util.List;

/**
 * Created 2/6/2020 by SuperMartijn642
 */
public class EntangledBinder extends Item {

    public EntangledBinder(){
        super(new Item.Properties().stacksTo(1).tab(ItemGroup.TAB_SEARCH));
        this.setRegistryName("item");
    }

    @Override
    public ActionResultType useOn(ItemUseContext context){
        if(context.getLevel().isClientSide || context.getPlayer() == null)
            return ActionResultType.SUCCESS;
        ItemStack stack = context.getItemInHand();
        CompoundNBT compound = stack.getTag() == null ? new CompoundNBT() : stack.getTag();
        if(compound.getBoolean("bound") && compound.getInt("dimension") == context.getLevel().getDimension().getType().getId() &&
            compound.getInt("boundx") == context.getClickedPos().getX() &&
            compound.getInt("boundy") == context.getClickedPos().getY() && compound.getInt("boundz") == context.getClickedPos().getZ())
            return ActionResultType.PASS;
        compound.putBoolean("bound", true);
        compound.putInt("dimension", context.getLevel().getDimension().getType().getId());
        compound.putInt("boundx", context.getClickedPos().getX());
        compound.putInt("boundy", context.getClickedPos().getY());
        compound.putInt("boundz", context.getClickedPos().getZ());
        compound.putInt("blockstate", Block.getId(context.getLevel().getBlockState(context.getClickedPos())));
        stack.setTag(compound);
        context.getPlayer().displayClientMessage(TextComponents.translation("entangled.entangled_binder.select").color(TextFormatting.YELLOW).get(), true);
        return ActionResultType.SUCCESS;
    }

    @Override
    public ActionResult<ItemStack> use(World worldIn, PlayerEntity playerIn, Hand handIn){
        if(playerIn.level.isClientSide)
            return super.use(worldIn, playerIn, handIn);
        CompoundNBT compound = playerIn.getItemInHand(handIn).getTag();
        if(playerIn.isSneaking() && compound != null && compound.getBoolean("bound")){
            compound.putBoolean("bound", false);
            playerIn.getItemInHand(handIn).setTag(compound);
            playerIn.displayClientMessage(TextComponents.translation("entangled.entangled_binder.clear").color(TextFormatting.YELLOW).get(), true);
        }
        return super.use(worldIn, playerIn, handIn);
    }

    @Override
    public void appendHoverText(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn){
        tooltip.add(TextComponents.translation("entangled.entangled_binder.info").color(TextFormatting.AQUA).get());

        CompoundNBT tag = stack.getOrCreateTag();
        if(tag.contains("bound") && tag.getBoolean("bound")){
            int x = tag.getInt("boundx"), y = tag.getInt("boundy"), z = tag.getInt("boundz");
            ITextComponent dimension = TextComponents.dimension(DimensionType.getById(tag.getInt("dimension"))).color(TextFormatting.GOLD).get();
            ITextComponent xText = TextComponents.string(Integer.toString(x)).color(TextFormatting.GOLD).get();
            ITextComponent yText = TextComponents.string(Integer.toString(y)).color(TextFormatting.GOLD).get();
            ITextComponent zText = TextComponents.string(Integer.toString(z)).color(TextFormatting.GOLD).get();
            if(tag.contains("blockstate")){
                ITextComponent name = TextComponents.blockState(Block.stateById(tag.getInt("blockstate"))).color(TextFormatting.GOLD).get();
                tooltip.add(TextComponents.translation("entangled.entangled_binder.info.target.known", name, xText, yText, zText, dimension).color(TextFormatting.YELLOW).get());
            }else
                tooltip.add(TextComponents.translation("entangled.entangled_binder.info.target.unknown", xText, yText, zText, dimension).color(TextFormatting.YELLOW).get());
        }
    }
}
