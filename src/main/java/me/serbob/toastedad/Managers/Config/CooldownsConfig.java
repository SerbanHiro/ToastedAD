package me.serbob.toastedad.Managers.Config;

import me.serbob.toastedad.ToastedAD;
import me.serbob.toastedad.Utils.Logger;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class CooldownsConfig {
    private static File cooldownsFile;
    private static YamlConfiguration cooldownsConfig;

    public static void loadCooldownsFile() {
        cooldownsFile = new File(ToastedAD.instance.getDataFolder(), "cooldowns.yml");
        cooldownsConfig = YamlConfiguration.loadConfiguration(cooldownsFile);
    }

    public static void saveDefaultCooldownsConfig() {
        InputStream inputStream = ToastedAD.instance.getResource("cooldowns.yml");
        if (inputStream == null) {
            Logger.log(Logger.LogLevel.WARNING,"Default cooldowns.yml not found in JAR.");
            return;
        }
        File localCooldownsFile = new File(ToastedAD.instance.getDataFolder(), "cooldowns.yml");
        if (!localCooldownsFile.exists()) {
            try {
                localCooldownsFile.createNewFile();
                Files.copy(inputStream, localCooldownsFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                Logger.log(Logger.LogLevel.INFO,"Default cooldowns.yml saved to plugin data folder.");
            } catch (IOException e) {
                Logger.log(Logger.LogLevel.WARNING,"Error saving default cooldowns.yml to plugin data folder: " + e.getMessage());
            }
        }
    }

    public static void addUserToCooldownsStorage(String playerName) {
        int playerCooldown = ToastedAD.playerTime.get(playerName);
        String cooldownEntry = playerName + ":" + playerCooldown;

        List<String> cooldownsList = cooldownsConfig.getStringList("cooldowns_storage");
        cooldownsList.add(cooldownEntry);
        cooldownsConfig.set("cooldowns_storage", cooldownsList);

        try {
            cooldownsConfig.save(cooldownsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadCooldownedUsers() {
        List<String> cooldownsList = cooldownsConfig.getStringList("cooldowns_storage");
        for (String entry : cooldownsList) {
            String[] parts = entry.split(":");
            String playerName = parts[0];
            int playerCooldown = Integer.parseInt(parts[1]);

            ToastedAD.playerTime.putIfAbsent(playerName,playerCooldown);
        }

        cooldownsList.clear();
        cooldownsConfig.set("cooldowns_storage", cooldownsList);

        try {
            cooldownsConfig.save(cooldownsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
