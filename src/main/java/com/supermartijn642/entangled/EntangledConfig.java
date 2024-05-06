package com.supermartijn642.entangled;

import com.supermartijn642.configlib.api.ConfigBuilders;
import com.supermartijn642.configlib.api.IConfigBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Created 12/10/2020 by SuperMartijn642
 */
public class EntangledConfig {

    public static final Supplier<Boolean> renderBlockHighlight;
    public static final Supplier<Boolean> rotateRenderedBlock;

    public static final Supplier<Boolean> allowDimensional;
    public static final Supplier<Integer> maxDistance;
    public static final Supplier<Boolean> useWhitelist;
    private static Supplier<String> blacklistString;
    public static final Supplier<List<Block>> blacklist = () -> {
        List<Block> list = new ArrayList<>();
        if (blacklistString != null) {
            for (String key : blacklistString.get().split(",")) {
                if (ForgeRegistries.BLOCKS.containsKey(new ResourceLocation(key))) {
                    list.add(ForgeRegistries.BLOCKS.getValue(new ResourceLocation(key)));
                }
            }
        }
        return list;
    };

    static {
        IConfigBuilder builder = ConfigBuilders.newTomlConfig("entangled", null, false);
        builder.push("Client");
        renderBlockHighlight = builder
            .dontSync()
            .comment("When looking at an Entangled Block, should its bound block be highlighted?")
            .define("renderBlockHighlight", true);
        rotateRenderedBlock = builder
            .dontSync()
            .comment("Should the block rendered inside entangled blocks rotate?")
            .define("rotateRenderedBlock", true);
        builder.pop();
        builder.push("General");
        allowDimensional = builder
            .comment("Can entangled blocks be bound between different dimensions? Previously bound entangled blocks won't be affected.")
            .define("allowDimensional", true);
        maxDistance = builder
            .comment("What is the max range in which entangled blocks can be bound? Only affects blocks in the same dimension. -1 for infinite range. Previously bound entangled blocks won't be affected.")
            .define("maxDistance", -1, -1, Integer.MAX_VALUE);
        useWhitelist = builder
            .comment("Whether to use whitelist, if true, use blacklist as whitelist")
            .define("useWhitelist", false);
        blacklistString = builder
            .comment("Add blacklist to Entangled Block, when useWhitelist is true, it will as whitelist. Separate with commas(,).")
            .define("blacklist", "", 0, Integer.MAX_VALUE);
        builder.pop();
        builder.build();
    }
}
