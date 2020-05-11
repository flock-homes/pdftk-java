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
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import pdftk.com.lowagie.text.exceptions.InvalidPdfException;
import pdftk.com.lowagie.text.pdf.PdfReader;

class InputPdf {
  String m_filename = "";
  String m_password = "";
  boolean m_authorized_b = true;
  byte[] m_buffer = null; // Copy input to memory if reading from stdin.

  // keep track of which pages get output under which readers,
  // because one reader mayn't output the same page twice;
  static class PagesReader {
    HashSet<Integer> first = new HashSet<Integer>();
    PdfReader second;

    PagesReader(PdfReader second) {
      this.second = second;
    }
  };

  ArrayList<PagesReader> m_readers = new ArrayList<PagesReader>();

  int m_num_pages = 0;

  // For compatibility with Java < 9
  private static byte[] readAllBytes(InputStream inputStream) throws IOException {
    final int bufferSize = 0x2000;
    byte[] buffer = new byte[bufferSize];
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    while (true) {
      int readBytes = inputStream.read(buffer, 0, bufferSize);
      if (readBytes < 0) break;
      outputStream.write(buffer, 0, readBytes);
    }
    return outputStream.toByteArray();
  }

  InputPdf.PagesReader add_reader(boolean keep_artifacts_b, boolean ask_about_warnings_b) {
    boolean open_success_b = true;
    InputPdf.PagesReader pr = null;

    // No point on asking for a password after we read all of stdin.
    if (m_filename.equals("-")) ask_about_warnings_b = false;

    try {
      PdfReader reader = null;
      if (m_filename.equals("PROMPT")) {
        m_filename = pdftk.prompt_for_filename("Please enter a filename for an input PDF:");
      }
      if (m_password.isEmpty()) {
        if (m_filename.equals("-")) {
          if (m_buffer == null) m_buffer = readAllBytes(System.in);
          reader = new PdfReader(m_buffer);
        } else {
          reader = new PdfReader(m_filename);
        }
      } else {
        if (m_password.equals("PROMPT")) {
          m_password = pdftk.prompt_for_password("open", "the input PDF:\n   " + m_filename);
        }

        byte[] password =
            passwords.utf8_password_to_pdfdoc(
                m_password, false); // allow user to enter greatest selection of chars

        if (password != null) {
          if (m_filename.equals("-")) {
            if (m_buffer == null) m_buffer = readAllBytes(System.in);
            reader = new PdfReader(m_buffer, password);
          } else {
            reader = new PdfReader(m_filename, password);
          }
        } else { // bad password
          System.err.println("Error: Password used to decrypt input PDF:");
          System.err.println("   " + m_filename);
          System.err.println("   includes invalid characters.");
          return null; // <--- return
        }
      }

      if (reader == null) {
        System.err.println("Error: Unexpected null from open_reader()");
        return null; // <--- return
      }

      if (!keep_artifacts_b) {
        // generally useful operations
        reader.consolidateNamedDestinations();
        reader.removeUnusedObjects();
        // reader->shuffleSubsetNames(); // changes the PDF subset names, but not the PostScript
        // font names
      }

      m_num_pages = reader.getNumberOfPages();

      // keep tally of which pages have been laid claim to in this reader;
      // when creating the final PDF, this tally will be decremented
      pr = new InputPdf.PagesReader(reader);
      m_readers.add(pr);

      m_authorized_b = true; // instead of:  ( !reader->encrypted || reader->ownerPasswordUsed );

      if (open_success_b && reader.encrypted && !reader.ownerPasswordUsed) {
        System.err.println("WARNING: The creator of the input PDF:");
        System.err.println("   " + m_filename);
        System.err.println(
            "   has set an owner password (which is not required to handle this PDF).");
        System.err.println("   You did not supply this password. Please respect any copyright.");
      }

      if (!m_authorized_b) {
        open_success_b = false;
      }
    } catch (InvalidPdfException e) { // file open error
      System.err.println("Error: " + e.getMessage());
      open_success_b = false;
    } catch (IOException ioe_p) { // file open error
      if (ioe_p.getMessage().equals("Bad password")) {
        m_authorized_b = false;
      } else if (ioe_p.getMessage().indexOf("not found as file or resource") != -1) {
        System.err.println("Error: Unable to find file.");
      } else { // unexpected error
        System.err.println("Error: Unexpected Exception in open_reader()");
        ioe_p.printStackTrace(); // debug
      }
      open_success_b = false;
    } catch (Throwable t_p) { // unexpected error
      System.err.println("Error: Unexpected Exception in open_reader()");
      t_p.printStackTrace(); // debug

      open_success_b = false;
    }

    if (!m_authorized_b && ask_about_warnings_b) {
      // prompt for a new password
      System.err.println("The password you supplied for the input PDF:");
      System.err.println("   " + m_filename);
      System.err.println("   did not work.  This PDF is encrypted, and you must supply the");
      System.err.println("   owner or the user password to open it. To quit, enter a blank");
      System.err.println("   password at the next prompt.");

      m_password = pdftk.prompt_for_password("open", "the input PDF:\n   " + m_filename);
      if (!m_password.isEmpty()) { // reset flags try again
        m_authorized_b = true;
        return (add_reader(keep_artifacts_b, ask_about_warnings_b)); // <--- recurse, return
      }
    }

    // report
    if (!open_success_b) { // file open error
      System.err.println("Error: Failed to open PDF file: ");
      System.err.println("   " + m_filename);
      if (!m_authorized_b) {
        System.err.println("   OWNER OR USER PASSWORD REQUIRED, but not given (or incorrect)");
      }
    }

    return open_success_b ? pr : null;
  }
};
