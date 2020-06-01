package com.thekdub.mcuuid.utilities;

import com.thekdub.mcuuid.MCUUID;
import com.thekdub.mcuuid.exceptions.UUIDNotFoundException;
import com.thekdub.mcuuid.exceptions.UserNotFoundException;
import com.thekdub.mcuuid.objects.NameEntry;
import com.thekdub.mcuuid.objects.UUIDEntry;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.LinkedHashSet;

public class DataStore {

  private static Connection connection = null;

  public static void init() {
    if (connection == null) {
      try {
        connection = DriverManager.getConnection("jdbc:sqlite:" + MCUUID.instance.getDataFolder() + File.separator
              + "uuid.db");
        connection.createStatement().execute("CREATE TABLE IF NOT EXISTS data (uuid TEXT, name TEXT, " +
              "time INTEGER, PRIMARY KEY(uuid, name, time));");
        connection.createStatement().execute("CREATE TABLE IF NOT EXISTS retrieved (user TEXT, time INTEGER, " +
              "PRIMARY KEY(user));");
      }
      catch (SQLException e) {
        e.printStackTrace();
      }
    }
  }

  public static void close() {
    try {
      connection.close();
      connection = null;
    }
    catch (SQLException e) {
      e.printStackTrace();
    }
  }

  private static boolean nameCached(String name) {
    name = name.toLowerCase().replaceAll("[^a-z0-9_]", "");
    try {
      ResultSet rs = connection.prepareStatement("SELECT uuid FROM data WHERE name='" + name
            + "';").executeQuery();
      return rs.next();
    }
    catch (SQLException e) {
      e.printStackTrace();
    }
    return false;
  }

  private static boolean uuidCached(String uuid) {
    uuid = uuid.toLowerCase().replaceAll("[^a-z0-9]", "");
    try {
      ResultSet rs = connection.prepareStatement("SELECT name FROM data WHERE uuid='" + uuid
            + "';").executeQuery();
      return rs.next();
    }
    catch (SQLException e) {
      e.printStackTrace();
    }
    return false;
  }

  private static boolean needsUpdate(String user) {
    user = user.toLowerCase().replaceAll("[^a-z0-9_]", "");
    long time = 0L;
    try {
      ResultSet rs = connection.prepareStatement("SELECT time FROM retrieved WHERE user='" + user
            + "';").executeQuery();
      if (rs.next()) {
        time = rs.getLong("time");
      }
    }
    catch (SQLException e) {
      e.printStackTrace();
    }
    return time < System.currentTimeMillis() - MCUUID.instance.updateFrequency;
  }

  public static String getName(String uuid) throws IOException, UUIDNotFoundException {
    uuid = uuid.toLowerCase().replaceAll("[^a-z0-9]", "");
    if (uuidCached(uuid) && !needsUpdate(uuid)) {
      try {
        ResultSet rs = connection.prepareStatement("SELECT name FROM data WHERE uuid='" + uuid
              + "' ORDER BY time DESC;").executeQuery();
        if (rs.next()) {
          return rs.getString("name");
        }
      }
      catch (SQLException e) {
        e.printStackTrace();
      }
    }
    else {
      updateUUID(uuid);
      try {
        ResultSet rs = connection.prepareStatement("SELECT name FROM data WHERE uuid='" + uuid
              + "' ORDER BY time DESC;").executeQuery();
        if (rs.next()) {
          return rs.getString("name");
        }
      }
      catch (SQLException e) {
        e.printStackTrace();
      }
    }
    throw new UUIDNotFoundException(uuid);
  }

  public static String getUUID(String name) throws IOException, UserNotFoundException {
    name = name.toLowerCase().replaceAll("[^a-z0-9_]", "");
    if (nameCached(name) && !needsUpdate(name)) {
      try {
        ResultSet rs = connection.prepareStatement("SELECT uuid FROM data WHERE name='" + name
              + "' ORDER BY time DESC;").executeQuery();
        if (rs.next()) {
          return rs.getString("uuid");
        }
      }
      catch (SQLException e) {
        e.printStackTrace();
      }
    }
    else {
      updateName(name);
      try {
        ResultSet rs = connection.prepareStatement("SELECT uuid FROM data WHERE name='" + name
              + "' ORDER BY time DESC;").executeQuery();
        if (rs.next()) {
          return rs.getString("uuid");
        }
      }
      catch (SQLException e) {
        e.printStackTrace();
      }
    }
    throw new UserNotFoundException(name);
  }

  public static LinkedHashSet<NameEntry> getNameHistory(String uuid) throws IOException, UUIDNotFoundException {
    uuid = uuid.toLowerCase().replaceAll("[^a-z0-9]", "");
    if (uuidCached(uuid) && !needsUpdate(uuid)) {
      LinkedHashSet<NameEntry> nameEntries = new LinkedHashSet<>();
      try {
        ResultSet rs = connection.prepareStatement("SELECT name,time FROM data WHERE uuid='" + uuid
              + "' ORDER BY time DESC;").executeQuery();
        while (rs.next()) {
          nameEntries.add(new NameEntry(rs.getString("name"), rs.getLong("time")));
        }
      }
      catch (SQLException e) {
        e.printStackTrace();
      }
      return nameEntries;
    }
    else {
      updateUUID(uuid);
      LinkedHashSet<NameEntry> nameEntries = new LinkedHashSet<>();
      try {
        ResultSet rs = connection.prepareStatement("SELECT name,time FROM data WHERE uuid='" + uuid
              + "' ORDER BY time DESC;").executeQuery();
        while (rs.next()) {
          nameEntries.add(new NameEntry(rs.getString("name"), rs.getLong("time")));
        }
      }
      catch (SQLException e) {
        e.printStackTrace();
      }
      return nameEntries;
    }
  }

  public static LinkedHashSet<UUIDEntry> getUUIDHistory(String name) throws IOException, UserNotFoundException {
    name = name.toLowerCase().replaceAll("[^a-z0-9_]", "");
    if (nameCached(name) && !needsUpdate(name)) {
      LinkedHashSet<UUIDEntry> uuidEntries = new LinkedHashSet<>();
      try {
        ResultSet rs = connection.prepareStatement("SELECT uuid,time FROM data WHERE name='" + name
              + "' ORDER BY time DESC;").executeQuery();
        while (rs.next()) {
          uuidEntries.add(new UUIDEntry(rs.getString("uuid"), rs.getLong("time")));
        }
      }
      catch (SQLException e) {
        e.printStackTrace();
      }
      return uuidEntries;
    }
    else {
      updateName(name);
      LinkedHashSet<UUIDEntry> uuidEntries = new LinkedHashSet<>();
      try {
        ResultSet rs = connection.prepareStatement("SELECT uuid,time FROM data WHERE name='" + name
              + "' ORDER BY time DESC;").executeQuery();
        while (rs.next()) {
          uuidEntries.add(new UUIDEntry(rs.getString("uuid"), rs.getLong("time")));
        }
      }
      catch (SQLException e) {
        e.printStackTrace();
      }
      return uuidEntries;
    }
  }

  public static void updateUUID(String uuid) throws IOException, UUIDNotFoundException {
    uuid = uuid.toLowerCase().replaceAll("[^a-z0-9]", "");
    if (uuid.length() != 32) {
      Logger.write("Failed to update UUID '" + uuid + "'. Invalid UUID");
      throw new UUIDNotFoundException(uuid);
    }
    LinkedHashSet<NameEntry> nameEntries = Parser.parseNameRequest(NetRequest.fetchNames(uuid));
    if (nameEntries.size() == 0) {
      Logger.write("Failed to update UUID '" + uuid + "'. Could not retrieve uuid data");
      throw new UUIDNotFoundException(uuid);
    }
    try {
      for (NameEntry nameEntry : nameEntries) {
        connection.createStatement().execute("REPLACE INTO data (uuid, name, time) VALUES ('" + uuid + "','"
              + nameEntry.name + "'," + nameEntry.changedToAt + ");");
        Logger.write("Updated UUID '" + uuid + "' with nameEntry '" + nameEntry + "'");
        connection.createStatement().execute("REPLACE INTO retrieved (user, time) VALUES ('" + nameEntry.name
              + "'," + System.currentTimeMillis() + ");");
      }
      connection.createStatement().execute("REPLACE INTO retrieved (user, time) VALUES ('" + uuid + "',"
            + System.currentTimeMillis() + ");");
    }
    catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public static void updateName(String name) throws IOException, UserNotFoundException {
    name = name.toLowerCase().replaceAll("[^a-z0-9_]", "");
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
      connection.createStatement().execute("REPLACE INTO retrieved (user, time) VALUES ('" + name + "',"
            + System.currentTimeMillis() + ");");
    } catch (UUIDNotFoundException e) {
      throw new UserNotFoundException(name);
    }
    catch (SQLException e) {
      e.printStackTrace();
    }
  }
}
