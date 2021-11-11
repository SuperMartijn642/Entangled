package com.supermartijn642.entangled;

import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.block.BaseBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Created 2/6/2020 by SuperMartijn642
 */
public class EntangledBlock extends BaseBlock implements EntityBlock {

    public static final BooleanProperty ON = BooleanProperty.create("on");

    public EntangledBlock(){
        super("block", true, Properties.of(new Material.Builder(MaterialColor.COLOR_BROWN).noCollider().build()).speedFactor(1f).sound(SoundType.STONE).strength(2f));
        this.registerDefaultState(this.defaultBlockState().setValue(ON, false));
    }

    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player playerIn, InteractionHand hand, BlockHitResult result){
        if(worldIn.isClientSide)
            return InteractionResult.PASS;
        ItemStack stack = playerIn.getItemInHand(hand);
        if(playerIn.isCrouching() && stack.isEmpty() && state.getValue(ON)){
            ((EntangledBlockTile)worldIn.getBlockEntity(pos)).bind(null, null);
            playerIn.displayClientMessage(TextComponents.translation("entangled.entangled_block.unbind").color(ChatFormatting.YELLOW).get(), true);
            worldIn.setBlockAndUpdate(pos, state.setValue(ON, false));
        }else if(stack.getItem() == Entangled.item){
            CompoundTag compound = stack.getTag();
            if(compound == null || !compound.getBoolean("bound"))
                playerIn.displayClientMessage(TextComponents.translation("entangled.entangled_block.no_selection").color(ChatFormatting.RED).get(), true);
            else{
                BlockPos pos2 = new BlockPos(compound.getInt("boundx"), compound.getInt("boundy"), compound.getInt("boundz"));
                if(pos2.equals(pos))
                    playerIn.displayClientMessage(TextComponents.translation("entangled.entangled_block.self").color(ChatFormatting.RED).get(), true);
                else{
                    if(!worldIn.getBlockState(pos).getValue(ON))
                        worldIn.setBlockAndUpdate(pos, state.setValue(ON, true));
                    EntangledBlockTile tile = (EntangledBlockTile)worldIn.getBlockEntity(pos);
                    if(compound.getString("dimension").equals(worldIn.dimension().location().toString())){
                        if(EntangledConfig.maxDistance.get() == -1 || pos.closerThan(pos2, EntangledConfig.maxDistance.get() + 0.5)){
                            tile.bind(pos2, compound.getString("dimension"));
                            playerIn.displayClientMessage(TextComponents.translation("entangled.entangled_block.bind").color(ChatFormatting.YELLOW).get(), true);
                        }else
                            playerIn.displayClientMessage(TextComponents.translation("entangled.entangled_block.too_far").color(ChatFormatting.RED).get(), true);
                    }else{
                        if(EntangledConfig.allowDimensional.get()){
                            tile.bind(pos2, compound.getString("dimension"));
                            playerIn.displayClientMessage(TextComponents.translation("entangled.entangled_block.bind").color(ChatFormatting.YELLOW).get(), true);
                        }else
                            playerIn.displayClientMessage(TextComponents.translation("entangled.entangled_block.wrong_dimension").color(ChatFormatting.RED).get(), true);
                    }
                }
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state){
        return new EntangledBlockTile(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> entityType){
        return entityType == Entangled.tile ?
            (world2, pos, state2, entity) -> ((EntangledBlockTile)entity).tick() : null;
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
    public void appendHoverText(ItemStack stack, BlockGetter worldIn, List<Component> tooltip, TooltipFlag flagIn){
        String key = EntangledConfig.allowDimensional.get() ?
            EntangledConfig.maxDistance.get() == -1 ? "infinite_other_dimension" : "ranged_other_dimension" :
            EntangledConfig.maxDistance.get() == -1 ? "infinite_same_dimension" : "ranged_same_dimension";
        Component maxDistance = TextComponents.string(Integer.toString(EntangledConfig.maxDistance.get())).color(ChatFormatting.GOLD).get();
        tooltip.add(TextComponents.translation("entangled.entangled_block.info." + key, maxDistance).color(ChatFormatting.AQUA).get());

        CompoundTag tag = stack.getOrCreateTag().getCompound("tileData");
        if(tag.contains("bound") && tag.getBoolean("bound")){
            int x = tag.getInt("boundx"), y = tag.getInt("boundy"), z = tag.getInt("boundz");
            Component dimension = TextComponents.dimension(ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(tag.getString("dimension")))).color(ChatFormatting.GOLD).get();
            Component name = TextComponents.blockState(Block.stateById(tag.getInt("blockstate"))).color(ChatFormatting.GOLD).get();
            Component xText = TextComponents.string(Integer.toString(x)).color(ChatFormatting.GOLD).get();
            Component yText = TextComponents.string(Integer.toString(y)).color(ChatFormatting.GOLD).get();
            Component zText = TextComponents.string(Integer.toString(z)).color(ChatFormatting.GOLD).get();
            tooltip.add(TextComponents.translation("entangled.entangled_block.info.bound", name, xText, yText, zText, dimension).color(ChatFormatting.YELLOW).get());
        }
    }

    @Nullable
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
        return entity instanceof EntangledBlockTile ? ((EntangledBlockTile)entity).getAnalogOutputSignal() : 0;
    }

    @Override
    public boolean isSignalSource(BlockState p_60571_){
        return true;
    }

    @Override
    public int getSignal(BlockState state, BlockGetter world, BlockPos pos, Direction direction){
        BlockEntity entity = world.getBlockEntity(pos);
        return entity instanceof EntangledBlockTile ? ((EntangledBlockTile)entity).getRedstoneSignal(direction) : 0;
    }

    @Override
    public int getDirectSignal(BlockState state, BlockGetter world, BlockPos pos, Direction direction){
        BlockEntity entity = world.getBlockEntity(pos);
        return entity instanceof EntangledBlockTile ? ((EntangledBlockTile)entity).getDirectRedstoneSignal(direction) : 0;
    }
}
