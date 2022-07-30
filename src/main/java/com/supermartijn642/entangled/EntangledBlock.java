package com.supermartijn642.entangled;

import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.block.BaseBlock;
import com.supermartijn642.core.block.BlockProperties;
import com.supermartijn642.core.block.EntityHoldingBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * Created 2/6/2020 by SuperMartijn642
 */
public class EntangledBlock extends BaseBlock implements EntityHoldingBlock {

    public static final BooleanProperty ON = BooleanProperty.create("on");

    public EntangledBlock(){
        super(true, BlockProperties.create(new Material.Builder(MaterialColor.COLOR_BROWN).noCollider().build()).sound(SoundType.STONE).destroyTime(1).explosionResistance(2));
        this.registerDefaultState(this.defaultBlockState().setValue(ON, false));
    }

    @Override
    protected InteractionFeedback interact(BlockState state, World level, BlockPos pos, PlayerEntity player, Hand hand, Direction hitSide, Vec3d hitLocation){
        if(level.isClientSide)
            return InteractionFeedback.PASS;
        ItemStack stack = player.getItemInHand(hand);
        if(player.isCrouching() && stack.isEmpty() && state.getValue(ON)){
            ((EntangledBlockEntity)level.getBlockEntity(pos)).bind(null, 0);
            player.displayClientMessage(TextComponents.translation("entangled.entangled_block.unbind").color(TextFormatting.YELLOW).get(), true);
            level.setBlockAndUpdate(pos, state.setValue(ON, false));
            return InteractionFeedback.SUCCESS;
        }else if(stack.getItem() == Entangled.item){
            CompoundNBT compound = stack.getTag();
            if(compound == null || !compound.getBoolean("bound"))
                player.displayClientMessage(TextComponents.translation("entangled.entangled_block.no_selection").color(TextFormatting.RED).get(), true);
            else{
                BlockPos pos2 = new BlockPos(compound.getInt("boundx"), compound.getInt("boundy"), compound.getInt("boundz"));
                if(pos2.equals(pos))
                    player.displayClientMessage(TextComponents.translation("entangled.entangled_block.self").color(TextFormatting.RED).get(), true);
                else{
                    if(!level.getBlockState(pos).getValue(ON))
                        level.setBlockAndUpdate(pos, state.setValue(ON, true));
                    EntangledBlockEntity tile = (EntangledBlockEntity)level.getBlockEntity(pos);
                    if(compound.getInt("dimension") == level.getDimension().getType().getId()){
                        if(EntangledConfig.maxDistance.get() == -1 || pos.closerThan(pos2, EntangledConfig.maxDistance.get() + 0.5)){
                            tile.bind(pos2, compound.getInt("dimension"));
                            player.displayClientMessage(TextComponents.translation("entangled.entangled_block.bind").color(TextFormatting.YELLOW).get(), true);
                        }else
                            player.displayClientMessage(TextComponents.translation("entangled.entangled_block.too_far").color(TextFormatting.RED).get(), true);
                    }else{
                        if(EntangledConfig.allowDimensional.get()){
                            tile.bind(pos2, compound.getInt("dimension"));
                            player.displayClientMessage(TextComponents.translation("entangled.entangled_block.bind").color(TextFormatting.YELLOW).get(), true);
                        }else
                            player.displayClientMessage(TextComponents.translation("entangled.entangled_block.wrong_dimension").color(TextFormatting.RED).get(), true);
                    }
                }
            }
            return InteractionFeedback.SUCCESS;
        }
        return InteractionFeedback.PASS;
    }

    @Override
    public TileEntity createNewBlockEntity(){
        return new EntangledBlockEntity();
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
    protected void appendItemInformation(ItemStack stack, @Nullable IBlockReader level, Consumer<ITextComponent> info, boolean advanced){
        String key = EntangledConfig.allowDimensional.get() ?
            EntangledConfig.maxDistance.get() == -1 ? "infinite_other_dimension" : "ranged_other_dimension" :
            EntangledConfig.maxDistance.get() == -1 ? "infinite_same_dimension" : "ranged_same_dimension";
        ITextComponent maxDistance = TextComponents.string(Integer.toString(EntangledConfig.maxDistance.get())).color(TextFormatting.GOLD).get();
        info.accept(TextComponents.translation("entangled.entangled_block.info." + key, maxDistance).color(TextFormatting.AQUA).get());

        CompoundNBT tag = stack.getOrCreateTag().getCompound("tileData");
        if(tag.contains("bound") && tag.getBoolean("bound")){
            int x = tag.getInt("boundx"), y = tag.getInt("boundy"), z = tag.getInt("boundz");
            ITextComponent dimension = TextComponents.dimension(DimensionType.getById(tag.getInt("dimension"))).color(TextFormatting.GOLD).get();
            ITextComponent name = TextComponents.blockState(Block.stateById(tag.getInt("blockstate"))).color(TextFormatting.GOLD).get();
            ITextComponent xText = TextComponents.string(Integer.toString(x)).color(TextFormatting.GOLD).get();
            ITextComponent yText = TextComponents.string(Integer.toString(y)).color(TextFormatting.GOLD).get();
            ITextComponent zText = TextComponents.string(Integer.toString(z)).color(TextFormatting.GOLD).get();
            info.accept(TextComponents.translation("entangled.entangled_block.info.bound", name, xText, yText, zText, dimension).color(TextFormatting.YELLOW).get());
        }
    }

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

    @Override
    public boolean hasAnalogOutputSignal(BlockState state){
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, World world, BlockPos pos){
        TileEntity entity = world.getBlockEntity(pos);
        return entity instanceof EntangledBlockEntity ? ((EntangledBlockEntity)entity).getAnalogOutputSignal() : 0;
    }

    @Override
    public boolean isSignalSource(BlockState p_60571_){
        return true;
    }

    @Override
    public int getSignal(BlockState state, IBlockReader world, BlockPos pos, Direction direction){
        TileEntity entity = world.getBlockEntity(pos);
        return entity instanceof EntangledBlockEntity ? ((EntangledBlockEntity)entity).getRedstoneSignal(direction) : 0;
    }

    @Override
    public int getDirectSignal(BlockState state, IBlockReader world, BlockPos pos, Direction direction){
        TileEntity entity = world.getBlockEntity(pos);
        return entity instanceof EntangledBlockEntity ? ((EntangledBlockEntity)entity).getDirectRedstoneSignal(direction) : 0;
    }
}
