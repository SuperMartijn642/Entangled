package com.supermartijn642.entangled;

import com.supermartijn642.core.block.BaseBlockEntity;
import com.supermartijn642.core.block.TickableBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

public class EntangledBlockEntity extends BaseBlockEntity implements TickableBlockEntity {

    private boolean bound = false;
    private BlockPos boundPos;
    private int boundDimension;
    private IBlockState boundBlockState;
    private TileEntity boundBlockEntity;
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
        if(this.world == null || this.world.isRemote)
            return;
        if(this.bound && this.boundPos != null){
            World level = this.getBoundDimension();
            if(level != null && (level.isBlockLoaded(this.boundPos) || this.boundBlockState == null || this.analogOutputSignal == -1)){
                IBlockState state = level.getBlockState(this.boundPos);
                int analogOutputSignal = state.hasComparatorInputOverride() ?
                    state.getComparatorInputOverride(level, this.boundPos) : 0;

                boolean signalChanged = false;
                for(EnumFacing direction : EnumFacing.values()){
                    int redstoneSignal = state.getWeakPower(level, this.boundPos, direction);
                    int directRedstoneSignal = state.getStrongPower(level, this.boundPos, direction);
                    if(redstoneSignal != this.redstoneSignal[direction.getIndex()]
                        || directRedstoneSignal != this.directRedstoneSignal[direction.getIndex()]){
                        signalChanged = true;
                        this.redstoneSignal[direction.getIndex()] = redstoneSignal;
                        this.directRedstoneSignal[direction.getIndex()] = directRedstoneSignal;
                    }
                }

                boolean entityChanged = false;
                TileEntity entity = level.getTileEntity(this.boundPos);
                if(entity != this.boundBlockEntity){
                    entityChanged = true;
                    this.boundBlockEntity = entity;
                }

                if(state != this.boundBlockState || analogOutputSignal != this.analogOutputSignal || signalChanged || entityChanged || this.shouldUpdateOnceLoaded){
                    this.boundBlockState = state;
                    this.analogOutputSignal = analogOutputSignal;
                    this.dataChanged();
                    this.world.updateComparatorOutputLevel(this.getPos(), this.getBlockState().getBlock());
                    this.world.notifyNeighborsOfStateChange(this.getPos(), this.getBlockState().getBlock(), false);
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
        return this.boundPos;
    }

    public int getBoundDimensionIdentifier(){
        return this.boundDimension;
    }

    public IBlockState getBoundBlockState(){
        return this.boundBlockState;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing){
        if(this.bound && this.boundBlockEntity != null && this.callDepth < 10){
            if(!this.boundBlockEntity.isInvalid()){
                this.callDepth++;
                boolean value = this.boundBlockEntity.hasCapability(capability, facing);
                this.callDepth--;
                return value;
            }else{
                this.boundBlockEntity = null;
                this.shouldUpdateOnceLoaded = true;
            }
        }
        return false;
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing){
        if(this.bound && this.boundBlockEntity != null && this.callDepth < 10){
            if(!this.boundBlockEntity.isInvalid()){
                this.callDepth++;
                T value = this.boundBlockEntity.getCapability(capability, facing);
                this.callDepth--;
                return value;
            }else{
                this.boundBlockEntity = null;
                this.shouldUpdateOnceLoaded = true;
            }
        }
        return null;
    }

    public boolean bind(BlockPos pos, int dimension){
        if(!this.canBindTo(pos, dimension))
            return false;
        this.boundPos = pos == null ? null : new BlockPos(pos);
        this.boundDimension = dimension;
        this.bound = pos != null;
        this.boundBlockState = null;
        this.world.notifyNeighborsOfStateChange(this.getPos(), this.getBlockState().getBlock(), false);
        this.dataChanged();
        return true;
    }

    public boolean canBindTo(BlockPos pos, int dimension){
        return (pos == null && dimension == 0) ||
            dimension == this.world.provider.getDimensionType().getId() ?
            EntangledConfig.maxDistance.get() == -1 || super.pos.distanceSq(pos) <= (EntangledConfig.maxDistance.get() + 0.5) * (EntangledConfig.maxDistance.get() + 0.5) :
            EntangledConfig.allowDimensional.get();
    }

    private World getBoundDimension(){
        return this.world.isRemote ?
            this.world.provider.getDimensionType().getId() == this.boundDimension ? this.world : null :
            DimensionManager.getWorld(this.boundDimension);
    }

    private boolean isTargetLoaded(){
        if(this.world.isRemote || !this.bound)
            return false;
        World level = this.world.provider.getDimensionType().getId() == this.boundDimension ?
            this.world : DimensionManager.getWorld(this.boundDimension);
        return level != null && level.isBlockLoaded(this.boundPos);
    }

    public int getRedstoneSignal(EnumFacing side){
        if(!this.bound)
            return 0;
        if(this.isTargetLoaded() && this.callDepth < 10){
            this.callDepth++;
            World level = this.getBoundDimension();
            this.redstoneSignal[side.getIndex()] = level.getBlockState(this.boundPos).getWeakPower(level, this.boundPos, side);
            this.callDepth--;
            return Math.max(this.redstoneSignal[side.getIndex()], 0);
        }
        return Math.max(this.redstoneSignal[side.getIndex()], 0);
    }

    public int getDirectRedstoneSignal(EnumFacing side){
        if(!this.bound)
            return 0;
        if(this.isTargetLoaded() && this.callDepth < 10){
            this.callDepth++;
            World level = this.getBoundDimension();
            this.directRedstoneSignal[side.getIndex()] = level.getBlockState(this.boundPos).getStrongPower(level, this.boundPos, side);
            this.callDepth--;
            return Math.max(this.directRedstoneSignal[side.getIndex()], 0);
        }
        return Math.max(this.directRedstoneSignal[side.getIndex()], 0);
    }

    public int getAnalogOutputSignal(){
        if(!this.bound)
            return 0;
        if(this.isTargetLoaded() && this.callDepth < 10){
            this.callDepth++;
            World level = this.getBoundDimension();
            this.analogOutputSignal = level.getBlockState(this.boundPos).getComparatorInputOverride(level, this.boundPos);
            this.callDepth--;
            return Math.max(this.analogOutputSignal, 0);
        }
        return Math.max(this.analogOutputSignal, 0);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound){
        if(compound.hasKey("bound")){ // Saved on an older version
            NBTTagCompound data = new NBTTagCompound();
            data.setBoolean("bound", compound.getBoolean("bound"));
            data.setInteger("boundx", compound.getInteger("boundx"));
            data.setInteger("boundy", compound.getInteger("boundy"));
            data.setInteger("boundz", compound.getInteger("boundz"));
            data.setInteger("dimension", compound.getInteger("dimension"));
            compound.setTag("data", data);
        }
        super.readFromNBT(compound);
    }

    @Override
    protected NBTTagCompound writeData(){
        NBTTagCompound compound = new NBTTagCompound();
        if(this.bound){
            compound.setBoolean("bound", true);
            compound.setInteger("boundx", this.boundPos.getX());
            compound.setInteger("boundy", this.boundPos.getY());
            compound.setInteger("boundz", this.boundPos.getZ());
            compound.setInteger("dimension", this.boundDimension);
            if(this.boundBlockState != null)
                compound.setInteger("blockstate", Block.getStateId(this.boundBlockState));
            for(EnumFacing direction : EnumFacing.values()){
                int index = direction.getIndex();
                compound.setInteger("redstoneSignal" + index, this.redstoneSignal[index]);
                compound.setInteger("directRedstoneSignal" + index, this.directRedstoneSignal[index]);
            }
            compound.setInteger("analogOutputSignal", this.analogOutputSignal);
        }
        return compound;
    }

    @Override
    protected void readData(NBTTagCompound compound){
        this.bound = compound.getBoolean("bound");
        if(this.bound){
            this.boundPos = new BlockPos(compound.getInteger("boundx"), compound.getInteger("boundy"), compound.getInteger("boundz"));
            this.boundDimension = compound.getInteger("dimension");
            this.boundBlockState = compound.hasKey("blockstate") ? Block.getStateById(compound.getInteger("blockstate")) : null;
            for(EnumFacing direction : EnumFacing.values()){
                int index = direction.getIndex();
                this.redstoneSignal[index] = compound.getInteger("redstoneSignal" + index);
                this.directRedstoneSignal[index] = compound.getInteger("directRedstoneSignal" + index);
            }
            this.analogOutputSignal = compound.getInteger("analogOutputSignal");
        }
    }
}
