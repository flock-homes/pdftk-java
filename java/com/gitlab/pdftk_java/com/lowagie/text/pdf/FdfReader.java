/*
 * Copyright 2003 by Paulo Soares.
 *
 *
 * The Original Code is 'iText, a free JAVA-PDF library'.
 *
 * The Initial Developer of the Original Code is Bruno Lowagie. Portions created by
 * the Initial Developer are Copyright (C) 1999, 2000, 2001, 2002 by Bruno Lowagie.
 * All Rights Reserved.
 * Co-Developer of the code is Paulo Soares. Portions created by the Co-Developer
 * are Copyright (C) 2000, 2001, 2002 by Paulo Soares. All Rights Reserved.
 *
 * Contributor(s): all the names of the contributors are added in the source code
 * where applicable.
 *
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 * 
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 51 Franklin St, Fifth Floor,
 * Boston, MA  02110-1301, USA.
 *
 * If you didn't download this code from the following link, you should check if
 * you aren't using an obsolete version:
 * http://www.lowagie.com/iText/
 */

// pdftk-java iText base version 4.2.0
// pdftk-java modified yes (rich text, multi-valued fields)

package com.gitlab.pdftk_java.com.lowagie.text.pdf;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import com.gitlab.pdftk_java.com.lowagie.text.exceptions.InvalidPdfException;

/** Reads an FDF form and makes the fields available
 * @author Paulo Soares (psoares@consiste.pt)
 */
public class FdfReader extends PdfReader {
    
    HashMap fields;
    String fileSpec;
    PdfName encoding;
    
    /** Reads an FDF form.
     * @param filename the file name of the form
     * @throws IOException on error
     */    
    public FdfReader(String filename) throws IOException {
        super(filename);
    }
    
    /** Reads an FDF form.
     * @param pdfIn the byte array with the form
     * @throws IOException on error
     */    
    public FdfReader(byte pdfIn[]) throws IOException {
        super(pdfIn);
    }
    
    /** Reads an FDF form.
     * @param url the URL of the document
     * @throws IOException on error
     */    
    public FdfReader(URL url) throws IOException {
        super(url);
    }
    
    /** Reads an FDF form.
     * @param is the <CODE>InputStream</CODE> containing the document. The stream is read to the
     * end but is not closed
     * @throws IOException on error
     */    
    public FdfReader(InputStream is) throws IOException {
        super(is);
    }
    
    protected void readPdf() throws IOException {
        fields = new HashMap();
        try {
            tokens.checkFdfHeader();
            rebuildXref();
            readDocObj();
        }
        finally {
            try {
                tokens.close();
            }
            catch (Exception e) {
                // empty on purpose
            }
        }
        readFields();
    }
    
    protected void kidNode(PdfDictionary merged, String name) {
        PdfArray kids = merged.getAsArray(PdfName.KIDS);
        if (kids == null || kids.isEmpty()) {
            if (name.length() > 0)
                name = name.substring(1);
            fields.put(name, merged);
        }
        else {
            merged.remove(PdfName.KIDS);
            for (int k = 0; k < kids.size(); ++k) {
                PdfDictionary dic = new PdfDictionary();
                dic.merge(merged);
                PdfDictionary newDic = kids.getAsDict(k);
                PdfString t = newDic.getAsString(PdfName.T);
                String newName = name;
                if (t != null)
                    newName += "." + t.toUnicodeString();
                dic.merge(newDic);
                dic.remove(PdfName.T);
                kidNode(dic, newName);
            }
        }
    }

    // A file specification may be a string or a dictionary
    // See sect. 7.11.3 of the PDF Reference
    // See https://gitlab.com/pdftk-java/pdftk/-/issues/56
    protected void readFileSpecification(PdfDictionary fdf) {
        PdfObject fs = getPdfObject(fdf.get(PdfName.F));
        if (fs != null) {
            if (fs.isString()) {
                fileSpec = ((PdfString)fs).toUnicodeString();
            } else if (fs.isDictionary()) {
                PdfObject uf = getPdfObject(((PdfDictionary)fs).get(PdfName.UF));
                if (uf != null && uf.isString()) {
                    fileSpec = ((PdfString)uf).toUnicodeString();
                }
                else {
                    PdfObject f = getPdfObject(((PdfDictionary)fs).get(PdfName.F));
                    if (f != null && f.isString()) {
                        fileSpec = ((PdfString)f).toUnicodeString();
                    }
                }
            }
        }
    }
    
    protected void readFields() throws IOException {
        PdfDictionary fdf = null;
        catalog = trailer.getAsDict(PdfName.ROOT);
        if (catalog != null) {
            fdf = catalog.getAsDict(PdfName.FDF);
        }
        if (fdf == null) {
            throw new InvalidPdfException("Invalid FDF catalog.");
        }
        readFileSpecification(fdf);
        PdfArray fld = fdf.getAsArray(PdfName.FIELDS);
        if (fld == null)
            return;
        encoding = fdf.getAsName(PdfName.ENCODING);
        PdfDictionary merged = new PdfDictionary();
        merged.put(PdfName.KIDS, fld);
        kidNode(merged, "");
    }

    /** Gets all the fields. The map is keyed by the fully qualified
     * field name and the value is a merged <CODE>PdfDictionary</CODE>
     * with the field content.
     * @return all the fields
     */    
    public HashMap getFields() {
        return fields;
    }
    
    /** Gets the field dictionary.
     * @param name the fully qualified field name
     * @return the field dictionary
     */    
    public PdfDictionary getField(String name) {
        return (PdfDictionary)fields.get(name);
    }

    private String decodeString(PdfString vs) {
            if (encoding == null || vs.getEncoding() != null)
                return vs.toUnicodeString();
            byte b[] = vs.getBytes();
            if (b.length >= 2 && b[0] == (byte)254 && b[1] == (byte)255)
                return vs.toUnicodeString();
            try {
                if (encoding.equals(PdfName.SHIFT_JIS))
                    return new String(b, "SJIS");
                else if (encoding.equals(PdfName.UHC))
                    return new String(b, "MS949");
                else if (encoding.equals(PdfName.GBK))
                    return new String(b, "GBK");
                else if (encoding.equals(PdfName.BIGFIVE))
                    return new String(b, "Big5");
            }
            catch (Exception e) {
            }
            return vs.toUnicodeString();
    }
    
    /** Gets the field value or <CODE>null</CODE> if the field does not
     * exist or has no value defined.
     * @param name the fully qualified field name
     * @return the field value or <CODE>null</CODE>
     */    
    public String getFieldValue(String name) {
        PdfDictionary field = (PdfDictionary)fields.get(name);
        if (field == null)
            return null;
        PdfObject v = getPdfObject(field.get(PdfName.V));
        if (v == null)
            return null;
        if (v.isName())
            return PdfName.decodeName(((PdfName)v).toString());
        else if (v.isString()) {
            return decodeString((PdfString)v);
        }
        return null;
    }
    
    // ssteward
    // in a PDF, the Rich Text value of a field may be stored in
    // a string or a stream; I wonder if this applied to FDF, too?
    public String getFieldRichValue(String name) {
        PdfDictionary field = (PdfDictionary)fields.get(name);
        if (field == null)
            return null;
        PdfObject rv = getPdfObject(field.get(PdfName.RV));
        if (rv == null)
            return null;
        if (rv.isName())
            return PdfName.decodeName(((PdfName)rv).toString());
        else if (rv.isString())
            return ((PdfString)rv).toUnicodeString();
        else
            return null;
	}

    public String[] getFieldMultiValue(String name) {
        PdfDictionary field = (PdfDictionary)fields.get(name);
        if (field == null)
            return null;
        PdfArray v = field.getAsArray(PdfName.V);
        if (v == null) {
            String singlevalue = getFieldValue(name);
            if (singlevalue != null) return new String[]{singlevalue};
        }
        else {
            ArrayList<String> values = new ArrayList();
            ArrayList<PdfObject> vv = v.getArrayList();
            for (PdfObject vi : vv) {
                if (vi.isString()) {
                    values.add(decodeString((PdfString)vi));
                }
            }
            String[] ret = new String[values.size()];
            ret = values.toArray(ret);
            return ret;
        }
        return null;
    }

    /** Gets the PDF file specification contained in the FDF.
     * @return the PDF file specification contained in the FDF
     */    
    public String getFileSpec() {
        return fileSpec;
    }
}
