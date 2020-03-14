package com.thekdub.mcuuid;

import com.thekdub.mcuuid.exceptions.UUIDNotFoundException;
import com.thekdub.mcuuid.exceptions.UserNotFoundException;
import com.thekdub.mcuuid.objects.Name;
import com.thekdub.mcuuid.utilities.DataStore;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.LinkedHashSet;

public class UUIDAPI {

  // TODO: Add UUID caching

  public static String getName(String uuid) throws IOException, UUIDNotFoundException {
    for (Name n : getNames(uuid)) {
      if (n.changedToAt == 0)
        return n.name;
    }
    return "";
  }

  public static LinkedHashSet<Name> getNames(String uuid) throws IOException, UUIDNotFoundException {
    return DataStore.getNames(uuid);
  }

  public static String getUUID(Player player) throws IOException, UserNotFoundException {
    return getUUID(player.getName(), System.currentTimeMillis());
  }

  public static String getUUID(Player player, long ms) throws IOException, UserNotFoundException {
    return getUUID(player.getName(), ms);
  }

  public static String getUUID(String name) throws IOException, UserNotFoundException {
    return getUUID(name, System.currentTimeMillis());
  }

  public static String getUUID(String name, long ms) throws IOException, UserNotFoundException {
    return DataStore.getUUID(name, ms);
  }


}
