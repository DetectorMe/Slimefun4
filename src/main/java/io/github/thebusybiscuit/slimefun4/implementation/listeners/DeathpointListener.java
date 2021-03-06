package io.github.thebusybiscuit.slimefun4.implementation.listeners;

import me.mrCookieSlime.Slimefun.Lists.SlimefunItems;
import me.mrCookieSlime.Slimefun.SlimefunPlugin;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DeathpointListener implements Listener {

    private final SimpleDateFormat format = new SimpleDateFormat("(MMM d, yyyy @ hh:mm)");

    public DeathpointListener(SlimefunPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onDeath(EntityDeathEvent e) {
        if (e.getEntity().getType() == EntityType.PLAYER) {
            Player p = (Player) e.getEntity();

            if (p.getInventory().containsAtLeast(SlimefunItems.GPS_EMERGENCY_TRANSMITTER, 1)) {
                SlimefunPlugin.getGPSNetwork().addWaypoint(p, "player:death " + SlimefunPlugin.getLocal().getMessage(p, "gps.deathpoint").replace("%date%", format.format(new Date())), p.getLocation().getBlock().getLocation());
            }
        }
    }
}
