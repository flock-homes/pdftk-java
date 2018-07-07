import static org.junit.Assert.assertEquals;
import org.junit.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;

public class BlackBoxTest {
  @Test
  public void dump_data() throws IOException {
    ByteArrayOutputStream stdout = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdout));
    pdftk.main(new String[]{"files/blank.pdf", "dump_data_utf8"});
    String data = stdout.toString();
    String expectedData = new String(Files.readAllBytes(Paths.get("files/blank.data")));
    assertEquals(expectedData, data);
  }
};
