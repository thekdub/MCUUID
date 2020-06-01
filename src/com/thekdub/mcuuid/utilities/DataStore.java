package com.thekdub.mcuuid.utilities;

import com.thekdub.mcuuid.MCUUID;
import com.thekdub.mcuuid.exceptions.UUIDNotFoundException;
import com.thekdub.mcuuid.exceptions.UserNotFoundException;
import com.thekdub.mcuuid.objects.NameEntry;
import com.thekdub.mcuuid.objects.UUIDEntry;

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
              "time INTEGER, PRIMARY KEY(uuid, name));");
      }
      catch (SQLException e) {
        e.printStackTrace();
      }
    }
  }

  public static void save() {
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
      ResultSet rs = connection.prepareStatement("SELECT uuid FROM data WHERE name='" + name + "';").executeQuery();
      if (rs.next()) {
        return true;
      }
    }
    catch (SQLException e) {
      e.printStackTrace();
    }
    return false;
  }

  private static boolean uuidCached(String uuid) {
    uuid = uuid.toLowerCase().replaceAll("[^a-z0-9]", "");
    try {
      ResultSet rs = connection.prepareStatement("SELECT name FROM data WHERE uuid='" + uuid + "';").executeQuery();
      if (rs.next()) {
        return true;
      }
    }
    catch (SQLException e) {
      e.printStackTrace();
    }
    return false;
  }

  public static String getName(String uuid) throws IOException, UUIDNotFoundException {
    uuid = uuid.toLowerCase().replaceAll("[^a-z0-9]", "");
    if (uuidCached(uuid)) {
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
    if (nameCached(name)) {
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
      System.out.println("getUUID updated");
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
    if (uuidCached(uuid)) {
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

  public static LinkedHashSet<UUIDEntry> getUUIDHistory(String name) throws IOException, UUIDNotFoundException {
    name = name.toLowerCase().replaceAll("[^a-z0-9_]", "");
    if (uuidCached(name)) {
      LinkedHashSet<UUIDEntry> nameEntries = new LinkedHashSet<>();
      try {
        ResultSet rs = connection.prepareStatement("SELECT uuid,time FROM data WHERE name='" + name
              + "' ORDER BY time DESC;").executeQuery();
        while (rs.next()) {
          nameEntries.add(new UUIDEntry(rs.getString("uuid"), rs.getLong("time")));
        }
      }
      catch (SQLException e) {
        e.printStackTrace();
      }
      return nameEntries;
    }
    else {
      updateUUID(name);
      LinkedHashSet<UUIDEntry> nameEntries = new LinkedHashSet<>();
      try {
        ResultSet rs = connection.prepareStatement("SELECT uuid,time FROM data WHERE name='" + name
              + "' ORDER BY time DESC;").executeQuery();
        while (rs.next()) {
          nameEntries.add(new UUIDEntry(rs.getString("uuid"), rs.getLong("time")));
        }
      }
      catch (SQLException e) {
        e.printStackTrace();
      }
      return nameEntries;
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
      }
    }
    catch (SQLException e) {
      e.printStackTrace();
    }
    save();
  }

  public static void updateName(String name) throws IOException, UserNotFoundException {
    name = name.toLowerCase().replaceAll("[^a-z0-9_]", "");
    String uuid = Parser.parseUUIDRequest(NetRequest.fetchUUID(name, System.currentTimeMillis()));
    if (name.length() > 16 || name.length() == 0) {
      Logger.write("Failed to update uuid '" + name + "'. Invalid uuid");
      throw new UserNotFoundException(name);
    }
    if (uuid.length() != 32) {
      Logger.write("Failed to update uuid '" + name + "'. Retrieved invalid UUID '" + uuid + "'");
      throw new UserNotFoundException(name);
    }
    try {
      updateUUID(uuid);
    } catch (UUIDNotFoundException e) {
      throw new UserNotFoundException(name);
    }
  }
}
