package com.supermartijn642.entangled;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

/**
 * Created 5/17/2021 by SuperMartijn642
 */
public class EntangledBlockBakedItemModel implements BakedModel {

    private final BakedModel originalModel;

    public EntangledBlockBakedItemModel(BakedModel originalModel){
        this.originalModel = originalModel;
    }

    @Override
    public boolean isCustomRenderer(){
        return true;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction direction, Random random){
        return this.originalModel.getQuads(state, direction, random);
    }

    @Override
    public boolean useAmbientOcclusion(){
        return this.originalModel.useAmbientOcclusion();
    }

    @Override
    public boolean isGui3d(){
        return this.originalModel.isGui3d();
    }

    @Override
    public boolean usesBlockLight(){
        return this.originalModel.usesBlockLight();
    }

    @Override
    public TextureAtlasSprite getParticleIcon(){
        return this.originalModel.getParticleIcon();
    }

    @Override
    public ItemTransforms getTransforms(){
        return this.originalModel.getTransforms();
    }

    @Override
    public ItemOverrides getOverrides(){
        return this.originalModel.getOverrides();
    }
}
