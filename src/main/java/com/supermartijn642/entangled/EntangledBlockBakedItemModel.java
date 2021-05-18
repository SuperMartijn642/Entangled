package com.supermartijn642.entangled;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraftforge.client.model.BakedModelWrapper;
import org.apache.commons.lang3.tuple.Pair;

import javax.vecmath.Matrix4f;

/**
 * Created 5/17/2021 by SuperMartijn642
 */
public class EntangledBlockBakedItemModel extends BakedModelWrapper<IBakedModel> {

    public EntangledBlockBakedItemModel(IBakedModel originalModel){
        super(originalModel);
    }

    @Override
    public boolean isBuiltInRenderer(){
        return true;
    }

    @Override
    public Pair<? extends IBakedModel,Matrix4f> handlePerspective(ItemCameraTransforms.TransformType cameraTransformType){
        Pair<? extends IBakedModel,Matrix4f> pair = super.handlePerspective(cameraTransformType);
        return Pair.of(this, pair.getRight());
    }
}
