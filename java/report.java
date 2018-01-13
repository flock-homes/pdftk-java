import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import pdftk.com.lowagie.text.pdf.PdfReader;
import pdftk.com.lowagie.text.pdf.PdfWriter;

class report {

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
