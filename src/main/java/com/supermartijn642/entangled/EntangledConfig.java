package com.supermartijn642.entangled;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.DimensionType;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

import java.io.File;
import java.util.HashMap;

/**
 * Created 12/10/2020 by SuperMartijn642
 */
@Mod.EventBusSubscriber
public class EntangledConfig {

    private static final String FILE_NAME = "entangled.cfg";

    public static Configuration instance;

    public static boolean allowDimensional;
    public static int maxDistance;

    public static void init(File dir){
        instance = new Configuration(new File(dir, FILE_NAME));
        instance.load();

        instance.addCustomCategoryComment(Configuration.CATEGORY_GENERAL, "");

        allowDimensional = instance.getBoolean("allowDimensional", Configuration.CATEGORY_GENERAL, true, "Can entangled blocks be bound between different dimensions? Previously bound entangled blocks won't be affected.");
        maxDistance = instance.getInt("maxDistance", Configuration.CATEGORY_GENERAL, -1, -1, Integer.MAX_VALUE, "What is the max range in which entangled blocks can be bound? Only affects blocks in the same dimension. -1 for infinite range. Previously bound entangled blocks won't be affected.");

        if(instance.hasChanged())
            instance.save();
    }

    @SubscribeEvent
    public static void onWorldLoad(WorldEvent.Load e){
        if(!e.getWorld().isRemote && e.getWorld().provider.getDimensionType() == DimensionType.OVERWORLD){
            Entangled.allowDimensional = allowDimensional;
            Entangled.maxDistance = maxDistance;
        }
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent e){
        Entangled.channel.sendTo(new EntangledConfigPacket(), (EntityPlayerMP)e.player);
    }
}
