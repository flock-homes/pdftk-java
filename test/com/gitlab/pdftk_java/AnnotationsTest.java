package com.gitlab.pdftk_java;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import java.io.IOException;

public class AnnotationsTest extends BlackBox {
  @Test
  public void dump_links() throws IOException {
    pdftk("test/files/refs.pdf", "dump_data_annots");
    String expectedData = slurp("test/files/refs_annots.txt");
    assertEquals(expectedData, systemOut.getLog());
  }

  @Test
  public void dump_text() throws IOException {
    pdftk("test/files/annotation.pdf", "dump_data_annots");
    String expectedData = slurp("test/files/annotation_annots.txt");
    assertEquals(expectedData, systemOut.getLog());
  }
};
