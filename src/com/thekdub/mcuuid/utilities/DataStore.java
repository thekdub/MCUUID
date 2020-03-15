package com.thekdub.mcuuid.utilities;

import com.thekdub.mcuuid.MCUUID;
import com.thekdub.mcuuid.exceptions.UUIDNotFoundException;
import com.thekdub.mcuuid.exceptions.UserNotFoundException;
import com.thekdub.mcuuid.objects.Name;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class DataStore {

  private static File file;
  private static YamlConfiguration yml;

  public static void init() {
    if (file == null) {
      file = new File(MCUUID.instance.getDataFolder() + File.separator + "uuidCache.yml");
    }
    if (yml == null)
      yml = YamlConfiguration.loadConfiguration(file);
  }

  public static void save() {
    init();
    try {
      yml.save(file);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static boolean nameCached(String name) {
    init();
    return yml.contains("name." + name) && yml.getLong("name." + name + ".cached") >=
          System.currentTimeMillis() - MCUUID.instance.updateFrequency;
  }

  public static boolean uuidCached(String uuid) {
    init();
    return yml.contains("uuid." + uuid) && yml.getLong("uuid." + uuid + ".cached") >=
          System.currentTimeMillis() - MCUUID.instance.updateFrequency;
  }

  private static boolean validateUUID(String uuid) {
    if (!uuidCached(uuid))
      return false;
    if (!nameCached(yml.getString("uuid." + uuid + ".name")))
      return false;
    return uuid.equalsIgnoreCase(yml.getString("name." + yml.getString("uuid." + uuid + ".name") + ".uuid"));
  }

  private static boolean validateName(String name) {
    if (!uuidCached(name))
      return false;
    if (!nameCached(yml.getString("name." + name + ".uuid")))
      return false;
    return name.equalsIgnoreCase(yml.getString("uuid." + yml.getString("name." + name + ".uuid") + ".name"));
  }

  public static String getName(String uuid) throws IOException, UUIDNotFoundException {
    if (uuidCached(uuid)) {
      return yml.getString("uuid." + uuid + ".name");
    }
    else {
      updateUUID(uuid);
      return getName(uuid);
    }
  }

  public static String getUUID(String name) throws IOException, UserNotFoundException {
    if (nameCached(name)) {
      return yml.getString("name." + name + ".uuid");
    }
    else {
      updateName(name);
      return getUUID(name);
    }
  }

  private static void updateUUID(String uuid) throws IOException, UUIDNotFoundException {
    String name = "";
    long changedToAt = -1;
    for (Name n : Parser.parseNameRequest(NetRequest.fetchName(uuid))) {
      if (n.changedToAt > changedToAt || changedToAt == -1) {
        name = n.name;
        changedToAt = n.changedToAt;
      }
    }
    Logger.write("Updated UUID '" + uuid + "' with name '" + name + "'");
    yml.set("uuid." + uuid + ".name", name);
    yml.set("uuid." + uuid + ".cached", System.currentTimeMillis());
    yml.set("name." + name + ".uuid", uuid);
    yml.set("name." + name + ".cached", System.currentTimeMillis());
    save();
  }

  private static void updateName(String name) throws IOException, UserNotFoundException {
    String uuid = Parser.parseUUIDRequest(NetRequest.fetchUUID(name, System.currentTimeMillis()));
    Logger.write("Updated name '" + name + "' with UUID '" + uuid + "'");
    yml.set("uuid." + uuid + ".name", name);
    yml.set("uuid." + uuid + ".cached", System.currentTimeMillis());
    yml.set("name." + name + ".uuid", uuid);
    yml.set("name." + name + ".cached", System.currentTimeMillis());
    save();
  }

}
