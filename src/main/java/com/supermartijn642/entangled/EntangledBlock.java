package com.supermartijn642.entangled;

import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.block.BaseBlock;
import com.supermartijn642.core.block.BlockProperties;
import com.supermartijn642.core.block.EntityHoldingBlock;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
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

public class EntangledBlock extends BaseBlock implements EntityHoldingBlock {

    public static final PropertyBool ON = PropertyBool.create("on");

    public EntangledBlock(){
        super(true, BlockProperties.create(new Material(MapColor.BROWN)).sound(SoundType.STONE).destroyTime(1).explosionResistance(2));
        this.setDefaultState(this.blockState.getBaseState().withProperty(ON, false));
    }

    @Override
    protected InteractionFeedback interact(IBlockState state, World level, BlockPos pos, EntityPlayer player, EnumHand hand, EnumFacing hitSide, Vec3d hitLocation){
        if(level.isRemote)
            return InteractionFeedback.PASS;
        ItemStack stack = player.getHeldItem(hand);
        if(player.isSneaking() && stack.isEmpty() && state.getValue(ON)){
            ((EntangledBlockEntity)level.getTileEntity(pos)).bind(null, 0);
            player.sendStatusMessage(TextComponents.translation("entangled.entangled_block.unbind").color(TextFormatting.YELLOW).get(), true);
            level.setBlockState(pos, state.withProperty(ON, false));
            return InteractionFeedback.SUCCESS;
        }else if(stack.getItem() == Entangled.item){
            NBTTagCompound compound = stack.getTagCompound();
            if(compound == null || !compound.getBoolean("bound"))
                player.sendStatusMessage(TextComponents.translation("entangled.entangled_block.no_selection").color(TextFormatting.RED).get(), true);
            else{
                BlockPos pos2 = new BlockPos(compound.getInteger("boundx"), compound.getInteger("boundy"), compound.getInteger("boundz"));
                if(pos2.equals(pos))
                    player.sendStatusMessage(TextComponents.translation("entangled.entangled_block.self").color(TextFormatting.RED).get(), true);
                else{
                    if(!level.getBlockState(pos).getValue(ON))
                        level.setBlockState(pos, state.withProperty(ON, true));
                    EntangledBlockEntity tile = (EntangledBlockEntity)level.getTileEntity(pos);
                    if(compound.getInteger("dimension") == level.provider.getDimensionType().getId()){
                        if(EntangledConfig.maxDistance.get() == -1 || pos.distanceSq(pos2) <= (EntangledConfig.maxDistance.get() + 0.5) * (EntangledConfig.maxDistance.get() + 0.5)){
                            tile.bind(pos2, compound.getInteger("dimension"));
                            player.sendStatusMessage(TextComponents.translation("entangled.entangled_block.bind").color(TextFormatting.YELLOW).get(), true);
                        }else
                            player.sendStatusMessage(TextComponents.translation("entangled.entangled_block.too_far").color(TextFormatting.RED).get(), true);
                    }else{
                        if(EntangledConfig.allowDimensional.get()){
                            tile.bind(pos2, compound.getInteger("dimension"));
                            player.sendStatusMessage(TextComponents.translation("entangled.entangled_block.bind").color(TextFormatting.YELLOW).get(), true);
                        }else
                            player.sendStatusMessage(TextComponents.translation("entangled.entangled_block.wrong_dimension").color(TextFormatting.RED).get(), true);
                    }
                }
            }
            return InteractionFeedback.SUCCESS;
        }
        return InteractionFeedback.PASS;
    }

    @Override
    public int getMetaFromState(IBlockState state){
        return state.getValue(ON) ? 1 : 0;
    }

    @Override
    public IBlockState getStateFromMeta(int meta){
        return this.getDefaultState().withProperty(ON, meta == 1);
    }

    @Nullable
    @Override
    public TileEntity createNewBlockEntity(){
        return new EntangledBlockEntity();
    }

    @Override
    protected BlockStateContainer createBlockState(){
        return new BlockStateContainer(this, ON);
    }

    @Override
    public boolean isOpaqueCube(IBlockState state){
        return false;
    }

    @Override
    protected void appendItemInformation(ItemStack stack, IBlockAccess level, Consumer<ITextComponent> info, boolean advanced){
        String key = EntangledConfig.allowDimensional.get() ?
            EntangledConfig.maxDistance.get() == -1 ? "infinite_other_dimension" : "ranged_other_dimension" :
            EntangledConfig.maxDistance.get() == -1 ? "infinite_same_dimension" : "ranged_same_dimension";
        ITextComponent maxDistance = TextComponents.string(Integer.toString(EntangledConfig.maxDistance.get())).color(TextFormatting.GOLD).get();
        info.accept(TextComponents.translation("entangled.entangled_block.info." + key, maxDistance).color(TextFormatting.AQUA).get());

        NBTTagCompound tag = stack.hasTagCompound() ? stack.getTagCompound().getCompoundTag("tileData") : new NBTTagCompound();
        if(tag.hasKey("bound") && tag.getBoolean("bound")){
            int x = tag.getInteger("boundx"), y = tag.getInteger("boundy"), z = tag.getInteger("boundz");
            ITextComponent dimension = TextComponents.dimension(DimensionType.getById(tag.getInteger("dimension"))).color(TextFormatting.GOLD).get();
            ITextComponent name = TextComponents.blockState(Block.getStateById(tag.getInteger("blockstate"))).color(TextFormatting.GOLD).get();
            ITextComponent xText = TextComponents.string(Integer.toString(x)).color(TextFormatting.GOLD).get();
            ITextComponent yText = TextComponents.string(Integer.toString(y)).color(TextFormatting.GOLD).get();
            ITextComponent zText = TextComponents.string(Integer.toString(z)).color(TextFormatting.GOLD).get();
            info.accept(TextComponents.translation("entangled.entangled_block.info.bound", name, xText, yText, zText, dimension).color(TextFormatting.YELLOW).get());
        }
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase player, EnumHand hand){
        ItemStack stack = player.getHeldItem(hand);
        NBTTagCompound compound = stack.hasTagCompound() ? stack.getTagCompound().getCompoundTag("tileData") : new NBTTagCompound();
        if(compound.getBoolean("bound")){
            BlockPos pos2 = new BlockPos(compound.getInteger("boundx"), compound.getInteger("boundy"), compound.getInteger("boundz"));
            if(compound.getInteger("dimension") == world.provider.getDimensionType().getId()){
                if(EntangledConfig.maxDistance.get() >= 0 && pos.distanceSq(pos2) > EntangledConfig.maxDistance.get() * EntangledConfig.maxDistance.get()){
                    if(player instanceof EntityPlayer && !world.isRemote)
                        player.sendMessage(TextComponents.translation("entangled.entangled_block.too_far").color(TextFormatting.RED).get());
                    return null;
                }
            }else{
                if(!EntangledConfig.allowDimensional.get()){
                    if(player instanceof EntityPlayer && !world.isRemote)
                        player.sendMessage(TextComponents.translation("entangled.entangled_block.wrong_dimension").color(TextFormatting.RED).get());
                    return null;
                }
            }
            return this.getDefaultState().withProperty(ON, true);
        }
        return this.getDefaultState();
    }

    @Override
    public boolean hasComparatorInputOverride(IBlockState state){
        return true;
    }

    @Override
    public int getComparatorInputOverride(IBlockState state, World world, BlockPos pos){
        TileEntity entity = world.getTileEntity(pos);
        return entity instanceof EntangledBlockEntity ? ((EntangledBlockEntity)entity).getAnalogOutputSignal() : 0;
    }

    @Override
    public boolean canProvidePower(IBlockState state){
        return true;
    }

    @Override
    public int getWeakPower(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing direction){
        TileEntity entity = world.getTileEntity(pos);
        return entity instanceof EntangledBlockEntity ? ((EntangledBlockEntity)entity).getRedstoneSignal(direction) : 0;
    }

    @Override
    public int getStrongPower(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing direction){
        TileEntity entity = world.getTileEntity(pos);
        return entity instanceof EntangledBlockEntity ? ((EntangledBlockEntity)entity).getDirectRedstoneSignal(direction) : 0;
    }
}
