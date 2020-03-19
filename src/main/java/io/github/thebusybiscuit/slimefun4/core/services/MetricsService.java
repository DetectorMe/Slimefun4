package io.github.thebusybiscuit.slimefun4.core.services;

import java.util.HashMap;
import java.util.Map;

import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import io.github.thebusybiscuit.slimefun4.core.services.localization.Language;
import me.mrCookieSlime.Slimefun.SlimefunPlugin;

public class MetricsService extends Metrics {

	public MetricsService(SlimefunPlugin plugin) {
		super(plugin, 4574);

		addCustomChart(new SimplePie("auto_updates", () -> 
			SlimefunPlugin.getCfg().getBoolean("options.auto-update") ? "enabled": "disabled"
		));

		addCustomChart(new SimplePie("resourcepack", () -> {
			String version = SlimefunPlugin.getItemTextureService().getVersion();

			if (version != null && version.startsWith("v")) {
				return version + " (Official)";
			}
			else if (SlimefunPlugin.getItemTextureService().isActive()) {
				return "Custom / Modified";
			}
			else {
				return "None";
			}
		}));

		addCustomChart(new SimplePie("branch", SlimefunPlugin.getUpdater().getBranch()::getName));

		addCustomChart(new SimplePie("language", () -> {
			Language language = SlimefunPlugin.getLocal().getDefaultLanguage();
			return SlimefunPlugin.getLocal().isLanguageLoaded(language.getID()) ? language.getID(): "Unsupported Language";
		}));

		addCustomChart(new AdvancedPie("player_languages", () -> {
			Map<String, Integer> languages = new HashMap<>();

			for (Player p : Bukkit.getOnlinePlayers()) {
				Language language = SlimefunPlugin.getLocal().getLanguage(p);
				String lang = SlimefunPlugin.getLocal().isLanguageLoaded(language.getID()) ? language.getID(): "Unsupported Language";
				languages.merge(lang, 1, Integer::sum);
			}

			return languages;
		}));
	}

}
