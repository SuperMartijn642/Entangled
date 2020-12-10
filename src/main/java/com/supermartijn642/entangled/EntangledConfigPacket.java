package com.supermartijn642.entangled;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Created 12/10/2020 by SuperMartijn642
 */
public class EntangledConfigPacket implements IMessage, IMessageHandler<EntangledConfigPacket,IMessage> {

    private boolean allowDimensional;
    private int maxDistance;

    public EntangledConfigPacket(){
        this.allowDimensional = EntangledConfig.allowDimensional;
        this.maxDistance = EntangledConfig.maxDistance;
    }

    @Override
    public void fromBytes(ByteBuf buffer){
        this.allowDimensional = buffer.readBoolean();
        this.maxDistance = buffer.readInt();
    }

    @Override
    public void toBytes(ByteBuf buffer){
        buffer.writeBoolean(this.allowDimensional);
        buffer.writeInt(this.maxDistance);
    }

    @Override
    public IMessage onMessage(EntangledConfigPacket message, MessageContext ctx){
        Entangled.allowDimensional = message.allowDimensional;
        Entangled.maxDistance = message.maxDistance;
        return null;
    }

}
