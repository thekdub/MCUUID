package com.thekdub.mcuuid.utilities;

import com.thekdub.mcuuid.MCUUID;
import com.thekdub.mcuuid.exceptions.UUIDNotFoundException;
import com.thekdub.mcuuid.exceptions.UserNotFoundException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

class NetRequest {

  private static HashMap<String, Long> failedCooldown = new HashMap<>();

  static String fetchUUID(String player, long ms) throws IOException, UserNotFoundException {
    player = player.replaceAll("[^a-zA-Z0-9_]", "");
    if (failedCooldown.containsKey(player)) {
      if (failedCooldown.get(player) + MCUUID.instance.failureCooldown > System.currentTimeMillis()) {
        throw new UserNotFoundException(player);
      }
      else {
        failedCooldown.remove(player);
      }
    }
    Logger.write("Fetching UUID for '" + player + "' from Mojang API.");
    String url = "https://api.mojang.com/users/profiles/minecraft/" + player + "?at=" + ms;
    HttpURLConnection urlConnection = (HttpURLConnection) (new URL(url)).openConnection();
    urlConnection.setRequestMethod("GET");
    BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
    StringBuilder result = new StringBuilder();
    String line;
    while ((line = br.readLine()) != null) {
      result.append(line);
    }
    br.close();
    if (result.length() == 0) {
      failedCooldown.put(player, System.currentTimeMillis());
      Logger.write("Failed to fetch UUID for '" + player + "' from Mojang API.");
      throw new UserNotFoundException(player);
    }
    return result.toString();
  }

  static String fetchNames(String uuid) throws IOException, UUIDNotFoundException {
    uuid = uuid.replaceAll("[^a-zA-Z0-9]", "");
    if (failedCooldown.containsKey(uuid)) {
      if (failedCooldown.get(uuid) + MCUUID.instance.failureCooldown > System.currentTimeMillis()) {
        throw new UUIDNotFoundException(uuid);
      }
      else {
        failedCooldown.remove(uuid);
      }
    }
    Logger.write("Fetching name for '" + uuid + "' from Mojang API.");
    String url = "https://api.mojang.com/user/profiles/" + uuid + "/names";
    HttpURLConnection urlConnection = (HttpURLConnection) (new URL(url)).openConnection();
    urlConnection.setRequestMethod("GET");
    BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
    StringBuilder result = new StringBuilder();
    String line;
    while ((line = br.readLine()) != null) {
      result.append(line);
    }
    br.close();
    if (result.length() == 0) {
      failedCooldown.put(uuid, System.currentTimeMillis());
      Logger.write("Failed to fetch name for '" + uuid + "' from Mojang API.");
      throw new UUIDNotFoundException(uuid);
    }
    return result.toString();
  }
}
