package mods.flammpfeil;

import jp.nyatla.nymmd.core.PmdBone;
import jp.nyatla.nymmd.types.MmdMatrix;
import jp.nyatla.nymmd.types.MmdVector3;
import jp.nyatla.nymmd.types.MmdVector4;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import org.lwjgl.opengl.GL11;

/**
 * Created by Furia on 2017/01/02.
 */
public class ModelRenderMatrix extends ModelRenderer {
    boolean compiled = false;
    int displayList;

    public int matrixIndex;
    public MmdMatrix[] matrices;
    public PmdBone[] bones;

    public ModelRenderMatrix(ModelBase model, int texOffX, int texOffY, MmdMatrix[] matrices, PmdBone[] bones, int index) {
        super(model, texOffX, texOffY);

        this.matrixIndex = index;
        this.matrices = matrices;
        this.bones = bones;
    }

    public void compileDisplayList(float scale)
    {
        this.displayList = GLAllocation.generateDisplayLists(1);
        GlStateManager.glNewList(this.displayList, 4864);
        VertexBuffer vertexbuffer = Tessellator.getInstance().getBuffer();

        for (int i = 0; i < this.cubeList.size(); ++i)
        {
            ((ModelBox)this.cubeList.get(i)).render(vertexbuffer, scale);
        }

        GlStateManager.glEndList();
        this.compiled = true;
    }

    @Override
    public void render(float scale) {
        if (!this.isHidden)
        {
            if (this.showModel)
            {
                if (!this.compiled)
                {
                    this.compileDisplayList(scale);
                }

                GL11.glPushMatrix();

                GlStateManager.translate(this.offsetX, this.offsetY, this.offsetZ);

                postRender(scale);

                GlStateManager.callList(this.displayList);

                if (this.childModels != null)
                {
                    for (int k = 0; k < this.childModels.size(); ++k)
                    {
                        ((ModelRenderer)this.childModels.get(k)).render(scale);
                    }
                }

                GlStateManager.translate(-this.offsetX, -this.offsetY, -this.offsetZ);

                GL11.glPopMatrix();
            }
        }
    }

    @Override
    public void postRender(float scale) {

        //ボーン構造をスケーリング&空間変形
        float boneScaling = 0.125f;
        float invBoneScaling = 1.0f / boneScaling;
        if(false)
        {
            GL11.glScaled(boneScaling, boneScaling, -boneScaling);
            GL11.glRotatef(180, 1, 0, 0);

            //head位置補正
            GL11.glTranslatef(0, -12f, 0);

            //変形適用
            GL11.glMultMatrix(matrices[matrixIndex].getMatrixBuffer());

            //初期姿勢適用
            MmdVector3 vec = bones[matrixIndex]._pmd_bone_position;
            GL11.glTranslatef(vec.x, vec.y, vec.z);

            /*
            MmdMatrix inv = new MmdMatrix();
            inv.inverse(bones[matrixIndex].m_matInvTransform);
            GL11.glMultMatrix(inv.getMatrixBuffer());
            */

            //ボーン構造スケーリング解放&空間変形
            GL11.glScaled(invBoneScaling, invBoneScaling, -invBoneScaling);
            GL11.glRotatef(-180, 1, 0, 0);
        }
        else
        {
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

            MmdMatrix transform = matrices[matrixIndex];

            MmdMatrix inv = new MmdMatrix();
            inv.inverse(bones[matrixIndex].m_matInvTransform);


            MmdMatrix scaleInvMat = new MmdMatrix();
            scaleInvMat.identity();
            scaleInvMat.m00 = invBoneScaling;
            scaleInvMat.m11 = invBoneScaling;
            scaleInvMat.m22 = -invBoneScaling;

            MmdMatrix tmp = new MmdMatrix();
            tmp.identity();
            tmp = mul( scaleMat    ,tmp);
            tmp = mul( rotMat      ,tmp);
            tmp = mul( moveMat     ,tmp);
            tmp = mul( transform   ,tmp);
            tmp = mul( inv         ,tmp);
            tmp = mul( scaleInvMat ,tmp);
            tmp = mul( rotMat      ,tmp);

            GL11.glMultMatrix(tmp.getMatrixBuffer());
        }

    }

    public MmdMatrix mul(MmdMatrix a, MmdMatrix b){
        MmdMatrix tmp = new MmdMatrix();
        tmp.mul(a, b);
        return tmp;
    }
}
