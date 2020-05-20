package net.okocraft.autorestart.tasks;

import net.okocraft.autorestart.AutoRestartPlugin;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class RestartTask implements Runnable {

    private final AutoRestartPlugin plugin;

    public RestartTask(@NotNull AutoRestartPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        plugin.getGeneralConfig().getCommandsBeforeRestart().forEach(this::executeRestartCommand);

        if (plugin.getGeneralConfig().isKickBefore()) {
            kickPlayers();
        }

        if (plugin.getTimer().isRunning()) {
            plugin.getTimer().stop();
        }

        executeRestartCommand(plugin.getGeneralConfig().getRestartCommand());
    }

    private void executeRestartCommand(@NotNull String command) {
        if (!plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command)) {
            plugin.getLogger().warning("Execution failed: " + command);
        }
    }

    private void kickPlayers() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            player.kickPlayer(plugin.getMessageConfig().getKickMessage());
        }
    }
}
