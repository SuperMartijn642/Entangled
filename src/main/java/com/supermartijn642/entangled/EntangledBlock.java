package com.supermartijn642.entangled;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;

import javax.annotation.Nullable;

/**
 * Created 2/6/2020 by SuperMartijn642
 */
public class EntangledBlock extends Block {

    public static final BooleanProperty ON = BooleanProperty.create("on");

    public EntangledBlock(){
        super(Properties.create(new Material.Builder(MaterialColor.BROWN).doesNotBlockMovement().build()).speedFactor(1f).harvestTool(ToolType.PICKAXE).sound(SoundType.STONE).hardnessAndResistance(2f));
        this.setRegistryName("block");
        this.setDefaultState(this.getDefaultState().with(ON, false));
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity playerIn, Hand hand, BlockRayTraceResult result){
        if(worldIn.isRemote)
            return ActionResultType.PASS;
        ItemStack stack = playerIn.getHeldItem(hand);
        if(playerIn.isCrouching() && stack.isEmpty() && state.get(ON)){
            ((EntangledBlockTile)worldIn.getTileEntity(pos)).bind(null, 0);
            playerIn.sendMessage(new StringTextComponent(TextFormatting.YELLOW + "Block unbound!"));
            worldIn.setBlockState(pos, state.with(ON, false));
        }else if(stack.getItem() == Entangled.item){
            CompoundNBT compound = stack.getTag();
            if(compound == null || !compound.getBoolean("bound"))
                playerIn.sendMessage(new StringTextComponent(TextFormatting.RED + "No block selected!"));
            else{
                BlockPos pos2 = new BlockPos(compound.getInt("boundx"), compound.getInt("boundy"), compound.getInt("boundz"));
                if(pos2.equals(pos))
                    playerIn.sendMessage(new StringTextComponent(TextFormatting.RED + "Can't bind a block to itself!"));
                else{
                    if(!worldIn.getBlockState(pos).get(ON))
                        worldIn.setBlockState(pos, state.with(ON, true));
                    ((EntangledBlockTile)worldIn.getTileEntity(pos)).bind(pos2, compound.getInt("dimension"));
                    playerIn.sendMessage(new StringTextComponent(TextFormatting.YELLOW + "Block bound!"));
                }
            }
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.PASS;
    }

    @Override
    public boolean hasTileEntity(BlockState state){
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world){
        return new EntangledBlockTile();
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block,BlockState> builder){
        builder.add(ON);
    }

    @Override
    public VoxelShape getRenderShape(BlockState state, IBlockReader worldIn, BlockPos pos){
        return state.get(ON) ? VoxelShapes.empty() : VoxelShapes.fullCube();
    }
}
