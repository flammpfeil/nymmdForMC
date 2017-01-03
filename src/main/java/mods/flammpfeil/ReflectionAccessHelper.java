package mods.flammpfeil;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

/**
 * Created by Furia on 2017/01/01.
 */
public class ReflectionAccessHelper {
    static public void setMainModel(RenderLivingBase render, ModelBase base){
        ObfuscationReflectionHelper.setPrivateValue(RenderLivingBase.class, render, base, "mainModel");
    }
}
