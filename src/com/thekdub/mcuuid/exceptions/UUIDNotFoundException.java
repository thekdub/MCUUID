package com.thekdub.mcuuid.exceptions;

import com.thekdub.mcuuid.utilities.Logger;

public class UUIDNotFoundException extends Exception {
  public UUIDNotFoundException(String uuid) {
    super("The UUID '" + uuid + "' could not be found by the Mojang API!");
    Logger.write("A UUIDNotFoundException was thrown for the UUID '" + uuid + "'");
  }
}