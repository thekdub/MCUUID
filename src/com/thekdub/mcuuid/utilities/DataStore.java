package com.thekdub.mcuuid.utilities;

import com.thekdub.mcuuid.MCUUID;
import com.thekdub.mcuuid.exceptions.UUIDNotFoundException;
import com.thekdub.mcuuid.exceptions.UserNotFoundException;
import com.thekdub.mcuuid.objects.Name;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;

public class DataStore {

  private static String file;
  private static YamlConfiguration yml;

  private static void init() {
    if (file == null) {
      file = MCUUID.instance.getDataFolder() + "uuidCache.yml";
    }
    if (yml == null)
      yml = YamlConfiguration.loadConfiguration(new File(file));
  }

  private static void save() {
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
    return yml.contains("uuid." + uuid);
  }

  public static boolean valid(String uuid, String name) {
    return false;
  }

  public static LinkedHashSet<Name> getNames(String uuid) throws IOException, UUIDNotFoundException {
    if (uuidCached(uuid)) {
      LinkedHashSet<Name> names = new LinkedHashSet<>();
      ConfigurationSection section = yml.getConfigurationSection("uuid." + uuid);
      for (String name : section.getKeys(false)) {
        names.add(new Name(name, section.getLong(name)));
      }
    }
    else {

    }
    return Parser.parseNameRequest(NetRequest.fetchName(uuid));
  }

  public static String getUUID(String name, long ms) throws IOException, UserNotFoundException {
    if (nameCached(name)) {

    }
    else {

    }
    return Parser.parseUUIDRequest(NetRequest.fetchUUID(name, ms));
  }

  /* Data Structure
  name:
    $user.name:
      uuid: $uuid
      set: $set
  uuid:
    $user.uuid:
      $name: $set

   */

}
