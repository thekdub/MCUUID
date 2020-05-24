package com.thekdub.mcuuid;

import com.thekdub.mcuuid.exceptions.UUIDNotFoundException;
import com.thekdub.mcuuid.exceptions.UserNotFoundException;
import com.thekdub.mcuuid.utilities.DataStore;
import com.thekdub.mcuuid.utilities.Logger;
import com.thekdub.mcuuid.utilities.Parser;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class MCUUID extends JavaPlugin {
  public static MCUUID instance;
  public long updateFrequency = 0L;

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
    Logger.save();
    Logger.write("MCUUID Unloaded Successfully.");
  }

  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    if (cmd.getLabel().equalsIgnoreCase("mcuuid")) {
      if (args.length == 0) { //Display How To Use
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8[&2MC&6UUID&8] &7"
              + "Use &b/MCUUID (UUID) &7to find a user's name from their UUID"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "    &7"
              + "Use &b/MCUUID (name) &7to find a user's UUID from their name"));
      }
      else { //Process Arguments
        String arg = args[0];
        boolean forceUpdate = false;
        if (arg.equalsIgnoreCase(".reload")) {
          Bukkit.getPluginManager().disablePlugin(this);
          Bukkit.getPluginManager().enablePlugin(this);
          return true;
        }
        if (arg.equalsIgnoreCase(".update") && args.length >= 2) {
          forceUpdate = true;
          arg = args[1];
        }
        if (arg.length() <= 16) { //Username
          if (Bukkit.getPlayer(arg) != null)
            arg = Bukkit.getPlayer(arg).getName();
          try {
            if (forceUpdate) {
              UUIDAPI.updateName(arg);
            }
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8[&2MC&6UUID&8] &7"
                  + "The UUID of the user '&b" + arg + "&7' is '&b" + UUIDAPI.getUUID(arg) + "&7'"));
          } catch (IOException e) {
            e.printStackTrace();
          } catch (UserNotFoundException e) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8[&2MC&6UUID&8] &c"
                  + e.getMessage()));
          }
        }
        else { //UUID
          try {
            if (forceUpdate) {
              UUIDAPI.updateUUID(arg);
            }
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8[&2MC&6UUID&8] &7"
                  + "The name of the UUID '&b" + arg + "&7' is '&b" + UUIDAPI.getName(arg) + "&7'"));
          } catch (IOException e) {
            e.printStackTrace();
          } catch (UUIDNotFoundException e) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8[&2MC&6UUID&8] &c"
                  + e.getMessage()));
          }
        }
      }
      return true;
    }
    return false;
  }
}
