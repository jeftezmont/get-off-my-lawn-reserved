package draylar.goml.api.group;

import com.mojang.authlib.GameProfile;
import draylar.goml.api.Claim;
import net.minecraft.item.ItemStack;
import net.minecraft.server.PlayerConfigEntry;
import net.minecraft.text.Text;

import java.util.List;
import java.util.UUID;

public interface PlayerGroup {
    Text selfDisplayName();
    Text fullDisplayName();
    Key getKey();
    ItemStack icon();
    PlayerGroupProvider provider();
    boolean isPartOf(UUID uuid);
    List<Member> getMembers();
    boolean canSave();

    boolean addClaim(Claim claim);
    boolean removeClaim(Claim claim);

    record Member(PlayerConfigEntry profile, String role) {}

    record Key(String providerId, String groupId) {
        public String compact() {
            return providerId + ":" + groupId;
        }
        public static final Key EMPTY = new Key("", "");

        public static Key of(String value) {
            var split = value.split(":", 2);
            if (split.length < 2) {
                return EMPTY;
            }
            return new Key(split[0], split[1]);
        }

        @Override
        public String toString() {
            return compact();
        }
    }
}
