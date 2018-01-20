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

  boolean loadTitle( String buff ) {
    LoadableString loader = new LoadableString( m_title );
    boolean success = loader.LoadString( buff, m_title_label );
    m_title = loader.ss;
    return success;
  }
  boolean loadLevel( String buff ) {
    LoadableInt loader = new LoadableInt( m_level );
    boolean success = loader.LoadInt( buff, m_level_label );
    m_level = loader.ii;
    return success;
  }
  boolean loadPageNum( String buff ) {
    LoadableInt loader = new LoadableInt( m_page_num );
    boolean success = loader.LoadInt( buff, m_page_number_label );
    m_page_num = loader.ii;
    return success;
  }
  
};
//
//ostream& operator<<( ostream& ss, const PdfBookmark& bb );
