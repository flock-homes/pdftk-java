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

import java.text.NumberFormat;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import pdftk.com.lowagie.text.Rectangle;

class LoadableRectangle {
  static final NumberFormat c_format = NumberFormat.getInstance(Locale.ROOT);

  Rectangle rr = null;

  static Rectangle BufferFloats(String buff, int buff_ii) {
    final String FLOATRE = "(-?\\d*\\.?\\d+)";
    Pattern p =
        Pattern.compile(
            "\\s?" + FLOATRE + "\\s" + FLOATRE + "\\s" + FLOATRE + "\\s" + FLOATRE + ".*");
    Matcher m = p.matcher(buff.substring(buff_ii));
    if (m.matches()) {
      return new Rectangle(
          Float.parseFloat(m.group(1)),
          Float.parseFloat(m.group(2)),
          Float.parseFloat(m.group(3)),
          Float.parseFloat(m.group(4)));
    } else {
      return null;
    }
  }

  boolean LoadRectangle(String buff, String label) {
    int label_len = label.length();
    if (buff.startsWith(label)) {
      if (rr == null) {
        rr = BufferFloats(buff, label_len);
      } else { // warning
        System.err.println(
            "pdftk Warning: "
                + label
                + " ("
                + position()
                + ") not empty when reading new "
                + label
                + " ("
                + position(BufferFloats(buff, label_len))
                + ") -- skipping newer item");
      }
      return true;
    }
    return false;
  }

  LoadableRectangle(Rectangle rr) {
    this.rr = rr;
  }

  static boolean equalPosition(Rectangle r, Rectangle s) {
    return r != null
        && s != null
        && r.left() == s.left()
        && r.bottom() == s.bottom()
        && r.right() == s.right()
        && r.top() == s.top();
  }

  String position() {
    return position(rr);
  }

  static String position(Rectangle r) {
    if (r == null) return "";
    return c_format.format(r.left())
        + " "
        + c_format.format(r.bottom())
        + " "
        + c_format.format(r.right())
        + " "
        + c_format.format(r.top());
  }

  String dimensions() {
    return dimensions(rr);
  }

  static String dimensions(Rectangle r) {
    if (r == null) return "";
    return c_format.format(r.right() - r.left()) + " " + c_format.format(r.top() - r.bottom());
  }
};
