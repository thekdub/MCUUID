package com.thekdub.mcuuid.objects;

public class NameEntry {
  public final String name;
  public final long changedToAt;

  public NameEntry(String name) {
    this.name = name.toLowerCase();
    this.changedToAt = 0L;
  }

  public NameEntry(String name, long changedToAt) {
    this.name = name.toLowerCase();
    this.changedToAt = changedToAt;
  }

  public String toString() {
    return name + " @ " + changedToAt;
  }
}
