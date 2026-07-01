package fr.mrmicky.fastinv;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public final class FastInvManager {

    private static final AtomicBoolean REGISTERED = new AtomicBoolean(false);
    private FastInvManager() {
        throw new UnsupportedOperationException();
    }

    public static void register(Plugin plugin) {
        Objects.requireNonNull(plugin, "plugin");

        if (REGISTERED.getAndSet(true)) {
            throw new IllegalStateException("FastInv is already registered");
        }

        Bukkit.getPluginManager().registerEvents(new InventoryListener(plugin), plugin);
    }

    public static final class InventoryListener implements Listener {

        private final Plugin plugin;

        public InventoryListener(Plugin plugin) {
            this.plugin = plugin;
        }

        @EventHandler
        public void onInventoryClick(InventoryClickEvent e) {
            FastInv inv = getFastInv(e.getInventory());

            if (inv != null && e.getClickedInventory() != null) {
                boolean wasCancelled = e.isCancelled();
                e.setCancelled(true);

                inv.handleClick(e);

                if (!wasCancelled && !e.isCancelled()) {
                    e.setCancelled(false);
                }
            }
        }

        @EventHandler
        public void onInventoryDrag(InventoryDragEvent e) {
            FastInv inv = getFastInv(e.getInventory());

            if (inv != null) {
                boolean wasCancelled = e.isCancelled();
                e.setCancelled(true);

                inv.handleDrag(e);

                if (!wasCancelled && !e.isCancelled()) {
                    e.setCancelled(false);
                }
            }
        }

        @EventHandler
        public void onInventoryOpen(InventoryOpenEvent e) {
            FastInv inv = getFastInv(e.getInventory());

            if (inv != null) {
                inv.handleOpen(e);
            }
        }

        @EventHandler
        public void onInventoryClose(InventoryCloseEvent e) {
            FastInv inv = getFastInv(e.getInventory());

            if (inv != null) {
                if (inv.handleClose(e)) {
                    Player player = (Player) e.getPlayer();
                    if (isFolia()) {
                        player.getScheduler().run(this.plugin, scheduledTask -> inv.open(player), null);
                    } else {
                        Bukkit.getScheduler().runTask(this.plugin, () -> inv.open(player));
                    }
                }
            }
        }

        private FastInv getFastInv(Inventory inventory) {
            if (inventory == null) {
                return null;
            }
            try {
                if (inventory.getHolder() instanceof FastInv) {
                    return (FastInv) inventory.getHolder();
                }
            } catch (Throwable t) {
                // ignore
            }
            return null;
        }

        private boolean isFastInv(Inventory inventory) {
            return getFastInv(inventory) != null;
        }

        private boolean isFolia() {
            try {
                Class.forName("io.papermc.paper.threadedregions.scheduler.RegionScheduler");
                return true;
            } catch (ClassNotFoundException e) {
                return false;
            }
        }

        @EventHandler
        public void onPluginDisable(PluginDisableEvent e) {
            if (e.getPlugin() == this.plugin) {
                REGISTERED.set(false);
            }
        }
    }
}
