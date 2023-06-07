package com.supermartijn642.entangled;

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
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Created 2/6/2020 by SuperMartijn642
 */
public class EntangledBlock extends BaseBlock implements EntityHoldingBlock {

    public static final BooleanProperty ON = BooleanProperty.create("on");

    public EntangledBlock(){
        super(true, BlockProperties.create().mapColor(MapColor.COLOR_BROWN).sound(SoundType.STONE).destroyTime(1).explosionResistance(2));
        this.registerDefaultState(this.defaultBlockState().setValue(ON, false));
    }

    @Override
    protected InteractionFeedback interact(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, Direction hitSide, Vec3 hitLocation){
        if(level.isClientSide)
            return InteractionFeedback.PASS;
        ItemStack stack = player.getItemInHand(hand);
        if(player.isCrouching() && stack.isEmpty() && state.getValue(ON)){
            ((EntangledBlockEntity)level.getBlockEntity(pos)).bind(null, null);
            player.displayClientMessage(TextComponents.translation("entangled.entangled_block.unbind").color(ChatFormatting.YELLOW).get(), true);
            level.setBlockAndUpdate(pos, state.setValue(ON, false));
            return InteractionFeedback.SUCCESS;
        }else if(stack.getItem() == Entangled.item){
            CompoundTag compound = stack.getTag();
            if(compound == null || !compound.getBoolean("bound"))
                player.displayClientMessage(TextComponents.translation("entangled.entangled_block.no_selection").color(ChatFormatting.RED).get(), true);
            else{
                BlockPos pos2 = new BlockPos(compound.getInt("boundx"), compound.getInt("boundy"), compound.getInt("boundz"));
                if(pos2.equals(pos))
                    player.displayClientMessage(TextComponents.translation("entangled.entangled_block.self").color(ChatFormatting.RED).get(), true);
                else{
                    if(!level.getBlockState(pos).getValue(ON))
                        level.setBlockAndUpdate(pos, state.setValue(ON, true));
                    EntangledBlockEntity tile = (EntangledBlockEntity)level.getBlockEntity(pos);
                    if(compound.getString("dimension").equals(level.dimension().location().toString())){
                        if(EntangledConfig.maxDistance.get() == -1 || pos.closerThan(pos2, EntangledConfig.maxDistance.get() + 0.5)){
                            tile.bind(pos2, compound.getString("dimension"));
                            player.displayClientMessage(TextComponents.translation("entangled.entangled_block.bind").color(ChatFormatting.YELLOW).get(), true);
                        }else
                            player.displayClientMessage(TextComponents.translation("entangled.entangled_block.too_far").color(ChatFormatting.RED).get(), true);
                    }else{
                        if(EntangledConfig.allowDimensional.get()){
                            tile.bind(pos2, compound.getString("dimension"));
                            player.displayClientMessage(TextComponents.translation("entangled.entangled_block.bind").color(ChatFormatting.YELLOW).get(), true);
                        }else
                            player.displayClientMessage(TextComponents.translation("entangled.entangled_block.wrong_dimension").color(ChatFormatting.RED).get(), true);
                    }
                }
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
        builder.add(ON);
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter worldIn, BlockPos pos){
        return state.getValue(ON) ? Shapes.empty() : Shapes.block();
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
            Player player = context.getPlayer();
            BlockPos pos = context.getClickedPos();
            BlockPos pos2 = new BlockPos(compound.getInt("boundx"), compound.getInt("boundy"), compound.getInt("boundz"));
            if(compound.getString("dimension").equals(context.getLevel().dimension().location().toString())){
                if(EntangledConfig.maxDistance.get() >= 0 && !pos.closerThan(pos2, EntangledConfig.maxDistance.get() + 0.5)){
                    if(player != null && !context.getLevel().isClientSide)
                        player.displayClientMessage(TextComponents.translation("entangled.entangled_block.too_far").color(ChatFormatting.RED).get(), true);
                    return null;
                }
            }else{
                if(!EntangledConfig.allowDimensional.get()){
                    if(player != null && !context.getLevel().isClientSide)
                        player.displayClientMessage(TextComponents.translation("entangled.entangled_block.wrong_dimension").color(ChatFormatting.RED).get(), true);
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
    public int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos){
        BlockEntity entity = world.getBlockEntity(pos);
        return entity instanceof EntangledBlockEntity ? ((EntangledBlockEntity)entity).getAnalogOutputSignal() : 0;
    }

    @Override
    public boolean isSignalSource(BlockState p_60571_){
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
}
