import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;

import java.io.OutputStream;

import pdftk.com.lowagie.text.Document;
import pdftk.com.lowagie.text.pdf.PdfArray;
import pdftk.com.lowagie.text.pdf.PdfCopy;
import pdftk.com.lowagie.text.pdf.PdfDictionary;
import pdftk.com.lowagie.text.pdf.PdfIndirectReference;
import pdftk.com.lowagie.text.pdf.PdfName;
import pdftk.com.lowagie.text.pdf.PdfNumber;
import pdftk.com.lowagie.text.pdf.PdfObject;
import pdftk.com.lowagie.text.pdf.PdfReader;
import pdftk.com.lowagie.text.pdf.PdfWriter;

class TK_Session {
  
  boolean m_valid_b;
  boolean m_authorized_b;
  boolean m_input_pdf_readers_opened_b; // have m_input_pdf readers been opened?
  boolean m_verbose_reporting_b;
  boolean m_ask_about_warnings_b;

  //typedef unsigned long PageNumber;
  enum PageRotate { NORTH(0), EAST(90), SOUTH(180), WEST(270);
                    final int value;
                    PageRotate(int value) { this.value = value; }
  }; // DF rotation
  //typedef bool PageRotateAbsolute; // DF absolute / relative rotation

  class InputPdf {
    String m_filename;
    String m_password;
    boolean m_authorized_b = true;

    // keep track of which pages get output under which readers,
    // because one reader mayn't output the same page twice;
    class PagesReader {
      TreeSet<Integer> first;
      PdfReader second;
    };
    ArrayList<PagesReader> m_readers = new ArrayList<PagesReader>();

    long m_num_pages = 0;

  };
  // pack input PDF in the order they're given on the command line
  ArrayList<InputPdf> m_input_pdf = new ArrayList<InputPdf>();
  //typedef vector< InputPdf >::size_type InputPdfIndex;

  // store input PDF handles here
  HashMap<String, Long> m_input_pdf_index = new HashMap<String, Long>();

  boolean add_reader( InputPdf input_pdf_p, boolean keep_artifacts_b ) {
    /* NOT TRANSLATED */
    return false;
  }
  boolean open_input_pdf_readers() {
    /* NOT TRANSLATED */
    return false;
  }

  
  ArrayList<String> m_input_attach_file_filename = new ArrayList<String>();
  int m_input_attach_file_pagenum;

  String m_update_info_filename;
  boolean m_update_info_utf8_b;
  String m_update_xmp_filename;

  enum keyword {
    none_k,

    // the operations
    cat_k, // combine pages from input PDFs into a single output
    shuffle_k, // like cat, but interleaves pages from input ranges
    burst_k, // split a single, input PDF into individual pages
    barcode_burst_k, // barcode_burst project
    filter_k, // apply 'filters' to a single, input PDF based on output args
    dump_data_k, // no PDF output
    dump_data_utf8_k,
    dump_data_fields_k,
    dump_data_fields_utf8_k,
    dump_data_annots_k,
    generate_fdf_k,
    unpack_files_k, // unpack files from input; no PDF output

    // these are treated the same as operations,
    // but they are processed using the filter operation
    fill_form_k, // read FDF file and fill PDF form fields
    attach_file_k, // attach files to output
    update_info_k,
    update_info_utf8_k, // if info isn't utf-8, it is encoded using xml entities
    update_xmp_k,
    background_k, // promoted from output option to operation in pdftk 1.10
    multibackground_k, // feature added by Bernhard R. Link <brlink@debian.org>, Johann Felix Soden <johfel@gmx.de>
    stamp_k,
    multistamp_k, // feature added by Bernhard R. Link <brlink@debian.org>, Johann Felix Soden <johfel@gmx.de>
    rotate_k, // rotate given pages as directed

    // optional attach_file argument
    attach_file_to_page_k,

    // cat page range keywords
    end_k,
    even_k,
    odd_k,

    output_k,

    // encryption & decryption
    input_pw_k,
    owner_pw_k,
    user_pw_k,
    user_perms_k,

    // output arg.s, only
    encrypt_40bit_k,
    encrypt_128bit_k,

    // user permissions
    perm_printing_k,
    perm_modify_contents_k,
    perm_copy_contents_k,
    perm_modify_annotations_k,
    perm_fillin_k,
    perm_screen_readers_k,
    perm_assembly_k,
    perm_degraded_printing_k,
    perm_all_k,

    // filters
    filt_uncompress_k,
    filt_compress_k,

    // forms
    flatten_k,
    need_appearances_k,
    drop_xfa_k,
    drop_xmp_k,
    keep_first_id_k,
    keep_final_id_k,

    // pdftk options
    verbose_k,
    dont_ask_k,
    do_ask_k,

    // page rotation
    rot_north_k,
    rot_east_k,
    rot_south_k,
    rot_west_k,
    rot_left_k,
    rot_right_k,
    rot_upside_down_k
  };
  final keyword first_operation_k = keyword.cat_k;
  final keyword final_operation_k = keyword.unpack_files_k;
  static keyword is_keyword( String ss, Integer keyword_len_p ) {
    /* NOT TRANSLATED */
    return keyword.none_k;
  }

  keyword m_operation;

  class PageRef {
    int m_input_pdf_index;
    int m_page_num; // 1-based
    PageRotate m_page_rot; // DF rotation
    boolean m_page_abs; // DF absolute / relative rotation

    PageRef( int input_pdf_index, int page_num) {
      m_input_pdf_index = input_pdf_index;
      m_page_num = page_num;
      m_page_rot = PageRotate.NORTH;
      m_page_abs = false;
    }
    PageRef( int input_pdf_index, int page_num,
             PageRotate page_rot, boolean page_abs ) {
      m_input_pdf_index = input_pdf_index;
      m_page_num = page_num;
      m_page_rot = page_rot;
      m_page_abs = page_abs;
    }
  };
  ArrayList<ArrayList<PageRef>> m_page_seq = new ArrayList<ArrayList<PageRef>>(); // one vector for each given page range

  String m_form_data_filename;
  String m_background_filename;
  String m_stamp_filename;
  String m_output_filename;
  boolean m_output_utf8_b;
  String m_output_owner_pw;
  String m_output_user_pw;
  int m_output_user_perms;
  boolean m_multistamp_b; // use all pages of input stamp PDF, not just the first
  boolean m_multibackground_b; // use all pages of input background PDF, not just the first
  boolean m_output_uncompress_b;
  boolean m_output_compress_b;
  boolean m_output_flatten_b;
  boolean m_output_need_appearances_b;
  boolean m_output_drop_xfa_b;
  boolean m_output_drop_xmp_b;
  boolean m_output_keep_first_id_b;
  boolean m_output_keep_final_id_b;
  boolean m_cat_full_pdfs_b; // we are merging entire docs, not select pages

  enum encryption_strength {
    none_enc,
    bits40_enc,
    bits128_enc
  };
  encryption_strength m_output_encryption_strength;

  TK_Session( String[] args ) {
    /* NOT TRANSLATED */
  }

  boolean is_valid() {
    /* NOT TRANSLATED */
    return false;
  }

  void dump_session_data() {
    /* NOT TRANSLATED */
  }

  void attach_files
  ( PdfReader input_reader_p,
    PdfWriter writer_p ) {
    /* NOT TRANSLATED */
  }

  void unpack_files
    ( PdfReader input_reader_p ) {
    /* NOT TRANSLATED */
  }

  int create_output_page( PdfCopy writer_p, PageRef page_ref, int output_page_count ) {
    /* NOT TRANSLATED */
    return -1;
  }

static char GetPdfVersionChar( PdfName version_p ) {
  char version_cc= PdfWriter.VERSION_1_4; // default

  if( version_p != null )
    if( version_p.equals( PdfName.VERSION_1_4 ) )
      version_cc= PdfWriter.VERSION_1_4;
    else if( version_p.equals( PdfName.VERSION_1_5 ) )
      version_cc= PdfWriter.VERSION_1_5;
    else if( version_p.equals( PdfName.VERSION_1_6 ) )
      version_cc= PdfWriter.VERSION_1_6;
    else if( version_p.equals( PdfName.VERSION_1_7 ) )
      version_cc= PdfWriter.VERSION_1_7;
    else if( version_p.equals( PdfName.VERSION_1_3 ) )
      version_cc= PdfWriter.VERSION_1_3;
    else if( version_p.equals( PdfName.VERSION_1_2 ) )
      version_cc= PdfWriter.VERSION_1_2;
    else if( version_p.equals( PdfName.VERSION_1_1 ) )
      version_cc= PdfWriter.VERSION_1_1;
    else if( version_p.equals( PdfName.VERSION_1_0 ) )
      version_cc= PdfWriter.VERSION_1_0;

  return version_cc;
}

int create_output() {
  int ret_val= 0; // default: no error

  if( is_valid() ) {

    /*
    bool rdfcat_available_b= false;
    { // is rdfcat available?  first character should be a digit;
      // grab stderr to keep messages appearing to user;
      // 2>&1 might not work on older versions of Windows (e.g., 98);
      FILE* pp= popen( "rdfcat --version 2>&1", "r" );
      if( pp ) {
        int cc= fgetc( pp );
        if( '0'<= cc && cc<= '9' ) {
          rdfcat_available_b= true;
        }
        pclose( pp );
      }
    }
    */

    if( m_verbose_reporting_b ) {
      System.out.println();
      System.out.println("Creating Output ...");
    }

    String creator= "pdftk " + pdftk.PDFTK_VER + " - www.pdftk.com";
    String jv_creator_p= creator;

    if( m_output_owner_pw== "PROMPT" ) {
      m_output_owner_pw = pdftk.prompt_for_password( "owner", "the output PDF" );
    }
    if( m_output_user_pw== "PROMPT" ) {
      m_output_user_pw= pdftk.prompt_for_password( "user", "the output PDF");
    }

    byte[] output_owner_pw_p= new byte[0];
    if( !m_output_owner_pw.isEmpty() ) {
      output_owner_pw_p= passwords.utf8_password_to_pdfdoc(m_output_owner_pw, true );
      if (output_owner_pw_p == null) { // error
        System.err.println("Error: Owner password used to encrypt output PDF includes");
        System.err.println("   invalid characters.");
        System.err.println("   No output created.");
        ret_val= 1;
      }
    }

    byte[] output_user_pw_p= new byte[0];
    if( !m_output_user_pw.isEmpty() ) {
      output_user_pw_p= passwords.utf8_password_to_pdfdoc(m_output_user_pw, true );
      if (output_user_pw_p == null) { // error
        System.err.println("Error: User password used to encrypt output PDF includes");
        System.err.println("   invalid characters.");
        System.err.println("   No output created.");
        ret_val= 1;
      }
    }

    if( ret_val !=0 )
      return ret_val; // <--- exit

    try {
      switch( m_operation ) {

      case cat_k :
      case shuffle_k : { // catenate pages or shuffle pages
        Document output_doc_p= new Document();

        OutputStream ofs_p= 
          pdftk.get_output_stream( m_output_filename, 
                             m_ask_about_warnings_b );

        if( ofs_p == null ) { // file open error
          ret_val= 1;
          break;
        }
        PdfCopy writer_p= new PdfCopy( output_doc_p, ofs_p );

        // update to suit any features that we add, e.g. encryption;
        char max_version_cc= PdfWriter.VERSION_1_2;

        //
        output_doc_p.addCreator( jv_creator_p );

        // un/compress output streams?
        if( m_output_uncompress_b ) {
          writer_p.filterStreams= true;
          writer_p.compressStreams= false;
        }
        else if( m_output_compress_b ) {
          writer_p.filterStreams= false;
          writer_p.compressStreams= true;
        }

        // encrypt output?
        if( m_output_encryption_strength!= encryption_strength.none_enc ||
            !m_output_owner_pw.isEmpty() || 
            !m_output_user_pw.isEmpty() )
          {
            // if no stregth is given, default to 128 bit,
            boolean bit128_b=
              ( m_output_encryption_strength!= encryption_strength.bits40_enc );

            writer_p.setEncryption( output_user_pw_p,
                                     output_owner_pw_p,
                                     m_output_user_perms,
                                     bit128_b );

            if( bit128_b )
              max_version_cc= PdfWriter.VERSION_1_4;
            else // 1.1 probably okay, here
              max_version_cc= PdfWriter.VERSION_1_3;
          }

        // copy file ID?
        if( m_output_keep_first_id_b ||
            m_output_keep_final_id_b )
          {
            PdfReader input_reader_p= 
              m_output_keep_first_id_b ?
              m_input_pdf.get(0).m_readers.get(0).second :
              m_input_pdf.get(m_input_pdf.size()- 1).m_readers.get(0).second;
                
            PdfDictionary trailer_p= input_reader_p.getTrailer();
            
            PdfArray file_id_p= (PdfArray) input_reader_p.getPdfObject( trailer_p.get( PdfName.ID ) );
            if( file_id_p != null && file_id_p.isArray() ) {

              writer_p.setFileID( file_id_p );
            }
          }

        // set output PDF version to the max PDF ver of all the input PDFs;
        // also find the maximum extension levels, if present -- this can
        // only be added /after/ opening the document;
        //
        // collected extensions information; uses PdfName::hashCode() for key
        HashMap< PdfName, PdfName > ext_base_versions;
        HashMap< PdfName, Integer > ext_levels;
        for( InputPdf it : m_input_pdf)
          {
            PdfReader reader_p= it.m_readers.get(0).second;

            ////
            // PDF version number

            // version in header
            if( max_version_cc< reader_p.getPdfVersion() )
              max_version_cc= reader_p.getPdfVersion();

            // version override in catalog; used only if greater than header version, per PDF spec;
            PdfDictionary catalog_p= reader_p.getCatalog();
            if( catalog_p.contains( PdfName.VERSION ) ) {

              PdfName version_p= (PdfName) reader_p.getPdfObject( catalog_p.get( PdfName.VERSION ) );
              char version_cc= GetPdfVersionChar( version_p );

              if( max_version_cc< version_cc )
                max_version_cc= version_cc;
            }

            ////
            // PDF extensions

            if( catalog_p.contains( PdfName.EXTENSIONS ) ) {
              PdfDictionary extensions_p= (PdfDictionary) reader_p.getPdfObject( catalog_p.get( PdfName.EXTENSIONS ) );
              if( extensions_p != null && extensions_p.isDictionary() ) {

                // iterate over developers
                Set<PdfObject> keys_p= extensions_p.getKeys();
                Iterator<PdfObject> kit= keys_p.iterator();
                while( kit.hasNext() ) {
                  PdfName developer_p= (PdfName) reader_p.getPdfObject( kit.next() );
                    
                  PdfDictionary dev_exts_p= (PdfDictionary) reader_p.getPdfObject( extensions_p.get( developer_p ) );
                  if( dev_exts_p != null && dev_exts_p.isDictionary() ) {

                    if( dev_exts_p.contains( PdfName.BASEVERSION ) &&
                        dev_exts_p.contains( PdfName.EXTENSIONLEVEL ) )
                      {
                        // use the greater base version or the greater extension level

                        PdfName base_version_p= (PdfName) reader_p.getPdfObject( dev_exts_p.get( PdfName.BASEVERSION ) );
                        PdfNumber ext_level_p= (PdfNumber) reader_p.getPdfObject( dev_exts_p.get( PdfName.EXTENSIONLEVEL ) );

                        if( !ext_base_versions.containsKey( developer_p ) ||
                            GetPdfVersionChar( ext_base_versions.get( developer_p ) )<
                            GetPdfVersionChar( base_version_p ) )
                          { // new developer or greater base version
                            ext_base_versions.put( developer_p, base_version_p );
                            ext_levels.put( developer_p, ext_level_p.intValue() );
                          }
                        else if( GetPdfVersionChar( ext_base_versions.get( developer_p ) )==
                                 GetPdfVersionChar( base_version_p ) &&
                                 ext_levels.get( developer_p )< ext_level_p.intValue() )
                          { // greater extension level for current base version
                            ext_levels.put( developer_p, ext_level_p.intValue() );
                          }
                      }
                  }
                }
              }
            }
          }
        // set the pdf version
        writer_p.setPdfVersion( max_version_cc );

        // open the doc
        output_doc_p.open();

        // set any pdf version extensions we might have found
        if( !ext_base_versions.isEmpty() ) {
          PdfDictionary extensions_dict_p= new PdfDictionary();
          PdfIndirectReference extensions_ref_p= writer_p.getPdfIndirectReference();
          for( Map.Entry<PdfName, PdfName> it : ext_base_versions.entrySet())
            {
              PdfDictionary ext_dict_p= new PdfDictionary();
              ext_dict_p.put( PdfName.BASEVERSION, it.getValue() );
              ext_dict_p.put( PdfName.EXTENSIONLEVEL, 
                              new PdfNumber( ext_levels.get(it.getKey()) ) );
              
              extensions_dict_p.put( it.getKey(), ext_dict_p );
            }

          writer_p.addToBody( extensions_dict_p, extensions_ref_p );
          writer_p.setExtensions( extensions_ref_p );
        }

        if( m_operation== keyword.shuffle_k ) {
          int max_seq_length= 0;
          for( ArrayList< PageRef > jt : m_page_seq)
            {
              max_seq_length= ( max_seq_length< jt.size() ) ? jt.size() : max_seq_length;
            }

          int output_page_count= 0;
          // iterate over ranges
          for( int ii= 0; ( ii< max_seq_length && ret_val== 0 ); ++ii ) {
            // iterate over ranges
            for( ArrayList< PageRef > jt : m_page_seq )
              {
                if (ret_val != 0) break;
                if( ii< jt.size() ) {
                  ret_val= create_output_page( writer_p, jt.get(ii), output_page_count );
                  ++output_page_count;
                }
              }
          }
        }
        else { // cat_k
          
          int output_page_count= 0;
          // iterate over page ranges
          for( ArrayList< PageRef > jt : m_page_seq )
            {
              if (ret_val != 0) break;
              // iterate over pages in page range
              for( PageRef it : jt )
                {
                  if (ret_val != 0) break;
                  ret_val= create_output_page( writer_p, it, output_page_count );
                  ++output_page_count;
                }
            }

          // first impl added a bookmark for each input PDF and then
          // added any of that PDFs bookmarks under that; now it
          // appends input PDF bookmarks, which is more attractive;
          // OTOH, some folks might want pdftk to add bookmarks for
          // input PDFs, esp if they don't have bookmarks -- TODO
          // but then, it would be nice to allow the user to specify
          // a label -- using the PDF filename is unattractive;
          if( m_cat_full_pdfs_b ) { // add bookmark info
            // cerr << "cat full pdfs!" << endl; // debug

            PdfDictionary output_outlines_p= 
              new PdfDictionary( PdfName.OUTLINES );
            PdfIndirectReference output_outlines_ref_p= 
              writer_p.getPdfIndirectReference();

            PdfDictionary after_child_p= null;
            PdfIndirectReference after_child_ref_p= null;
              
            int page_count= 1;
            int num_bookmarks_total= 0;
            /* used for adding doc bookmarks
            itext::PdfDictionary* prev_p= 0;
            itext::PdfIndirectReference* prev_ref_p= 0;
            */
            // iterate over page ranges; each full PDF has one page seq in m_page_seq;
            // using m_page_seq instead of m_input_pdf, so the doc order is right
            for( ArrayList< PageRef > jt : m_page_seq)
              {
                PdfReader reader_p=
                  m_input_pdf.get(jt.get(0).m_input_pdf_index).m_readers.get(0).second;
                long reader_page_count= 
                  m_input_pdf.get(jt.get(0).m_input_pdf_index).m_num_pages;
            
                /* used for adding doc bookmarks
                itext::PdfDictionary* item_p= new itext::PdfDictionary();
                itext::PdfIndirectReference* item_ref_p= writer_p->getPdfIndirectReference();

                item_p->put( itext::PdfName::PARENT, outlines_ref_p );
                item_p->put( itext::PdfName::TITLE, 
                             new itext::PdfString( JvNewStringUTF( (*it).m_filename.c_str() ) ) );
                  
                // wire into linked list
                if( prev_p ) {
                  prev_p->put( itext::PdfName::NEXT, item_ref_p );
                  item_p->put( itext::PdfName::PREV, prev_ref_p );
                }
                else { // first item; wire into outlines dict
                  output_outlines_p->put( itext::PdfName::FIRST, item_ref_p );
                }

                // the destination
                itext::PdfDestination* dest_p= new itext::PdfDestination(itext::PdfDestination::FIT);
                itext::PdfIndirectReference* page_ref_p= writer_p->getPageReference( page_count );
                if( page_ref_p ) {
                  dest_p->addPage( page_ref_p );
                }
                item_p->put( itext::PdfName::DEST, dest_p );
                */

                // pdf bookmarks -> children
                { 
                  PdfDictionary catalog_p= reader_p.getCatalog();
                  PdfDictionary outlines_p= (PdfDictionary) reader_p.getPdfObject( catalog_p.get( PdfName.OUTLINES ) );
                  if( outlines_p != null && outlines_p.isDictionary() ) {

                    PdfDictionary top_outline_p= (PdfDictionary)
                      reader_p.getPdfObject( outlines_p.get( PdfName.FIRST ) );
                    if( top_outline_p != null && top_outline_p.isDictionary() ) {

                      vector<PdfBookmark> bookmark_data;
                      int rr= ReadOutlines( bookmark_data, top_outline_p, 0, reader_p, true );
                      if( rr== 0 && !bookmark_data.empty() ) {

                        // passed in by reference, so must use variable:
                        Iterator<PdfBookmark> vit= bookmark_data.iterator();
                        BuildBookmarks( writer_p,
                                        vit,
                                        //item_p, item_ref_p, // used for adding doc bookmarks
                                        output_outlines_p, output_outlines_ref_p,
                                        after_child_p, after_child_ref_p,
                                        after_child_p, after_child_ref_p,
                                        0, num_bookmarks_total, 
                                        page_count- 1, // page offset is 0-based
                                        0,
                                        true );
                      }
                      /*
                      else if( rr!= 0 )
                      cerr << "ReadOutlines error" << endl; // debug
                      else
                      cerr << "empty bookmark data" << endl; // debug
                      */
                    }
                  }
                  /*
                  else
                    cerr << "no outlines" << endl; // debug
                  */
                }

                /* used for adding doc bookmarks
                // finished with prev; add to body
                if( prev_p )
                  writer_p->addToBody( prev_p, prev_ref_p );

                prev_p= item_p;
                prev_ref_p= item_ref_p;
                */

                page_count+= reader_page_count;

              }
            /* used for adding doc bookmarks
            if( prev_p ) { // wire into outlines dict
              // finished with prev; add to body
              writer_p->addToBody( prev_p, prev_ref_p );

              output_outlines_p->put( itext::PdfName::LAST, prev_ref_p );
              output_outlines_p->put( itext::PdfName::COUNT, new itext::PdfNumber( (jint)m_input_pdf.size() ) );
            }
            */

            if( num_bookmarks_total ) { // we encountered bookmarks

              // necessary for serial appending to outlines
              if( after_child_p && after_child_ref_p )
                writer_p.addToBody( after_child_p, after_child_ref_p );

              writer_p.addToBody( output_outlines_p, output_outlines_ref_p );
              writer_p.setOutlines( output_outlines_ref_p );
            }
          }
          
        }

        output_doc_p.close();
        writer_p.close();
      }
      break;
      
      case burst_k : { // burst input into pages

        // we should have been given only a single, input file
        if( 1< m_input_pdf.size() ) { // error
          System.err.println("Error: Only one input PDF file may be given for \"burst\" op.");
          System.err.println("   No output created.");
          break;
        }

        // grab the first reader, since there's only one
        PdfReader input_reader_p= 
          m_input_pdf.begin().m_readers.front().second;
        jint input_num_pages= 
          m_input_pdf.begin().m_num_pages;

        if( m_output_filename== "PROMPT" ) {
          prompt_for_filename( "Please enter a filename pattern for the PDF pages (e.g. pg_%04d.pdf):",
                               m_output_filename );
        }
        if( m_output_filename.empty() ) {
          m_output_filename= "pg_%04d.pdf";
        }

        // locate the input PDF Info dictionary that holds metadata
        PdfDictionary input_info_p= 0; {
          PdfDictionary input_trailer_p= input_reader_p.getTrailer();
          if( input_trailer_p && input_trailer_p.isDictionary() ) {
            input_info_p= (PdfDictionary)
              input_reader_p.getPdfObject( input_trailer_p.get( PdfName.INFO ) );
            if( input_info_p && input_info_p.isDictionary() ) {
              // success
            }
            else {
              input_info_p= 0;
            }
          }
        }

        for( jint ii= 0; ii< input_num_pages; ++ii ) {

          // the filename
          String jv_output_filename_p= m_output_filename.format(ii+ 1);

          Document output_doc_p= new Document();
          FileOutputStream ofs_p= new FileOutputStream( jv_output_filename_p );
          PdfCopy writer_p= new PdfCopy( output_doc_p, ofs_p );
          writer_p.setFromReader( input_reader_p );

          output_doc_p.addCreator( jv_creator_p );

          // un/compress output streams?
          if( m_output_uncompress_b ) {
            writer_p.filterStreams= true;
            writer_p.compressStreams= false;
          }
          else if( m_output_compress_b ) {
            writer_p.filterStreams= false;
            writer_p.compressStreams= true;
          }

          // encrypt output?
          if( m_output_encryption_strength!= none_enc ||
              !m_output_owner_pw.empty() || 
              !m_output_user_pw.empty() )
            {
              // if no stregth is given, default to 128 bit,
              jboolean bit128_b=
                ( m_output_encryption_strength!= bits40_enc );

              writer_p.setEncryption( output_user_pw_p,
                                       output_owner_pw_p,
                                       m_output_user_perms,
                                       bit128_b );
            }

          output_doc_p.open(); // must open writer before copying (possibly) indirect object

          { // copy the Info dictionary metadata
            if( input_info_p ) {
              PdfDictionary* writer_info_p= writer_p.getInfo();
              if( writer_info_p ) {
                PdfDictionary* info_copy_p= writer_p.copyDictionary( input_info_p );
                if( info_copy_p ) {
                  writer_info_p.putAll( info_copy_p );
                }
              }
            }
            jbyteArray input_reader_xmp_p= input_reader_p.getMetadata();
            if( input_reader_xmp_p ) {
              writer_p.setXmpMetadata( input_reader_xmp_p );
            }
          }

          PdfImportedPage* page_p= 
            writer_p.getImportedPage( input_reader_p, ii+ 1 );
          writer_p.addPage( page_p );

          output_doc_p.close();
          writer_p.close();
        }

        ////
        // dump document data

        string doc_data_fn= "doc_data.txt";
        if( !m_output_filename.empty() ) {
          char path_delim= PATH_DELIM;
          long loc= 0;
          if( (loc=m_output_filename.rfind( path_delim ))!= string::npos ) {

            doc_data_fn= m_output_filename.substr( 0, loc )+ 
              ((char)PATH_DELIM)+ doc_data_fn;
          }          
        }
        ofstream ofs = new ofstream( doc_data_fn.c_str() );
        if( ofs ) {
          ReportOnPdf( ofs, input_reader_p, m_output_utf8_b );
        }
        else { // error
          System.err.println("Error: unable to open file for output: doc_data.txt");
          ret_val= 1;
        }

      }
      break;

      case filter_k: { // apply operations to given PDF file

        // we should have been given only a single, input file
        if( 1< m_input_pdf.size() ) { // error
          System.err.println("Error: Only one input PDF file may be given for this");
          System.err.println("   operation.  Maybe you meant to use the \"cat\" operator?");
          System.err.println("   No output created.");
          ret_val= 1;
          break;
        }

        // try opening the FDF file before we get too involved;
        // if input is stdin ("-"), don't pass it to both the FDF and XFDF readers
        FdfReader* fdf_reader_p= 0;
        XfdfReader* xfdf_reader_p= 0;
        if( m_form_data_filename== "PROMPT" ) { // handle case where user enters '-' or (empty) at the prompt
          prompt_for_filename( "Please enter a filename for the form data:", 
                               m_form_data_filename );
        }
        if( !m_form_data_filename.empty() ) { // we have form data to process
          if( m_form_data_filename== "-" ) { // form data on stdin
            //JArray<jbyte>* in_arr= itext::RandomAccessFileOrArray::InputStreamToArray( java::System::in );
            
            // first try fdf
            try {
              fdf_reader_p= new FdfReader( System.in );
            }
            catch( IOException ioe_p ) { // file open error

              // maybe it's xfdf?
              try {
                xfdf_reader_p= new XfdfReader( System.in );
              }
              catch( IOException ioe_p ) { // file open error
                System.err.println("Error: Failed read form data on stdin.");
                System.err.println("   No output created.");
                ret_val= 1;
                //ioe_p->printStackTrace(); // debug
                break;
              }
            }
          }
          else { // form data file

            // first try fdf
            try {
              fdf_reader_p=
                new FdfReader( m_form_data_filename );
            }
            catch( IOException ioe_p ) { // file open error
              // maybe it's xfdf?
              try {
                xfdf_reader_p=
                  new XfdfReader( m_form_data_filename );
              }
              catch( IOException ioe_p ) { // file open error
                System.err.println("Error: Failed to open form data file: ");
                System.err.println("   " + m_form_data_filename);
                System.err.println("   No output created.");
                ret_val= 1;
                //ioe_p->printStackTrace(); // debug
                break;
              }
            }
          }
        }

        // try opening the PDF background or stamp before we get too involved
        PdfReader* mark_p= 0;
        bool background_b= true; // set false for stamp
        //
        // background
        if( m_background_filename== "PROMPT" ) {
          prompt_for_filename( "Please enter a filename for the background PDF:", 
                               m_background_filename );
        }
        if( !m_background_filename.empty() ) {
          try {
            mark_p= new PdfReader( JvNewStringUTF( m_background_filename.c_str() ) );
            mark_p.removeUnusedObjects();
            //reader->shuffleSubsetNames(); // changes the PDF subset names, but not the PostScript font names
          }
          catch( IOException ioe_p ) { // file open error
            System.err.println("Error: Failed to open background PDF file: ");
            System.err.println("   " + m_background_filename);
            System.err.println("   No output created.");
            ret_val= 1;
            break;
          }
        }
        //
        // stamp
        if( !mark_p ) {
          if( m_stamp_filename== "PROMPT" ) {
            prompt_for_filename( "Please enter a filename for the stamp PDF:", 
                                 m_stamp_filename );
          }
          if( !m_stamp_filename.empty() ) {
            background_b= false;
            try {
              mark_p= new PdfReader( m_stamp_filename );
              mark_p.removeUnusedObjects();
              //reader->shuffleSubsetNames(); // changes the PDF subset names, but not the PostScript font names
            }
            catch( IOException ioe_p ) { // file open error
              System.err.println("Error: Failed to open stamp PDF file: ");
              System.err.println("   " + m_stamp_filename);
              System.err.println("   No output created.");
              ret_val= 1;
              break;
            }
          }
        }

        //
        OutputStream ofs_p= get_output_stream( m_output_filename, m_ask_about_warnings_b );
        if( !ofs_p ) { // file open error
          System.err.println("Error: unable to open file for output: " + m_output_filename);
          ret_val= 1;
          break;
        }

        //
        PdfReader input_reader_p= m_input_pdf.get(0).m_readers.get(0).second;

        // drop the xfa?
        if( m_output_drop_xfa_b ) {
          PdfDictionary catalog_p= input_reader_p.catalog;
          if( catalog_p && catalog_p.isDictionary() ) {
              
            PdfDictionary acro_form_p= (PdfDictionary)
              input_reader_p.getPdfObject( catalog_p.get( PdfName::ACROFORM ) );
            if( acro_form_p && acro_form_p.isDictionary() ) {

              acro_form_p.remove( PdfName::XFA );
            }
          }
        }

        // drop the xmp?
        if( m_output_drop_xmp_b ) {
          PdfDictionary* catalog_p= input_reader_p.catalog;
          if( catalog_p && catalog_p.isDictionary() ) {
              
            catalog_p.remove( PdfName::METADATA );
          }
        }

        //
        PdfStamperImp* writer_p=
          new PdfStamperImp( input_reader_p, ofs_p, 0, false /* append mode */ );

        // update the info?
        if( m_update_info_filename== "PROMPT" ) {
          prompt_for_filename( "Please enter an Info file filename:",
                               m_update_info_filename );
        }
        if( !m_update_info_filename.empty() ) {
          if( m_update_info_filename== "-" ) {
            if( !UpdateInfo( input_reader_p, cin, m_update_info_utf8_b ) ) {
              System.err.println("Warning: no Info added to output PDF.");
              ret_val= 3;
            }
          }
          else {
            ifstream ifs = new ifstream( m_update_info_filename.c_str() );
            if( ifs ) {
              if( !UpdateInfo( input_reader_p, ifs, m_update_info_utf8_b ) ) {
                System.err.println("Warning: no Info added to output PDF.");
                ret_val= 3;
              }
            }
            else { // error
              System.err.println("Error: unable to open FDF file for input: " + m_update_info_filename);
              ret_val= 1;
              break;
            }
          }
        }

        /*
        // update the xmp?
        if( !m_update_xmp_filename.empty() ) {
          if( rdfcat_available_b ) {
            if( m_update_xmp_filename== "PROMPT" ) {
              prompt_for_filename( "Please enter an Info file filename:",
                                   m_update_xmp_filename );
            }
            if( !m_update_xmp_filename.empty() ) {
              UpdateXmp( input_reader_p, m_update_xmp_filename );
            }
          }
          else { // error
            cerr << "Error: to use this feature, you must install the rdfcat program." << endl;
            cerr << "   Perhaps the replace_xmp feature would suit you, instead?" << endl;
            break;
          }
        }
        */

        // rotate pages?
        if( !m_page_seq.empty() ) {
          for( ArrayList< PageRef > jt : m_page_seq ) {
            for ( PageRef kt : jt ) {
              apply_rotation_to_page( input_reader_p, kt.m_page_num,
                                      kt.m_page_rot, kt.m_page_abs );
            }
          }
        }

        // un/compress output streams?
        if( m_output_uncompress_b ) {
          add_marks_to_pages( input_reader_p );
          writer_p.filterStreams= true;
          writer_p.compressStreams= false;
        }
        else if( m_output_compress_b ) {
          remove_marks_from_pages( input_reader_p );
          writer_p.filterStreams= false;
          writer_p.compressStreams= true;
        }

        // encrypt output?
        if( m_output_encryption_strength!= none_enc ||
            !m_output_owner_pw.empty() ||
            !m_output_user_pw.empty() )
          {

            // if no stregth is given, default to 128 bit,
            // (which is incompatible w/ Acrobat 4)
            boolean bit128_b=
              ( m_output_encryption_strength!= bits40_enc );

            writer_p.setEncryption( output_user_pw_p,
                                      output_owner_pw_p,
                                      m_output_user_perms,
                                      bit128_b );
          }

        // fill form fields?
        if( fdf_reader_p || xfdf_reader_p ) {
          if( input_reader_p.getAcroForm() ) { // we really have a form to fill

            AcroFields* fields_p= writer_p.getAcroFields();
            fields_p.setGenerateAppearances( true ); // have iText create field appearances
            if( ( fdf_reader_p && fields_p.setFields( fdf_reader_p ) ) ||
                ( xfdf_reader_p && fields_p.setFields( xfdf_reader_p ) ) )
              { // Rich Text input found

                // set the PDF so that Acrobat will create appearances;
                // this might appear contradictory to our setGenerateAppearances( true ) call,
                // above; setting this, here, allows us to keep the generated appearances,
                // in case the PDF is opened somewhere besides Acrobat; yet, Acrobat/Reader
                // will create the Rich Text appearance if it has a chance
                m_output_need_appearances_b= true;
                /*
                itext::PdfDictionary* catalog_p= input_reader_p->catalog;
                if( catalog_p && catalog_p->isDictionary() ) {
              
                  itext::PdfDictionary* acro_form_p= (itext::PdfDictionary*)
                    input_reader_p->getPdfObject( catalog_p->get( itext::PdfName::ACROFORM ) );
                  if( acro_form_p && acro_form_p->isDictionary() ) {

                    acro_form_p->put( itext::PdfName::NEEDAPPEARANCES, itext::PdfBoolean::PDFTRUE );
                  }
                }
                */
              }
          }
          else { // warning
            System.err.println("Warning: input PDF is not an acroform, so its fields were not filled.");
            ret_val= 3;
          }
        }

        // flatten form fields?
        writer_p.setFormFlattening( m_output_flatten_b );

        // cue viewer to render form field appearances?
        if( m_output_need_appearances_b ) {
          PdfDictionary* catalog_p= input_reader_p.catalog;
          if( catalog_p && catalog_p.isDictionary() ) {
            PdfDictionary acro_form_p= (PdfDictionary)
              input_reader_p.getPdfObject( catalog_p.get( PdfName.ACROFORM ) );
            if( acro_form_p && acro_form_p.isDictionary() ) {
              acro_form_p.put( PdfName.NEEDAPPEARANCES, 
                                PdfBoolean.PDFTRUE );
            }
          }
        }

        // add background/watermark?
        if( mark_p ) {

          jint mark_num_pages= 1; // default: use only the first page of mark
          if( m_multistamp_b || m_multibackground_b ) { // use all pages of mark
            mark_num_pages= mark_p.getNumberOfPages();
          }

          // the mark information; initialized inside loop
          PdfImportedPage* mark_page_p= 0;
          Rectangle* mark_page_size_p= 0;
          jint mark_page_rotation= 0;

          // iterate over document's pages, adding mark_page as
          // a layer above (stamp) or below (watermark) the page content;
          // scale mark_page and move it so it fits within the document's page;
          //
          jint num_pages= input_reader_p.getNumberOfPages();
          for( jint ii= 0; ii< num_pages; ) {
            ++ii; // page refs are 1-based, not 0-based

            // the mark page and its geometry
            if( ii<= mark_num_pages ) {
              mark_page_size_p= mark_p.getCropBox( ii );
              mark_page_rotation= mark_p.getPageRotation( ii );
              for( jint mm= 0; mm< mark_page_rotation; mm+=90 ) {
                mark_page_size_p= mark_page_size_p.rotate();
              }

              // create a PdfTemplate from the first page of mark
              // (PdfImportedPage is derived from PdfTemplate)
              mark_page_p= writer_p.getImportedPage( mark_p, ii );
            }

            // the target page geometry
            Rectangle* doc_page_size_p= 
              input_reader_p.getCropBox( ii );
            jint doc_page_rotation= input_reader_p.getPageRotation( ii );
            for( jint mm= 0; mm< doc_page_rotation; mm+=90 ) {
              doc_page_size_p= doc_page_size_p.rotate();
            }

            jfloat h_scale= doc_page_size_p.width() / mark_page_size_p.width();
            jfloat v_scale= doc_page_size_p.height() / mark_page_size_p.height();
            jfloat mark_scale= (h_scale< v_scale) ? h_scale : v_scale;

            jfloat h_trans= (jfloat)(doc_page_size_p.left()- mark_page_size_p.left()* mark_scale +
                                     (doc_page_size_p.width()- 
                                      mark_page_size_p.width()* mark_scale) / 2.0);
            jfloat v_trans= (jfloat)(doc_page_size_p.bottom()- mark_page_size_p.bottom()* mark_scale +
                                     (doc_page_size_p.height()- 
                                      mark_page_size_p.height()* mark_scale) / 2.0);
          
            PdfContentByte* content_byte_p= 
              ( background_b ) ? writer_p.getUnderContent( ii ) : writer_p.getOverContent( ii );

            if( mark_page_rotation== 0 ) {
              content_byte_p.addTemplate( mark_page_p, 
                                           mark_scale, 0,
                                           0, mark_scale,
                                           h_trans, 
                                           v_trans );
            }
            else if( mark_page_rotation== 90 ) {
              content_byte_p.addTemplate( mark_page_p, 
                                           0, -1* mark_scale,
                                           mark_scale, 0,
                                           h_trans, 
                                           v_trans+ mark_page_size_p.height()* mark_scale );
            }
            else if( mark_page_rotation== 180 ) {
              content_byte_p.addTemplate( mark_page_p, 
                                           -1* mark_scale, 0,
                                           0, -1* mark_scale,
                                           h_trans+ mark_page_size_p.width()* mark_scale, 
                                           v_trans+ mark_page_size_p.height()* mark_scale );
            }
            else if( mark_page_rotation== 270 ) {
              content_byte_p.addTemplate( mark_page_p, 
                                           0, mark_scale,
                                           -1* mark_scale, 0,
                                           h_trans+ mark_page_size_p.width()* mark_scale, v_trans );
            }
          }
        }

        // attach file to document?
        if( !m_input_attach_file_filename.empty() ) {
          this.attach_files( input_reader_p,
                              writer_p );
        }

        // performed in add_reader(), but this eliminates objects after e.g. drop_xfa, drop_xmp
        input_reader_p.removeUnusedObjects();

        // done; write output
        writer_p.close();
      }
      break;

      case dump_data_fields_k :
      case dump_data_annots_k :
      case dump_data_k: { // report on input document

        // we should have been given only a single, input file
        if( 1< m_input_pdf.size() ) { // error
          System.err.println("Error: Only one input PDF file may be used for the dump_data operation");
          System.err.println("   No output created.");
          ret_val= 1;
          break;
        }

        PdfReader input_reader_p= 
          m_input_pdf.get(0).m_readers.get(0).second;

        if( m_output_filename.empty() || m_output_filename== "-" ) {
          if( m_operation== dump_data_k ) {
            ReportOnPdf( cout, input_reader_p, m_output_utf8_b );
          }
          else if( m_operation== dump_data_fields_k ) {
            ReportAcroFormFields( cout, input_reader_p, m_output_utf8_b );
          }
          else if( m_operation== dump_data_annots_k ) {
            ReportAnnots( cout, input_reader_p, m_output_utf8_b );
          }
        }
        else {
          ofstream ofs = new ofstream( m_output_filename.c_str() );
          if( ofs ) {
            if( m_operation== dump_data_k ) {
              ReportOnPdf( ofs, input_reader_p, m_output_utf8_b );
            }
            else if( m_operation== dump_data_fields_k ) {
              ReportAcroFormFields( ofs, input_reader_p, m_output_utf8_b );
            }
            else if( m_operation== dump_data_annots_k ) {
              ReportAnnots( ofs, input_reader_p, m_output_utf8_b );
            }
          }
          else { // error
            System.err.println("Error: unable to open file for output: " + m_output_filename);
          }
        }
      }
      break;

      case generate_fdf_k : { // create a dummy FDF file that would work with the input PDF form

        // we should have been given only a single, input file
        if( 1< m_input_pdf.size() ) { // error
          System.err.println("Error: Only one input PDF file may be used for the generate_fdf operation");
          System.err.println("   No output created.");
          break;
        }

        PdfReader input_reader_p= 
          m_input_pdf.get(0).m_readers.get(0).second;

        OutputStream ofs_p= 
          get_output_stream( m_output_filename, 
                             m_ask_about_warnings_b );
        if( ofs_p ) {
          FdfWriter writer_p= new FdfWriter();
          input_reader_p.getAcroFields().exportAsFdf( writer_p );
          writer_p.writeTo( ofs_p );
          // no writer_p->close() function

          //delete writer_p; // OK? GC? -- NOT okay!
        }
        else { // error: get_output_stream() reports error
          ret_val= 1;
          break;
        }
      }
        break;

      case unpack_files_k: { // copy PDF file attachments into current directory

        // we should have been given only a single, input file
        if( 1< m_input_pdf.size() ) { // error
          System.err.println("Error: Only one input PDF file may be given for \"unpack_files\" op.");
          System.err.println("   No output created.");
          ret_val= 1;
          break;
        }

        PdfReader input_reader_p= 
          m_input_pdf.get(0).m_readers.get(0).second;

        this.unpack_files( input_reader_p );
      }
        break;
      default:
        // error
        System.err.println("Unexpected pdftk Error in create_output()");
        ret_val= 2;
        break;
      }
    }
    catch( Throwable t_p )
      {
        System.err.println("Unhandled Java Exception in create_output():");
        t_p.printStackTrace();
        ret_val= 2;
      }
  }
  else { // error
    ret_val= 1;
  }

  return ret_val;
}

  private enum ArgState {
    input_files_e,
    input_pw_e,

    page_seq_e,
    form_data_filename_e,
    
    attach_file_filename_e,
    attach_file_pagenum_e,

    update_info_filename_e,
    update_xmp_filename_e,

    output_e, // state where we expect output_k, next
    output_filename_e,

    output_args_e, // output args are order-independent; switch here
    output_owner_pw_e,
    output_user_pw_e,
    output_user_perms_e,

    background_filename_e,
    stamp_filename_e,

    done_e
  };

  // convenience function; return true iff handled
  private boolean handle_some_output_options( TK_Session.keyword kw, ArgState arg_state_p ) {
    /* NOT TRANSLATED */
    return false;
  }

};
