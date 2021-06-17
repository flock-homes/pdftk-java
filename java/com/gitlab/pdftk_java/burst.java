/*
 *   This file is part of the pdftk port to java
 *
 *   Copyright (c) Marc Vinyals 2017-2020
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

import com.gitlab.pdftk_java.com.lowagie.text.Document;
import com.gitlab.pdftk_java.com.lowagie.text.DocumentException;
import com.gitlab.pdftk_java.com.lowagie.text.pdf.PdfCopy;
import com.gitlab.pdftk_java.com.lowagie.text.pdf.PdfDictionary;
import com.gitlab.pdftk_java.com.lowagie.text.pdf.PdfImportedPage;
import com.gitlab.pdftk_java.com.lowagie.text.pdf.PdfName;
import com.gitlab.pdftk_java.com.lowagie.text.pdf.PdfObject;
import com.gitlab.pdftk_java.com.lowagie.text.pdf.PdfReader;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.IllegalFormatException;

class burst {

  TK_Session session;
  String m_output_filename;
  InputPdf m_input_pdf;
  boolean m_output_utf8_b;

  burst(TK_Session session) {
    this.session = session;
    m_output_filename = session.m_output_filename;
    // grab the first reader, since there's only one
    m_input_pdf = session.m_input_pdf.get(0);
    m_output_utf8_b = session.m_output_utf8_b;
  }

  // burst input into pages
  ErrorCode create_output_burst() throws DocumentException, IOException {
    ErrorCode ret_val = ErrorCode.NO_ERROR;

    PdfReader input_reader_p = m_input_pdf.m_readers.get(0).second;
    int input_num_pages = m_input_pdf.m_num_pages;

    if (m_output_filename.equals("PROMPT")) {
      m_output_filename =
          pdftk.prompt_for_filename(
              "Please enter a filename pattern for the PDF pages (e.g. pg_%04d.pdf):");
    }
    try {
      String s1 = String.format(m_output_filename, 1);
      String s2 = String.format(m_output_filename, 2);
      if (s1.equals(s2)) {
        m_output_filename += "pg_%04d.pdf";
        String.format(m_output_filename, 1);
      }
    } catch (IllegalFormatException e) {
      System.err.println("Error: Invalid output pattern:");
      System.err.println("   " + m_output_filename);
      return ErrorCode.ERROR;
    }

    // locate the input PDF Info dictionary that holds metadata
    PdfDictionary input_info_p = null;
    {
      PdfDictionary input_trailer_p = input_reader_p.getTrailer();
      if (input_trailer_p != null) {
        PdfObject input_info_po = input_reader_p.getPdfObject(input_trailer_p.get(PdfName.INFO));
        if (input_info_po != null && input_info_po.isDictionary()) {
          // success
          input_info_p = (PdfDictionary) input_info_po;
        }
      }
    }

    for (int ii = 0; ii < input_num_pages; ++ii) {

      // the filename
      String output_filename_p = String.format(m_output_filename, ii + 1);
      OutputStream ofs_p = pdftk.get_output_stream_file(output_filename_p);
      if (ofs_p == null) {
        ret_val = ErrorCode.PARTIAL;
        continue;
      }

      Document output_doc_p = new Document();
      PdfCopy writer_p = new PdfCopy(output_doc_p, ofs_p);

      session.prepare_writer(writer_p);

      output_doc_p.addCreator(TK_Session.creator);

      output_doc_p.open(); // must open writer before copying (possibly) indirect object
      // Call setFromReader() after open(),
      // otherwise topPageParent is not properly set.
      // See https://gitlab.com/pdftk-java/pdftk/issues/18
      writer_p.setFromReader(input_reader_p);

      { // copy the Info dictionary metadata
        if (input_info_p != null) {
          PdfDictionary writer_info_p = writer_p.getInfo();
          if (writer_info_p != null) {
            PdfDictionary info_copy_p = writer_p.copyDictionary(input_info_p);
            if (info_copy_p != null) {
              writer_info_p.putAll(info_copy_p);
            }
          }
        }
        byte[] input_reader_xmp_p = input_reader_p.getMetadata();
        if (input_reader_xmp_p != null) {
          writer_p.setXmpMetadata(input_reader_xmp_p);
        }
      }

      PdfImportedPage page_p = writer_p.getImportedPage(input_reader_p, ii + 1);
      writer_p.addPage(page_p);

      output_doc_p.close();
      writer_p.close();
    }

    ////
    // dump document data

    String doc_data_fn = "doc_data.txt";
    int loc = m_output_filename.lastIndexOf(File.separatorChar);
    if (loc >= 0) {
      doc_data_fn = m_output_filename.substring(0, loc) + File.separatorChar + doc_data_fn;
    }
    try {
      PrintStream ofs = pdftk.get_print_stream(doc_data_fn, m_output_utf8_b);
      report.ReportOnPdf(ofs, input_reader_p, m_output_utf8_b);
    } catch (IOException e) { // error
      System.err.println("Error: unable to open file for output: doc_data.txt");
      ret_val = ErrorCode.PARTIAL;
    }
    return ret_val;
  }
}
