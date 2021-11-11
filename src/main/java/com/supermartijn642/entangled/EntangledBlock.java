package com.supermartijn642.entangled;

import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.block.BaseBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
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
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.ToolType;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Created 2/6/2020 by SuperMartijn642
 */
public class EntangledBlock extends BaseBlock {

    public static final BooleanProperty ON = BooleanProperty.create("on");

    public EntangledBlock(){
        super("block", true, Properties.of(new Material.Builder(MaterialColor.COLOR_BROWN).noCollider().build()).speedFactor(1f).harvestTool(ToolType.PICKAXE).sound(SoundType.STONE).strength(2f));
        this.registerDefaultState(this.defaultBlockState().setValue(ON, false));
    }

    @Override
    public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity playerIn, Hand hand, BlockRayTraceResult result){
        if(worldIn.isClientSide)
            return ActionResultType.PASS;
        ItemStack stack = playerIn.getItemInHand(hand);
        if(playerIn.isCrouching() && stack.isEmpty() && state.getValue(ON)){
            ((EntangledBlockTile)worldIn.getBlockEntity(pos)).bind(null, 0);
            playerIn.displayClientMessage(TextComponents.translation("entangled.entangled_block.unbind").color(TextFormatting.YELLOW).get(), true);
            worldIn.setBlockAndUpdate(pos, state.setValue(ON, false));
        }else if(stack.getItem() == Entangled.item){
            CompoundNBT compound = stack.getTag();
            if(compound == null || !compound.getBoolean("bound"))
                playerIn.displayClientMessage(TextComponents.translation("entangled.entangled_block.no_selection").color(TextFormatting.RED).get(), true);
            else{
                BlockPos pos2 = new BlockPos(compound.getInt("boundx"), compound.getInt("boundy"), compound.getInt("boundz"));
                if(pos2.equals(pos))
                    playerIn.displayClientMessage(TextComponents.translation("entangled.entangled_block.self").color(TextFormatting.RED).get(), true);
                else{
                    if(!worldIn.getBlockState(pos).getValue(ON))
                        worldIn.setBlockAndUpdate(pos, state.setValue(ON, true));
                    EntangledBlockTile tile = (EntangledBlockTile)worldIn.getBlockEntity(pos);
                    if(compound.getInt("dimension") == worldIn.getDimension().getType().getId()){
                        if(EntangledConfig.maxDistance.get() == -1 || pos.closerThan(pos2, EntangledConfig.maxDistance.get() + 0.5)){
                            tile.bind(pos2, compound.getInt("dimension"));
                            playerIn.displayClientMessage(TextComponents.translation("entangled.entangled_block.bind").color(TextFormatting.YELLOW).get(), true);
                        }else
                            playerIn.displayClientMessage(TextComponents.translation("entangled.entangled_block.too_far").color(TextFormatting.RED).get(), true);
                    }else{
                        if(EntangledConfig.allowDimensional.get()){
                            tile.bind(pos2, compound.getInt("dimension"));
                            playerIn.displayClientMessage(TextComponents.translation("entangled.entangled_block.bind").color(TextFormatting.YELLOW).get(), true);
                        }else
                            playerIn.displayClientMessage(TextComponents.translation("entangled.entangled_block.wrong_dimension").color(TextFormatting.RED).get(), true);
                    }
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
    protected void createBlockStateDefinition(StateContainer.Builder<Block,BlockState> builder){
        builder.add(ON);
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, IBlockReader worldIn, BlockPos pos){
        return state.getValue(ON) ? VoxelShapes.empty() : VoxelShapes.block();
    }

    @Override
    public void appendHoverText(ItemStack stack, IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn){
        String key = EntangledConfig.allowDimensional.get() ?
            EntangledConfig.maxDistance.get() == -1 ? "infinite_other_dimension" : "ranged_other_dimension" :
            EntangledConfig.maxDistance.get() == -1 ? "infinite_same_dimension" : "ranged_same_dimension";
        ITextComponent maxDistance = TextComponents.string(Integer.toString(EntangledConfig.maxDistance.get())).color(TextFormatting.GOLD).get();
        tooltip.add(TextComponents.translation("entangled.entangled_block.info." + key, maxDistance).color(TextFormatting.AQUA).get());

        CompoundNBT tag = stack.getOrCreateTag().getCompound("tileData");
        if(tag.contains("bound") && tag.getBoolean("bound")){
            int x = tag.getInt("boundx"), y = tag.getInt("boundy"), z = tag.getInt("boundz");
            ITextComponent dimension = TextComponents.dimension(DimensionType.getById(tag.getInt("dimension"))).color(TextFormatting.GOLD).get();
            ITextComponent name = TextComponents.blockState(Block.stateById(tag.getInt("blockstate"))).color(TextFormatting.GOLD).get();
            ITextComponent xText = TextComponents.string(Integer.toString(x)).color(TextFormatting.GOLD).get();
            ITextComponent yText = TextComponents.string(Integer.toString(y)).color(TextFormatting.GOLD).get();
            ITextComponent zText = TextComponents.string(Integer.toString(z)).color(TextFormatting.GOLD).get();
            tooltip.add(TextComponents.translation("entangled.entangled_block.info.bound", name, xText, yText, zText, dimension).color(TextFormatting.YELLOW).get());
        }
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context){
        ItemStack stack = context.getItemInHand();
        CompoundNBT compound = stack.getOrCreateTag().getCompound("tileData");
        if(compound.getBoolean("bound")){
            PlayerEntity player = context.getPlayer();
            BlockPos pos = context.getClickedPos();
            BlockPos pos2 = new BlockPos(compound.getInt("boundx"), compound.getInt("boundy"), compound.getInt("boundz"));
            if(compound.getInt("dimension") == context.getLevel().getDimension().getType().getId()){
                if(EntangledConfig.maxDistance.get() >= 0 && !pos.closerThan(pos2, EntangledConfig.maxDistance.get() + 0.5)){
                    if(player != null && !context.getLevel().isClientSide)
                        player.displayClientMessage(TextComponents.translation("entangled.entangled_block.too_far").color(TextFormatting.RED).get(), true);
                    return null;
                }
            }else{
                if(!EntangledConfig.allowDimensional.get()){
                    if(player != null && !context.getLevel().isClientSide)
                        player.displayClientMessage(TextComponents.translation("entangled.entangled_block.wrong_dimension").color(TextFormatting.RED).get(), true);
                    return null;
                }
            }
            return this.defaultBlockState().setValue(ON, true);
        }
        return this.defaultBlockState();
    }
}
