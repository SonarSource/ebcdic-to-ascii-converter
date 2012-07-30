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
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.net.URL;

import static org.fest.assertions.Assertions.assertThat;

public class MainTest {

  @Rule
  public TemporaryFolder tempFolder = new TemporaryFolder();

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void should_convert_all_files_in_directory_and_sub_directories() throws Exception {
    URL ebcdicSource = FileConverterTest.class.getResource("/EBCDIC.txt");
    File expectedOutputFile = new File(FileConverterTest.class.getResource("/ASCII.txt").toURI());

    File source = tempFolder.newFolder();
    File destination = tempFolder.newFolder();
    File inputFile1 = new File(source, "cobol.txt");
    File outputFile1 = new File(destination, "cobol.txt");
    FileUtils.copyURLToFile(ebcdicSource, inputFile1);
    File inputFile2 = new File(source, "sub/cobol.txt");
    File outputFile2 = new File(destination, "sub/cobol.txt");
    FileUtils.copyURLToFile(ebcdicSource, inputFile2);
    File inputFile3 = new File(source, "sub/sub/cobol.txt");
    File outputFile3 = new File(destination, "sub/sub/cobol.txt");
    FileUtils.copyURLToFile(ebcdicSource, inputFile3);

    String[] args = new String[] {"-f", "CP1047", "-t", "UTF-8", "-l", "80", source.getAbsolutePath(), destination.getAbsolutePath()};
    Main.main(args);

    assertThat(FileUtils.contentEquals(outputFile1, expectedOutputFile)).isTrue();
    assertThat(FileUtils.contentEquals(outputFile1, inputFile1)).isFalse();
    assertThat(FileUtils.contentEquals(outputFile2, expectedOutputFile)).isTrue();
    assertThat(FileUtils.contentEquals(outputFile2, inputFile2)).isFalse();
    assertThat(FileUtils.contentEquals(outputFile3, expectedOutputFile)).isTrue();
    assertThat(FileUtils.contentEquals(outputFile2, inputFile2)).isFalse();
  }

}
