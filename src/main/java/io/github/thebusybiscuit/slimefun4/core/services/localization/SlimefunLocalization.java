package io.github.thebusybiscuit.slimefun4.core.services.localization;

import java.util.Collection;
import java.util.List;
import java.util.function.UnaryOperator;

import org.bukkit.ChatColor;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import io.github.thebusybiscuit.cscorelib2.chat.ChatColors;
import io.github.thebusybiscuit.cscorelib2.config.Localization;
import me.mrCookieSlime.Slimefun.SlimefunPlugin;

public abstract class SlimefunLocalization extends Localization implements Keyed {

	public SlimefunLocalization(SlimefunPlugin plugin) {
		super(plugin);
	}

	public abstract Language getLanguage(String id);
	public abstract Language getLanguage(Player p);
	public abstract Language getDefaultLanguage();

	public abstract boolean hasLanguage(String id);
	public abstract Collection<Language> getLanguages();

	public String getMessage(Player p, String key) {
		Language language = getLanguage(p);
		return language.getMessages().getString(key);
	}

	public List<String> getMessages(Player p, String key) {
		Language language = getLanguage(p);
		return language.getMessages().getStringList(key);
	}

	public String getResearchName(Player p, NamespacedKey key) {
		Language language = getLanguage(p);
		if (language.getResearches() == null) return null;
		return language.getResearches().getString(key.getNamespace() + "." + key.getKey());
	}

	public String getCategoryName(Player p, NamespacedKey key) {
		Language language = getLanguage(p);
		if (language.getCategories() == null) return null;
		return language.getCategories().getString(key.getNamespace() + "." + key.getKey());
	}

	public String getResourceString(Player p, String key) {
		Language language = getLanguage(p);
		
		String value = language.getResources() != null ? language.getResources().getString(key): null;
		
		if (value != null) {
			return value;
		}
		else {
			return getLanguage("en").getResources().getString(key);
		}
	}

	@Override
	public void sendMessage(CommandSender sender, String key, boolean addPrefix) {
		String prefix = addPrefix ? getPrefix(): "";

		if (sender instanceof Player) {
			sender.sendMessage(ChatColors.color(prefix + getMessage((Player) sender, key)));
		}
		else {
			sender.sendMessage(ChatColor.stripColor(ChatColors.color(prefix + getMessage(key))));
		}
	}

	@Override
	public void sendMessage(CommandSender sender, String key) {
		sendMessage(sender, key, true);
	}

	public void sendMessage(CommandSender sender, String key, UnaryOperator<String> function) {
		sendMessage(sender, key, true, function);
	}

	@Override
	public void sendMessage(CommandSender sender, String key, boolean addPrefix, UnaryOperator<String> function) {
		String prefix = addPrefix ? getPrefix(): "";

		if (sender instanceof Player) {
			sender.sendMessage(ChatColors.color(prefix + function.apply(getMessage((Player) sender, key))));
		}
		else {
			sender.sendMessage(ChatColor.stripColor(ChatColors.color(prefix + function.apply(getMessage(key)))));
		}
	}

	@Override
	public void sendMessages(CommandSender sender, String key) {
		String prefix = getPrefix();

		if (sender instanceof Player) {
			for (String translation : getMessages((Player) sender, key)) {
				String message = ChatColors.color(prefix + translation);
				sender.sendMessage(message);
			}
		}
		else {
			for (String translation : getMessages(key)) {
				String message = ChatColors.color(prefix + translation);
				sender.sendMessage(ChatColor.stripColor(message));
			}
		}
	}

	@Override
	public void sendMessages(CommandSender sender, String key, boolean addPrefix, UnaryOperator<String> function) {
		String prefix = addPrefix ? getPrefix(): "";

		if (sender instanceof Player) {
			for (String translation : getMessages((Player) sender, key)) {
				String message = ChatColors.color(prefix + function.apply(translation));
				sender.sendMessage(message);
			}
		}
		else {
			for (String translation : getMessages(key)) {
				String message = ChatColors.color(prefix + function.apply(translation));
				sender.sendMessage(ChatColor.stripColor(message));
			}
		}
	}

	public void sendMessages(CommandSender sender, String key, UnaryOperator<String> function) {
		sendMessages(sender, key, true, function);
	}

}
