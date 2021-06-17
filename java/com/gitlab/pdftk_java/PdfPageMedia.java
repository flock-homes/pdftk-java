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

import com.gitlab.pdftk_java.com.lowagie.text.Rectangle;

class PdfPageMedia {
  static final String PREFIX = "PageMedia";
  static final String BEGIN_MARK = "PageMediaBegin";
  static final String NUMBER_LABEL = "PageMediaNumber:";
  static final String ROTATION_LABEL = "PageMediaRotation:";
  static final String RECT_LABEL = "PageMediaRect:";
  static final String DIMENSIONS_LABEL = "PageMediaDimensions:";
  static final String CROP_LABEL = "PageMediaCropRect:";

  int m_number = -1;
  int m_rotation = -1;
  Rectangle m_rect = null;
  Rectangle m_crop = null;

  boolean valid() {
    if (m_rotation >= 0 && m_rotation % 90 != 0) return false;
    return m_number > 0 && (m_rotation >= 0 || m_rect != null || m_crop != null);
  }

  public String toString() {
    StringBuilder ss = new StringBuilder();
    ss.append(BEGIN_MARK + System.lineSeparator());
    ss.append(NUMBER_LABEL + " " + m_number + System.lineSeparator());
    if (m_rotation != -1) ss.append(ROTATION_LABEL + " " + m_rotation + System.lineSeparator());
    if (m_rect != null) {
      ss.append(RECT_LABEL + " " + LoadableRectangle.position(m_rect) + System.lineSeparator());
      ss.append(
          DIMENSIONS_LABEL + " " + LoadableRectangle.dimensions(m_rect) + System.lineSeparator());
      if (m_crop != null && !LoadableRectangle.equalPosition(m_rect, m_crop)) {
        ss.append(CROP_LABEL + " " + LoadableRectangle.position(m_crop) + System.lineSeparator());
      }
    }
    return ss.toString();
  }

  boolean loadNumber(String buff) {
    LoadableInt loader = new LoadableInt(m_number);
    boolean success = loader.LoadInt(buff, NUMBER_LABEL);
    m_number = loader.ii;
    return success;
  }

  boolean loadRotation(String buff) {
    LoadableInt loader = new LoadableInt(m_rotation);
    boolean success = loader.LoadInt(buff, ROTATION_LABEL);
    m_rotation = (loader.ii % 360 + 360) % 360;
    return success;
  }

  boolean loadRect(String buff) {
    LoadableRectangle loader = new LoadableRectangle(m_rect);
    boolean success = loader.LoadRectangle(buff, RECT_LABEL);
    m_rect = loader.rr;
    return success;
  }

  boolean loadDimensions(String buff) {
    // Ignoring dimensions. Alternative: cross-check with Rect.
    return buff.startsWith(DIMENSIONS_LABEL);
  }

  boolean loadCrop(String buff) {
    LoadableRectangle loader = new LoadableRectangle(m_crop);
    boolean success = loader.LoadRectangle(buff, CROP_LABEL);
    m_crop = loader.rr;
    return success;
  }
}
