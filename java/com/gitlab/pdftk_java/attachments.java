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

import com.gitlab.pdftk_java.com.lowagie.text.Rectangle;
import com.gitlab.pdftk_java.com.lowagie.text.pdf.PRStream;
import com.gitlab.pdftk_java.com.lowagie.text.pdf.PdfAnnotation;
import com.gitlab.pdftk_java.com.lowagie.text.pdf.PdfArray;
import com.gitlab.pdftk_java.com.lowagie.text.pdf.PdfDictionary;
import com.gitlab.pdftk_java.com.lowagie.text.pdf.PdfFileSpecification;
import com.gitlab.pdftk_java.com.lowagie.text.pdf.PdfIndirectReference;
import com.gitlab.pdftk_java.com.lowagie.text.pdf.PdfName;
import com.gitlab.pdftk_java.com.lowagie.text.pdf.PdfNameTree;
import com.gitlab.pdftk_java.com.lowagie.text.pdf.PdfNumber;
import com.gitlab.pdftk_java.com.lowagie.text.pdf.PdfObject;
import com.gitlab.pdftk_java.com.lowagie.text.pdf.PdfReader;
import com.gitlab.pdftk_java.com.lowagie.text.pdf.PdfString;
import com.gitlab.pdftk_java.com.lowagie.text.pdf.PdfWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class attachments {

  TK_Session session;
  String m_output_filename;
  ArrayList<String> m_input_attach_file_filename;
  int m_input_attach_file_pagenum;
  String m_input_attach_file_relation;
  boolean m_ask_about_warnings_b;

  attachments(TK_Session session) {
    this.session = session;
    m_output_filename = session.m_output_filename;
    m_input_attach_file_filename = session.m_input_attach_file_filename;
    m_input_attach_file_pagenum = session.m_input_attach_file_pagenum;
    m_input_attach_file_relation = session.m_input_attach_file_relation;
    m_ask_about_warnings_b = session.m_ask_about_warnings_b;
  }

  static String drop_path(String ss) {
    int loc = ss.lastIndexOf(File.separatorChar);
    if (loc != -1 && loc != ss.length() - 1) {
      return ss.substring(loc + 1);
    }
    return ss;
  }

  static String normalize_pathname(String output_pathname) {
    if (output_pathname == "PROMPT") {
      output_pathname =
          pdftk.prompt_for_filename(
              "Please enter the directory where you want attachments unpacked:");
    }
    if (output_pathname.lastIndexOf(File.separatorChar) == output_pathname.length() - 1) {
      return output_pathname;
    } else { // add delim to end
      return output_pathname + File.separatorChar;
    }
  }

  static void unpack_file(
      PdfReader input_reader_p,
      PdfDictionary filespec_p,
      String output_pathname,
      boolean ask_about_warnings_b) {
    PdfObject type_p = input_reader_p.getPdfObject(filespec_p.get(PdfName.TYPE));
    if (type_p != null && (type_p.equals(PdfName.FILESPEC) || type_p.equals(PdfName.F))) {
      PdfObject ef_p = input_reader_p.getPdfObject(filespec_p.get(PdfName.EF));
      if (ef_p != null && ef_p.isDictionary()) {

        // UF introduced in PDF 1.7
        PdfObject fn_p = input_reader_p.getPdfObject(filespec_p.get(PdfName.UF));
        if (fn_p == null) { // try the F key
          fn_p = input_reader_p.getPdfObject(filespec_p.get(PdfName.F));
        }

        if (fn_p != null && fn_p.isString()) {

          // patch by Johann Felix Soden <johfel@gmx.de>
          // patch tweaked by Sid Steward:
          // toString() doesn't ensure conversion from internal encoding (e.g., Y+diaeresis)
          String fn_str = ((PdfString) fn_p).toUnicodeString();
          String fn = drop_path(fn_str);

          // did the user supply a path?
          if (!output_pathname.isEmpty()) { // prepend it
            fn = output_pathname + fn; // output_pathname has been normalized, already
          }

          // assuming that F key is used to store the data, and not DOS, Mac, or Unix
          PdfObject f_p = input_reader_p.getPdfObject(((PdfDictionary) ef_p).get(PdfName.F));
          if (f_p != null && f_p.isStream()) {

            try {
              byte[] bytes_p = input_reader_p.getStreamBytes((PRStream) f_p);

              if (ask_about_warnings_b) {
                // test for existing file by this name
                if (pdftk.file_exists(fn)) {
                  if (!pdftk.confirm_overwrite(fn)) {
                    System.out.println("   Skipping: " + fn);
                    return; // <--- return
                  }
                }
              }
              FileOutputStream ofs = new FileOutputStream(fn);
              ofs.write(bytes_p);
              ofs.close();
            } catch (IOException e) { // error
              System.err.println("Error: unable to create the file:");
              System.err.println("   " + fn);
              System.err.println("   Skipping.");
            }
          }
        }
      }
    }
  }

  void attach_files(PdfReader input_reader_p, PdfWriter writer_p) throws IOException {
    if (!m_input_attach_file_filename.isEmpty()) {

      if (m_input_attach_file_pagenum == -1) { // our signal to prompt the user for a pagenum
        System.out.println("Please enter the page number you want to attach these files to.");
        System.out.println("   The first page is 1.  The final page is \"end\".");
        System.out.println("   To attach files at the document level, just press Enter.");

        Scanner s = new Scanner(System.in);
        String buff = s.nextLine();
        if (buff.isEmpty()) { // attach to document
          m_input_attach_file_pagenum = 0;
        }
        if (buff.equals("end")) { // the final page
          m_input_attach_file_pagenum = input_reader_p.getNumberOfPages();
        } else {
          Pattern p = Pattern.compile("([0-9]*).*");
          Matcher m = p.matcher(buff);
          m.matches();
          try {
            m_input_attach_file_pagenum = Integer.valueOf(m.group(1));
          } catch (NumberFormatException e) {
            m_input_attach_file_pagenum = 0;
          }
        }
      } else if (m_input_attach_file_pagenum == -2) { // the final page ("end")
        m_input_attach_file_pagenum = input_reader_p.getNumberOfPages();
      }

      if (m_input_attach_file_pagenum != 0) { // attach to a page using annotations
        final int trans = 27;
        final int margin = 18;

        if (0 < m_input_attach_file_pagenum
            && m_input_attach_file_pagenum <= input_reader_p.getNumberOfPages()) {

          PdfDictionary page_p = input_reader_p.getPageN(m_input_attach_file_pagenum);
          if (page_p != null && page_p.isDictionary()) {

            Rectangle crop_box_p = input_reader_p.getCropBox(m_input_attach_file_pagenum);
            float corner_top = crop_box_p.top() - margin;
            float corner_left = crop_box_p.left() + margin;

            PdfObject annots_po = input_reader_p.getPdfObject(page_p.get(PdfName.ANNOTS));
            boolean annots_new_b = false;
            if (annots_po == null) { // create Annots array
              annots_po = new PdfArray();
              annots_new_b = true;
            }
            if (annots_po.isArray()) {
              // grab corner_top and corner_left from the bottom right of the newest annot
              PdfArray annots_p = (PdfArray) annots_po;
              ArrayList<PdfObject> annots_array_p = annots_p.getArrayList();
              for (PdfObject ii : annots_array_p) {
                PdfObject annot_p = input_reader_p.getPdfObject(ii);
                if (annot_p != null && annot_p.isDictionary()) {
                  PdfObject annot_bbox_p =
                      input_reader_p.getPdfObject(((PdfDictionary) annot_p).get(PdfName.RECT));
                  if (annot_bbox_p != null && annot_bbox_p.isArray()) {
                    ArrayList<PdfObject> bbox_array_p = ((PdfArray) annot_bbox_p).getArrayList();
                    if (bbox_array_p.size() == 4) {
                      corner_top = ((PdfNumber) bbox_array_p.get(1)).floatValue();
                      corner_left = ((PdfNumber) bbox_array_p.get(2)).floatValue();
                    }
                  }
                }
              }
              for (String vit : m_input_attach_file_filename) {
                if (vit.equals("PROMPT")) {
                  vit = pdftk.prompt_for_filename("Please enter a filename for attachment:");
                }

                String filename = drop_path(vit);

                // wrap our location over page bounds, if needed
                if (crop_box_p.right() < corner_left + trans) {
                  corner_left = crop_box_p.left() + margin;
                }
                if (corner_top - trans < crop_box_p.bottom()) {
                  corner_top = crop_box_p.top() - margin;
                }

                Rectangle annot_bbox_p =
                    new Rectangle(corner_left, corner_top - trans, corner_left + trans, corner_top);

                PdfAnnotation annot_p =
                    PdfAnnotation.createFileAttachment(
                        writer_p,
                        annot_bbox_p,
                        filename, // contents
                        null,
                        vit, // the file path
                        filename); // display name

                PdfIndirectReference ref_p = writer_p.addToBody(annot_p).getIndirectReference();

                annots_p.add(ref_p);

                // advance the location of our annotation
                corner_left += trans;
                corner_top -= trans;
              }
              if (annots_new_b) { // add new Annots array to page dict
                PdfIndirectReference ref_p = writer_p.addToBody(annots_p).getIndirectReference();
                page_p.put(PdfName.ANNOTS, ref_p);
              }
            }
          } else { // error
            System.err.println("Internal Error: unable to get page dictionary");
          }
        } else { // error
          System.err.print("Error: page number " + m_input_attach_file_pagenum);
          System.err.println(" is not present in the input PDF.");
        }
      } else { // attach to document using the EmbeddedFiles name tree
        PdfDictionary catalog_p = input_reader_p.catalog; // to top, Root dict
        if (catalog_p != null && catalog_p.isDictionary()) {

          // the Names dict
          PdfObject names_po = input_reader_p.getPdfObject(catalog_p.get(PdfName.NAMES));
          boolean names_new_b = false;
          PdfObject af_po = input_reader_p.getPdfObject(catalog_p.get(new PdfName("AF")));
          boolean af_new_b = false;
          if (names_po == null) { // create Names dict
            names_po = new PdfDictionary();
            names_new_b = true;
          }
          if (af_po == null) {
            af_po = new PdfArray();
            af_new_b = true;
          }
          if (names_po != null && names_po.isDictionary() && af_po != null && af_po.isArray()) {
            PdfDictionary names_p = (PdfDictionary) names_po;
            PdfArray af_p = (PdfArray) af_po;

            // the EmbeddedFiles name tree (ref. 1.5, sec. 3.8.5), which is a dict at top
            PdfObject emb_files_tree_p =
                input_reader_p.getPdfObject(names_p.get(PdfName.EMBEDDEDFILES));
            HashMap<String, PdfIndirectReference> emb_files_map_p = null;
            boolean emb_files_tree_new_b = false;
            if (emb_files_tree_p != null) { // read current name tree of attachments into a map
              emb_files_map_p = PdfNameTree.readTree((PdfDictionary) emb_files_tree_p);
            } else { // create material
              emb_files_map_p = new HashMap<String, PdfIndirectReference>();
              emb_files_tree_new_b = true;
            }

            ////
            // add matter to name tree

            for (String vit : m_input_attach_file_filename) {
              if (vit.equals("PROMPT")) {
                vit = pdftk.prompt_for_filename("Please enter a filename for attachment:");
              }

              String filename = drop_path(vit);

              PdfFileSpecification filespec_p = null;
              try {
                // create the file spec. from file
                filespec_p =
                    PdfFileSpecification.fileEmbedded(
                        writer_p, vit, // the file path
                        filename, // the display name
                        null);
                filespec_p.put(
                    new PdfName("AFRelationship"), new PdfName(m_input_attach_file_relation));
              } catch (IOException ioe_p) { // file open error
                System.err.println("Error: Failed to open attachment file: ");
                System.err.println("   " + vit);
                System.err.println("   Skipping this file.");
                continue;
              }

              // add file spec. to PDF via indirect ref.
              PdfIndirectReference ref_p = writer_p.addToBody(filespec_p).getIndirectReference();

              // contruct a name, if necessary, to prevent possible key collision on the name tree
              String key_p = vit;
              for (int counter = 1;
                  emb_files_map_p.containsKey(key_p);
                  ++counter) { // append a unique suffix
                key_p = vit + "-" + counter;
              }

              // add file spec. to map
              emb_files_map_p.put(key_p, ref_p);
              af_p.add(ref_p);
            }

            if (!emb_files_map_p.isEmpty()) {
              // create a name tree from map
              PdfDictionary emb_files_tree_new_p = PdfNameTree.writeTree(emb_files_map_p, writer_p);

              if (emb_files_tree_new_b && emb_files_tree_new_p != null) {
                // adding new material
                PdfIndirectReference ref_p =
                    writer_p.addToBody(emb_files_tree_new_p).getIndirectReference();
                names_p.put(PdfName.EMBEDDEDFILES, ref_p);
              } else if (emb_files_tree_p != null && emb_files_tree_new_p != null) {
                // supplementing old material
                ((PdfDictionary) emb_files_tree_p).merge(emb_files_tree_new_p);
              } else { // error
                System.err.println("Internal Error: no valid EmbeddedFiles tree to add to PDF.");
              }

              if (names_new_b) {
                // perform addToBody only after packing new names_p into names_p;
                // use the resulting ref. to pack our new Names dict. into the catalog (Root)
                PdfIndirectReference ref_p = writer_p.addToBody(names_p).getIndirectReference();
                catalog_p.put(PdfName.NAMES, ref_p);
              }
              if (af_new_b) {
                catalog_p.put(new PdfName("AF"), af_p);
              }
            }
          } else { // error
            System.err.println("Internal Error: couldn't read or create PDF Names dictionary.");
          }
        } else { // error
          System.err.println("Internal Error: couldn't read input PDF Root dictionary.");
          System.err.println("   File attachment failed; no new files attached to output.");
        }
      }
    }
  }

  void unpack_files(PdfReader input_reader_p) {
    // output pathname; PROMPT if necessary
    String output_pathname = normalize_pathname(m_output_filename);

    { // unpack document attachments
      PdfDictionary catalog_p = input_reader_p.catalog; // to top, Root dict
      if (catalog_p != null && catalog_p.isDictionary()) {

        // the Names dict
        PdfObject names_p = input_reader_p.getPdfObject(catalog_p.get(PdfName.NAMES));
        if (names_p != null && names_p.isDictionary()) {

          // the EmbeddedFiles name tree (ref. 1.5, sec. 3.8.5), which is a dict at top
          PdfObject emb_files_tree_p =
              input_reader_p.getPdfObject(((PdfDictionary) names_p).get(PdfName.EMBEDDEDFILES));
          HashMap<Object, PdfObject> emb_files_map_p = null;
          if (emb_files_tree_p != null && emb_files_tree_p.isDictionary()) {
            // read current name tree of attachments into a map
            emb_files_map_p = PdfNameTree.readTree((PdfDictionary) emb_files_tree_p);

            for (PdfObject value_p : emb_files_map_p.values()) {
              PdfObject filespec_p = input_reader_p.getPdfObject(value_p);
              if (filespec_p != null && filespec_p.isDictionary()) {

                unpack_file(
                    input_reader_p,
                    (PdfDictionary) filespec_p,
                    output_pathname,
                    m_ask_about_warnings_b);
              }
            }
          }
        }
      }
    }

    { // unpack page attachments
      int num_pages = input_reader_p.getNumberOfPages();
      for (int ii = 1; ii <= num_pages; ++ii) { // 1-based page ref.s

        PdfDictionary page_p = input_reader_p.getPageN(ii);
        if (page_p != null && page_p.isDictionary()) {

          PdfObject annots_p = input_reader_p.getPdfObject(page_p.get(PdfName.ANNOTS));
          if (annots_p != null && annots_p.isArray()) {

            ArrayList<PdfObject> annots_array_p = ((PdfArray) annots_p).getArrayList();
            for (PdfObject jj : annots_array_p) {
              PdfObject annot_po = input_reader_p.getPdfObject(jj);
              if (annot_po != null && annot_po.isDictionary()) {
                PdfDictionary annot_p = (PdfDictionary) annot_po;

                PdfObject subtype_p = input_reader_p.getPdfObject(annot_p.get(PdfName.SUBTYPE));
                if (subtype_p != null && subtype_p.equals(PdfName.FILEATTACHMENT)) {

                  PdfObject filespec_p = input_reader_p.getPdfObject(annot_p.get(PdfName.FS));
                  if (filespec_p != null && filespec_p.isDictionary()) {

                    unpack_file(
                        input_reader_p,
                        (PdfDictionary) filespec_p,
                        output_pathname,
                        m_ask_about_warnings_b);
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}
