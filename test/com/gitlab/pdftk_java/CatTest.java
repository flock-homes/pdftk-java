package com.gitlab.pdftk_java;

import org.junit.Test;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class CatTest extends BlackBox {
  @Test
  public void cat() throws IOException {
    byte[] expected = slurpBytes("test/files/cat-refs-refsalt.pdf");
    pdftk("test/files/refs.pdf", "test/files/refsalt.pdf",
          "cat", "output", "-");
    assertPdfEqualsAsSVG(expected, systemOut.getLogAsBytes());
  }

  @Test
  public void cat_rotate_page_no_op() {
    byte[] expected = getPdf("test/files/blank.pdf", "cat", "output", "-");
    byte[] actual = getPdf("test/files/blank.pdf", "cat", "1north", "output", "-");
    assertPdfEqualsAsSVG(expected, actual);
  }

  @Test
  public void cat_rotate_range_no_op() {
    byte[] expected = getPdf("test/files/blank.pdf", "cat", "output", "-");
    byte[] actual = getPdf("test/files/blank.pdf", "cat", "1-1north", "output", "-");
    assertPdfEqualsAsSVG(expected, actual);
  }

  @Test
  public void cat_rotate_page() {
    pdftk("test/files/blank.pdf", "cat", "1east", "output", "-");
  }

  @Test
  public void cat_rotate_range() {
    byte[] expected = getPdf("test/files/blank.pdf", "cat", "1east", "output", "-");
    byte[] actual = getPdf("test/files/blank.pdf", "cat", "1-1east", "output", "-");
    assertPdfEqualsAsSVG(expected, actual);
  }

  @Test
  public void duplicate_stdin() throws IOException {
    InputStream stdinMock = new FileInputStream("test/files/blank.pdf");
    InputStream originalIn = System.in;
    System.setIn(stdinMock);
    pdftk("A=-", "cat", "A", "A", "A", "output", "-");
    System.setIn(originalIn);
  }
};
