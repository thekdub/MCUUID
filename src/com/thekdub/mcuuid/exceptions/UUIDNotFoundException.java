package com.thekdub.mcuuid.exceptions;

public class UUIDNotFoundException extends Exception {
  public UUIDNotFoundException(String uuid) {
    super("The UUID '" + uuid + "' could not be found by the Mojang API!");
  }
}