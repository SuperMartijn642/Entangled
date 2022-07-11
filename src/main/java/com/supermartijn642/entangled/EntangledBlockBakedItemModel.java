package com.supermartijn642.entangled;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraftforge.client.model.BakedModelWrapper;

/**
 * Created 5/17/2021 by SuperMartijn642
 */
public class EntangledBlockBakedItemModel extends BakedModelWrapper<BakedModel> {

    public EntangledBlockBakedItemModel(BakedModel originalModel){
        super(originalModel);
    }

    @Override
    public boolean isCustomRenderer(){
        return true;
    }

    @Override
    public BakedModel applyTransform(ItemTransforms.TransformType cameraTransformType, PoseStack poseStack, boolean applyLeftHandTransform){
        super.applyTransform(cameraTransformType, poseStack, applyLeftHandTransform);
        return this;
    }
}
