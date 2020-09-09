package net.okocraft.autorestart.command;

import net.okocraft.autorestart.AutoRestartPlugin;
import net.okocraft.autorestart.tasks.RestartTask;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AutoRestartCommand implements CommandExecutor, TabCompleter {

    private final AutoRestartPlugin plugin;

    public AutoRestartCommand(@NotNull AutoRestartPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "cancel":
                cancelRestarting(sender);
                break;
            case "check":
                sendRestartTime(sender);
                break;
            case "now":
                restartNow(sender);
                break;
            case "reload":
                reload(sender);
                break;
            case "reschedule":
                rescheduleRestarting(sender);
                break;
            case "restart":
                scheduleRestartingSecond(sender, args);
                break;
            case "time":
                scheduleRestartingTime(sender, args);
                break;
            default:
                sendHelp(sender);
        }

        return true;
    }

    @Override
    @NotNull
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {
        if (args.length == 1) {
            return StringUtil.copyPartialMatches(
                    args[0].toLowerCase(),
                    List.of("cancel", "check", "help", "now", "reload", "reschedule", "restart", "time"),
                    new ArrayList<>()
            );
        } else {
            return Collections.emptyList();
        }
    }

    private void cancelRestarting(@NotNull CommandSender sender) {
        if (checkPermission(sender, "autorestart.cancel")) {
            plugin.cancelAllTask();
            sender.sendMessage(plugin.getMessageConfig().getCancelRestarting());
        }
    }


    private void reload(@NotNull CommandSender sender) {
        if (checkPermission(sender, "autorestart.reload")) {
            sender.sendMessage(plugin.getMessageConfig().getReloadMessage());
            plugin.reload();
            sender.sendMessage(plugin.getMessageConfig().getCheckMessage());
        }
    }

    private void rescheduleRestarting(@NotNull CommandSender sender) {
        if (checkPermission(sender, "autorestart.reschedule")) {
            plugin.scheduleRestarting();
            sender.sendMessage(plugin.getMessageConfig().getCheckMessage());
        }
    }

    private void restartNow(@NotNull CommandSender sender) {
        if (checkPermission(sender, "autorestart.now")) {
            plugin.cancelAllTask();
            plugin.getServer().getScheduler().runTask(plugin, new RestartTask(plugin));
            sender.sendMessage(plugin.getMessageConfig().getRestartNowMessage());
        }
    }


    private void sendHelp(@NotNull CommandSender sender) {
        if (checkPermission(sender, "autorestart.help")) {
            sender.sendMessage(plugin.getMessageConfig().getHelp());
        }
    }

    private void scheduleRestartingSecond(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!checkPermission(sender, "autorestart.restart")) {
            return;
        }

        long seconds;
        if (args.length < 2) {
            seconds = plugin.getGeneralConfig().getDefaultNoticeTime();
        } else {
            try {
                seconds = Long.parseLong(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage(plugin.getMessageConfig().getInvalidArg(args[1]));
                return;
            }
        }

        if (seconds < 1) {
            sender.sendMessage(plugin.getMessageConfig().getInvalidArg(args[1]));
            return;
        }

        String reason = 2 < args.length ? args[2] : null;
        String formattedReason = reason != null ? plugin.getMessageConfig().getFormattedReason(reason) : "";

        if (!plugin.getGeneralConfig().getSecondsToBroadcast().contains(seconds)) {
            plugin.getServer().broadcastMessage(plugin.getMessageConfig().getRestartSecondMessage(seconds, formattedReason));
        }

        plugin.scheduleRestarting(seconds, reason);
    }

    private void scheduleRestartingTime(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!checkPermission(sender, "autorestart.time")) {
            return;
        }

        LocalDateTime restartTime;
        if (args.length < 2) {
            restartTime = plugin.getGeneralConfig().getNextAutoRestartTime();
            restartTime = restartTime != null ? restartTime :
                    LocalDateTime.now().plusSeconds(plugin.getGeneralConfig().getDefaultNoticeTime());
        } else {
            try {
                LocalTime time = LocalTime.parse(args[1], DateTimeFormatter.ofPattern("HH:mm"));
                restartTime = time.withSecond(0).withNano(0).atDate(LocalDate.now());
            } catch (DateTimeParseException e) {
                sender.sendMessage(plugin.getMessageConfig().getInvalidArg(args[1]));
                return;
            }
        }

        long duration = Duration.between(LocalDateTime.now(), restartTime).getSeconds() + 1;
        if (duration < 1) {
            duration += 86400;
        }

        String reason = 2 < args.length ? args[2] : null;
        String formattedReason = reason != null ? plugin.getMessageConfig().getFormattedReason(reason) : "";

        plugin.scheduleRestarting(duration, reason);

        if (!plugin.getGeneralConfig().getSecondsToBroadcast().contains(duration)) {
            plugin.getServer().broadcastMessage(plugin.getMessageConfig().getRestartTimeMessage(formattedReason));
        }
    }

    private void sendRestartTime(@NotNull CommandSender sender) {
        if (checkPermission(sender, "autorestart.check")) {
            sender.sendMessage(plugin.getMessageConfig().getCheckMessage());
        }
    }

    private boolean checkPermission(@NotNull CommandSender sender, @NotNull String perm) {
        if (sender.hasPermission(perm)) {
            return true;
        } else {
            sender.sendMessage(plugin.getMessageConfig().getNoPermission(perm));
            return false;
        }
    }
}
