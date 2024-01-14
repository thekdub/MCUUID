package com.thekdub.mcuuid.utilities;

import com.thekdub.mcuuid.MCUUID;
import com.thekdub.mcuuid.exceptions.InvalidUUIDException;
import com.thekdub.mcuuid.exceptions.UUIDNotFoundException;
import com.thekdub.mcuuid.exceptions.UserNotFoundException;
import com.thekdub.mcuuid.objects.NameEntry;
import com.thekdub.mcuuid.objects.UUIDEntry;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.LinkedHashSet;

// Note: I am aware the database transaction strings in this are improperly handled; I no longer create statements in this way and have not done so in new applications for years.
public class DataStore {

  private static Connection connection = null;

  public static void init() {
    if (connection == null) {
      try {
        connection = DriverManager.getConnection("jdbc:sqlite:" + MCUUID.instance.getDataFolder() + File.separator
              + "uuid.db");
      }
      catch (SQLException e) {
        e.printStackTrace();
      }
      try (PreparedStatement data = connection.prepareStatement("CREATE TABLE IF NOT EXISTS data (uuid TEXT, " +
            "name TEXT, time INTEGER, PRIMARY KEY(uuid, name, time));");
      PreparedStatement retrieved = connection.prepareStatement("CREATE TABLE IF NOT EXISTS retrieved (user TEXT, " +
            "time INTEGER, PRIMARY KEY(user));")) {
        data.execute();
        retrieved.execute();
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
    try (PreparedStatement ps = connection.prepareStatement("SELECT uuid FROM data WHERE name='" + name
            + "';");
          ResultSet rs = ps.executeQuery()) {
      return rs.next();
    }
    catch (SQLException e) {
      e.printStackTrace();
    }
    return false;
  }

  private static boolean uuidCached(String uuid) throws InvalidUUIDException {
    uuid = Parser.formatUUID(uuid);
    try (PreparedStatement ps = connection.prepareStatement("SELECT name FROM data WHERE uuid='" + uuid
            + "';");
          ResultSet rs = ps.executeQuery()) {
      return rs.next();
    }
    catch (SQLException e) {
      e.printStackTrace();
    }
    return false;
  }

  private static boolean needsUpdate(String user) {
    user = user.toLowerCase().replaceAll("[^a-z0-9_-]", "");
    long time = 0L;
    try (PreparedStatement ps = connection.prepareStatement("SELECT time FROM retrieved WHERE user='" + user
            + "';");
          ResultSet rs = ps.executeQuery()) {
      if (rs.next()) {
        time = rs.getLong("time");
      }
    }
    catch (SQLException e) {
      e.printStackTrace();
    }
    return time < System.currentTimeMillis() - MCUUID.instance.updateFrequency;
  }

  public static String getName(String uuid) throws IOException, UUIDNotFoundException, InvalidUUIDException {
    uuid = Parser.formatUUID(uuid);
    if (uuidCached(uuid) && !needsUpdate(uuid)) {
      try (PreparedStatement ps = connection.prepareStatement("SELECT name FROM data WHERE uuid='" + uuid
              + "' ORDER BY time DESC;");
            ResultSet rs = ps.executeQuery()) {
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
      try (PreparedStatement ps = connection.prepareStatement("SELECT name FROM data WHERE uuid='" + uuid
              + "' ORDER BY time DESC;");
            ResultSet rs = ps.executeQuery()) {
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
      try (PreparedStatement ps = connection.prepareStatement("SELECT uuid FROM data WHERE name='" + name
              + "' ORDER BY time DESC;");
            ResultSet rs = ps.executeQuery()) {
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
      try (PreparedStatement ps = connection.prepareStatement("SELECT uuid FROM data WHERE name='" + name
              + "' ORDER BY time DESC;");
            ResultSet rs = ps.executeQuery()) {
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

  public static LinkedHashSet<NameEntry> getNameHistory(String uuid) throws IOException, UUIDNotFoundException,
        InvalidUUIDException {
    uuid = Parser.formatUUID(uuid);
    if (uuidCached(uuid) && !needsUpdate(uuid)) {
      LinkedHashSet<NameEntry> nameEntries = new LinkedHashSet<>();
      try (PreparedStatement ps = connection.prepareStatement("SELECT name,time FROM data WHERE uuid='" + uuid
              + "' ORDER BY time DESC;");
            ResultSet rs = ps.executeQuery()) {
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
      try (PreparedStatement ps = connection.prepareStatement("SELECT name,time FROM data WHERE uuid='" + uuid
              + "' ORDER BY time DESC;");
            ResultSet rs = ps.executeQuery()) {
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
      try (PreparedStatement ps = connection.prepareStatement("SELECT uuid,time FROM data WHERE name='" + name
              + "' ORDER BY time DESC;");
            ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          try {
            uuidEntries.add(new UUIDEntry(Parser.parseUUID(rs.getString("uuid")),
                  rs.getLong("time")));
          }
          catch (InvalidUUIDException e) {
            e.printStackTrace();
          }
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
      try (PreparedStatement ps = connection.prepareStatement("SELECT uuid,time FROM data WHERE name='" + name
              + "' ORDER BY time DESC;");
            ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          try {
            uuidEntries.add(new UUIDEntry(Parser.parseUUID(rs.getString("uuid")),
                  rs.getLong("time")));
          }
          catch (InvalidUUIDException e) {
            e.printStackTrace();
          }
        }
      }
      catch (SQLException e) {
        e.printStackTrace();
      }
      return uuidEntries;
    }
  }

  public static void updateUUID(String uuid) throws IOException, UUIDNotFoundException, InvalidUUIDException {
    uuid = Parser.formatUUID(uuid);
    if (uuid.length() != 36) {
      Logger.write("Failed to update UUID '" + uuid + "'. Invalid UUID");
      throw new UUIDNotFoundException(uuid);
    }
    LinkedHashSet<NameEntry> nameEntries = Parser.parseNameRequest(NetRequest.fetchNames(uuid));
    if (nameEntries.size() == 0) {
      Logger.write("Failed to update UUID '" + uuid + "'. Could not retrieve uuid data");
      throw new UUIDNotFoundException(uuid);
    }
    for (NameEntry nameEntry : nameEntries) {
      try (PreparedStatement data = connection.prepareStatement("REPLACE INTO DATA (uuid, name, time) VALUES (" +
            "'" + uuid + "','" +
            nameEntry.name + "'," +
            nameEntry.changedToAt + ");");
            PreparedStatement retrieved = connection.prepareStatement("REPLACE INTO retrieved (user, time) VALUES (" +
                  "'" + nameEntry.name + "'," +
                  System.currentTimeMillis() + ");")) {
        data.execute();
        Logger.write("Updated UUID '" + uuid + "' with nameEntry '" + nameEntry + "'");
        retrieved.execute();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
    try (PreparedStatement statement = connection.prepareStatement("REPLACE INTO retrieved (user, time) VALUES (" +
          "'" + uuid + "'," +
          System.currentTimeMillis() + ");")) {
      statement.execute();
    } catch (SQLException e) {
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
    if (uuid.length() != 36) {
      Logger.write("Failed to update name '" + name + "'. Retrieved invalid UUID '" + uuid + "'");
      throw new UserNotFoundException(name);
    }
    try (PreparedStatement statement = connection.prepareStatement("REPLACE INTO retrieved (user, time) VALUES ('"
          + name + "'," + System.currentTimeMillis() + ");")) {
      updateUUID(uuid);
      statement.execute();
    } catch (UUIDNotFoundException | InvalidUUIDException e) {
      throw new UserNotFoundException(name);
    }
    catch (SQLException e) {
      e.printStackTrace();
    }
  }
}
