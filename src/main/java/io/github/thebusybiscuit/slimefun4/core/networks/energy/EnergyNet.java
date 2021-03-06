package io.github.thebusybiscuit.slimefun4.core.networks.energy;

import io.github.thebusybiscuit.cscorelib2.math.DoubleHandler;
import io.github.thebusybiscuit.slimefun4.api.network.Network;
import io.github.thebusybiscuit.slimefun4.api.network.NetworkComponent;
import io.github.thebusybiscuit.slimefun4.core.attributes.EnergyNetComponent;
import io.github.thebusybiscuit.slimefun4.utils.holograms.SimpleHologram;
import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.SlimefunItem;
import me.mrCookieSlime.Slimefun.SlimefunPlugin;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import me.mrCookieSlime.Slimefun.api.Slimefun;
import me.mrCookieSlime.Slimefun.api.energy.ChargableBlock;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.HashSet;
import java.util.Set;

/**
 * The {@link EnergyNet} is an implementation of {@link Network} that deals with
 * electrical energy being send from and to nodes.
 *
 * @author meiamsome
 * @author TheBusyBiscuit
 * @see Network
 * @see EnergyNetComponent
 * @see EnergyNetComponentType
 */
public class EnergyNet extends Network {

    private static final int RANGE = 6;

    public static EnergyNetComponentType getComponent(Block b) {
        return getComponent(b.getLocation());
    }

    public static EnergyNetComponentType getComponent(String id) {
        if (SlimefunPlugin.getRegistry().getEnergyGenerators().contains(id)) return EnergyNetComponentType.GENERATOR;
        if (SlimefunPlugin.getRegistry().getEnergyCapacitors().contains(id)) return EnergyNetComponentType.CAPACITOR;
        if (SlimefunPlugin.getRegistry().getEnergyConsumers().contains(id)) return EnergyNetComponentType.CONSUMER;
        return EnergyNetComponentType.NONE;
    }

    public static EnergyNetComponentType getComponent(Location l) {
        if (!BlockStorage.hasBlockInfo(l)) {
            return EnergyNetComponentType.NONE;
        }

        String id = BlockStorage.checkID(l);

        if (SlimefunPlugin.getRegistry().getEnergyGenerators().contains(id)) {
            return EnergyNetComponentType.GENERATOR;
        }

        if (SlimefunPlugin.getRegistry().getEnergyCapacitors().contains(id)) {
            return EnergyNetComponentType.CAPACITOR;
        }

        if (SlimefunPlugin.getRegistry().getEnergyConsumers().contains(id)) {
            return EnergyNetComponentType.CONSUMER;
        }

        return EnergyNetComponentType.NONE;
    }

    public static EnergyNet getNetworkFromLocation(Location l) {
        return SlimefunPlugin.getNetworkManager().getNetworkFromLocation(l, EnergyNet.class);
    }

    public static EnergyNet getNetworkFromLocationOrCreate(Location l) {
        EnergyNet energyNetwork = getNetworkFromLocation(l);

        if (energyNetwork == null) {
            energyNetwork = new EnergyNet(l);
            SlimefunPlugin.getNetworkManager().registerNetwork(energyNetwork);
        }

        return energyNetwork;
    }

    private final Set<Location> generators = new HashSet<>();
    private final Set<Location> storage = new HashSet<>();
    private final Set<Location> consumers = new HashSet<>();

    protected EnergyNet(Location l) {
        super(l);
    }

    @Override
    public int getRange() {
        return RANGE;
    }

    @Override
    public NetworkComponent classifyLocation(Location l) {
        if (regulator.equals(l)) return NetworkComponent.REGULATOR;
        switch (getComponent(l)) {
            case CAPACITOR:
                return NetworkComponent.CONNECTOR;
            case CONSUMER:
            case GENERATOR:
                return NetworkComponent.TERMINUS;
            default:
                return null;
        }
    }

    @Override
    public void onClassificationChange(Location l, NetworkComponent from, NetworkComponent to) {
        if (from == NetworkComponent.TERMINUS) {
            generators.remove(l);
            consumers.remove(l);
        }

        switch (getComponent(l)) {
            case CAPACITOR:
                storage.add(l);
                break;
            case CONSUMER:
                consumers.add(l);
                break;
            case GENERATOR:
                generators.add(l);
                break;
            default:
                break;
        }
    }

    public void tick(Block b) {
        if (!regulator.equals(b.getLocation())) {
            SimpleHologram.update(b, "&4检测到连接至了多个能源调节器");
            return;
        }

        super.tick();

        if (connectorNodes.isEmpty() && terminusNodes.isEmpty()) {
            SimpleHologram.update(b, "&4找不到能源网络");
        } else {
            double supply = DoubleHandler.fixDouble(tickAllGenerators() + tickAllCapacitors());
            double demand = 0;

            int available = (int) supply;

            for (Location destination : consumers) {
                int capacity = ChargableBlock.getMaxCharge(destination);
                int charge = ChargableBlock.getCharge(destination);

                if (charge < capacity) {
                    int rest = capacity - charge;
                    demand += rest;

                    if (available > 0) {
                        if (available > rest) {
                            ChargableBlock.setUnsafeCharge(destination, capacity, false);
                            available = available - rest;
                        } else {
                            ChargableBlock.setUnsafeCharge(destination, charge + available, false);
                            available = 0;
                        }
                    }
                }
            }

            for (Location battery : storage) {
                if (available > 0) {
                    int capacity = ChargableBlock.getMaxCharge(battery);

                    if (available > capacity) {
                        ChargableBlock.setUnsafeCharge(battery, capacity, true);
                        available = available - capacity;
                    } else {
                        ChargableBlock.setUnsafeCharge(battery, available, true);
                        available = 0;
                    }
                } else ChargableBlock.setUnsafeCharge(battery, 0, true);
            }

            for (Location source : generators) {
                if (ChargableBlock.isChargable(source)) {
                    if (available > 0) {
                        int capacity = ChargableBlock.getMaxCharge(source);

                        if (available > capacity) {
                            ChargableBlock.setUnsafeCharge(source, capacity, false);
                            available = available - capacity;
                        } else {
                            ChargableBlock.setUnsafeCharge(source, available, false);
                            available = 0;
                        }
                    } else ChargableBlock.setUnsafeCharge(source, 0, false);
                }
            }

            updateHologram(b, supply, demand);
        }
    }

    private double tickAllGenerators() {
        double supply = 0;
        Set<Location> exploded = new HashSet<>();

        for (Location source : generators) {
            long timestamp = System.currentTimeMillis();
            SlimefunItem item = BlockStorage.check(source);
            Config config = BlockStorage.getLocationInfo(source);

            double energy = item.getEnergyTicker().generateEnergy(source, item, config);

            if (item.getEnergyTicker().explode(source)) {
                exploded.add(source);
                BlockStorage.clearBlockInfo(source);

                Slimefun.runSync(() -> {
                    source.getBlock().setType(Material.LAVA);
                    source.getWorld().createExplosion(source, 0F, false);
                });
            } else {
                supply += energy;
            }

            SlimefunPlugin.getTicker().addBlockTimings(source, System.currentTimeMillis() - timestamp);
        }

        generators.removeAll(exploded);

        return supply;
    }

    private double tickAllCapacitors() {
        double supply = 0;

        for (Location battery : storage) {
            supply += ChargableBlock.getCharge(battery);
        }

        return supply;
    }

    private void updateHologram(Block b, double supply, double demand) {
        if (demand > supply) {
            String netLoss = DoubleHandler.getFancyDouble(Math.abs(supply - demand));
            SimpleHologram.update(b, "&4&l- &c" + netLoss + " &7J &e\u26A1");
        } else {
            String netGain = DoubleHandler.getFancyDouble(supply - demand);
            SimpleHologram.update(b, "&2&l+ &a" + netGain + " &7J &e\u26A1");
        }
    }
}
