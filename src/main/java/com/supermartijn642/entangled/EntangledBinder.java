package com.supermartijn642.entangled;

import com.supermartijn642.core.TextComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.List;

/**
 * Created 2/6/2020 by SuperMartijn642
 */
public class EntangledBinder extends Item {

    public EntangledBinder(){
        super(new Item.Properties().stacksTo(1).tab(CreativeModeTab.TAB_SEARCH));
        this.setRegistryName("item");
    }

    @Override
    public InteractionResult useOn(UseOnContext context){
        if(context.getLevel().isClientSide || context.getPlayer() == null)
            return InteractionResult.SUCCESS;
        ItemStack stack = context.getItemInHand();
        CompoundTag compound = stack.getTag() == null ? new CompoundTag() : stack.getTag();
        if(compound.getBoolean("bound") && compound.getString("dimension").equals(context.getLevel().dimension().location().toString()) &&
            compound.getInt("boundx") == context.getClickedPos().getX() &&
            compound.getInt("boundy") == context.getClickedPos().getY() && compound.getInt("boundz") == context.getClickedPos().getZ())
            return InteractionResult.PASS;
        compound.putBoolean("bound", true);
        compound.putString("dimension", context.getLevel().dimension().location().toString());
        compound.putInt("boundx", context.getClickedPos().getX());
        compound.putInt("boundy", context.getClickedPos().getY());
        compound.putInt("boundz", context.getClickedPos().getZ());
        compound.putInt("blockstate", Block.getId(context.getLevel().getBlockState(context.getClickedPos())));
        stack.setTag(compound);
        context.getPlayer().displayClientMessage(TextComponents.translation("entangled.entangled_binder.select").color(ChatFormatting.YELLOW).get(), true);
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn){
        if(playerIn.level.isClientSide)
            return super.use(worldIn, playerIn, handIn);
        CompoundTag compound = playerIn.getItemInHand(handIn).getTag();
        if(playerIn.isCrouching() && compound != null && compound.getBoolean("bound")){
            compound.putBoolean("bound", false);
            playerIn.getItemInHand(handIn).setTag(compound);
            playerIn.displayClientMessage(TextComponents.translation("entangled.entangled_binder.clear").color(ChatFormatting.YELLOW).get(), true);
        }
        return super.use(worldIn, playerIn, handIn);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn){
        tooltip.add(TextComponents.translation("entangled.entangled_binder.info").color(ChatFormatting.AQUA).get());

        CompoundTag tag = stack.getOrCreateTag();
        if(tag.contains("bound") && tag.getBoolean("bound")){
            int x = tag.getInt("boundx"), y = tag.getInt("boundy"), z = tag.getInt("boundz");
            Component dimension = TextComponents.dimension(ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(tag.getString("dimension")))).color(ChatFormatting.GOLD).get();
            Component xText = TextComponents.string(Integer.toString(x)).color(ChatFormatting.GOLD).get();
            Component yText = TextComponents.string(Integer.toString(y)).color(ChatFormatting.GOLD).get();
            Component zText = TextComponents.string(Integer.toString(z)).color(ChatFormatting.GOLD).get();
            if(tag.contains("blockstate")){
                Component name = TextComponents.blockState(Block.stateById(tag.getInt("blockstate"))).color(ChatFormatting.GOLD).get();
                tooltip.add(TextComponents.translation("entangled.entangled_binder.info.target.known", name, xText, yText, zText, dimension).color(ChatFormatting.YELLOW).get());
            }else
                tooltip.add(TextComponents.translation("entangled.entangled_binder.info.target.unknown", xText, yText, zText, dimension).color(ChatFormatting.YELLOW).get());
        }
    }
}
