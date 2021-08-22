/* -*- Mode: Java; tab-width: 4; c-basic-offset: 4 -*- */
/*
 * $Id: PdfCopy.java,v 1.35 2005/05/04 14:32:21 blowagie Exp $
 * $Name:  $
 *
 * Copyright 1999, 2000, 2001, 2002 Bruno Lowagie
 *
 *
 * The Original Code is 'iText, a free JAVA-PDF library'.
 *
 * The Initial Developer of the Original Code is Bruno Lowagie. Portions created by
 * the Initial Developer are Copyright (C) 1999, 2000, 2001, 2002 by Bruno Lowagie.
 * All Rights Reserved.
 * Co-Developer of the code is Paulo Soares. Portions created by the Co-Developer
 * are Copyright (C) 2000, 2001, 2002 by Paulo Soares. All Rights Reserved.
 * Co-Developer of the code is Sid Steward. Portions created by the Co-Developer
 * are Copyright (C) 2004, 2010 by Sid Steward. All Rights Reserved.
 *
 * This module by Mark Thompson. Copyright (C) 2002 Mark Thompson
 *
 * Contributor(s): all the names of the contributors are added in the source code
 * where applicable.
 *
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 * 
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 51 Franklin St, Fifth Floor,
 * Boston, MA  02110-1301, USA.
 *
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 * 
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 51 Franklin St, Fifth Floor,
 * Boston, MA  02110-1301, USA.
 *
 *
 * If you didn't download this code from the following link, you should check if
 * you aren't using an obsolete version:
 * http://www.lowagie.com/iText/
 */

// pdftk-java iText base version 4.2.0
// pdftk-java modified yes (field renaming, added outlines, extras)

package com.gitlab.pdftk_java.com.lowagie.text.pdf;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import com.gitlab.pdftk_java.com.lowagie.text.Document;
import com.gitlab.pdftk_java.com.lowagie.text.DocumentException;
import com.gitlab.pdftk_java.com.lowagie.text.ExceptionConverter;
import com.gitlab.pdftk_java.com.lowagie.text.Rectangle;
import java.util.ArrayList;

/**
 * Make copies of PDF documents. Documents can be edited after reading and
 * before writing them out.
 * @author Mark Thompson
 */

public class PdfCopy extends PdfWriter {
    /**
     * This class holds information about indirect references, since they are
     * renumbered by iText.
     */
    static class IndirectReferences {
        PdfIndirectReference theRef;
        boolean hasCopied;
        IndirectReferences(PdfIndirectReference ref) {
            theRef = ref;
            hasCopied = false;
        }
        void setCopied() { hasCopied = true; }
        boolean getCopied() { return hasCopied; }
        PdfIndirectReference getRef() { return theRef; }
    };
    protected HashMap indirects;
    protected HashMap indirectMap;
    protected int currentObjectNum = 1;
    protected PdfReader reader;
	// ssteward: why does PdfCopy have an acroForm, when PdfDocument already has one
    protected PdfIndirectReference acroForm;
    protected int[] namePtr = {0};
    /** Holds value of property rotateContents. */
    private boolean rotateContents = true;
    protected PdfArray fieldArray;
    protected HashMap fieldTemplates;
	protected PdfIndirectReference m_new_bookmarks = null; // ssteward: pdftk 1.46
	protected PdfIndirectReference m_new_extensions = null; // ssteward: pdftk 1.46
    
	// ssteward: pdftk 1.10; to ensure unique form field names, as pages are added
	protected HashSet fullFormFieldNames = null; // all full field names; track to prevent collision
	protected HashSet topFormFieldNames = null; // across all readers; track to prevent collision
	protected class TopFormFieldData {
		HashMap newNamesRefs = null; // map new top parent field names to refs
		HashMap newNamesKids = null; // map new top parent field names to kids PdfArray
		HashSet allNames = null; // ~all~ names, new and not-new
		public TopFormFieldData() {
			newNamesRefs= new HashMap();
			newNamesKids= new HashMap();
			allNames= new HashSet();
		}
	};
	protected HashMap topFormFieldReadersData = null;

    /**
     * A key to allow us to hash indirect references
     */
    protected static class RefKey {
        int num;
        int gen;
        RefKey(int num, int gen) {
            this.num = num;
            this.gen = gen;
        }
        RefKey(PdfIndirectReference ref) {
            num = ref.getNumber();
            gen = ref.getGeneration();
        }
        RefKey(PRIndirectReference ref) {
            num = ref.getNumber();
            gen = ref.getGeneration();
        }
        public int hashCode() {
            return (gen<<16)+num;
        }
        public boolean equals(Object o) {
            if (!(o instanceof RefKey)) return false;
            RefKey other = (RefKey)o;
            return this.gen == other.gen && this.num == other.num;
        }
        public String toString() {
            return Integer.toString(num) + ' ' + gen;
        }
    }
    
    /**
     * Constructor
     * @param document
     * @param os outputstream
     */
    public PdfCopy(Document document, OutputStream os) throws DocumentException {
        super(new PdfDocument(), os);
        document.addDocListener(pdf);
        pdf.addWriter(this);
        indirectMap = new HashMap();

		// ssteward: pdftk 1.10
		fullFormFieldNames = new HashSet();
		topFormFieldNames = new HashSet();
		topFormFieldReadersData = new HashMap();
    }

    /** Getter for property rotateContents.
     * @return Value of property rotateContents.
     *
     */
    public boolean isRotateContents() {
        return this.rotateContents;
    }
    
    /** Setter for property rotateContents.
     * @param rotateContents New value of property rotateContents.
     *
     */
    public void setRotateContents(boolean rotateContents) {
        this.rotateContents = rotateContents;
    }

    /**
     * Grabs a page from the input document
     * @param reader the reader of the document
     * @param pageNumber which page to get
     * @return the page
     */
    public PdfImportedPage getImportedPage(PdfReader reader, int pageNumber) {
        if (currentPdfReaderInstance != null) {
            if (currentPdfReaderInstance.getReader() != reader) {
                try {
                    currentPdfReaderInstance.getReader().close();
                    currentPdfReaderInstance.getReaderFile().close();
                }
                catch (IOException ioe) {
                    // empty on purpose
                }
                currentPdfReaderInstance = reader.getPdfReaderInstance(this);
            }
        }
        else {
            currentPdfReaderInstance = reader.getPdfReaderInstance(this);
        }
        return currentPdfReaderInstance.getImportedPage(pageNumber);            
    }
    
    
    /**
     * Translate a PRIndirectReference to a PdfIndirectReference
     * In addition, translates the object numbers, and copies the
     * referenced object to the output file.
     * NB: PRIndirectReferences (and PRIndirectObjects) really need to know what
     * file they came from, because each file has its own namespace. The translation
     * we do from their namespace to ours is *at best* heuristic, and guaranteed to
     * fail under some circumstances.
     */
    protected PdfIndirectReference copyIndirect(PRIndirectReference in) throws IOException, BadPdfFormatException {
        PdfIndirectReference theRef;
        RefKey key = new RefKey(in);
        IndirectReferences iRef = (IndirectReferences)indirects.get(key);
        if (iRef != null) {
            theRef = iRef.getRef();
            if (iRef.getCopied()) {
                return theRef;
            }
        }
        else {
            theRef = body.getPdfIndirectReference();
            iRef = new IndirectReferences(theRef);
            indirects.put(key, iRef);
		}

		// ssteward; if this is a ref to a dictionary with a parent,
		// and we haven't copied the parent yet, then don't recurse
		// into this dictionary; wait to recurse via the parent;
		// this was written to fix a problem with references to pages
		// inside pdf destinations; this problem caused pdf bloat;
		// the pdf spec says broken indirect ref.s (ref, but no obj) are okay;
		//
		// update: we /do/ want to recurse up form field parents (see addPage()),
		// just /not/ pages with parents
		//
		// note: copying an indirect reference to a dictionary is
		// more suspect than copying a dictionary
		//
		// simplify this by not recursing into /any/ type==page via indirect ref?

		PdfObject obj= PdfReader.getPdfObjectRelease(in);
		if( obj!= null && obj.isDictionary() ) {
			PdfDictionary in_dict= (PdfDictionary)obj;

			// /Type should always contain a name, but this is ignored in practice. See
			// https://gitlab.com/pdftk-java/pdftk/issues/47
			PdfObject type= in_dict.get(PdfName.TYPE);
			if(PdfName.PAGE.equals(type)) {

				PdfObject parent_obj=
					in_dict.get( PdfName.PARENT );
				if( parent_obj!= null && parent_obj.isIndirect() ) {
					PRIndirectReference parent_iref= (PRIndirectReference)parent_obj;

					RefKey parent_key= new RefKey( parent_iref );
					IndirectReferences parent_ref= (IndirectReferences)indirects.get( parent_key );

					if( parent_ref== null || !parent_ref.getCopied() ) {
						// parent has not been copied yet, so we've jumped here somehow;
						return theRef;
					}
				}
			}
		}
		iRef.setCopied();
        obj = copyObject(obj);
		addToBody(obj, theRef);
        return theRef;
    }
    
    /**
     * Translate a PRDictionary to a PdfDictionary. Also translate all of the
     * objects contained in it.
     */
    public PdfDictionary copyDictionary(PdfDictionary in)
    throws IOException, BadPdfFormatException {
        PdfDictionary out = new PdfDictionary();
        // /Type should always contain a name, but this is ignored in practice. See
        // https://gitlab.com/pdftk-java/pdftk/issues/47
        PdfObject type = PdfReader.getPdfObjectRelease(in.get(PdfName.TYPE));
        
        for (Iterator it = in.getKeys().iterator(); it.hasNext();) {
            PdfName key = (PdfName)it.next();
            PdfObject value = in.get(key);
            // System.out.println("Copy " + key);
            if (type != null && PdfName.PAGE.equals(type)) {
                if (!key.equals(PdfName.B) && !key.equals(PdfName.PARENT))
                    out.put(key, copyObject(value));
            }
            else
                out.put(key, copyObject(value));
        }
        return out;
    }
    
    /**
     * Translate a PRStream to a PdfStream. The data part copies itself.
     */
    protected PdfStream copyStream(PRStream in) throws IOException, BadPdfFormatException {
        PRStream out = new PRStream(in, null);
        
        for (Iterator it = in.getKeys().iterator(); it.hasNext();) {
            PdfName key = (PdfName) it.next();
            PdfObject value = in.get(key);
            out.put(key, copyObject(value));
        }
        
        return out;
    }
    
    
    /**
     * Translate a PRArray to a PdfArray. Also translate all of the objects contained
     * in it
     */
    protected PdfArray copyArray(PdfArray in) throws IOException, BadPdfFormatException {
        PdfArray out = new PdfArray();
        
        for (Iterator i = in.listIterator(); i.hasNext();) {
            PdfObject value = (PdfObject)i.next();
            out.add(copyObject(value));
        }
        return out;
    }
    
    /**
     * Translate a PR-object to a Pdf-object
     */
    protected PdfObject copyObject(PdfObject in) throws IOException,BadPdfFormatException {
        if (in == null)
            return PdfNull.PDFNULL;
        switch (in.type) {
            case PdfObject.DICTIONARY:
                //	        System.out.println("Dictionary: " + in.toString());
                return copyDictionary((PdfDictionary)in);
            case PdfObject.INDIRECT:
                return copyIndirect((PRIndirectReference)in);
            case PdfObject.ARRAY:
                return copyArray((PdfArray)in);
            case PdfObject.NUMBER:
            case PdfObject.NAME:
            case PdfObject.STRING:
            case PdfObject.NULL:
            case PdfObject.BOOLEAN:
            case 0:
                return in;
            case PdfObject.STREAM:
                return copyStream((PRStream)in);
                //                return in;
            default:
                if (in.type < 0) {
                    String lit = ((PdfLiteral)in).toString();
                    if (lit.equals("true") || lit.equals("false")) {
                        return new PdfBoolean(lit);
                    }
                    return new PdfLiteral(lit);
                }
                System.err.println("CANNOT COPY type " + in.type);
                return null;
        }
    }
    
    /**
     * convenience method. Given an importedpage, set our "globals"
     */
    protected int setFromIPage(PdfImportedPage iPage) {
        int pageNum = iPage.getPageNumber();
        PdfReaderInstance inst = currentPdfReaderInstance = iPage.getPdfReaderInstance();
        reader = inst.getReader();
        setFromReader(reader);
        return pageNum;
    }
    
    /**
     * convenience method. Given a reader, set our "globals"
     */
    public void setFromReader(PdfReader reader) {
        this.reader = reader;
        indirects = (HashMap)indirectMap.get(reader);
        if (indirects == null) {
            indirects = new HashMap();
            indirectMap.put(reader,indirects);
            PdfDictionary catalog = reader.getCatalog();
            PRIndirectReference ref = null;
            PdfObject o = catalog.get(PdfName.ACROFORM);
            if (o == null || o.type() != PdfObject.INDIRECT)
                return;
            ref = (PRIndirectReference)o;
                if (acroForm == null) acroForm = body.getPdfIndirectReference();
                indirects.put(new RefKey(ref), new IndirectReferences(acroForm));
        }
    }
    /**
     * Add an imported page to our output
     * @param iPage an imported page
     * @throws IOException, BadPdfFormatException
     */
    public void addPage(PdfImportedPage iPage) throws IOException, BadPdfFormatException, DocumentException {
        int pageNum = setFromIPage(iPage); // sets this.reader
        
        PdfDictionary thePage = reader.getPageN(pageNum);
        PRIndirectReference origRef = reader.getPageOrigRef(pageNum);
        reader.releasePage(pageNum);
        RefKey key = new RefKey(origRef);
        PdfIndirectReference pageRef;
        IndirectReferences iRef = (IndirectReferences)indirects.get(key);
        if (iRef != null && !iRef.getCopied()) {
            pageReferences.add(iRef.getRef());
            iRef.setCopied();
        }
        pageRef = getCurrentPage();
        if (iRef == null) {
            iRef = new IndirectReferences(pageRef);
            indirects.put(key, iRef);
        }
            iRef.setCopied();
			
			// ssteward
			if( !this.topFormFieldReadersData.containsKey( reader ) ) { // add
				this.topFormFieldReadersData.put( reader, new TopFormFieldData() );
			}
			TopFormFieldData readerData= (TopFormFieldData)topFormFieldReadersData.get(reader);

			// ssteward
			// if duplicate form field names are encountered
			// make names unique by inserting a new top parent "field";
			// insert this new parent into the PdfReader of the input document,
			// since any PdfWriter material may have been written already; our
			// changes to the PdfReader will be copied, below, into the PdfWriter;
			//
			{
				PdfObject annots= PdfReader.getPdfObject(thePage.get(PdfName.ANNOTS));
				if( annots!= null && annots.isArray() ) {
					ArrayList<PdfObject> annots_arr= ((PdfArray)annots).getArrayList();
					for( PdfObject annot_ref_obj : annots_arr ) {
						// an annotation may be direct or indirect; ours must be indirect
						if( annot_ref_obj!= null && annot_ref_obj.isIndirect() ) {
							PdfIndirectReference annot_ref= (PdfIndirectReference)annot_ref_obj;
							PdfObject annot_obj= PdfReader.getPdfObject(annot_ref);
							if( annot_obj!= null && annot_obj.isDictionary() ) {
								PdfDictionary annot = (PdfDictionary)annot_obj;
								PdfObject subtype= PdfReader.getPdfObject(annot.get(PdfName.SUBTYPE));
								if( PdfName.WIDGET.equals(subtype) ) {
									// we have a form field

									// get its full name
									//
									String full_name= ""; // construct a full name from partial names using '.', e.g.: foo.bar.
									String top_name= "";
									boolean is_unicode_b= false; // if names are unicode, they must all be unicode
									PdfObject tt_obj= PdfReader.getPdfObject(annot.get(PdfName.T));
									if( tt_obj!= null && tt_obj.isString() ) {
										PdfString tt = (PdfString)tt_obj;
										top_name= tt.toString();
										is_unicode_b= ( is_unicode_b || PdfString.isUnicode( tt.getBytes() ) );
									}
									//
									// dig upwards, parent-wise; replace annot as we go with the
									// top-most form field dictionary
									PdfIndirectReference parent_ref=
										(PdfIndirectReference)annot.get(PdfName.PARENT);
									while( parent_ref!= null && parent_ref.isIndirect() ) {
										annot_ref= parent_ref;
										annot= (PdfDictionary)PdfReader.getPdfObject(annot_ref);
										parent_ref= (PdfIndirectReference)annot.get(PdfName.PARENT);

										tt_obj= PdfReader.getPdfObject(annot.get(PdfName.T));
										if( tt_obj!= null && tt_obj.isString() ) {
											PdfString tt = (PdfString)tt_obj;
											if( top_name.length()!= 0 ) {
												full_name+= top_name;
												full_name+= ".";
											}
											top_name= tt.toString();
											is_unicode_b= ( is_unicode_b || PdfString.isUnicode( tt.getBytes() ) );
										}
									}
									
									// once we have seen a top-level field parent, we wave
									// it through and assume that it harbors no illegal field duplicates;
									// this is good, because sometimes authors want a single field
									// represented by more than one annotation on the page; this logic
									// respects that programming
									//
									//System.err.println( full_name+ top_name+ "." ); // debug
									if( readerData.allNames.contains( top_name ) ) {
										// a parent we have seen or created in this reader
										this.fullFormFieldNames.add( full_name+ top_name+ "." ); // tally
									}
									else if( this.fullFormFieldNames.contains( full_name+ top_name+ "." ) ) {
										// insert new, top-most parent

										// name for new parent
										int new_parent_name_ii= 1;
										String new_parent_name= Integer.toString( new_parent_name_ii );
										while( this.fullFormFieldNames.contains( full_name+ top_name+ "."+ new_parent_name+ "." ) ||
											   this.topFormFieldNames.contains( new_parent_name ) &&
											   !readerData.newNamesKids.containsKey( new_parent_name ) )
											{
												new_parent_name= Integer.toString( ++new_parent_name_ii );
											}

										PdfIndirectReference new_parent_ref= null;
										PdfArray new_parent_kids= null;
										//
										if( readerData.newNamesKids.containsKey( new_parent_name ) ) {
											// a new parent we already created
											new_parent_ref= (PdfIndirectReference)
												readerData.newNamesRefs.get( new_parent_name );
											new_parent_kids= (PdfArray)
												readerData.newNamesKids.get( new_parent_name );
										}
										else { // create a new parent using this name
											PdfDictionary new_parent= new PdfDictionary();
											PdfString new_parent_name_pdf= new PdfString( new_parent_name );
											if( is_unicode_b ) { // if names are unicode, they must all be unicode
												new_parent_name_pdf= new PdfString( new_parent_name, PdfObject.TEXT_UNICODE );
											}
											new_parent_ref= reader.getPRIndirectReference( new_parent );
											new_parent.put( PdfName.T, new_parent_name_pdf );

											new_parent_kids= new PdfArray();
											PdfIndirectReference new_parent_kids_ref=
												reader.getPRIndirectReference( new_parent_kids );
											new_parent.put(PdfName.KIDS, new_parent_kids_ref);

											// tally new parent
											readerData.newNamesRefs.put( new_parent_name, new_parent_ref );
											readerData.newNamesKids.put( new_parent_name, new_parent_kids );
											readerData.allNames.add( new_parent_name );
											this.topFormFieldNames.add( new_parent_name );
										}

										// wire annot and new parent together
										annot.put( PdfName.PARENT, new_parent_ref );
										new_parent_kids.add( annot_ref ); // the new parent must point at the field, too

										// tally full field name
										this.fullFormFieldNames.add( full_name+ top_name+ "."+ new_parent_name+ "." );
									}
									else {
										// tally parent
										readerData.allNames.add( top_name );
										this.topFormFieldNames.add( top_name );

										// tally full field name
										this.fullFormFieldNames.add( full_name+ top_name+ "." );
									}
								}
							}
						}
					}
				}
			}

		// copy the page; this will copy our work, above, into the target document
        PdfDictionary newPage = copyDictionary(thePage);
			

			// ssteward: pdftk-1.00;
			// copy page form field ref.s into document AcroForm
			// dig down into source page to find indirect ref., then
			// look up ref. in dest page.; store ref.s in the PdfDocument acroForm,
			// not the PdfCopy acroForm (which isn't really a PdfAcroForm, anyhow);
			//
			// dig down to annot, and then dig up to topmost Parent
			{
				PdfObject annots= PdfReader.getPdfObject(thePage.get(PdfName.ANNOTS));
				if( annots!= null && annots.isArray() ) {
					ArrayList<PdfObject> annots_arr= ((PdfArray)annots).getArrayList();
					for( PdfObject annot_ref_obj : annots_arr ) {
						// an annotation may be direct or indirect; ours must be indirect
						if( annot_ref_obj!= null && annot_ref_obj.isIndirect() ) {
							PdfIndirectReference annot_ref= (PdfIndirectReference)annot_ref_obj;
							PdfObject annot_obj= PdfReader.getPdfObject(annot_ref);
							if( annot_obj!= null && annot_obj.isDictionary() ) {
								PdfDictionary annot= (PdfDictionary)annot_obj;
								PdfObject subtype= PdfReader.getPdfObject(annot.get(PdfName.SUBTYPE));
								if( PdfName.WIDGET.equals(subtype) ) {
									// we have a form field

									// dig upwards, parent-wise
									PdfIndirectReference parent_ref=
										(PdfIndirectReference)annot.get(PdfName.PARENT);
									while( parent_ref!= null && parent_ref.isIndirect() ) {
										annot_ref= parent_ref;
										annot= (PdfDictionary)PdfReader.getPdfObject(annot_ref);
										parent_ref= (PdfIndirectReference)annot.get(PdfName.PARENT);
									}
								
									RefKey annot_key= new RefKey(annot_ref);
									IndirectReferences annot_iRef= (IndirectReferences)indirects.get(annot_key);
									PdfAcroForm acroForm= this.getAcroForm();
									acroForm.addDocumentField( annot_iRef.getRef() );
								}
							}
						}
					}
				}
			}

			// ssteward: pdftk-1.00;
			// merge the reader AcroForm/DR dictionary with the target
			//
			// pdftk-1.10: I noticed that the PdfAcroForm.isValid() apprears to stomp on this (TODO)
			//
			PdfObject catalog_obj= reader.getCatalog();
			if( catalog_obj!= null && catalog_obj.isDictionary() ) {
				PdfDictionary catalog= (PdfDictionary)catalog_obj;
				PdfObject acroForm_obj= PdfReader.getPdfObject(catalog.get(PdfName.ACROFORM));
				if( acroForm_obj!= null && acroForm_obj.isDictionary() ) {
					PdfDictionary acroForm= (PdfDictionary)acroForm_obj;
					PdfObject dr_obj= PdfReader.getPdfObject(acroForm.get(PdfName.DR));
					if( dr_obj!= null && dr_obj.isDictionary() ) {
						PdfDictionary dr= (PdfDictionary)dr_obj;
						PdfDictionary acroForm_target= this.getAcroForm();
						PdfDictionary dr_target= (PdfDictionary)PdfReader.getPdfObject(acroForm_target.get(PdfName.DR));
						if( dr_target== null ) {
							PdfDictionary dr_copy= copyDictionary( dr );
							acroForm_target.put( PdfName.DR, dr_copy );
						}
						else {
							for( Iterator it= dr.getKeys().iterator(); it.hasNext(); ) {
								PdfName dr_key= (PdfName)it.next();
								PdfObject dr_val= (PdfObject)dr.get(dr_key);
								if( !dr_target.contains( dr_key ) ) { // copy key/value to dr_target
									dr_target.put( dr_key, copyObject( dr_val ) );
								}
							}
						}
					}
				}
			}
			
        root.addPage(newPage);
        ++currentPageNumber;
    }

    /**
     * Adds a blank page.
     * @param	rect The page dimension
     * @param	rotation The rotation angle in degrees
     * @since	2.1.5
     */
    public void addPage(Rectangle rect, int rotation) {
    	PdfRectangle mediabox = new PdfRectangle(rect, rotation);
    	PageResources resources = new PageResources();
    	PdfPage page = new PdfPage(mediabox, new HashMap(), resources.getResources(), 0);
    	page.put(PdfName.TABS, getTabs());
    	root.addPage(page);
    	++currentPageNumber;
    }

    /**
     * Copy the acroform for an input document. Note that you can only have one,
     * we make no effort to merge them.
     * @param reader The reader of the input file that is being copied
     * @throws IOException, BadPdfFormatException
     */
	/* ssteward: why PdfCopy.acroForm when PdfDocument.acroForm?
    public void copyAcroForm(PdfReader reader) throws IOException, BadPdfFormatException {
        setFromReader(reader);
        
        PdfDictionary catalog = reader.getCatalog();
        PRIndirectReference hisRef = null;
        PdfObject o = catalog.get(PdfName.ACROFORM);
        if (o != null && o.type() == PdfObject.INDIRECT)
            hisRef = (PRIndirectReference)o;
        if (hisRef == null) return; // bugfix by John Englar
        RefKey key = new RefKey(hisRef);
        PdfIndirectReference myRef;
        IndirectReferences iRef = (IndirectReferences)indirects.get(key);
        if (iRef != null) {
            acroForm = myRef = iRef.getRef();
        }
        else {
            acroForm = myRef = body.getPdfIndirectReference();
            iRef = new IndirectReferences(myRef);
            indirects.put(key, iRef);
        }
        if (! iRef.getCopied()) {
            iRef.setCopied();
            PdfDictionary theForm = copyDictionary((PdfDictionary)PdfReader.getPdfObject(hisRef));
            addToBody(theForm, myRef);
        }
    }
	*/
    
    /*
     * the getCatalog method is part of PdfWriter.
     * we wrap this so that we can extend it
     */
    protected PdfDictionary getCatalog( PdfIndirectReference rootObj ) {
        try {
            PdfDictionary theCat = pdf.getCatalog(rootObj);
			if( m_new_bookmarks!= null ) {
				theCat.put( PdfName.OUTLINES, m_new_bookmarks );
			}
			if( m_new_extensions!= null ) {
				theCat.put( PdfName.EXTENSIONS, m_new_extensions );
			}
            if (fieldArray == null) {
                if (acroForm != null) theCat.put(PdfName.ACROFORM, acroForm);
            }
            else
                addFieldResources(theCat);
            return theCat;
        }
        catch (IOException e) {
            throw new ExceptionConverter(e);
        }
    }

    /**
     * Sets the bookmarks. The list structure is defined in
     * <CODE>SimpleBookmark#</CODE>.
     * @param outlines the bookmarks or <CODE>null</CODE> to remove any
     */
	/*
    public void setOutlines(List outlines) {
        newBookmarks = outlines;
    }
	*/
	// ssteward: pdftk 1.46
	public void setOutlines( PdfIndirectReference outlines ) {
		m_new_bookmarks= outlines;
	}
	public void setExtensions( PdfIndirectReference extensions ) {
		m_new_extensions= extensions;
	}

    private void addFieldResources(PdfDictionary catalog) throws IOException {
        if (fieldArray == null)
            return;
        PdfDictionary acroForm = new PdfDictionary();
        catalog.put(PdfName.ACROFORM, acroForm);
        acroForm.put(PdfName.FIELDS, fieldArray);
        acroForm.put(PdfName.DA, new PdfString("/Helv 0 Tf 0 g "));
        if (fieldTemplates.isEmpty())
            return;
        PdfDictionary dr = new PdfDictionary();
        acroForm.put(PdfName.DR, dr);
        for (Iterator it = fieldTemplates.keySet().iterator(); it.hasNext();) {
            PdfTemplate template = (PdfTemplate)it.next();
            PdfFormField.mergeResources(dr, (PdfDictionary)template.getResources());
        }
        // if (dr.get(PdfName.ENCODING) == null) dr.put(PdfName.ENCODING, PdfName.WIN_ANSI_ENCODING);
        PdfDictionary fonts = dr.getAsDict(PdfName.FONT);
        if (fonts == null) {
            fonts = new PdfDictionary();
            dr.put(PdfName.FONT, fonts);
        }
        if (!fonts.contains(PdfName.HELV)) {
            PdfDictionary dic = new PdfDictionary(PdfName.FONT);
            dic.put(PdfName.BASEFONT, PdfName.HELVETICA);
            dic.put(PdfName.ENCODING, PdfName.WIN_ANSI_ENCODING);
            dic.put(PdfName.NAME, PdfName.HELV);
            dic.put(PdfName.SUBTYPE, PdfName.TYPE1);
            fonts.put(PdfName.HELV, addToBody(dic).getIndirectReference());
        }
        if (!fonts.contains(PdfName.ZADB)) {
            PdfDictionary dic = new PdfDictionary(PdfName.FONT);
            dic.put(PdfName.BASEFONT, PdfName.ZAPFDINGBATS);
            dic.put(PdfName.NAME, PdfName.ZADB);
            dic.put(PdfName.SUBTYPE, PdfName.TYPE1);
            fonts.put(PdfName.ZADB, addToBody(dic).getIndirectReference());
        }
    }

    /**
     * Signals that the <CODE>Document</CODE> was closed and that no other
     * <CODE>Elements</CODE> will be added.
     * <P>
     * The pages-tree is built and written to the outputstream.
     * A Catalog is constructed, as well as an Info-object,
     * the referencetable is composed and everything is written
     * to the outputstream embedded in a Trailer.
     */
    
    public void close() {
        if (open) {
            PdfReaderInstance ri = currentPdfReaderInstance;
            pdf.close();
            super.close();
            if (ri != null) {
                try {
                    ri.getReader().close();
                    ri.getReaderFile().close();
                }
                catch (IOException ioe) {
                    // empty on purpose
                }
            }
        }
    }
    public PdfIndirectReference add(PdfOutline outline) { return null; }
    public void addAnnotation(PdfAnnotation annot) {  }
    PdfIndirectReference add(PdfPage page, PdfContents contents) throws PdfException { return null; }

    public void freeReader(PdfReader reader) throws IOException {
        indirectMap.remove(reader);
        if (currentPdfReaderInstance != null) {
            if (currentPdfReaderInstance.getReader() == reader) {
                try {
                    currentPdfReaderInstance.getReader().close();
                    currentPdfReaderInstance.getReaderFile().close();
                }
                catch (IOException ioe) {
                    // empty on purpose
                }
                currentPdfReaderInstance = null;
            }
        }
    }
    
    /**
     * Create a page stamp. New content and annotations, including new fields, are allowed.
     * The fields added cannot have parents in another pages. This method modifies the PdfReader instance.<p>
     * The general usage to stamp something in a page is:
     * <p>
     * <pre>
     * PdfImportedPage page = copy.getImportedPage(reader, 1);
     * PdfCopy.PageStamp ps = copy.createPageStamp(page);
     * ps.addAnnotation(PdfAnnotation.createText(copy, new Rectangle(50, 180, 70, 200), "Hello", "No Thanks", true, "Comment"));
     * PdfContentByte under = ps.getUnderContent();
     * under.addImage(img);
     * PdfContentByte over = ps.getOverContent();
     * over.beginText();
     * over.setFontAndSize(bf, 18);
     * over.setTextMatrix(30, 30);
     * over.showText("total page " + totalPage);
     * over.endText();
     * ps.alterContents();
     * copy.addPage(page);
     * </pre>
     * @param iPage an imported page
     * @return the <CODE>PageStamp</CODE>
     */
    public PageStamp createPageStamp(PdfImportedPage iPage) {
        int pageNum = iPage.getPageNumber();
        PdfReader reader = iPage.getPdfReaderInstance().getReader();
        PdfDictionary pageN = reader.getPageN(pageNum);
        return new PageStamp(reader, pageN, this);
    }
    
    public static class PageStamp {
        
        PdfDictionary pageN;
        PdfCopy.StampContent under;
        PdfCopy.StampContent over;
        PageResources pageResources;
        PdfReader reader;
        PdfCopy cstp;
        
        PageStamp(PdfReader reader, PdfDictionary pageN, PdfCopy cstp) {
            this.pageN = pageN;
            this.reader = reader;
            this.cstp = cstp;
        }
        
        public PdfContentByte getUnderContent(){
            if (under == null) {
                if (pageResources == null) {
                    pageResources = new PageResources();
                    PdfDictionary resources = pageN.getAsDict(PdfName.RESOURCES);
                    pageResources.setOriginalResources(resources, cstp.namePtr);
                }
                under = new PdfCopy.StampContent(cstp, pageResources);
            }
            return under;
        }
        
        public PdfContentByte getOverContent(){
            if (over == null) {
                if (pageResources == null) {
                    pageResources = new PageResources();
                    PdfDictionary resources = pageN.getAsDict(PdfName.RESOURCES);
                    pageResources.setOriginalResources(resources, cstp.namePtr);
                }
                over = new PdfCopy.StampContent(cstp, pageResources);
            }
            return over;
        }
        
        public void alterContents() throws IOException {
            if (over == null && under == null)
                return;
            PdfArray ar = null;
            PdfObject content = PdfReader.getPdfObject(pageN.get(PdfName.CONTENTS), pageN);
            if (content == null) {
                ar = new PdfArray();
                pageN.put(PdfName.CONTENTS, ar);
            } else if (content.isArray()) {
                ar = (PdfArray)content;
            } else if (content.isStream()) {
                ar = new PdfArray();
                ar.add(pageN.get(PdfName.CONTENTS));
                pageN.put(PdfName.CONTENTS, ar);
            } else {
                ar = new PdfArray();
                pageN.put(PdfName.CONTENTS, ar);
            }
            ByteBuffer out = new ByteBuffer();
            if (under != null) {
                out.append(PdfContents.SAVESTATE);
                applyRotation(pageN, out);
                out.append(under.getInternalBuffer());
                out.append(PdfContents.RESTORESTATE);
            }
            if (over != null)
                out.append(PdfContents.SAVESTATE);
            PdfStream stream = new PdfStream(out.toByteArray());
            stream.flateCompress(cstp.getCompressionLevel());
            PdfIndirectReference ref1 = cstp.addToBody(stream).getIndirectReference();
            ar.addFirst(ref1);
            out.reset();
            if (over != null) {
                out.append(' ');
                out.append(PdfContents.RESTORESTATE);
                out.append(PdfContents.SAVESTATE);
                applyRotation(pageN, out);
                out.append(over.getInternalBuffer());
                out.append(PdfContents.RESTORESTATE);
                stream = new PdfStream(out.toByteArray());
                stream.flateCompress(cstp.getCompressionLevel());
                ar.add(cstp.addToBody(stream).getIndirectReference());
            }
            pageN.put(PdfName.RESOURCES, pageResources.getResources());
        }
        
        void applyRotation(PdfDictionary pageN, ByteBuffer out) {
            if (!cstp.rotateContents)
                return;
            Rectangle page = reader.getPageSizeWithRotation(pageN);
            int rotation = page.getRotation();
            switch (rotation) {
                case 90:
                    out.append(PdfContents.ROTATE90);
                    out.append(page.getTop());
                    out.append(' ').append('0').append(PdfContents.ROTATEFINAL);
                    break;
                case 180:
                    out.append(PdfContents.ROTATE180);
                    out.append(page.getRight());
                    out.append(' ');
                    out.append(page.getTop());
                    out.append(PdfContents.ROTATEFINAL);
                    break;
                case 270:
                    out.append(PdfContents.ROTATE270);
                    out.append('0').append(' ');
                    out.append(page.getRight());
                    out.append(PdfContents.ROTATEFINAL);
                    break;
            }
        }
        
        private void addDocumentField(PdfIndirectReference ref) {
            if (cstp.fieldArray == null)
                cstp.fieldArray = new PdfArray();
            cstp.fieldArray.add(ref);
        }

        private void expandFields(PdfFormField field, ArrayList allAnnots) {
            allAnnots.add(field);
            ArrayList kids = field.getKids();
            if (kids != null) {
                for (int k = 0; k < kids.size(); ++k)
                    expandFields((PdfFormField)kids.get(k), allAnnots);
            }
        }

        public void addAnnotation(PdfAnnotation annot) {
            try {
                ArrayList allAnnots = new ArrayList();
                if (annot.isForm()) {
                    PdfFormField field = (PdfFormField)annot;
                    if (field.getParent() != null)
                        return;
                    expandFields(field, allAnnots);
                    if (cstp.fieldTemplates == null)
                        cstp.fieldTemplates = new HashMap();
                }
                else
                    allAnnots.add(annot);
                for (int k = 0; k < allAnnots.size(); ++k) {
                    annot = (PdfAnnotation)allAnnots.get(k);
                    if (annot.isForm()) {
                        if (!annot.isUsed()) {
                            HashMap templates = annot.getTemplates();
                            if (templates != null)
                                cstp.fieldTemplates.putAll(templates);
                        }
                        PdfFormField field = (PdfFormField)annot;
                        if (field.getParent() == null)
                            addDocumentField(field.getIndirectReference());
                    }
                    if (annot.isAnnotation()) {
                        PdfObject pdfobj = PdfReader.getPdfObject(pageN.get(PdfName.ANNOTS), pageN);
                        PdfArray annots = null;
                        if (pdfobj == null || !pdfobj.isArray()) {
                            annots = new PdfArray();
                            pageN.put(PdfName.ANNOTS, annots);
                        }
                        else 
                            annots = (PdfArray)pdfobj;
                        annots.add(annot.getIndirectReference());
                        if (!annot.isUsed()) {
                            PdfRectangle rect = (PdfRectangle)annot.get(PdfName.RECT);
                            if (rect != null && (rect.left() != 0 || rect.right() != 0 || rect.top() != 0 || rect.bottom() != 0)) {
                                int rotation = reader.getPageRotation(pageN);
                                Rectangle pageSize = reader.getPageSizeWithRotation(pageN);
                                switch (rotation) {
                                    case 90:
                                        annot.put(PdfName.RECT, new PdfRectangle(
                                        pageSize.getTop() - rect.bottom(),
                                        rect.left(),
                                        pageSize.getTop() - rect.top(),
                                        rect.right()));
                                        break;
                                    case 180:
                                        annot.put(PdfName.RECT, new PdfRectangle(
                                        pageSize.getRight() - rect.left(),
                                        pageSize.getTop() - rect.bottom(),
                                        pageSize.getRight() - rect.right(),
                                        pageSize.getTop() - rect.top()));
                                        break;
                                    case 270:
                                        annot.put(PdfName.RECT, new PdfRectangle(
                                        rect.bottom(),
                                        pageSize.getRight() - rect.left(),
                                        rect.top(),
                                        pageSize.getRight() - rect.right()));
                                        break;
                                }
                            }
                        }
                    }
                    if (!annot.isUsed()) {
                        annot.setUsed();
                        cstp.addToBody(annot, annot.getIndirectReference());
                    }
                }
            }
            catch (IOException e) {
                throw new ExceptionConverter(e);
            }
        }
    }
    
    public static class StampContent extends PdfContentByte {
        PageResources pageResources;
        
        /** Creates a new instance of StampContent */
        StampContent(PdfWriter writer, PageResources pageResources) {
            super(writer);
            this.pageResources = pageResources;
        }
        
        /**
         * Gets a duplicate of this <CODE>PdfContentByte</CODE>. All
         * the members are copied by reference but the buffer stays different.
         *
         * @return a copy of this <CODE>PdfContentByte</CODE>
         */
        public PdfContentByte getDuplicate() {
            return new PdfCopy.StampContent(writer, pageResources);
        }
        
        PageResources getPageResources() {
            return pageResources;
        }
    }
}
