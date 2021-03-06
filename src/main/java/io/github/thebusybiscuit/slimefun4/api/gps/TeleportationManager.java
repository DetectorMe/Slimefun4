package io.github.thebusybiscuit.slimefun4.api.gps;

import io.github.thebusybiscuit.cscorelib2.chat.ChatColors;
import io.github.thebusybiscuit.cscorelib2.item.CustomItem;
import io.github.thebusybiscuit.cscorelib2.math.DoubleHandler;
import io.github.thebusybiscuit.cscorelib2.skull.SkullItem;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import me.mrCookieSlime.Slimefun.SlimefunPlugin;
import me.mrCookieSlime.Slimefun.api.Slimefun;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class TeleportationManager {

    private final Set<UUID> teleporterUsers = new HashSet<>();
    private final int[] teleporterBorder = {0, 1, 2, 3, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 26, 27, 35, 36, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53};
    private final int[] teleporterInventory = {19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43};

    private final GPSNetwork network;

    public TeleportationManager(GPSNetwork gpsNetwork) {
        network = gpsNetwork;
    }

    public void openTeleporterGUI(Player p, UUID uuid, Block b, int complexity) {
        if (teleporterUsers.contains(p.getUniqueId())) {
            return;
        }

        p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1F, 1F);
        teleporterUsers.add(p.getUniqueId());

        ChestMenu menu = new ChestMenu("&3Teleporter");
        menu.addMenuCloseHandler(pl -> teleporterUsers.remove(pl.getUniqueId()));

        for (int slot : teleporterBorder) {
            menu.addItem(slot, ChestMenuUtils.getBackground(), ChestMenuUtils.getEmptyClickHandler());
        }

        menu.addItem(4, new CustomItem(SkullItem.fromHash("c9c8881e42915a9d29bb61a16fb26d059913204d265df5b439b3d792acd56"), ChatColor.YELLOW + SlimefunPlugin.getLocal().getMessage(p, "machines.TELEPORTER.gui.title")));
        menu.addMenuClickHandler(4, ChestMenuUtils.getEmptyClickHandler());

        Location source = new Location(b.getWorld(), b.getX() + 0.5D, b.getY() + 2D, b.getZ() + 0.5D);
        int index = 0;

        for (Map.Entry<String, Location> entry : network.getWaypoints(uuid).entrySet()) {
            if (index >= teleporterInventory.length) break;
            int slot = teleporterInventory[index];

            Location l = entry.getValue();
            ItemStack globe = network.getIcon(entry);

            menu.addItem(slot, new CustomItem(globe, entry.getKey().replace("player:death ", ""), "", "&8\u21E8 &7" + SlimefunPlugin.getLocal().getResourceString(p, "tooltips.world") + ": &r" + l.getWorld().getName(), "&8\u21E8 &7X: &r" + l.getX(), "&8\u21E8 &7Y: &r" + l.getY(), "&8\u21E8 &7Z: &r" + l.getZ(), "&8\u21E8 &7" + SlimefunPlugin.getLocal().getMessage(p, "machines.TELEPORTER.gui.time") + ": &r" + DoubleHandler.fixDouble(0.5 * getTeleportationTime(complexity, source, l)) + "s", "", "&8\u21E8 &c" + SlimefunPlugin.getLocal().getMessage(p, "machines.TELEPORTER.gui.tooltip")));
            menu.addMenuClickHandler(slot, (pl, s, item, action) -> {
                pl.closeInventory();
                teleport(pl.getUniqueId(), complexity, source, l, false);
                return false;
            });

            index++;
        }

        menu.open(p);
    }

    public void teleport(UUID uuid, int complexity, Location source, Location destination, boolean resistance) {
        teleporterUsers.add(uuid);

        int time = getTeleportationTime(complexity, source, destination);
        updateProgress(uuid, 100 / time, 0, source, destination, resistance);
    }

    public int getTeleportationTime(int complexity, Location source, Location destination) {
        if (complexity < 100) return 100;

        int speed = 50_000 + complexity * complexity;
        return 1 + Math.min(4 * distanceSquared(source, destination) / speed, 40);
    }

    private int distanceSquared(Location source, Location destination) {
        if (source.getWorld().getUID().equals(destination.getWorld().getUID())) {
            int distance = (int) source.distanceSquared(destination);
            return Math.min(distance, 100_000_000);
        } else {
            return 100_000_000;
        }
    }

    private boolean isValid(Player p, Location source) {
        return p != null && p.getLocation().distanceSquared(source) < 2.0;
    }

    private void cancel(UUID uuid, Player p) {
        teleporterUsers.remove(uuid);

        if (p != null) {
            p.sendTitle(ChatColors.color(SlimefunPlugin.getLocal().getMessage(p, "machines.TELEPORTER.cancelled")), ChatColors.color("&c&k40&r&c%"), 20, 60, 20);
        }
    }

    private void updateProgress(UUID uuid, int speed, int progress, Location source, Location destination, boolean resistance) {
        Player p = Bukkit.getPlayer(uuid);

        if (isValid(p, source)) {
            if (progress > 99) {
                p.sendTitle(ChatColors.color(SlimefunPlugin.getLocal().getMessage(p, "machines.TELEPORTER.teleported")), ChatColors.color("&b100%"), 20, 60, 20);

                p.teleport(destination);

                if (resistance) {
                    p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 600, 20));
                    SlimefunPlugin.getLocal().sendMessage(p, "machines.TELEPORTER.invulnerability");
                }

                destination.getWorld().spawnParticle(Particle.PORTAL, new Location(destination.getWorld(), destination.getX(), destination.getY() + 1, destination.getZ()), progress * 2, 0.2F, 0.8F, 0.2F);
                destination.getWorld().playSound(destination, Sound.BLOCK_BEACON_ACTIVATE, 1F, 1F);
                teleporterUsers.remove(uuid);
            } else {
                p.sendTitle(ChatColors.color(SlimefunPlugin.getLocal().getMessage(p, "machines.TELEPORTER.teleporting")), ChatColors.color("&b" + progress + "%"), 0, 60, 0);

                source.getWorld().spawnParticle(Particle.PORTAL, source, progress * 2, 0.2F, 0.8F, 0.2F);
                source.getWorld().playSound(source, Sound.BLOCK_BEACON_AMBIENT, 1F, 0.6F);

                Slimefun.runSync(() -> updateProgress(uuid, speed, progress + speed, source, destination, resistance), 10L);
            }
        } else {
            cancel(uuid, p);
        }
    }

}
