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
  public void cat_exclude_range() {
    byte[] expected = getPdf("test/files/refs.pdf", "cat", "1-3", "6-8", "output", "-");
    byte[] actual = getPdf("test/files/refs.pdf", "cat", "~4-5", "output", "-");
    assertPdfEqualsAsSVG(expected, actual);
  }

  @Test
  public void cat_include_exclude_range() {
    byte[] expected = getPdf("test/files/refs.pdf", "cat", "2-3", "6-7", "output", "-");
    byte[] actual = getPdf("test/files/refs.pdf", "cat", "2-end~4-5~end", "output", "-");
    assertPdfEqualsAsSVG(expected, actual);
  }

  @Test
  public void cat_even() {
    byte[] expected = getPdf("test/files/refs.pdf", "cat", "2", "4", "6", "output", "-");
    byte[] actual = getPdf("test/files/refs.pdf", "cat", "2-7even", "output", "-");
    assertPdfEqualsAsSVG(expected, actual);
  }

  @Test
  public void cat_odd() {
    byte[] expected = getPdf("test/files/refs.pdf", "cat", "3", "5", "7", "output", "-");
    byte[] actual = getPdf("test/files/refs.pdf", "cat", "2-7odd", "output", "-");
    assertPdfEqualsAsSVG(expected, actual);
  }

  @Test
  public void cat_handles() {
    byte[] expected = getPdf("test/files/refs.pdf", "test/files/refsalt.pdf", "cat", "output", "-");
    byte[] actual = getPdf("A=test/files/refs.pdf", "B=test/files/refsalt.pdf", "cat", "B", "A", "output", "-");
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
