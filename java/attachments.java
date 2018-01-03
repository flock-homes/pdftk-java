class attachments {
static String
  drop_path( String ss )
{
  char path_delim= pdftk.PATH_DELIM; // given at compile-time
  int loc = ss.lastIndexOf( path_delim );
  if( loc != -1 && loc!= ss.length()- 1 ) {
    return ss.substring( loc+ 1 );
  }
  return ss;
}

}
