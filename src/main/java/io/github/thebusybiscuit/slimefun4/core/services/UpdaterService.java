package io.github.thebusybiscuit.slimefun4.core.services;

import java.io.File;
import java.util.logging.Level;

import org.bukkit.plugin.Plugin;

import io.github.thebusybiscuit.cscorelib2.updater.GitHubBuildsUpdater;
import io.github.thebusybiscuit.cscorelib2.updater.Updater;
import io.github.thebusybiscuit.slimefun4.api.SlimefunBranch;

public class UpdaterService {

	private final Updater updater;
	private final SlimefunBranch branch;

	public UpdaterService(Plugin plugin, File file) {
		String version = plugin.getDescription().getVersion();

		if (version.equals("UNOFFICIAL")) {
			// This Server is using a modified build that is not a public release.
			plugin.getLogger().log(Level.WARNING, "##################################################");
			plugin.getLogger().log(Level.WARNING, "It looks like you are using an unofficially modified build of Slimefun!");
			plugin.getLogger().log(Level.WARNING, "Auto-Updates have been disabled, this build is not considered safe.");
			plugin.getLogger().log(Level.WARNING, "Do not report bugs encountered in this Version of Slimefun.");
			plugin.getLogger().log(Level.WARNING, "##################################################");
			updater = null;
			branch = SlimefunBranch.UNOFFICIAL;
		}
		else if (version.startsWith("DEV - ")) {
			// If we are using a development build, we want to switch to our custom 
			updater = new GitHubBuildsUpdater(plugin, file, "TheBusyBiscuit/Slimefun4/master");
			branch = SlimefunBranch.DEVELOPMENT;
		}
		else if (version.startsWith("RC - ")) {
			// If we are using a "stable" build, we want to switch to our custom 
			updater = new GitHubBuildsUpdater(plugin, file, "TheBusyBiscuit/Slimefun4/stable", "RC - ");
			branch = SlimefunBranch.STABLE;
		}
		else {
			updater = null;
			branch = SlimefunBranch.UNKNOWN;
		}
	}
	
	public SlimefunBranch getBranch() {
		return branch;
	}

	public void start() {
		if (updater != null) {
			updater.start();
		}
	}

}
