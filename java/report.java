import java.util.ArrayList;
import java.util.Iterator;

import java.io.InputStream;
import java.io.OutputStream;

import pdftk.com.lowagie.text.pdf.PdfDictionary;
import pdftk.com.lowagie.text.pdf.PdfIndirectReference;
import pdftk.com.lowagie.text.pdf.PdfReader;
import pdftk.com.lowagie.text.pdf.PdfWriter;

class report {

static void
ReportAcroFormFields( OutputStream ofs,
                      PdfReader reader_p,
                      boolean utf8_b ) {
  /* NOT TRANSLATED */
}

static void
ReportAnnots( OutputStream ofs,
              PdfReader reader_p,
              boolean utf8_b ) {
  /* NOT TRANSLATED */
}

static void
ReportOnPdf( OutputStream ofs,
             PdfReader reader_p,
             boolean utf8_b ) {
  /* NOT TRANSLATED */
}

static boolean
UpdateInfo( PdfReader reader_p,
            InputStream ifs,
            boolean utf8_b ) {
  /* NOT TRANSLATED */
  return false;
}

//////
////
// created for data import, maybe useful for export, too

static final String g_uninitString= "PdftkEmptyString";

//
class PdfInfo {
  static final String m_prefix= "Info";
  static final String m_begin_mark= "InfoBegin";
  static final String m_key_label= "InfoKey:";
  static final String m_value_label= "InfoValue:";

  String m_key = g_uninitString;
  String m_value = g_uninitString;

  boolean valid() { return( m_key!= g_uninitString && m_value!= g_uninitString ); }
};

//
class PdfBookmark {
  static final String m_prefix= "Bookmark";
  static final String m_begin_mark= "BookmarkBegin";
  static final String m_title_label= "BookmarkTitle:";
  static final String m_level_label= "BookmarkLevel:";
  static final String m_page_number_label= "BookmarkPageNumber:";
  //static const string m_empty_string;

  String m_title = g_uninitString;
  int m_level = -1;
  int m_page_num = -1; // zero means no destination
  boolean valid() { return( 0< m_level && 0<= m_page_num && m_title!= g_uninitString ); }
};
//
//ostream& operator<<( ostream& ss, const PdfBookmark& bb );

static ArrayList<PdfBookmark>
ReadOutlines( PdfDictionary outline_p,
              int level,
              PdfReader reader_p,              
              boolean utf8_b ) {
  /* NOT TRANSLATED */
  return null;
}

static class BuildBookmarksState {
  PdfDictionary final_child_p;
  PdfIndirectReference final_child_ref_p;
  int num_bookmarks_total;
};

static int
BuildBookmarks(PdfWriter writer_p,
               Iterator<PdfBookmark> it,
               PdfDictionary parent_p,
               PdfIndirectReference parent_ref_p,
               PdfDictionary after_child_p,
               PdfIndirectReference after_child_ref_p,
               int parent_level,
               int page_num_offset,
               int level_offset,
               boolean utf8_b,
               BuildBookmarksState state) {
  /* NOT TRANSLATED */
  return 1;
}

};
