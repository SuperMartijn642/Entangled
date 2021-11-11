package com.supermartijn642.entangled;

import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.ToolType;
import com.supermartijn642.core.block.BaseBlock;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.DimensionType;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class EntangledBlock extends BaseBlock {

    public static final PropertyBool ON = PropertyBool.create("on");

    public EntangledBlock(){
        super("block", true, Properties.create(new Material(MapColor.BROWN) {
            @Override
            public boolean isToolNotRequired(){
                return super.isToolNotRequired();
            }
        }).harvestTool(ToolType.PICKAXE).sound(SoundType.STONE).hardnessAndResistance(2f));
        this.setDefaultState(this.blockState.getBaseState().withProperty(ON, false));

        this.setCreativeTab(CreativeTabs.SEARCH);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ){
        if(worldIn.isRemote)
            return false;
        ItemStack stack = playerIn.getHeldItem(hand);
        if(playerIn.isSneaking() && stack == ItemStack.EMPTY && state.getValue(ON)){
            ((EntangledBlockTile)worldIn.getTileEntity(pos)).bind(null, 0);
            playerIn.sendStatusMessage(TextComponents.translation("entangled.entangled_block.unbind").color(TextFormatting.YELLOW).get(), true);
            worldIn.setBlockState(pos, state.withProperty(ON, false));
        }else if(stack != null && stack.getItem() == Entangled.item){
            NBTTagCompound compound = stack.getTagCompound();
            if(compound == null || !compound.getBoolean("bound"))
                playerIn.sendStatusMessage(TextComponents.translation("entangled.entangled_block.no_selection").color(TextFormatting.RED).get(), true);
            else{
                BlockPos pos2 = new BlockPos(compound.getInteger("boundx"), compound.getInteger("boundy"), compound.getInteger("boundz"));
                if(pos2.equals(pos))
                    playerIn.sendStatusMessage(TextComponents.translation("entangled.entangled_block.self").color(TextFormatting.RED).get(), true);
                else{
                    if(!worldIn.getBlockState(pos).getValue(ON))
                        worldIn.setBlockState(pos, state.withProperty(ON, true));
                    EntangledBlockTile tile = (EntangledBlockTile)worldIn.getTileEntity(pos);
                    if(compound.getInteger("dimension") == worldIn.provider.getDimensionType().getId()){
                        if(EntangledConfig.maxDistance.get() == -1 || pos.distanceSq(pos2) <= (EntangledConfig.maxDistance.get() + 0.5) * (EntangledConfig.maxDistance.get() + 0.5)){
                            tile.bind(pos2, compound.getInteger("dimension"));
                            playerIn.sendStatusMessage(TextComponents.translation("entangled.entangled_block.bind").color(TextFormatting.YELLOW).get(), true);
                        }else
                            playerIn.sendStatusMessage(TextComponents.translation("entangled.entangled_block.too_far").color(TextFormatting.RED).get(), true);
                    }else{
                        if(EntangledConfig.allowDimensional.get()){
                            tile.bind(pos2, compound.getInteger("dimension"));
                            playerIn.sendStatusMessage(TextComponents.translation("entangled.entangled_block.bind").color(TextFormatting.YELLOW).get(), true);
                        }else
                            playerIn.sendStatusMessage(TextComponents.translation("entangled.entangled_block.wrong_dimension").color(TextFormatting.RED).get(), true);
                    }
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public int getMetaFromState(IBlockState state){
        return state.getValue(ON) ? 1 : 0;
    }

    @Override
    public IBlockState getStateFromMeta(int meta){
        return this.getDefaultState().withProperty(ON, meta == 1);
    }

    @Override
    public boolean hasTileEntity(IBlockState state){
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state){
        return new EntangledBlockTile();
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
    public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn){
        String key = EntangledConfig.allowDimensional.get() ?
            EntangledConfig.maxDistance.get() == -1 ? "infinite_other_dimension" : "ranged_other_dimension" :
            EntangledConfig.maxDistance.get() == -1 ? "infinite_same_dimension" : "ranged_same_dimension";
        ITextComponent maxDistance = TextComponents.string(Integer.toString(EntangledConfig.maxDistance.get())).color(TextFormatting.GOLD).get();
        tooltip.add(TextComponents.translation("entangled.entangled_block.info." + key, maxDistance).color(TextFormatting.AQUA).format());

        NBTTagCompound tag = stack.hasTagCompound() ? stack.getTagCompound().getCompoundTag("tileData") : new NBTTagCompound();
        if(tag.hasKey("bound") && tag.getBoolean("bound")){
            int x = tag.getInteger("boundx"), y = tag.getInteger("boundy"), z = tag.getInteger("boundz");
            ITextComponent dimension = TextComponents.dimension(DimensionType.getById(tag.getInteger("dimension"))).color(TextFormatting.GOLD).get();
            ITextComponent name = TextComponents.blockState(Block.getStateById(tag.getInteger("blockstate"))).color(TextFormatting.GOLD).get();
            ITextComponent xText = TextComponents.string(Integer.toString(x)).color(TextFormatting.GOLD).get();
            ITextComponent yText = TextComponents.string(Integer.toString(y)).color(TextFormatting.GOLD).get();
            ITextComponent zText = TextComponents.string(Integer.toString(z)).color(TextFormatting.GOLD).get();
            tooltip.add(TextComponents.translation("entangled.entangled_block.info.bound", name, xText, yText, zText, dimension).color(TextFormatting.YELLOW).format());
        }
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand){
        ItemStack stack = placer.getHeldItem(hand);
        NBTTagCompound compound = stack.hasTagCompound() ? stack.getTagCompound().getCompoundTag("tileData") : new NBTTagCompound();
        if(compound.getBoolean("bound")){
            BlockPos pos2 = new BlockPos(compound.getInteger("boundx"), compound.getInteger("boundy"), compound.getInteger("boundz"));
            if(compound.getInteger("dimension") == world.provider.getDimensionType().getId()){
                if(EntangledConfig.maxDistance.get() >= 0 && pos.distanceSq(pos2) > EntangledConfig.maxDistance.get() * EntangledConfig.maxDistance.get()){
                    if(placer instanceof EntityPlayer && !world.isRemote)
                        placer.sendMessage(TextComponents.translation("entangled.entangled_block.too_far").color(TextFormatting.RED).get());
                    return null;
                }
            }else{
                if(!EntangledConfig.allowDimensional.get()){
                    if(placer instanceof EntityPlayer && !world.isRemote)
                        placer.sendMessage(TextComponents.translation("entangled.entangled_block.wrong_dimension").color(TextFormatting.RED).get());
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
        return entity instanceof EntangledBlockTile ? ((EntangledBlockTile)entity).getAnalogOutputSignal() : 0;
    }

    @Override
    public boolean canProvidePower(IBlockState state){
        return true;
    }

    @Override
    public int getWeakPower(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing direction){
        TileEntity entity = world.getTileEntity(pos);
        return entity instanceof EntangledBlockTile ? ((EntangledBlockTile)entity).getRedstoneSignal(direction) : 0;
    }

    @Override
    public int getStrongPower(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing direction){
        TileEntity entity = world.getTileEntity(pos);
        return entity instanceof EntangledBlockTile ? ((EntangledBlockTile)entity).getDirectRedstoneSignal(direction) : 0;
    }
}
