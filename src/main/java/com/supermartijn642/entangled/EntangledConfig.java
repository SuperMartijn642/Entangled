package com.supermartijn642.entangled;

import com.supermartijn642.configlib.api.ConfigBuilders;
import com.supermartijn642.configlib.api.IConfigBuilder;

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
            .comment("Should the `entangled:invalid_targets` tag be treated as a whitelist rather than a blacklist? If true, entangled blocks can only be bound to blocks in the tag.")
            .define("useWhitelist", false);
        builder.pop();
        builder.build();
    }
}
