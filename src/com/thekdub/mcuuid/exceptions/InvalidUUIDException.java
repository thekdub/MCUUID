package com.thekdub.mcuuid.exceptions;

import com.thekdub.mcuuid.utilities.Logger;

public class InvalidUUIDException extends Exception {
  public InvalidUUIDException(String uuid) {
    super("The UUID '" + uuid + "' is not valid!");
    Logger.write("An InvalidUUIDException was thrown for the UUID '" + uuid + "'");
  }
  public InvalidUUIDException() {
    super("The UUID 'null' is not valid!");
    Logger.write("An InvalidUUIDException was thrown for the UUID 'null'");
  }
}
