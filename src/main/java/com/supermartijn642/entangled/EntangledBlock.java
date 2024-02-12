package com.supermartijn642.entangled;

import com.supermartijn642.core.CommonUtils;
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
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * Created 2/6/2020 by SuperMartijn642
 */
public class EntangledBlock extends BaseBlock implements EntityHoldingBlock {

    public static boolean canBindTo(ResourceLocation blockDimension, BlockPos blockPosition, ResourceLocation targetDimension, BlockPos targetPosition){
        // Validate dimension exists
        if(CommonUtils.getLevel(RegistryKey.create(Registry.DIMENSION_REGISTRY, targetDimension)) == null)
            return false;
        // Check dimension
        if(!blockDimension.equals(targetDimension))
            return EntangledConfig.allowDimensional.get();
        // Check not itself
        if(blockPosition.equals(targetPosition))
            return false;
        // Check distance
        int maxDistance = EntangledConfig.maxDistance.get();
        return maxDistance == -1 || blockPosition.closerThan(targetPosition, maxDistance + 0.5);
    }

    public static final EnumProperty<State> STATE_PROPERTY = EnumProperty.create("state", State.class);

    public EntangledBlock(){
        super(true, BlockProperties.create(new Material.Builder(MaterialColor.COLOR_BROWN).noCollider().build()).sound(SoundType.STONE).destroyTime(1).explosionResistance(2));
        this.registerDefaultState(this.defaultBlockState().setValue(STATE_PROPERTY, State.UNBOUND));
    }

    @Override
    protected InteractionFeedback interact(BlockState state, World level, BlockPos pos, PlayerEntity player, Hand hand, Direction hitSide, Vector3d hitLocation){
        TileEntity entity = level.getBlockEntity(pos);
        if(!(entity instanceof EntangledBlockEntity))
            return InteractionFeedback.PASS;
        ItemStack stack = player.getItemInHand(hand);
        if(player.isCrouching() && stack.isEmpty() && ((EntangledBlockEntity)entity).isBound()){
            if(!level.isClientSide){
                ((EntangledBlockEntity)entity).unbind();
                player.displayClientMessage(TextComponents.translation("entangled.entangled_block.unbind").color(TextFormatting.YELLOW).get(), true);
            }
            return InteractionFeedback.SUCCESS;
        }else if(stack.getItem() == Entangled.item){
            if(!level.isClientSide){
                if(EntangledBinderItem.isBound(stack)){
                    ResourceLocation targetDimension = EntangledBinderItem.getBoundDimension(stack);
                    BlockPos targetPos = EntangledBinderItem.getBoundPosition(stack);
                    if(canBindTo(level.dimension().location(), pos, targetDimension, targetPos)){
                        ((EntangledBlockEntity)entity).bind(targetPos, targetDimension);
                        player.displayClientMessage(TextComponents.translation("entangled.entangled_block.bind").color(TextFormatting.YELLOW).get(), true);
                    }else if(CommonUtils.getLevel(RegistryKey.create(Registry.DIMENSION_REGISTRY, targetDimension)) == null)
                        player.displayClientMessage(TextComponents.translation("entangled.entangled_binder.unknown_dimension", targetDimension).color(TextFormatting.RED).get(), true);
                    else if(!level.dimension().location().equals(targetDimension) && !EntangledConfig.allowDimensional.get())
                        player.displayClientMessage(TextComponents.translation("entangled.entangled_block.wrong_dimension").color(TextFormatting.RED).get(), true);
                    else if(pos.equals(targetPos))
                        player.displayClientMessage(TextComponents.translation("entangled.entangled_block.self").color(TextFormatting.RED).get(), true);
                    else
                        player.displayClientMessage(TextComponents.translation("entangled.entangled_block.too_far").color(TextFormatting.RED).get(), true);
                }else
                    player.displayClientMessage(TextComponents.translation("entangled.entangled_block.no_selection").color(TextFormatting.RED).get(), true);
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
        builder.add(STATE_PROPERTY);
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, IBlockReader worldIn, BlockPos pos){
        return state.getValue(STATE_PROPERTY).isBound() ? VoxelShapes.empty() : VoxelShapes.block();
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
            ITextComponent dimension = TextComponents.dimension(RegistryKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(tag.getString("dimension")))).color(TextFormatting.GOLD).get();
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
            ResourceLocation placeDimension = context.getLevel().dimension().location();
            BlockPos placePos = context.getClickedPos();
            ResourceLocation targetDimension = EntangledBinderItem.getBoundDimension(stack);
            BlockPos targetPos = EntangledBinderItem.getBoundPosition(stack);
            if(!canBindTo(placeDimension, placePos, targetDimension, targetPos)){
                PlayerEntity player = context.getPlayer();
                if(player != null){
                    if(CommonUtils.getLevel(RegistryKey.create(Registry.DIMENSION_REGISTRY, targetDimension)) == null)
                        player.displayClientMessage(TextComponents.translation("entangled.entangled_binder.unknown_dimension", targetDimension).color(TextFormatting.RED).get(), true);
                    else if(!placeDimension.equals(targetDimension) && !EntangledConfig.allowDimensional.get())
                        player.displayClientMessage(TextComponents.translation("entangled.entangled_block.wrong_dimension").color(TextFormatting.RED).get(), true);
                    else if(placePos.equals(targetPos))
                        player.displayClientMessage(TextComponents.translation("entangled.entangled_block.self").color(TextFormatting.RED).get(), true);
                    else
                        player.displayClientMessage(TextComponents.translation("entangled.entangled_block.too_far").color(TextFormatting.RED).get(), true);
                }
                return null;
            }
            return this.defaultBlockState().setValue(STATE_PROPERTY, State.BOUND_VALID);
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
    public boolean isSignalSource(BlockState state){
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

    public enum State implements IStringSerializable {
        UNBOUND,
        BOUND_VALID,
        BOUND_INVALID;

        public boolean isBound(){
            return this != UNBOUND;
        }

        @Override
        public String getSerializedName(){
            return this.name().toLowerCase(Locale.ROOT);
        }
    }
}
