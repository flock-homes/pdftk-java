import static org.junit.Assert.assertEquals;
import org.junit.Test;
import java.io.IOException;

import com.gitlab.pdftk_java.pdftk;

public class DataTest extends BlackBoxTest {
  @Test
  public void dump_data() throws IOException {
    exit.expectSystemExitWithStatus(0);
    pdftk.main(new String[]{"test/files/blank.pdf", "dump_data_utf8"});
    String expectedData = slurp("test/files/blank.data");
    assertEquals(expectedData, systemOutRule.getLog());
  }

  @Test
  public void update_info_incomplete_record() {
    exit.expectSystemExitWithStatus(0);
    systemInMock.provideLines("InfoBegin", "InfoKey: Title", " ","InfoBegin", "InfoKey: Author", " ");
    pdftk.main(new String[]{"test/files/blank.pdf", "update_info", "-", "output", "-"});
  }
};
