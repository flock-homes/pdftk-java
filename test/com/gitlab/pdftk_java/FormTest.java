package com.gitlab.pdftk_java;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;
import org.junit.Test;
import java.io.IOException;

public class FormTest extends BlackBox {
  @Test
  public void dump_data_fields() throws IOException {
    pdftk("test/files/form.pdf", "dump_data_fields");
    String expectedData = slurp("test/files/form.data");
    assertEquals(expectedData, systemOut.getLog());
  }

  @Test
  public void generate_fdf() throws IOException {
    pdftk("test/files/form.pdf", "generate_fdf", "output", "-");
    byte[] expectedData = slurpBytes("test/files/form.fdf");
    assertArrayEquals(expectedData, systemOut.getLogAsBytes());
  }

  @Test
  public void fill_from_fdf() throws IOException {
    pdftk("test/files/form.pdf", "fill_form", "test/files/form-filled.fdf", "output", "-");
  }

};
