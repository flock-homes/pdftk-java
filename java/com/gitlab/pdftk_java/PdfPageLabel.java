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

import java.util.ArrayList;
import java.util.HashMap;
import pdftk.com.lowagie.text.pdf.PdfDictionary;
import pdftk.com.lowagie.text.pdf.PdfName;
import pdftk.com.lowagie.text.pdf.PdfPageLabels;

class PdfPageLabel {
  static final String PREFIX = "PageLabel";
  static final String BEGIN_MARK = "PageLabelBegin";
  static final String NEW_INDEX_LABEL = "PageLabelNewIndex:";
  static final String START_LABEL = "PageLabelStart:";
  static final String PREFIX_LABEL = "PageLabelPrefix:";
  static final String NUM_STYLE_LABEL = "PageLabelNumStyle:";

  enum NumberingStyle {
    DECIMAL_ARABIC_NUMERALS(
        PdfPageLabels.DECIMAL_ARABIC_NUMERALS, "DecimalArabicNumerals", PdfName.D),
    UPPERCASE_ROMAN_NUMERALS(
        PdfPageLabels.UPPERCASE_ROMAN_NUMERALS, "UppercaseRomanNumerals", PdfName.R),
    LOWERCASE_ROMAN_NUMERALS(
        PdfPageLabels.LOWERCASE_ROMAN_NUMERALS, "LowercaseRomanNumerals", new PdfName("r")),
    UPPERCASE_LETTERS(PdfPageLabels.UPPERCASE_LETTERS, "UppercaseLetters", PdfName.A),
    LOWERCASE_LETTERS(PdfPageLabels.LOWERCASE_LETTERS, "LowercaseLetters", new PdfName("a")),
    EMPTY(PdfPageLabels.EMPTY, "NoNumber", null),
    ERROR(-1, "[PDFTK ERROR]", null);
    final int value;
    final String representation;
    final PdfName tag;
    static final HashMap<String, NumberingStyle> fromString = new HashMap<String, NumberingStyle>();
    static final HashMap<PdfName, NumberingStyle> fromPdfName =
        new HashMap<PdfName, NumberingStyle>();

    static {
      for (NumberingStyle style : NumberingStyle.values()) {
        fromString.put(style.representation, style);
        fromPdfName.put(style.tag, style);
      }
    }

    NumberingStyle(int value, String representation, PdfName tag) {
      this.value = value;
      this.representation = representation;
      this.tag = tag;
    }
  }

  int m_new_index = -1;
  int m_start = -1;
  String m_prefix = null;
  String m_num_style = null;

  boolean valid() {
    return m_new_index > 0
        && m_start > 0
        && m_num_style != null
        && NumberingStyle.fromString.containsKey(m_num_style);
  }

  public String toString() {
    StringBuilder ss = new StringBuilder();
    ss.append(BEGIN_MARK + System.lineSeparator());
    ss.append(NEW_INDEX_LABEL + " " + m_new_index + System.lineSeparator());
    ss.append(START_LABEL + " " + m_start + System.lineSeparator());
    if (m_prefix != null) {
      ss.append(PREFIX_LABEL + " " + m_prefix + System.lineSeparator());
    }
    ss.append(NUM_STYLE_LABEL + " " + m_num_style + System.lineSeparator());
    return ss.toString();
  }

  static PdfDictionary BuildPageLabels(ArrayList<PdfPageLabel> pagelabels_data) {
    PdfPageLabels pagelabels = new PdfPageLabels();
    for (PdfPageLabel pagelabel : pagelabels_data) {
      int num_style = NumberingStyle.fromString.get(pagelabel.m_num_style).value;
      pagelabels.addPageLabel(
          pagelabel.m_new_index, num_style, pagelabel.m_prefix, pagelabel.m_start);
    }
    return pagelabels.getDictionary();
  }

  boolean loadPrefix(String buff) {
    LoadableString loader = new LoadableString(m_prefix);
    boolean success = loader.LoadString(buff, PREFIX_LABEL);
    m_prefix = loader.ss;
    return success;
  }

  boolean loadNumStyle(String buff) {
    LoadableString loader = new LoadableString(m_num_style);
    boolean success = loader.LoadEnum(buff, NUM_STYLE_LABEL, NumberingStyle.fromString.keySet());
    if (success) m_num_style = loader.ss;
    return success;
  }

  boolean loadNewIndex(String buff) {
    LoadableInt loader = new LoadableInt(m_new_index);
    boolean success = loader.LoadInt(buff, NEW_INDEX_LABEL);
    m_new_index = loader.ii;
    return success;
  }

  boolean loadStart(String buff) {
    LoadableInt loader = new LoadableInt(m_start);
    boolean success = loader.LoadInt(buff, START_LABEL);
    m_start = loader.ii;
    return success;
  }
};
