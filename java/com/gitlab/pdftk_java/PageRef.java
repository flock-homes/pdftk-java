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

class PageRef {
  int m_input_pdf_index;
  int m_page_num; // 1-based
  PageRotate m_page_rot; // DF rotation
  boolean m_page_abs; // DF absolute / relative rotation

  PageRef(int input_pdf_index, int page_num) {
    m_input_pdf_index = input_pdf_index;
    m_page_num = page_num;
    m_page_rot = PageRotate.NORTH;
    m_page_abs = false;
  }

  PageRef(int input_pdf_index, int page_num, PageRotate page_rot, boolean page_abs) {
    m_input_pdf_index = input_pdf_index;
    m_page_num = page_num;
    m_page_rot = page_rot;
    m_page_abs = page_abs;
  }
}
