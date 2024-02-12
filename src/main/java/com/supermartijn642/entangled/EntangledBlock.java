package com.supermartijn642.entangled;

import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.block.BaseBlock;
import com.supermartijn642.core.block.BlockProperties;
import com.supermartijn642.core.block.EntityHoldingBlock;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.DimensionType;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

import javax.annotation.Nullable;
import java.util.Locale;
import java.util.function.Consumer;

public class EntangledBlock extends BaseBlock implements EntityHoldingBlock {

    public static boolean canBindTo(int blockDimension, BlockPos blockPosition, int targetDimension, BlockPos targetPosition){
        // Validate dimension exists
        if(!DimensionManager.isDimensionRegistered(targetDimension))
            return false;
        // Check dimension
        if(blockDimension != targetDimension)
            return EntangledConfig.allowDimensional.get();
        // Check not itself
        if(blockPosition.equals(targetPosition))
            return false;
        // Check distance
        int maxDistance = EntangledConfig.maxDistance.get();
        return maxDistance == -1 || blockPosition.distanceSq(targetPosition) <= (maxDistance + 0.5) * (maxDistance + 0.5);
    }

    public static final PropertyEnum<State> STATE_PROPERTY = PropertyEnum.create("state", State.class);

    public EntangledBlock(){
        super(true, BlockProperties.create(new Material(MapColor.BROWN)).sound(SoundType.STONE).destroyTime(1).explosionResistance(2));
        this.setDefaultState(this.blockState.getBaseState().withProperty(STATE_PROPERTY, State.UNBOUND));
    }

    @Override
    protected InteractionFeedback interact(IBlockState state, World level, BlockPos pos, EntityPlayer player, EnumHand hand, EnumFacing hitSide, Vec3d hitLocation){
        TileEntity entity = level.getTileEntity(pos);
        if(!(entity instanceof EntangledBlockEntity))
            return InteractionFeedback.PASS;
        ItemStack stack = player.getHeldItem(hand);
        if(player.isSneaking() && stack.isEmpty() && ((EntangledBlockEntity)entity).isBound()){
            if(!level.isRemote){
                ((EntangledBlockEntity)entity).unbind();
                player.sendStatusMessage(TextComponents.translation("entangled.entangled_block.unbind").color(TextFormatting.YELLOW).get(), true);
            }
            return InteractionFeedback.SUCCESS;
        }else if(stack.getItem() == Entangled.item){
            if(!level.isRemote){
                if(EntangledBinderItem.isBound(stack)){
                    int targetDimension = EntangledBinderItem.getBoundDimension(stack);
                    BlockPos targetPos = EntangledBinderItem.getBoundPosition(stack);
                    if(canBindTo(level.provider.getDimension(), pos, targetDimension, targetPos)){
                        ((EntangledBlockEntity)entity).bind(targetPos, targetDimension);
                        player.sendStatusMessage(TextComponents.translation("entangled.entangled_block.bind").color(TextFormatting.YELLOW).get(), true);
                    }else if(!DimensionManager.isDimensionRegistered(targetDimension))
                        player.sendStatusMessage(TextComponents.translation("entangled.entangled_binder.unknown_dimension", targetDimension).color(TextFormatting.RED).get(), true);
                    else if(level.provider.getDimension() != targetDimension && !EntangledConfig.allowDimensional.get())
                        player.sendStatusMessage(TextComponents.translation("entangled.entangled_block.wrong_dimension").color(TextFormatting.RED).get(), true);
                    else if(pos.equals(targetPos))
                        player.sendStatusMessage(TextComponents.translation("entangled.entangled_block.self").color(TextFormatting.RED).get(), true);
                    else
                        player.sendStatusMessage(TextComponents.translation("entangled.entangled_block.too_far").color(TextFormatting.RED).get(), true);
                }else
                    player.sendStatusMessage(TextComponents.translation("entangled.entangled_block.no_selection").color(TextFormatting.RED).get(), true);
            }
            return InteractionFeedback.SUCCESS;
        }
        return InteractionFeedback.PASS;
    }

    @Override
    public int getMetaFromState(IBlockState state){
        return state.getValue(STATE_PROPERTY).ordinal();
    }

    @Override
    public IBlockState getStateFromMeta(int meta){
        return this.getDefaultState().withProperty(STATE_PROPERTY, State.values()[meta]);
    }

    @Nullable
    @Override
    public TileEntity createNewBlockEntity(){
        return new EntangledBlockEntity();
    }

    @Override
    protected BlockStateContainer createBlockState(){
        return new BlockStateContainer(this, STATE_PROPERTY);
    }

    @Override
    public boolean isOpaqueCube(IBlockState state){
        return false;
    }

    @Override
    protected void appendItemInformation(ItemStack stack, IBlockAccess level, Consumer<ITextComponent> info, boolean advanced){
        String key = EntangledConfig.allowDimensional.get() ?
            EntangledConfig.maxDistance.get() == -1 ? "infinite_other_dimension" : "ranged_other_dimension" :
            EntangledConfig.maxDistance.get() == -1 ? "infinite_same_dimension" : "ranged_same_dimension";
        ITextComponent maxDistance = TextComponents.string(Integer.toString(EntangledConfig.maxDistance.get())).color(TextFormatting.GOLD).get();
        info.accept(TextComponents.translation("entangled.entangled_block.info." + key, maxDistance).color(TextFormatting.AQUA).get());

        NBTTagCompound tag = stack.hasTagCompound() ? stack.getTagCompound().getCompoundTag("tileData") : new NBTTagCompound();
        if(tag.hasKey("bound") && tag.getBoolean("bound")){
            int x = tag.getInteger("boundx"), y = tag.getInteger("boundy"), z = tag.getInteger("boundz");
            ITextComponent dimension = TextComponents.dimension(DimensionType.getById(tag.getInteger("dimension"))).color(TextFormatting.GOLD).get();
            ITextComponent name = TextComponents.blockState(Block.getStateById(tag.getInteger("blockstate"))).color(TextFormatting.GOLD).get();
            ITextComponent xText = TextComponents.string(Integer.toString(x)).color(TextFormatting.GOLD).get();
            ITextComponent yText = TextComponents.string(Integer.toString(y)).color(TextFormatting.GOLD).get();
            ITextComponent zText = TextComponents.string(Integer.toString(z)).color(TextFormatting.GOLD).get();
            info.accept(TextComponents.translation("entangled.entangled_block.info.bound", name, xText, yText, zText, dimension).color(TextFormatting.YELLOW).get());
        }
    }

    @Override
    public IBlockState getStateForPlacement(World level, BlockPos placePos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase player, EnumHand hand){
        ItemStack stack = player.getHeldItem(hand);
        NBTTagCompound compound = stack.hasTagCompound() ? stack.getTagCompound().getCompoundTag("tileData") : new NBTTagCompound();
        if(compound.getBoolean("bound")){
            int placeDimension = level.provider.getDimension();
            int targetDimension = EntangledBinderItem.getBoundDimension(stack);
            BlockPos targetPos = EntangledBinderItem.getBoundPosition(stack);
            if(!canBindTo(placeDimension, placePos, targetDimension, targetPos)){
                if(player instanceof EntityPlayer){
                    if(!DimensionManager.isDimensionRegistered(targetDimension))
                        ((EntityPlayer)player).sendStatusMessage(TextComponents.translation("entangled.entangled_binder.unknown_dimension", targetDimension).color(TextFormatting.RED).get(), true);
                    else if(placeDimension != targetDimension && !EntangledConfig.allowDimensional.get())
                        ((EntityPlayer)player).sendStatusMessage(TextComponents.translation("entangled.entangled_block.wrong_dimension").color(TextFormatting.RED).get(), true);
                    else if(placePos.equals(targetPos))
                        ((EntityPlayer)player).sendStatusMessage(TextComponents.translation("entangled.entangled_block.self").color(TextFormatting.RED).get(), true);
                    else
                        ((EntityPlayer)player).sendStatusMessage(TextComponents.translation("entangled.entangled_block.too_far").color(TextFormatting.RED).get(), true);
                }
                return null;
            }
            return this.getDefaultState().withProperty(STATE_PROPERTY, State.BOUND_VALID);
        }
        return this.getDefaultState();
    }

    @Override
    public boolean hasComparatorInputOverride(IBlockState state){
        return true;
    }

    @Override
    public int getComparatorInputOverride(IBlockState state, World world, BlockPos pos){
        TileEntity entity = world.getTileEntity(pos);
        return entity instanceof EntangledBlockEntity ? ((EntangledBlockEntity)entity).getAnalogOutputSignal() : 0;
    }

    @Override
    public boolean canProvidePower(IBlockState state){
        return true;
    }

    @Override
    public int getWeakPower(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing direction){
        TileEntity entity = world.getTileEntity(pos);
        return entity instanceof EntangledBlockEntity ? ((EntangledBlockEntity)entity).getRedstoneSignal(direction) : 0;
    }

    @Override
    public int getStrongPower(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing direction){
        TileEntity entity = world.getTileEntity(pos);
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
        public String getName(){
            return this.name().toLowerCase(Locale.ROOT);
        }
    }
}
