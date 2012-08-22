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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.nio.charset.Charset;

public class FileConverter {

  private static final int INITIAL_BUFFER_SIZE = 2048;
  private static final int LF = '\n';
  private static final int NEL = 0x15;
  private static final int WS = ' ';
  static final Charset CP1047 = Charset.forName("Cp1047");
  private static final char[] NON_PRINTABLE_EBCDIC_CHARS = new char[] { 0x00, 0x01, 0x02, 0x03, 0x9C, 0x09, 0x86, 0x7F, 0x97, 0x8D, 0x8E,
      0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x10, 0x11, 0x12, 0x13, 0x9D, 0x85, 0x08, 0x87, 0x18, 0x19, 0x92, 0x8F, 0x1C, 0x1D, 0x1E, 0x1F, 0x80,
      0x81, 0x82, 0x83, 0x84, 0x0A, 0x17, 0x1B, 0x88, 0x89, 0x8A, 0x8B, 0x8C, 0x05, 0x06, 0x07, 0x90, 0x91, 0x16, 0x93, 0x94, 0x95, 0x96,
      0x04, 0x98, 0x99, 0x9A, 0x9B, 0x14, 0x15, 0x9E, 0x1A, 0x20, 0xA0 };

  private final Charset ebcdicCharset;
  private final Charset outputCharset;
  private int fixedLength = -1;

  public FileConverter(Charset ebcdicCharset, Charset outputCharset) {
    this.ebcdicCharset = ebcdicCharset;
    this.outputCharset = outputCharset;
  }

  public void setFixedLength(int numberOfColumn) {
    this.fixedLength = numberOfColumn;
  }

  void convert(File ebcdicInputFile, File convertedOutputFile) {
    Reader reader = null;
    Writer writer = null;
    try {
      reader = new BufferedReader(new InputStreamReader(new FileInputStream(ebcdicInputFile), ebcdicCharset));
      int[] ebcdicInput = loadContent(reader);
      close(reader);
      writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(convertedOutputFile), outputCharset));
      convert(ebcdicInput, writer);
    } catch (Exception e) {
      throw new EbcdicToAsciiConverterException("Unable to convert file " + ebcdicInputFile.getAbsolutePath(), e);
    } finally {
      close(writer);
    }
  }

  private void close(Closeable closeable) {
    try {
      if (closeable != null) {
        closeable.close();
      }
    } catch (IOException e) {
      throw new EbcdicToAsciiConverterException("Unable to close", e);
    }
  }

  void convert(String input, Writer convertedOutputWriter) throws IOException {
    convert(loadContent(new StringReader(input)), convertedOutputWriter);
  }

  private void convert(int[] ebcdicInput, Writer convertedOutputWriter) throws IOException {
    int convertedChar;
    for (int index = 0; index < ebcdicInput.length; index++) {
      int character = ebcdicInput[index];
      if (fixedLength != -1 && index > 0 && index % fixedLength == 0) {
        convertedOutputWriter.append((char) LF);
      }
      if (fixedLength == -1 && character == NEL) {
        convertedChar = LF;
      } else {
        convertedChar = replaceNonPrintableCharacterByWhitespace(character);
      }
      convertedOutputWriter.append((char) convertedChar);
    }
  }

  private int replaceNonPrintableCharacterByWhitespace(int character) {
    for (char nonPrintableChar : NON_PRINTABLE_EBCDIC_CHARS) {
      if (nonPrintableChar == (char) character) {
        return WS;
      }
    }
    return character;
  }

  private int[] loadContent(Reader reader) throws IOException {
    int[] buffer = new int[INITIAL_BUFFER_SIZE];
    int bufferIndex = 0;
    int bufferSize = buffer.length;
    int character;
    while ((character = reader.read()) != -1) {
      if (bufferIndex == bufferSize) {
        buffer = resizeArray(buffer, bufferSize + INITIAL_BUFFER_SIZE);
        bufferSize = buffer.length;
      }
      buffer[bufferIndex++] = character;
    }
    return resizeArray(buffer, bufferIndex);
  }

  final int[] resizeArray(int[] orignalArray, int newSize) {
    int[] resizedArray = new int[newSize];
    for (int i = 0; i < newSize && i < orignalArray.length; i++) {
      resizedArray[i] = orignalArray[i];
    }
    return resizedArray;
  }

}
