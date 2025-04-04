package nl.mtvehicles.core.listeners;

import nl.mtvehicles.core.Main;
import nl.mtvehicles.core.infrastructure.dataconfig.DefaultConfig;
import nl.mtvehicles.core.infrastructure.modules.ConfigModule;
import nl.mtvehicles.core.infrastructure.vehicle.VehicleData;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TNTSpawnListener implements Listener {

    private final Map<UUID, Long> cooldownMap = new HashMap<>();

    @EventHandler
    public void onPlayerClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        trySpawnTNT(event.getPlayer());
    }

    @EventHandler
    public void onPlayerClickAtEntity(PlayerInteractAtEntityEvent event) {
        trySpawnTNT(event.getPlayer());
    }

    private void trySpawnTNT(Player player) {
        if (!(boolean) ConfigModule.defaultConfig.get(DefaultConfig.Option.AIRPLANE_TNT)) return;
        if (player.getInventory().getItemInMainHand().getType() != Material.TNT) return;
        Entity vehicle = player.getVehicle();
        if (!(vehicle instanceof ArmorStand)) return;
        String customName = vehicle.getCustomName();
        if (customName == null || !customName.startsWith("MTVEHICLES_MAINSEAT_")) return;
        String license = customName.replace("MTVEHICLES_MAINSEAT_", "");
        if (license.isEmpty() || !VehicleData.type.get(license).isAirplane()) return;
        
        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        long lastTime = cooldownMap.getOrDefault(playerId, 0L);
        
        int cooldown = ((int) ConfigModule.defaultConfig.get(DefaultConfig.Option.AIRPLANE_COOLDOWN)) * 1000;
        if (currentTime - lastTime >= cooldown) {
            spawnFallingTNT(player);
            cooldownMap.put(playerId, currentTime);
        }
    }

    private void spawnFallingTNT(Player player) {
        Location playerLocation = player.getLocation().clone().add(0, -1, 0);
        player.getWorld().spawn(playerLocation, TNTPrimed.class);
        ItemStack mainHandItem = player.getInventory().getItemInMainHand();
        mainHandItem.setAmount(mainHandItem.getAmount() - 1);
    }
}
