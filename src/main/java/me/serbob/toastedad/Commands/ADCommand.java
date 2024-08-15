package me.serbob.toastedad.Commands;

import me.serbob.toastedad.ToastedAD;
import me.serbob.toastedad.Events.PlayerAdvertiseEvent;
import me.serbob.toastedad.Utils.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.List;

public class ADCommand implements CommandExecutor {
    private static final int DEFAULT_COOLDOWN = Integer.MAX_VALUE;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatUtil.c("&cOnly players can use this command!"));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("toastedad.use")) {
            player.sendMessage(ChatUtil.c(ToastedAD.instance.getConfig().getString("Messages.no_perms", "&cYou don't have permission to use this command.")));
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(ChatUtil.c(ToastedAD.instance.getConfig().getString("Messages.usage", "&cUsage: /ad <message> or /ad <time>")));
            return true;
        }

        if (args[0].equalsIgnoreCase("<time>")) {
            handleTimeCommand(player);
        } else {
            handleAdvertiseCommand(player, args);
        }

        return true;
    }

    private void handleTimeCommand(Player player) {
        int cooldown = ToastedAD.playerTime.getOrDefault(player.getName(), 0);
        if (cooldown > 0) {
            player.sendMessage(ChatUtil.c(ToastedAD.instance.getConfig().getString("Messages.check_cooldown", "&cYou need to wait {cooldown} seconds."))
                    .replace("{cooldown}", String.valueOf(cooldown)));
        } else {
            player.sendMessage(ChatUtil.c("&aYou can now advertise."));
        }
    }

    private void handleAdvertiseCommand(Player player, String[] args) {
        if (ToastedAD.playerTime.containsKey(player.getName())) {
            player.sendMessage(ChatUtil.c(ToastedAD.instance.getConfig().getString("Messages.cooldown", "&cYou need to wait {cooldown} seconds."))
                    .replace("{cooldown}", ToastedAD.playerTime.get(player.getName()).toString()));
            return;
        }

        AdvertiseInfo adInfo = getAdvertiseInfo(player);
        if (adInfo.cooldown == DEFAULT_COOLDOWN) {
            player.sendMessage(ChatUtil.c("&cYou don't have any advertising permissions."));
            return;
        }

        ToastedAD.playerTime.put(player.getName(), adInfo.cooldown);
        playAdvertiseSound(player, adInfo);
        String message = buildMessage(args, adInfo.useColor, adInfo.isFormat);

        PlayerAdvertiseEvent event = new PlayerAdvertiseEvent(player, message);
        Bukkit.getServer().getPluginManager().callEvent(event);

        broadcastAdvertiseMessage(player, message);
    }

    private void playAdvertiseSound(Player player, AdvertiseInfo adInfo) {
        if (adInfo.sound != null) {
            player.playSound(player.getLocation(), adInfo.sound, adInfo.volume, adInfo.pitch);
        }
    }

    private String buildMessage(String[] args, boolean useColor, boolean isFormat) {
        String message = String.join(" ", args);
        if (useColor && !isFormat) {
            message = message.replaceAll("&[klmnor]", "");
        }
        return useColor ? ChatUtil.c(message) : message;
    }

    private void broadcastAdvertiseMessage(Player player, String message) {
        List<String> worldsAllowed = ToastedAD.instance.getConfig().getStringList("Messages.worlds_allowed");
        String playerWorld = player.getWorld().getName();

        if (!worldsAllowed.contains(playerWorld)) {
            player.sendMessage(ChatUtil.c("&cYou can't advertise in this world."));
            return;
        }

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (worldsAllowed.contains(p.getWorld().getName())) {
                for (String key : ToastedAD.instance.getConfig().getStringList("Messages.advertise_message")) {
                    String formattedMessage = key.replace("{player}", player.getDisplayName())
                            .replace("{message}", message);
                    p.sendMessage(ChatUtil.c(formattedMessage));
                }
            }
        }
    }

    private AdvertiseInfo getAdvertiseInfo(Player player) {
        AdvertiseInfo adInfo = new AdvertiseInfo();
        ConfigurationSection permSection = ToastedAD.instance.getConfig().getConfigurationSection("Permissions");
        if (permSection == null) {
            return adInfo;
        }

        for (String perm : permSection.getKeys(false)) {
            if (!player.hasPermission("toastedad.perms." + perm)) {
                continue;
            }

            ConfigurationSection permConfig = permSection.getConfigurationSection(perm);
            if (permConfig == null) {
                continue;
            }

            int cooldown = permConfig.getInt("cooldown", DEFAULT_COOLDOWN);
            if (cooldown < adInfo.cooldown) {
                adInfo.cooldown = cooldown;
                adInfo.sound = getSound(permConfig.getString("sound.value"));
                adInfo.pitch = (float) permConfig.getDouble("sound.pitch", 1.0);
                adInfo.volume = (float) permConfig.getDouble("sound.volume", 1.0);
                adInfo.isFormat = permConfig.getBoolean("format", false);
                adInfo.useColor = permConfig.getBoolean("color", false);
            }
        }

        return adInfo;
    }

    private Sound getSound(String soundName) {
        try {
            return Sound.valueOf(soundName);
        } catch (IllegalArgumentException | NullPointerException e) {
            return null;
        }
    }

    private static class AdvertiseInfo {
        int cooldown = DEFAULT_COOLDOWN;
        Sound sound = null;
        float pitch = 1.0f;
        float volume = 1.0f;
        boolean isFormat = false;
        boolean useColor = false;
    }
}