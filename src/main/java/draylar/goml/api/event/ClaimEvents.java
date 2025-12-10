package draylar.goml.api.event;

import draylar.goml.api.Claim;
import draylar.goml.api.ClaimBox;
import draylar.goml.api.PermissionReason;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class ClaimEvents {

    /**
     * This callback is triggered when a player's permission is denied inside a claim.
     * Callback handlers can confirm the denial, pass, or veto the denial through {@link InteractionResult}.
     */
    public static final Event<InteractionHandler> PERMISSION_DENIED = EventFactory.createArrayBacked(InteractionHandler.class,
            (listeners) -> (player, world, hand, pos, reason) -> {
                for (InteractionHandler event : listeners) {
                    InteractionResult result = event.check(player, world, hand, pos, reason);

                    if (result != InteractionResult.PASS) {
                        return result;
                    }
                }

                return InteractionResult.PASS;
            }
    );

    public static final Event<GenericClaimEvent> CLAIM_CREATED = EventFactory.createArrayBacked(GenericClaimEvent.class,
            (listeners) -> (claim) -> {
                for (var event : listeners) {
                    event.onEvent(claim);
                }
            }
    );

    public static final Event<GenericClaimEvent> CLAIM_DESTROYED = EventFactory.createArrayBacked(GenericClaimEvent.class,
            (listeners) -> (claim) -> {
                for (var event : listeners) {
                    event.onEvent(claim);
                }
            }
    );

    public static final Event<ClaimResizedEvent> CLAIM_RESIZED = EventFactory.createArrayBacked(ClaimResizedEvent.class,
            (listeners) -> (claim, x, y) -> {
                for (var event : listeners) {
                    event.onResizeEvent(claim, x, y);
                }
            }
    );

    public static final Event<GenericClaimEvent> CLAIM_UPDATED = EventFactory.createArrayBacked(GenericClaimEvent.class,
        (listeners) -> (claim) -> {
            for (var event : listeners) {
                event.onEvent(claim);
            }
        }
    );

    public interface InteractionHandler {
        InteractionResult check(Player player, Level world, InteractionHand hand, BlockPos pos, PermissionReason reason);
    }

    public interface GenericClaimEvent {
        void onEvent(Claim claim);
    }

    public interface ClaimResizedEvent {
        void onResizeEvent(Claim claim, ClaimBox oldSize, ClaimBox newSize);
    }
}
