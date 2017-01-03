package mods.flammpfeil;

import jp.nyatla.nymmd.MmdException;
import jp.nyatla.nymmd.MmdMotionPlayerGL2;
import jp.nyatla.nymmd.MmdPmdModel;
import jp.nyatla.nymmd.MmdVmdMotion_BasicClass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.resources.IResource;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

/**
 * Created by Furia on 2016/12/31.
 */
public class TESRmmd extends TileEntitySpecialRenderer<DummyTileEntity> {

    MmdPmdModel pmd = null;
    MmdMotionPlayerGL2 mp = null;

    static public ItemStack stack;
    static public EntityLivingBase entity;

    @Override
    public void renderTileEntityAt(DummyTileEntity te, double x, double y, double z, float partialTicks, int destroyStage) {
        //super.renderTileEntityAt(te, x, y, z, partialTicks, destroyStage);

        if(mp == null){
            try {
                IResource res = Minecraft.getMinecraft().getResourceManager().getResource(ModelBakeEventHandler.modelLoc);
                pmd = new MmdPmdModel(res.getInputStream(),null);

                mp = new MmdMotionPlayerGL2();

                mp.setPmd(pmd);

                MmdVmdMotion_BasicClass md = new MmdVmdMotion_BasicClass(null);

                mp.setVmd(md);

            }catch(Exception e){
                e.printStackTrace();
            }
        }else{
            try {
                mp.updateMotion(1000f * (Minecraft.getSystemTime() % 3000) / 1000.0f);

                GL11.glPushMatrix();
                float scale = 0.1f;
                GL11.glTranslatef(0.5f,1f,1f);
                GL11.glScalef(scale, scale, scale);
                mp.render();
                GL11.glPopMatrix();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
