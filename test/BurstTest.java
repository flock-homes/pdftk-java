import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;

import com.gitlab.pdftk_java.pdftk;

public class BurstTest extends BlackBox {
  @Rule
  public final TemporaryFolder tmpDirectory = new TemporaryFolder();

  @Test
  public void burst_issue18() throws IOException {
    exit.expectSystemExitWithStatus(0);
    String pattern = tmpDirectory.getRoot().getPath() + "/page%04d.pdf";
    pdftk.main(new String[]{"test/files/issue18.pdf", "burst", "output", pattern});
    Files.delete(Paths.get("doc_data.txt"));
  }
};
