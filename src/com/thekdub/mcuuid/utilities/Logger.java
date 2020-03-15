package com.thekdub.mcuuid.utilities;

import com.thekdub.mcuuid.MCUUID;

import java.io.*;

public class Logger {

  private static File file;
  private static BufferedWriter writer;

  public static void init() {
    if (file == null) {
      file = new File(MCUUID.instance.getDataFolder() + File.separator + "output.log");
    }
    if (writer == null) {
      try {
        writer = new BufferedWriter(new FileWriter(file));
      }
      catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public static void save() {
    init();
    try {
      writer.flush();
      writer.close();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void write(String msg) {
    init();
    try {
      writer.write(Parser.parseMillis(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss:SSS") + " >> " + msg);
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }


}
