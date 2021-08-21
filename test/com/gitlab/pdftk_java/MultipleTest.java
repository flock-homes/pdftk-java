package com.gitlab.pdftk_java;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MultipleTest extends BlackBox {
  private String[] getFormNames(String formData) {
    HashSet<String> formNames = new HashSet<String>();
    Pattern pattern = Pattern.compile("FieldName: (.*)");
    Matcher matcher = pattern.matcher(formData);
    while (matcher.find()) {
      formNames.add(matcher.group(1));
    }
    return formNames.toArray(new String[formNames.size()]);
  }

  @Test
  public void cat_renames_clashing_forms() {
    pdftk("test/files/form.pdf", "dump_data_fields_utf8");
    String[] original_fields = getFormNames(systemOut.getLog());
    systemOut.clearLog();
    String duplicated = tmpDirectory.getRoot().getPath()+"/dup.pdf";
    pdftk("A=test/files/form.pdf", "cat", "A", "A", "output", duplicated);
    pdftk(duplicated, "dump_data_fields_utf8");
    String[] duplicated_fields = getFormNames(systemOut.getLog());
    assertEquals(2*original_fields.length, duplicated_fields.length);
  }

  @Test
  public void can_fill_cat_form() {
    String duplicated = tmpDirectory.getRoot().getPath()+"/dup.pdf";
    pdftk("A=test/files/form.pdf", "cat", "A", "A", "output", duplicated);
    String fdf = tmpDirectory.getRoot().getPath()+"/dup.fdf";
    pdftk(duplicated, "generate_fdf", "output", fdf);
    pdftk(duplicated, "fill_form", fdf, "output", "-");
  }
};
