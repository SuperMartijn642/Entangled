package com.supermartijn642.entangled;

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
public class EntangledBlockTile extends TileEntity {

    private boolean bound = false;
    private BlockPos pos;
    private int dimension;

    public EntangledBlockTile(){
        super(Entangled.tile);
    }

    @Override
    public CompoundNBT write(CompoundNBT compound){
        super.write(compound);
        compound.putBoolean("bound", this.bound);
        if(this.bound){
            compound.putInt("boundx", this.pos.getX());
            compound.putInt("boundy", this.pos.getY());
            compound.putInt("boundz", this.pos.getZ());
            compound.putInt("dimension", this.dimension);
        }
        return compound;
    }

    @Override
    public void read(CompoundNBT compound){
        super.read(compound);
        this.bound = compound.getBoolean("bound");
        if(this.bound){
            this.pos = new BlockPos(compound.getInt("boundx"), compound.getInt("boundy"), compound.getInt("boundz"));
            this.dimension = compound.getInt("dimension");
        }
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability){
        if(this.world == null)
            return LazyOptional.empty();
        if(this.bound){
            if(this.world.isRemote && this.world.getDimension().getType().getId() != this.dimension)
                return LazyOptional.empty();
            TileEntity tile = this.getDimension().getTileEntity(this.pos);
            if(checkTile(tile))
                return tile.getCapability(capability);
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
            TileEntity tile = this.getDimension().getTileEntity(this.pos);
            if(checkTile(tile))
                return tile.getCapability(capability, facing);
        }
        return LazyOptional.empty();
    }

    public void bind(BlockPos pos, int dimension){
        this.pos = pos == null ? null : new BlockPos(pos);
        this.dimension = dimension;
        this.bound = pos != null;
        if(this.bound){
            this.world.notifyNeighborsOfStateChange(this.pos, this.getBlockState().getBlock());
            this.world.notifyBlockUpdate(this.getPos(), this.getBlockState(), this.getBlockState(), 2);
        }
    }

    private World getDimension(){
        return DimensionManager.getWorld(this.world.getServer(), DimensionType.getById(this.dimension), false, false);
    }

    private boolean checkTile(TileEntity tile){
        return tile != null && !(tile instanceof EntangledBlockTile);
    }

}
