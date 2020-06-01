package com.thekdub.mcuuid.objects;

public class UUIDEntry {
  public final String uuid;
  public final long changedToAt;

  public UUIDEntry(String uuid) {
    this.uuid = uuid.toLowerCase();
    this.changedToAt = 0L;
  }

  public UUIDEntry(String uuid, long changedToAt) {
    this.uuid = uuid.toLowerCase();
    this.changedToAt = changedToAt;
  }

  public String toString() {
    return uuid + " @ " + changedToAt;
  }
}
