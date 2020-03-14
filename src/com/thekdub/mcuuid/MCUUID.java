package com.thekdub.mcuuid;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
      System.out.println(UUIDAPI.getNames(uuid));
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
    updateFrequency = parseTime(getConfig().getString("UUID_Refresh_Frequency"));
  }

  public void onDisable() {

  }

  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    return false;
  }

  private long parseTime(String str) {
    Pattern timePattern = Pattern.compile("(?:([0-9]+)\\s*y[a-z]*[,\\s]*)?(?:([0-9]+)\\s*mo[a-z]*[,\\s]*)?(?:([0-9]+)\\s*w[a-z]*[,\\s]*)?(?:([0-9]+)\\s*d[a-z]*[,\\s]*)?(?:([0-9]+)\\s*h[a-z]*[,\\s]*)?(?:([0-9]+)\\s*m[a-z]*[,\\s]*)?(?:([0-9]+)\\s*(?:s[a-z]*)?)?", 2);
    Matcher m = timePattern.matcher(str);
    long years = 0L;
    long msyear = 31536000000L;
    long months = 0L;
    long msmonth = 2628000000L;
    long weeks = 0L;
    long msweek = 604800000L;
    long days = 0L;
    long msday = 86400000L;
    long hours = 0L;
    long mshour = 3600000L;
    long minutes = 0L;
    long msminute = 60000L;
    long seconds = 0L;
    long mssecond = 1000L;
    boolean found = false;
    while (m.find()) {
      if ((m.group() != null) && (!m.group().isEmpty())) {
        for (int i = 0; i < m.groupCount(); i++) {
          if ((m.group(i) != null) && (!m.group(i).isEmpty())) {
            found = true;
            break;
          }
        }
        if (found) {
          if ((m.group(1) != null) && (!m.group(1).isEmpty())) {
            years = Integer.parseInt(m.group(1));
          }
          if ((m.group(2) != null) && (!m.group(2).isEmpty())) {
            months = Integer.parseInt(m.group(2));
          }
          if ((m.group(3) != null) && (!m.group(3).isEmpty())) {
            weeks = Integer.parseInt(m.group(3));
          }
          if ((m.group(4) != null) && (!m.group(4).isEmpty())) {
            days = Integer.parseInt(m.group(4));
          }
          if ((m.group(5) != null) && (!m.group(5).isEmpty())) {
            hours = Integer.parseInt(m.group(5));
          }
          if ((m.group(6) != null) && (!m.group(6).isEmpty())) {
            minutes = Integer.parseInt(m.group(6));
          }
          if ((m.group(7) != null) && (!m.group(7).isEmpty())) {
            seconds = Integer.parseInt(m.group(7));
          }
        }
      }
    }
    long val = years * msyear + months * msmonth + weeks * msweek + days * msday + hours * mshour + minutes * msminute + seconds * mssecond;
    return years + months + weeks + days + hours + minutes + seconds > 0L ? val : val < 0L ? 9223372036854775806L : -1L;
  }

}
