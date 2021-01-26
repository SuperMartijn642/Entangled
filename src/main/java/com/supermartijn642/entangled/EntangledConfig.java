package com.supermartijn642.entangled;

import com.supermartijn642.configlib.ModConfigBuilder;

import java.util.function.Supplier;

/**
 * Created 12/10/2020 by SuperMartijn642
 */
public class EntangledConfig {

    public static final Supplier<Boolean> renderBlockHighlight;

    public static final Supplier<Boolean> allowDimensional;
    public static final Supplier<Integer> maxDistance;

    static{
        ModConfigBuilder builder = new ModConfigBuilder("entangled");
        builder.push("Client");
        renderBlockHighlight = builder.dontSync().comment("When looking at an Entangled Block, should its bound block be highlighted?")
            .define("renderBlockHighlight", true);
        builder.pop();
        builder.push("General");
        allowDimensional = builder.comment("Can entangled blocks be bound between different dimensions? Previously bound entangled blocks won't be affected.")
            .define("allowDimensional", true);
        maxDistance = builder.comment("What is the max range in which entangled blocks can be bound? Only affects blocks in the same dimension. -1 for infinite range. Previously bound entangled blocks won't be affected.")
            .define("maxDistance", -1, -1, Integer.MAX_VALUE);
        builder.pop();
        builder.build();
    }
}
