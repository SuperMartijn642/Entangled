package com.supermartijn642.entangled;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraftforge.client.model.BakedModelWrapper;

/**
 * Created 5/17/2021 by SuperMartijn642
 */
public class EntangledBlockBakedItemModel extends BakedModelWrapper<IBakedModel> {

    public EntangledBlockBakedItemModel(IBakedModel originalModel){
        super(originalModel);
    }

    @Override
    public boolean isCustomRenderer(){
        return true;
    }

    @Override
    public IBakedModel handlePerspective(ItemCameraTransforms.TransformType cameraTransformType, MatrixStack mat){
        super.handlePerspective(cameraTransformType, mat);
        return this;
    }
}
