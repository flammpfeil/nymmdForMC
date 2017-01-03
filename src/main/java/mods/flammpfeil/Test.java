package mods.flammpfeil;

import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemSword;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

/**
 * Created by Furia on 2015/08/14.
 */
@Mod(modid = Test.MODID, version = Test.VERSION)
public class Test {
    public static final String MODID = "nymmdtest";
    public static final String VERSION = "1.0";

    Item item;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event){

        item = new ItemSword(Item.ToolMaterial.DIAMOND).setUnlocalizedName("test").setRegistryName(MODID, "test");

        GameRegistry.register(item);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {

    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event){

        Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(item,0, ModelBakeEventHandler.modelLoc);

        ForgeHooksClient.registerTESRItemStack(item, 0, DummyTileEntity.class);

        ClientRegistry.bindTileEntitySpecialRenderer(DummyTileEntity.class, new TESRmmd());

        MinecraftForge.EVENT_BUS.register(new ModelBakeEventHandler());

        MinecraftForge.EVENT_BUS.register(new RenderPlayerEventHandler());

    }
}
