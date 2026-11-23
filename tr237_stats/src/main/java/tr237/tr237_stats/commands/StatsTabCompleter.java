package tr237.tr237_stats.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tr237.tr237_stats.database.DatabaseManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class StatsTabCompleter implements TabCompleter {

    private final DatabaseManager databaseManager;

    public StatsTabCompleter(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender,
                                                @NotNull Command command,
                                                @NotNull String label,
                                                @NotNull String[] args) {
        String cmdName = command.getName().toLowerCase(Locale.ROOT);
        if (cmdName.equals("topkills")) {
            return Collections.emptyList();
        }

        if (cmdName.equals("statsreset") && !sender.hasPermission("tr237.stats.admin") && !sender.isOp()) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            Set<String> candidates = new HashSet<>(databaseManager.getCachedPlayerNames());
            for (Player online : Bukkit.getOnlinePlayers()) {
                candidates.add(online.getName());
            }

            String prefix = args[0].toLowerCase(Locale.ROOT);
            List<String> matches = new ArrayList<>();
            for (String name : candidates) {
                if (name.toLowerCase(Locale.ROOT).startsWith(prefix)) {
                    matches.add(name);
                }
            }
            Collections.sort(matches);
            return matches;
        }

        return Collections.emptyList();
    }
}