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

import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.text.WordUtils;
import pdftk.com.lowagie.text.DocumentException;
import pdftk.com.lowagie.text.pdf.FdfWriter;
import pdftk.com.lowagie.text.pdf.PdfDictionary;
import pdftk.com.lowagie.text.pdf.PdfName;
import pdftk.com.lowagie.text.pdf.PdfNumber;
import pdftk.com.lowagie.text.pdf.PdfReader;
import pdftk.com.lowagie.text.pdf.PdfWriter;

class TK_Session {

  boolean m_valid_b = false;
  boolean m_authorized_b = true;
  boolean m_input_pdf_readers_opened_b = false; // have m_input_pdf readers been opened?
  boolean m_verbose_reporting_b = false;
  boolean m_ask_about_warnings_b = pdftk.ASK_ABOUT_WARNINGS; // set default at compile-time

  static final String creator = "pdftk-java " + pdftk.PDFTK_VER;

  // pack input PDF in the order they're given on the command line
  ArrayList<InputPdf> m_input_pdf = new ArrayList<InputPdf>();
  // typedef vector< InputPdf >::size_type InputPdfIndex;

  // store input PDF handles here
  HashMap<String, Integer> m_input_pdf_index = new HashMap<String, Integer>();

  InputPdf.PagesReader add_reader(InputPdf input_pdf_p, boolean keep_artifacts_b) {
    InputPdf.PagesReader ret_val = input_pdf_p.add_reader(keep_artifacts_b, m_ask_about_warnings_b);
    // update session state
    m_authorized_b = m_authorized_b && input_pdf_p.m_authorized_b;
    return ret_val;
  }

  InputPdf.PagesReader add_reader(InputPdf input_pdf_p) {
    return add_reader(input_pdf_p, false);
  }

  boolean open_input_pdf_readers() {
    // try opening the input files and init m_input_pdf readers
    boolean open_success_b = true;

    if (!m_input_pdf_readers_opened_b) {
      if (m_operation == keyword.filter_k && m_input_pdf.size() == 1) {
        // don't touch input pdf -- preserve artifacts
        open_success_b = (add_reader(m_input_pdf.get(0), true) != null);
      } else {
        for (InputPdf it : m_input_pdf) {
          open_success_b = (add_reader(it) != null) && open_success_b;
        }
      }
      m_input_pdf_readers_opened_b = open_success_b;
    }

    return open_success_b;
  }

  ArrayList<String> m_input_attach_file_filename = new ArrayList<String>();
  int m_input_attach_file_pagenum = 0;
  String m_input_attach_file_relation = "Unspecified";

  String m_update_info_filename = "";
  boolean m_update_info_utf8_b = false;
  String m_update_xmp_filename = "";

  keyword m_operation = keyword.none_k;

  ArrayList<ArrayList<PageRef>> m_page_seq =
      new ArrayList<ArrayList<PageRef>>(); // one vector for each given page range

  String m_form_data_filename = "";
  String m_background_filename = "";
  String m_stamp_filename = "";
  String m_output_filename = "";
  boolean m_output_utf8_b = false;
  String m_output_owner_pw = "";
  String m_output_user_pw = "";
  int m_output_user_perms = 0;
  boolean m_multistamp_b = false; // use all pages of input stamp PDF, not just the first
  boolean m_multibackground_b = false; // use all pages of input background PDF, not just the first
  boolean m_output_uncompress_b = false;
  boolean m_output_compress_b = false;
  boolean m_output_flatten_b = false;
  boolean m_output_need_appearances_b = false;
  boolean m_output_drop_xfa_b = false;
  boolean m_output_drop_xmp_b = false;
  boolean m_output_keep_first_id_b = false;
  boolean m_output_keep_final_id_b = false;
  boolean m_cat_full_pdfs_b = true; // we are merging entire docs, not select pages

  enum encryption_strength {
    none_enc(PdfWriter.INVALID_ENCRYPTION, PdfWriter.VERSION_1_2), // 1.1 probably okay, here
    rc4_40_enc(PdfWriter.STANDARD_ENCRYPTION_40, PdfWriter.VERSION_1_3), // 1.1 probably okay, here
    rc4_128_enc(PdfWriter.STANDARD_ENCRYPTION_128, PdfWriter.VERSION_1_4),
    aes128_enc(PdfWriter.ENCRYPTION_AES_128, PdfWriter.VERSION_1_6);
    final int value;
    final char pdf_version;

    encryption_strength(int value, char pdf_version) {
      this.value = value;
      this.pdf_version = pdf_version;
    }
  };

  encryption_strength m_output_encryption_strength = encryption_strength.none_enc;

  byte[] m_output_owner_pw_pdfdoc = new byte[0];
  byte[] m_output_user_pw_pdfdoc = new byte[0];

  void parse(String[] args) {
    ArgState arg_state = ArgState.input_files_e;

    // set one and only one to true when p/w used; use to
    // enforce rule that either all p/w use handles or no p/w use handles
    boolean password_using_handles_not_b = false;
    boolean password_using_handles_b = false;

    int password_input_pdf_index = 0;

    boolean fail_b = false;

    // first, look for our "dont_ask" or "do_ask" keywords, since this
    // setting must be known before we begin opening documents, etc.
    for (String argv : args) {
      keyword kw = keyword.is_keyword(argv);
      if (kw == keyword.dont_ask_k) {
        m_ask_about_warnings_b = false;
      } else if (kw == keyword.do_ask_k) {
        m_ask_about_warnings_b = true;
      }
    }

    // iterate over cmd line arguments
    for (String argv : args) {

      if (fail_b || arg_state == ArgState.done_e) break;
      keyword arg_keyword = keyword.is_keyword(argv);

      // these keywords can be false hits because of their loose matching requirements;
      // since they are suffixes to page ranges, their appearance here is most likely a false match;
      if (arg_keyword == keyword.even_k || arg_keyword == keyword.odd_k) {
        arg_keyword = keyword.none_k;
      }

      switch (arg_state) {
        case input_files_e:
        case input_pw_e:
          {
            // look for keywords that would advance our state,
            // and then handle the specifics of the above cases

            if (arg_keyword == keyword.input_pw_k) { // input PDF passwords keyword

              arg_state = ArgState.input_pw_e;
            } else if (arg_keyword == keyword.cat_k) {
              m_operation = keyword.cat_k;
              arg_state = ArgState.page_seq_e; // collect page sequeces
            } else if (arg_keyword == keyword.shuffle_k) {
              m_operation = keyword.shuffle_k;
              arg_state = ArgState.page_seq_e; // collect page sequeces
            } else if (arg_keyword == keyword.burst_k) {
              m_operation = keyword.burst_k;
              arg_state = ArgState.output_args_e; // makes "output <fn>" bit optional
            } else if (arg_keyword == keyword.filter_k) {
              m_operation = keyword.filter_k;
              arg_state = ArgState.output_e; // look for an output filename
            } else if (arg_keyword == keyword.dump_data_k) {
              m_operation = keyword.dump_data_k;
              arg_state = ArgState.output_e;
            } else if (arg_keyword == keyword.dump_data_utf8_k) {
              m_operation = keyword.dump_data_k;
              m_output_utf8_b = true;
              arg_state = ArgState.output_e;
            } else if (arg_keyword == keyword.dump_data_fields_k) {
              m_operation = keyword.dump_data_fields_k;
              arg_state = ArgState.output_e;
            } else if (arg_keyword == keyword.dump_data_fields_utf8_k) {
              m_operation = keyword.dump_data_fields_k;
              m_output_utf8_b = true;
              arg_state = ArgState.output_e;
            } else if (arg_keyword == keyword.dump_data_k) {
              m_operation = keyword.dump_data_k;
              arg_state = ArgState.output_e;
            } else if (arg_keyword == keyword.dump_data_annots_k) {
              m_operation = keyword.dump_data_annots_k;
              arg_state = ArgState.output_e;
            } else if (arg_keyword == keyword.generate_fdf_k) {
              m_operation = keyword.generate_fdf_k;
              m_output_utf8_b = true;
              arg_state = ArgState.output_e;
            } else if (arg_keyword == keyword.fill_form_k) {
              m_operation = keyword.filter_k;
              arg_state = ArgState.form_data_filename_e; // look for an FDF filename
            } else if (arg_keyword == keyword.attach_file_k) {
              m_operation = keyword.filter_k;
              arg_state = ArgState.attach_file_filename_e;
            } else if (arg_keyword == keyword.attach_file_to_page_k) {
              arg_state = ArgState.attach_file_pagenum_e;
            } else if (arg_keyword == keyword.attach_file_relation_k) {
              arg_state = ArgState.attach_file_relation_e;
            } else if (arg_keyword == keyword.unpack_files_k) {
              m_operation = keyword.unpack_files_k;
              arg_state = ArgState.output_e;
            } else if (arg_keyword == keyword.update_info_k) {
              m_operation = keyword.filter_k;
              m_update_info_utf8_b = false;
              arg_state = ArgState.update_info_filename_e;
            } else if (arg_keyword == keyword.update_info_utf8_k) {
              m_operation = keyword.filter_k;
              m_update_info_utf8_b = true;
              arg_state = ArgState.update_info_filename_e;
            }
            /*
            else if( arg_keyword== update_xmp_k ) {
              m_operation= filter_k;
              arg_state= update_xmp_filename_e;
            }
            */
            else if (arg_keyword == keyword.background_k) {
              m_operation = keyword.filter_k;
              arg_state = ArgState.background_filename_e;
            } else if (arg_keyword == keyword.multibackground_k) {
              m_operation = keyword.filter_k;
              m_multibackground_b = true;
              arg_state = ArgState.background_filename_e;
            } else if (arg_keyword == keyword.stamp_k) {
              m_operation = keyword.filter_k;
              arg_state = ArgState.stamp_filename_e;
            } else if (arg_keyword == keyword.multistamp_k) {
              m_operation = keyword.filter_k;
              m_multistamp_b = true;
              arg_state = ArgState.stamp_filename_e;
            } else if (arg_keyword == keyword.rotate_k) {
              m_operation = keyword.filter_k;
              arg_state = ArgState.page_seq_e; // collect page sequeces
            } else if (arg_keyword == keyword.output_k) { // we reached the output section
              arg_state = ArgState.output_filename_e;
            } else if (arg_keyword == keyword.none_k) {
              // here is where the two cases (input_files_e, input_pw_e) diverge

              String handle, data;
              {
                Pattern p = Pattern.compile("(?:([A-Z]+)=)?(.*)", Pattern.DOTALL);
                Matcher m = p.matcher(argv);
                m.matches();
                handle = m.group(1);
                data = m.group(2);
              }

              if (arg_state == ArgState.input_files_e) {
                // input_files_e:
                // expecting input handle=filename pairs, or
                // an input filename w/o a handle
                //
                // treat argv[ii] like an optional input handle and filename
                // like this: [<handle>=]<filename>

                InputPdf input_pdf = new InputPdf();
                input_pdf.m_filename = data;

                if (handle == null) { // no handle
                  m_input_pdf.add(input_pdf);
                } else { // use given handle for filename; test, first

                  // look up handle
                  Integer it = m_input_pdf_index.get(handle);
                  if (it != null) { // error: alreay in use

                    System.err.println("Error: Handle given here: ");
                    System.err.println("      " + argv);
                    System.err.println("   is already associated with: ");
                    System.err.println("      " + m_input_pdf.get(it).m_filename);
                    System.err.println("   Exiting.");
                    fail_b = true;
                  } else { // add handle/filename association
                    m_input_pdf.add(input_pdf);
                    m_input_pdf_index.put(handle, m_input_pdf.size() - 1);
                  }
                }
              } // end: arg_state== input_files_e
              else if (arg_state == ArgState.input_pw_e) {
                // expecting input handle=password pairs, or
                // an input PDF password w/o a handle
                //
                // treat argv[ii] like an input handle and password
                // like this <handle>=<password>; if no handle is
                // given, assign passwords to input in order;

                // if handles not used for input PDFs, then assume
                // any equals signs found in p/w are part of p/w
                if (m_input_pdf_index.size() == 0) {
                  handle = null;
                  data = argv;
                }

                if (handle == null) { // no equal sign; try using default handles
                  if (password_using_handles_b) { // error: expected a handle

                    System.err.println("Error: Expected a user-supplied handle for this input");
                    System.err.println("   PDF password: " + argv);
                    System.err.println();
                    System.err.println("   Handles must be supplied with ~all~ input");
                    System.err.println("   PDF passwords, or with ~no~ input PDF passwords.");
                    System.err.println("   If no handles are supplied, then passwords are applied");
                    System.err.println("   according to input PDF order.");
                    System.err.println();
                    System.err.println("   Handles are given like this: <handle>=<password>, and");
                    System.err.println("   they must be one or more upper-case letters.");
                    fail_b = true;
                  } else {
                    password_using_handles_not_b = true;

                    if (password_input_pdf_index < m_input_pdf.size()) {
                      m_input_pdf.get(password_input_pdf_index).m_password = argv;
                      ++password_input_pdf_index;
                    } else { // error
                      System.err.println("Error: more input passwords than input PDF documents.");
                      System.err.println("   Exiting.");
                      fail_b = true;
                    }
                  }
                } else { // handle given; use for password
                  if (password_using_handles_not_b) { // error; remark and set fail_b

                    System.err.println("Error: Expected ~no~ user-supplied handle for this input");
                    System.err.println("   PDF password: " + argv);
                    System.err.println();
                    System.err.println("   Handles must be supplied with ~all~ input");
                    System.err.println("   PDF passwords, or with ~no~ input PDF passwords.");
                    System.err.println("   If no handles are supplied, then passwords are applied");
                    System.err.println("   according to input PDF order.");
                    System.err.println();
                    System.err.println("   Handles are given like this: <handle>=<password>, and");
                    System.err.println("   they must be one or more upper-case letters.");
                    fail_b = true;
                  } else {
                    password_using_handles_b = true;

                    // look up this handle
                    Integer it = m_input_pdf_index.get(handle);
                    if (it != null) { // found

                      if (m_input_pdf.get(it).m_password.isEmpty()) {
                        m_input_pdf.get(it).m_password = data; // set
                      } else { // error: password already given

                        System.err.println("Error: Handle given here: ");
                        System.err.println("      " + argv);
                        System.err.println("   is already associated with this password: ");
                        System.err.println("      " + m_input_pdf.get(it).m_password);
                        System.err.println("   Exiting.");
                        fail_b = true;
                      }
                    } else { // error: no input file matches this handle

                      System.err.println("Error: Password handle: " + argv);
                      System.err.println("   is not associated with an input PDF file.");
                      System.err.println("   Exiting.");
                      fail_b = true;
                    }
                  }
                }
              } // end: arg_state== input_pw_e
              else { // error
                System.err.println("Error: Internal error: unexpected arg_state.  Exiting.");
                fail_b = true;
              }
            } else { // error: unexpected keyword; remark and set fail_b
              System.err.println("Error: Unexpected command-line data: ");
              System.err.println("      " + argv);
              if (arg_state == ArgState.input_files_e) {
                System.err.println("   where we were expecting an input PDF filename,");
                System.err.println("   operation (e.g. \"cat\") or \"input_pw\".  Exiting.");
              } else {
                System.err.println("   where we were expecting an input PDF password");
                System.err.println("   or operation (e.g. \"cat\").  Exiting.");
              }
              fail_b = true;
            }
          }
          break;

        case page_seq_e:
          {
            if (m_page_seq.isEmpty()) {
              // we just got here; validate input filenames

              if (m_input_pdf.isEmpty()) { // error; remark and set fail_b
                System.err.println("Error: No input files.  Exiting.");
                fail_b = true;
                break;
              }

              // try opening input PDF readers
              if (!open_input_pdf_readers()) { // failure
                fail_b = true;
                break;
              }
            } // end: first pass init. pdf files

            if (arg_keyword == keyword.output_k) {
              arg_state = ArgState.output_filename_e; // advance state
            } else if (arg_keyword == keyword.none_k) { // treat argv[ii] like a page sequence

              boolean even_pages_b = false;
              boolean odd_pages_b = false;

              Pattern p =
                  Pattern.compile(
                      "([A-Z]*)(r?)(end|[0-9]*)(-(r?)(end|[0-9]*))?(.*)", Pattern.DOTALL);
              Matcher m = p.matcher(argv);
              m.matches();
              String handle = m.group(1);
              String pre_reverse = m.group(2);
              String pre_range = m.group(3);
              String hyphen = m.group(4);
              String post_reverse = m.group(5);
              String post_range = m.group(6);
              String keywords = m.group(7);

              int range_pdf_index = 0;
              { // defaults to first input document
                if (!handle.isEmpty()) {
                  // validate handle
                  Integer it = m_input_pdf_index.get(handle);
                  if (it == null) { // error

                    System.err.println("Error: Given handle has no associated file: ");
                    System.err.println("   " + handle + ", used here: " + argv);
                    System.err.println("   Exiting.");
                    fail_b = true;
                    break;
                  } else {
                    range_pdf_index = it;
                  }
                }
              }

              PageRange page_num =
                  new PageRange(m_input_pdf.get(range_pdf_index).m_num_pages, argv);
              if (!page_num.parse(pre_reverse, pre_range, post_reverse, post_range)) {
                fail_b = true;
                break;
              }

              // DF declare rotate vars
              PageRotate page_rotate = PageRotate.NORTH;
              boolean page_rotate_absolute = false;

              StringBuilder trailing_keywords = new StringBuilder(keywords);
              // trailing keywords (excluding "end" which should have been handled above)
              while (trailing_keywords.length()
                  > 0) { // possibly more than one keyword, e.g., 3-endevenwest

                // read keyword
                arg_keyword = keyword.consume_keyword(trailing_keywords);

                if (arg_keyword == keyword.even_k) {
                  even_pages_b = true;
                } else if (arg_keyword == keyword.odd_k) {
                  odd_pages_b = true;
                } else if (arg_keyword == keyword.rot_north_k) {
                  page_rotate = PageRotate.NORTH; // rotate 0
                  page_rotate_absolute = true;
                } else if (arg_keyword == keyword.rot_east_k) {
                  page_rotate = PageRotate.EAST; // rotate 90
                  page_rotate_absolute = true;
                } else if (arg_keyword == keyword.rot_south_k) {
                  page_rotate = PageRotate.SOUTH; // rotate 180
                  page_rotate_absolute = true;
                } else if (arg_keyword == keyword.rot_west_k) {
                  page_rotate = PageRotate.WEST; // rotate 270
                  page_rotate_absolute = true;
                } else if (arg_keyword == keyword.rot_left_k) {
                  page_rotate = PageRotate.WEST; // rotate -90
                  page_rotate_absolute = false;
                } else if (arg_keyword == keyword.rot_right_k) {
                  page_rotate = PageRotate.EAST; // rotate +90
                  page_rotate_absolute = false;
                } else if (arg_keyword == keyword.rot_upside_down_k) {
                  page_rotate = PageRotate.SOUTH; // rotate +180
                  page_rotate_absolute = false;
                } else { // error
                  System.err.println("Error: Unexpected text in page range end, here: ");
                  System.err.println("   " + argv /*(argv[ii]+ jj)*/);
                  System.err.println("   Exiting.");
                  System.err.println("   Acceptable keywords, for example: \"even\" or \"odd\".");
                  System.err.println("   To rotate pages, use: \"north\" \"south\" \"east\"");
                  System.err.println("       \"west\" \"left\" \"right\" or \"down\"");
                  fail_b = true;
                  break;
                }
              }

              ////
              // pack this range into our m_page_seq;

              if (page_num.beg == 0 && page_num.end == 0) { // ref the entire document
                page_num.beg = 1;
                page_num.end = m_input_pdf.get(range_pdf_index).m_num_pages;

                // test that it's a /full/ pdf
                m_cat_full_pdfs_b = m_cat_full_pdfs_b && (!even_pages_b && !odd_pages_b);
              } else if (page_num.beg == 0 || page_num.end == 0) { // error
                System.err.println("Error: Input page numbers include 0 (zero)");
                System.err.println("   The first PDF page is 1 (one)");
                System.err.println("   Exiting.");
                fail_b = true;
                break;
              } else // the user specified select pages
              m_cat_full_pdfs_b = false;

              ArrayList<PageRef> temp_page_seq = new ArrayList<PageRef>();
              boolean reverse_sequence_b = (page_num.end < page_num.beg);
              if (reverse_sequence_b) { // swap
                int temp = page_num.end;
                page_num.end = page_num.beg;
                page_num.beg = temp;
              }

              for (int kk = page_num.beg; kk <= page_num.end; ++kk) {
                if ((!even_pages_b || ((kk % 2) == 0)) && (!odd_pages_b || ((kk % 2) == 1))) {
                  if (kk <= m_input_pdf.get(range_pdf_index).m_num_pages) {

                    // look to see if this page of this document
                    // has already been referenced; if it has,
                    // create a new reader; associate this page
                    // with a reader;
                    //
                    boolean associated = false;
                    for (InputPdf.PagesReader it : m_input_pdf.get(range_pdf_index).m_readers) {
                      if (!it.first.contains(kk)) { // kk not assoc. w/ this reader
                        it.first.add(kk); // create association
                        associated = true;
                        break;
                      }
                    }
                    //
                    if (!associated) {
                      // need to create a new reader for kk
                      InputPdf.PagesReader new_reader =
                          add_reader(m_input_pdf.get(range_pdf_index));
                      if (new_reader != null) {
                        new_reader.first.add(kk);
                      } else {
                        System.err.println("Internal Error: unable to add reader");
                        fail_b = true;
                        break;
                      }
                    }

                    //
                    temp_page_seq.add(
                        new PageRef(
                            range_pdf_index, kk, page_rotate, page_rotate_absolute)); // DF rotate

                  } else { // error; break later to get most feedback
                    System.err.println("Error: Page number: " + kk);
                    System.err.println(
                        "   does not exist in file: "
                            + m_input_pdf.get(range_pdf_index).m_filename);
                    fail_b = true;
                  }
                }
              }
              if (fail_b) break;

              if (reverse_sequence_b) {
                Collections.reverse(temp_page_seq);
              }

              m_page_seq.add(temp_page_seq);

            } else { // error
              System.err.println("Error: expecting page ranges.  Instead, I got:");
              System.err.println("   " + argv);
              fail_b = true;
              break;
            }
          }
          break;

        case form_data_filename_e:
          {
            if (arg_keyword == keyword.none_k) { // treat argv[ii] like an FDF file filename

              if (m_form_data_filename.isEmpty()) {
                m_form_data_filename = argv;
              } else { // error
                System.err.println("Error: Multiple fill_form filenames given: ");
                System.err.println("   " + m_form_data_filename + " and " + argv);
                System.err.println("Exiting.");
                fail_b = true;
                break;
              }

              // advance state
              arg_state = ArgState.output_e; // look for an output filename
            } else { // error
              System.err.println("Error: expecting a form data filename,");
              System.err.println("   instead I got this keyword: " + argv);
              System.err.println("Exiting.");
              fail_b = true;
              break;
            }
          } // end: case form_data_filename_e
          break;

        case attach_file_filename_e:
          {
            // keep packing filenames until we reach an expected keyword

            if (arg_keyword == keyword.attach_file_to_page_k) {
              arg_state = ArgState.attach_file_pagenum_e; // advance state
            } else if (arg_keyword == keyword.attach_file_relation_k) {
              arg_state = ArgState.attach_file_relation_e; // advance state
            } else if (arg_keyword == keyword.output_k) {
              arg_state = ArgState.output_filename_e; // advance state
            } else if (arg_keyword == keyword.none_k) {
              // pack argv[ii] into our list of attachment filenames
              m_input_attach_file_filename.add(argv);
            } else { // error
              System.err.println("Error: expecting an attachment filename,");
              System.err.println("   instead I got this keyword: " + argv);
              System.err.println("Exiting.");
              fail_b = true;
              break;
            }
          }
          break;

        case attach_file_pagenum_e:
          {
            if (argv.equals("PROMPT")) { // query the user, later
              m_input_attach_file_pagenum = -1;
            } else if (argv.equals("end")) { // attach to the final page
              m_input_attach_file_pagenum = -2;
            } else {
              try {
                m_input_attach_file_pagenum = Integer.parseInt(argv);
              } catch (NumberFormatException e) { // error
                System.err.println("Error: expecting a (1-based) page number.  Instead, I got:");
                System.err.println("   " + argv);
                System.err.println("Exiting.");
                fail_b = true;
              }
            }

            // advance state
            arg_state = ArgState.output_e; // look for an output filename
          } // end: case attach_file_pagenum_e
          break;

        case attach_file_relation_e:
          {
            if (argv.matches("(?i)Source|Data|Alternative|Supplement|Unspecified")) {
              argv = WordUtils.capitalizeFully(argv);
            } else {
              System.err.println("Warning: non-standard attachment relationship: " + argv + ".");
            }
            m_input_attach_file_relation = argv;

            // advance state
            arg_state = ArgState.output_e; // look for an output filename
          }
          break;

        case update_info_filename_e:
          {
            if (arg_keyword == keyword.none_k) {
              if (m_update_info_filename.isEmpty()) {
                m_update_info_filename = argv;
              } else { // error
                System.err.println("Error: Multiple update_info filenames given: ");
                System.err.println("   " + m_update_info_filename + " and " + argv);
                System.err.println("Exiting.");
                fail_b = true;
                break;
              }
            } else { // error
              System.err.println("Error: expecting an INFO file filename,");
              System.err.println("   instead I got this keyword: " + argv);
              System.err.println("Exiting.");
              fail_b = true;
              break;
            }

            // advance state
            arg_state = ArgState.output_e; // look for an output filename
          } // end: case update_info_filename_e
          break;

          /*
          case update_xmp_filename_e : {
            if( arg_keyword== none_k ) {
                if( m_update_xmp_filename.empty() ) {
                  m_update_xmp_filename= argv[ii];
                }
                else { // error
                  cerr << "Error: Multiple update_xmp filenames given: " << endl;
                  cerr << "   " << m_update_xmp_filename << " and " << argv[ii] << endl;
                  cerr << "Exiting." << endl;
                  fail_b= true;
                  break;
                }
              }
            else { // error
              cerr << "Error: expecting an XMP file filename," << endl;
              cerr << "   instead I got this keyword: " << argv[ii] << endl;
              cerr << "Exiting." << endl;
              fail_b= true;
              break;
            }

            // advance state
            arg_state= output_e; // look for an output filename

          } // end: case update_xmp_filename_e
          break;
          */

        case output_e:
          {
            if (m_input_pdf.isEmpty()) { // error; remark and set fail_b
              System.err.println("Error: No input files.  Exiting.");
              fail_b = true;
              break;
            }

            if (arg_keyword == keyword.output_k) {
              arg_state = ArgState.output_filename_e; // advance state
            } else { // error
              System.err.println("Error: expecting \"output\" keyword.  Instead, I got:");
              System.err.println("   " + argv);
              fail_b = true;
              break;
            }
          }
          break;

        case output_filename_e:
          {
            // we have closed all possible input operations and arguments;
            // see if we should perform any default action based on the input state
            //
            if (m_operation == keyword.none_k) {
              if (1 < m_input_pdf.size()) {
                // no operation given for multiple input PDF, so combine them
                m_operation = keyword.cat_k;
              } else {
                m_operation = keyword.filter_k;
              }
            }

            // try opening input PDF readers (in case they aren't already)
            if (!open_input_pdf_readers()) { // failure
              fail_b = true;
              break;
            }

            if ((m_operation == keyword.cat_k || m_operation == keyword.shuffle_k)) {
              if (m_page_seq.isEmpty()) {
                // combining pages, but no sequences given; merge all input PDFs in order
                for (int ii = 0; ii < m_input_pdf.size(); ++ii) {
                  InputPdf input_pdf = m_input_pdf.get(ii);

                  ArrayList<PageRef> temp_page_seq = new ArrayList<PageRef>();
                  for (int jj = 1; jj <= input_pdf.m_num_pages; ++jj) {
                    temp_page_seq.add(new PageRef(ii, jj)); // DF rotate
                    input_pdf
                        .m_readers
                        .get(input_pdf.m_readers.size() - 1)
                        .first
                        .add(jj); // create association
                  }
                  m_page_seq.add(temp_page_seq);
                }
              }
              /* no longer necessary -- are upstream testing is smarter
              else { // page ranges or docs (e.g. A B A) were given
                m_cat_full_pdfs_b= false; // TODO: handle cat A B A case for bookmarks
              }
              */
            }

            if (m_output_filename.isEmpty()) {
              m_output_filename = argv;

              if (!m_output_filename.equals(
                  "-")) { // input and output may both be "-" (stdin and stdout)
                // simple-minded test to see if output matches an input filename
                for (InputPdf it : m_input_pdf) {
                  if (it.m_filename.equals(m_output_filename)) {
                    System.err.println("Error: The given output filename: " + m_output_filename);
                    System.err.println("   matches an input filename.  Exiting.");
                    fail_b = true;
                    break;
                  }
                }
              }
            } else { // error
              System.err.println("Error: Multiple output filenames given: ");
              System.err.println("   " + m_output_filename + " and " + argv);
              System.err.println("Exiting.");
              fail_b = true;
              break;
            }

            // advance state
            arg_state = ArgState.output_args_e;
          }
          break;

        case output_args_e:
          {
            // output args are order-independent but must follow "output <fn>", if present;
            // we are expecting any of these keywords:
            // owner_pw_k, user_pw_k, user_perms_k ...
            // added output_k case in pdftk 1.10; this permits softer "output <fn>" enforcement
            //

            ArgStateMutable arg_state_m = new ArgStateMutable();
            arg_state_m.value = arg_state;
            if (handle_some_output_options(arg_keyword, arg_state_m)) {
              arg_state = arg_state_m.value;
            } else {
              System.err.println("Error: Unexpected data in output section: ");
              System.err.println("      " + argv);
              System.err.println("Exiting.");
              fail_b = true;
              break;
            }
          }
          break;

        case output_owner_pw_e:
          {
            if (m_output_owner_pw.isEmpty()) {

              if (argv.equals("PROMPT") || !argv.equals(m_output_user_pw)) {
                m_output_owner_pw = argv;
              } else { // error: identical user and owner password
                // are interpreted by Acrobat (per the spec.) that
                // the doc has no owner password
                System.err.println("Error: The user and owner passwords are the same.");
                System.err.println("   PDF Viewers interpret this to mean your PDF has");
                System.err.println("   no owner password, so they must be different.");
                System.err.println("   Or, supply no owner password to pdftk if this is");
                System.err.println("   what you desire.");
                System.err.println("Exiting.");
                fail_b = true;
                break;
              }
            } else { // error: we already have an output owner pw
              System.err.println("Error: Multiple output owner passwords given: ");
              System.err.println("   " + m_output_owner_pw + " and " + argv);
              System.err.println("Exiting.");
              fail_b = true;
              break;
            }

            // revert state
            arg_state = ArgState.output_args_e;
          }
          break;

        case output_user_pw_e:
          {
            if (m_output_user_pw.isEmpty()) {
              if (argv.equals("PROMPT") || !m_output_owner_pw.equals(argv)) {
                m_output_user_pw = argv;
              } else { // error: identical user and owner password
                // are interpreted by Acrobat (per the spec.) that
                // the doc has no owner password
                System.err.println("Error: The user and owner passwords are the same.");
                System.err.println("   PDF Viewers interpret this to mean your PDF has");
                System.err.println("   no owner password, so they must be different.");
                System.err.println("   Or, supply no owner password to pdftk if this is");
                System.err.println("   what you desire.");
                System.err.println("Exiting.");
                fail_b = true;
                break;
              }
            } else { // error: we already have an output user pw
              System.err.println("Error: Multiple output user passwords given: ");
              System.err.println("   " + m_output_user_pw + " and " + argv);
              System.err.println("Exiting.");
              fail_b = true;
              break;
            }

            // revert state
            arg_state = ArgState.output_args_e;
          }
          break;

        case output_user_perms_e:
          {

            // we may be given any number of permission arguments,
            // so keep an eye out for other, state-altering keywords
            ArgStateMutable arg_state_m = new ArgStateMutable();
            arg_state_m.value = arg_state;
            if (handle_some_output_options(arg_keyword, arg_state_m)) {
              arg_state = arg_state_m.value;
              break;
            }

            switch (arg_keyword) {

                // possible permissions
              case perm_printing_k:
                // if both perm_printing_k and perm_degraded_printing_k
                // are given, then perm_printing_k wins;
                m_output_user_perms |= PdfWriter.AllowPrinting;
                break;
              case perm_modify_contents_k:
                // Acrobat 5 and 6 don't set both bits, even though
                // they both respect AllowModifyContents --> AllowAssembly;
                // so, no harm in this;
                m_output_user_perms |= (PdfWriter.AllowModifyContents | PdfWriter.AllowAssembly);
                break;
              case perm_copy_contents_k:
                // Acrobat 5 _does_ allow the user to allow copying contents
                // yet hold back screen reader perms; this is counter-intuitive,
                // and Acrobat 6 does not allow Copy w/o SceenReaders;
                m_output_user_perms |= (PdfWriter.AllowCopy | PdfWriter.AllowScreenReaders);
                break;
              case perm_modify_annotations_k:
                m_output_user_perms |= (PdfWriter.AllowModifyAnnotations | PdfWriter.AllowFillIn);
                break;
              case perm_fillin_k:
                m_output_user_perms |= PdfWriter.AllowFillIn;
                break;
              case perm_screen_readers_k:
                m_output_user_perms |= PdfWriter.AllowScreenReaders;
                break;
              case perm_assembly_k:
                m_output_user_perms |= PdfWriter.AllowAssembly;
                break;
              case perm_degraded_printing_k:
                m_output_user_perms |= PdfWriter.AllowDegradedPrinting;
                break;
              case perm_all_k:
                m_output_user_perms =
                    (PdfWriter.AllowPrinting
                        | // top quality printing
                        PdfWriter.AllowModifyContents
                        | PdfWriter.AllowCopy
                        | PdfWriter.AllowModifyAnnotations
                        | PdfWriter.AllowFillIn
                        | PdfWriter.AllowScreenReaders
                        | PdfWriter.AllowAssembly);
                break;

              default: // error: unexpected matter
                System.err.println("Error: Unexpected data in output section: ");
                System.err.println("      " + argv);
                System.err.println("Exiting.");
                fail_b = true;
                break;
            }
          }
          break;

        case background_filename_e:
          {
            if (arg_keyword == keyword.none_k) {
              if (m_background_filename.isEmpty()) {
                m_background_filename = argv;
              } else { // error
                System.err.println("Error: Multiple background filenames given: ");
                System.err.println("   " + m_background_filename + " and " + argv);
                System.err.println("Exiting.");
                fail_b = true;
                break;
              }
            } else { // error
              System.err.println("Error: expecting a PDF filename for background operation,");
              System.err.println("   instead I got this keyword: " + argv);
              System.err.println("Exiting.");
              fail_b = true;
              break;
            }

            // revert state
            // this is more liberal than used with other operations, since we want
            // to preserve backward-compatibility with pdftk 1.00 where "background"
            // was documented as an output option; in pdftk 1.10 we changed it to
            // an operation
            arg_state = ArgState.output_args_e;
          }
          break;

        case stamp_filename_e:
          {
            if (arg_keyword == keyword.none_k) {
              if (m_stamp_filename.isEmpty()) {
                m_stamp_filename = argv;
              } else { // error
                System.err.println("Error: Multiple stamp filenames given: ");
                System.err.println("   " + m_stamp_filename + " and " + argv);
                System.err.println("Exiting.");
                fail_b = true;
                break;
              }
            } else { // error
              System.err.println("Error: expecting a PDF filename for stamp operation,");
              System.err.println("   instead I got this keyword: " + argv);
              System.err.println("Exiting.");
              fail_b = true;
              break;
            }

            // advance state
            arg_state = ArgState.output_e; // look for an output filename
          }
          break;

        default:
          { // error
            System.err.println("Internal Error: Unexpected arg_state.  Exiting.");
            fail_b = true;
            break;
          }
      } // end: switch(arg_state)
    } // end: iterate over command-line arguments

    if (fail_b) {
      System.err.println("Errors encountered.  No output created.");
      m_valid_b = false;

      m_input_pdf.clear();

      // preserve other data members for diagnostic dump
    } else {
      m_valid_b = true;

      if (!m_input_pdf_readers_opened_b) {
        open_input_pdf_readers();
      }
    }
  }

  boolean is_valid() {
    return (m_valid_b
        && (m_operation == keyword.dump_data_k
            || m_operation == keyword.dump_data_fields_k
            || m_operation == keyword.dump_data_annots_k
            || m_operation == keyword.generate_fdf_k
            || m_authorized_b)
        && !m_input_pdf.isEmpty()
        && m_input_pdf_readers_opened_b
        && (m_operation == keyword.cat_k
            || m_operation == keyword.shuffle_k
            || m_operation == keyword.burst_k
            || m_operation == keyword.filter_k
            || m_operation == keyword.dump_data_k
            || m_operation == keyword.dump_data_utf8_k
            || m_operation == keyword.dump_data_fields_k
            || m_operation == keyword.dump_data_fields_utf8_k
            || m_operation == keyword.dump_data_annots_k
            || m_operation == keyword.generate_fdf_k
            || m_operation == keyword.unpack_files_k)
        &&

        // these op.s require a single input PDF file
        (!(m_operation == keyword.burst_k || m_operation == keyword.filter_k)
            || (m_input_pdf.size() == 1))
        &&

        // these op.s do not require an output filename
        (m_operation == keyword.burst_k
            || m_operation == keyword.dump_data_k
            || m_operation == keyword.dump_data_fields_k
            || m_operation == keyword.dump_data_annots_k
            || m_operation == keyword.generate_fdf_k
            || m_operation == keyword.unpack_files_k
            || !m_output_filename.isEmpty()));
  }

  void dump_session_data() {
    if (!m_verbose_reporting_b) return;

    if (!m_input_pdf_readers_opened_b) {
      System.out.println("Input PDF Open Errors");
      return;
    }

    //
    if (is_valid()) {
      System.out.println("Command Line Data is valid.");
    } else {
      System.out.println("Command Line Data is NOT valid.");
    }

    // input files
    System.out.println();
    System.out.println("Input PDF Filenames & Passwords in Order\n( <filename>[, <password>] ) ");
    if (m_input_pdf.isEmpty()) {
      System.out.println("   No input PDF filenames have been given.");
    } else {
      for (InputPdf it : m_input_pdf) {
        System.out.print("   " + it.m_filename);
        if (!it.m_password.isEmpty()) {
          System.out.print(", " + it.m_password);
        }

        if (!it.m_authorized_b) {
          System.out.print(", OWNER OR USER PASSWORD REQUIRED, but not given (or incorrect)");
        }

        System.out.println();
      }
    }

    // operation
    System.out.println();
    System.out.println("The operation to be performed: ");
    switch (m_operation) {
      case cat_k:
        System.out.println("   cat - Catenate given page ranges into a new PDF.");
        break;
      case shuffle_k:
        System.out.println("   shuffle - Interleave given page ranges into a new PDF.");
        break;
      case burst_k:
        System.out.println("   burst - Split a single, input PDF into individual pages.");
        break;
      case filter_k:
        System.out.println(
            "   filter - Apply 'filters' to a single, input PDF based on output args.");
        System.out.println("      (When the operation is omitted, this is the default.)");
        break;
      case dump_data_k:
        System.out.println("   dump_data - Report statistics on a single, input PDF.");
        break;
      case dump_data_fields_k:
        System.out.println("   dump_data_fields - Report form field data on a single, input PDF.");
        break;
      case dump_data_annots_k:
        System.out.println("   dump_data_annots - Report annotation data on a single, input PDF.");
        break;
      case generate_fdf_k:
        System.out.println("   generate_fdf - Generate a dummy FDF file from a PDF.");
        break;
      case unpack_files_k:
        System.out.println("   unpack_files - Copy PDF file attachments into given directory.");
        break;
      case none_k:
        System.out.println("   NONE - No operation has been given.  See usage instructions.");
        break;
      default:
        System.out.println("   INTERNAL ERROR - An unexpected operation has been given.");
        break;
    }

    // pages
    /*
    cout << endl;
    cout << "The following pages will be operated on, in the given order." << endl;
    if( m_page_seq.empty() ) {
      cout << "   No pages or page ranges have been given." << endl;
    }
    else {
      for( vector< PageRef >::const_iterator it= m_page_seq.begin();
           it!= m_page_seq.end(); ++it )
        {
          map< string, InputPdf >::const_iterator jt=
            m_input_pdf.find( it->m_handle );
          if( jt!= m_input_pdf.end() ) {
            cout << "   Handle: " << it->m_handle << "  File: " << jt->second.m_filename;
            cout << "  Page: " << it->m_page_num << endl;
          }
          else { // error
            cout << "   Internal Error: handle not found in m_input_pdf: " << it->m_handle << endl;
          }
        }
    }
    */

    // output file; may be PDF or text
    System.out.println();
    System.out.println("The output file will be named:");
    if (m_output_filename.isEmpty()) {
      System.out.println("   No output filename has been given.");
    } else {
      System.out.println("   " + m_output_filename);
    }

    // output encryption
    System.out.println();
    boolean output_encrypted_b =
        m_output_encryption_strength != encryption_strength.none_enc
            || !m_output_user_pw.isEmpty()
            || !m_output_owner_pw.isEmpty();

    System.out.println("Output PDF encryption settings:");
    if (output_encrypted_b) {
      System.out.println("   Output PDF will be encrypted.");

      switch (m_output_encryption_strength) {
        case none_enc:
          System.out.println("   Encryption strength not given. Defaulting to: AES 128 bits.");
          break;
        case rc4_40_enc:
          System.out.println("   Given output encryption strength: AES 40 bits");
          break;
        case rc4_128_enc:
          System.out.println("   Given output encryption strength: RC4 128 bits");
          break;
        case aes128_enc:
          System.out.println("   Given output encryption strength: AES 128 bits");
      }

      System.out.println();
      {
        if (m_output_user_pw.isEmpty()) System.out.println("   No user password given.");
        else System.out.println("   Given user password: " + m_output_user_pw);
        if (m_output_owner_pw.isEmpty()) System.out.println("   No owner password given.");
        else System.out.println("   Given owner password: " + m_output_owner_pw);
        //
        // the printing section: Top Quality or Degraded, but not both;
        // AllowPrinting is a superset of both flag settings
        if ((m_output_user_perms & PdfWriter.AllowPrinting) == PdfWriter.AllowPrinting)
          System.out.println("   ALLOW Top Quality Printing");
        else if ((m_output_user_perms & PdfWriter.AllowPrinting) == PdfWriter.AllowDegradedPrinting)
          System.out.println("   ALLOW Degraded Printing (Top-Quality Printing NOT Allowed)");
        else System.out.println("   Printing NOT Allowed");
        if ((m_output_user_perms & PdfWriter.AllowModifyContents) == PdfWriter.AllowModifyContents)
          System.out.println("   ALLOW Modifying of Contents");
        else System.out.println("   Modifying of Contents NOT Allowed");
        if ((m_output_user_perms & PdfWriter.AllowCopy) == PdfWriter.AllowCopy)
          System.out.println("   ALLOW Copying of Contents");
        else System.out.println("   Copying of Contents NOT Allowed");
        if ((m_output_user_perms & PdfWriter.AllowModifyAnnotations)
            == PdfWriter.AllowModifyAnnotations)
          System.out.println("   ALLOW Modifying of Annotations");
        else System.out.println("   Modifying of Annotations NOT Allowed");
        if ((m_output_user_perms & PdfWriter.AllowFillIn) == PdfWriter.AllowFillIn)
          System.out.println("   ALLOW Fill-In");
        else System.out.println("   Fill-In NOT Allowed");
        if ((m_output_user_perms & PdfWriter.AllowScreenReaders) == PdfWriter.AllowScreenReaders)
          System.out.println("   ALLOW Screen Readers");
        else System.out.println("   Screen Readers NOT Allowed");
        if ((m_output_user_perms & PdfWriter.AllowAssembly) == PdfWriter.AllowAssembly)
          System.out.println("   ALLOW Assembly");
        else System.out.println("   Assembly NOT Allowed");
      }
    } else {
      System.out.println("   Output PDF will not be encrypted.");
    }

    // compression filter
    System.out.println();
    if (m_operation != keyword.filter_k
        || output_encrypted_b
        || !(m_output_compress_b || m_output_uncompress_b)) {
      System.out.println("No compression or uncompression being performed on output.");
    } else {
      if (m_output_compress_b) {
        System.out.println("Compression will be applied to some PDF streams.");
      } else {
        System.out.println("Some PDF streams will be uncompressed.");
      }
    }
  }

  static void apply_rotation_to_page(
      PdfReader reader_p, int page_num, int rotation, boolean absolute) {
    // DF rotate
    PdfDictionary page_p = reader_p.getPageN(page_num);
    if (!absolute) {
      rotation = reader_p.getPageRotation(page_num) + rotation;
    }
    rotation = rotation % 360;
    page_p.remove(PdfName.ROTATE);
    if (rotation != PageRotate.NORTH.value) { // default rotation
      page_p.put(PdfName.ROTATE, new PdfNumber(rotation));
    }
  }

  char prepare_writer(PdfWriter writer_p) throws DocumentException {
    // un/compress output streams?
    if (m_output_uncompress_b) {
      writer_p.filterStreams = true;
      writer_p.compressStreams = false;
    } else if (m_output_compress_b) {
      writer_p.filterStreams = false;
      writer_p.compressStreams = true;
    }

    // encrypt output?
    if (m_output_encryption_strength != encryption_strength.none_enc
        || !m_output_owner_pw.isEmpty()
        || !m_output_user_pw.isEmpty()) {

      // if no strength is given, default to AES 128 bit,
      if (m_output_encryption_strength == encryption_strength.none_enc) {
        m_output_encryption_strength = encryption_strength.aes128_enc;
      }
      int encryption_type = m_output_encryption_strength.value;

      writer_p.setEncryption(
          m_output_user_pw_pdfdoc,
          m_output_owner_pw_pdfdoc,
          m_output_user_perms,
          m_output_encryption_strength.value);
    }

    return m_output_encryption_strength.pdf_version;
  }

  ErrorCode create_output() {
    if (!is_valid()) return ErrorCode.ERROR;

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

    if (m_verbose_reporting_b) {
      System.out.println();
      System.out.println("Creating Output ...");
    }

    if (m_output_owner_pw.equals("PROMPT")) {
      m_output_owner_pw = pdftk.prompt_for_password("owner", "the output PDF");
    }
    if (m_output_user_pw.equals("PROMPT")) {
      m_output_user_pw = pdftk.prompt_for_password("user", "the output PDF");
    }

    if (!m_output_owner_pw.isEmpty()) {
      m_output_owner_pw_pdfdoc = passwords.utf8_password_to_pdfdoc(m_output_owner_pw, true);
      if (m_output_owner_pw_pdfdoc == null) { // error
        System.err.println("Error: Owner password used to encrypt output PDF includes");
        System.err.println("   invalid characters.");
        return ErrorCode.ERROR;
      }
    }

    if (!m_output_user_pw.isEmpty()) {
      m_output_user_pw_pdfdoc = passwords.utf8_password_to_pdfdoc(m_output_user_pw, true);
      if (m_output_user_pw_pdfdoc == null) { // error
        System.err.println("Error: User password used to encrypt output PDF includes");
        System.err.println("   invalid characters.");
        return ErrorCode.ERROR;
      }
    }

    switch (m_operation) {
      case burst_k:
      case dump_data_fields_k:
      case dump_data_annots_k:
      case dump_data_k:
      case generate_fdf_k:
      case unpack_files_k:
        // we should have been given only a single, input file
        if (1 < m_input_pdf.size()) { // error
          System.err.println(
              "Error: Only one input PDF file may be used for the " + m_operation + " operation");
          return ErrorCode.ERROR;
        }
    }

    try {
      switch (m_operation) {
        case cat_k:
        case shuffle_k:
          cat cat = new cat(this);
          return cat.create_output_cat();
        case burst_k:
          burst burst = new burst(this);
          return burst.create_output_burst();
        case filter_k:
          filter filter = new filter(this);
          return filter.create_output_filter();
        case dump_data_fields_k:
        case dump_data_annots_k:
        case dump_data_k:
          { // report on input document
            PdfReader input_reader_p = m_input_pdf.get(0).m_readers.get(0).second;

            try {
              PrintStream ofs = pdftk.get_print_stream(m_output_filename, m_output_utf8_b);
              if (m_operation == keyword.dump_data_k) {
                report.ReportOnPdf(ofs, input_reader_p, m_output_utf8_b);
              } else if (m_operation == keyword.dump_data_fields_k) {
                report.ReportAcroFormFields(ofs, input_reader_p, m_output_utf8_b);
              } else if (m_operation == keyword.dump_data_annots_k) {
                report.ReportAnnots(ofs, input_reader_p, m_output_utf8_b);
              }
            } catch (FileNotFoundException e) { // error
              System.err.println("Error: unable to open file for output: " + m_output_filename);
              return ErrorCode.ERROR;
            }
          }
          break;

        case generate_fdf_k:
          { // create a dummy FDF file that would work with the input PDF form
            PdfReader input_reader_p = m_input_pdf.get(0).m_readers.get(0).second;

            OutputStream ofs_p = pdftk.get_output_stream(m_output_filename, m_ask_about_warnings_b);
            if (ofs_p != null) {
              FdfWriter writer_p = new FdfWriter();
              input_reader_p.getAcroFields().exportAsFdf(writer_p);
              writer_p.writeTo(ofs_p);
              // no writer_p->close() function

              // delete writer_p; // OK? GC? -- NOT okay!
            } else { // error: get_output_stream() reports error
              return ErrorCode.ERROR;
            }
          }
          break;

        case unpack_files_k:
          { // copy PDF file attachments into current directory
            PdfReader input_reader_p = m_input_pdf.get(0).m_readers.get(0).second;
            attachments attachments = new attachments(this);
            attachments.unpack_files(input_reader_p);
          }
          break;

        default:
          // error
          System.err.println("Unexpected pdftk Error in create_output()");
          return ErrorCode.BUG;
      }
    } catch (NoClassDefFoundError error) {
      pdftk.describe_missing_library(error);
      return ErrorCode.ERROR;
    } catch (Throwable t_p) {
      System.err.println("Unhandled Java Exception in create_output():");
      t_p.printStackTrace();
      return ErrorCode.BUG;
    }

    return ErrorCode.NO_ERROR;
  }

  private enum ArgState {
    input_files_e,
    input_pw_e,

    page_seq_e,
    form_data_filename_e,

    attach_file_filename_e,
    attach_file_pagenum_e,
    attach_file_relation_e,

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

  class ArgStateMutable {
    ArgState value;
  }

  // convenience function; return true iff handled
  private boolean handle_some_output_options(keyword kw, ArgStateMutable arg_state_p) {
    switch (kw) {
      case output_k:
        // added this case for the burst operation and "output" support;
        // also helps with backward compatibility of the "background" feature
        // change state
        arg_state_p.value = ArgState.output_filename_e;
        break;

        // state-altering keywords
      case owner_pw_k:
        // change state
        arg_state_p.value = ArgState.output_owner_pw_e;
        break;
      case user_pw_k:
        // change state
        arg_state_p.value = ArgState.output_user_pw_e;
        break;
      case user_perms_k:
        // change state
        arg_state_p.value = ArgState.output_user_perms_e;
        break;

        ////
        // no arguments to these keywords, so the state remains unchanged
      case encrypt_40bit_k:
        m_output_encryption_strength = encryption_strength.rc4_40_enc;
        break;
      case encrypt_128bit_k:
        m_output_encryption_strength = encryption_strength.rc4_128_enc;
        break;
      case encrypt_aes128_k:
        m_output_encryption_strength = encryption_strength.aes128_enc;
      case filt_uncompress_k:
        m_output_uncompress_b = true;
        break;
      case filt_compress_k:
        m_output_compress_b = true;
        break;
      case flatten_k:
        m_output_flatten_b = true;
        break;
      case need_appearances_k:
        m_output_need_appearances_b = true;
        break;
      case drop_xfa_k:
        m_output_drop_xfa_b = true;
        break;
      case drop_xmp_k:
        m_output_drop_xmp_b = true;
        break;
      case keep_first_id_k:
        m_output_keep_first_id_b = true;
        break;
      case keep_final_id_k:
        m_output_keep_final_id_b = true;
        break;
      case verbose_k:
        m_verbose_reporting_b = true;
        break;
      case dont_ask_k:
        m_ask_about_warnings_b = false;
        break;
      case do_ask_k:
        m_ask_about_warnings_b = true;
        break;

      case background_k:
        if (m_operation != keyword.filter_k) { // warning
          System.err.println(
              "Warning: the \"background\" output option works only in filter mode.");
          System.err.println("  This means it won't work in combination with \"cat\", \"burst\",");
          System.err.println("  \"attach_file\", etc.  To run pdftk in filter mode, simply omit");
          System.err.println(
              "  the operation, e.g.: pdftk in.pdf output out.pdf background back.pdf");
          System.err.println(
              "  Or, use background as an operation; this is the preferred technique:");
          System.err.println("    pdftk in.pdf background back.pdf output out.pdf");
        }
        // change state
        arg_state_p.value = ArgState.background_filename_e;
        break;

      default: // not handled here; no change to *arg_state_p
        return false;
    }

    return true;
  }
};
