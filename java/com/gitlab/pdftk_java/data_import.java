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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;
import org.apache.commons.lang3.StringEscapeUtils;
import pdftk.com.lowagie.text.Rectangle;
import pdftk.com.lowagie.text.pdf.PRIndirectReference;
import pdftk.com.lowagie.text.pdf.PdfArray;
import pdftk.com.lowagie.text.pdf.PdfDictionary;
import pdftk.com.lowagie.text.pdf.PdfName;
import pdftk.com.lowagie.text.pdf.PdfNumber;
import pdftk.com.lowagie.text.pdf.PdfObject;
import pdftk.com.lowagie.text.pdf.PdfReader;
import pdftk.com.lowagie.text.pdf.PdfString;

class data_import {

  static PdfData LoadDataFile(InputStream input_stream) {
    PdfData pdf_data_p = new PdfData();
    Scanner ifs = new Scanner(input_stream);

    String buff = "";

    String buff_prev = "";
    int buff_prev_len = 0;

    PdfInfo info = new PdfInfo();
    boolean info_b = false;

    PdfBookmark bookmark = new PdfBookmark();
    boolean bookmark_b = false;

    PdfPageLabel pagelabel = new PdfPageLabel();
    boolean pagelabel_b = false;

    PdfPageMedia pagemedia = new PdfPageMedia();
    boolean pagemedia_b = false;

    boolean eof = !ifs.hasNextLine();

    while (!eof) {
      eof = !ifs.hasNextLine();
      if (eof) buff = "";
      else buff = ifs.nextLine();

      if (eof
          || buff.startsWith(PdfInfo.BEGIN_MARK)
          || buff.startsWith(PdfBookmark.BEGIN_MARK)
          || buff.startsWith(PdfPageLabel.BEGIN_MARK)
          || buff.startsWith(PdfPageMedia.BEGIN_MARK)
          || !buff_prev.isEmpty()
              && !buff.startsWith(buff_prev)) { // start of a new record or end of file
        // pack data and reset

        if (info_b) {
          if (info.valid()) {
            pdf_data_p.m_info.add(info);
          } else { // warning
            System.err.println("pdftk Warning: data info record not valid -- skipped; data:");
            System.err.print(info);
          }
        } else if (bookmark_b) {
          if (bookmark.valid()) {
            pdf_data_p.m_bookmarks.add(bookmark);
          } else { // warning
            System.err.println("pdftk Warning: data bookmark record not valid -- skipped; data:");
            System.err.print(bookmark);
          }
        } else if (pagelabel_b) {
          if (pagelabel.valid()) {
            pdf_data_p.m_pagelabels.add(pagelabel);
          } else { // warning
            System.err.println("pdftk Warning: page label record not valid -- skipped; data:");
            System.err.print(pagelabel);
          }
        } else if (pagemedia_b) {
          if (pagemedia.valid()) {
            pdf_data_p.m_pagemedia.add(pagemedia);
          } else { // warning
            System.err.println("pdftk Warning: page media record not valid -- skipped; data:");
            System.err.print(pagemedia);
          }
        }

        // reset
        buff_prev = "";
        buff_prev_len = 0;
        //
        info = new PdfInfo();
        info_b = false;
        //
        bookmark = new PdfBookmark();
        bookmark_b = false;
        //
        pagelabel = new PdfPageLabel();
        pagelabel_b = false;
        //
        pagemedia = new PdfPageMedia();
        pagemedia_b = false;
      }

      // whitespace or comment; skip
      if (buff.isEmpty() || buff.startsWith("#")) {
        continue;
      }

      // info record
      else if (buff.startsWith(PdfInfo.PREFIX)) {
        buff_prev_len = PdfInfo.PREFIX.length();
        info_b = true;

        if (buff.startsWith(PdfInfo.BEGIN_MARK) || info.loadKey(buff) || info.loadValue(buff)) {
          // success
        } else { // warning
          System.err.println("pdftk Warning: unexpected Info case in LoadDataFile(); continuing");
        }
      }

      // bookmark record
      else if (buff.startsWith(PdfBookmark.PREFIX)) {
        buff_prev_len = PdfBookmark.PREFIX.length();
        bookmark_b = true;

        if (buff.startsWith(PdfBookmark.BEGIN_MARK)
            || bookmark.loadTitle(buff)
            || bookmark.loadLevel(buff)
            || bookmark.loadPageNum(buff)) {
          // success
        } else { // warning
          System.err.println(
              "pdftk Warning: unexpected Bookmark case in LoadDataFile(); continuing");
        }
      }

      // page label record
      else if (buff.startsWith(PdfPageLabel.PREFIX)) {
        buff_prev_len = PdfPageLabel.PREFIX.length();
        pagelabel_b = true;

        if (buff.startsWith(PdfPageLabel.BEGIN_MARK)
            || pagelabel.loadNewIndex(buff)
            || pagelabel.loadStart(buff)
            || pagelabel.loadPrefix(buff)
            || pagelabel.loadNumStyle(buff)) {
          // success
        } else { // warning
          System.err.println(
              "pdftk Warning: unexpected PageLabel case in LoadDataFile(); continuing");
        }
      }

      // page media record
      else if (buff.startsWith(PdfPageMedia.PREFIX)) {
        buff_prev_len = PdfPageMedia.PREFIX.length();
        pagemedia_b = true;

        if (buff.startsWith(PdfPageMedia.BEGIN_MARK)
            || pagemedia.loadNumber(buff)
            || pagemedia.loadRotation(buff)
            || pagemedia.loadRect(buff)
            || pagemedia.loadCrop(buff)) {
          // success
        } else { // warning
          System.err.println(
              "pdftk Warning: unexpected PageMedia case in LoadDataFile(); continuing");
        }
      }

      // pdf id
      else if (buff.startsWith(PdfData.PREFIX)) {
        buff_prev_len = 0; // not a record

        if (pdf_data_p.loadID0(buff) || pdf_data_p.loadID1(buff)) {
          // success
        } else { // warning
          System.err.println("pdftk Warning: unexpected PdfID case in LoadDataFile(); continuing");
        }
      }

      // number of pages
      else if (pdf_data_p.loadNumPages(buff)) {
        buff_prev_len = 0; // not a record
      } else { // warning
        System.err.println("pdftk Warning: unexpected case 1 in LoadDataFile(); continuing");
      }

      buff_prev = buff.substring(0, buff_prev_len);
    }

    if (buff_prev_len != 0) { // warning; some incomplete record hasn't been packed
      System.err.println("pdftk Warning in LoadDataFile(): incomplete record;");
    }

    return pdf_data_p;
  }

  static PdfArray toPdfArray(Rectangle r) {
    if (r == null) return new PdfArray();
    return new PdfArray(new float[] {r.left(), r.bottom(), r.right(), r.top()});
  }

  static boolean UpdateInfo(PdfReader reader_p, InputStream ifs, boolean utf8_b) {
    boolean ret_val_b = true;

    PdfData pdf_data = LoadDataFile(ifs);
    if (pdf_data == null) { // error
      System.err.println("pdftk Error in UpdateInfo(): LoadDataFile() failure;");
      return false;
    }

    // trailer data
    PdfDictionary trailer_p = reader_p.getTrailer();
    if (trailer_p != null) {

      PdfObject root_po = reader_p.getPdfObject(trailer_p.get(PdfName.ROOT));
      if (root_po != null && root_po.isDictionary()) {
        PdfDictionary root_p = (PdfDictionary) root_po;

        // bookmarks
        if (!pdf_data.m_bookmarks.isEmpty()) {

          // build bookmarks
          PdfDictionary outlines_p = new PdfDictionary(PdfName.OUTLINES);
          if (outlines_p != null) {
            PRIndirectReference outlines_ref_p = reader_p.getPRIndirectReference(outlines_p);

            int num_bookmarks_total =
                bookmarks.BuildBookmarks(
                    reader_p,
                    pdf_data.m_bookmarks.listIterator(),
                    outlines_p,
                    outlines_ref_p,
                    0,
                    utf8_b);

            if (root_p.contains(PdfName.OUTLINES)) {
              // erase old bookmarks
              PdfObject old_outlines_p = reader_p.getPdfObject(root_p.get(PdfName.OUTLINES));
              if (old_outlines_p != null && old_outlines_p.isDictionary()) {
                bookmarks.RemoveBookmarks(reader_p, (PdfDictionary) old_outlines_p);
              }
            }
            // insert into document
            root_p.put(PdfName.OUTLINES, outlines_ref_p);
          }
        }

        // page labels
        if (!pdf_data.m_pagelabels.isEmpty()) {
          PdfDictionary pagelabels_p = PdfPageLabel.BuildPageLabels(pdf_data.m_pagelabels);
          PRIndirectReference pagelabels_ref_p = reader_p.getPRIndirectReference(pagelabels_p);
          root_p.put(PdfName.PAGELABELS, pagelabels_ref_p);
        }

      } else { // error
        System.err.println("pdftk Error in UpdateInfo(): no Root dictionary found;");
        ret_val_b = false;
      }

      // page media
      for (PdfPageMedia pagemedia : pdf_data.m_pagemedia) {
        PdfDictionary page_p = reader_p.getPageN(pagemedia.m_number);
        if (page_p != null) {
          if (pagemedia.m_rotation >= 0)
            page_p.put(PdfName.ROTATE, new PdfNumber(pagemedia.m_rotation));
          if (pagemedia.m_rect != null) page_p.put(PdfName.MEDIABOX, toPdfArray(pagemedia.m_rect));
          if (pagemedia.m_crop != null) page_p.put(PdfName.CROPBOX, toPdfArray(pagemedia.m_crop));
        } else {
          System.err.println(
              "pdftk Error in UpdateInfo(): page " + pagemedia.m_number + " not found;");
          ret_val_b = false;
        }
      }

      // metadata
      if (!pdf_data.m_info.isEmpty()) {
        PdfObject info_po = reader_p.getPdfObject(trailer_p.get(PdfName.INFO));
        if (info_po != null && info_po.isDictionary()) {
          PdfDictionary info_p = (PdfDictionary) info_po;

          for (PdfInfo it : pdf_data.m_info) {
            if (it.m_value.isEmpty()) {
              info_p.remove(new PdfName(it.m_key));
            } else {
              if (utf8_b) { // UTF-8 encoded input
                // patch by Quentin Godfroy <godfroy@clipper.ens.fr>
                // and Chris Adams <cadams@salk.edu>
                info_p.put(new PdfName(it.m_key), new PdfString(it.m_value));
              } else { // XML entities input
                String jvs = XmlStringToJcharArray(it.m_value);
                info_p.put(new PdfName(it.m_key), new PdfString(jvs));
              }
            }
          }
        } else { // error
          System.err.println("pdftk Error in UpdateInfo(): no Info dictionary found;");
          ret_val_b = false;
        }
      }
    } else { // error
      System.err.println("pdftk Error in UpdateInfo(): no document trailer found;");
      ret_val_b = false;
    }

    return ret_val_b;
  }

  //////
  ////
  // created for data import, maybe useful for export, too

  //
  static class PdfInfo {
    static final String PREFIX = "Info";
    static final String BEGIN_MARK = "InfoBegin";
    static final String KEY_LABEL = "InfoKey:";
    static final String VALUE_LABEL = "InfoValue:";

    String m_key = null;
    String m_value = null;

    boolean valid() {
      return (m_key != null && m_value != null);
    }

    public String toString() {
      return BEGIN_MARK
          + System.lineSeparator()
          + KEY_LABEL
          + " "
          + m_key
          + System.lineSeparator()
          + VALUE_LABEL
          + " "
          + m_value
          + System.lineSeparator();
    }

    boolean loadKey(String buff) {
      LoadableString loader = new LoadableString(m_key);
      boolean success = loader.LoadString(buff, KEY_LABEL);
      m_key = loader.ss;
      return success;
    }

    boolean loadValue(String buff) {
      LoadableString loader = new LoadableString(m_value);
      boolean success = loader.LoadString(buff, VALUE_LABEL);
      m_value = loader.ss;
      return success;
    }
  };

  static class PdfData {
    ArrayList<PdfInfo> m_info = new ArrayList<PdfInfo>();
    ArrayList<PdfBookmark> m_bookmarks = new ArrayList<PdfBookmark>();
    ArrayList<PdfPageLabel> m_pagelabels = new ArrayList<PdfPageLabel>();
    ArrayList<PdfPageMedia> m_pagemedia = new ArrayList<PdfPageMedia>();

    static final String PREFIX = "PdfID";
    static final String ID_0_LABEL = "PdfID0:";
    static final String ID_1_LABEL = "PdfID1:";
    static final String NUM_PAGES_LABEL = "NumberOfPages:";

    int m_num_pages = -1;

    String m_id_0 = null;
    String m_id_1 = null;

    public String toString() {
      StringBuilder ss = new StringBuilder();
      for (PdfInfo vit : m_info) {
        ss.append(vit);
      }
      ss.append(ID_0_LABEL + " " + m_id_0 + System.lineSeparator());
      ss.append(ID_1_LABEL + " " + m_id_1 + System.lineSeparator());
      ss.append(NUM_PAGES_LABEL + " " + m_num_pages + System.lineSeparator());
      for (PdfBookmark vit : m_bookmarks) {
        ss.append(vit);
      }
      return ss.toString();
    }

    boolean loadNumPages(String buff) {
      LoadableInt loader = new LoadableInt(m_num_pages);
      boolean success = loader.LoadInt(buff, NUM_PAGES_LABEL);
      m_num_pages = loader.ii;
      return success;
    }

    boolean loadID0(String buff) {
      LoadableString loader = new LoadableString(m_id_0);
      boolean success = loader.LoadString(buff, ID_0_LABEL);
      m_id_0 = loader.ss;
      return success;
    }

    boolean loadID1(String buff) {
      LoadableString loader = new LoadableString(m_id_1);
      boolean success = loader.LoadString(buff, ID_1_LABEL);
      m_id_1 = loader.ss;
      return success;
    }
  };

  static String XmlStringToJcharArray(String jvs) {
    return StringEscapeUtils.unescapeXml(jvs);
  }
};
