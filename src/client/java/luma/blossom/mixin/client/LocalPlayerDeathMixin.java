package luma.blossom.mixin.client;

import luma.blossom.DeathMarkerManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LocalPlayerDeathMixin {
    
    @Inject(method = "die", at = @At("HEAD"))
    private void onDeath(DamageSource damageSource, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        
        LocalPlayer localPlayer = Minecraft.getInstance().player;
        if (localPlayer != null && entity == localPlayer) {
            DeathMarkerManager.getInstance().onPlayerDeath(
                localPlayer.blockPosition(),
                localPlayer.level().dimension()
            );
        }
    }
}
