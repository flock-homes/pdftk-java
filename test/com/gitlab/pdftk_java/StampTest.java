package com.gitlab.pdftk_java;

import org.junit.Test;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class StampTest extends BlackBox {

  void stdin_operation(String operation) throws IOException {
    byte[] expected = getPdf("test/files/blank.pdf", operation, "test/files/duck.pdf", "output", "-");
    InputStream stdinMock = new FileInputStream("test/files/blank.pdf");
    InputStream originalIn = System.in;
    System.setIn(stdinMock);
    byte[] actual = getPdf("-", operation, "test/files/duck.pdf", "output", "-");
    System.setIn(originalIn);
    assertPdfEqualsAsSVG(expected, actual);
  }

  void operation_stdin(String operation) throws IOException {
    byte[] expected = getPdf("test/files/blank.pdf", operation, "test/files/duck.pdf", "output", "-");
    InputStream stdinMock = new FileInputStream("test/files/duck.pdf");
    InputStream originalIn = System.in;
    System.setIn(stdinMock);
    byte[] actual = getPdf("test/files/blank.pdf", operation, "-", "output", "-");
    System.setIn(originalIn);
    assertPdfEqualsAsSVG(expected, actual);
  }

  @Test
  public void stdin_background() throws IOException {
    stdin_operation("background");
  }

  @Test
  public void stdin_multibackground() throws IOException {
    stdin_operation("multibackground");
  }

  @Test
  public void stdin_stamp() throws IOException {
    stdin_operation("stamp");
  }

  @Test
  public void stdin_multistamp() throws IOException {
    stdin_operation("multistamp");
  }

  @Test
  public void background_stdin() throws IOException {
    operation_stdin("background");
  }

  @Test
  public void multibackground_stdin() throws IOException {
    operation_stdin("multibackground");
  }

  @Test
  public void stamp_stdin() throws IOException {
    operation_stdin("stamp");
  }

  @Test
  public void multistamp_stdin() throws IOException {
    operation_stdin("multistamp");
  }

};
