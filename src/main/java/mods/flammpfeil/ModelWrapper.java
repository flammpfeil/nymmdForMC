package mods.flammpfeil;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.eventbus.Subscribe;
import jp.nyatla.nymmd.*;
import jp.nyatla.nymmd.core.PmdBone;
import jp.nyatla.nymmd.types.MmdMatrix;
import jp.nyatla.nymmd.types.MmdVector3;
import jp.nyatla.nymmd.types.MmdVector4;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.*;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.IResource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Furia on 2017/01/01.
 */
public class ModelWrapper extends ModelPlayer {

    public final ModelPlayer original;

    public static ModelResourceLocation modelLoc = new ModelResourceLocation("flammpfeil.nymmd.test:model/simple.pmd");

    MmdPmdModel pmd = null;
    MmdMotionPlayerGL2 mp = null;

    Map<Integer, ModelRenderer> bindings = Maps.newHashMap();
    Map<Integer, ModelRenderer> bindingsOriginal = Maps.newHashMap();

    public ModelWrapper(float modelSize, boolean smallArmsIn, ModelPlayer original) {
        super(modelSize, smallArmsIn);
        this.original = original;
    }

    Map<String, ModelRenderer> targets = (new Function<ModelWrapper, Map<String, ModelRenderer>>(){
        @Nullable
        @Override
        public  Map<String, ModelRenderer> apply(@Nullable ModelWrapper input) {
            Map<String, ModelRenderer> result = Maps.newHashMap();

            result.put("左腕",input.bipedLeftArm);
            result.put("右腕",input.bipedRightArm);

            result.put("左足親",input.bipedLeftLeg);
            result.put("右足親",input.bipedRightLeg);

            result.put("頭",input.bipedHead);

            result.put("上半身", input.bipedBody);

            return result;
        }
    }).apply(this);

    Map<String, MmdVector3> offsets = (new Function<ModelWrapper, Map<String, MmdVector3>>(){
        @Nullable
        @Override
        public  Map<String, MmdVector3> apply(@Nullable ModelWrapper input) {
            Map<String, MmdVector3> result = Maps.newHashMap();

            result.put("左腕", new MmdVector3(0,0, -0.25f));
            result.put("右腕", new MmdVector3(0,0, 0.25f));

            result.put("左足親", new MmdVector3(0,0, -0.1f));
            result.put("右足親", new MmdVector3(0,0, 0.1f));

            return result;
        }
    }).apply(this);

    @Subscribe
    public void onUpdateBonePre(MmdMotionPlayer.UpdateBoneEvent.Pre event){

        for(Map.Entry<String, ModelRenderer> entry : targets.entrySet()){
            String name = entry.getKey();
            ModelRenderer model = entry.getValue();


            int index = event.motionPlayer.getBoneIndexByName(name);

            if(index < 0) continue;

            MmdVector3 offset;
            if(offsets.containsKey(name))
                offset = offsets.get(name);
            else
                offset = new MmdVector3();

            float x,y,z;
            x = -model.rotateAngleX + offset.x;
            y = model.rotateAngleY + offset.y;
            z = -model.rotateAngleZ + offset.z;

            Set<String> list = Sets.newHashSet("右腕","左腕","頭");
            if(list.contains(name)){
                x -= -bipedBody.rotateAngleX;
                y -= bipedBody.rotateAngleY;
                z -= -bipedBody.rotateAngleZ;
            }

            if(name.equals("左腕"))
            switch(leftArmPose){
                case BOW_AND_ARROW:

                    y -= Math.toRadians(-35);

                    if(bipedHead.rotateAngleX < 0) {
                        x -= -bipedHead.rotateAngleX;
                        y -= -bipedHead.rotateAngleX;
                        z += -bipedHead.rotateAngleX;
                    }

                    break;
                default:
            }


            if(name.equals("右腕"))
                switch(rightArmPose){
                case BOW_AND_ARROW:

                    y += Math.toRadians(-35);

                    if(bipedHead.rotateAngleX < 0) {
                        x -= -bipedHead.rotateAngleX;
                        y += -bipedHead.rotateAngleX;
                        z -= -bipedHead.rotateAngleX;
                    }
                    break;
                default:
            }

            event.bones[index].m_vec4Rotate.QuaternionCreateEuler(new MmdVector3(x,y,z));
        }
    }
    @Subscribe
    public void onUpdateBonePost(MmdMotionPlayer.UpdateBoneEvent.Post event){

    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
        super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn);

        if(mp == null){
            try {
                IResource res = Minecraft.getMinecraft().getResourceManager().getResource(modelLoc);
                pmd = new MmdPmdModel(res.getInputStream(),null);

                mp = new MmdMotionPlayerGL2();

                mp.setPmd(pmd);

                MmdVmdMotion_BasicClass md = new MmdVmdMotion_BasicClass(null);

                mp.setVmd(md);

                mp.eventBus.register(this);
/*
                {
                    int index = mp.getBoneIndexByName("頭");
                    bindings.put(index, this.bipedHead);
                    bindingsOriginal.put(index, original.bipedHead);
                }

                {
                    int index = mp.getBoneIndexByName("上半身");
                    bindings.put(index, this.bipedBody);
                    bindingsOriginal.put(index, original.bipedBody);
                }
*/


                /*
                {
                    int index = mp.getBoneIndexByName("右足");

                    PmdBone bone = pmd.getBoneArray()[index];
                    MmdVector3 iniPos = bone._pmd_bone_position;
                    ModelRenderer model = this.bipedRightLeg = new ModelRenderer(this, 0, 16);
                    //= new ModelRenderMatrix(this, 0, 16, mp._skinning_mat, pmd.getBoneArray(), index);
                    model.addBox(-2, 0, -2, 4, 6, 4);

                    bindings.put(index, this.bipedRightLeg);
                    bindingsOriginal.put(index, original.bipedRightLeg);
                }
                {
                    int index = mp.getBoneIndexByName("右ひざ" );//"左足");

                    PmdBone bone = pmd.getBoneArray()[index];
                    MmdVector3 iniPos = bone._pmd_bone_position;
                    ModelRenderer model = this.bipedLeftLeg = new ModelRenderer(this, 0, 22);
                            //= new ModelRenderMatrix(this, 0, 22, mp._skinning_mat, pmd.getBoneArray(), index);
                    model.addBox(-2, 0, -2, 4, 6, 4); // 0 12

                    bindings.put(index, this.bipedLeftLeg);
                    bindingsOriginal.put(index, original.bipedLeftLeg);
                }
                */

                //*
                {
                    int index = mp.getBoneIndexByName("右手首");
                    bindings.put(index, this.bipedRightArm);
                    bindingsOriginal.put(index, original.bipedRightArm);
                }
                {
                    int index = mp.getBoneIndexByName("左手首");
                    bindings.put(index, this.bipedLeftArm);
                    bindingsOriginal.put(index, original.bipedLeftArm);
                }/**/

            }catch(Exception e){
                e.printStackTrace();
            }
        }

        if(original instanceof ModelPlayer && mp != null)
        //if(false)
        {
            ModelPlayer modelPlayer = (ModelPlayer) this;

            try {
                mp.updateMotion(1000f);
                //mp.updateMotion(1000f * (Minecraft.getSystemTime() % 6000) / 3000.0f);
            } catch (MmdException e) {
                e.printStackTrace();
            }

            for(Map.Entry<Integer, ModelRenderer> entry : bindings.entrySet()){
                setTransform(entry.getValue(), bindingsOriginal.get(entry.getKey()), entry.getKey(), entityIn);
            }
        }

    }

    @Override
    public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        //super.render(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);

        GL11.glPushMatrix();
        GL11.glTranslatef(0, 1.5f ,0);
        //GL11.glTranslatef(1, 0f ,0);
        GL11.glRotatef(180, 1 ,0, 0);
        //float mscale = 0.125f;
        float mscale = 0.08f;
        GL11.glScalef(mscale, mscale, mscale);
        mp.render();
        GL11.glPopMatrix();
    }

    //*
    float skeletonScaling = 2.0f;
    MmdMatrix beginTransform = (new Function<Object,MmdMatrix>(){
        @Nullable
        @Override
        public MmdMatrix apply(@Nullable Object input) {
            MmdMatrix scaleMat = new MmdMatrix();
            scaleMat.identity();
            scaleMat.m00 = skeletonScaling;
            scaleMat.m11 = skeletonScaling;
            scaleMat.m22 = -skeletonScaling;

            MmdMatrix rotMat = new MmdMatrix();
            MmdVector4 rotQt = new MmdVector4();
            rotQt.QuaternionCreateAxis(new MmdVector3(1, 0, 0), Math.PI);
            rotMat.QuaternionToMatrix(rotQt);


            MmdMatrix moveMat = new MmdMatrix();
            moveMat.identity();
            moveMat.m30 = 0;
            moveMat.m31 = -12;
            moveMat.m32 = 0;

            MmdMatrix tmp = new MmdMatrix();
            tmp = mul(scaleMat, rotMat);
            tmp = mul(moveMat, tmp);

            return tmp;
        }
    }).apply(null);

    MmdMatrix afterTransform = (new Function<Object,MmdMatrix>(){
        @Nullable
        @Override
        public MmdMatrix apply(@Nullable Object input) {
            float invBoneScaling = 1.0f / skeletonScaling;

            MmdMatrix rotMat = new MmdMatrix();
            MmdVector4 rotQt = new MmdVector4();
            rotQt.QuaternionCreateAxis(new MmdVector3(1, 0, 0), Math.PI);
            rotMat.QuaternionToMatrix(rotQt);

            MmdMatrix scaleInvMat = new MmdMatrix();
            scaleInvMat.identity();
            scaleInvMat.m00 = invBoneScaling;
            scaleInvMat.m11 = invBoneScaling;
            scaleInvMat.m22 = -invBoneScaling;

            MmdMatrix tmp = new MmdMatrix();
            tmp.identity();
            tmp = mul(scaleInvMat, tmp);
            tmp = mul(rotMat, tmp);

            return tmp;
        }
    }).apply(null);
/**/

    public void setTransform(ModelRenderer model , ModelRenderer org, int index , Entity entityIn) {
        MmdMatrix matrix = mp._skinning_mat[index];
        PmdBone bone = pmd.getBoneArray()[index];

        MmdVector3 pos;
        MmdVector3 rot;

        {
            /*
            float boneScaling = 2.0f;//0.5f;//125f;
            float invBoneScaling = 1.0f / boneScaling;

            MmdMatrix scaleMat = new MmdMatrix();
            scaleMat.identity();
            scaleMat.m00 = boneScaling;
            scaleMat.m11 = boneScaling;
            scaleMat.m22 = -boneScaling;

            MmdMatrix rotMat = new MmdMatrix();
            MmdVector4 rotQt = new MmdVector4();
            rotQt.QuaternionCreateAxis(new MmdVector3(1, 0, 0), Math.PI);
            rotMat.QuaternionToMatrix(rotQt);


            MmdMatrix moveMat = new MmdMatrix();
            moveMat.identity();
            moveMat.m30 = 0;
            moveMat.m31 = -12;
            moveMat.m32 = 0;

            MmdMatrix transform = matrix;

            MmdMatrix inv = new MmdMatrix();
            inv.inverse(bone.m_matInvTransform);

            MmdMatrix scaleInvMat = new MmdMatrix();
            scaleInvMat.identity();
            scaleInvMat.m00 = invBoneScaling;
            scaleInvMat.m11 = invBoneScaling;
            scaleInvMat.m22 = -invBoneScaling;

            MmdMatrix tmp = new MmdMatrix();
            tmp.identity();
            tmp = mul(scaleMat, tmp);
            tmp = mul(rotMat, tmp);
            tmp = mul(moveMat, tmp);
            tmp = mul(transform, tmp);
            tmp = mul(inv, tmp);
            tmp = mul(scaleInvMat, tmp);
            tmp = mul(rotMat, tmp);
            /**/

            //*
            MmdMatrix transform = matrix;

            MmdMatrix inv = new MmdMatrix();
            inv.inverse(bone.m_matInvTransform);

            MmdMatrix tmp = new MmdMatrix();
            //*
            tmp.identity();

            tmp = mul(beginTransform, tmp);
            tmp = mul(transform, tmp);
            tmp = mul(inv, tmp);

            if(bone.getName().equals("右手首")){

                //offset
                MmdMatrix moveMat = new MmdMatrix();


                MmdMatrix rotMat = new MmdMatrix();
                MmdVector4 rotQt = new MmdVector4();
                rotQt.QuaternionCreateEuler(new MmdVector3((float)Math.toRadians(5), 0, (float)(Math.PI * -0.25)));//(float)(Math.PI * (Minecraft.getSystemTime() % 6000) / 3000.0f)));
                rotMat.QuaternionToMatrix(rotQt);
                tmp = mul(rotMat, tmp);


                //rotPos
                moveMat.identity();
                moveMat.m30 = 0;
                moveMat.m31 = 4.5;
                moveMat.m32 = -0.5;

                tmp = mul(moveMat,tmp);


                moveMat.identity();
                moveMat.m30 = 3;
                moveMat.m31 = 10.5;
                moveMat.m32 = 1;
                tmp = mul(tmp, moveMat);
            }else {

                //offset
                MmdMatrix moveMat = new MmdMatrix();


                MmdMatrix rotMat = new MmdMatrix();
                MmdVector4 rotQt = new MmdVector4();
                rotQt.QuaternionCreateEuler(new MmdVector3((float) Math.toRadians(5), 0, (float) (Math.PI * 0.25)));//(float)(Math.PI * (Minecraft.getSystemTime() % 6000) / 3000.0f)));
                rotMat.QuaternionToMatrix(rotQt);
                tmp = mul(rotMat, tmp);


                //rotPos
                moveMat.identity();
                moveMat.m30 = 0;
                moveMat.m31 = 4.5;
                moveMat.m32 = -0.5;

                tmp = mul(moveMat, tmp);


                moveMat.identity();
                moveMat.m30 = -3;
                moveMat.m31 = 10.5;
                moveMat.m32 = 1;
                tmp = mul(tmp, moveMat);
            }

            tmp = mul(afterTransform, tmp);

            if(entityIn.isSneaking()){

                MmdMatrix moveMat = new MmdMatrix();
                moveMat.identity();
                moveMat.m30 = 0;
                moveMat.m31 = -4;
                moveMat.m32 = 0;
                tmp = mul(tmp, moveMat);
            }
            /**/



            pos = tmp.getPos();
            rot = tmp.getRotXYZ();
        }

        model.rotationPointX = (float)(pos.x);//org.rotationPointX +
        model.rotationPointZ = (float)(pos.z);//org.rotationPointZ +
        model.rotationPointY = (float)(pos.y);//org.rotationPointY +

        model.rotateAngleX =rot.x;
        model.rotateAngleY =rot.y;
        model.rotateAngleZ =rot.z;

    }

    public static MmdMatrix mul(MmdMatrix a, MmdMatrix b){
        MmdMatrix tmp = new MmdMatrix();
        tmp.mul(a, b);
        return tmp;
    }
}
