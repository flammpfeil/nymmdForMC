package jp.nyatla.nymmd;


import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Vector;

import jp.nyatla.nymmd.types.*;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import org.lwjgl.opengl.GL11;


public class MmdMotionPlayerGL2 extends MmdMotionPlayer
{

	private class Material
	{
		public FloatBuffer color;// Diffuse,Specular,Ambientの順
		public float fShininess;
		public short[] indices;
		public int ulNumIndices;
		//public int texture_id;
		public int unknown;
	}	
	public MmdMotionPlayerGL2()
	{
		super();
	}
	private final MmdMatrix __tmp_matrix = new MmdMatrix();
	private Material[] _materials;
	private float[] _fbuf;
	private MmdTexUV[] _tex_array;
	
	@Override
	public void setPmd(MmdPmdModel_BasicClass i_pmd_model) throws MmdException
	{
		super.setPmd(i_pmd_model);
		
		//確保済みリソースのリセット
		//OpenGLResourceの生成
		final int number_of_vertex=i_pmd_model.getNumberOfVertex();
		this._fbuf=new float[number_of_vertex*3*2];
		
		MmdPmdModel_BasicClass.IResourceProvider tp=i_pmd_model.getResourceProvider();
		
		//Material配列の作成
		PmdMaterial[] m = i_pmd_model.getMaterials();// this._ref_materials;
		Vector<Material> materials = new Vector<Material>();
		for (int i = 0; i < m.length; i++){
			final Material new_material = new Material();
			new_material.unknown=m[i].unknown;
			// D,A,S[rgba]
			float[] color = new float[12];
			m[i].col4Diffuse.getValue(color, 0);
			m[i].col4Ambient.getValue(color, 4);
			m[i].col4Specular.getValue(color, 8);

			new_material.color = makeFloatBuffer(12);
			new_material.color.put(color);
			new_material.color.position(0);

			new_material.fShininess = m[i].fShininess;
			/*
			if (m[i].texture_name != null)
			{
				new_material.texture_id = this._tex_list.getTexture(m[i].texture_name,tp);
			} else {
				new_material.texture_id = 0;
			}
			*/
			//new_material.indices=ShortBuffer.wrap(m[i].indices);
			new_material.indices = m[i].indices;

			new_material.ulNumIndices = m[i].indices.length;
			materials.add(new_material);
		}
		this._materials = materials.toArray(new Material[materials.size()]);

		this._tex_array = this._ref_pmd_model.getUvArray();
		return;		
	}
	public void setVmd(MmdVmdMotion_BasicClass i_vmd_model) throws MmdException
	{
		super.setVmd(i_vmd_model);
	}
	
	/**
	 * この関数はupdateMotionがskinning_matを更新するを呼び出します。
	 */
	@Override
	protected void onUpdateSkinningMatrix(MmdMatrix[] i_skinning_mat) throws MmdException
	{
		MmdVector3 vp;
		MmdMatrix mat;
		MmdVector3[] org_pos_array=this._ref_pmd_model.getPositionArray();
		MmdVector3[] org_normal_array=this._ref_pmd_model.getNormatArray();
		PmdSkinInfo[] org_skin_info=this._ref_pmd_model.getSkinInfoArray();
		
		int number_of_vertex=this._ref_pmd_model.getNumberOfVertex();
		float[] ft=this._fbuf;
		int p1=0;
		int p2=number_of_vertex*3;
		for (int i = 0; i<this._ref_pmd_model.getNumberOfVertex() ; i++)
		{
			PmdSkinInfo info_ptr=org_skin_info[i];
			if (info_ptr.fWeight == 0.0f)
			{
				mat = i_skinning_mat[info_ptr.unBoneNo_1];
			} else if (info_ptr.fWeight >= 0.9999f) {
				mat = i_skinning_mat[info_ptr.unBoneNo_0];
			} else {
				final MmdMatrix mat0 = i_skinning_mat[info_ptr.unBoneNo_0];
				final MmdMatrix mat1 = i_skinning_mat[info_ptr.unBoneNo_1];
				mat = this.__tmp_matrix;
				mat.MatrixLerp(mat0, mat1, info_ptr.fWeight);
			}
			vp=org_pos_array[i];
			ft[p1++]=((float)(vp.x * mat.m00 + vp.y * mat.m10 + vp.z * mat.m20 + mat.m30));
			ft[p1++]=((float)(vp.x * mat.m01 + vp.y * mat.m11 + vp.z * mat.m21 + mat.m31));
			ft[p1++]=((float)(vp.x * mat.m02 + vp.y * mat.m12 + vp.z * mat.m22 + mat.m32));			
			
			vp=org_normal_array[i];
			ft[p2++]=((float)(vp.x * mat.m00 + vp.y * mat.m10 + vp.z * mat.m20));
			ft[p2++]=((float)(vp.x * mat.m01 + vp.y * mat.m11 + vp.z * mat.m21));
			ft[p2++]=((float)(vp.x * mat.m02 + vp.y * mat.m12 + vp.z * mat.m22));
		}
		return;
	}
	public void render()
	{
		GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
		GL11.glPushClientAttrib(GL11.GL_ALL_CLIENT_ATTRIB_BITS);

		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glCullFace(GL11.GL_FRONT);
		GL11.glEnable(GL11.GL_NORMALIZE);
		GL11.glShadeModel(GL11.GL_SMOOTH);

		// とりあえずbufferに変換しよう
		// とりあえず転写用


		VertexBuffer wr = Tessellator.getInstance().getBuffer();
		int number_of_vertex=this._ref_pmd_model.getNumberOfVertex();

		// 頂点座標、法線、テクスチャ座標の各配列をセット
		for (int i = this._materials.length-1; i>=0 ; i--)
		{
			wr.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX_NORMAL);

			final Material mt_ptr=this._materials[i];



			for(int pos : mt_ptr.indices){
				int npos = number_of_vertex*3+pos*3;
				//wr.setNormal(_fbuf[npos++], _fbuf[npos++], _fbuf[npos++]);
				int vpos = pos*3;
				//wr.addVertexWithUV(_fbuf[vpos++],_fbuf[vpos++],_fbuf[vpos++],this._tex_array[pos].u,this._tex_array[pos].v);
				wr.pos(_fbuf[vpos++],_fbuf[vpos++],-_fbuf[vpos++]).tex(this._tex_array[pos].u,this._tex_array[pos].v).normal(_fbuf[npos++], _fbuf[npos++], _fbuf[npos++]).endVertex();
			}

			// マテリアル設定
			/**/
			GL11.glEnable(GL11.GL_COLOR_MATERIAL);
			GL11.glColorMaterial(GL11.GL_FRONT_AND_BACK,GL11.GL_AMBIENT_AND_DIFFUSE);
			GL11.glColor4f(mt_ptr.color.get(0),mt_ptr.color.get(1),mt_ptr.color.get(2),mt_ptr.color.get(3));

			/**/

			/*
			FloatBuffer color = makeFloatBuffer(12);
			mt_ptr.color.position(0);
			color.put(mt_ptr.color);
			color.position(0);
			GL11.glMaterial(GL11.GL_FRONT_AND_BACK, GL11.GL_DIFFUSE, color);
			color.position(4);
			GL11.glMaterial(GL11.GL_FRONT_AND_BACK, GL11.GL_AMBIENT, color);
			color.position(8);
			GL11.glMaterial(GL11.GL_FRONT_AND_BACK, GL11.GL_SPECULAR, color);
			GL11.glMaterialf(GL11.GL_FRONT_AND_BACK, GL11.GL_SHININESS, 0);//mt_ptr.fShininess);
			/**/

            //カリング判定：何となくうまくいったから
			if ((0x100 & mt_ptr.unknown) == 0x100)
            {
				GL11.glDisable(GL11.GL_CULL_FACE);
            }
            else
            {
				GL11.glEnable(GL11.GL_CULL_FACE);
            }


			/*
			if (mt_ptr.texture_id!=0) {
				// テクスチャありならBindする
				GL11.glEnable(GL11.GL_TEXTURE_2D);
				GL11.glBindTexture(GL11.GL_TEXTURE_2D, mt_ptr.texture_id);
			} else {
				// テクスチャなし
				GL11.glDisable(GL11.GL_TEXTURE_2D);
			}
			*/
			GL11.glDisable(GL11.GL_TEXTURE_2D);

			// 頂点インデックスを指定してポリゴン描画
			//GL11.glDrawElements(GL11.GL_TRIANGLES, mt_ptr.indices);


			Tessellator.getInstance().draw();
		}

		GL11.glPopClientAttrib();
		GL11.glPopAttrib();
		return;
	}

    private static FloatBuffer makeFloatBuffer(int i_size)
    {
        ByteBuffer bb = ByteBuffer.allocateDirect(i_size*4);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer fb = bb.asFloatBuffer();
        return fb;
    }
}
