package draylar.goml.other;

import draylar.goml.api.Claim;
import draylar.goml.api.group.PlayerGroup;
import draylar.goml.api.group.PlayerGroupProvider;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.NameToIdCache;
import net.minecraft.util.UserCache;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class VanillaTeamGroups {
    public static void init() {
        PlayerGroupProvider.register("minecraft_team", TeamProvider.INSTANCE);

    }

    public static void onRemove(Team team) {
        var value = TeamGroup.CACHE.get(team);
        if (value != null) {
            for (var claim : List.copyOf(value.claims)) {
                claim.untrust(value);
            }
        }
    }

    private record TeamProvider() implements PlayerGroupProvider {
        public static final PlayerGroupProvider INSTANCE = new TeamProvider();
        @Override
        public @Nullable PlayerGroup getGroupOf(PlayerEntity player) {
            var team = player.getScoreboardTeam();
            if (team != null) {
                return TeamGroup.of(player.getEntityWorld().getServer().getApiServices().nameToIdCache(), team);
            }
            return null;
        }

        @Override
        public @Nullable PlayerGroup getGroupOf(MinecraftServer server, UUID uuid) {
            var profile = server.getApiServices().nameToIdCache().getByUuid(uuid);

            if (profile.isPresent()) {
                var team = server.getScoreboard().getTeam(profile.get().name());
                if (team  != null) {
                    return TeamGroup.of(server.getApiServices().nameToIdCache(), team);
                }
            }
            return null;
        }

        @Override
        @Nullable
        public PlayerGroup fromKey(MinecraftServer server, PlayerGroup.Key key) {
            var team = server.getScoreboard().getTeam(key.groupId());
            if (team  != null) {
                return TeamGroup.of(server.getApiServices().nameToIdCache(), team);
            }
            return null;
        }

        @Override
        public Text getName() {
            return Text.translatable("text.goml.vanilla_team.name");
        }
    }

    private record TeamGroup(NameToIdCache cache, Team team, HashSet<Claim> claims) implements PlayerGroup {
        public static final WeakHashMap<Team, TeamGroup> CACHE = new WeakHashMap<>();
        private TeamGroup(NameToIdCache cache, Team guild) {
            this(cache, guild, new HashSet<>());
        }

        public static PlayerGroup of(NameToIdCache cache, Team team) {
            var g = CACHE.get(team);

            if (g == null) {
                g = new TeamGroup(cache, team);
                CACHE.put(team, g);
            }

            return g;
        }

        @Override
        public Text selfDisplayName() {
            return this.team.getDisplayName();
        }

        @Override
        public Text fullDisplayName() {
            return Text.translatable("text.goml.vanilla_team.display", Text.empty().append(this.selfDisplayName()).formatted(Formatting.WHITE));
        }

        @Override
        public Key getKey() {
            return new Key("minecraft_team", team.getName());
        }

        @Override
        public ItemStack icon() {
            var stack = new ItemStack(Items.LEATHER_HELMET);
            var i = this.team.getColor().getColorValue();
            stack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(i != null ? i : 0xFFFFFF));
            stack.set(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplayComponent.DEFAULT.with(DataComponentTypes.DYED_COLOR, false));
            return stack;
        }

        @Override
        public PlayerGroupProvider provider() {
            return TeamProvider.INSTANCE;
        }

        @Override
        public boolean isPartOf(UUID uuid) {
            var profile = this.cache.getByUuid(uuid);
            return profile.isPresent() && this.team.getPlayerList().contains(profile.get().name());
        }

        @Override
        public boolean canSave() {
            return true;
        }

        @Override
        public List<Member> getMembers() {
            List<Member> list = new ArrayList<>();
            for (var x : this.team.getPlayerList()) {
                var profile = this.cache.findByName(x);
                if (profile.isPresent()) {
                    Member member = new Member(profile.get(), "");
                    list.add(member);
                }
            }
            return list;
        }

        @Override
        public boolean addClaim(Claim claim) {
            return this.claims.add(claim);
        }

        @Override
        public boolean removeClaim(Claim claim) {
            return this.claims.remove(claim);
        }

        @Override
        public boolean equals(Object obj) {
            return obj == this;
        }

        @Override
        public int hashCode() {
            return this.team.getName().hashCode() + 31 * "minecraft_team".hashCode();
        }
    }
}
