package com.supermartijn642.entangled;

import com.refinedmods.refinedstorage.capability.NetworkNodeProxyCapability;
import com.supermartijn642.core.CommonUtils;
import com.supermartijn642.core.block.BaseBlockEntity;
import com.supermartijn642.core.block.TickableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created 2/6/2020 by SuperMartijn642
 */
public class EntangledBlockEntity extends BaseBlockEntity implements TickableBlockEntity {

    public static final TagKey<Block> BLACKLISTED_BLOCKS = TagKey.create(Registries.BLOCK, new ResourceLocation("entangled", "invalid_targets"));

    /**
     * Whether the block is bound to a position
     */
    private boolean bound = false;
    /**
     * Whether the block at the bound position is valid
     */
    private boolean valid = false;
    private boolean revalidate = false;
    private BlockPos boundPos;
    private ResourceKey<Level> boundDimension;
    private BlockState boundBlockState;
    private BlockEntity boundBlockEntity;
    private final int[] redstoneSignal = new int[]{0, 0, 0, 0, 0, 0};
    private final int[] directRedstoneSignal = new int[]{0, 0, 0, 0, 0, 0};
    private int analogOutputSignal = -1;
    // Make sure we don't get in infinite loop when entangled blocks are linked to each other
    private int callDepth = 0;

    public EntangledBlockEntity(BlockPos pos, BlockState state){
        super(Entangled.tile, pos, state);
    }

    private void updateBoundBlockData(boolean forceLoad){
        if(this.level == null || !this.bound || this.boundPos == null)
            return;

        Level level = this.getBoundDimension();
        if(level == null)
            return;
        ChunkSource chunkSource = level.getChunkSource();
        if(chunkSource instanceof ServerChunkCache && ((ServerChunkCache)chunkSource).mainThread != Thread.currentThread())
            return;

        boolean sendUpdate = false;
        if(forceLoad || chunkSource.getChunkNow(SectionPos.blockToSectionCoord(this.boundPos.getX()), SectionPos.blockToSectionCoord(this.boundPos.getZ())) != null){
            // Get the block and entity
            BlockState state = level.getBlockState(this.boundPos);
            BlockEntity entity = level.getBlockEntity(this.boundPos);
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
                if(!this.level.isClientSide)
                    this.valid = this.isValidBlock(state);
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
            this.updateStateAndNeighbors();
            this.dataChanged();
        }
    }

    @Override
    public void update(){
        boolean forceLoad = this.boundBlockState == null
            || (this.boundBlockEntity == null ? this.boundBlockState.hasBlockEntity() : this.boundBlockEntity.isRemoved())
            || this.analogOutputSignal == -1;
        this.updateBoundBlockData(forceLoad);
        if(!this.level.isClientSide && this.revalidate){
            if(this.bound){
                this.valid = this.boundBlockState != null && this.isValidBlock(this.boundBlockState);
                this.updateStateAndNeighbors();
            }
            this.revalidate = false;
        }
    }

    private boolean isValidBlock(BlockState state){
        return EntangledConfig.useWhitelist.get() == state.is(BLACKLISTED_BLOCKS);
    }

    public boolean isBound(){
        return this.bound;
    }

    public boolean isBoundAndValid(){
        return this.bound && this.valid;
    }

    @Nullable
    public BlockPos getBoundBlockPos(){
        return this.boundPos;
    }

    public ResourceKey<Level> getBoundDimensionIdentifier(){
        return this.boundDimension;
    }

    public BlockState getBoundBlockState(){
        return this.boundBlockState;
    }

    private boolean isValidCapability(Capability<?> capability){
        return !CommonUtils.isModLoaded("refinedstorage") || capability != NetworkNodeProxyCapability.NETWORK_NODE_PROXY_CAPABILITY;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability){
        if(!this.isValidCapability(capability))
            return LazyOptional.empty();
        if(this.isBoundAndValid() && this.callDepth < 10){
            if(this.boundBlockEntity == null ? this.boundBlockState == null || this.boundBlockState.hasBlockEntity() : this.boundBlockEntity.isRemoved())
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
        if(!this.isValidCapability(capability))
            return LazyOptional.empty();
        if(this.isBoundAndValid() && this.callDepth < 10){
            if(this.boundBlockEntity == null ? this.boundBlockState == null || this.boundBlockState.hasBlockEntity() : this.boundBlockEntity.isRemoved())
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

    public void bind(BlockPos pos, ResourceLocation dimension){
        this.bound = true;
        this.valid = true;
        this.boundPos = new BlockPos(pos);
        this.boundDimension = ResourceKey.create(Registries.DIMENSION, dimension);
        this.boundBlockState = null;
        this.boundBlockEntity = null;
        this.updateStateAndNeighbors();
        this.dataChanged();
    }

    public void unbind(){
        this.bound = false;
        this.valid = true;
        this.boundPos = null;
        this.boundDimension = null;
        this.boundBlockState = null;
        this.boundBlockEntity = null;
        this.updateStateAndNeighbors();
        this.dataChanged();
    }

    private void updateStateAndNeighbors(){
        if(this.level.isClientSide)
            return;
        EntangledBlock.State properState = this.bound && this.valid ? EntangledBlock.State.BOUND_VALID : this.bound ? EntangledBlock.State.BOUND_INVALID : EntangledBlock.State.UNBOUND;
        if(this.getBlockState().getValue(EntangledBlock.STATE_PROPERTY) != properState)
            this.level.setBlockAndUpdate(this.worldPosition, this.getBlockState().setValue(EntangledBlock.STATE_PROPERTY, properState));
        else{
            this.level.updateNeighbourForOutputSignal(this.worldPosition, this.getBlockState().getBlock());
            this.level.updateNeighborsAt(this.getBlockPos(), this.getBlockState().getBlock());
        }
    }

    private Level getBoundDimension(){
        if(!this.isBound() || this.boundDimension == null)
            return null;
        return this.level.isClientSide ?
            this.level.dimension() == this.boundDimension ? this.level : null :
            this.level.getServer().getLevel(this.boundDimension);
    }

    private boolean isTargetLoaded(){
        if(this.level.isClientSide || !this.isBound())
            return false;
        Level level = this.level.dimension() == this.boundDimension ?
            this.level : this.level.getServer().getLevel(this.boundDimension);
        return level != null && level.isLoaded(this.boundPos);
    }

    public int getRedstoneSignal(Direction side){
        if(!this.isBoundAndValid())
            return 0;
        if(this.isTargetLoaded() && this.callDepth < 10){
            this.callDepth++;
            Level level = this.getBoundDimension();
            this.redstoneSignal[side.get3DDataValue()] = level.getBlockState(this.boundPos).getSignal(level, this.boundPos, side);
            this.callDepth--;
            return Math.max(this.redstoneSignal[side.get3DDataValue()], 0);
        }
        return Math.max(this.redstoneSignal[side.get3DDataValue()], 0);
    }

    public int getDirectRedstoneSignal(Direction side){
        if(!this.isBoundAndValid())
            return 0;
        if(this.isTargetLoaded() && this.callDepth < 10){
            this.callDepth++;
            Level level = this.getBoundDimension();
            this.directRedstoneSignal[side.get3DDataValue()] = level.getBlockState(this.boundPos).getDirectSignal(level, this.boundPos, side);
            this.callDepth--;
            return Math.max(this.directRedstoneSignal[side.get3DDataValue()], 0);
        }
        return Math.max(this.directRedstoneSignal[side.get3DDataValue()], 0);
    }

    public int getAnalogOutputSignal(){
        if(!this.isBoundAndValid())
            return 0;
        if(this.isTargetLoaded() && this.callDepth < 10){
            this.callDepth++;
            Level level = this.getBoundDimension();
            this.analogOutputSignal = level.getBlockState(this.boundPos).getAnalogOutputSignal(level, this.boundPos);
            this.callDepth--;
            return Math.max(this.analogOutputSignal, 0);
        }
        return Math.max(this.analogOutputSignal, 0);
    }

    @Override
    public void load(CompoundTag compound){
        if(compound.contains("bound")){ // Saved on an older version
            CompoundTag data = new CompoundTag();
            data.putBoolean("bound", compound.getBoolean("bound"));
            data.putInt("boundx", compound.getInt("boundx"));
            data.putInt("boundy", compound.getInt("boundy"));
            data.putInt("boundz", compound.getInt("boundz"));
            data.putString("dimension", compound.getString("dimension"));
            compound.put("data", data);
        }
        super.load(compound);
    }

    @Override
    protected CompoundTag writeData(){
        CompoundTag compound = new CompoundTag();
        if(this.bound){
            compound.putBoolean("bound", true);
            compound.putBoolean("valid", this.valid);
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
    protected void readData(CompoundTag compound){
        this.bound = compound.getBoolean("bound");
        if(this.bound){
            this.valid = !compound.contains("valid", Tag.TAG_BYTE) || compound.getBoolean("valid");
            this.revalidate = true;
            this.boundPos = new BlockPos(compound.getInt("boundx"), compound.getInt("boundy"), compound.getInt("boundz"));
            this.boundDimension = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(compound.getString("dimension")));
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
