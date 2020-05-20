package net.okocraft.autorestart.config;

import com.github.siroshun09.configapi.bukkit.BukkitConfig;
import net.okocraft.autorestart.AutoRestartPlugin;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MessageConfig extends BukkitConfig {

    private final AutoRestartPlugin plugin;

    public MessageConfig(@NotNull AutoRestartPlugin plugin) {
        super(plugin, "message.yml", true);
        this.plugin = plugin;
    }

    @NotNull
    public String getKickMessage() {
        return colorize(getString("restart.kick", "Server is restarting. Please wait a little..."));
    }

    @NotNull
    public String getCountdownMessage(long seconds) {
        return colorize(getPrefix() +
                getString("restart.countdown.message", "The server will restart in %time% seconds.")
                        .replace("%time%", String.valueOf(seconds)));
    }

    @NotNull
    public String getCountdownBarTitle(long seconds) {
        return colorize(getString("restart.countdown.bossbar", "&eThe server will restart in %time% seconds")
                .replace("%time%", String.valueOf(seconds)));
    }

    @NotNull
    public String getCancelRestarting() {
        return colorize(getPrefix() + getString("command.cancel", "Restart has been cancelled."));
    }

    @NotNull
    public String getCheckMessage() {
        String time = plugin.getRestartTimeAsString();
        if (time.isEmpty()) {
            return colorize(getPrefix() + getString("command.check.not-scheduled", "&7Restart is not scheduled."));
        } else {
            return colorize(getPrefix() +
                    getString("command.check.scheduled", "Restart has been scheduled at &b%time%")
                            .replace("%time%", time));
        }
    }

    @NotNull
    public String getRestartNowMessage() {
        return colorize(getPrefix() + getString("command.now", "Restart server now."));
    }

    @NotNull
    public String getReloadMessage() {
        return colorize(getPrefix() +
                getString("command.reload", "&bAutoRestart is reloading... Please check the server console."));
    }

    @NotNull
    public String getRestartSecondMessage(long second) {
        return colorize(getPrefix() + getString("command.second", "&cThe server will restart in &b%time% seconds")
                .replace("%time%", String.valueOf(second)));
    }

    @NotNull
    public String getRestartTimeMessage() {
        return colorize(getPrefix() + getString("command.time", "&cThe server will restart at &b%time%")
                .replace("%time%", plugin.getRestartTimeAsString()));
    }

    @NotNull
    public String getNoPermission(@NotNull String perm) {
        return colorize(getPrefix() +
                getString("command.no-permission", "&cYou don't have permission: %perm%").replace("%perm%", perm));
    }

    @NotNull
    public String getInvalidArg(@NotNull String arg) {
        return colorize(getPrefix() +
                getString("command.invalid-arg", "Invalid argument: &b%arg%").replace("%arg%", arg));
    }

    @NotNull
    public String getHelp() {
        return colorize(String.join("\n",
                getStringList("command.help", List.of(
                        "&8&m===========&e AutoRestart &8&m===========",
                        "&7 command: &b/autorestart &7(Alias: &b/are&7)",
                        "&7 ",
                        "&b /are cancel&8: &7Cancel restart task",
                        "&b /are check&8: &7Check the next restart",
                        "&b /are help&8: &7Show this help",
                        "&b /are now&8: &7Restart server now",
                        "&b /are reload&8: &7Reload config.yml and message.yml",
                        "&b /are reschedule&8: &7Schedule the next auto restart",
                        "&b /are schedule {seconds}&8: &7Schedule the restart task",
                        "&b /are time {HH:mm}&8: &7Schedule the restart task",
                        "&7 "))));
    }

    @NotNull
    private String getPrefix() {
        return getString("prefix", "&8[&6AutoRestart&8]&7 ");
    }

    @NotNull
    private String colorize(@NotNull String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
