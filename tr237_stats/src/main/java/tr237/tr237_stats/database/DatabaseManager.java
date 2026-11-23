package tr237.tr237_stats.database;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import tr237.tr237_stats.models.PlayerStats;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class DatabaseManager {

    private final Plugin plugin;
    private final Map<UUID, PlayerStats> cache = new ConcurrentHashMap<>();
    private Connection connection;

    public DatabaseManager(Plugin plugin) {
        this.plugin = plugin;
    }

    public void connect() {
        try {
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }
            File dbFile = new File(plugin.getDataFolder(), "stats.db");
            Class.forName("org.sqlite.JDBC");
            this.connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());

            try (Statement stmt = connection.createStatement()) {
                stmt.executeUpdate("""
                        CREATE TABLE IF NOT EXISTS player_stats (
                            uuid TEXT PRIMARY KEY,
                            name TEXT NOT NULL,
                            kills INTEGER NOT NULL DEFAULT 0,
                            deaths INTEGER NOT NULL DEFAULT 0
                        )
                        """);
            }

            loadAllIntoCache();
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to connect to SQLite database: " + e.getMessage());
        }
    }

    private void loadAllIntoCache() {
        String sql = "SELECT uuid, name, kills, deaths FROM player_stats";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                UUID uuid = UUID.fromString(rs.getString("uuid"));
                String name = rs.getString("name");
                int kills = rs.getInt("kills");
                int deaths = rs.getInt("deaths");
                cache.put(uuid, new PlayerStats(uuid, name, kills, deaths));
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Error loading player stats: " + e.getMessage());
        }
    }

    public PlayerStats getOrCreateStats(UUID uuid, String playerName) {
        PlayerStats stats = cache.computeIfAbsent(uuid, id -> new PlayerStats(id, playerName, 0, 0));
        if (playerName != null && !playerName.isBlank() && !playerName.equals(stats.getName())) {
            stats.setName(playerName);
            saveStatsAsync(stats);
        }
        return stats;
    }

    public PlayerStats getStatsByName(String name) {
        for (PlayerStats stats : cache.values()) {
            if (stats.getName().equalsIgnoreCase(name)) {
                return stats;
            }
        }
        return null;
    }

    public List<PlayerStats> getTopKills(int limit) {
        return cache.values().stream()
                .sorted(Comparator.comparingInt(PlayerStats::getKills).reversed()
                        .thenComparingDouble(PlayerStats::getKdRatio).reversed())
                .limit(Math.max(1, limit))
                .toList();
    }

    public void saveStatsAsync(PlayerStats stats) {
        PlayerStats snapshot = new PlayerStats(stats.getUuid(), stats.getName(), stats.getKills(), stats.getDeaths());
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> writeStatsToDb(snapshot));
    }

    private synchronized void writeStatsToDb(PlayerStats stats) {
        if (connection == null) return;
        String sql = """
                INSERT INTO player_stats (uuid, name, kills, deaths)
                VALUES (?, ?, ?, ?)
                ON CONFLICT(uuid) DO UPDATE SET
                    name = excluded.name,
                    kills = excluded.kills,
                    deaths = excluded.deaths
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, stats.getUuid().toString());
            ps.setString(2, stats.getName());
            ps.setInt(3, stats.getKills());
            ps.setInt(4, stats.getDeaths());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to save stats for " + stats.getName() + ": " + e.getMessage());
        }
    }

    public void resetStats(UUID uuid, String name, Consumer<Boolean> callback) {
        PlayerStats stats = getOrCreateStats(uuid, name);
        stats.reset();
        saveStatsAsync(stats);
        if (callback != null) {
            callback.accept(true);
        }
    }

    public List<String> getCachedPlayerNames() {
        List<String> names = new ArrayList<>();
        for (PlayerStats stats : cache.values()) {
            names.add(stats.getName());
        }
        return names;
    }

    public void close() {
        for (PlayerStats stats : cache.values()) {
            writeStatsToDb(stats);
        }
        cache.clear();
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ignored) {
            }
        }
    }
}