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

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.File;
import java.io.StringWriter;
import java.nio.charset.Charset;

import static org.fest.assertions.Assertions.assertThat;

public class FileConverterTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private FileConverter converter = new FileConverter(Charset.forName("UTF-8"), Charset.forName("UTF-8"));

  @Test
  public void shouldConvertCarriageReturnAndLineFeedIntoWhitespace() throws Exception {
    StringWriter writer = new StringWriter();
    converter.convert("This is\ra\nline", writer);
    assertThat(writer.toString()).isEqualTo("This is a line");
  }

  @Test
  public void shouldConvertNextLineIntoLineFeed() throws Exception {
    StringWriter writer = new StringWriter();
    converter.convert("This is a " + (char) 0x15 + " new line", writer);
    assertThat(writer.toString()).isEqualTo("This is a \n new line");
  }

  public static void main(String[] args) {
    FileConverter converter = new FileConverter(Charset.forName("UTF-8"), Charset.forName("UTF-8"));
    StringWriter writer = new StringWriter();
    try {
      converter.convert("This is a " + (char) 0x15 + " new line", writer);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Test
  public void shouldConvertNextLineIntoWhitespaceWhenFixedLength() throws Exception {
    converter.setFixedLength(30);
    StringWriter writer = new StringWriter();
    converter.convert("This is a" + (char) 0x15 + "space", writer);
    assertThat(writer.toString()).isEqualTo("This is a space");
  }

  @Test
  public void shouldInsertLineFeedAtTheEndOfFixedLengthLines() throws Exception {
    converter.setFixedLength(3);
    StringWriter writer = new StringWriter();
    converter.convert("a23456789", writer);
    assertThat(writer.toString()).isEqualTo("a23\n456\n789");
  }

  @Test
  public void shouldConvertEbcdicFile() throws Exception {
    converter = new FileConverter(FileConverter.CP1047, Charset.forName("UTF-8"));
    converter.setFixedLength(80);
    File ebcdicFile = new File(FileConverterTest.class.getResource("/EBCDIC.txt").toURI());
    File expectedOutputFile = new File(FileConverterTest.class.getResource("/ASCII.txt").toURI());

    File workingFile = new File("target/converter/cobol.txt");
    FileUtils.copyFile(ebcdicFile, workingFile);

    converter.convert(workingFile, workingFile);
    assertThat(FileUtils.contentEquals(workingFile, expectedOutputFile)).isTrue();
  }

  @Test
  public void shouldConvertAllEbcdicFilesInDirectoryAndSubDirectories() throws Exception {
    File ebcdicFile = new File(FileConverterTest.class.getResource("/EBCDIC.txt").toURI());
    File expectedOutputFile = new File(FileConverterTest.class.getResource("/ASCII.txt").toURI());

    File workingFile1 = new File("target/converter/cobol.txt");
    FileUtils.copyFile(ebcdicFile, workingFile1);
    File workingFile2 = new File("target/converter/sub/cobol.txt");
    FileUtils.copyFile(ebcdicFile, workingFile2);
    File workingFile3 = new File("target/converter/sub/sub/cobol.txt");
    FileUtils.copyFile(ebcdicFile, workingFile3);
    File workingHiddenFile = new File("target/converter/sub/sub/.cobol.txt");
    FileUtils.copyFile(ebcdicFile, workingHiddenFile);

    String[] args = new String[] {new File("target/converter").getAbsolutePath(), "Cp1047", "UTF-8"};
    FileConverter.main(args);

    assertThat(FileUtils.contentEquals(workingFile1, expectedOutputFile)).isTrue();
    assertThat(FileUtils.contentEquals(workingFile2, expectedOutputFile)).isTrue();
    assertThat(FileUtils.contentEquals(workingFile3, expectedOutputFile)).isTrue();
    // assertThat(FileUtils.contentEquals(workingFile3, workingHiddenFile), is(false)); TODO : this is not an hidden file on Windows
  }

  @Test
  public void shouldThrowAnExceptionWhenDirectoryDoesntExist() {
    thrown.expect(EbcdicToAsciiConverterException.class);
    converter.convertAllEbcdicFileIn(new File("unknown"));
  }

  @Test
  public void shouldThrowAnExceptionWhenDirectoryIsFile() throws Exception {
    thrown.expect(EbcdicToAsciiConverterException.class);
    converter.convertAllEbcdicFileIn(new File(FileConverterTest.class.getResource("/EBCDIC.txt").toURI()));
  }

  @Test
  public void shouldDecreaseArraySize() {
    int[] newArray = converter.resizeArray(new int[] {1, 2, 3}, 2);
    assertThat(newArray).hasSize(2).isEqualTo(new int[] {1, 2});
  }

  @Test
  public void shouldIncreaseArraySize() {
    int[] newArray = converter.resizeArray(new int[] {1, 2, 3}, 4);
    assertThat(newArray).hasSize(4).isEqualTo(new int[] {1, 2, 3, 0});
  }
}
