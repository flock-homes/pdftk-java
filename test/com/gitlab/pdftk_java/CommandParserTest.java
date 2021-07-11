package com.gitlab.pdftk_java;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import com.gitlab.pdftk_java.TK_Session;

public class CommandParserTest {

  TK_Session parse(String... args) {
    TK_Session session = new TK_Session();
    session.parse(args);
    return session;
  }

  @Test
  public void TestEmpty() {
    TK_Session session = parse();
    assertFalse(session.is_valid());
  }

  @Test
  public void TestNoInputNoOp() {
    TK_Session session = parse("output", "-");
    assertFalse(session.is_valid());
  }

  @Test
  public void TestNoInput() {
    TK_Session session = parse("cat", "output", "-");
    assertFalse(session.is_valid());
  }

  @Test
  public void TestNoOutput() {
    TK_Session session = parse("test/files/blank.pdf");
    assertFalse(session.is_valid());
  }

  @Test
  public void TestTwoOutputs() {
    TK_Session session = parse("test/files/blank.pdf", "cat", "output", "-", "output", "-");
    assertFalse(session.is_valid());
  }

  @Test
  public void TestOperationAfterOutput() {
    TK_Session session = parse("test/files/blank.pdf", "output", "-", "cat");
    assertFalse(session.is_valid());
  }

  @Test
  public void TestNoOperationFilter() {
    TK_Session session = parse("test/files/blank.pdf", "output", "-");
    assertTrue(session.is_valid());
  }

  @Test
  public void TestNoOperationCat() {
    TK_Session session = parse("test/files/blank.pdf", "test/files/blank.pdf", "output", "-");
    assertTrue(session.is_valid());
  }

}
