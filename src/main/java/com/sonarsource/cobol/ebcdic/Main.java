/*
 * EBCDIC to ASCII converter
 * Copyright (C) 2012 SonarSource
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package com.sonarsource.cobol.ebcdic;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public final class Main {

  private static final String INPUT_CHARSET_DEFAULT = "CP1047";
  private static final int FIXED_LENGTH_DEFAULT = 80;

  public static void main(String[] args) {
    Charset input = charsetForName(INPUT_CHARSET_DEFAULT);
    Charset output = Charset.defaultCharset();
    File source = null;
    File destination = null;
    int fixedLength = FIXED_LENGTH_DEFAULT;
    for (int i = 0; i < args.length; i++) {
      String arg = args[i];
      if ("-h".equals(arg) || "--help".equals(arg)) {
        printUsage();
      } else if ("-f".equals(arg)) {
        i++;
        if (i >= args.length) {
          printError("Missing argument for option -f");
        }
        input = charsetForName(args[i]);
      } else if ("-t".equals(arg)) {
        i++;
        if (i >= args.length) {
          printError("Missing argument for option -t");
        }
        output = charsetForName(args[i]);
      } else if ("-l".equals(arg)) {
        i++;
        if (i >= args.length) {
          printError("Missing argument for option -l");
        }
        try {
          fixedLength = Integer.parseInt(args[i]);
        } catch (NumberFormatException e) {
          printError("Expected number for option -l, but got: " + args[i]);
        }
      } else {
        File dir = new File(args[i]);
        if (source == null) {
          source = dir;
          if (!source.isDirectory()) {
            printError("No such directory: " + source);
          }
        } else {
          destination = dir;
        }
      }
    }

    if (source == null) {
      printError("Missing source.");
    }
    if (destination == null) {
      printError("Missing destination.");
    }

    try {
      FileConverter converter = new FileConverter(input, output);
      converter.setFixedLength(fixedLength);

      List<String> files = listFiles(source);
      for (String s : files) {
        File sourceFile = new File(source, s);
        File destFile = new File(destination, s);
        log("Converting " + sourceFile + " into " + destFile);
        destFile.getParentFile().mkdirs();
        converter.convert(sourceFile, destFile);
      }
      log("SUCCESS");
    } catch (EbcdicToAsciiConverterException e) {
      log("Unable to convert files", e);
      log("FAILURE");
    }
  }

  private static Charset charsetForName(String charsetName) {
    try {
      return Charset.forName(charsetName);
    } catch (Exception e) {
      printError("Unknown charset: " + charsetName);
      throw new EbcdicToAsciiConverterException("'" + charsetName + "' is an unknown charset.", e);
    }
  }

  private static void printUsage() {
    log("");
    log("Usage: [options] source destination");
    log("");
    log("Convert source into destination.");
    log("  source         relative or absolute path to the directory containing files to be converted");
    log("  destination    relative or absolute path to the directory, which will contain result of conversion");
    log("");
    log("Options:");
    log("  -h, --help     Display help information");
    log("  -f encoding    encoding of original text (" + INPUT_CHARSET_DEFAULT + " by default)");
    log("  -t encoding    encoding for output (by default the one of the OS: " + Charset.defaultCharset().displayName() + ")");
    log("  -l length      number of characters to split output by lines (" + FIXED_LENGTH_DEFAULT + " by default)");
    System.exit(0);
  }

  private static List<String> listFiles(File dir) {
    List<String> files = new ArrayList<String>();
    recursivelyListFiles(dir, "", files);
    return files;
  }

  private static void recursivelyListFiles(File dir, String relativePath, List<String> files) {
    for (String s : dir.list()) {
      String path = relativePath + File.separator + s;
      File file = new File(dir, s);
      if (file.isFile() && !file.isHidden()) {
        files.add(path);
      } else if (file.isDirectory() && !file.isHidden()) {
        recursivelyListFiles(file, path, files);
      }
    }
  }

  private static void printError(String message) {
    log("");
    log(message);
    printUsage();
  }

  private static void log(String message) {
    System.out.println(message);
  }

  private static void log(String message, Throwable e) {
    System.out.println(message);
    e.printStackTrace();
  }

  private Main() {
  }

}
