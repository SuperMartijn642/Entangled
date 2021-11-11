package com.supermartijn642.entangled;

import com.supermartijn642.core.block.BaseTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created 2/6/2020 by SuperMartijn642
 */
public class EntangledBlockTile extends BaseTileEntity implements ITickableTileEntity {

    private boolean bound = false;
    private BlockPos pos;
    private RegistryKey<World> dimension;
    private BlockState blockState;
    private final int[] redstoneSignal = new int[]{0, 0, 0, 0, 0, 0};
    private final int[] directRedstoneSignal = new int[]{0, 0, 0, 0, 0, 0};
    private int analogOutputSignal = -1;

    public EntangledBlockTile(){
        super(Entangled.tile);
    }

    @Override
    public void tick(){
        if(this.level == null || this.level.isClientSide)
            return;
        if(this.bound && this.pos != null){
            World world = this.getDimension();
            if(world != null && (world.isAreaLoaded(this.pos, 1) || this.blockState == null || this.analogOutputSignal == -1)){
                BlockState state = world.getBlockState(this.pos);
                int analogOutputSignal = state.hasAnalogOutputSignal() ?
                    state.getAnalogOutputSignal(world, this.pos) : 0;

                boolean signalChanged = false;
                for(Direction direction : Direction.values()){
                    int redstoneSignal = state.getSignal(world, this.pos, direction);
                    int directRedstoneSignal = state.getDirectSignal(world, this.pos, direction);
                    if(redstoneSignal != this.redstoneSignal[direction.get3DDataValue()]
                        || directRedstoneSignal != this.directRedstoneSignal[direction.get3DDataValue()]){
                        signalChanged = true;
                        this.redstoneSignal[direction.get3DDataValue()] = redstoneSignal;
                        this.directRedstoneSignal[direction.get3DDataValue()] = directRedstoneSignal;
                    }
                }

                if(state != this.blockState || analogOutputSignal != this.analogOutputSignal || signalChanged){
                    this.blockState = state;
                    this.analogOutputSignal = analogOutputSignal;
                    this.dataChanged();
                    this.level.updateNeighbourForOutputSignal(this.worldPosition, this.getBlockState().getBlock());
                    this.level.updateNeighborsAt(this.worldPosition, this.getBlockState().getBlock());
                }
            }
        }
    }

    public boolean isBound(){
        return this.bound;
    }

    @Nullable
    public BlockPos getBoundBlockPos(){
        return this.pos;
    }

    public RegistryKey<World> getBoundDimension(){
        return this.dimension;
    }

    public BlockState getBoundBlockState(){
        return this.blockState;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability){
        if(this.level == null)
            return LazyOptional.empty();
        if(this.bound){
            if(this.level.isClientSide && this.level.dimension() != this.dimension)
                return LazyOptional.empty();
            World world = this.getDimension();
            if(world != null){
                TileEntity tile = world.getBlockEntity(this.pos);
                if(this.checkTile(tile))
                    return tile.getCapability(capability);
            }
        }
        return LazyOptional.empty();
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing){
        if(this.level == null)
            return LazyOptional.empty();
        if(this.bound){
            if(this.level.isClientSide && this.level.dimension() != this.dimension)
                return LazyOptional.empty();
            World world = this.getDimension();
            if(world != null){
                TileEntity tile = world.getBlockEntity(this.pos);
                if(this.checkTile(tile))
                    return tile.getCapability(capability, facing);
            }
        }
        return LazyOptional.empty();
    }

    public boolean bind(BlockPos pos, String dimension){
        if(!this.canBindTo(pos, dimension))
            return false;
        this.pos = pos == null ? null : new BlockPos(pos);
        this.dimension = dimension == null ? null : RegistryKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(dimension));
        this.bound = pos != null;
        this.level.updateNeighborsAt(this.getBlockPos(), this.getBlockState().getBlock());
        this.dataChanged();
        return true;
    }

    public boolean canBindTo(BlockPos pos, String dimension){
        return (pos == null && dimension == null) ||
            dimension.equals(this.level.dimension().location().toString()) ?
            EntangledConfig.maxDistance.get() == -1 || super.worldPosition.closerThan(pos, EntangledConfig.maxDistance.get() + 0.5) :
            EntangledConfig.allowDimensional.get();
    }

    private World getDimension(){
        if(this.dimension == null)
            return null;
        return this.level.isClientSide ?
            this.level.dimension() == this.dimension ? this.level : null :
            this.level.getServer().getLevel(this.dimension);
    }

    private boolean isTargetLoaded(){
        if(this.level.isClientSide || !this.bound)
            return false;
        World world = this.level.dimension() == this.dimension ?
            this.level : this.level.getServer().getLevel(this.dimension);
        return world != null && world.isLoaded(this.pos);
    }

    public int getRedstoneSignal(Direction side){
        if(!this.bound)
            return 0;
        if(this.isTargetLoaded()){
            World world = this.getDimension();
            return world.getBlockState(this.pos).getSignal(world, this.pos, side);
        }
        return Math.max(this.redstoneSignal[side.get3DDataValue()], 0);
    }

    public int getDirectRedstoneSignal(Direction side){
        if(!this.bound)
            return 0;
        if(this.isTargetLoaded()){
            World world = this.getDimension();
            return world.getBlockState(this.pos).getDirectSignal(world, this.pos, side);
        }
        return Math.max(this.directRedstoneSignal[side.get3DDataValue()], 0);
    }

    public int getAnalogOutputSignal(){
        if(!this.bound)
            return 0;
        if(this.isTargetLoaded()){
            World world = this.getDimension();
            return world.getBlockState(this.pos).getAnalogOutputSignal(world, this.pos);
        }
        return Math.max(this.analogOutputSignal, 0);
    }

    private boolean checkTile(TileEntity tile){
        return tile != null && !(tile instanceof EntangledBlockTile);
    }

    @Override
    public void load(BlockState state, CompoundNBT compound){
        if(compound.contains("bound")){ // Saved on an older version
            CompoundNBT data = new CompoundNBT();
            data.putBoolean("bound", compound.getBoolean("bound"));
            data.putInt("boundx", compound.getInt("boundx"));
            data.putInt("boundy", compound.getInt("boundy"));
            data.putInt("boundz", compound.getInt("boundz"));
            data.putString("dimension", compound.getString("dimension"));
            compound.put("data", data);
        }
        super.load(state, compound);
    }

    @Override
    protected CompoundNBT writeData(){
        CompoundNBT compound = new CompoundNBT();
        if(this.bound){
            compound.putBoolean("bound", true);
            compound.putInt("boundx", this.pos.getX());
            compound.putInt("boundy", this.pos.getY());
            compound.putInt("boundz", this.pos.getZ());
            compound.putString("dimension", this.dimension.location().toString());
            compound.putInt("blockstate", Block.getId(this.blockState));
            for(Direction direction : Direction.values()){
                int index = direction.get3DDataValue();
                compound.putInt("redstoneSignal" + index, this.redstoneSignal[index]);
                compound.putInt("directRedstoneSignal" + index, this.directRedstoneSignal[index]);
            }
            compound.putInt("analogOutputSignal", this.analogOutputSignal);
        }
        return compound;
    }

    @Override
    protected void readData(CompoundNBT compound){
        this.bound = compound.getBoolean("bound");
        if(this.bound){
            this.pos = new BlockPos(compound.getInt("boundx"), compound.getInt("boundy"), compound.getInt("boundz"));
            this.dimension = RegistryKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(compound.getString("dimension")));
            this.blockState = Block.stateById(compound.getInt("blockstate"));
            for(Direction direction : Direction.values()){
                int index = direction.get3DDataValue();
                this.redstoneSignal[index] = compound.getInt("redstoneSignal" + index);
                this.directRedstoneSignal[index] = compound.getInt("directRedstoneSignal" + index);
            }
            this.analogOutputSignal = compound.getInt("analogOutputSignal");
        }
    }
}
