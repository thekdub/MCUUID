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

  private static File file;
  private static YamlConfiguration yml;

  /* File Formatting:
  uuid:
    $uuid:
      name: $name
      cached: $cached
      history:
        $name: $set
        $name2: $set2
        ...
  name:
    $name:
      uuid: $uuid
      cached: $cached

     Class Structure:
     init() -- Initializes the cache file if not initialized.
     save() -- Saves the cache file.
     scrub() -- Scans through cache and removes expired entries.
     boolean nameCached(name) -- Returns true if name is cached
     boolean uuidCached(uuid) -- Returns true if uuid is cached
     String getUUID(name) -- Returns uuid
     String getName(uuid) -- Returns name
     Set<Name> getNameHistory(uuid) -- Returns a set of Name objects
     updateUUID(uuid) -- Update's the uuid's cached information
     updateName(name) -- Retrieves the user's UUID then calls updateUUID(uuid)
   */


  private static void init() {
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

  public static void scrub() {
    init();
    for (String uuid : yml.getConfigurationSection("uuid").getKeys(false)) {
      if (!uuidCached(uuid)) {
        yml.set("uuid." + uuid, null);
      }
    }
    for (String name : yml.getConfigurationSection("name").getKeys(false)) {
      if (!nameCached(name)) {
        yml.set("name." + name, null);
      }
    }
    save();
  }

  private static boolean nameCached(String name) {
    name = name.toLowerCase();
    init();
    return yml.contains("name." + name) && yml.getLong("name." + name + ".cached") >=
          System.currentTimeMillis() - MCUUID.instance.updateFrequency;
  }

  private static boolean uuidCached(String uuid) {
    init();
    return yml.contains("uuid." + uuid) && yml.getLong("uuid." + uuid + ".cached") >=
          System.currentTimeMillis() - MCUUID.instance.updateFrequency;
  }

  public static String getName(String uuid) throws IOException, UUIDNotFoundException {
    if (uuidCached(uuid)) {
      return yml.getString("uuid." + uuid + ".name");
    }
    else {
      updateUUID(uuid);
      return yml.getString("uuid." + uuid + ".name");
    }
  }

  public static String getUUID(String name) throws IOException, UserNotFoundException {
    name = name.toLowerCase();
    if (nameCached(name)) {
      return yml.getString("name." + name + ".uuid");
    }
    else {
      updateName(name);
      return yml.getString("name." + name + ".uuid");
    }
  }

  public static LinkedHashSet<Name> getNameHistory(String uuid) throws IOException, UUIDNotFoundException {
    if (uuidCached(uuid)) { // TODO: Add sorting of name history
      ConfigurationSection history = yml.getConfigurationSection("uuid." + uuid + ".history");
      LinkedHashSet<Name> names = new LinkedHashSet<>();
      for (String name : history.getKeys(false)) {
        names.add(new Name(name, history.getLong(name)));
      }
      return names;
    }
    else {
      updateUUID(uuid);
      ConfigurationSection history = yml.getConfigurationSection("uuid." + uuid + ".history");
      LinkedHashSet<Name> names = new LinkedHashSet<>();
      for (String name : history.getKeys(false)) {
        names.add(new Name(name, history.getLong(name)));
      }
      return names;
    }
  }

  public static void updateUUID(String uuid) throws IOException, UUIDNotFoundException {
    init();
    String name = "";
    long changedToAt = -1;
    LinkedHashSet<Name> names = Parser.parseNameRequest(NetRequest.fetchNames(uuid));
    for (Name n : names) {
      if (n.changedToAt > changedToAt || changedToAt == -1) {
        name = n.name.toLowerCase();
        changedToAt = n.changedToAt;
      }
    }
    if (uuid.length() != 32) {
      Logger.write("Failed to update UUID '" + uuid + "'. Invalid UUID");
      throw new UUIDNotFoundException(uuid);
    }
    if (name.length() > 16 || name.length() == 0) {
      Logger.write("Failed to update UUID '" + uuid + "'. Retrieved invalid name '" + name + "'");
      throw new UUIDNotFoundException(uuid);
    }
    Logger.write("Updated UUID '" + uuid + "' with name '" + name + "'");
    yml.set("uuid." + uuid + ".name", name);
    yml.set("uuid." + uuid + ".cached", System.currentTimeMillis());
    yml.set("uuid." + uuid + ".history", null);
    for (Name n : names) {
      yml.set("uuid." + uuid + ".history." + n.name, n.changedToAt);
    }
    yml.set("name." + name + ".uuid", uuid);
    yml.set("name." + name + ".cached", System.currentTimeMillis());
    save();
  }

  public static void updateName(String name) throws IOException, UserNotFoundException {
    name = name.toLowerCase();
    String uuid = Parser.parseUUIDRequest(NetRequest.fetchUUID(name, System.currentTimeMillis()));
    if (name.length() > 16 || name.length() == 0) {
      Logger.write("Failed to update name '" + name + "'. Invalid name");
      throw new UserNotFoundException(name);
    }
    if (uuid.length() != 32) {
      Logger.write("Failed to update name '" + name + "'. Retrieved invalid UUID '" + uuid + "'");
      throw new UserNotFoundException(name);
    }
    try {
      updateUUID(uuid);
    } catch (UUIDNotFoundException e) {
      throw new UserNotFoundException(name);
    }
  }
}
