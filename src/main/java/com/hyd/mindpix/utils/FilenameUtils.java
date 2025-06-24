package com.hyd.mindpix.utils;

import java.io.File;

public class FilenameUtils {

  public static String getFileNameFromPath(String filePath) {
    int lastSeparatorIndex = filePath.lastIndexOf(File.separator);
    if (lastSeparatorIndex != -1) {
      return filePath.substring(lastSeparatorIndex + 1);
    } else {
      return filePath;
    }
  }
}
