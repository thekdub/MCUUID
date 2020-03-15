package com.thekdub.mcuuid;

import com.thekdub.mcuuid.utilities.DataStore;
import com.thekdub.mcuuid.utilities.Logger;
import com.thekdub.mcuuid.utilities.Parser;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class MCUUID extends JavaPlugin {
  public static MCUUID instance;
  public long updateFrequency = 0L;

  public static void main(String[] args) {
    String uuid = "19d936397bb74a26b98fdae1b65e394d";
    String name = "fadsfaswfe";
    try {
      System.out.println(UUIDAPI.getUUID(name));
    } catch (Exception e) {
      e.printStackTrace();
    }
    try {
      System.out.println(UUIDAPI.getName(uuid));
    } catch (Exception e) {
      e.printStackTrace();
    }
    try {
      System.out.println(UUIDAPI.getName(uuid));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void onEnable() {
    instance = this;
    String dir = getDataFolder() + File.separator;
    if (!new File(dir + "config.yml").exists()) {
      saveDefaultConfig();
    }
    updateFrequency = Parser.parseTime(getConfig().getString("UUID_Refresh_Frequency"));
    Logger.write("MCUUID Loaded Successfully.");
  }

  public void onDisable() {
    DataStore.save();
    Logger.write("MCUUID Unloaded Successfully.");
  }

  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    return false;
  }
}
