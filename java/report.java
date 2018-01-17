import java.util.ArrayList;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import org.apache.commons.lang3.StringEscapeUtils;

import pdftk.com.lowagie.text.Rectangle;
import pdftk.com.lowagie.text.pdf.PdfArray;
import pdftk.com.lowagie.text.pdf.PdfBoolean;
import pdftk.com.lowagie.text.pdf.PdfDictionary;
import pdftk.com.lowagie.text.pdf.PdfName;
import pdftk.com.lowagie.text.pdf.PdfNumber;
import pdftk.com.lowagie.text.pdf.PdfObject;
import pdftk.com.lowagie.text.pdf.PdfReader;
import pdftk.com.lowagie.text.pdf.PdfString;
import pdftk.com.lowagie.text.pdf.PdfWriter;
import pdftk.com.lowagie.text.pdf.PRIndirectReference;

class report {

static String
OutputXmlString( String jss_p )
{
  return StringEscapeUtils.escapeXml10( jss_p );
}

static String
OutputUtf8String( String jss_p )
{
  return jss_p;
}
  
static String
OutputPdfString( PdfString pdfss_p,
                 boolean utf8_b )
{
  if( pdfss_p != null && pdfss_p.isString() ) {
    String jss_p= pdfss_p.toUnicodeString();
    if( utf8_b ) {
      return OutputUtf8String( jss_p );
    }
    else {
      return OutputXmlString( jss_p );
    }
  }
  return "";
}

static String
OutputPdfName( PdfName pdfnn_p )
{
  if( pdfnn_p != null && pdfnn_p.isName() ) {
    String jnn_p= new String( pdfnn_p.getBytes() );
    jnn_p= PdfName.decodeName( jnn_p );
    return OutputXmlString( jnn_p );
  }
  return "";
}

  
static void
ReportAcroFormFields( PrintWriter ofs,
                      PdfReader reader_p,
                      boolean utf8_b ) {
  System.err.println( "NOT TRANSLATED: ReportAcroFormFields" );
  /* NOT TRANSLATED */
}

static void
ReportAction( PrintWriter ofs, 
              PdfReader reader_p,
              PdfDictionary action_p,
              boolean utf8_b,
              String prefix )
{
  if( action_p.contains( PdfName.S ) ) {
    PdfName s_p= (PdfName)
      reader_p.getPdfObject( action_p.get( PdfName.S ) );

    // URI action
    if( s_p.equals( PdfName.URI ) ) {
      ofs.println( prefix + "ActionSubtype: URI" );

      // report URI
      if( action_p.contains( PdfName.URI ) ) {
        PdfString uri_p= (PdfString)
          reader_p.getPdfObject( action_p.get( PdfName.URI ) );
        if( uri_p != null && uri_p.isString() ) {
          
          ofs.println( prefix + "ActionURI: " +
                       OutputPdfString( uri_p, utf8_b ) );
        }
      }

      // report IsMap
      if( action_p.contains( PdfName.ISMAP ) ) {
        PdfBoolean ismap_p= (PdfBoolean)
          reader_p.getPdfObject( action_p.get( PdfName.ISMAP ) );
        if( ismap_p != null && ismap_p.isBoolean() )
          if( ismap_p.booleanValue() )
            ofs.println( prefix + "ActionIsMap: true" );
          else
            ofs.println( prefix + "ActionIsMap: false" );
      }
      else
        ofs.println( prefix + "ActionIsMap: false" );
    }
  }

  // subsequent actions? can be a single action or an array
  if( action_p.contains( PdfName.NEXT ) ) {
    PdfObject next_p= reader_p.getPdfObject( action_p.get( PdfName.NEXT ) );
    if( next_p.isDictionary() ) {
      ReportAction( ofs, reader_p, (PdfDictionary)next_p, utf8_b, prefix );
    }
    else if( next_p.isArray() ) {
      ArrayList<PdfObject> actions_p= ((PdfArray)next_p).getArrayList();
      for( PdfObject ii : actions_p ) {
        PdfDictionary next_action_p= (PdfDictionary)
          reader_p.getPdfObject( ii );
        if( next_action_p != null && next_action_p.isDictionary() )
          ReportAction( ofs, reader_p, next_action_p, utf8_b, prefix ); // recurse
      }
    }
  }
}
  
static final int LLx= 0;
static final int LLy= 1;
static final int URx= 2;
static final int URy= 3;
  
static void
ReportAnnot( PrintWriter ofs,
             PdfReader reader_p,
             int page_num,
             PdfDictionary page_p,
             PdfDictionary annot_p,
             boolean utf8_b )
{
  // report things common to all annots

  // subtype
  PdfName subtype_p= (PdfName)
    reader_p.getPdfObject( annot_p.get( PdfName.SUBTYPE ) );
  if( subtype_p != null && subtype_p.isName() ) {
    ofs.println( "AnnotSubtype: " + OutputPdfName( subtype_p ) );
  }

  ////
  // rect

  // get raw rect from annot
  float[] rect = { 0.0f, 0.0f, 0.0f, 0.0f };
  PdfArray rect_p= (PdfArray)
    reader_p.getPdfObject( annot_p.get( PdfName.RECT ) );
  if( rect_p != null && rect_p.isArray() ) {
    ArrayList<PdfObject> rect_al_p= rect_p.getArrayList();
    if( rect_al_p != null && rect_al_p.size()== 4 ) {

      for( int ii= 0; ii< 4; ++ii ) {
        PdfNumber coord_p= (PdfNumber)
          reader_p.getPdfObject( rect_al_p.get( ii ) );
        if( coord_p != null && coord_p.isNumber() )
          rect[ ii ]= (float)coord_p.floatValue();
        else
          rect[ ii ]= -1; // error value
      }
    }
  }
  
  // transform rect according to page crop box
  // grab width and height for later xform
  float page_crop_width= 0;
  float page_crop_height= 0;
  {
    Rectangle page_crop_p= reader_p.getCropBox( page_num );
    rect[0]= rect[0]- page_crop_p.left();
    rect[1]= rect[1]- page_crop_p.bottom();
    rect[2]= rect[2]- page_crop_p.left();
    rect[3]= rect[3]- page_crop_p.bottom();

    page_crop_width= (float)(page_crop_p.right()- page_crop_p.left());
    page_crop_height= (float)(page_crop_p.top()- page_crop_p.bottom());
  }

  // create new rect based on page rotation
  int page_rot= (int)(reader_p.getPageRotation( page_num )) % 360;
  float[] rot_rect = { 0.0f, 0.0f, 0.0f, 0.0f };
  switch( page_rot ) {

  case 90:
    rot_rect[0]= rect[LLy];
    rot_rect[1]= page_crop_width- rect[URx];
    rot_rect[2]= rect[URy];
    rot_rect[3]= page_crop_width- rect[LLx];
    break;

  case 180:
    rot_rect[0]= page_crop_width- rect[URx];
    rot_rect[1]= page_crop_height- rect[URy];
    rot_rect[2]= page_crop_width- rect[LLx];
    rot_rect[3]= page_crop_height- rect[LLy];
    break;

  case 270:
    rot_rect[0]= page_crop_height- rect[URy];
    rot_rect[1]= rect[LLx];
    rot_rect[2]= page_crop_height- rect[LLy];
    rot_rect[3]= rect[URx];
    break;

  default: // 0 deg
    rot_rect[0]= rect[0];
    rot_rect[1]= rect[1];
    rot_rect[2]= rect[2];
    rot_rect[3]= rect[3];
    break;
  }

  // output rotated rect
  ofs.println( "AnnotRect: " + rot_rect[0] + " " + rot_rect[1] +
               " " + rot_rect[2] + " " + rot_rect[3] );

}

static void
ReportAnnots( PrintWriter ofs,
              PdfReader reader_p,
              boolean utf8_b ) {
  reader_p.resetReleasePage();

  ////
  // document information

  // document page count
  ofs.println("NumberOfPages: " + (int)reader_p.getNumberOfPages());

  // document base url
  PdfDictionary uri_p= (PdfDictionary)
    reader_p.getPdfObject( reader_p.catalog.get( PdfName.URI ) );
  if( uri_p != null && uri_p.isDictionary() ) {
    
    PdfString base_p= (PdfString)
      reader_p.getPdfObject( uri_p.get( PdfName.BASE ) );
    if( base_p != null && base_p.isString() ) {
      ofs.println("PdfUriBase: " + OutputPdfString( base_p, utf8_b ));
    }
  }

  ////
  // iterate over pages

  for( int ii= 1; ii<= reader_p.getNumberOfPages(); ++ii ) {
    PdfDictionary page_p= reader_p.getPageN( ii );

    PdfArray annots_p= (PdfArray)
      reader_p.getPdfObject( page_p.get( PdfName.ANNOTS ) );
    if( annots_p != null && annots_p.isArray() ) {

      ArrayList<PdfDictionary> annots_al_p= annots_p.getArrayList();
      if( annots_al_p != null ) {

        // iterate over annotations
        for( PdfDictionary annot_p : annots_al_p ) {

          if( annot_p != null && annot_p.isDictionary() ) {

            PdfName type_p= (PdfName)
              reader_p.getPdfObject( annot_p.get( PdfName.TYPE ) );
            if( type_p.equals( PdfName.ANNOT ) ) {

              PdfName subtype_p= (PdfName)
                reader_p.getPdfObject( annot_p.get( PdfName.SUBTYPE ) );
            
              // link annotation
              if( subtype_p.equals( PdfName.LINK ) ) {

                ofs.println("---"); // delim
                ReportAnnot( ofs, reader_p, ii, page_p, annot_p, utf8_b ); // base annot items
                ofs.println("AnnotPageNumber: " + ii);

                // link-specific items
                if( annot_p.contains( PdfName.A ) ) { // action
                  PdfDictionary action_p= (PdfDictionary)
                    reader_p.getPdfObject( annot_p.get( PdfName.A ) );
                  if( action_p != null && action_p.isDictionary() ) {

                    ReportAction( ofs, reader_p, action_p, utf8_b, "Annot" );
                  }
                }
              }
            }
          }
        }
      }
    }
    reader_p.releasePage( ii );
  }
  reader_p.resetReleasePage();
}

static void
ReportOnPdf( PrintWriter ofs,
             PdfReader reader_p,
             boolean utf8_b ) {
  System.err.println( "NOT TRANSLATED: ReportOnPdf" );
  /* NOT TRANSLATED */
}

};
