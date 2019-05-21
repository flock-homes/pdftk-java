import org.junit.Rule;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.junit.contrib.java.lang.system.TextFromStandardInputStream;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;

import com.gitlab.pdftk_java.pdftk;

public class BlackBox {
  @Rule
  public final SystemOutRule systemOutRule =
    new SystemOutRule().muteForSuccessfulTests();

  @Rule
  public final ExpectedSystemExit exit = ExpectedSystemExit.none();

  @Rule
  public final TextFromStandardInputStream systemInMock
    = TextFromStandardInputStream.emptyStandardInputStream();

  public String slurp(String filename) throws IOException {
    return new String(slurpBytes(filename));
  }
  public byte[] slurpBytes(String filename) throws IOException {
    return Files.readAllBytes(Paths.get(filename));
  }
};
