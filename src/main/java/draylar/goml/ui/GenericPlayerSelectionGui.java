package draylar.goml.ui;

import com.mojang.authlib.GameProfile;
import draylar.goml.api.group.PlayerGroupProvider;
import draylar.goml.registry.GOMLTextures;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerConfigEntry;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.ApiStatus;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

@ApiStatus.Internal
public class GenericPlayerSelectionGui extends PagedGui {
    private final PlayerManager playerManager;
    private final Predicate<PlayerConfigEntry> shouldDisplay;
    private final Consumer<PlayerConfigEntry> onClick;
    private int ticker;
    private List<PlayerConfigEntry> cachedPlayers = Collections.emptyList();

    public GenericPlayerSelectionGui(ServerPlayerEntity player, Text title, Predicate<PlayerConfigEntry> shouldDisplay, Consumer<PlayerConfigEntry> onClick, Runnable postClose) {
        super(player, postClose);
        this.shouldDisplay = shouldDisplay;
        this.onClick = onClick;
        this.playerManager = Objects.requireNonNull(player.getEntityWorld().getServer()).getPlayerManager();
        this.setTitle(title);
    }

    public void updateAndOpen() {
        this.updateDisplay();
        this.open();
    }

    @Override
    protected int getPageAmount() {
        return this.getContentAmount() / PAGE_SIZE;
    }

    protected int getContentAmount() {
        return this.cachedPlayers.size();
    }

    @Override
    protected void updateDisplay() {
        List<PlayerConfigEntry> list = new ArrayList<>();
        for (var p : this.playerManager.getPlayerList()) {
            if (this.shouldDisplay.test(p.getPlayerConfigEntry())) {
                list.add(p.getPlayerConfigEntry());
            }
        }

        for (var group : PlayerGroupProvider.getAllGroups(this.player)) {
            for (var member : group.getMembers()) {
                if (!list.contains(member.profile()) && this.shouldDisplay.test(member.profile())) {
                    list.add(member.profile());
                }
            }
        }


        list.sort(Comparator.comparing(PlayerConfigEntry::name));
        this.cachedPlayers = list;
        super.updateDisplay();
    }

    @Override
    protected DisplayElement getElement(int id) {
        return this.getPlayerElement(id);
    }

    protected DisplayElement getPlayerElement(int id) {
        if (this.cachedPlayers.size() > id) {
            var player = this.cachedPlayers.get(id);
            var b = new GuiElementBuilder(Items.PLAYER_HEAD)
                    .setName(Text.literal(player.name()))
                    .setProfile(player.id())
                    .setCallback((x, y, z) -> {
                        playClickSound(this.player);
                        this.onClick.accept(player);
                        this.close(this.closeCallback != null);
                    });

            var x = PlayerGroupProvider.getShared(this.player, player.id());

            if (!x.isEmpty()) {
                b.addLoreLine(Text.translatable("text.goml.gui.shared_groups").formatted(Formatting.GOLD));

                for (var g : x) {
                    b.addLoreLine(Text.literal("- ").append(g.fullDisplayName()).formatted(Formatting.GRAY));
                }
            }


            return DisplayElement.of(b);
        }

        return DisplayElement.empty();
    }

    @Override
    protected DisplayElement getNavElement(int id) {
        return switch (id) {
            case 5 -> DisplayElement.of(new GuiElementBuilder(Items.NAME_TAG)
                    .setName(Text.translatable("text.goml.gui.player_selector.by_name").formatted(Formatting.GREEN))
                    .setCallback((x, y, z) -> {
                        playClickSound(this.player);

                        this.ignoreCloseCallback = true;
                        this.close(true);
                        this.ignoreCloseCallback = false;
                        new NamePlayerSelectorGui(this.player, this.shouldDisplay, this::refreshOpen, (p) -> {
                            this.onClick.accept(p);
                            this.close(true);
                            if (this.closeCallback != null) {
                                this.closeCallback.run();
                            }
                        });
                    }));
            default -> super.getNavElement(id);
        };
    }

    @Override
    public void onTick() {
        this.ticker++;
        if (this.ticker == 20) {
            this.ticker = 0;
            this.updateDisplay();
        }
        super.onTick();
    }
}
