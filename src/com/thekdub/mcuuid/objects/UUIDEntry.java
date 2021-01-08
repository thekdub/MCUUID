package com.thekdub.mcuuid.objects;

import java.util.UUID;

public class UUIDEntry {
  public final UUID uuid;
  public final long changedToAt;

  public UUIDEntry(UUID uuid) {
    this.uuid = uuid;
    this.changedToAt = 0L;
  }

  public UUIDEntry(UUID uuid, long changedToAt) {
    this.uuid = uuid;
    this.changedToAt = changedToAt;
  }

  public String toString() {
    return uuid + " @ " + changedToAt;
  }
}
