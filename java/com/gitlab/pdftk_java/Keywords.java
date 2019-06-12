/*
 *   This file is part of the pdftk port to java
 *
 *   Copyright (c) Marc Vinyals 2017-2018
 *
 *   The program is a java port of PDFtk, the PDF Toolkit
 *   Copyright (c) 2003-2013 Steward and Lee, LLC
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 2 of the License, or
 *   (at your option) any later version.
 *
 *   The program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.gitlab.pdftk_java;

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
    multibackground_k, // feature added by Bernhard R. Link <brlink@debian.org>, Johann Felix Soden
    // <johfel@gmx.de>
    stamp_k,
    multistamp_k, // feature added by Bernhard R. Link <brlink@debian.org>, Johann Felix Soden
    // <johfel@gmx.de>
    rotate_k, // rotate given pages as directed

    // optional attach_file argument
    attach_file_to_page_k,

    // cat page range keywords
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

class Keywords {
  static keyword is_keyword(String ss) {
    ss = ss.toLowerCase();

    // operations
    if (ss.equals("cat")) {
      return keyword.cat_k;
    } else if (ss.equals("shuffle")) {
      return keyword.shuffle_k;
    } else if (ss.equals("burst")) {
      return keyword.burst_k;
    } else if (ss.equals("filter")) {
      return keyword.filter_k;
    } else if (ss.equals("dump_data")
        || ss.equals("dumpdata")
        || ss.equals("data_dump")
        || ss.equals("datadump")) {
      return keyword.dump_data_k;
    } else if (ss.equals("dump_data_utf8")) {
      return keyword.dump_data_utf8_k;
    } else if (ss.equals("dump_data_fields")) {
      return keyword.dump_data_fields_k;
    } else if (ss.equals("dump_data_fields_utf8")) {
      return keyword.dump_data_fields_utf8_k;
    } else if (ss.equals("dump_data_annots")) {
      return keyword.dump_data_annots_k;
    } else if (ss.equals("generate_fdf")
        || ss.equals("fdfgen")
        || ss.equals("fdfdump")
        || ss.equals("dump_data_fields_fdf")) {
      return keyword.generate_fdf_k;
    } else if (ss.equals("fill_form") || ss.equals("fillform")) {
      return keyword.fill_form_k;
    } else if (ss.equals("attach_file") || ss.equals("attach_files") || ss.equals("attachfile")) {
      return keyword.attach_file_k;
    } else if (ss.equals("unpack_file") || ss.equals("unpack_files") || ss.equals("unpackfiles")) {
      return keyword.unpack_files_k;
    } else if (ss.equals("update_info") || ss.equals("undateinfo")) {
      return keyword.update_info_k;
    } else if (ss.equals("update_info_utf8") || ss.equals("undateinfoutf8")) {
      return keyword.update_info_utf8_k;
    }
    /* requires more testing and work
    else if( strcmp( ss_copy, "update_xmp" ) ||
             strcmp( ss_copy, "undatexmp" ) ) {
      return update_xmp_k;
    }
    */
    else if (ss.equals("background")) {
      // pdftk 1.10: making background an operation
      // (and preserving old behavior for backwards compatibility)
      return keyword.background_k;
    } else if (ss.equals("multibackground")) {
      return keyword.multibackground_k;
    } else if (ss.equals("multistamp")) {
      return keyword.multistamp_k;
    } else if (ss.equals("stamp")) {
      return keyword.stamp_k;
    } else if (ss.equals("rotate")) {
      return keyword.rotate_k;
    }

    // cat range keywords
    else if (ss.startsWith("even")) { // note: strncmp
      return keyword.even_k;
    } else if (ss.startsWith("odd")) { // note: strncmp
      return keyword.odd_k;
    }

    // file attachment option
    else if (ss.equals("to_page") || ss.equals("topage")) {
      return keyword.attach_file_to_page_k;
    } else if (ss.equals("output")) {
      return keyword.output_k;
    }

    // encryption & decryption; depends on context
    else if (ss.equals("owner_pw") || ss.equals("ownerpw")) {
      return keyword.owner_pw_k;
    } else if (ss.equals("user_pw") || ss.equals("userpw")) {
      return keyword.user_pw_k;
    } else if (ss.equals("input_pw") || ss.equals("inputpw")) {
      return keyword.input_pw_k;
    } else if (ss.equals("allow")) {
      return keyword.user_perms_k;
    }

    // expect these only in output section
    else if (ss.equals("encrypt_40bit")
        || ss.equals("encrypt_40bits")
        || ss.equals("encrypt40bit")
        || ss.equals("encrypt40bits")
        || ss.equals("encrypt40_bit")
        || ss.equals("encrypt40_bits")
        || ss.equals("encrypt_40_bit")
        || ss.equals("encrypt_40_bits")) {
      return keyword.encrypt_40bit_k;
    } else if (ss.equals("encrypt_128bit")
        || ss.equals("encrypt_128bits")
        || ss.equals("encrypt128bit")
        || ss.equals("encrypt128bits")
        || ss.equals("encrypt128_bit")
        || ss.equals("encrypt128_bits")
        || ss.equals("encrypt_128_bit")
        || ss.equals("encrypt_128_bits")) {
      return keyword.encrypt_128bit_k;
    }

    // user permissions; must follow user_perms_k;
    else if (ss.equals("printing")) {
      return keyword.perm_printing_k;
    } else if (ss.equals("modifycontents")) {
      return keyword.perm_modify_contents_k;
    } else if (ss.equals("copycontents")) {
      return keyword.perm_copy_contents_k;
    } else if (ss.equals("modifyannotations")) {
      return keyword.perm_modify_annotations_k;
    } else if (ss.equals("fillin")) {
      return keyword.perm_fillin_k;
    } else if (ss.equals("screenreaders")) {
      return keyword.perm_screen_readers_k;
    } else if (ss.equals("assembly")) {
      return keyword.perm_assembly_k;
    } else if (ss.equals("degradedprinting")) {
      return keyword.perm_degraded_printing_k;
    } else if (ss.equals("allfeatures")) {
      return keyword.perm_all_k;
    } else if (ss.equals("uncompress")) {
      return keyword.filt_uncompress_k;
    } else if (ss.equals("compress")) {
      return keyword.filt_compress_k;
    } else if (ss.equals("flatten")) {
      return keyword.flatten_k;
    } else if (ss.equals("need_appearances")) {
      return keyword.need_appearances_k;
    } else if (ss.equals("drop_xfa")) {
      return keyword.drop_xfa_k;
    } else if (ss.equals("drop_xmp")) {
      return keyword.drop_xmp_k;
    } else if (ss.equals("keep_first_id")) {
      return keyword.keep_first_id_k;
    } else if (ss.equals("keep_final_id")) {
      return keyword.keep_final_id_k;
    } else if (ss.equals("verbose")) {
      return keyword.verbose_k;
    } else if (ss.equals("dont_ask") || ss.equals("dontask")) {
      return keyword.dont_ask_k;
    } else if (ss.equals("do_ask")) {
      return keyword.do_ask_k;
    }

    // more cat range keywords
    else if (ss.equals("north")) {
      return keyword.rot_north_k;
    } else if (ss.equals("south")) {
      return keyword.rot_south_k;
    } else if (ss.equals("east")) {
      return keyword.rot_east_k;
    } else if (ss.equals("west")) {
      return keyword.rot_west_k;
    } else if (ss.equals("left")) {
      return keyword.rot_left_k;
    } else if (ss.equals("right")) {
      return keyword.rot_right_k;
    } else if (ss.equals("down")) {
      return keyword.rot_upside_down_k;
    }

    return keyword.none_k;
  }

  static keyword consume_keyword(StringBuilder ssb) {
    String ss = new String(ssb).toLowerCase();
    // cat range keywords
    if (ss.startsWith("even")) { // note: strncmp
      ssb.delete(0, 4);
      return keyword.even_k;
    } else if (ss.startsWith("odd")) { // note: strncmp
      ssb.delete(0, 3);
      return keyword.odd_k;
    } else {
      ssb.setLength(0);
      return is_keyword(ss);
    }
  }
};
