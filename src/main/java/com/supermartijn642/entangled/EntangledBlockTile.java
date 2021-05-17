package com.supermartijn642.entangled;

import com.supermartijn642.core.block.BaseTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
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
public class EntangledBlockTile extends BaseTileEntity implements ITickableTileEntity {

    private boolean bound = false;
    private BlockPos pos;
    private int dimension;
    private BlockState blockState;
    private BlockState lastBlockState;

    public EntangledBlockTile(){
        super(Entangled.tile);
    }

    @Override
    public void tick(){
        if(this.world == null || this.world.isRemote)
            return;
        if(this.bound && this.pos != null){
            World world = DimensionManager.getWorld(this.world.getServer(), DimensionType.getById(this.dimension), true, false);
            if(world != null && (world.isAreaLoaded(this.pos, 1) || this.blockState == null)){
                this.blockState = world.getBlockState(this.pos);
                if(this.blockState != this.lastBlockState){
                    this.lastBlockState = this.blockState;
                    this.dataChanged();
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
        if(this.world == null)
            return LazyOptional.empty();
        if(this.bound){
            if(this.world.isRemote && this.world.getDimension().getType().getId() != this.dimension)
                return LazyOptional.empty();
            World world = this.getDimension();
            if(world != null){
                TileEntity tile = world.getTileEntity(this.pos);
                if(checkTile(tile))
                    return tile.getCapability(capability);
            }
        }
        return LazyOptional.empty();
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing){
        if(this.world == null)
            return LazyOptional.empty();
        if(this.bound){
            if(this.world.isRemote && this.world.getDimension().getType().getId() != this.dimension)
                return LazyOptional.empty();
            World world = this.getDimension();
            if(world != null){
                TileEntity tile = world.getTileEntity(this.pos);
                if(checkTile(tile))
                    return tile.getCapability(capability, facing);
            }
        }
        return LazyOptional.empty();
    }

    public boolean bind(BlockPos pos, int dimension){
        if(!canBindTo(pos, dimension))
            return false;
        this.pos = pos == null ? null : new BlockPos(pos);
        this.dimension = dimension;
        this.bound = pos != null;
        this.world.notifyNeighborsOfStateChange(this.getPos(), this.getBlockState().getBlock());
        this.dataChanged();
        return true;
    }

    public boolean canBindTo(BlockPos pos, int dimension){
        return dimension == this.world.getDimension().getType().getId() ?
            EntangledConfig.maxDistance.get() == -1 || super.pos.withinDistance(pos, EntangledConfig.maxDistance.get() + 0.5) :
            EntangledConfig.allowDimensional.get();
    }

    private World getDimension(){
        return this.world.isRemote ?
            this.world.getDimension().getType().getId() == this.dimension ? this.world : null :
            DimensionManager.getWorld(this.world.getServer(), DimensionType.getById(this.dimension), false, false);
    }

    private boolean checkTile(TileEntity tile){
        return tile != null && !(tile instanceof EntangledBlockTile);
    }

    @Override
    public void read(CompoundNBT compound){
        if(compound.contains("bound")){ // Saved on an older version
            CompoundNBT data = new CompoundNBT();
            data.putBoolean("bound", compound.getBoolean("bound"));
            data.putInt("boundx", compound.getInt("boundx"));
            data.putInt("boundy", compound.getInt("boundy"));
            data.putInt("boundz", compound.getInt("boundz"));
            data.putInt("dimension", compound.getInt("dimension"));
            compound.put("data", data);
        }
        super.read(compound);
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
            compound.putInt("blockstate", Block.getStateId(this.blockState));
        }
        return compound;
    }

    @Override
    protected void readData(CompoundNBT compound){
        this.bound = compound.getBoolean("bound");
        if(this.bound){
            this.pos = new BlockPos(compound.getInt("boundx"), compound.getInt("boundy"), compound.getInt("boundz"));
            this.dimension = compound.getInt("dimension");
            this.blockState = Block.getStateById(compound.getInt("blockstate"));
        }
    }
}
