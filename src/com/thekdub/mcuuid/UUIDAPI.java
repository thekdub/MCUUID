package com.thekdub.mcuuid;

import com.thekdub.mcuuid.exceptions.UUIDNotFoundException;
import com.thekdub.mcuuid.exceptions.UserNotFoundException;
import com.thekdub.mcuuid.objects.Name;
import com.thekdub.mcuuid.utilities.DataStore;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashSet;

public class UUIDAPI {

  public static String getName(String uuid) throws IOException, UUIDNotFoundException {
    return DataStore.getName(uuid);
  }

  public static String getUUID(Player player) throws IOException, UserNotFoundException {
    return getUUID(player.getName().toLowerCase());
  }

  public static String getUUID(String name) throws IOException, UserNotFoundException {
    name = name.toLowerCase();
    return DataStore.getUUID(name);
  }

  public static void updateName(String name) throws IOException, UserNotFoundException {
    name = name.toLowerCase();
    DataStore.updateName(name);
  }

  public static void updateUUID(String uuid) throws IOException, UUIDNotFoundException {
    DataStore.updateUUID(uuid);
  }

  public static LinkedHashSet<Name> getNameHistory(String uuid) throws IOException, UUIDNotFoundException {
    return DataStore.getNameHistory(uuid);
  }

}
