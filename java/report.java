import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.lang3.StringEscapeUtils;

import pdftk.com.lowagie.text.pdf.PdfName;
import pdftk.com.lowagie.text.pdf.PdfReader;
import pdftk.com.lowagie.text.pdf.PdfString;
import pdftk.com.lowagie.text.pdf.PdfWriter;

class report {

static void 
OutputXmlString( StringBuilder ofs,
                 String jss_p )
{
  ofs.append( StringEscapeUtils.escapeXml10( jss_p ) );
}

static void 
OutputUtf8String( StringBuilder ofs,
                  String jss_p )
{
  ofs.append( jss_p );
}
  
static void
OutputPdfString( StringBuilder ofs,
                 PdfString pdfss_p,
                 boolean utf8_b )
{
  if( pdfss_p != null && pdfss_p.isString() ) {
    String jss_p= pdfss_p.toUnicodeString();
    if( utf8_b ) {
      OutputUtf8String( ofs, jss_p );
    }
    else {
      OutputXmlString( ofs, jss_p );
    }
  }
}

static void
OutputPdfName( StringBuilder ofs,
               PdfName pdfnn_p )
{
  if( pdfnn_p != null && pdfnn_p.isName() ) {
    String jnn_p= new String( pdfnn_p.getBytes() );
    jnn_p= PdfName.decodeName( jnn_p );
    OutputXmlString( ofs, jnn_p );
  }
}

  
static void
ReportAcroFormFields( OutputStream ofs,
                      PdfReader reader_p,
                      boolean utf8_b ) {
  System.err.println( "NOT TRANSLATED: ReportAcroFormFields" );
  /* NOT TRANSLATED */
}

static void
ReportAnnots( OutputStream ofs,
              PdfReader reader_p,
              boolean utf8_b ) {
  System.err.println( "NOT TRANSLATED: ReportAnnots" );
  /* NOT TRANSLATED */
}

static void
ReportOnPdf( OutputStream ofs,
             PdfReader reader_p,
             boolean utf8_b ) {
  System.err.println( "NOT TRANSLATED: ReportOnPdf" );
  /* NOT TRANSLATED */
}

static boolean
UpdateInfo( PdfReader reader_p,
            InputStream ifs,
            boolean utf8_b ) {
  System.err.println( "NOT TRANSLATED: UpdateInfo" );
  /* NOT TRANSLATED */
  return false;
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


static String
XmlStringToJcharArray( String jvs )
{
  System.err.println( "NOT TRANSLATED: XmlStringToJcharArray" );
  /* NOT TRANSLATED */
  return null;
}


};
