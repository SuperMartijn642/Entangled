package com.supermartijn642.entangled;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

/**
 * Created 2/6/2020 by SuperMartijn642
 */
public class EntangledBinder extends Item {

    public EntangledBinder(){
        super(new Item.Properties().maxStackSize(1).group(ItemGroup.SEARCH));
        this.setRegistryName("item");
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        if(context.getWorld().isRemote)
            return ActionResultType.SUCCESS;
        ItemStack stack = context.getPlayer().getHeldItem(context.getHand());
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
        context.getPlayer().sendMessage(new StringTextComponent(TextFormatting.YELLOW + "Block selected!"));
        return ActionResultType.SUCCESS;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        if(playerIn.world.isRemote)
            return super.onItemRightClick(worldIn, playerIn, handIn);
        CompoundNBT compound = playerIn.getHeldItem(handIn).getTag();
        if(playerIn.isCrouching() && compound != null && compound.getBoolean("bound")){
            compound.putBoolean("bound", false);
            playerIn.getHeldItem(handIn).setTag(compound);
            playerIn.sendMessage(new StringTextComponent(TextFormatting.YELLOW + "Connection cleared!"));
        }
        return super.onItemRightClick(worldIn, playerIn, handIn);
    }
}
