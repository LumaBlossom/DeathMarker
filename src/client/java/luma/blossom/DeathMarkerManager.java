package luma.blossom;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class DeathMarkerManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("DeathMarker");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String SAVE_FILE = "deathmarker.json";
    
    private static DeathMarkerManager instance;
    
    private DeathMarkerData currentMarker;
    private boolean wasDeadLastTick = false;
    private String currentWorldId = null;

    private DeathMarkerManager() {
    }

    public static DeathMarkerManager getInstance() {
        if (instance == null) {
            instance = new DeathMarkerManager();
        }
        return instance;
    }

    public void onPlayerDeath(BlockPos position, ResourceKey<Level> dimension) {
        this.currentMarker = new DeathMarkerData(position, dimension);
        LOGGER.info("Death marker set at {} in {}", position, dimension.location());
        saveMarker();
    }

    public void onWorldJoin(String worldId) {
        if (worldId == null || worldId.equals(currentWorldId)) {
            return;
        }
        
        this.currentWorldId = worldId;
        this.wasDeadLastTick = false;
        loadMarker();
    }

    public void onWorldLeave() {
        saveMarker();
        this.currentMarker = null;
        this.currentWorldId = null;
        this.wasDeadLastTick = false;
    }

    public void tick() {
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        
        if (player == null) {
            wasDeadLastTick = false;
            return;
        }

        String worldId = getWorldId();
        if (worldId != null && !worldId.equals(currentWorldId)) {
            onWorldJoin(worldId);
        }

        boolean isDead = player.isDeadOrDying();
        
        if (isDead && !wasDeadLastTick) {
            LOGGER.info("Player death detected! Position: {}", player.blockPosition());
            onPlayerDeath(player.blockPosition(), player.level().dimension());
        }
        wasDeadLastTick = isDead;

        if (currentMarker == null) {
            return;
        }

        if (!player.level().dimension().equals(currentMarker.getDimension())) {
            return;
        }

        DeathMarkerConfig config = DeathMarkerClient.getConfig();
        double clearDist = config != null ? config.clearDistance : 2.0;
        double distanceToMarker = currentMarker.getDistanceTo(player.getX(), player.getY(), player.getZ());
        if (distanceToMarker <= clearDist && !player.isDeadOrDying()) {
            LOGGER.info("Player reached death location - marker cleared");
            currentMarker = null;
            saveMarker();
            return;
        }

        if (currentMarker.isPlayerInRange(player.getX(), player.getY(), player.getZ())) {
            currentMarker.addTick();
        }

        if (currentMarker.isExpired()) {
            LOGGER.info("Death marker expired - items have despawned");
            currentMarker = null;
            saveMarker();
        }
    }

    private String getWorldId() {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null) {
            return null;
        }
        
        if (client.getSingleplayerServer() != null) {
            return client.getSingleplayerServer().getWorldPath(LevelResource.ROOT).getParent().getFileName().toString();
        }
        
        if (client.getCurrentServer() != null) {
            return client.getCurrentServer().ip.replace(":", "_").replace("/", "_");
        }
        
        return "unknown";
    }

    private Path getSaveDir() {
        Minecraft client = Minecraft.getInstance();
        Path gameDir = client.gameDirectory.toPath();
        Path saveDir = gameDir.resolve("deathmarker").resolve(currentWorldId != null ? currentWorldId : "unknown");
        
        try {
            if (!Files.exists(saveDir)) {
                Files.createDirectories(saveDir);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to create save directory", e);
        }
        
        return saveDir;
    }

    private void saveMarker() {
        if (currentWorldId == null) {
            return;
        }
        
        Path saveFile = getSaveDir().resolve(SAVE_FILE);
        
        try {
            if (currentMarker == null) {
                Files.deleteIfExists(saveFile);
                return;
            }
            
            JsonObject json = new JsonObject();
            json.addProperty("x", currentMarker.getPosition().getX());
            json.addProperty("y", currentMarker.getPosition().getY());
            json.addProperty("z", currentMarker.getPosition().getZ());
            json.addProperty("dimension", currentMarker.getDimension().location().toString());
            json.addProperty("accumulatedTicks", currentMarker.getAccumulatedTicks());
            
            Files.writeString(saveFile, GSON.toJson(json));
            LOGGER.debug("Saved death marker to {}", saveFile);
        } catch (IOException e) {
            LOGGER.error("Failed to save death marker", e);
        }
    }

    private void loadMarker() {
        if (currentWorldId == null) {
            return;
        }
        
        Path saveFile = getSaveDir().resolve(SAVE_FILE);
        
        if (!Files.exists(saveFile)) {
            currentMarker = null;
            return;
        }
        
        try {
            String content = Files.readString(saveFile);
            JsonObject json = JsonParser.parseString(content).getAsJsonObject();
            
            int x = json.get("x").getAsInt();
            int y = json.get("y").getAsInt();
            int z = json.get("z").getAsInt();
            String dimensionStr = json.get("dimension").getAsString();
            long accumulatedTicks = json.get("accumulatedTicks").getAsLong();
            
            BlockPos position = new BlockPos(x, y, z);
            ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(dimensionStr));
            
            currentMarker = new DeathMarkerData(position, dimension, accumulatedTicks);
            LOGGER.info("Loaded death marker from {} at {} in {}", saveFile, position, dimensionStr);
        } catch (Exception e) {
            LOGGER.error("Failed to load death marker", e);
            currentMarker = null;
        }
    }

    public DeathMarkerData getCurrentMarker() {
        return currentMarker;
    }

    public void clearMarker() {
        this.currentMarker = null;
        saveMarker();
        LOGGER.info("Death marker cleared");
    }

    public boolean hasMarker() {
        return currentMarker != null;
    }
}
