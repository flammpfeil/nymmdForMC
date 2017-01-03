package mods.flammpfeil;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Created by Furia on 2015/07/25.
 */
public class ModelBakeEventHandler {

    public static ModelResourceLocation modelLoc = new ModelResourceLocation("flammpfeil.nymmd.test:model/simple.pmd");

    @SubscribeEvent
    public void onModelBake(ModelBakeEvent event){
        event.getModelRegistry().putObject(modelLoc, new BakedMMDModel());
    }
}
