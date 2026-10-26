package net.dianacraft.alliances.mixin;

import net.dianacraft.alliances.Events;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
    @Inject(at = @At("TAIL"), method = "tickServer")
    private void onEndTick(BooleanSupplier shouldKeepTicking, CallbackInfo info) {
        Events.onServerTickEnd((MinecraftServer) (Object) this);
    }

}