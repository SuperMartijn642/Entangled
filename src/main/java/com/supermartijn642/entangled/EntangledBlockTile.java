package com.supermartijn642.entangled;

import com.supermartijn642.core.block.BaseTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

public class EntangledBlockTile extends BaseTileEntity implements ITickable {

    private boolean bound = false;
    private BlockPos pos;
    private int dimension;
    private IBlockState blockState = Blocks.AIR.getDefaultState();
    private IBlockState lastBlockState = this.blockState;

    @Override
    public void update(){
        if(this.world == null || this.world.isRemote)
            return;
        if(this.bound && this.pos != null){
            World world = DimensionManager.getWorld(this.dimension);
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

    public IBlockState getBoundBlockState(){
        return this.blockState;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing){
        if(this.world == null)
            return false;
        if(this.bound){
            if(this.world.isRemote && this.world.provider.getDimensionType().getId() != this.dimension)
                return false;
            World world = this.getDimension();
            if(world != null){
                TileEntity tile = world.getTileEntity(this.pos);
                if(checkTile(tile))
                    return tile.hasCapability(capability, facing);
            }
        }
        return false;
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing){
        if(this.world == null)
            return null;
        if(this.bound){
            if(this.world.isRemote && this.world.provider.getDimensionType().getId() != this.dimension)
                return null;
            World world = this.getDimension();
            if(world != null){
                TileEntity tile = world.getTileEntity(this.pos);
                if(checkTile(tile))
                    return tile.getCapability(capability, facing);
            }
        }
        return null;
    }

    public boolean bind(BlockPos pos, int dimension){
        if(!canBindTo(pos, dimension))
            return false;
        this.pos = pos == null ? null : new BlockPos(pos);
        this.dimension = dimension;
        this.bound = pos != null;
        this.world.notifyNeighborsOfStateChange(this.getPos(), this.getBlockState().getBlock(), false);
        this.dataChanged();
        return true;
    }

    public boolean canBindTo(BlockPos pos, int dimension){
        return dimension == this.world.provider.getDimensionType().getId() ?
            EntangledConfig.maxDistance.get() == -1 || super.pos.distanceSq(pos) <= (EntangledConfig.maxDistance.get() + 0.5) * (EntangledConfig.maxDistance.get() + 0.5) :
            EntangledConfig.allowDimensional.get();
    }

    private World getDimension(){
        return this.world.isRemote ?
            this.world.provider.getDimensionType().getId() == this.dimension ? this.world : null :
            DimensionManager.getWorld(this.dimension);
    }

    private boolean checkTile(TileEntity tile){
        return tile != null && !(tile instanceof EntangledBlockTile);
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
            compound.setInteger("boundx", this.pos.getX());
            compound.setInteger("boundy", this.pos.getY());
            compound.setInteger("boundz", this.pos.getZ());
            compound.setInteger("dimension", this.dimension);
            compound.setInteger("blockstate", Block.getStateId(this.blockState));
        }
        return compound;
    }

    @Override
    protected void readData(NBTTagCompound compound){
        this.bound = compound.getBoolean("bound");
        if(this.bound){
            this.pos = new BlockPos(compound.getInteger("boundx"), compound.getInteger("boundy"), compound.getInteger("boundz"));
            this.dimension = compound.getInteger("dimension");
            this.blockState = Block.getStateById(compound.getInteger("blockstate"));
        }
    }
}
