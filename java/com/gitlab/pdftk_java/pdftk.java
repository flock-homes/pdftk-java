/*
 *   This file is part of the pdftk port to java
 *
 *   Copyright (c) Marc Vinyals 2017-2018
 *
 *   The program is a java port of PDFtk, the PDF Toolkit
 *   Copyright (c) 2003-2013 Steward and Lee, LLC
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 2 of the License, or
 *   (at your option) any later version.
 *
 *   The program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.gitlab.pdftk_java;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class pdftk {

  /* TODO: should read from compiler */
  static final String PDFTK_VER = "3.1.2";
  static final boolean ASK_ABOUT_WARNINGS = false;

  // For compatibility with Java < 9
  static void copyStream(InputStream inputStream, OutputStream outputStream) throws IOException {
    final int bufferSize = 0x2000;
    byte[] buffer = new byte[bufferSize];
    while (true) {
      int readBytes = inputStream.read(buffer, 0, bufferSize);
      if (readBytes < 0) break;
      outputStream.write(buffer, 0, readBytes);
    }
  }

  static byte[] readAllBytes(InputStream inputStream) throws IOException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    copyStream(inputStream, outputStream);
    return outputStream.toByteArray();
  }

  static String prompt_for_password(String pass_name, String pass_app) {
    System.out.println("Please enter the " + pass_name + " password to use on " + pass_app + ".");
    System.out.println("   It can be empty, or have a maximum of 32 characters:");
    Scanner s = new Scanner(System.in);
    String password = s.nextLine();
    if (32 < password.length()) { // too long; trim
      System.out.println("The password you entered was over 32 characters long,");
      System.out.println("   so I am dropping: \"" + password.substring(32) + "\"");
      password = password.substring(0, 32);
    }
    return password;
  }

  static String prompt_for_filename(String message) {
    // input could be multibyte, so try working
    // with bytes instead of formatted input features

    System.out.println(message);

    Scanner s = new Scanner(System.in);
    return s.nextLine();
  }

  static boolean confirm_overwrite(String filename) {
    System.out.println(
        "Warning: the output file: " + filename + " already exists.  Overwrite? (y/n)");
    Scanner s = new Scanner(System.in);
    String buff = s.nextLine();
    return buff.startsWith("y") || buff.startsWith("Y");
  }

  static boolean file_exists(String filename) {
    try {
      FileInputStream fp = new FileInputStream(filename);
      return true;
    } catch (FileNotFoundException e) {
      return false;
    }
  }

  static OutputStream get_output_stream(String output_filename, boolean ask_about_warnings_b) {
    if (output_filename.isEmpty() || output_filename.equals("PROMPT")) {
      output_filename = prompt_for_filename("Please enter a name for the output:");
      // recurse; try again
      return get_output_stream(output_filename, ask_about_warnings_b);
    }
    if (output_filename.equals("-")) { // stdout
      return System.out;
    }

    if (ask_about_warnings_b) {
      // test for existing file by this name
      boolean output_exists_b = false;
      if (file_exists(output_filename)) {
        if (!confirm_overwrite(output_filename)) {
          // recurse; try again
          return get_output_stream("PROMPT", ask_about_warnings_b);
        }
      }
    }

    return get_output_stream_file(output_filename);
  }

  static OutputStream get_output_stream_file(String output_filename) {
    OutputStream os_p = null;
    // attempt to open the stream
    try {
      os_p = new FileOutputStream(output_filename);
    } catch (IOException ioe_p) { // file open error
      System.err.println("Error: Failed to open output file: ");
      System.err.println("   " + output_filename);
      System.err.println("   No output created.");
      os_p = null;
    }
    return os_p;
  }

  static PrintStream get_print_stream(String output_filename, boolean output_utf8_b)
      throws IOException {
    Charset encoding = (output_utf8_b ? StandardCharsets.UTF_8 : StandardCharsets.US_ASCII);
    if (output_filename.isEmpty() || output_filename.equals("-")) {
      return new PrintStream(System.out, true, encoding.name());
    } else {
      return new PrintStream(output_filename, encoding.name());
    }
  }

  public static void main(String[] args) {
    System.exit(main_noexit(args));
  }

  public static int main_noexit(String[] args) {
    boolean help_b = false;
    boolean version_b = false;
    boolean synopsis_b = (args.length == 0);
    ErrorCode ret_val = ErrorCode.NO_ERROR; // default: no error

    for (String argv : args) {
      version_b = version_b || (argv.equals("--version")) || (argv.equals("-version"));
      help_b = help_b || (argv.equals("--help")) || (argv.equals("-help")) || (argv.equals("-h"));
    }

    if (help_b) {
      describe_full();
    } else if (version_b) {
      describe_header();
    } else if (synopsis_b) {
      describe_synopsis();
    } else {
      try {
        TK_Session tk_session = new TK_Session(args);

        tk_session.dump_session_data();

        if (tk_session.is_valid()) {
          // create_output() prints necessary error messages
          ret_val = tk_session.create_output();
        } else { // error
          System.err.println("Done.  Input errors, so no output created.");
          ret_val = ErrorCode.ERROR;
        }
      }
      // per https://bugs.launchpad.net/ubuntu/+source/pdftk/+bug/544636
      catch (java.lang.ClassCastException c_p) {
        String message = c_p.getMessage();
        if (message.indexOf("com.lowagie.text.pdf.PdfDictionary") >= 0
            && message.indexOf("com.lowagie.text.pdf.PRIndirectReference") >= 0) {
          System.err.println("Error: One input PDF seems to not conform to the PDF standard.");
          System.err.println("Perhaps the document information dictionary is a direct object");
          System.err.println("   instead of an indirect reference.");
          System.err.println("Please report this bug to the program which produced the PDF.");
          System.err.println();
        }
        System.err.println("Java Exception:");
        c_p.printStackTrace();
        ret_val = ErrorCode.ERROR;
      } catch (NoClassDefFoundError error) {
        describe_missing_library(error);
        ret_val = ErrorCode.ERROR;
      } catch (java.lang.Throwable t_p) {
        System.err.println("Unhandled Java Exception in main():");
        t_p.printStackTrace();
        ret_val = ErrorCode.BUG;
      }
    }
    if (ret_val == ErrorCode.BUG) {
      describe_bug_report();
    }
    return ret_val.code;
  }

  static void describe_header() {
    System.out.println(
        "pdftk port to java " + PDFTK_VER + " a Handy Tool for Manipulating PDF Documents");
    System.out.println(
        "Copyright (c) 2017-2018 Marc Vinyals - https://gitlab.com/pdftk-java/pdftk");
    System.out.println("Copyright (c) 2003-2013 Steward and Lee, LLC.");
    System.out.println("pdftk includes a modified version of the iText library.");
    System.out.println("Copyright (c) 1999-2009 Bruno Lowagie, Paulo Soares, et al.");
    System.out.println(
        "This is free software; see the source code for copying conditions. There is");
    System.out.println(
        "NO warranty, not even for MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.");
  }

  static void describe_resource(String resource) {
    InputStream inputStream = pdftk.class.getResourceAsStream(resource);
    try {
      copyStream(inputStream, System.out);
    } catch (IOException e) {
      e.printStackTrace();
      describe_bug_report();
    }
  }

  static void describe_synopsis() {
    describe_resource("resources/synopsis.txt");
  }

  static void describe_full() {
    describe_header();
    System.out.println();

    describe_synopsis();
    System.out.println();

    describe_resource("resources/description.txt");
  }

  static void describe_bug_report() {
    System.err.println("There was a problem with pdftk-java. Please report it at");
    System.err.println("https://gitlab.com/pdftk-java/pdftk/issues");
    System.err.println(
        "including the message above, the version of pdftk-java ("
            + PDFTK_VER
            + "), and if possible steps to reproduce the error.");
  }

  static void describe_missing_library(Throwable error) {
    System.err.println("Error: could not load a required library for this operation.");
    System.err.println(error);
    System.err.println("Make sure that bcprov and commons-lang3 are installed and included in the");
    System.err.println("classpath. See also https://gitlab.com/pdftk-java/pdftk/issues/2.");
  }
};
