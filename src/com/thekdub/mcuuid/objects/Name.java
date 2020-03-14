package com.thekdub.mcuuid.objects;

public class Name {
  public final String name;
  public final long changedToAt;

  public Name(String str) {
    str = str.replaceAll("[\"{}]", "");
    if (str.contains(",")) {
      String[] spl = str.split(",");
      name = spl[0].split(":")[1];
      changedToAt = Long.parseLong(spl[1].split(":")[1]);
    }
    else {
      name = str.split(":")[1];
      changedToAt = 0L;
    }
  }

  public Name(String name, long changedToAt) {
    this.name = name;
    this.changedToAt = changedToAt;
  }

  public String toString() {
    return name + " @ " + changedToAt;
  }
}
