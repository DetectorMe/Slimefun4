package io.github.thebusybiscuit.slimefun4.implementation.items.blocks;

import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config;
import me.mrCookieSlime.Slimefun.Lists.RecipeType;
import me.mrCookieSlime.Slimefun.Objects.Category;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.SimpleSlimefunItem;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.SlimefunItem;
import me.mrCookieSlime.Slimefun.Objects.handlers.BlockTicker;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import me.mrCookieSlime.Slimefun.api.SlimefunItemStack;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class InfusedHopper extends SimpleSlimefunItem<BlockTicker> {

    protected boolean silent = false;

    public InfusedHopper(Category category, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(category, item, recipeType, recipe);
    }

    @Override
    public BlockTicker getItemHandler() {
        return new BlockTicker() {

            @Override
            public void tick(Block b, SlimefunItem sfItem, Config data) {
                if (b.getType() != Material.HOPPER) {
                    // we're no longer a hopper, we were probably destroyed. skipping this tick.
                    BlockStorage.clearBlockInfo(b);
                    return;
                }

                Location l = b.getLocation().add(0.5, 1.2, 0.5);
                boolean sound = false;

                for (Entity item : b.getWorld().getNearbyEntities(l, 3.5D, 3.5D, 3.5D, n -> isValidItem(l, n))) {
                    item.setVelocity(new Vector(0, 0.1, 0));
                    item.teleport(l);
                    sound = true;
                }

                if (sound && !silent) {
                    b.getWorld().playSound(b.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1F, 2F);
                }
            }

            @Override
            public boolean isSynchronized() {
                return true;
            }
        };
    }

    private boolean isValidItem(Location l, Entity entity) {
        if (entity instanceof Item && entity.isValid()) {
            Item item = (Item) entity;
            return !SlimefunUtils.hasNoPickupFlag(item) && item.getLocation().distanceSquared(l) > 0.25;
        }

        return false;
    }
}
