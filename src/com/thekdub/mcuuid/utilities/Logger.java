package com.thekdub.mcuuid.utilities;

import com.thekdub.mcuuid.MCUUID;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Logger {

  private static File file;
  private static BufferedWriter writer;

  private static void init() {
    if (file == null) {
      file = new File(MCUUID.instance.getDataFolder() + File.separator + "output.log");
    }
    if (writer == null) {
      try {
        writer = new BufferedWriter(new FileWriter(file, true));
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public static void save() {
    init();
    try {
      writer.flush();
      writer.close();
      file = null;
      writer = null;
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void write(String msg) {
    init();
    try {
      writer.write(Parser.parseMillis(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss:SSS") + " >> " + msg + "\n");
      writer.flush();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }


}
