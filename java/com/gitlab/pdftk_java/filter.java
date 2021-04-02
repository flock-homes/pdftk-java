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

import java.awt.geom.AffineTransform;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import pdftk.com.lowagie.text.DocumentException;
import pdftk.com.lowagie.text.Rectangle;
import pdftk.com.lowagie.text.pdf.AcroFields;
import pdftk.com.lowagie.text.pdf.FdfReader;
import pdftk.com.lowagie.text.pdf.PdfAnnotation;
import pdftk.com.lowagie.text.pdf.PdfArray;
import pdftk.com.lowagie.text.pdf.PdfBoolean;
import pdftk.com.lowagie.text.pdf.PdfContentByte;
import pdftk.com.lowagie.text.pdf.PdfDictionary;
import pdftk.com.lowagie.text.pdf.PdfImportedPage;
import pdftk.com.lowagie.text.pdf.PdfName;
import pdftk.com.lowagie.text.pdf.PdfNumber;
import pdftk.com.lowagie.text.pdf.PdfObject;
import pdftk.com.lowagie.text.pdf.PdfReader;
import pdftk.com.lowagie.text.pdf.PdfRectangle;
import pdftk.com.lowagie.text.pdf.PdfStamperImp;
import pdftk.com.lowagie.text.pdf.XfdfReader;

class filter {

  TK_Session session;
  String m_output_filename;
  ArrayList<InputPdf> m_input_pdf;
  ArrayList<ArrayList<PageRef>> m_page_seq;
  String m_form_data_filename;
  String m_background_filename;
  String m_stamp_filename;
  String m_update_info_filename;
  boolean m_output_need_appearances_b;

  filter(TK_Session session) {
    this.session = session;
    m_output_filename = session.m_output_filename;
    m_input_pdf = session.m_input_pdf;
    m_page_seq = session.m_page_seq;
    m_form_data_filename = session.m_form_data_filename;
    m_background_filename = session.m_background_filename;
    m_stamp_filename = session.m_stamp_filename;
    m_update_info_filename = session.m_update_info_filename;
    m_output_need_appearances_b = session.m_output_need_appearances_b;
  }

  ////
  // when uncompressing a PDF, we add this marker to every page,
  // so the PDF is easier to navigate; when compressing a PDF,
  // we remove this marker

  static final String g_page_marker = "pdftk_PageNum";

  static void add_mark_to_page(PdfReader reader_p, int page_index, int page_num) {
    PdfName page_marker_p = new PdfName(g_page_marker);
    PdfDictionary page_p = reader_p.getPageN(page_index);
    if (page_p != null && page_p.isDictionary()) {
      page_p.put(page_marker_p, new PdfNumber(page_num));
    }
  }

  static void add_marks_to_pages(PdfReader reader_p) {
    int num_pages = reader_p.getNumberOfPages();
    for (int ii = 1; ii <= num_pages; ++ii) { // 1-based page ref.s
      add_mark_to_page(reader_p, ii, ii);
    }
  }

  static void remove_mark_from_page(PdfReader reader_p, int page_num) {
    PdfName page_marker_p = new PdfName(g_page_marker);
    PdfDictionary page_p = reader_p.getPageN(page_num);
    if (page_p != null && page_p.isDictionary()) {
      page_p.remove(page_marker_p);
    }
  }

  static void remove_marks_from_pages(PdfReader reader_p) {
    int num_pages = reader_p.getNumberOfPages();
    for (int ii = 1; ii <= num_pages; ++ii) { // 1-based page ref.s
      remove_mark_from_page(reader_p, ii);
    }
  }

  // apply operations to given PDF file
  ErrorCode create_output_filter() throws DocumentException, IOException {
    ErrorCode ret_val = ErrorCode.NO_ERROR;

    // we should have been given only a single, input file
    if (1 < m_input_pdf.size()) { // error
      System.err.println("Error: Only one input PDF file may be given for this");
      System.err.println("   operation.  Maybe you meant to use the \"cat\" operator?");
      return ErrorCode.ERROR;
    }

    // try opening the FDF file before we get too involved;
    // if input is stdin ("-"), don't pass it to both the FDF and XFDF readers
    FdfReader fdf_reader_p = null;
    XfdfReader xfdf_reader_p = null;
    if (m_form_data_filename.equals("PROMPT")) {
      // handle case where user enters '-' or (empty) at the prompt
      m_form_data_filename =
          pdftk.prompt_for_filename("Please enter a filename for the form data:");
    }
    if (!m_form_data_filename.isEmpty()) { // we have form data to process
      if (m_form_data_filename.equals("-")) { // form data on stdin
        // JArray<jbyte>* in_arr= itext::RandomAccessFileOrArray::InputStreamToArray(
        // java::System::in );

        // first try fdf
        try {
          fdf_reader_p = new FdfReader(System.in);
        } catch (IOException ioe_p) { // file open error
          // maybe it's xfdf?
          try {
            xfdf_reader_p = new XfdfReader(System.in);
          } catch (IOException ioe2_p) { // file open error
            System.err.println("Error: Failed read form data on stdin.");
            // ioe_p->printStackTrace(); // debug
            return ErrorCode.ERROR;
          }
        }
      } else { // form data file

        // first try fdf
        try {
          fdf_reader_p = new FdfReader(m_form_data_filename);
        } catch (IOException ioe_p) { // file open error
          // maybe it's xfdf?
          try {
            xfdf_reader_p = new XfdfReader(m_form_data_filename);
          } catch (IOException ioe2_p) { // file open error
            System.err.println("Error: Failed to open form data file: ");
            System.err.println("   " + m_form_data_filename);
            // ioe_p->printStackTrace(); // debug
            return ErrorCode.ERROR;
          }
        }
      }
    }

    // try opening the PDF background or stamp before we get too involved
    PdfReader mark_p = null;
    boolean background_b = true; // set false for stamp
    //
    // background
    if (!m_background_filename.isEmpty()) {
      InputPdf input = new InputPdf();
      input.m_filename = m_background_filename;
      input.m_role = "background";
      input.m_role_determined = "the background";
      InputPdf.PagesReader reader = input.add_reader(false, false);
      if (reader == null) return ErrorCode.ERROR;
      mark_p = reader.second;
    }

    //
    // stamp
    if (mark_p == null && !m_stamp_filename.isEmpty()) {
      background_b = false;
      InputPdf input = new InputPdf();
      input.m_filename = m_stamp_filename;
      input.m_role = "stamp";
      input.m_role_determined = "the stamp";
      InputPdf.PagesReader reader = input.add_reader(false, false);
      if (reader == null) return ErrorCode.ERROR;
      mark_p = reader.second;
    }

    //
    OutputStream ofs_p = pdftk.get_output_stream(m_output_filename, session.m_ask_about_warnings_b);
    if (ofs_p == null) return ErrorCode.ERROR; // file open error

    //
    PdfReader input_reader_p = m_input_pdf.get(0).m_readers.get(0).second;

    // drop the xfa?
    if (session.m_output_drop_xfa_b) {
      PdfDictionary catalog_p = input_reader_p.catalog;
      if (catalog_p != null && catalog_p.isDictionary()) {

        PdfObject acro_form_p = input_reader_p.getPdfObject(catalog_p.get(PdfName.ACROFORM));
        if (acro_form_p != null && acro_form_p.isDictionary()) {

          ((PdfDictionary) acro_form_p).remove(PdfName.XFA);
        }
      }
    }

    // drop the xmp?
    if (session.m_output_drop_xmp_b) {
      PdfDictionary catalog_p = input_reader_p.catalog;
      if (catalog_p != null) {
        catalog_p.remove(PdfName.METADATA);
      }
    }

    //
    PdfStamperImp writer_p =
        new PdfStamperImp(input_reader_p, ofs_p, '\0', false /* append mode */);

    data_import.HashMapMutable more_info = new data_import.HashMapMutable();

    // update the info?
    if (m_update_info_filename.equals("PROMPT")) {
      m_update_info_filename = pdftk.prompt_for_filename("Please enter an Info file filename:");
    }
    if (!m_update_info_filename.isEmpty()) {
      if (m_update_info_filename.equals("-")) {
        if (!data_import.UpdateInfo(
            input_reader_p, more_info, System.in, session.m_update_info_utf8_b)) {
          System.err.println("Warning: no Info added to output PDF.");
          ret_val = ErrorCode.WARNING;
        }
      } else {
        try {
          FileInputStream ifs = new FileInputStream(m_update_info_filename);
          if (!data_import.UpdateInfo(
              input_reader_p, more_info, ifs, session.m_update_info_utf8_b)) {
            System.err.println("Warning: no Info added to output PDF.");
            ret_val = ErrorCode.WARNING;
          }
        } catch (FileNotFoundException e) { // error
          System.err.println("Error: unable to open FDF file for input: " + m_update_info_filename);
          return ErrorCode.ERROR;
        }
      }
    }

    /*
    // update the xmp?
    if( !m_update_xmp_filename.empty() ) {
      if( rdfcat_available_b ) {
        if( m_update_xmp_filename== "PROMPT" ) {
          prompt_for_filename( "Please enter an Info file filename:",
                               m_update_xmp_filename );
        }
        if( !m_update_xmp_filename.empty() ) {
          UpdateXmp( input_reader_p, m_update_xmp_filename );
        }
      }
      else { // error
        cerr << "Error: to use this feature, you must install the rdfcat program." << endl;
        cerr << "   Perhaps the replace_xmp feature would suit you, instead?" << endl;
        break;
      }
    }
    */

    // rotate pages?
    if (!m_page_seq.isEmpty()) {
      for (ArrayList<PageRef> jt : m_page_seq) {
        for (PageRef kt : jt) {
          session.apply_rotation_to_page(
              input_reader_p, kt.m_page_num, kt.m_page_rot.value, kt.m_page_abs);
        }
      }
    }

    if (session.m_output_uncompress_b) {
      add_marks_to_pages(input_reader_p);
    } else {
      remove_marks_from_pages(input_reader_p);
    }
    session.prepare_writer(writer_p);

    // fill form fields?
    if (fdf_reader_p != null || xfdf_reader_p != null) {
      if (input_reader_p.getAcroForm() != null) { // we really have a form to fill

        AcroFields fields_p = writer_p.getAcroFields();
        fields_p.setGenerateAppearances(true); // have iText create field appearances
        if ((fdf_reader_p != null && fields_p.setFields(fdf_reader_p))
            || (xfdf_reader_p != null
                && fields_p.setFields(xfdf_reader_p))) { // Rich Text input found

          // set the PDF so that Acrobat will create appearances;
          // this might appear contradictory to our setGenerateAppearances( true ) call,
          // above; setting this, here, allows us to keep the generated appearances,
          // in case the PDF is opened somewhere besides Acrobat; yet, Acrobat/Reader
          // will create the Rich Text appearance if it has a chance
          m_output_need_appearances_b = true;
        }
      } else { // warning
        System.err.println("Warning: input PDF is not an acroform, so its fields were not filled.");
        ret_val = ErrorCode.WARNING;
      }
    }

    // flatten form fields?
    writer_p.setFormFlattening(session.m_output_flatten_b);

    // cue viewer to render form field appearances?
    if (m_output_need_appearances_b) {
      PdfDictionary catalog_p = input_reader_p.catalog;
      if (catalog_p != null && catalog_p.isDictionary()) {
        PdfObject acro_form_p = input_reader_p.getPdfObject(catalog_p.get(PdfName.ACROFORM));
        if (acro_form_p != null && acro_form_p.isDictionary()) {
          ((PdfDictionary) acro_form_p).put(PdfName.NEEDAPPEARANCES, PdfBoolean.PDFTRUE);
        }
      }
    }

    // add background/watermark?
    if (mark_p != null) {

      int mark_num_pages = 1; // default: use only the first page of mark
      if (session.m_multistamp_b || session.m_multibackground_b) { // use all pages of mark
        mark_num_pages = mark_p.getNumberOfPages();
      }

      // the mark information; initialized inside loop
      PdfImportedPage mark_page_p = null;
      Rectangle mark_page_size_p = null;
      int mark_page_rotation = 0;
      ArrayList<PdfDictionary> mark_annots = new ArrayList();

      // iterate over document's pages, adding mark_page as
      // a layer above (stamp) or below (watermark) the page content;
      // scale mark_page and move it so it fits within the document's page;
      //
      int num_pages = input_reader_p.getNumberOfPages();
      for (int ii = 0; ii < num_pages; ) {
        ++ii; // page refs are 1-based, not 0-based

        // the mark page and its geometry
        if (ii <= mark_num_pages) {
          mark_page_size_p = mark_p.getCropBox(ii);
          mark_page_rotation = mark_p.getPageRotation(ii);
          for (int mm = 0; mm < mark_page_rotation; mm += 90) {
            mark_page_size_p = mark_page_size_p.rotate();
          }

          // create a PdfTemplate from the first page of mark
          // (PdfImportedPage is derived from PdfTemplate)
          mark_page_p = writer_p.getImportedPage(mark_p, ii);

          PdfDictionary mark_full_page_p = mark_p.getPageN(ii);
          mark_annots = report.getAnnots(mark_p, mark_full_page_p);
        }

        // the target page geometry
        Rectangle doc_page_size_p = input_reader_p.getCropBox(ii);
        int doc_page_rotation = input_reader_p.getPageRotation(ii);
        for (int mm = 0; mm < doc_page_rotation; mm += 90) {
          doc_page_size_p = doc_page_size_p.rotate();
        }

        float h_scale = doc_page_size_p.width() / mark_page_size_p.width();
        float v_scale = doc_page_size_p.height() / mark_page_size_p.height();
        float mark_scale = (h_scale < v_scale) ? h_scale : v_scale;

        float h_trans =
            (float)
                (doc_page_size_p.left()
                    - mark_page_size_p.left() * mark_scale
                    + (doc_page_size_p.width() - mark_page_size_p.width() * mark_scale) / 2.0);
        float v_trans =
            (float)
                (doc_page_size_p.bottom()
                    - mark_page_size_p.bottom() * mark_scale
                    + (doc_page_size_p.height() - mark_page_size_p.height() * mark_scale) / 2.0);

        PdfContentByte content_byte_p =
            (background_b) ? writer_p.getUnderContent(ii) : writer_p.getOverContent(ii);

        float[] trans = null;
        if (mark_page_rotation == 0) {
          trans = new float[] {mark_scale, 0, 0, mark_scale, h_trans, v_trans};
        } else if (mark_page_rotation == 90) {
          trans =
              new float[] {
                0,
                -1 * mark_scale,
                mark_scale,
                0,
                h_trans,
                v_trans + mark_page_size_p.height() * mark_scale
              };
        } else if (mark_page_rotation == 180) {
          trans =
              new float[] {
                -1 * mark_scale,
                0,
                0,
                -1 * mark_scale,
                h_trans + mark_page_size_p.width() * mark_scale,
                v_trans + mark_page_size_p.height() * mark_scale
              };
        } else if (mark_page_rotation == 270) {
          trans =
              new float[] {
                0,
                mark_scale,
                -1 * mark_scale,
                0,
                h_trans + mark_page_size_p.width() * mark_scale,
                v_trans
              };
        }
        if (trans != null) {
          content_byte_p.addTemplate(
              mark_page_p, trans[0], trans[1], trans[2], trans[3], trans[4], trans[5]);
        }

        for (PdfDictionary annot : mark_annots) {
          PdfAnnotation new_annot = new PdfAnnotation(writer_p, null);
          new_annot.putAll(annot);
          PdfArray rect = (PdfArray) annot.get(PdfName.RECT);
          float[] rect_f = new float[4];
          for (int i = 0; i < 4; ++i) rect_f[i] = rect.getAsNumber(i).floatValue();
          AffineTransform M = new AffineTransform(trans);
          float[] new_rect_f = new float[4];
          M.transform(rect_f, 0, new_rect_f, 0, 2);
          PdfRectangle new_rect =
              new PdfRectangle(new_rect_f[0], new_rect_f[1], new_rect_f[2], new_rect_f[3], 0);
          new_annot.put(PdfName.RECT, new_rect);
          writer_p.addAnnotation(new_annot, ii);
        }
      }
    }

    // attach file to document?
    if (!session.m_input_attach_file_filename.isEmpty()) {
      attachments attachments = new attachments(session);
      attachments.attach_files(input_reader_p, writer_p);
    }

    // performed in add_reader(), but this eliminates objects after e.g. drop_xfa,
    // drop_xmp
    input_reader_p.removeUnusedObjects();

    // done; write output
    writer_p.close(more_info.dict);
    return ret_val;
  }
}
