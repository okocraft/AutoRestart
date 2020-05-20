package net.okocraft.autorestart.config;

import com.github.siroshun09.configapi.bukkit.BukkitConfig;
import net.okocraft.autorestart.AutoRestartPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GeneralConfig extends BukkitConfig {

    private final AutoRestartPlugin plugin;

    public GeneralConfig(@NotNull AutoRestartPlugin plugin) {
        super(plugin, "config.yml", true);
        this.plugin = plugin;
    }

    @NotNull
    public String getRestartCommand() {
        return getString("restart.command", "restart");
    }

    @NotNull
    public List<String> getCommandsBeforeRestart() {
        return getStringList("restart.commands-before");
    }

    @NotNull
    public List<Long> getSecondsToBroadcast() {
        return getLongList("restart.seconds-to-broadcast", List.of(1L, 2L, 3L, 4L, 5L, 10L, 30L, 60L));
    }

    public long getDefaultNoticeTime() {
        return getLong("restart.default-notice-time", 60L);
    }

    public boolean isBossBarEnabled() {
        return getBoolean("restart.enable-bossbar", true);
    }

    public boolean isKickBefore() {
        return getBoolean("restart.kick-before", true);
    }

    @Nullable
    public LocalDateTime getNextAutoRestartTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        Set<Long> durations = new HashSet<>();
        LocalDateTime now = LocalDateTime.now().withNano(0);

        for (String strTime : getStringList("restart.auto.time")) {
            try {
                LocalTime time = LocalTime.parse(strTime, formatter).withSecond(0).withNano(0);

                long duration = Duration.between(now, time.atDate(now.toLocalDate())).getSeconds();
                if (duration < 1) {
                    duration += 86400;
                }

                durations.add(duration);
            } catch (DateTimeParseException e) {
                plugin.getLogger().severe("Invalid time format: " + strTime);
            }
        }

        long nextRestartSeconds = durations.stream().sorted().findFirst().orElse(0L);
        return nextRestartSeconds != 0 ? now.plusSeconds(nextRestartSeconds) : null;
    }
}
