package com.supermartijn642.entangled;

import com.supermartijn642.core.block.BaseBlockEntity;
import com.supermartijn642.core.block.TickableBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created 2/6/2020 by SuperMartijn642
 */
public class EntangledBlockEntity extends BaseBlockEntity implements TickableBlockEntity {

    private boolean bound = false;
    private BlockPos pos;
    private int dimension;
    private BlockState blockState;
    private final int[] redstoneSignal = new int[]{0, 0, 0, 0, 0, 0};
    private final int[] directRedstoneSignal = new int[]{0, 0, 0, 0, 0, 0};
    private int analogOutputSignal = -1;
    private boolean shouldUpdateOnceLoaded = false;
    // Make sure we don't get in infinite loop when entangled blocks are linked to each other
    private int callDepth = 0;

    public EntangledBlockEntity(){
        super(Entangled.tile);
    }

    @Override
    public void update(){
        if(this.level == null || this.level.isClientSide)
            return;
        if(this.bound && this.pos != null){
            World level = this.getDimension();
            if(level != null && (level.hasChunkAt(this.pos) || this.blockState == null || this.analogOutputSignal == -1)){
                BlockState state = level.getBlockState(this.pos);
                int analogOutputSignal = state.hasAnalogOutputSignal() ?
                    state.getAnalogOutputSignal(level, this.pos) : 0;

                boolean signalChanged = false;
                for(Direction direction : Direction.values()){
                    int redstoneSignal = state.getSignal(level, this.pos, direction);
                    int directRedstoneSignal = state.getDirectSignal(level, this.pos, direction);
                    if(redstoneSignal != this.redstoneSignal[direction.get3DDataValue()]
                        || directRedstoneSignal != this.directRedstoneSignal[direction.get3DDataValue()]){
                        signalChanged = true;
                        this.redstoneSignal[direction.get3DDataValue()] = redstoneSignal;
                        this.directRedstoneSignal[direction.get3DDataValue()] = directRedstoneSignal;
                    }
                }

                if(state != this.blockState || analogOutputSignal != this.analogOutputSignal || signalChanged || this.shouldUpdateOnceLoaded){
                    this.blockState = state;
                    this.analogOutputSignal = analogOutputSignal;
                    this.dataChanged();
                    this.level.updateNeighbourForOutputSignal(this.worldPosition, this.getBlockState().getBlock());
                    this.level.updateNeighborsAt(this.worldPosition, this.getBlockState().getBlock());
                    this.shouldUpdateOnceLoaded = false;
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

    public int getBoundDimension(){
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
            if((this.level.isClientSide && this.level.getDimension().getType().getId() != this.dimension) || this.callDepth >= 10)
                return LazyOptional.empty();
            World level = this.getDimension();
            if(level != null && level.hasChunkAt(this.pos)){
                TileEntity entity = level.getBlockEntity(this.pos);
                if(entity != null){
                    this.callDepth++;
                    LazyOptional<T> value = entity.getCapability(capability);
                    this.callDepth--;
                    return value;
                }
            }else
                this.shouldUpdateOnceLoaded = true;
        }
        return LazyOptional.empty();
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing){
        if(this.level == null)
            return LazyOptional.empty();
        if(this.bound){
            if((this.level.isClientSide && this.level.getDimension().getType().getId() != this.dimension) || this.callDepth >= 10)
                return LazyOptional.empty();
            World level = this.getDimension();
            if(level != null && level.hasChunkAt(this.pos)){
                TileEntity entity = level.getBlockEntity(this.pos);
                if(entity != null){
                    this.callDepth++;
                    LazyOptional<T> value = entity.getCapability(capability, facing);
                    this.callDepth--;
                    return value;
                }
            }else
                this.shouldUpdateOnceLoaded = true;
        }
        return LazyOptional.empty();
    }

    public boolean bind(BlockPos pos, int dimension){
        if(!this.canBindTo(pos, dimension))
            return false;
        this.pos = pos == null ? null : new BlockPos(pos);
        this.dimension = dimension;
        this.bound = pos != null;
        this.level.updateNeighborsAt(this.getBlockPos(), this.getBlockState().getBlock());
        this.dataChanged();
        return true;
    }

    public boolean canBindTo(BlockPos pos, int dimension){
        return (pos == null && dimension == 0) ||
            dimension == this.level.getDimension().getType().getId() ?
            EntangledConfig.maxDistance.get() == -1 || super.worldPosition.closerThan(pos, EntangledConfig.maxDistance.get() + 0.5) :
            EntangledConfig.allowDimensional.get();
    }

    private World getDimension(){
        return this.level.isClientSide ?
            this.level.getDimension().getType().getId() == this.dimension ? this.level : null :
            DimensionManager.getWorld(this.level.getServer(), DimensionType.getById(this.dimension), false, false);
    }

    private boolean isTargetLoaded(){
        if(this.level.isClientSide || !this.bound)
            return false;
        World world = this.level.getDimension().getType().getId() == this.dimension ?
            this.level : DimensionManager.getWorld(this.level.getServer(), DimensionType.getById(this.dimension), false, false);
        return world != null && world.isLoaded(this.pos);
    }

    public int getRedstoneSignal(Direction side){
        if(!this.bound)
            return 0;
        if(this.isTargetLoaded() && this.callDepth < 10){
            this.callDepth++;
            World world = this.getDimension();
            this.redstoneSignal[side.get3DDataValue()] = world.getBlockState(this.pos).getSignal(world, this.pos, side);
            this.callDepth--;
            return Math.max(this.redstoneSignal[side.get3DDataValue()], 0);
        }
        return Math.max(this.redstoneSignal[side.get3DDataValue()], 0);
    }

    public int getDirectRedstoneSignal(Direction side){
        if(!this.bound)
            return 0;
        if(this.isTargetLoaded() && this.callDepth < 10){
            this.callDepth++;
            World world = this.getDimension();
            this.directRedstoneSignal[side.get3DDataValue()] = world.getBlockState(this.pos).getDirectSignal(world, this.pos, side);
            this.callDepth--;
            return Math.max(this.directRedstoneSignal[side.get3DDataValue()], 0);
        }
        return Math.max(this.directRedstoneSignal[side.get3DDataValue()], 0);
    }

    public int getAnalogOutputSignal(){
        if(!this.bound)
            return 0;
        if(this.isTargetLoaded() && this.callDepth < 10){
            this.callDepth++;
            World world = this.getDimension();
            this.analogOutputSignal = world.getBlockState(this.pos).getAnalogOutputSignal(world, this.pos);
            this.callDepth--;
            return Math.max(this.analogOutputSignal, 0);
        }
        return Math.max(this.analogOutputSignal, 0);
    }

    @Override
    public void load(CompoundNBT compound){
        if(compound.contains("bound")){ // Saved on an older version
            CompoundNBT data = new CompoundNBT();
            data.putBoolean("bound", compound.getBoolean("bound"));
            data.putInt("boundx", compound.getInt("boundx"));
            data.putInt("boundy", compound.getInt("boundy"));
            data.putInt("boundz", compound.getInt("boundz"));
            data.putInt("dimension", compound.getInt("dimension"));
            compound.put("data", data);
        }
        super.load(compound);
    }

    @Override
    protected CompoundNBT writeData(){
        CompoundNBT compound = new CompoundNBT();
        if(this.bound){
            compound.putBoolean("bound", true);
            compound.putInt("boundx", this.pos.getX());
            compound.putInt("boundy", this.pos.getY());
            compound.putInt("boundz", this.pos.getZ());
            compound.putInt("dimension", this.dimension);
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
            this.dimension = compound.getInt("dimension");
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
