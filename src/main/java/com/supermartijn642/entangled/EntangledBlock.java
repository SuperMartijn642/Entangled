package com.supermartijn642.entangled;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockFaceShape;
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
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class EntangledBlock extends Block implements ITileEntityProvider {

    public static final PropertyBool ON = PropertyBool.create("on");

    public EntangledBlock(){
        super(new Material(MapColor.BROWN){
            @Override
            public boolean isToolNotRequired(){
                return false;
            }
        });
        this.setRegistryName("block");
        this.setUnlocalizedName(Entangled.MODID + ":block");
        this.setDefaultState(this.blockState.getBaseState().withProperty(ON, false));

        this.setSoundType(SoundType.STONE);
        this.setHarvestLevel("pickaxe",0);
        this.setHardness(2f);
        this.setResistance(2f);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if(worldIn.isRemote)
            return false;
        ItemStack stack = playerIn.getHeldItem(hand);
        if(playerIn.isSneaking() && stack == ItemStack.EMPTY && state.getValue(ON)){
            ((EntangledBlockTile)worldIn.getTileEntity(pos)).bind(null,0);
            playerIn.sendMessage(new TextComponentString(TextFormatting.YELLOW + "Block unbound!"));
            worldIn.setBlockState(pos, state.withProperty(ON, false));
        }
        else if(stack != null && stack.getItem() == Entangled.item){
            NBTTagCompound compound = stack.getTagCompound();
            if(compound == null || !compound.getBoolean("bound"))
                playerIn.sendMessage(new TextComponentString(TextFormatting.RED + "No block selected!"));
            else{
                BlockPos pos2 = new BlockPos(compound.getInteger("boundx"),compound.getInteger("boundy"),compound.getInteger("boundz"));
                if(pos2.equals(pos))
                    playerIn.sendMessage(new TextComponentString(TextFormatting.RED + "Can't bind a block to itself!"));
                else{
                    worldIn.setBlockState(pos, state.withProperty(ON, true));
                    ((EntangledBlockTile)worldIn.getTileEntity(pos)).bind(pos2,compound.getInteger("dimension"));
                    playerIn.sendMessage(new TextComponentString(TextFormatting.YELLOW + "Block bound!"));
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(ON) ? 1 : 0;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(ON, meta == 1);
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new EntangledBlockTile();
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, ON);
    }

    @Override
    public boolean isOpaqueCube(IBlockState state){
        return false;
    }
}
