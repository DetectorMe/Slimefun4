package io.github.thebusybiscuit.slimefun4.core.attributes;

import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.SlimefunItem;

/**
 * This Interface, when attached to a class that inherits from {@link SlimefunItem}, marks
 * the Item as radioactive.
 * Carrying such an item will give the wearer the radiation effect.
 * 
 * You can specify a level of {@link Radioactivity} for the severity of the effect.
 * 
 * @author TheBusyBiscuit
 *
 */
public interface Radioactive {
	
	Radioactivity getRadioactivity();

}
