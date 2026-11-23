package tr237.tr237_stats.commands;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import tr237.tr237_stats.Tr237_stats;
import tr237.tr237_stats.database.DatabaseManager;
import tr237.tr237_stats.models.PlayerStats;

import java.util.List;

public class StatsCommand implements CommandExecutor {

    private final Tr237_stats plugin;
    private final DatabaseManager databaseManager;

    public StatsCommand(Tr237_stats plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
    }

    private void send(CommandSender sender, String raw) {
        if (raw != null && !raw.isEmpty()) {
            sender.sendMessage(LegacyComponentSerializer.legacySection().deserialize(raw.replace('&', '§')));
        }
    }

    private String msg(String path) {
        return plugin.getConfig().getString("messages." + path, "");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             @NotNull String[] args) {
        String cmdName = command.getName().toLowerCase();

        if (cmdName.equals("topkills")) {
            return handleTopKills(sender);
        }

        if (cmdName.equals("statsreset")) {
            return handleStatsReset(sender, args);
        }

        return handleStatsView(sender, args);
    }

    private boolean handleStatsView(CommandSender sender, String[] args) {
        PlayerStats targetStats;

        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                send(sender, msg("only-players"));
                return true;
            }
            targetStats = databaseManager.getOrCreateStats(player.getUniqueId(), player.getName());
        } else {
            String targetName = args[0];
            Player onlineTarget = Bukkit.getPlayerExact(targetName);
            if (onlineTarget != null) {
                targetStats = databaseManager.getOrCreateStats(onlineTarget.getUniqueId(), onlineTarget.getName());
            } else {
                targetStats = databaseManager.getStatsByName(targetName);
            }

            if (targetStats == null) {
                send(sender, msg("player-not-found").replace("%player%", targetName));
                return true;
            }
        }

        List<String> lines = plugin.getConfig().getStringList("messages.stats-card");
        for (String line : lines) {
            String formatted = line
                    .replace("%player%", targetStats.getName())
                    .replace("%kills%", String.valueOf(targetStats.getKills()))
                    .replace("%deaths%", String.valueOf(targetStats.getDeaths()))
                    .replace("%kd%", targetStats.getFormattedKd());
            send(sender, formatted);
        }
        return true;
    }

    private boolean handleTopKills(CommandSender sender) {
        List<PlayerStats> topList = databaseManager.getTopKills(10);
        send(sender, msg("top-header"));

        if (topList.isEmpty()) {
            send(sender, msg("top-empty"));
            return true;
        }

        String template = msg("top-entry");
        int rank = 1;
        for (PlayerStats entry : topList) {
            String formatted = template
                    .replace("%rank%", String.valueOf(rank++))
                    .replace("%player%", entry.getName())
                    .replace("%kills%", String.valueOf(entry.getKills()))
                    .replace("%deaths%", String.valueOf(entry.getDeaths()))
                    .replace("%kd%", entry.getFormattedKd());
            send(sender, formatted);
        }
        return true;
    }

    private boolean handleStatsReset(CommandSender sender, String[] args) {
        if (!sender.hasPermission("tr237.stats.admin") && !sender.isOp()) {
            send(sender, msg("no-permission"));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(LegacyComponentSerializer.legacySection().deserialize("§cUsage: /statsreset <player>"));
            return true;
        }

        String targetName = args[0];
        Player onlineTarget = Bukkit.getPlayerExact(targetName);
        if (onlineTarget != null) {
            databaseManager.resetStats(onlineTarget.getUniqueId(), onlineTarget.getName(), ok ->
                    send(sender, msg("reset-success").replace("%player%", onlineTarget.getName())));
            return true;
        }

        PlayerStats cached = databaseManager.getStatsByName(targetName);
        if (cached == null) {
            send(sender, msg("player-not-found").replace("%player%", targetName));
            return true;
        }

        databaseManager.resetStats(cached.getUuid(), cached.getName(), ok ->
                send(sender, msg("reset-success").replace("%player%", cached.getName())));
        return true;
    }
}