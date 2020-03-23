package supermartijn642.entangled;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

public class TEEntangledBlock extends TileEntity {

    private boolean bound = false;
    private BlockPos pos;
    private World dimension;

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setBoolean("bound",this.bound);
        if(this.bound){
            compound.setInteger("boundx",this.pos.getX());
            compound.setInteger("boundy",this.pos.getY());
            compound.setInteger("boundz",this.pos.getZ());
            compound.setInteger("dimension",this.dimension.provider.getDimension());
        }
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.bound = compound.getBoolean("bound");
        if(this.bound) {
            this.pos = new BlockPos(compound.getInteger("boundx"), compound.getInteger("boundy"), compound.getInteger("boundz"));
            this.dimension = DimensionManager.getWorld(compound.getInteger("dimension"));
            if(this.dimension == null)
                this.bound = false;
        }
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        if(this.bound && !this.world.isRemote){
            TileEntity tile = this.dimension.getTileEntity(this.pos);
            if(tile != null)
                return tile.hasCapability(capability, facing);
        }
        return false;
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if(this.bound && !this.world.isRemote){
            TileEntity tile = this.dimension.getTileEntity(this.pos);
            if(tile != null)
                return tile.getCapability(capability, facing);
        }
        return null;
    }

    public void bind(BlockPos pos, int dimension){
        this.pos = pos == null ? null : new BlockPos(pos);
        this.dimension = DimensionManager.getWorld(dimension);
        this.bound = pos != null && this.dimension != null;
        if(this.bound)
            this.world.notifyNeighborsOfStateChange(this.pos,this.blockType,true);
    }

}
