import java.util.ArrayList;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import org.apache.commons.lang3.StringEscapeUtils;

import pdftk.com.lowagie.text.Rectangle;
import pdftk.com.lowagie.text.pdf.PdfArray;
import pdftk.com.lowagie.text.pdf.PdfBoolean;
import pdftk.com.lowagie.text.pdf.PdfDictionary;
import pdftk.com.lowagie.text.pdf.PdfName;
import pdftk.com.lowagie.text.pdf.PdfNumber;
import pdftk.com.lowagie.text.pdf.PdfObject;
import pdftk.com.lowagie.text.pdf.PdfReader;
import pdftk.com.lowagie.text.pdf.PdfString;
import pdftk.com.lowagie.text.pdf.PdfWriter;
import pdftk.com.lowagie.text.pdf.PRIndirectReference;

class data_import {

static PdfData
  LoadDataFile( InputStream ifs ) {
  PdfData pdf_data_p = new PdfData();
  System.err.println( "NOT TRANSLATED: LoadDataFile" );
  /* NOT TRANSLATED */
  return null;
}
  
static boolean
UpdateInfo( PdfReader reader_p,
            InputStream ifs,
            boolean utf8_b ) {
  boolean ret_val_b= true;

  PdfData pdf_data = LoadDataFile( ifs );
  if( pdf_data != null ) {
    
    { // trailer data
      PdfDictionary trailer_p= reader_p.getTrailer();
      if( trailer_p != null && trailer_p.isDictionary() ) {

        // bookmarks
        if( !pdf_data.m_bookmarks.isEmpty() ) {
          
          // build bookmarks
          PdfDictionary outlines_p= new PdfDictionary( PdfName.OUTLINES );
          if( outlines_p != null ) {
            PRIndirectReference outlines_ref_p= reader_p.getPRIndirectReference( outlines_p );

            int num_bookmarks_total = bookmarks.BuildBookmarks( reader_p,
                            pdf_data.m_bookmarks.listIterator(),
                            outlines_p,
                            outlines_ref_p,
                            0,
                            utf8_b );
            
            PdfDictionary root_p= (PdfDictionary)
              reader_p.getPdfObject( trailer_p.get( PdfName.ROOT ) );
            if( root_p.contains( PdfName.OUTLINES ) ) {
              // erase old bookmarks
              PdfDictionary old_outlines_p= (PdfDictionary)
                reader_p.getPdfObject( root_p.get( PdfName.OUTLINES ) );
              bookmarks.RemoveBookmarks( reader_p, old_outlines_p );
            }
            // insert into document
            root_p.put( PdfName.OUTLINES, (PdfObject)outlines_ref_p );
          }
        }

        // metadata
        if( !pdf_data.m_info.isEmpty() ) {
          PdfDictionary info_p= (PdfDictionary)
            reader_p.getPdfObject( trailer_p.get( PdfName.INFO ) );
          if( info_p != null && info_p.isDictionary() ) {

            for( PdfInfo it : pdf_data.m_info ) {
              if( it.m_value.isEmpty() ) {
                info_p.remove( new PdfName( it.m_key ) );
              }
              else {
                if( utf8_b ) { // UTF-8 encoded input
                  info_p.put( new PdfName( it.m_key ) ,
                               // patch by Quentin Godfroy <godfroy@clipper.ens.fr>, Chris Adams <cadams@salk.edu>
                               new PdfString( it.m_value ) );
                }
                else { // XML entities input
                  String jvs = XmlStringToJcharArray( it.m_value );
                  info_p.put( new PdfName( it.m_key ),
                               new PdfString( jvs ) );
                }
              }
            }
          }
          else { // error
            System.err.println( "pdftk Error in UpdateInfo(): no Info dictionary found;" );
            ret_val_b= false;
          }
        }
      }
      else { // error
        System.err.println( "pdftk Error in UpdateInfo(): no document trailer found;" );
        ret_val_b= false;
      }
    }

  }
  else { // error
    System.err.println( "pdftk Error in UpdateInfo(): LoadDataFile() failure;" );
  }
  // cerr << pdf_data; // debug

  return ret_val_b;
}

//////
////
// created for data import, maybe useful for export, too

//
class PdfInfo {
  static final String m_prefix= "Info";
  static final String m_begin_mark= "InfoBegin";
  static final String m_key_label= "InfoKey:";
  static final String m_value_label= "InfoValue:";

  String m_key = null;
  String m_value = null;

  boolean valid() { return( m_key!= null && m_value!= null ); }

  public String toString() {
    return m_begin_mark + System.lineSeparator() +
      m_key_label + " " + m_key + System.lineSeparator() +
      m_value_label + " " + m_value + System.lineSeparator();
  }
};

static class PdfData {
  ArrayList<PdfInfo> m_info = new ArrayList<PdfInfo>();
  ArrayList<bookmarks.PdfBookmark> m_bookmarks = new ArrayList<bookmarks.PdfBookmark>();

  int m_num_pages = -1;

  String m_id_0 = null;
  String m_id_1 = null;

  public String toString() {
    StringBuilder ss = new StringBuilder();
    for (PdfInfo vit : m_info) {
      ss.append(vit);
    }
    ss.append("PdfID0: " + m_id_0 + System.lineSeparator() +
              "PdfID1: " + m_id_1 + System.lineSeparator() +
              "NumberOfPages: " + m_num_pages + System.lineSeparator());
    for (bookmarks.PdfBookmark vit : m_bookmarks) {
      ss.append(vit);
    }
    return ss.toString();
  }
};

static String
XmlStringToJcharArray( String jvs )
{
  return StringEscapeUtils.unescapeXml( jvs );
}

};
