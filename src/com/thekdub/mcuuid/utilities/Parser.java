package com.thekdub.mcuuid.utilities;

import com.thekdub.mcuuid.exceptions.InvalidUUIDException;
import com.thekdub.mcuuid.exceptions.UUIDNotFoundException;
import com.thekdub.mcuuid.objects.NameEntry;
import org.omg.CORBA.DynAnyPackage.Invalid;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.TimeZone;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {
  static LinkedHashSet<NameEntry> parseNameRequest(String str) {
    str = str.substring(1, str.length() - 1).replace("\"", "");
    LinkedHashSet<NameEntry> nameEntries = new LinkedHashSet<NameEntry>();
    if (str.contains("},{")) {
      for (String s : str.split("[}],[{]")) {
        nameEntries.add(makeName(s));
      }
    }
    else {
      nameEntries.add(makeName(str.substring(1, str.length() - 1)));
    }
    return nameEntries;
  }

  private static NameEntry makeName(String str) {
    str = str.replaceAll("[\"{}]", "");
    if (str.contains(",")) {
      String[] split = str.split(",");
      return new NameEntry(split[0].split(":")[1], Long.parseLong(split[1].split(":")[1]));
    }
    else {
      return new NameEntry(str.split(":")[1]);
    }
  }

  static String parseUUIDRequest(String str) {
    str = str.replaceAll("[\"{}]", "").split(",")[1];
    try {
      return formatUUID(str.substring(3));
    }
    catch (InvalidUUIDException e) {
      e.printStackTrace();
    }
    return str;
  }

  public static long parseTime(String str) {
    Pattern timePattern = Pattern.compile("(?:([0-9]+)\\s*y[a-z]*[,\\s]*)?(?:([0-9]+)\\s*mo[a-z]*[,\\s]*)?(?:([0-9]+)\\s*w[a-z]*[,\\s]*)?(?:([0-9]+)\\s*d[a-z]*[,\\s]*)?(?:([0-9]+)\\s*h[a-z]*[,\\s]*)?(?:([0-9]+)\\s*m[a-z]*[,\\s]*)?(?:([0-9]+)\\s*(?:s[a-z]*)?)?", 2);
    Matcher m = timePattern.matcher(str);
    long years = 0L;
    long msyear = 31536000000L;
    long months = 0L;
    long msmonth = 2628000000L;
    long weeks = 0L;
    long msweek = 604800000L;
    long days = 0L;
    long msday = 86400000L;
    long hours = 0L;
    long mshour = 3600000L;
    long minutes = 0L;
    long msminute = 60000L;
    long seconds = 0L;
    long mssecond = 1000L;
    boolean found = false;
    while (m.find()) {
      if ((m.group() != null) && (!m.group().isEmpty())) {
        for (int i = 0; i < m.groupCount(); i++) {
          if ((m.group(i) != null) && (!m.group(i).isEmpty())) {
            found = true;
            break;
          }
        }
        if (found) {
          if ((m.group(1) != null) && (!m.group(1).isEmpty())) {
            years = Integer.parseInt(m.group(1));
          }
          if ((m.group(2) != null) && (!m.group(2).isEmpty())) {
            months = Integer.parseInt(m.group(2));
          }
          if ((m.group(3) != null) && (!m.group(3).isEmpty())) {
            weeks = Integer.parseInt(m.group(3));
          }
          if ((m.group(4) != null) && (!m.group(4).isEmpty())) {
            days = Integer.parseInt(m.group(4));
          }
          if ((m.group(5) != null) && (!m.group(5).isEmpty())) {
            hours = Integer.parseInt(m.group(5));
          }
          if ((m.group(6) != null) && (!m.group(6).isEmpty())) {
            minutes = Integer.parseInt(m.group(6));
          }
          if ((m.group(7) != null) && (!m.group(7).isEmpty())) {
            seconds = Integer.parseInt(m.group(7));
          }
        }
      }
    }
    long val = years * msyear + months * msmonth + weeks * msweek + days * msday + hours * mshour + minutes * msminute + seconds * mssecond;
    return years + months + weeks + days + hours + minutes + seconds > 0L ? val : val < 0L ? 9223372036854775806L : -1L;
  }

  public static String parseMillis(long millis, String dateFormat) {
    // "MM-dd-yyyy HH:mm" = MonthMonth-DayDay-YearYearYearYear HourHour:MinuteMinute
    DateFormat format = new SimpleDateFormat(dateFormat);
    format.setTimeZone(TimeZone.getTimeZone("America/New_York"));
    Date time = new Date(millis);
    return format.format(time);
  }

  public static UUID parseUUID(String uuid) throws InvalidUUIDException {
    uuid = uuid.toLowerCase().replaceAll("[^0-9a-z]", "");
    if (uuid.length() == 32) {
      return UUID.fromString(uuid.substring(0,8) + "-" + uuid.substring(8,12) + "-" + uuid.substring(12,16) + "-"
            + uuid.substring(16,20) + "-" + uuid.substring(20,32));
    }
    throw new InvalidUUIDException(uuid);
  }

  public static String formatUUID(String uuid) throws InvalidUUIDException {
    uuid = uuid.toLowerCase().replaceAll("[^0-9a-z]", "");
    if (uuid.length() == 32) {
      return uuid.substring(0,8) + "-" + uuid.substring(8,12) + "-" + uuid.substring(12,16) + "-"
            + uuid.substring(16,20) + "-" + uuid.substring(20,32);
    }
    throw new InvalidUUIDException(uuid);
  }
}
