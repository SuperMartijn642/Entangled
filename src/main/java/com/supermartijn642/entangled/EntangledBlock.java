package com.supermartijn642.entangled;

import com.supermartijn642.core.CommonUtils;
import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.block.BaseBlock;
import com.supermartijn642.core.block.BlockProperties;
import com.supermartijn642.core.block.EntityHoldingBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.function.Consumer;

/**
 * Created 2/6/2020 by SuperMartijn642
 */
public class EntangledBlock extends BaseBlock implements EntityHoldingBlock {

    public static boolean canBindTo(ResourceLocation blockDimension, BlockPos blockPosition, ResourceLocation targetDimension, BlockPos targetPosition){
        // Validate dimension exists
        if(CommonUtils.getLevel(ResourceKey.create(Registries.DIMENSION, targetDimension)) == null)
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
        super(true, BlockProperties.create().mapColor(MapColor.COLOR_BROWN).sound(SoundType.STONE).destroyTime(1).explosionResistance(2));
        this.registerDefaultState(this.defaultBlockState().setValue(STATE_PROPERTY, State.UNBOUND));
    }

    @Override
    protected InteractionFeedback interact(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, Direction hitSide, Vec3 hitLocation){
        BlockEntity entity = level.getBlockEntity(pos);
        if(!(entity instanceof EntangledBlockEntity))
            return InteractionFeedback.PASS;
        ItemStack stack = player.getItemInHand(hand);
        if(player.isCrouching() && stack.isEmpty() && ((EntangledBlockEntity)entity).isBound()){
            if(!level.isClientSide){
                ((EntangledBlockEntity)entity).unbind();
                player.displayClientMessage(TextComponents.translation("entangled.entangled_block.unbind").color(ChatFormatting.YELLOW).get(), true);
            }
            return InteractionFeedback.SUCCESS;
        }else if(stack.getItem() == Entangled.item){
            if(!level.isClientSide){
                if(EntangledBinderItem.isBound(stack)){
                    ResourceLocation targetDimension = EntangledBinderItem.getBoundDimension(stack);
                    BlockPos targetPos = EntangledBinderItem.getBoundPosition(stack);
                    if(canBindTo(level.dimension().location(), pos, targetDimension, targetPos)){
                        ((EntangledBlockEntity)entity).bind(targetPos, targetDimension);
                        player.displayClientMessage(TextComponents.translation("entangled.entangled_block.bind").color(ChatFormatting.YELLOW).get(), true);
                    }else if(CommonUtils.getLevel(ResourceKey.create(Registries.DIMENSION, targetDimension)) == null)
                        player.displayClientMessage(TextComponents.translation("entangled.entangled_binder.unknown_dimension", targetDimension).color(ChatFormatting.RED).get(), true);
                    else if(!level.dimension().location().equals(targetDimension) && !EntangledConfig.allowDimensional.get())
                        player.displayClientMessage(TextComponents.translation("entangled.entangled_block.wrong_dimension").color(ChatFormatting.RED).get(), true);
                    else if(pos.equals(targetPos))
                        player.displayClientMessage(TextComponents.translation("entangled.entangled_block.self").color(ChatFormatting.RED).get(), true);
                    else
                        player.displayClientMessage(TextComponents.translation("entangled.entangled_block.too_far").color(ChatFormatting.RED).get(), true);
                }else
                    player.displayClientMessage(TextComponents.translation("entangled.entangled_block.no_selection").color(ChatFormatting.RED).get(), true);
            }
            return InteractionFeedback.SUCCESS;
        }
        return InteractionFeedback.PASS;
    }

    @Override
    public BlockEntity createNewBlockEntity(BlockPos pos, BlockState state){
        return new EntangledBlockEntity(pos, state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){
        builder.add(STATE_PROPERTY);
    }

    @Override
    public VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context){
        return state.getValue(STATE_PROPERTY).isBound() ? Shapes.empty() : Shapes.block();
    }

    @Override
    protected void appendItemInformation(ItemStack stack, @Nullable BlockGetter level, Consumer<Component> info, boolean advanced){
        String key = EntangledConfig.allowDimensional.get() ?
            EntangledConfig.maxDistance.get() == -1 ? "infinite_other_dimension" : "ranged_other_dimension" :
            EntangledConfig.maxDistance.get() == -1 ? "infinite_same_dimension" : "ranged_same_dimension";
        Component maxDistance = TextComponents.string(Integer.toString(EntangledConfig.maxDistance.get())).color(ChatFormatting.GOLD).get();
        info.accept(TextComponents.translation("entangled.entangled_block.info." + key, maxDistance).color(ChatFormatting.AQUA).get());

        CompoundTag tag = stack.getOrCreateTag().getCompound("tileData");
        if(tag.contains("bound") && tag.getBoolean("bound")){
            int x = tag.getInt("boundx"), y = tag.getInt("boundy"), z = tag.getInt("boundz");
            Component dimension = TextComponents.dimension(ResourceKey.create(Registries.DIMENSION, new ResourceLocation(tag.getString("dimension")))).color(ChatFormatting.GOLD).get();
            Component name = TextComponents.blockState(Block.stateById(tag.getInt("blockstate"))).color(ChatFormatting.GOLD).get();
            Component xText = TextComponents.string(Integer.toString(x)).color(ChatFormatting.GOLD).get();
            Component yText = TextComponents.string(Integer.toString(y)).color(ChatFormatting.GOLD).get();
            Component zText = TextComponents.string(Integer.toString(z)).color(ChatFormatting.GOLD).get();
            info.accept(TextComponents.translation("entangled.entangled_block.info.bound", name, xText, yText, zText, dimension).color(ChatFormatting.YELLOW).get());
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context){
        ItemStack stack = context.getItemInHand();
        CompoundTag compound = stack.getOrCreateTag().getCompound("tileData");
        if(compound.getBoolean("bound")){
            ResourceLocation placeDimension = context.getLevel().dimension().location();
            BlockPos placePos = context.getClickedPos();
            ResourceLocation targetDimension = EntangledBinderItem.getBoundDimension(stack);
            BlockPos targetPos = EntangledBinderItem.getBoundPosition(stack);
            if(!canBindTo(placeDimension, placePos, targetDimension, targetPos)){
                Player player = context.getPlayer();
                if(player != null){
                    if(CommonUtils.getLevel(ResourceKey.create(Registries.DIMENSION, targetDimension)) == null)
                        player.displayClientMessage(TextComponents.translation("entangled.entangled_binder.unknown_dimension", targetDimension).color(ChatFormatting.RED).get(), true);
                    else if(!placeDimension.equals(targetDimension) && !EntangledConfig.allowDimensional.get())
                        player.displayClientMessage(TextComponents.translation("entangled.entangled_block.wrong_dimension").color(ChatFormatting.RED).get(), true);
                    else if(placePos.equals(targetPos))
                        player.displayClientMessage(TextComponents.translation("entangled.entangled_block.self").color(ChatFormatting.RED).get(), true);
                    else
                        player.displayClientMessage(TextComponents.translation("entangled.entangled_block.too_far").color(ChatFormatting.RED).get(), true);
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
    public int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos){
        BlockEntity entity = world.getBlockEntity(pos);
        return entity instanceof EntangledBlockEntity ? ((EntangledBlockEntity)entity).getAnalogOutputSignal() : 0;
    }

    @Override
    public boolean isSignalSource(BlockState state){
        return true;
    }

    @Override
    public int getSignal(BlockState state, BlockGetter world, BlockPos pos, Direction direction){
        BlockEntity entity = world.getBlockEntity(pos);
        return entity instanceof EntangledBlockEntity ? ((EntangledBlockEntity)entity).getRedstoneSignal(direction) : 0;
    }

    @Override
    public int getDirectSignal(BlockState state, BlockGetter world, BlockPos pos, Direction direction){
        BlockEntity entity = world.getBlockEntity(pos);
        return entity instanceof EntangledBlockEntity ? ((EntangledBlockEntity)entity).getDirectRedstoneSignal(direction) : 0;
    }

    public enum State implements StringRepresentable {
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
