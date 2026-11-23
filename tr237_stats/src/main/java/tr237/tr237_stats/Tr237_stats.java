package tr237.tr237_stats;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import tr237.tr237_stats.commands.StatsCommand;
import tr237.tr237_stats.commands.StatsTabCompleter;
import tr237.tr237_stats.database.DatabaseManager;
import tr237.tr237_stats.listeners.PlayerStatsListener;

public class Tr237_stats extends JavaPlugin {

    private DatabaseManager databaseManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.databaseManager = new DatabaseManager(this);
        this.databaseManager.connect();

        getServer().getPluginManager().registerEvents(new PlayerStatsListener(databaseManager), this);

        StatsCommand statsCommand = new StatsCommand(this, databaseManager);
        StatsTabCompleter tabCompleter = new StatsTabCompleter(databaseManager);

        registerCommand("stats", statsCommand, tabCompleter);
        registerCommand("topkills", statsCommand, tabCompleter);
        registerCommand("statsreset", statsCommand, tabCompleter);
    }

    private void registerCommand(String name, StatsCommand executor, StatsTabCompleter completer) {
        PluginCommand cmd = getCommand(name);
        if (cmd != null) {
            cmd.setExecutor(executor);
            cmd.setTabCompleter(completer);
        }
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.close();
        }
    }
}