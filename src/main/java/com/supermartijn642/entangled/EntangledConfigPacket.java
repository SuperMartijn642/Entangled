package com.supermartijn642.entangled;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Created 12/10/2020 by SuperMartijn642
 */
public class EntangledConfigPacket {

    private boolean allowDimensional;
    private int maxDistance;

    public EntangledConfigPacket(){
        this.allowDimensional = EntangledConfig.INSTANCE.allowDimensional.get();
        this.maxDistance = EntangledConfig.INSTANCE.maxDistance.get();
    }

    public EntangledConfigPacket(PacketBuffer buffer){
        decode(buffer);
    }

    public void encode(PacketBuffer buffer){
        buffer.writeBoolean(this.allowDimensional);
        buffer.writeInt(this.maxDistance);
    }

    private void decode(PacketBuffer buffer){
        this.allowDimensional = buffer.readBoolean();
        this.maxDistance = buffer.readInt();
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier){
        contextSupplier.get().setPacketHandled(true);

        Entangled.allowDimensional = this.allowDimensional;
        Entangled.maxDistance = this.maxDistance;
    }

}
