import static org.junit.Assert.assertEquals;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;

public class BlackBoxTest {
  @Rule
  public final SystemOutRule systemOutRule = new SystemOutRule().muteForSuccessfulTests();

  @Rule
  public final ExpectedSystemExit exit = ExpectedSystemExit.none();
  
  @Test
  public void dump_data() throws IOException {
    exit.expectSystemExitWithStatus(0);
    pdftk.main(new String[]{"test/files/blank.pdf", "dump_data_utf8"});
    String expectedData = new String(Files.readAllBytes(Paths.get("test/files/blank.data")));
    assertEquals(expectedData, systemOutRule.getLog());    
  }
};
