package luma.blossom;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

public class DeathMarkerClient implements ClientModInitializer {
    private static DeathMarkerConfig config;
    
    @Override
    public void onInitializeClient() {
        AutoConfig.register(DeathMarkerConfig.class, GsonConfigSerializer::new);
        config = AutoConfig.getConfigHolder(DeathMarkerConfig.class).getConfig();
        
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null && !client.isPaused()) {
                DeathMarkerManager.getInstance().tick();
            }
        });

        WorldRenderEvents.END.register(context -> {
            DeathMarkerRenderer.render(context);
        });
        
        DeathMarker.LOGGER.info("Thanks for using our mod <3");
    }
    
    public static DeathMarkerConfig getConfig() {
        return config;
    }
}