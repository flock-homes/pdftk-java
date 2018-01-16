import java.util.ArrayList;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import org.apache.commons.lang3.StringEscapeUtils;

import pdftk.com.lowagie.text.pdf.PdfArray;
import pdftk.com.lowagie.text.pdf.PdfDictionary;
import pdftk.com.lowagie.text.pdf.PdfName;
import pdftk.com.lowagie.text.pdf.PdfObject;
import pdftk.com.lowagie.text.pdf.PdfReader;
import pdftk.com.lowagie.text.pdf.PdfString;
import pdftk.com.lowagie.text.pdf.PdfWriter;
import pdftk.com.lowagie.text.pdf.PRIndirectReference;

class report {

static String
OutputXmlString( String jss_p )
{
  return StringEscapeUtils.escapeXml10( jss_p );
}

static String
OutputUtf8String( String jss_p )
{
  return jss_p;
}
  
static String
OutputPdfString( PdfString pdfss_p,
                 boolean utf8_b )
{
  if( pdfss_p != null && pdfss_p.isString() ) {
    String jss_p= pdfss_p.toUnicodeString();
    if( utf8_b ) {
      return OutputUtf8String( jss_p );
    }
    else {
      return OutputXmlString( jss_p );
    }
  }
  return "";
}

static String
OutputPdfName( PdfName pdfnn_p )
{
  if( pdfnn_p != null && pdfnn_p.isName() ) {
    String jnn_p= new String( pdfnn_p.getBytes() );
    jnn_p= PdfName.decodeName( jnn_p );
    return OutputXmlString( jnn_p );
  }
  return "";
}

  
static void
ReportAcroFormFields( PrintWriter ofs,
                      PdfReader reader_p,
                      boolean utf8_b ) {
  System.err.println( "NOT TRANSLATED: ReportAcroFormFields" );
  /* NOT TRANSLATED */
}

static void
ReportAction( PrintWriter ofs, 
              PdfReader reader_p,
              PdfDictionary action_p,
              boolean utf8_b,
              String prefix ) {
  System.err.println( "NOT TRANSLATED: ReportAction" );
  /* NOT TRANSLATED */
}
  
static void
ReportAnnot( PrintWriter ofs,
             PdfReader reader_p,
             int page_num,
             PdfDictionary page_p,
             PdfDictionary annot_p,
             boolean utf8_b ) {
  System.err.println( "NOT TRANSLATED: ReportAnnot" );
  /* NOT TRANSLATED */
}

  
static void
ReportAnnots( PrintWriter ofs,
              PdfReader reader_p,
              boolean utf8_b ) {
  reader_p.resetReleasePage();

  ////
  // document information

  // document page count
  ofs.println("NumberOfPages: " + (int)reader_p.getNumberOfPages());

  // document base url
  PdfDictionary uri_p= (PdfDictionary)
    reader_p.getPdfObject( reader_p.catalog.get( PdfName.URI ) );
  if( uri_p != null && uri_p.isDictionary() ) {
    
    PdfString base_p= (PdfString)
      reader_p.getPdfObject( uri_p.get( PdfName.BASE ) );
    if( base_p != null && base_p.isString() ) {
      ofs.println("PdfUriBase: " + OutputPdfString( base_p, utf8_b ));
    }
  }

  ////
  // iterate over pages

  for( int ii= 1; ii<= reader_p.getNumberOfPages(); ++ii ) {
    PdfDictionary page_p= reader_p.getPageN( ii );

    PdfArray annots_p= (PdfArray)
      reader_p.getPdfObject( page_p.get( PdfName.ANNOTS ) );
    if( annots_p != null && annots_p.isArray() ) {

      ArrayList<PdfDictionary> annots_al_p= annots_p.getArrayList();
      if( annots_al_p != null ) {

        // iterate over annotations
        for( PdfDictionary annot_p : annots_al_p ) {

          if( annot_p != null && annot_p.isDictionary() ) {

            PdfName type_p= (PdfName)
              reader_p.getPdfObject( annot_p.get( PdfName.TYPE ) );
            if( type_p.equals( PdfName.ANNOT ) ) {

              PdfName subtype_p= (PdfName)
                reader_p.getPdfObject( annot_p.get( PdfName.SUBTYPE ) );
            
              // link annotation
              if( subtype_p.equals( PdfName.LINK ) ) {

                ofs.println("---"); // delim
                ReportAnnot( ofs, reader_p, ii, page_p, annot_p, utf8_b ); // base annot items
                ofs.println("AnnotPageNumber: " + ii);

                // link-specific items
                if( annot_p.contains( PdfName.A ) ) { // action
                  PdfDictionary action_p= (PdfDictionary)
                    reader_p.getPdfObject( annot_p.get( PdfName.A ) );
                  if( action_p != null && action_p.isDictionary() ) {

                    ReportAction( ofs, reader_p, action_p, utf8_b, "Annot" );
                  }
                }
              }
            }
          }
        }
      }
    }
    reader_p.releasePage( ii );
  }
  reader_p.resetReleasePage();
}

static void
ReportOnPdf( PrintWriter ofs,
             PdfReader reader_p,
             boolean utf8_b ) {
  System.err.println( "NOT TRANSLATED: ReportOnPdf" );
  /* NOT TRANSLATED */
}

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
