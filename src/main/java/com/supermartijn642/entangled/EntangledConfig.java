package com.supermartijn642.entangled;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.PacketDistributor;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Created 12/10/2020 by SuperMartijn642
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EntangledConfig {

    static{
        Pair<EntangledConfig,ForgeConfigSpec> pair = new ForgeConfigSpec.Builder().configure(EntangledConfig::new);
        CONFIG_SPEC = pair.getRight();
        INSTANCE = pair.getLeft();
    }

    public static final ForgeConfigSpec CONFIG_SPEC;
    public static final EntangledConfig INSTANCE;

    public final ForgeConfigSpec.BooleanValue allowDimensional;
    public final ForgeConfigSpec.IntValue maxDistance;

    private EntangledConfig(ForgeConfigSpec.Builder builder){
        builder.push("General");
        this.allowDimensional = builder.worldRestart().comment("Can entangled blocks be bound between different dimensions? Previously bound entangled blocks won't be affected.")
            .define("allowDimensional", true);
        this.maxDistance = builder.worldRestart().comment("What is the max range in which entangled blocks can be bound? Only affects blocks in the same dimension. -1 for infinite range. Previously bound entangled blocks won't be affected.")
            .defineInRange("maxDistance", -1, -1, Integer.MAX_VALUE);
        builder.pop();
    }

    @SubscribeEvent
    public static void onWorldLoad(WorldEvent.Load e){
        if(!e.getWorld().isRemote() && e.getWorld() instanceof World && ((World)e.getWorld()).getDimensionKey() == World.OVERWORLD){
            Entangled.allowDimensional = INSTANCE.allowDimensional.get();
            Entangled.maxDistance = INSTANCE.maxDistance.get();
        }
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent e){
        Entangled.CHANNEL.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity)e.getPlayer()), new EntangledConfigPacket());
    }
}
