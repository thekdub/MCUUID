package com.thekdub.mcuuid.objects;

public class Name {
  public final String name;
  public final long changedToAt;

  public Name(String name) {
    this.name = name;
    this.changedToAt = 0L;
  }

  public Name(String name, long changedToAt) {
    this.name = name;
    this.changedToAt = changedToAt;
  }

  public String toString() {
    return name + " @ " + changedToAt;
  }
}
