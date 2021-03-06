package io.github.thebusybiscuit.slimefun4.implementation.items.androids;

import me.mrCookieSlime.Slimefun.Lists.RecipeType;
import me.mrCookieSlime.Slimefun.Objects.Category;
import me.mrCookieSlime.Slimefun.SlimefunPlugin;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import me.mrCookieSlime.Slimefun.api.SlimefunItemStack;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.function.Predicate;

public abstract class ButcherAndroid extends ProgrammableAndroid {

    private static final String METADATA_KEY = "android_killer";

    public ButcherAndroid(Category category, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(category, item, recipeType, recipe);
    }

    @Override
    public AndroidType getAndroidType() {
        return AndroidType.FIGHTER;
    }

    @Override
    protected void killEntities(Block b, double damage, Predicate<Entity> predicate) {
        double radius = 4.0 + getTier();

        for (Entity n : b.getWorld().getNearbyEntities(b.getLocation(), radius, radius, radius, n -> n instanceof LivingEntity && !(n instanceof ArmorStand) && !(n instanceof Player) && n.isValid() && predicate.test(n))) {
            boolean attack = false;

            switch (BlockFace.valueOf(BlockStorage.getLocationInfo(b.getLocation(), "rotation"))) {
                case NORTH:
                    attack = n.getLocation().getZ() < b.getZ();
                    break;
                case EAST:
                    attack = n.getLocation().getX() > b.getX();
                    break;
                case SOUTH:
                    attack = n.getLocation().getZ() > b.getZ();
                    break;
                case WEST:
                    attack = n.getLocation().getX() < b.getX();
                    break;
                default:
                    break;
            }

            if (attack) {
                if (n.hasMetadata(METADATA_KEY)) {
                    n.removeMetadata(METADATA_KEY, SlimefunPlugin.instance);
                }

                n.setMetadata(METADATA_KEY, new FixedMetadataValue(SlimefunPlugin.instance, new AndroidInstance(this, b)));

                ((LivingEntity) n).damage(damage);
                break;
            }
        }
    }

}
