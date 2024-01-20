package me.serbob.toastedad;

import me.serbob.toastedad.Commands.ADCommand;
import me.serbob.toastedad.Managers.Config.CooldownsConfig;
import me.serbob.toastedad.Metrics.Metrics;
import me.serbob.toastedad.TabCompleter.ADTabCompleter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

public final class ToastedAD extends JavaPlugin {
    public static Map<String,Integer> playerTime = new ConcurrentHashMap<>();
    public static ToastedAD instance;
    @Override
    public void onEnable() {
        instance=this;
        saveDefaultConfig();
        CooldownsConfig.saveDefaultCooldownsConfig();
        CooldownsConfig.loadCooldownsFile();

        getCommand("ad").setExecutor(new ADCommand());
        getCommand("ad").setTabCompleter(new ADTabCompleter());

        registerPerms();

        startScheduler();
        CooldownsConfig.loadCooldownedUsers();

        enableMetrics();
    }
    @Override
    public void onDisable() {
        for(String cooldownedPlayer:playerTime.keySet()) {
            CooldownsConfig.addUserToCooldownsStorage(cooldownedPlayer);
        }
    }
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
        getServer().getScheduler().scheduleAsyncRepeatingTask(this, () -> {
            for(Map.Entry<String, Integer> entry:playerTime.entrySet()) {
                int time = entry.getValue();
                if(time==0) {
                    playerTime.remove(entry.getKey());
                }
                playerTime.replace(entry.getKey(),--time);
            }
        }, 0L, 20L);
    }
    public void enableMetrics() {
        Metrics metrics = new Metrics(this,19313);
        metrics.addCustomChart(new Metrics.MultiLineChart("players_and_servers", new Callable<Map<String, Integer>>() {
            @Override
            public Map<String, Integer> call() throws Exception {
                Map<String, Integer> valueMap = new HashMap<>();
                valueMap.put("servers", 1);
                valueMap.put("players", Bukkit.getOnlinePlayers().size());
                return valueMap;
            }
        }));
        metrics.addCustomChart(new Metrics.DrilldownPie("java_version", () -> {
            Map<String, Map<String, Integer>> map = new HashMap<>();
            String javaVersion = System.getProperty("java.version");
            Map<String, Integer> entry = new HashMap<>();
            entry.put(javaVersion, 1);
            if (javaVersion.startsWith("1.7")) {
                map.put("Java 1.7", entry);
            } else if (javaVersion.startsWith("1.8")) {
                map.put("Java 1.8", entry);
            } else if (javaVersion.startsWith("1.9")) {
                map.put("Java 1.9", entry);
            } else {
                map.put("Other", entry);
            }
            return map;
        }));
    }
}
