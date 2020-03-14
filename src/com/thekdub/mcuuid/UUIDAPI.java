package com.thekdub.mcuuid;

import com.thekdub.mcuuid.exceptions.UUIDNotFoundException;
import com.thekdub.mcuuid.exceptions.UserNotFoundException;
import com.thekdub.mcuuid.utilities.DataStore;
import org.bukkit.entity.Player;

import java.io.IOException;

public class UUIDAPI {

//  public static String getName(String uuid) throws IOException, UUIDNotFoundException {
//    for (Name n : getNames(uuid)) {
//      if (n.changedToAt == 0)
//        return n.name;
//    }
//    return "";
//  }

  public static String getNames(String uuid) throws IOException, UUIDNotFoundException {
    return DataStore.getName(uuid);
  }

  public static String getUUID(Player player) throws IOException, UserNotFoundException {
    return getUUID(player.getName());
  }

  public static String getUUID(String name) throws IOException, UserNotFoundException {
    return DataStore.getUUID(name);
  }

}
