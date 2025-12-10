package draylar.goml.mixin;

import draylar.goml.api.ClaimUtils;
import draylar.goml.other.OriginOwner;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

@Mixin(Projectile.class)
public abstract class ProjectileMixin extends Entity implements OriginOwner {
    @Shadow @Nullable protected EntityReference<Entity> owner;

    public ProjectileMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

    @Inject(method = "onHit", at = @At("HEAD"), cancellable = true)
    private void preventEffects(HitResult hitResult, CallbackInfo ci) {
        if (this.level().isClientSide()) {
            return;
        }
        if (!ClaimUtils.hasMatchingClaims(this.level(), this.blockPosition(), this.goml$getOriginSafe(), this.owner != null ? this.owner.getUUID() : null)) {
            ci.cancel();
        }
    }

    @Inject(method = "mayInteract", at = @At("HEAD"), cancellable = true)
    private void preventModification(ServerLevel world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (!ClaimUtils.hasMatchingClaims(this.level(), this.blockPosition(), this.goml$getOriginSafe(), this.owner != null ? this.owner.getUUID() : null)) {
            cir.setReturnValue(false);
        }
    }
}
