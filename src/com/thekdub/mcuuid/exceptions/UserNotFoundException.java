package com.thekdub.mcuuid.exceptions;

import com.thekdub.mcuuid.utilities.Logger;

public class UserNotFoundException extends Exception {
  public UserNotFoundException(String user) {
    super("The user '" + user + "' could not be found by the Mojang API!");
    Logger.write("A UserNotFoundException was thrown for the user '" + user + "'");
  }
}
