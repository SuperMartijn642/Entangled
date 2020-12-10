package com.supermartijn642.entangled;

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
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

import java.util.List;

/**
 * Created 2/6/2020 by SuperMartijn642
 */
public class EntangledBinder extends Item {

    public EntangledBinder(){
        super(new Item.Properties().maxStackSize(1).group(ItemGroup.SEARCH));
        this.setRegistryName("item");
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context){
        if(context.getWorld().isRemote)
            return ActionResultType.SUCCESS;
        ItemStack stack = context.getItem();
        CompoundNBT compound = stack.getTag() == null ? new CompoundNBT() : stack.getTag();
        if(compound.getBoolean("bound") && compound.getInt("dimension") == context.getWorld().getDimension().getType().getId() &&
            compound.getInt("boundx") == context.getPos().getX() &&
            compound.getInt("boundy") == context.getPos().getY() && compound.getInt("boundz") == context.getPos().getZ())
            return ActionResultType.PASS;
        compound.putBoolean("bound", true);
        compound.putInt("dimension", context.getWorld().getDimension().getType().getId());
        compound.putInt("boundx", context.getPos().getX());
        compound.putInt("boundy", context.getPos().getY());
        compound.putInt("boundz", context.getPos().getZ());
        stack.setTag(compound);
        context.getPlayer().sendMessage(new TranslationTextComponent("entangled.entangled_binder.select").applyTextStyle(TextFormatting.YELLOW));
        return ActionResultType.SUCCESS;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn){
        if(playerIn.world.isRemote)
            return super.onItemRightClick(worldIn, playerIn, handIn);
        CompoundNBT compound = playerIn.getHeldItem(handIn).getTag();
        if(playerIn.isCrouching() && compound != null && compound.getBoolean("bound")){
            compound.putBoolean("bound", false);
            playerIn.getHeldItem(handIn).setTag(compound);
            playerIn.sendMessage(new TranslationTextComponent("entangled.entangled_binder.clear").applyTextStyle(TextFormatting.YELLOW));
        }
        return super.onItemRightClick(worldIn, playerIn, handIn);
    }

    @Override
    public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn){
        tooltip.add(new TranslationTextComponent("entangled.entangled_binder.info").applyTextStyle(TextFormatting.AQUA));

        CompoundNBT tag = stack.getOrCreateTag();
        if(tag.contains("bound") && tag.getBoolean("bound")){
            int x = tag.getInt("boundx"), y = tag.getInt("boundy"), z = tag.getInt("boundz");
            String dimension = DimensionType.getById(tag.getInt("dimension")).getRegistryName().getPath();
            dimension = dimension.substring(dimension.lastIndexOf(":") + 1);
            dimension = Character.toUpperCase(dimension.charAt(0)) + dimension.substring(1);
            tooltip.add(new TranslationTextComponent("entangled.entangled_binder.info.target", x, y, z, dimension).applyTextStyle(TextFormatting.YELLOW));
        }
    }
}
