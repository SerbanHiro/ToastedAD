package me.serbob.toastedad.Commands;

import me.serbob.toastedad.ToastedAD;
import me.serbob.toastedad.Utils.ToastedUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.Set;

import static me.serbob.toastedad.ToastedAD.playerTime;

public class AD implements CommandExecutor {
    public Sound sound;
    public Float pitch=0.0F,volume=0.0F;
    public boolean isFormat =false,useColor=false;
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED+"You are not a player!");
            return false;
        }
        if(!sender.hasPermission("toastedad.use")) {
            sender.sendMessage(ToastedUtil.c(ToastedAD.instance.getConfig().getString("Messages.no_perms")));
            return false;
        }
        if(args.length<1) {
            sender.sendMessage(ToastedUtil.c(ToastedAD.instance.getConfig().getString("Messages.usage")));
            return false;
        }
        Player player = (Player) sender;
        if(args[0].equalsIgnoreCase("<time>")) {
            if(playerTime.containsKey(player)) {
                sender.sendMessage(ToastedUtil.c(
                                ToastedAD.instance.getConfig().getString("Messages.check_cooldown"))
                        .replace("{cooldown}",playerTime.get(player)+""));
            } else {
                sender.sendMessage(ToastedUtil.c("You can now advertise."));
            }
            return true;
        }
        if(playerTime.containsKey(player)) {
            sender.sendMessage(ToastedUtil.c(
                    ToastedAD.instance.getConfig().getString("Messages.cooldown"))
                    .replace("{cooldown}",playerTime.get(player)+""));
            return false;
        }
        playerTime.putIfAbsent(player,shortestTime(player));
        try {
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.playSound(p.getLocation(), sound, volume, pitch);
            }
        } catch (Exception ignored){};
        String message="";
        for(int i=0;i<args.length;++i)
            message+=(args[i]+" ");
        if(useColor) {
            if(!isFormat) {
                message = message.replace("&k","")
                        .replace("&l","")
                        .replace("&m","")
                        .replace("&n","")
                        .replace("&o","")
                        .replace("&r","");
            }
            message = ToastedUtil.c(message);
        }
        String prefix=ToastedUtil.c(ToastedAD.instance.getConfig().getString(
                "Messages.advertise_message"))
                .replace("{player}",(player.getDisplayName()));
        Bukkit.broadcastMessage(prefix.replace(
                "{message}",message
        ));
        return true;
    }
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
