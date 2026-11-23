package tr237.tr237_stats.models;

import java.util.Locale;
import java.util.UUID;

public class PlayerStats {

    private final UUID uuid;
    private String name;
    private int kills;
    private int deaths;

    public PlayerStats(UUID uuid, String name, int kills, int deaths) {
        this.uuid = uuid;
        this.name = name;
        this.kills = Math.max(0, kills);
        this.deaths = Math.max(0, deaths);
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getKills() {
        return kills;
    }

    public void incrementKills() {
        this.kills++;
    }

    public int getDeaths() {
        return deaths;
    }

    public void incrementDeaths() {
        this.deaths++;
    }

    public void reset() {
        this.kills = 0;
        this.deaths = 0;
    }

    public double getKdRatio() {
        if (deaths == 0) {
            return kills;
        }
        return (double) kills / deaths;
    }

    public String getFormattedKd() {
        return String.format(Locale.US, "%.2f", getKdRatio());
    }
}