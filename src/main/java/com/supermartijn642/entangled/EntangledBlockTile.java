package com.supermartijn642.entangled;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
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
public class EntangledBlockTile extends TileEntity implements ITickableTileEntity {

    private boolean bound = false;
    private BlockPos pos;
    private RegistryKey<World> dimension;

    public EntangledBlockTile(){
        super(Entangled.tile);
    }

    @Override
    public void tick(){
        if(this.world == null || this.world.isRemote)
            return;
        if(this.bound && this.pos != null){
            World world = this.getDimension();
            if(world != null && (world.isAreaLoaded(this.pos, 1) || this.blockState == null)){
                this.blockState = world.getBlockState(this.pos);
                if(this.blockState != this.blockStateClient)
                    this.world.notifyBlockUpdate(this.getPos(), this.getBlockState(), this.getBlockState(), 2);
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

    @Override
    public CompoundNBT write(CompoundNBT compound){
        super.write(compound);
        compound.putBoolean("bound", this.bound);
        if(this.bound){
            compound.putInt("boundx", this.pos.getX());
            compound.putInt("boundy", this.pos.getY());
            compound.putInt("boundz", this.pos.getZ());
            compound.putString("dimension", this.dimension.func_240901_a_().toString());
        }
        return compound;
    }

    @Override
    public void read(BlockState state, CompoundNBT compound){
        super.read(state, compound);
        this.bound = compound.getBoolean("bound");
        if(this.bound){
            this.pos = new BlockPos(compound.getInt("boundx"), compound.getInt("boundy"), compound.getInt("boundz"));
            this.dimension = RegistryKey.func_240903_a_(Registry.WORLD_KEY, new ResourceLocation(compound.getString("dimension")));
        }
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability){
        if(this.world == null)
            return LazyOptional.empty();
        if(this.bound){
            if(this.world.isRemote && this.world.func_234923_W_() != this.dimension)
                return LazyOptional.empty();
            World world = this.getDimension();
            if(this.world != null){
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
            if(this.world.isRemote && this.world.func_234923_W_() != this.dimension)
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

    public void bind(BlockPos pos, String dimension){
        this.pos = pos == null ? null : new BlockPos(pos);
        this.dimension = dimension == null ? null : RegistryKey.func_240903_a_(Registry.WORLD_KEY, new ResourceLocation(dimension));
        this.bound = pos != null;
        this.world.notifyNeighborsOfStateChange(this.getPos(), this.getBlockState().getBlock());
        this.world.notifyBlockUpdate(this.getPos(), this.getBlockState(), this.getBlockState(), 2);
        this.markDirty();
    }

    private World getDimension(){
        if(this.dimension == null)
            return null;
        return this.world.isRemote ?
            this.world.func_234923_W_() == this.dimension ? this.world : null :
            this.world.getServer().getWorld(this.dimension);
    }

    private boolean checkTile(TileEntity tile){
        return tile != null && !(tile instanceof EntangledBlockTile);
    }

    private boolean boundClient = false;
    private BlockPos posClient;
    private RegistryKey<World> dimensionClient;
    private BlockState blockState;
    private BlockState blockStateClient;

    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket(){
        CompoundNBT compound = new CompoundNBT();
        if(this.boundClient != this.bound){
            compound.putBoolean("bound", this.bound);
            this.boundClient = this.bound;
        }
        if(this.bound && this.pos != null && !this.pos.equals(this.posClient)){
            compound.putInt("posX", this.pos.getX());
            compound.putInt("posY", this.pos.getY());
            compound.putInt("posZ", this.pos.getZ());
            this.posClient = new BlockPos(this.pos);
        }
        if(this.bound && this.dimensionClient != this.dimension){
            compound.putString("dimension", this.dimension.func_240901_a_().toString());
            this.dimensionClient = this.dimension;
        }
        if(this.bound && this.blockState != this.blockStateClient){
            compound.putInt("blockstate", Block.getStateId(this.blockState));
            this.blockStateClient = this.blockState;
        }
        return compound.isEmpty() ? null : new SUpdateTileEntityPacket(this.getPos(), 0, compound);
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt){
        this.handleTag(pkt.getNbtCompound());
    }

    @Override
    public CompoundNBT getUpdateTag(){
        CompoundNBT compound = super.getUpdateTag();
        compound.putBoolean("bound", this.bound);
        if(this.bound){
            if(this.pos != null){
                compound.putInt("posX", this.pos.getX());
                compound.putInt("posY", this.pos.getY());
                compound.putInt("posZ", this.pos.getZ());
            }
            compound.putString("dimension", this.dimension.func_240901_a_().toString());
            if(this.blockState != null)
                compound.putInt("blockstate", Block.getStateId(this.blockState));
        }
        return compound;
    }

    @Override
    public void handleUpdateTag(BlockState state, CompoundNBT tag){
        super.handleUpdateTag(state, tag);
        this.handleTag(tag);
    }

    private void handleTag(CompoundNBT tag){
        if(tag.contains("bound"))
            this.bound = tag.getBoolean("bound");
        if(tag.contains("posX"))
            this.pos = new BlockPos(tag.getInt("posX"), tag.getInt("posY"), tag.getInt("posZ"));
        if(tag.contains("dimension"))
            this.dimension = RegistryKey.func_240903_a_(Registry.WORLD_KEY, new ResourceLocation(tag.getString("dimension")));
        if(tag.contains("blockstate"))
            this.blockState = Block.getStateById(tag.getInt("blockstate"));
    }
}
