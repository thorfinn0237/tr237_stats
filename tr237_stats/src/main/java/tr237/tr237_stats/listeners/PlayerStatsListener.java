package tr237.tr237_stats.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import tr237.tr237_stats.database.DatabaseManager;
import tr237.tr237_stats.models.PlayerStats;

public class PlayerStatsListener implements Listener {

    private final DatabaseManager databaseManager;

    public PlayerStatsListener(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        databaseManager.getOrCreateStats(player.getUniqueId(), player.getName());
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getPlayer();
        PlayerStats victimStats = databaseManager.getOrCreateStats(victim.getUniqueId(), victim.getName());
        victimStats.incrementDeaths();
        databaseManager.saveStatsAsync(victimStats);

        Player killer = victim.getKiller();
        if (killer != null && !killer.getUniqueId().equals(victim.getUniqueId())) {
            PlayerStats killerStats = databaseManager.getOrCreateStats(killer.getUniqueId(), killer.getName());
            killerStats.incrementKills();
            databaseManager.saveStatsAsync(killerStats);
        }
    }
}