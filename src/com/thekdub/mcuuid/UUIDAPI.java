package com.thekdub.mcuuid;

import com.thekdub.mcuuid.exceptions.InvalidUUIDException;
import com.thekdub.mcuuid.exceptions.UUIDNotFoundException;
import com.thekdub.mcuuid.exceptions.UserNotFoundException;
import com.thekdub.mcuuid.objects.NameEntry;
import com.thekdub.mcuuid.objects.UUIDEntry;
import com.thekdub.mcuuid.utilities.DataStore;
import com.thekdub.mcuuid.utilities.Parser;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.UUID;

public class UUIDAPI {

  public static String getName(@Nonnull String uuid) throws IOException, UUIDNotFoundException, InvalidUUIDException {
    return getName(Parser.parseUUID(uuid));
  }

  public static String getName(@Nonnull UUID uuid) throws IOException, UUIDNotFoundException, InvalidUUIDException {
    return DataStore.getName(uuid.toString());
  }

  public static UUID getUUID(@Nonnull Player player) throws IOException, UserNotFoundException {
    return getUUID(player.getName());
  }

  public static UUID getUUID(@Nonnull String name) throws IOException, UserNotFoundException {
    name = name.toLowerCase();
    try {
      return Parser.parseUUID(DataStore.getUUID(name));
    }
    catch (InvalidUUIDException e) {
      throw new UserNotFoundException(name);
    }
  }

  public static void updateName(@Nonnull String name) throws IOException, UserNotFoundException {
    name = name.toLowerCase();
    DataStore.updateName(name);
  }

  public static void updateUUID(@Nonnull String uuid) throws IOException, UUIDNotFoundException, InvalidUUIDException {
    updateUUID(Parser.parseUUID(uuid));
  }

  public static void updateUUID(@Nonnull UUID uuid) throws IOException, UUIDNotFoundException, InvalidUUIDException {
    DataStore.updateUUID(uuid.toString());
  }

  public static LinkedHashSet<NameEntry> getNameHistory(@Nonnull String uuid) throws IOException, UUIDNotFoundException,
        InvalidUUIDException {
    return getNameHistory(Parser.parseUUID(uuid));
  }

  public static LinkedHashSet<NameEntry> getNameHistory(@Nonnull UUID uuid) throws IOException, UUIDNotFoundException,
        InvalidUUIDException {
    return DataStore.getNameHistory(uuid.toString());
  }

  public static LinkedHashSet<UUIDEntry> getUUIDHistory(@Nonnull String name) throws IOException,
        UserNotFoundException {
    return DataStore.getUUIDHistory(name);
  }

}
