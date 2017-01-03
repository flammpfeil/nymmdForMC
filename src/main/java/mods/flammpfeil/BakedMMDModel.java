package mods.flammpfeil;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.client.model.IPerspectiveAwareModel;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import java.util.List;

/**
 * Created by Furia on 2015/08/14.
 */
public class BakedMMDModel implements IPerspectiveAwareModel {

    public BakedMMDModel() {
    }

    @Override
    public boolean isBuiltInRenderer() {
        return true;
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return null;
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return ItemCameraTransforms.DEFAULT;
    }


    @Override
    public ItemOverrideList getOverrides() {
        return iol;
    }
    ItemOverrideList iol = new ItemOverrideList(ImmutableList.<ItemOverride>of()){
        @Override
        public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, World world, EntityLivingBase entity) {

            if(stack != null) {
                TESRmmd.stack = stack;
                if(entity != null)
                    TESRmmd.entity = entity;
            }else{
                TESRmmd.stack = stack;
                TESRmmd.entity = entity;
            }

            return super.handleItemState(originalModel, stack, world, entity);
        }

    };

    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        return Lists.newArrayList();
    }

    @Override
    public boolean isAmbientOcclusion() {
        return false;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public Pair<? extends IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType cameraTransformType) {
        return IPerspectiveAwareModel.MapWrapper.handlePerspective(this, ModelRotation.X0_Y0, cameraTransformType);
    }
}
