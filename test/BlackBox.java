import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.fail;
import org.junit.Rule;
import org.junit.contrib.java.lang.system.SystemErrRule;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.junit.contrib.java.lang.system.TextFromStandardInputStream;
import org.junit.rules.TemporaryFolder;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.ByteArrayOutputStream;
import java.lang.Process;
import java.lang.Runtime;

import com.gitlab.pdftk_java.pdftk;

// Utilities to write black-box tests, that is we provide a mock
// command line and look at the output.
public class BlackBox {
  @Rule
  public final SystemOutRule systemOut =
    new SystemOutRule().enableLog().muteForSuccessfulTests();

  @Rule
  public final SystemErrRule systemErr =
    new SystemErrRule().enableLog();

  @Rule
  public final TextFromStandardInputStream systemIn
    = TextFromStandardInputStream.emptyStandardInputStream();

  @Rule
  public final TemporaryFolder tmpDirectory = new TemporaryFolder();

  public String slurp(String filename) throws IOException {
    return new String(slurpBytes(filename));
  }
  public byte[] slurpBytes(String filename) throws IOException {
    return Files.readAllBytes(Paths.get(filename));
  }

  // Mock a command line call
  public void pdftk_error(int expected_error, String... args) {
    int error = pdftk.main_noexit(args);
    assertEquals(expected_error, error);
  }
  public void pdftk(String... args) {
    pdftk_error(0, args);
  }

  // Capture the output of a command line call.
  //
  // TODO: Ideally this should already be handled by SystemOutRule,
  // but it seems that no more output is produced after one call to
  // pdftk() and SystemOutRule.clear() is not enough.
  public byte[] getPdf(String... args) {
    PrintStream orig = System.out;
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputStream));
    pdftk(args);
    System.setOut(orig);
    return outputStream.toByteArray();
  }

  // For compatibility with Java < 9
  private byte[] readAllBytes(InputStream inputStream) throws IOException {
    final int bufferSize = 0x2000;
    byte[] buffer = new byte[bufferSize];
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    while (true) {
      int readBytes = inputStream.read(buffer, 0, bufferSize);
      if (readBytes < 0) break;
      outputStream.write(buffer, 0, readBytes);
    }
    return outputStream.toByteArray();
  }

  // Convert a PDF into an SVG using pdftocairo from Poppler
  private byte[] pdfToSVG(byte[] pdf) throws IOException {
    Process process = Runtime.getRuntime().exec(new String[]{"pdftocairo", "-svg", "-","-"});
    OutputStream pdfStream = process.getOutputStream();
    InputStream psStream = process.getInputStream();
    pdfStream.write(pdf);
    pdfStream.close();
    return readAllBytes(psStream);
  }

  // Compare two PDFs by checking that their SVG representations are
  // equal. This ignores any differences in forms, links, bookmarks,
  // etc.
  //
  // Note that we cannot compare two PDFs byte-by-byte because they
  // have time-sensitive data, and even after removing that there can
  // be harmless differences such as objects being reordered or
  // renamed.
  public void assertPdfEqualsAsSVG(byte[] pdf1, byte[] pdf2) {
    try {
      byte[] svg1 = pdfToSVG(pdf1);
      byte[] svg2 = pdfToSVG(pdf2);
      assertArrayEquals(svg1,svg2);
    }
    catch (IOException e) {
      fail("pdftocairo error");
    }
  }

};
