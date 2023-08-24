package com.supermartijn642.entangled;

import com.supermartijn642.core.block.BaseBlockEntity;
import com.supermartijn642.core.block.TickableBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.SectionPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.chunk.AbstractChunkProvider;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created 2/6/2020 by SuperMartijn642
 */
public class EntangledBlockEntity extends BaseBlockEntity implements TickableBlockEntity {

    private boolean bound = false;
    private BlockPos boundPos;
    private RegistryKey<World> boundDimension;
    private BlockState boundBlockState;
    private TileEntity boundBlockEntity;
    private final int[] redstoneSignal = new int[]{0, 0, 0, 0, 0, 0};
    private final int[] directRedstoneSignal = new int[]{0, 0, 0, 0, 0, 0};
    private int analogOutputSignal = -1;
    // Make sure we don't get in infinite loop when entangled blocks are linked to each other
    private int callDepth = 0;

    public EntangledBlockEntity(){
        super(Entangled.tile);
    }

    private void updateBoundBlockData(boolean forceLoad){
        if(this.level == null || this.level.isClientSide || !this.bound || this.boundPos == null)
            return;

        World level = this.getBoundDimension();
        if(level == null)
            return;
        AbstractChunkProvider chunkSource = level.getChunkSource();
        if(chunkSource instanceof ServerChunkProvider && ((ServerChunkProvider)chunkSource).mainThread != Thread.currentThread())
            return;

        boolean sendUpdate = false;
        if(forceLoad || chunkSource.getChunkNow(SectionPos.blockToSectionCoord(this.boundPos.getX()), SectionPos.blockToSectionCoord(this.boundPos.getZ())) != null){
            // Get the block and entity
            BlockState state = level.getBlockState(this.boundPos);
            TileEntity entity = level.getBlockEntity(this.boundPos);
            // Check redstone stuff
            int analogOutputSignal = state.hasAnalogOutputSignal() ?
                state.getAnalogOutputSignal(level, this.boundPos) : 0;
            boolean signalChanged = false;
            for(Direction direction : Direction.values()){
                int redstoneSignal = state.getSignal(level, this.boundPos, direction);
                int directRedstoneSignal = state.getDirectSignal(level, this.boundPos, direction);
                if(redstoneSignal != this.redstoneSignal[direction.get3DDataValue()]
                    || directRedstoneSignal != this.directRedstoneSignal[direction.get3DDataValue()]){
                    signalChanged = true;
                    this.redstoneSignal[direction.get3DDataValue()] = redstoneSignal;
                    this.directRedstoneSignal[direction.get3DDataValue()] = directRedstoneSignal;
                }
            }

            // Check if anything changed
            if(state != this.boundBlockState || entity != this.boundBlockEntity || analogOutputSignal != this.analogOutputSignal || signalChanged){
                this.boundBlockState = state;
                this.boundBlockEntity = entity;
                this.analogOutputSignal = analogOutputSignal;
                sendUpdate = true;
            }
        }else{
            // If the chunk is not available, just check if the entity is still valid
            if(this.boundBlockEntity != null && this.boundBlockEntity.isRemoved()){
                this.boundBlockEntity = null;
                sendUpdate = true;
            }
        }

        // Update the surrounding blocks
        if(sendUpdate){
            this.dataChanged();
            this.level.updateNeighbourForOutputSignal(this.worldPosition, this.getBlockState().getBlock());
            this.level.updateNeighborsAt(this.worldPosition, this.getBlockState().getBlock());
        }
    }

    @Override
    public void update(){
        boolean forceLoad = this.boundBlockState == null
            || (this.boundBlockEntity == null ? this.boundBlockState.hasTileEntity() : this.boundBlockEntity.isRemoved())
            || this.analogOutputSignal == -1;
        this.updateBoundBlockData(forceLoad);
    }

    public boolean isBound(){
        return this.bound;
    }

    @Nullable
    public BlockPos getBoundBlockPos(){
        return this.boundPos;
    }

    public RegistryKey<World> getBoundDimensionIdentifier(){
        return this.boundDimension;
    }

    public BlockState getBoundBlockState(){
        return this.boundBlockState;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability){
        if(this.bound && this.callDepth < 10){
            if(this.boundBlockEntity == null ? this.boundBlockState == null || this.boundBlockState.hasTileEntity() : this.boundBlockEntity.isRemoved())
                this.updateBoundBlockData(false);
            if(this.boundBlockEntity != null && !this.boundBlockEntity.isRemoved()){
                this.callDepth++;
                LazyOptional<T> value = this.boundBlockEntity.getCapability(capability);
                this.callDepth--;
                return value;
            }
        }
        return LazyOptional.empty();
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing){
        if(this.bound && this.callDepth < 10){
            if(this.boundBlockEntity == null ? this.boundBlockState == null || this.boundBlockState.hasTileEntity() : this.boundBlockEntity.isRemoved())
                this.updateBoundBlockData(false);
            if(this.boundBlockEntity != null && !this.boundBlockEntity.isRemoved()){
                this.callDepth++;
                LazyOptional<T> value = this.boundBlockEntity.getCapability(capability, facing);
                this.callDepth--;
                return value;
            }
        }
        return LazyOptional.empty();
    }

    public boolean bind(BlockPos pos, String dimension){
        if(!this.canBindTo(pos, dimension))
            return false;
        this.boundPos = pos == null ? null : new BlockPos(pos);
        this.boundDimension = dimension == null ? null : RegistryKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(dimension));
        this.bound = pos != null;
        this.boundBlockState = null;
        this.boundBlockEntity = null;
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

    private World getBoundDimension(){
        if(this.boundDimension == null)
            return null;
        return this.level.isClientSide ?
            this.level.dimension() == this.boundDimension ? this.level : null :
            this.level.getServer().getLevel(this.boundDimension);
    }

    private boolean isTargetLoaded(){
        if(this.level.isClientSide || !this.bound)
            return false;
        World level = this.level.dimension() == this.boundDimension ?
            this.level : this.level.getServer().getLevel(this.boundDimension);
        return level != null && level.isLoaded(this.boundPos);
    }

    public int getRedstoneSignal(Direction side){
        if(!this.bound)
            return 0;
        if(this.isTargetLoaded() && this.callDepth < 10){
            this.callDepth++;
            World level = this.getBoundDimension();
            this.redstoneSignal[side.get3DDataValue()] = level.getBlockState(this.boundPos).getSignal(level, this.boundPos, side);
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
            World level = this.getBoundDimension();
            this.directRedstoneSignal[side.get3DDataValue()] = level.getBlockState(this.boundPos).getDirectSignal(level, this.boundPos, side);
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
            World level = this.getBoundDimension();
            this.analogOutputSignal = level.getBlockState(this.boundPos).getAnalogOutputSignal(level, this.boundPos);
            this.callDepth--;
            return Math.max(this.analogOutputSignal, 0);
        }
        return Math.max(this.analogOutputSignal, 0);
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
            compound.putInt("boundx", this.boundPos.getX());
            compound.putInt("boundy", this.boundPos.getY());
            compound.putInt("boundz", this.boundPos.getZ());
            compound.putString("dimension", this.boundDimension.location().toString());
            compound.putInt("blockstate", Block.getId(this.boundBlockState));
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
            this.boundPos = new BlockPos(compound.getInt("boundx"), compound.getInt("boundy"), compound.getInt("boundz"));
            this.boundDimension = RegistryKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(compound.getString("dimension")));
            this.boundBlockState = Block.stateById(compound.getInt("blockstate"));
            for(Direction direction : Direction.values()){
                int index = direction.get3DDataValue();
                this.redstoneSignal[index] = compound.getInt("redstoneSignal" + index);
                this.directRedstoneSignal[index] = compound.getInt("directRedstoneSignal" + index);
            }
            this.analogOutputSignal = compound.getInt("analogOutputSignal");
        }
    }
}
