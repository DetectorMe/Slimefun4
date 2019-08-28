package me.mrCookieSlime.Slimefun.GPS;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import me.mrCookieSlime.Slimefun.SlimefunStartup;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config;
import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Variable;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu.MenuClickHandler;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu.MenuCloseHandler;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ClickAction;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.MenuHelper;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.MenuHelper.ChatHandler;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.Item.CustomItem;
import me.mrCookieSlime.CSCoreLibPlugin.general.Math.DoubleHandler;
import me.mrCookieSlime.CSCoreLibPlugin.general.World.CustomSkull;
import me.mrCookieSlime.Slimefun.GEO.OreGenResource;
import me.mrCookieSlime.Slimefun.GEO.OreGenSystem;
import me.mrCookieSlime.Slimefun.Setup.Messages;
import me.mrCookieSlime.Slimefun.api.Slimefun;

public class GPSNetwork {
	
	private Map<UUID, Set<Location>> transmitters = new HashMap<>();
	private int[] border = new int[] {0, 1, 3, 5, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 26, 27, 35, 36, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53};
	private int[] inventory = new int[] {19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43};
    private static final int[] teleporter_border = new int[] {0, 1, 2, 3, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 26, 27, 35, 36, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53};
    private static final int[] teleporter_inventory = new int[] {19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43};
	
	public void updateTransmitter(Location l, UUID uuid, NetworkStatus status) {
		Set<Location> set = new HashSet<>();
		if (transmitters.containsKey(uuid)) set = transmitters.get(uuid);
        if (status == NetworkStatus.ONLINE) {
            if (!set.contains(l)) {
				set.add(l);
				transmitters.put(uuid, set);
			}
		}
		else {
			set.remove(l);
			transmitters.put(uuid, set);
		}
	}
	
	public int getNetworkComplexity(UUID uuid) {
		if (!transmitters.containsKey(uuid)) return 0;
		int level = 0;
		for (Location l: transmitters.get(uuid)) {
			level = level + l.getBlockY();
		}
		return level;
	}
	
	public int countTransmitters(UUID uuid) {
		if (!transmitters.containsKey(uuid)) return 0;
		else return transmitters.get(uuid).size();
	}
	
	public void openTransmitterControlPanel(Player p) throws Exception {
		ChestMenu menu = new ChestMenu("&9控制面板");
		
		for (int slot: border) {
			menu.addItem(slot, new CustomItem(new ItemStack(Material.GRAY_STAINED_GLASS_PANE), " "),
                    (arg0, arg1, arg2, arg3) -> false);
		}
		
		menu.addItem(2, new CustomItem(CustomSkull.getItem("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjBjOWMxYTAyMmY0MGI3M2YxNGI0Y2JhMzdjNzE4YzZhNTMzZjNhMjg2NGI2NTM2ZDVmNDU2OTM0Y2MxZiJ9fX0="), "&7信号发射机概览 &e(已选中)"));
		menu.addMenuClickHandler(2, (arg0, arg1, arg2, arg3) -> false);
		
		menu.addItem(4, new CustomItem(CustomSkull.getItem("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGRjZmJhNThmYWYxZjY0ODQ3ODg0MTExODIyYjY0YWZhMjFkN2ZjNjJkNDQ4MWYxNGYzZjNiY2I2MzMwIn19fQ=="), "&7GPS网络信息", "", "&8\u21E8 &7状态: " + (getNetworkComplexity(p.getUniqueId()) > 0 ? "&2&l在线": "&4&l离线"), "&8\u21E8 &7复杂度: &r" + getNetworkComplexity(p.getUniqueId())));
		menu.addMenuClickHandler(4, (arg0, arg1, arg2, arg3) -> false);
		
		menu.addItem(6, new CustomItem(CustomSkull.getItem("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzljODg4MWU0MjkxNWE5ZDI5YmI2MWExNmZiMjZkMDU5OTEzMjA0ZDI2NWRmNWI0MzliM2Q3OTJhY2Q1NiJ9fX0="), "&7路径点概览 &r(单击选择)"));
		menu.addMenuClickHandler(6, (arg0, arg1, arg2, arg3) -> {
            try {
                openWaypointControlPanel(arg0);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        });
		
		int index = 0;
		for (Location l: getTransmitters(p.getUniqueId())) {
			if (index >= inventory.length) break;
			int slot = inventory[index];
			
			menu.addItem(slot, new CustomItem(CustomSkull.getItem("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjBjOWMxYTAyMmY0MGI3M2YxNGI0Y2JhMzdjNzE4YzZhNTMzZjNhMjg2NGI2NTM2ZDVmNDU2OTM0Y2MxZiJ9fX0="), "&bGPS 信号发射机", "&8\u21E8 &7世界: &r" + l.getWorld().getName(), "&8\u21E8 &7X: &r" + l.getX(), "&8\u21E8 &7Y: &r" + l.getY(), "&8\u21E8 &7Z: &r" + l.getZ(), "", "&8\u21E8 &7信号强度: &r" + l.getBlockY(), "&8\u21E8 &7延迟: &r" + DoubleHandler.fixDouble(1000D / l.getY()) + "ms"));
			menu.addMenuClickHandler(slot, (arg0, arg1, arg2, arg3) -> false);
			
			index++;
		}
		
		menu.open(p);
	}
	
	public static ItemStack getPlanet(Map.Entry<String, Location> entry) throws Exception {
		Location l = entry.getValue();
		if (entry.getKey().startsWith("&4Deathpoint")) {
			return CustomSkull.getItem("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWFlMzg1NWY5NTJjZDRhMDNjMTQ4YTk0NmUzZjgxMmE1OTU1YWQzNWNiY2I1MjYyN2VhNGFjZDQ3ZDMwODEifX19");
		}
		else if (l.getWorld().getEnvironment() == Environment.NETHER) {
			return CustomSkull.getItem("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDgzNTcxZmY1ODlmMWE1OWJiMDJiODA4MDBmYzczNjExNmUyN2MzZGNmOWVmZWJlZGU4Y2YxZmRkZSJ9fX0=");
		}
		else if (l.getWorld().getEnvironment() == Environment.THE_END) {
			return CustomSkull.getItem("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzZjYWM1OWIyYWFlNDg5YWEwNjg3YjVkODAyYjI1NTVlYjE0YTQwYmQ2MmIyMWViMTE2ZmE1NjljZGI3NTYifX19");
		}
		else {
			return CustomSkull.getItem("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzljODg4MWU0MjkxNWE5ZDI5YmI2MWExNmZiMjZkMDU5OTEzMjA0ZDI2NWRmNWI0MzliM2Q3OTJhY2Q1NiJ9fX0=");
		}
	}
	
	public void openWaypointControlPanel(Player p) throws Exception {
		ChestMenu menu = new ChestMenu("&9控制面板");
		
		for (int slot: border) {
			menu.addItem(slot, new CustomItem(new ItemStack(Material.GRAY_STAINED_GLASS_PANE), " "),
                    (arg0, arg1, arg2, arg3) -> false);
		}
		
		menu.addItem(2, new CustomItem(CustomSkull.getItem("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjBjOWMxYTAyMmY0MGI3M2YxNGI0Y2JhMzdjNzE4YzZhNTMzZjNhMjg2NGI2NTM2ZDVmNDU2OTM0Y2MxZiJ9fX0="), "&7信号发射机概览 &r(单击选择)"));
		menu.addMenuClickHandler(2, (arg0, arg1, arg2, arg3) -> {
            try {
                openTransmitterControlPanel(arg0);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        });
		
		menu.addItem(4, new CustomItem(CustomSkull.getItem("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGRjZmJhNThmYWYxZjY0ODQ3ODg0MTExODIyYjY0YWZhMjFkN2ZjNjJkNDQ4MWYxNGYzZjNiY2I2MzMwIn19fQ=="), "&7GPS网络信息", "", "&8\u21E8 &7状态: " + (getNetworkComplexity(p.getUniqueId()) > 0 ? "&2&l在线": "&4&l离线"), "&8\u21E8 &7复杂度: &r" + getNetworkComplexity(p.getUniqueId())));
		menu.addMenuClickHandler(4, (arg0, arg1, arg2, arg3) -> false);
		
		menu.addItem(6, new CustomItem(CustomSkull.getItem("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzljODg4MWU0MjkxNWE5ZDI5YmI2MWExNmZiMjZkMDU5OTEzMjA0ZDI2NWRmNWI0MzliM2Q3OTJhY2Q1NiJ9fX0="), "&7路径点概览 &e(已选中)"));
		menu.addMenuClickHandler(6, (arg0, arg1, arg2, arg3) -> false);
		
		int index = 0;
		for (final Map.Entry<String, Location> entry: getWaypoints(p.getUniqueId()).entrySet()) {
			if (index >= inventory.length) break;
			int slot = inventory[index];
			
			Location l = entry.getValue();
			ItemStack globe = getPlanet(entry);
			
			menu.addItem(slot, new CustomItem(globe, entry.getKey(), "&8\u21E8 &7世界: &r" + l.getWorld().getName(), "&8\u21E8 &7X: &r" + l.getX(), "&8\u21E8 &7Y: &r" + l.getY(), "&8\u21E8 &7Z: &r" + l.getZ(), "", "&8\u21E8 &c单击删除"));
			menu.addMenuClickHandler(slot, (arg0, arg1, arg2, arg3) -> {
                String id = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', entry.getKey())).toUpperCase().replace(" ", "_");
                Config cfg = new Config("data-storage/Slimefun/waypoints/" + arg0.getUniqueId().toString() + ".yml");
                cfg.setValue(id, null);
                cfg.save();
                arg0.playSound(arg0.getLocation(), Sound.UI_BUTTON_CLICK, 1F, 1F);
                try {
                    openWaypointControlPanel(arg0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            });
			
			index++;
		}
		
		menu.open(p);
	}

	public Map<String, Location> getWaypoints(UUID uuid) {
		Map<String, Location> map = new HashMap<>();
		Config cfg = new Config("data-storage/Slimefun/waypoints/" + uuid.toString() + ".yml");
		for (String key: cfg.getKeys()) {
			if (cfg.contains(key + ".world") && Bukkit.getWorld(cfg.getString(key + ".world")) != null) {
				map.put(cfg.getString(key + ".name"), cfg.getLocation(key));
			}
		}
		return map;
	}
	
	public void addWaypoint(Player p, final Location l) {
		if ((getWaypoints(p.getUniqueId()).size() + 2) > inventory.length) {
			Messages.local.sendTranslation(p, "gps.waypoint.max", true);
			return;
		}
		Messages.local.sendTranslation(p, "gps.waypoint.new", true);
		p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5F, 1F);
		MenuHelper.awaitChatInput(p, (p1, message) -> {
            addWaypoint(p1, message, l);
            return false;
        });
	}
	
	public void addWaypoint(Player p, String name, Location l) {
		if ((getWaypoints(p.getUniqueId()).size() + 2) > inventory.length) {
			Messages.local.sendTranslation(p, "gps.waypoint.max", true);
			return;
		}
		Config cfg = new Config("data-storage/Slimefun/waypoints/" + p.getUniqueId().toString() + ".yml");
		String id = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', name)).toUpperCase().replace(" ", "_");
		cfg.setValue(id, l);
		cfg.setValue(id + ".name", name);
		cfg.save();
		p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1F, 1F);
		Messages.local.sendTranslation(p, "gps.waypoint.added", true);
	}

	public Set<Location> getTransmitters(UUID uuid) {
		return transmitters.containsKey(uuid) ? transmitters.get(uuid): new HashSet<>();
	}

	public void scanChunk(Player p, Chunk chunk) {
		if (getNetworkComplexity(p.getUniqueId()) < 600) {
			Messages.local.sendTranslation(p, "gps.insufficient-complexity", true, new Variable("%complexity%", String.valueOf(600)));
			return;
		}
		ChestMenu menu = new ChestMenu("&4扫描结果");
		
		int index = 0;
		
		for (OreGenResource resource: OreGenSystem.listResources()) {
			int supply = OreGenSystem.getSupplies(resource, chunk, true);
			
			menu.addItem(index, new CustomItem(resource.getIcon(), "&7资源: &e" + resource.getName(), "", "&7已扫描的区块:", "&8\u21E8 &7X: " + chunk.getX() + " Z: " + chunk.getZ(), "", "&7结果: &e" + supply + " " + resource.getMeasurementUnit()),
                    (arg0, arg1, arg2, arg3) -> false);
			index++;
		}
		
		menu.open(p);
	}

	public static void openTeleporterGUI(Player p, UUID uuid, Block b, final int complexity) throws Exception {
		if (SlimefunStartup.instance.getUtilities().teleporterUsers.contains(p.getUniqueId())) return;
		
		p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1F, 1F);
        SlimefunStartup.instance.getUtilities().teleporterUsers.add(p.getUniqueId());
		
		ChestMenu menu = new ChestMenu("&3传送机");
		
		menu.addMenuCloseHandler(p12 -> SlimefunStartup.instance.getUtilities().teleporterUsers.remove(p12.getUniqueId()));
		
		for (int slot: teleporter_border) {
			menu.addItem(slot, new CustomItem(new ItemStack(Material.GRAY_STAINED_GLASS_PANE), " "),
                    (arg0, arg1, arg2, arg3) -> false);
		}
		
		menu.addItem(4, new CustomItem(CustomSkull.getItem("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzljODg4MWU0MjkxNWE5ZDI5YmI2MWExNmZiMjZkMDU5OTEzMjA0ZDI2NWRmNWI0MzliM2Q3OTJhY2Q1NiJ9fX0="), "&7路径点概览 &e(选择目的地)"));
		menu.addMenuClickHandler(4, (arg0, arg1, arg2, arg3) -> false);
		
		final Location source = new Location(b.getWorld(), b.getX() + 0.5D, b.getY() + 2D, b.getZ() + 0.5D);
		int index = 0;
		for (final Map.Entry<String, Location> entry: Slimefun.getGPSNetwork().getWaypoints(uuid).entrySet()) {
			if (index >= teleporter_inventory.length) break;
			int slot = teleporter_inventory[index];
			
			final Location l = entry.getValue();
			ItemStack globe = getPlanet(entry);
			
			menu.addItem(slot, new CustomItem(globe, entry.getKey(), "&8\u21E8 &7世界: &r" + l.getWorld().getName(), "&8\u21E8 &7X: &r" + l.getX(), "&8\u21E8 &7Y: &r" + l.getY(), "&8\u21E8 &7Z: &r" + l.getZ(), "&8\u21E8 &7传送耗时: &r" + (50 / TeleportationSequence.getSpeed(Slimefun.getGPSNetwork().getNetworkComplexity(uuid), source, l)) + "s", "", "&8\u21E8 &c单击选中"));
			menu.addMenuClickHandler(slot, (p1, arg1, arg2, arg3) -> {
                p1.closeInventory();
                TeleportationSequence.start(p1.getUniqueId(), complexity, source, l, false);
                return false;
            });
			
			index++;
		}
		
		menu.open(p);
	}

}
