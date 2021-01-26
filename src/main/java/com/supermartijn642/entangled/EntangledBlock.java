package com.supermartijn642.entangled;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Created 2/6/2020 by SuperMartijn642
 */
public class EntangledBlock extends Block {

    public static final BooleanProperty ON = BooleanProperty.create("on");

    public EntangledBlock(){
        super(Properties.create(new Material.Builder(MaterialColor.BROWN).doesNotBlockMovement().build()).harvestTool(ToolType.PICKAXE).sound(SoundType.STONE).hardnessAndResistance(2f));
        this.setRegistryName("block");
        this.setDefaultState(this.getDefaultState().with(ON, false));
    }

    @Override
    public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity playerIn, Hand hand, BlockRayTraceResult result) {
        if(worldIn.isRemote)
            return true;
        ItemStack stack = playerIn.getHeldItem(hand);
        if(playerIn.isSneaking() && stack.isEmpty() && state.get(ON)){
            ((EntangledBlockTile)worldIn.getTileEntity(pos)).bind(null, 0);
            playerIn.sendMessage(new TranslationTextComponent("entangled.entangled_block.unbind").applyTextStyle(TextFormatting.YELLOW));
            worldIn.setBlockState(pos, state.with(ON, false));
        }else if(stack.getItem() == Entangled.item){
            CompoundNBT compound = stack.getTag();
            if(compound == null || !compound.getBoolean("bound"))
                playerIn.sendMessage(new TranslationTextComponent("entangled.entangled_block.no_selection").applyTextStyle(TextFormatting.RED));
            else{
                BlockPos pos2 = new BlockPos(compound.getInt("boundx"), compound.getInt("boundy"), compound.getInt("boundz"));
                if(pos2.equals(pos))
                    playerIn.sendMessage(new TranslationTextComponent("entangled.entangled_block.self").applyTextStyle(TextFormatting.RED));
                else{
                    if(!worldIn.getBlockState(pos).get(ON))
                        worldIn.setBlockState(pos, state.with(ON, true));
                    EntangledBlockTile tile = (EntangledBlockTile)worldIn.getTileEntity(pos);
                    if(compound.getInt("dimension") == worldIn.getDimension().getType().getId()){
                        if(EntangledConfig.maxDistance.get() == -1 || pos.withinDistance(pos2, EntangledConfig.maxDistance.get() + 0.5)){
                            tile.bind(pos2, compound.getInt("dimension"));
                            playerIn.sendMessage(new TranslationTextComponent("entangled.entangled_block.bind").applyTextStyle(TextFormatting.YELLOW));
                        }else
                            playerIn.sendMessage(new TranslationTextComponent("entangled.entangled_block.too_far").applyTextStyle(TextFormatting.RED));
                    }else{
                        if(EntangledConfig.allowDimensional.get()){
                            tile.bind(pos2, compound.getInt("dimension"));
                            playerIn.sendMessage(new TranslationTextComponent("entangled.entangled_block.bind").applyTextStyle(TextFormatting.YELLOW));
                        }else
                            playerIn.sendMessage(new TranslationTextComponent("entangled.entangled_block.wrong_dimension").applyTextStyle(TextFormatting.RED));
                    }
                }
            }
            return true;
        }
        return true;
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

    @Override
    public void addInformation(ItemStack stack, IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn){
        String key = EntangledConfig.allowDimensional.get() ?
            EntangledConfig.maxDistance.get() == -1 ? "infinite_other_dimension" : "ranged_other_dimension" :
            EntangledConfig.maxDistance.get() == -1 ? "infinite_same_dimension" : "ranged_same_dimension";
        tooltip.add(new TranslationTextComponent("entangled.entangled_block.info." + key, EntangledConfig.maxDistance.get()).applyTextStyle(TextFormatting.AQUA));
    }
}
