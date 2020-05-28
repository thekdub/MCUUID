package com.thekdub.mcuuid;

import com.thekdub.mcuuid.exceptions.UUIDNotFoundException;
import com.thekdub.mcuuid.exceptions.UserNotFoundException;
import com.thekdub.mcuuid.objects.Name;
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
import java.util.LinkedHashSet;
import java.util.logging.Level;

public class MCUUID extends JavaPlugin {
  public static MCUUID instance;
  public long updateFrequency = 0L;
  public long failureCooldown = 0L;

  public void onEnable() {
    instance = this;
    String dir = getDataFolder() + File.separator;
    if (!new File(dir + "config.yml").exists()) {
      saveDefaultConfig();
    }
    updateFrequency = Parser.parseTime(getConfig().getString("UUID_Refresh_Frequency", "7d"));
    failureCooldown = Parser.parseTime(getConfig().getString("API_Failure_Cooldown", "1m"));
    DataStore.scrub();
    Logger.write("MCUUID Loaded Successfully.");
  }

  public void onDisable() {
    DataStore.save();
    Logger.save();
    Logger.write("MCUUID Unloaded Successfully.");
  }

  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    StringBuilder sArgs = new StringBuilder();
    for (String arg:args) {
      if (sArgs.length() > 0) {
        sArgs.append(" ");
      }
      sArgs.append(arg);
    }
    getLogger().log(Level.INFO, sender.getName() + ": /" + cmd.getLabel() + " " + sArgs.toString());
    if (cmd.getLabel().equalsIgnoreCase("mcuuid")) {
      if (args.length == 0) { //Display How To Use
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8[&2MC&6UUID&8] &7"
              + "Use &b/MCUUID (UUID) &7to find a user's name from a UUID"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "    &7"
              + "Use &b/MCUUID (name) &7to find a user's UUID from a name"));
      }
      else { //Process Arguments
        String arg = args[0];
        boolean forceUpdate = false;
        boolean history = false;
        for (String s:args) {
          if (s.equalsIgnoreCase(".reload")) {
            Bukkit.getPluginManager().disablePlugin(this);
            Bukkit.getPluginManager().enablePlugin(this);
            return true;
          }
          else if (s.equalsIgnoreCase(".update")) {
            forceUpdate = true;
          }
          else if (s.equalsIgnoreCase(".history")) {
            history = true;
          }
          else {
            arg = s;
          }
        }
        if (arg.length() <= 16) { //Username
          if (Bukkit.getPlayer(arg) != null)
            arg = Bukkit.getPlayer(arg).getName();
          try {
            if (forceUpdate) {
              UUIDAPI.updateName(arg);
            }
            if (history) {
              LinkedHashSet<Name> nameHistory = UUIDAPI.getNameHistory(UUIDAPI.getUUID(arg));
              if (nameHistory == null) {
                throw new UserNotFoundException(arg);
              }
              sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    "&8[&2MC&6UUID&8] &7The name history of the user '&b" + arg + "&7' is:"));
              for (Name name : nameHistory) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "  &b> &7"
                      + name.name + " &b@ &7" + (name.changedToAt > 0 ?
                      Parser.parseMillis(name.changedToAt, "yyyy-MM-dd HH:mm") : "creation")));
              }
            }
            else {
              sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    "&8[&2MC&6UUID&8] &7The UUID of the user '&b" + arg + "&7' is '&b"
                          + UUIDAPI.getUUID(arg) + "&7'"));
            }
          } catch (IOException e) {
            e.printStackTrace();
          } catch (UserNotFoundException | UUIDNotFoundException e) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                  "&8[&2MC&6UUID&8] &c" + e.getMessage()));
          }
        }
        else { //UUID
          try {
            if (forceUpdate) {
              UUIDAPI.updateUUID(arg);
            }
            if (history) {
              LinkedHashSet<Name> nameHistory = UUIDAPI.getNameHistory(arg);
              if (nameHistory == null) {
                throw new UUIDNotFoundException(arg);
              }
              sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    "&8[&2MC&6UUID&8] &7The name history of the user '&b" + UUIDAPI.getName(arg)
                          + "&7' is:"));
              for (Name name : nameHistory) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "  &b> &7"
                      + name.name + " &b@ &7" + (name.changedToAt > 0 ?
                      Parser.parseMillis(name.changedToAt, "yyyy-MM-dd HH:mm") : "creation")));
              }
            }
            else {
              sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    "&8[&2MC&6UUID&8] &7The name of the UUID '&b" + arg + "&7' is '&b"
                          + UUIDAPI.getName(arg) + "&7'"));
            }
          } catch (IOException e) {
            e.printStackTrace();
          } catch (UUIDNotFoundException e) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                  "&8[&2MC&6UUID&8] &c" + e.getMessage()));
          }
        }
      }
      return true;
    }
    return false;
  }
}
