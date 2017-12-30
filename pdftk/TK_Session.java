import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.PdfCopy;

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
    long m_input_pdf_index;
    long m_page_num; // 1-based
    PageRotate m_page_rot; // DF rotation
    boolean m_page_abs; // DF absolute / relative rotation

    PageRef( long input_pdf_index, long page_num) {
      m_input_pdf_index = input_pdf_index;
      m_page_num = page_num;
      m_page_rot = PageRotate.NORTH;
      m_page_abs = false;
    }
    PageRef( long input_pdf_index, long page_num,
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
  int create_output() {
    /* NOT TRANSLATED */
    return -1;
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
