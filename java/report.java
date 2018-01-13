import java.util.ArrayList;
import java.util.ListIterator;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import pdftk.com.lowagie.text.pdf.PdfDestination;
import pdftk.com.lowagie.text.pdf.PdfDictionary;
import pdftk.com.lowagie.text.pdf.PdfIndirectReference;
import pdftk.com.lowagie.text.pdf.PdfObject;
import pdftk.com.lowagie.text.pdf.PdfName;
import pdftk.com.lowagie.text.pdf.PdfNumber;
import pdftk.com.lowagie.text.pdf.PdfReader;
import pdftk.com.lowagie.text.pdf.PdfString;
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

//
class PdfBookmark {
  static final String m_prefix= "Bookmark";
  static final String m_begin_mark= "BookmarkBegin";
  static final String m_title_label= "BookmarkTitle:";
  static final String m_level_label= "BookmarkLevel:";
  static final String m_page_number_label= "BookmarkPageNumber:";
  //static const string m_empty_string;

  String m_title = null;
  int m_level = -1;
  int m_page_num = -1; // zero means no destination
  boolean valid() { return( 0< m_level && 0<= m_page_num && m_title!= null ); }

  public String toString() {
    return m_begin_mark + System.lineSeparator() +
      m_title_label + " " + m_title + System.lineSeparator() +
      m_level_label + " " + m_level + System.lineSeparator() +
      m_page_number_label + " " + m_page_num + System.lineSeparator();
  }
};
//
//ostream& operator<<( ostream& ss, const PdfBookmark& bb );

static ArrayList<PdfBookmark>
ReadOutlines( PdfDictionary outline_p,
              int level,
              PdfReader reader_p,              
              boolean utf8_b ) {
  System.err.println( "NOT TRANSLATED: ReadOutlines" );
  /* NOT TRANSLATED */
  return null;
}

static String
XmlStringToJcharArray( String jvs )
{
  System.err.println( "NOT TRANSLATED: XmlStringToJcharArray" );
  /* NOT TRANSLATED */
  return null;
}

static void
RemoveBookmarks( PdfReader reader_p,
                 PdfDictionary bookmark_p )
// call reader_p->removeUnusedObjects() afterward
{
  if( bookmark_p.contains( PdfName.FIRST ) ) { // recurse
    PdfDictionary first_p= (PdfDictionary)
      reader_p.getPdfObject( bookmark_p.get( PdfName.FIRST ) );
    RemoveBookmarks( reader_p, first_p );

    bookmark_p.remove( PdfName.FIRST );
  }

  if( bookmark_p.contains( PdfName.NEXT ) ) { // recurse
    PdfDictionary next_p= (PdfDictionary)
      reader_p.getPdfObject( bookmark_p.get( PdfName.NEXT ) );
    RemoveBookmarks( reader_p, next_p );

    bookmark_p.remove( PdfName.NEXT );
  }

  bookmark_p.remove( PdfName.PARENT );
  bookmark_p.remove( PdfName.PREV );
  bookmark_p.remove( PdfName.LAST );
}
  
static class BuildBookmarksState {
  PdfDictionary final_child_p;
  PdfIndirectReference final_child_ref_p;
  int num_bookmarks_total;
};

static void
BuildBookmarks(PdfWriter writer_p,
               ListIterator<PdfBookmark> it,
               PdfDictionary parent_p,
               PdfIndirectReference parent_ref_p,
               PdfDictionary after_child_p,
               PdfIndirectReference after_child_ref_p,
               int parent_level,
               int page_num_offset,
               int level_offset,
               boolean utf8_b,
               BuildBookmarksState state)
  throws IOException
{
  // when using after_child, caller must
  // call writer_p->addToBody( after_child_p, after_child_ref_p ) upon return
  PdfDictionary bookmark_prev_p= after_child_p;
  PdfIndirectReference bookmark_prev_ref_p= after_child_ref_p;

  PdfIndirectReference bookmark_first_ref_p= null;
  int num_bookmarks= 0;

  PdfBookmark it_content = it.next();it.previous();
  if( parent_level+ 1< it_content.m_level ) { // first child jumping levels

    ////
    // add missing level

    ++num_bookmarks; ++state.num_bookmarks_total;
    PdfDictionary bookmark_p= new PdfDictionary();
    PdfIndirectReference bookmark_ref_p= writer_p.getPdfIndirectReference();
    bookmark_first_ref_p= bookmark_ref_p;

    bookmark_p.put( PdfName.PARENT, (PdfObject)parent_ref_p );

    PdfString title_p= new PdfString( "" );
    bookmark_p.put( PdfName.TITLE, title_p );

    bookmark_prev_p= bookmark_p;
    bookmark_prev_ref_p= bookmark_ref_p;

    // recurse in loop
  }

  for( ;it.hasNext(); it_content = it.next() ) {
  
    if( parent_level+ 1< it_content.m_level ) { // encountered child; recurse
      BuildBookmarks( writer_p,
                      it,
                      bookmark_prev_p, // parent
                      bookmark_prev_ref_p,
                      null, null,
                      parent_level+ 1,
                      page_num_offset,
                      level_offset,
                      utf8_b,
                      state );
      it_content = it.previous();
      continue;
    }
    else if( it_content.m_level< parent_level+ 1 ) {
      break; // no more children; add children to parent and return
    }

    ////
    // create child

    ++num_bookmarks; ++state.num_bookmarks_total;
    PdfDictionary bookmark_p= new PdfDictionary();
    PdfIndirectReference bookmark_ref_p= writer_p.getPdfIndirectReference();
    if( bookmark_first_ref_p == null )
      bookmark_first_ref_p= bookmark_ref_p;

    bookmark_p.put( PdfName.PARENT, (PdfObject)parent_ref_p );

    if( bookmark_prev_ref_p != null ) {
      bookmark_p.put( PdfName.PREV, (PdfObject)bookmark_prev_ref_p );
      bookmark_prev_p.put( PdfName.NEXT, (PdfObject)bookmark_ref_p );
    }

    if( utf8_b ) { // UTF-8 encoded input
      bookmark_p.put( PdfName.TITLE,
                       new PdfString( it_content.m_title /*,
                       itext::PdfObject::TEXT_UNICODE*/ ) );
    }
    else { // XML entities input
      String jvs = XmlStringToJcharArray( it_content.m_title );

      bookmark_p.put( PdfName.TITLE,
                       new PdfString( jvs /*,
                       itext::PdfObject::TEXT_UNICODE*/ ) );
    }

    if( 0< it_content.m_page_num ) { // destination
      PdfDestination dest_p= new PdfDestination(PdfDestination.FIT);
      PdfIndirectReference page_ref_p= 
        writer_p.getPageReference( it_content.m_page_num+ page_num_offset );
      if( page_ref_p != null ) {
        dest_p.addPage( (PdfIndirectReference)page_ref_p );
      }
      bookmark_p.put( PdfName.DEST, dest_p );
    }

    // finished with prev; add to body
    if( bookmark_prev_p != null )
      writer_p.addToBody( bookmark_prev_p, bookmark_prev_ref_p );

    bookmark_prev_p= bookmark_p;
    bookmark_prev_ref_p= bookmark_ref_p;
  }

  // finished with prev; add to body (unless we're appending)
  if( bookmark_prev_p != null && after_child_p == null )
    writer_p.addToBody( bookmark_prev_p, bookmark_prev_ref_p );

  if( bookmark_first_ref_p != null && bookmark_prev_ref_p != null ) {
    // pack these children into parent before returning
    if( !parent_p.contains( PdfName.FIRST ) ) // in case we're appending
      parent_p.put( PdfName.FIRST, (PdfObject)bookmark_first_ref_p );
    parent_p.put( PdfName.LAST, (PdfObject)bookmark_prev_ref_p );
    if( parent_level== 0 ) { // only for top-level "outlines" dict
      parent_p.put( PdfName.COUNT, new PdfNumber( state.num_bookmarks_total ) );
    }
    else {
      parent_p.put( PdfName.COUNT, new PdfNumber( num_bookmarks ) );
    }
  }

  // pass back to calling function so it can call BuildBookmarks serially
  state.final_child_p= bookmark_prev_p;
  state.final_child_ref_p= bookmark_prev_ref_p;
}

};
