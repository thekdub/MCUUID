package com.thekdub.mcuuid.exceptions;

public class UserNotFoundException extends Exception {
  public UserNotFoundException(String user) {
    super("The user '" + user + "' could not be found by the Mojang API!");
  }
}
