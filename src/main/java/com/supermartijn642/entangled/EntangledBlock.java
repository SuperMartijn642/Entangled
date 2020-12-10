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
import net.minecraft.util.text.*;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

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

        this.setCreativeTab(CreativeTabs.SEARCH);
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
            playerIn.sendMessage(new TextComponentTranslation("entangled.entangled_block.unbind").setStyle(new Style().setColor(TextFormatting.YELLOW)));
            worldIn.setBlockState(pos, state.withProperty(ON, false));
        }else if(stack != null && stack.getItem() == Entangled.item){
            NBTTagCompound compound = stack.getTagCompound();
            if(compound == null || !compound.getBoolean("bound"))
                playerIn.sendMessage(new TextComponentTranslation("entangled.entangled_block.no_selection").setStyle(new Style().setColor(TextFormatting.RED)));
            else{
                BlockPos pos2 = new BlockPos(compound.getInteger("boundx"),compound.getInteger("boundy"),compound.getInteger("boundz"));
                if(pos2.equals(pos))
                    playerIn.sendMessage(new TextComponentTranslation("entangled.entangled_block.self").setStyle(new Style().setColor(TextFormatting.RED)));
                else{
                    if(!worldIn.getBlockState(pos).getValue(ON))
                        worldIn.setBlockState(pos, state.withProperty(ON, true));
                    EntangledBlockTile tile = (EntangledBlockTile)worldIn.getTileEntity(pos);
                    if(compound.getInteger("dimension") == worldIn.provider.getDimensionType().getId()){
                        if(Entangled.maxDistance == -1 || pos.distanceSq(pos2) <= (Entangled.maxDistance + 0.5) * (Entangled.maxDistance + 0.5)){
                            tile.bind(pos2, compound.getInteger("dimension"));
                            playerIn.sendMessage(new TextComponentTranslation("entangled.entangled_block.bind").setStyle(new Style().setColor(TextFormatting.YELLOW)));
                        }else
                            playerIn.sendMessage(new TextComponentTranslation("entangled.entangled_block.too_far").setStyle(new Style().setColor(TextFormatting.RED)));
                    }else{
                        if(Entangled.allowDimensional){
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

    @Override
    public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn){
        String key = Entangled.allowDimensional ?
            Entangled.maxDistance == -1 ? "infinite_other_dimension" : "ranged_other_dimension" :
            Entangled.maxDistance == -1 ? "infinite_same_dimension" : "ranged_same_dimension";
        tooltip.add(TextFormatting.AQUA + ClientProxy.translate("entangled.entangled_block.info." + key, Entangled.maxDistance));
    }
}
