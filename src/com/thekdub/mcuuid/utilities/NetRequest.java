package com.thekdub.mcuuid.utilities;

import com.thekdub.mcuuid.exceptions.UUIDNotFoundException;
import com.thekdub.mcuuid.exceptions.UserNotFoundException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class NetRequest {
  public static String fetchUUID(String player, long ms) throws IOException, UserNotFoundException {
    player = player.replaceAll("[^a-zA-Z0-9_]", "");
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
    if (result.length() == 0)
      throw new UserNotFoundException(player);
    return result.toString();
  }

  public static String fetchName(String uuid) throws IOException, UUIDNotFoundException {
    uuid = uuid.replaceAll("[^a-zA-Z0-9]", "");
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
    if (result.length() == 0)
      throw new UUIDNotFoundException(uuid);
    return result.toString();
  }
}
