package com.thekdub.mcuuid.utilities;

import com.thekdub.mcuuid.objects.Name;

import java.util.LinkedHashSet;

public class Parser {
  public static LinkedHashSet<Name> parseNameRequest(String str) {
    str = str.substring(1, str.length() - 1);
    LinkedHashSet<Name> names = new LinkedHashSet<Name>();
    if (str.contains("},{")) {
      for (String s : str.split("[}],[{]")) {
        names.add(new Name(s));
      }
    }
    return names;
  }

  public static String parseUUIDRequest(String str) {
    str = str.split(",")[0].replaceAll("[\"{}]", "");
    return str.substring(3);
  }
}
