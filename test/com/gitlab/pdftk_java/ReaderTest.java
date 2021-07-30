package com.gitlab.pdftk_java;

import org.junit.Test;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.containsString;

public class ReaderTest extends BlackBox {
  @Test
  public void mergerequest21() {
    pdftk_error(1, "test/files/CVE-2007-0103_AcrobatReader", "output", "/dev/null");
    assertThat(systemErr.getLog(), containsString("Invalid reference on Kids"));
  }

  public void mergerequest21_2() {
    pdftk_error(1, "test/files/CVE-2007-0103_AcrobatReader_mutated", "output", "/dev/null");
    assertThat(systemErr.getLog(), containsString("Invalid reference on Kids"));
  }
};
