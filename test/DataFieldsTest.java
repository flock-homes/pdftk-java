import static org.junit.Assert.assertEquals;
import org.junit.Test;
import java.io.IOException;

import com.gitlab.pdftk_java.pdftk;

public class DataFieldsTest extends BlackBox {
  @Test
  public void ignore_field_with_successor_names() throws IOException {
    exit.expectSystemExitWithStatus(0);
    pdftk.main(new String[]{"test/files/issue19.pdf", "dump_data_fields"});
    String expectedData = slurp("test/files/issue19.data");
    assertEquals(expectedData, systemOutRule.getLog());
  }

  @Test
  public void escape_unicode() throws IOException {
    exit.expectSystemExitWithStatus(0);
    pdftk.main(new String[]{"test/files/issue21.pdf", "dump_data_fields"});
    String expectedData = slurp("test/files/issue21.data");
    assertEquals(expectedData, systemOutRule.getLog());
  }
};
