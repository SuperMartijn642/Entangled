package com.supermartijn642.entangled;

import com.supermartijn642.core.block.BaseBlockEntity;
import com.supermartijn642.core.block.TickableBlockEntity;
import com.supermartijn642.core.data.TagLoader;
import com.supermartijn642.core.registry.Registries;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;

public class EntangledBlockEntity extends BaseBlockEntity implements TickableBlockEntity {

    public static final ResourceLocation BLACKLISTED_BLOCKS = new ResourceLocation("entangled", "invalid_targets");

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
    private int boundDimension;
    private IBlockState boundBlockState;
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
        if(this.world == null || !this.bound || this.boundPos == null)
            return;

        World level = this.getBoundDimension();
        if(level == null)
            return;

        boolean sendUpdate = false;
        if(level.isBlockLoaded(this.boundPos) || forceLoad){
            // Get the block and entity
            IBlockState state = level.getBlockState(this.boundPos);
            TileEntity entity = level.getTileEntity(this.boundPos);
            // Check redstone stuff
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

            // Check if anything changed
            if(state != this.boundBlockState || entity != this.boundBlockEntity || analogOutputSignal != this.analogOutputSignal || signalChanged){
                this.boundBlockState = state;
                this.boundBlockEntity = entity;
                this.analogOutputSignal = analogOutputSignal;
                if(!this.world.isRemote)
                    this.valid = this.isValidBlock(state);
                sendUpdate = true;
            }
        }else{
            // If the chunk is not available, just check if the entity is still valid
            if(this.boundBlockEntity != null && this.boundBlockEntity.isInvalid()){
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
            || (this.boundBlockEntity == null ? this.boundBlockState instanceof ITileEntityProvider : this.boundBlockEntity.isInvalid())
            || this.analogOutputSignal == -1;
        this.updateBoundBlockData(forceLoad);
        if(!this.world.isRemote && this.revalidate){
            if(this.bound){
                this.valid = this.boundBlockState != null && this.isValidBlock(this.boundBlockState);
                this.updateStateAndNeighbors();
            }
            this.revalidate = false;
        }
    }

    private boolean isValidBlock(IBlockState state){
        return !TagLoader.getTag(Registries.BLOCKS, BLACKLISTED_BLOCKS).contains(Registries.BLOCKS.getIdentifier(state.getBlock()));
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

    public int getBoundDimensionIdentifier(){
        return this.boundDimension;
    }

    public IBlockState getBoundBlockState(){
        return this.boundBlockState;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing){
        if(this.isBoundAndValid() && this.callDepth < 10){
            if(this.boundBlockEntity == null ? this.boundBlockState == null || this.boundBlockState instanceof ITileEntityProvider : this.boundBlockEntity.isInvalid())
                this.updateBoundBlockData(false);
            if(this.boundBlockEntity != null && !this.boundBlockEntity.isInvalid()){
                this.callDepth++;
                boolean value = this.boundBlockEntity.hasCapability(capability, facing);
                this.callDepth--;
                return value;
            }
        }
        return false;
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing){
        if(this.isBoundAndValid() && this.callDepth < 10){
            if(this.boundBlockEntity == null ? this.boundBlockState == null || this.boundBlockState instanceof ITileEntityProvider : this.boundBlockEntity.isInvalid())
                this.updateBoundBlockData(false);
            if(this.boundBlockEntity != null && !this.boundBlockEntity.isInvalid()){
                this.callDepth++;
                T value = this.boundBlockEntity.getCapability(capability, facing);
                this.callDepth--;
                return value;
            }
        }
        return null;
    }

    public void bind(BlockPos pos, int dimension){
        this.bound = true;
        this.valid = true;
        this.boundPos = new BlockPos(pos);
        this.boundDimension = dimension;
        this.boundBlockState = null;
        this.boundBlockEntity = null;
        this.updateStateAndNeighbors();
        this.dataChanged();
    }

    public void unbind(){
        this.bound = false;
        this.valid = true;
        this.boundPos = null;
        this.boundDimension = 0;
        this.boundBlockState = null;
        this.boundBlockEntity = null;
        this.updateStateAndNeighbors();
        this.dataChanged();
    }

    private void updateStateAndNeighbors(){
        if(this.world.isRemote)
            return;
        EntangledBlock.State properState = this.bound && this.valid ? EntangledBlock.State.BOUND_VALID : this.bound ? EntangledBlock.State.BOUND_INVALID : EntangledBlock.State.UNBOUND;
        if(this.getBlockState().getValue(EntangledBlock.STATE_PROPERTY) != properState)
            this.world.setBlockState(this.pos, this.getBlockState().withProperty(EntangledBlock.STATE_PROPERTY, properState));
        else{
            this.world.updateComparatorOutputLevel(this.pos, this.getBlockState().getBlock());
            this.world.notifyNeighborsOfStateChange(this.pos, this.getBlockState().getBlock(), false);
        }
    }

    private World getBoundDimension(){
        if(!this.isBound() || !DimensionManager.isDimensionRegistered(this.boundDimension))
            return null;
        return this.world.isRemote ?
            this.world.provider.getDimension() == this.boundDimension ? this.world : null :
            DimensionManager.getWorld(this.boundDimension);
    }

    private boolean isTargetLoaded(){
        if(this.world.isRemote || !this.isBound())
            return false;
        World level = this.world.provider.getDimension() == this.boundDimension ?
            this.world : DimensionManager.getWorld(this.boundDimension);
        return level != null && level.isBlockLoaded(this.boundPos);
    }

    public int getRedstoneSignal(EnumFacing side){
        if(!this.isBoundAndValid())
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
        if(!this.isBoundAndValid())
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
        if(!this.isBoundAndValid())
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
    public boolean shouldRefresh(World level, BlockPos pos, IBlockState oldState, IBlockState newSate){
        return oldState.getBlock() != newSate.getBlock();
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
            compound.setBoolean("valid", this.valid);
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
            this.valid = !compound.hasKey("valid", Constants.NBT.TAG_BYTE) || compound.getBoolean("valid");
            this.revalidate = true;
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
