package mods.flammpfeil;

import com.google.common.collect.Maps;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBase;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Map;

/**
 * Created by Furia on 2017/01/01.
 */
public class RenderPlayerEventHandler {

    Map<String,ModelWrapper> wrapper = Maps.newHashMap();

    @SubscribeEvent
    public void onRenderPlayerPre(RenderPlayerEvent.Pre event){

        if(!(event.getEntityPlayer() instanceof AbstractClientPlayer))
            return;

        String type = ((AbstractClientPlayer) event.getEntityPlayer()).getSkinType();

        if(!wrapper.containsKey(type))
            wrapper.put(type, new ModelWrapper(0.0f, !type.equals("default"), event.getRenderer().getMainModel()));

        ReflectionAccessHelper.setMainModel(event.getRenderer(), wrapper.get(type));
    }

    @SubscribeEvent
    public void onRenderPlayerPost(RenderPlayerEvent.Post event){

        if(!(event.getEntityPlayer() instanceof AbstractClientPlayer))
            return;

        String type = ((AbstractClientPlayer) event.getEntityPlayer()).getSkinType();

        if(wrapper.containsKey(type))
            ReflectionAccessHelper.setMainModel(event.getRenderer(), wrapper.get(type).original);
    }
}
