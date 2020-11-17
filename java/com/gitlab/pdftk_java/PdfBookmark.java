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

class PdfBookmark {
  static final String PREFIX = "Bookmark";
  static final String BEGIN_MARK = "BookmarkBegin";
  static final String TITLE_LABEL = "BookmarkTitle:";
  static final String LEVEL_LABEL = "BookmarkLevel:";
  static final String PAGE_NUMBER_LABEL = "BookmarkPageNumber:";

  String m_title = null;
  int m_level = -1;
  int m_page_num = -1; // zero means no destination

  boolean valid() {
    return (0 < m_level && 0 <= m_page_num && m_title != null);
  }

  public String toString() {
    return BEGIN_MARK
        + System.lineSeparator()
        + TITLE_LABEL
        + " "
        + m_title
        + System.lineSeparator()
        + LEVEL_LABEL
        + " "
        + m_level
        + System.lineSeparator()
        + PAGE_NUMBER_LABEL
        + " "
        + m_page_num
        + System.lineSeparator();
  }

  boolean loadTitle(String buff) {
    LoadableString loader = new LoadableString(m_title);
    boolean success = loader.LoadString(buff, TITLE_LABEL);
    m_title = loader.ss;
    return success;
  }

  boolean loadLevel(String buff) {
    LoadableInt loader = new LoadableInt(m_level);
    boolean success = loader.LoadInt(buff, LEVEL_LABEL);
    m_level = loader.ii;
    return success;
  }

  boolean loadPageNum(String buff) {
    LoadableInt loader = new LoadableInt(m_page_num);
    boolean success = loader.LoadInt(buff, PAGE_NUMBER_LABEL);
    m_page_num = loader.ii;
    return success;
  }
}
//
// ostream& operator<<( ostream& ss, const PdfBookmark& bb );
