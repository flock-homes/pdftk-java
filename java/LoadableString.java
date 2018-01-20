class LoadableString {
  String ss= null;
  static String BufferString( String buff, int buff_ii ) {
    //while( buff[buff_ii] && isspace(buff[buff_ii]) ) { ++buff_ii; }
    if (buff_ii>=buff.length()) return "";
    if( Character.isWhitespace(buff.charAt(buff_ii)) ) // one or no spaces before data
      ++buff_ii;
    return( buff.substring( buff_ii ) );
  }
  boolean LoadString(String buff, String label) {
    int label_len= label.length();
    if ( buff.startsWith(label) ) {
      if( ss== null ) {
        ss= BufferString( buff, label_len );
      }
      else { // warning
        System.err.println("pdftk Warning: " + label + " (" + ss + ") already loaded when reading new " + label + " (" + BufferString( buff, label_len ) + ") -- skipping newer item");
      }
      return true;
    }
    return false;
  }
  LoadableString(String ss) {
    this.ss = ss;
  }
  public String toString() {
    return ss;
  }
};
