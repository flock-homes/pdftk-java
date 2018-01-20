import java.util.regex.Matcher;
import java.util.regex.Pattern;

class LoadableInt {
  int ii= -1;
  boolean success;
  static int BufferInt( String buff, int buff_ii ) {
    //while( buff[buff_ii] && isspace(buff[buff_ii]) ) { ++buff_ii; }
    Pattern p = Pattern.compile("\\s?(\\d+).*"); // one or no spaces before data
    Matcher m = p.matcher(buff.substring(buff_ii));
    if (m.matches()) {
      return Integer.parseInt(m.group(1));
    }
    else {
      return 0;
    }
  }
  boolean LoadInt(String buff, String label) {
    int label_len= label.length();
    if ( buff.startsWith(label) ) {
      if( ii< 0 ) { // uninitialized ints are -1
        ii= BufferInt( buff, label_len );
      }
      else { // warning
        System.err.println("pdftk Warning: " + label + " (" + ii + ") not empty when reading new " + label + " (" + BufferInt( buff, label_len ) + ") -- skipping newer item");
      }
      return true;
    }
    return false;
  }
  LoadableInt( int ii ) {
    this.ii = ii;
  }
  public String toString() {
    return Integer.toString(ii);
  }
};
