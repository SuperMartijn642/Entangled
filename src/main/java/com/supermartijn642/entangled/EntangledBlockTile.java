package com.supermartijn642.entangled;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

public class EntangledBlockTile extends TileEntity implements ITickable {

    private boolean bound = false;
    private BlockPos pos;
    private int dimension;

    @Override
    public void update(){
        if(this.world == null || this.world.isRemote)
            return;
        if(this.bound && this.pos != null){
            World world = DimensionManager.getWorld(this.dimension);
            if(world != null && (world.isAreaLoaded(this.pos, 1) || this.blockState == null)){
                this.blockState = world.getBlockState(this.pos);
                if(this.blockState != this.blockStateClient){
                    IBlockState state = this.world.getBlockState(this.getPos());
                    this.world.notifyBlockUpdate(this.getPos(), state, state, 2);
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
    public NBTTagCompound writeToNBT(NBTTagCompound compound){
        super.writeToNBT(compound);
        compound.setBoolean("bound", this.bound);
        if(this.bound){
            compound.setInteger("boundx", this.pos.getX());
            compound.setInteger("boundy", this.pos.getY());
            compound.setInteger("boundz", this.pos.getZ());
            compound.setInteger("dimension", this.dimension);
        }
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound){
        super.readFromNBT(compound);
        this.bound = compound.getBoolean("bound");
        if(this.bound){
            this.pos = new BlockPos(compound.getInteger("boundx"), compound.getInteger("boundy"), compound.getInteger("boundz"));
            this.dimension = compound.getInteger("dimension");
        }
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing){
        if(this.world == null)
            return false;
        if(this.bound){
            if(this.world.isRemote && this.world.provider.getDimensionType().getId() != this.dimension)
                return false;
            World world = this.world.isRemote ? this.world : this.getDimension();
            TileEntity tile = world.getTileEntity(this.pos);
            if(checkTile(tile))
                return tile.hasCapability(capability, facing);
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
            World world = this.world.isRemote ? this.world : this.getDimension();
            TileEntity tile = world.getTileEntity(this.pos);
            if(checkTile(tile))
                return tile.getCapability(capability, facing);
        }
        return null;
    }

    public void bind(BlockPos pos, int dimension){
        this.pos = pos == null ? null : new BlockPos(pos);
        this.dimension = dimension;
        this.bound = pos != null;
        IBlockState state = this.world.getBlockState(this.getPos());
        this.world.notifyNeighborsOfStateChange(this.getPos(), state.getBlock(), false);
        this.world.notifyBlockUpdate(this.getPos(), state, state, 2);
        this.markDirty();
    }

    private World getDimension(){
        return DimensionManager.getWorld(this.dimension);
    }

    private boolean checkTile(TileEntity tile){
        return tile != null && !(tile instanceof EntangledBlockTile);
    }

    private boolean boundClient = false;
    private BlockPos posClient;
    private int dimensionClient;
    private IBlockState blockState;
    private IBlockState blockStateClient;

    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket(){
        NBTTagCompound compound = new NBTTagCompound();
        if(this.boundClient != this.bound){
            compound.setBoolean("bound", this.bound);
            this.boundClient = this.bound;
        }
        if(this.bound && this.pos != null && !this.pos.equals(this.posClient)){
            compound.setInteger("posX", this.pos.getX());
            compound.setInteger("posY", this.pos.getY());
            compound.setInteger("posZ", this.pos.getZ());
            this.posClient = new BlockPos(this.pos);
        }
        if(this.bound && this.dimensionClient != this.dimension){
            compound.setInteger("dimension", this.dimension);
            this.dimensionClient = this.dimension;
        }
        if(this.bound && this.blockState != this.blockStateClient){
            compound.setInteger("blockstate", Block.getStateId(this.blockState));
            this.blockStateClient = this.blockState;
        }
        return compound.hasNoTags() ? null : new SPacketUpdateTileEntity(this.getPos(), 0, compound);
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt){
        this.handleTag(pkt.getNbtCompound());
    }

    @Override
    public NBTTagCompound getUpdateTag(){
        NBTTagCompound compound = super.getUpdateTag();
        compound.setBoolean("bound", this.bound);
        if(this.bound){
            if(this.pos != null){
                compound.setInteger("posX", this.pos.getX());
                compound.setInteger("posY", this.pos.getY());
                compound.setInteger("posZ", this.pos.getZ());
            }
            compound.setInteger("dimension", this.dimension);
            if(this.blockState != null)
                compound.setInteger("blockstate", Block.getStateId(this.blockState));
        }
        return compound;
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag){
        super.handleUpdateTag(tag);
        this.handleTag(tag);
    }

    private void handleTag(NBTTagCompound tag){
        if(tag.hasKey("bound"))
            this.bound = tag.getBoolean("bound");
        if(tag.hasKey("posX"))
            this.pos = new BlockPos(tag.getInteger("posX"), tag.getInteger("posY"), tag.getInteger("posZ"));
        if(tag.hasKey("dimension"))
            this.dimension = tag.getInteger("dimension");
        if(tag.hasKey("blockstate"))
            this.blockState = Block.getStateById(tag.getInteger("blockstate"));
    }
}
