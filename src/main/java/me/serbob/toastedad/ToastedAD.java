package me.serbob.toastedad;

import jdk.jfr.internal.Logger;
import me.serbob.toastedad.Commands.AD;
import me.serbob.toastedad.TabCompleter.ADTC;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class ToastedAD extends JavaPlugin {
    public static Map<Player,Integer> playerTime = new HashMap<>();
    public static ToastedAD instance;
    @Override
    public void onEnable() {
        instance=this;
        saveDefaultConfig();
        getCommand("ad").setExecutor(new AD());
        getCommand("ad").setTabCompleter(new ADTC());
        registerPerms();
        startScheduler();
    }
    @Override
    public void onDisable() {}
    public void registerPerms() {
        ConfigurationSection permSection = ToastedAD.instance.getConfig().getConfigurationSection("Permissions");
        Set<String> perms = permSection.getKeys(false);
        for (String perm : perms) {
            String permissionName = "toastedad.perms" + perm.toLowerCase();
            if(ToastedAD.instance.getServer().getPluginManager().getPermission(permissionName)==null) {
                Permission permission = new Permission(permissionName);
                ToastedAD.instance.getServer().getPluginManager().addPermission(permission);
            }
        }
    }
    public void startScheduler() {
        getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
            for(Map.Entry<Player, Integer> entry:playerTime.entrySet()) {
                int time = entry.getValue();
                if(time==0) {
                    playerTime.remove(entry.getKey());
                }
                playerTime.replace(entry.getKey(),--time);
            }
        }, 0L, 20L);
    }
}
