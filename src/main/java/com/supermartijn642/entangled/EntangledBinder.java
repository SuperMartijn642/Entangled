package com.supermartijn642.entangled;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.TextComponents;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;

import java.util.List;

public class EntangledBinder extends Item {

    public EntangledBinder(){
        this.setMaxStackSize(1);
        this.setRegistryName("item");
        this.setUnlocalizedName(Entangled.MODID + ".item");
        this.setCreativeTab(CreativeTabs.SEARCH);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ){
        if(worldIn.isRemote)
            return EnumActionResult.SUCCESS;
        ItemStack stack = player.getHeldItem(hand);
        NBTTagCompound compound = stack.getTagCompound() == null ? new NBTTagCompound() : stack.getTagCompound();
        if(compound.getBoolean("bound") && compound.getInteger("dimension") == worldIn.provider.getDimension() && compound.getInteger("boundx") == pos.getX() &&
            compound.getInteger("boundy") == pos.getY() && compound.getInteger("boundz") == pos.getZ())
            return EnumActionResult.PASS;
        compound.setBoolean("bound", true);
        compound.setInteger("dimension", worldIn.provider.getDimension());
        compound.setInteger("boundx", pos.getX());
        compound.setInteger("boundy", pos.getY());
        compound.setInteger("boundz", pos.getZ());
        stack.setTagCompound(compound);
        player.sendMessage(TextComponents.translation("entangled.entangled_binder.select").color(TextFormatting.YELLOW).get());
        return EnumActionResult.SUCCESS;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn){
        if(playerIn.world.isRemote)
            return super.onItemRightClick(worldIn, playerIn, handIn);
        NBTTagCompound compound = playerIn.getHeldItem(handIn).getTagCompound();
        if(playerIn.isSneaking() && compound != null && compound.getBoolean("bound")){
            compound.setBoolean("bound", false);
            playerIn.getHeldItem(handIn).setTagCompound(compound);
            playerIn.sendMessage(TextComponents.translation("entangled.entangled_binder.clear").color(TextFormatting.YELLOW).get());
        }
        return super.onItemRightClick(worldIn, playerIn, handIn);
    }

    @Override
    public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn){
        tooltip.add(TextFormatting.AQUA + ClientUtils.translate("entangled.entangled_binder.info"));

        NBTTagCompound tag = stack.getTagCompound();
        if(tag != null && tag.hasKey("bound") && tag.getBoolean("bound")){
            int x = tag.getInteger("boundx"), y = tag.getInteger("boundy"), z = tag.getInteger("boundz");
            ITextComponent dimension = TextComponents.dimension(DimensionType.getById(tag.getInteger("dimension"))).get();
            tooltip.add(TextFormatting.YELLOW + ClientUtils.translate("entangled.entangled_binder.info.target", x, y, z, dimension));
        }
    }
}
