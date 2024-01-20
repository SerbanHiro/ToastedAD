package me.serbob.toastedad.Commands;

import me.serbob.toastedad.ToastedAD;
import me.serbob.toastedad.Events.PlayerAdvertiseEvent;
import me.serbob.toastedad.Utils.ToastedUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import static me.serbob.toastedad.ToastedAD.playerTime;

public class ADCommand implements CommandExecutor {
    public Sound sound;
    public Float pitch=0.0F,volume=0.0F;
    public boolean isFormat =false,useColor=false;
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "You are not a player!");
            return false;
        }

        Player player = (Player) sender;

        if (!checkPermission(player)) {
            return false;
        }

        if (!checkArgsLength(player, args)) {
            return false;
        }

        if (args[0].equalsIgnoreCase("<time>")) {
            handleTimeCommand(player);
        } else {
            handleAdvertiseCommand(player, args);
        }

        return true;
    }

    private boolean checkPermission(Player player) {
        if (!player.hasPermission("toastedad.use")) {
            player.sendMessage(ToastedUtil.c(ToastedAD.instance.getConfig().getString("Messages.no_perms")));
            return false;
        }
        return true;
    }

    private boolean checkArgsLength(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage(ToastedUtil.c(ToastedAD.instance.getConfig().getString("Messages.usage")));
            return false;
        }
        return true;
    }

    private void handleTimeCommand(Player player) {
        if (playerTime.containsKey(player.getName())) {
            player.sendMessage(ToastedUtil.c(
                            ToastedAD.instance.getConfig().getString("Messages.check_cooldown"))
                    .replace("{cooldown}", playerTime.get(player.getName()) + ""));
        } else {
            player.sendMessage(ToastedUtil.c("You can now advertise."));
        }
    }

    private void handleAdvertiseCommand(Player player, String[] args) {
        if (playerTime.containsKey(player.getName())) {
            player.sendMessage(ToastedUtil.c(
                            ToastedAD.instance.getConfig().getString("Messages.cooldown"))
                    .replace("{cooldown}", playerTime.get(player.getName()) + ""));
            return;
        }

        playerTime.putIfAbsent(player.getName(), shortestTime(player));
        playAdvertiseSound();
        String message = buildMessage(args);

        // Create and call the PlayerAdvertiseEvent
        PlayerAdvertiseEvent event = new PlayerAdvertiseEvent(player, message);
        Bukkit.getServer().getPluginManager().callEvent(event);

        // Broadcasting the advertisement
        broadcastAdvertiseMessage(player, message);
    }

    private void playAdvertiseSound() {
        try {
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.playSound(p.getLocation(), sound, volume, pitch);
            }
        } catch (Exception ignored) {
        }
    }

    private String buildMessage(String[] args) {
        String message = String.join(" ", args);
        if (useColor && !isFormat) {
            message = message.replaceAll("&[klmnor]", "");
        }
        return useColor ? ToastedUtil.c(message) : message;
    }

    private void broadcastAdvertiseMessage(Player player, String message) {
        List<String> worldsAllowed = ToastedAD.instance.getConfig().getStringList("Messages.worlds_allowed");
        String playerWorld = player.getWorld().getName();

        if (!worldsAllowed.contains(playerWorld)) {
            return; // Player is not in an allowed world, do not broadcast the message
        }

        for (Player p : Bukkit.getOnlinePlayers()) {
            String pWorld = p.getWorld().getName();
            if (worldsAllowed.contains(pWorld)) {
                // Player 'p' is in one of the allowed worlds, send the message to them
                for (String key : ToastedAD.instance.getConfig().getStringList("Messages.advertise_message")) {
                    String formattedMessage = key.replace("{player}", player.getDisplayName())
                            .replace("{message}", message);
                    p.sendMessage(ToastedUtil.c(formattedMessage));
                }
            }
        }
    }

    /**private void broadcastAdvertiseMessage(Player player, String message) {
        for (String key : ToastedAD.instance.getConfig().getStringList("Messages.advertise_message")) {
            String formattedMessage = key.replace("{player}", player.getDisplayName())
                    .replace("{message}", message);
            Bukkit.broadcastMessage(ToastedUtil.c(formattedMessage));
        }
    }*/

    public int shortestTime(Player player) {
        int shortTime=999999999;
        sound=null;
        ConfigurationSection permSection = ToastedAD.instance.getConfig().getConfigurationSection("Permissions");
        Set<String> perms = permSection.getKeys(false);
        for (String perm : perms) {
            if(!player.hasPermission("toastedad.perms."+perm)) {
                continue;
            }
            int actualTime = ToastedAD.instance.getConfig().getInt("" +
                    "Permissions."+perm+".cooldown");
            if(shortTime>actualTime) {
                shortTime=actualTime;
                try {
                    sound = Sound.valueOf(ToastedAD.instance.getConfig().getString("" +
                            "Permissions." + perm + ".sound.value"));
                } catch (Exception ignored){};
                pitch = Float.parseFloat(Objects.requireNonNull(ToastedAD.instance.getConfig().getString("" +
                        "Permissions." + perm + ".sound.pitch")));
                volume = Float.parseFloat(Objects.requireNonNull(ToastedAD.instance.getConfig().getString("" +
                        "Permissions." + perm + ".sound.volume")));
                isFormat = Boolean.parseBoolean(Objects.requireNonNull(ToastedAD.instance.getConfig().getString("" +
                        "Permissions." + perm + ".format")));
                useColor = Boolean.parseBoolean(Objects.requireNonNull(ToastedAD.instance.getConfig().getString("" +
                        "Permissions." + perm + ".color")));
            }
        }
        return shortTime;
    }
}
