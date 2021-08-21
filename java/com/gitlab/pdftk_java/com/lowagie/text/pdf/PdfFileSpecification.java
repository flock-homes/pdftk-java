/*
 * Copyright 2003 Paulo Soares
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
 *
 * If you didn't download this code from the following link, you should check if
 * you aren't using an obsolete version:
 * http://www.lowagie.com/iText/
 */

package com.gitlab.pdftk_java.com.lowagie.text.pdf;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Calendar;
import java.util.GregorianCalendar;
import com.gitlab.pdftk_java.com.lowagie.text.error_messages.MessageLocalization;

import com.gitlab.pdftk_java.com.lowagie.text.pdf.collection.PdfCollectionItem;
/** Specifies a file or an URL. The file can be extern or embedded.
 *
 * @author Paulo Soares (psoares@consiste.pt)
 */
public class PdfFileSpecification extends PdfDictionary {
    protected PdfWriter writer;
    protected PdfIndirectReference ref;
    
    /** Creates a new instance of PdfFileSpecification. The static methods are preferred. */
    public PdfFileSpecification() {

        // Ditching compatibility with Acrobat 5 for compatibility
        // with the ISO standard.
        // See https://gitlab.com/pdftk-java/pdftk/-/issues/61
        //
	// ssteward: for Acrobat 5 compatibility, set Type to "F" instead
	// of the (more correct) "Filesec"; this quirk is documented
	// in Implementation Note 38 on page 955 of the PDF Ref. ver. 1.5;
	// Acrobat 6 (and later) accept either "Filespec" or "F"
        //
	// super(PdfName.F);
        super(PdfName.FILESPEC);
    }
    
    /**
     * Creates a file specification of type URL.
     * @param writer the <CODE>PdfWriter</CODE>
     * @param url the URL
     * @return the file specification
     */    
    public static PdfFileSpecification url(PdfWriter writer, String url) {
        PdfFileSpecification fs = new PdfFileSpecification();
        fs.writer = writer;
        fs.put(PdfName.FS, PdfName.URL);
        fs.put(PdfName.F, new PdfString(url));
        return fs;
    }

    /**
     * Creates a file specification with the file embedded. The file may
     * come from the file system or from a byte array. The data is flate compressed.
     * @param writer the <CODE>PdfWriter</CODE>
     * @param filePath the file path
     * @param fileDisplay the file information that is presented to the user
     * @param fileStore the byte array with the file. If it is not <CODE>null</CODE>
     * it takes precedence over <CODE>filePath</CODE>
     * @throws IOException on error
     * @return the file specification
     */    
    public static PdfFileSpecification fileEmbedded(PdfWriter writer, String filePath, String fileDisplay, byte fileStore[]) throws IOException {
        return fileEmbedded(writer, filePath, fileDisplay, fileStore, PdfStream.BEST_COMPRESSION);
    }
    
    /**
     * Creates a file specification with the file embedded. The file may
     * come from the file system or from a byte array. The data is flate compressed.
     * @param writer the <CODE>PdfWriter</CODE>
     * @param filePath the file path
     * @param fileDisplay the file information that is presented to the user
     * @param fileStore the byte array with the file. If it is not <CODE>null</CODE>
     * it takes precedence over <CODE>filePath</CODE>
     * @param compressionLevel	the compression level to be used for compressing the file
     * it takes precedence over <CODE>filePath</CODE>
     * @throws IOException on error
     * @return the file specification
     * @since	2.1.3
     */    
    public static PdfFileSpecification fileEmbedded(PdfWriter writer, String filePath, String fileDisplay, byte fileStore[], int compressionLevel) throws IOException {
        return fileEmbedded(writer, filePath, fileDisplay, fileStore, null, null, compressionLevel);
    }
    
    
    /**
     * Creates a file specification with the file embedded. The file may
     * come from the file system or from a byte array.
     * @param writer the <CODE>PdfWriter</CODE>
     * @param filePath the file path
     * @param fileDisplay the file information that is presented to the user
     * @param fileStore the byte array with the file. If it is not <CODE>null</CODE>
     * it takes precedence over <CODE>filePath</CODE>
     * @param compress sets the compression on the data. Multimedia content will benefit little
     * from compression
     * @throws IOException on error
     * @return the file specification
     */    
    public static PdfFileSpecification fileEmbedded(PdfWriter writer, String filePath, String fileDisplay, byte fileStore[], boolean compress) throws IOException {
        return fileEmbedded(writer, filePath, fileDisplay, fileStore, null, null, compress ? PdfStream.BEST_COMPRESSION : PdfStream.NO_COMPRESSION);
    }
    
    /**
     * Creates a file specification with the file embedded. The file may
     * come from the file system or from a byte array.
     * @param writer the <CODE>PdfWriter</CODE>
     * @param filePath the file path
     * @param fileDisplay the file information that is presented to the user
     * @param fileStore the byte array with the file. If it is not <CODE>null</CODE>
     * it takes precedence over <CODE>filePath</CODE>
     * @param compress sets the compression on the data. Multimedia content will benefit little
     * from compression
     * @param mimeType the optional mimeType
     * @param fileParameter the optional extra file parameters such as the creation or modification date
     * @throws IOException on error
     * @return the file specification
     */    
    public static PdfFileSpecification fileEmbedded(PdfWriter writer, String filePath, String fileDisplay, byte fileStore[], boolean compress, String mimeType, PdfDictionary fileParameter) throws IOException {
    	return fileEmbedded(writer, filePath, fileDisplay, fileStore, mimeType, fileParameter, compress ? PdfStream.BEST_COMPRESSION : PdfStream.NO_COMPRESSION);
    }
    
    /**
     * Creates a file specification with the file embedded. The file may
     * come from the file system or from a byte array.
     * @param writer the <CODE>PdfWriter</CODE>
     * @param filePath the file path
     * @param fileDisplay the file information that is presented to the user
     * @param fileStore the byte array with the file. If it is not <CODE>null</CODE>
     * it takes precedence over <CODE>filePath</CODE>
     * @param mimeType the optional mimeType
     * @param fileParameter the optional extra file parameters such as the creation or modification date
     * @param compressionLevel the level of compression
     * @throws IOException on error
     * @return the file specification
     * @since	2.1.3
     */    
    public static PdfFileSpecification fileEmbedded(PdfWriter writer, String filePath, String fileDisplay, byte fileStore[], String mimeType, PdfDictionary fileParameter, int compressionLevel) throws IOException {
        PdfFileSpecification fs = new PdfFileSpecification();
        fs.writer = writer;
        fs.put(PdfName.F, new PdfString(fileDisplay));
        fs.setUnicodeFileName(fileDisplay, false);
        PdfEFStream stream;
        InputStream in = null;
        PdfIndirectReference ref;
        PdfIndirectReference refFileLength = null;
        PdfDate modDate = new PdfDate();
        try {
            if (fileStore == null) {
            refFileLength = writer.getPdfIndirectReference();
                File file = new File(filePath);
                if (file.canRead()) {
                    in = new FileInputStream(filePath);
                }
                else {
                    if (filePath.startsWith("file:/") || filePath.startsWith("http://") || filePath.startsWith("https://") || filePath.startsWith("jar:")) {
                        in = new URL(filePath).openStream();
                    }
                    else {
                        in = BaseFont.getResourceStream(filePath);
                        if (in == null)
                            throw new IOException(MessageLocalization.getComposedMessage("1.not.found.as.file.or.resource", filePath));
                    }
                }
                stream = new PdfEFStream(in, writer);

                Calendar calendar = new GregorianCalendar();
                calendar.setTimeInMillis(file.lastModified());
                modDate = new PdfDate(calendar);
            }
            else {
                stream = new PdfEFStream(fileStore);
            }
            stream.put(PdfName.TYPE, PdfName.EMBEDDEDFILE);
            stream.flateCompress(compressionLevel);
            PdfDictionary param = new PdfDictionary();
            if (fileParameter != null) {
                param.merge(fileParameter);
            }

            if (fileStore != null) {
                param.put(PdfName.SIZE, new PdfNumber(stream.getRawLength()));
                stream.put(PdfName.PARAMS, param);
            }
            else
                stream.put(PdfName.PARAMS, refFileLength);

            if (mimeType == null) mimeType = "application/octet-stream";
            stream.put(PdfName.SUBTYPE, new PdfName(mimeType));

            ref = writer.addToBody(stream).getIndirectReference();
            if (fileStore == null) {
                stream.writeLength();
                param.put(PdfName.SIZE, new PdfNumber(stream.getRawLength()));
            }
            param.put(PdfName.MODDATE, modDate);
            writer.addToBody(param, refFileLength);
        }
        finally {
            if (in != null)
                try{in.close();}catch(Exception e){}
        }
        PdfDictionary f = new PdfDictionary();
        f.put(PdfName.F, ref);
        f.put(PdfName.UF, ref);
        fs.put(PdfName.EF, f);
        return fs;
    }
    
    /**
     * Creates a file specification for an external file.
     * @param writer the <CODE>PdfWriter</CODE>
     * @param filePath the file path
     * @return the file specification
     */
    public static PdfFileSpecification fileExtern(PdfWriter writer, String filePath) {
        PdfFileSpecification fs = new PdfFileSpecification();
        fs.writer = writer;
        fs.put(PdfName.F, new PdfString(filePath));
        fs.setUnicodeFileName(filePath, false);
        return fs;
    }
    
    /**
     * Gets the indirect reference to this file specification.
     * Multiple invocations will retrieve the same value.
     * @throws IOException on error
     * @return the indirect reference
     */    
    public PdfIndirectReference getReference() throws IOException {
        if (ref != null)
            return ref;
        ref = writer.addToBody(this).getIndirectReference();
        return ref;
    }
    
    /**
     * Sets the file name (the key /F) string as an hex representation
     * to support multi byte file names. The name must have the slash and
     * backslash escaped according to the file specification rules
     * @param fileName the file name as a byte array
     */    
    /* ssteward: appears that fileName should hold the multi-byte data -- it isn't converted,
       but is packed literally into the PDF;
    public void setMultiByteFileName(byte fileName[]) {
        put(PdfName.F, new PdfString(fileName).setHexWriting(true));
    }
    */
    
    /**
     * Adds the unicode file name (the key /UF). This entry was introduced
     * in PDF 1.7. The filename must have the slash and backslash escaped
     * according to the file specification rules.
     * @param filename	the filename
     * @param unicode	if true, the filename is UTF-16BE encoded; otherwise PDFDocEncoding is used;
    */
    public void setUnicodeFileName(String filename, boolean unicode) {
        put(PdfName.UF, new PdfString(filename, unicode ? PdfObject.TEXT_UNICODE : PdfObject.TEXT_PDFDOCENCODING));
    }
    
    /**
     * Sets a flag that indicates whether an external file referenced by the file
     * specification is volatile. If the value is true, applications should never
     * cache a copy of the file.
     * @param volatile_file	if true, the external file should not be cached
     */
    public void setVolatile(boolean volatile_file) {
    	put(PdfName.V, new PdfBoolean(volatile_file));
    }
    
    /**
     * Adds a description for the file that is specified here.
     * @param description	some text
     * @param unicode		if true, the text is added as a unicode string
     */
    public void addDescription(String description, boolean unicode) {
        put(PdfName.DESC, new PdfString(description, unicode ? PdfObject.TEXT_UNICODE : PdfObject.TEXT_PDFDOCENCODING));
    }
    
    /**
     * Adds the Collection item dictionary.
     */
    public void addCollectionItem(PdfCollectionItem ci) {
    	put(PdfName.CI, ci);
    }
}
