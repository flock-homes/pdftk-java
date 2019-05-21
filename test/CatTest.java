import static org.junit.Assert.assertEquals;
import org.junit.Test;
import java.io.IOException;

import com.gitlab.pdftk_java.pdftk;

public class CatTest extends BlackBoxTest {
  @Test
  public void cat() throws IOException {
    exit.expectSystemExitWithStatus(0);
    pdftk.main(new String[]{"test/files/refs.pdf",
                            "test/files/refsalt.pdf",
                            "cat", "output", "-"});
    byte[] expectedData = slurpBytes("test/files/cat-refs-refsalt.pdf");
    assertEquals(expectedData, systemOutRule.getLogAsBytes());
  }

  @Test
  public void cat_rotate_page_no_op() throws IOException {
    exit.expectSystemExitWithStatus(0);
    pdftk.main(new String[]{"test/files/blank.pdf",
                            "cat", "1north", "output", "-"});
    byte[] expectedData = slurpBytes("test/files/blank.pdf");
    assertEquals(expectedData, systemOutRule.getLogAsBytes());
  }

  @Test
  public void cat_rotate_range_no_op() throws IOException {
    exit.expectSystemExitWithStatus(0);
    pdftk.main(new String[]{"test/files/blank.pdf",
                            "cat", "1-1north", "output", "-"});
    byte[] expectedData = slurpBytes("test/files/blank.pdf");
    assertEquals(expectedData, systemOutRule.getLogAsBytes());
  }

  @Test
  public void cat_rotate_page() throws IOException {
    exit.expectSystemExitWithStatus(0);
    pdftk.main(new String[]{"test/files/blank.pdf",
                            "cat", "1east", "output", "-"});
    byte[] expectedData = slurpBytes("test/files/blank.pdf");
    assertEquals(expectedData, systemOutRule.getLogAsBytes());
  }

  @Test
  public void cat_rotate_range() throws IOException {
    exit.expectSystemExitWithStatus(0);
    pdftk.main(new String[]{"test/files/blank.pdf",
                            "cat", "1-1east", "output", "-"});
    byte[] expectedData = slurpBytes("test/files/blank.pdf");
    assertEquals(expectedData, systemOutRule.getLogAsBytes());
  }
};
