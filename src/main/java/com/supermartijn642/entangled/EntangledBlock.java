package com.supermartijn642.entangled;

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
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class EntangledBlock extends BaseBlock {

    public static final PropertyBool ON = PropertyBool.create("on");

    public EntangledBlock(){
        super("block", true, Properties.create(new Material(MapColor.BROWN){
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
            playerIn.sendMessage(new TextComponentTranslation("entangled.entangled_block.unbind").setStyle(new Style().setColor(TextFormatting.YELLOW)));
            worldIn.setBlockState(pos, state.withProperty(ON, false));
        }else if(stack != null && stack.getItem() == Entangled.item){
            NBTTagCompound compound = stack.getTagCompound();
            if(compound == null || !compound.getBoolean("bound"))
                playerIn.sendMessage(new TextComponentTranslation("entangled.entangled_block.no_selection").setStyle(new Style().setColor(TextFormatting.RED)));
            else{
                BlockPos pos2 = new BlockPos(compound.getInteger("boundx"), compound.getInteger("boundy"), compound.getInteger("boundz"));
                if(pos2.equals(pos))
                    playerIn.sendMessage(new TextComponentTranslation("entangled.entangled_block.self").setStyle(new Style().setColor(TextFormatting.RED)));
                else{
                    if(!worldIn.getBlockState(pos).getValue(ON))
                        worldIn.setBlockState(pos, state.withProperty(ON, true));
                    EntangledBlockTile tile = (EntangledBlockTile)worldIn.getTileEntity(pos);
                    if(compound.getInteger("dimension") == worldIn.provider.getDimensionType().getId()){
                        if(EntangledConfig.maxDistance.get() == -1 || pos.distanceSq(pos2) <= (EntangledConfig.maxDistance.get() + 0.5) * (EntangledConfig.maxDistance.get() + 0.5)){
                            tile.bind(pos2, compound.getInteger("dimension"));
                            playerIn.sendMessage(new TextComponentTranslation("entangled.entangled_block.bind").setStyle(new Style().setColor(TextFormatting.YELLOW)));
                        }else
                            playerIn.sendMessage(new TextComponentTranslation("entangled.entangled_block.too_far").setStyle(new Style().setColor(TextFormatting.RED)));
                    }else{
                        if(EntangledConfig.allowDimensional.get()){
                            tile.bind(pos2, compound.getInteger("dimension"));
                            playerIn.sendMessage(new TextComponentTranslation("entangled.entangled_block.bind").setStyle(new Style().setColor(TextFormatting.YELLOW)));
                        }else
                            playerIn.sendMessage(new TextComponentTranslation("entangled.entangled_block.wrong_dimension").setStyle(new Style().setColor(TextFormatting.RED)));
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
        tooltip.add(new TextComponentTranslation("entangled.entangled_block.info." + key, EntangledConfig.maxDistance.get()).setStyle(new Style().setColor(TextFormatting.AQUA)).getFormattedText());

        NBTTagCompound tag = stack.hasTagCompound() ? stack.getTagCompound().getCompoundTag("tileData") : new NBTTagCompound();
        if(tag.hasKey("bound") && tag.getBoolean("bound")){
            int x = tag.getInteger("boundx"), y = tag.getInteger("boundy"), z = tag.getInteger("boundz");
            String dimension = DimensionType.getById(tag.getInteger("dimension")).getName();
            dimension = dimension.substring(dimension.lastIndexOf(":") + 1);
            dimension = Character.toUpperCase(dimension.charAt(0)) + dimension.substring(1);
            ITextComponent name = new TextComponentTranslation(Block.getStateById(tag.getInteger("blockstate")).getBlock().getUnlocalizedName() + ".name");
            tooltip.add(new TextComponentTranslation("entangled.entangled_block.info.bound", name, x, y, z, dimension).setStyle(new Style().setColor(TextFormatting.YELLOW)).getFormattedText());
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
                        placer.sendMessage(new TextComponentTranslation("entangled.entangled_block.too_far").setStyle(new Style().setColor(TextFormatting.RED)));
                    return null;
                }
            }else{
                if(!EntangledConfig.allowDimensional.get()){
                    if(placer instanceof EntityPlayer && !world.isRemote)
                        placer.sendMessage(new TextComponentTranslation("entangled.entangled_block.wrong_dimension").setStyle(new Style().setColor(TextFormatting.RED)));
                    return null;
                }
            }
            return this.getDefaultState().withProperty(ON, true);
        }
        return this.getDefaultState();
    }
}
