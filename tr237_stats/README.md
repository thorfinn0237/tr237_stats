# tr237_stats

Player statistics and leaderboard plugin for **Paper 1.21.4** (Java 21) under group `tr237`, backed by asynchronous **SQLite** storage.

## Features
- Automatic tracking of **Kills**, **Deaths**, and formatted **K/D Ratio**.
- Asynchronous SQLite persistence (`stats.db`) with in-memory caching for zero main-thread lag.
- Customizable messages and stats card in `config.yml`.
- Top 10 kills leaderboard (`/topkills`) and admin stats reset (`/statsreset <player>`).

## Commands & Permissions
- `/stats [player]` — View your stats or another player's stats (`tr237.stats.use`).
- `/topkills` — View top 10 players by kills (`tr237.stats.use`).
- `/statsreset <player>` — Reset a player's stats (`tr237.stats.admin` or OP).

## Build
```bash
mvn clean package