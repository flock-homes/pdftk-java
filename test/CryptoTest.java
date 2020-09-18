import org.junit.Test;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import java.io.IOException;

import com.gitlab.pdftk_java.pdftk;

public class CryptoTest extends BlackBox {
  @Test
  public void idempotent() throws IOException {
    byte[] expected = slurpBytes("test/files/blank.pdf");
    String encrypted = tmpDirectory.getRoot().getPath()+"/encrypted.pdf";
    pdftk("test/files/blank.pdf", "output", encrypted, "encrypt_128bit", "user_pw", "correcthorsebatterystaple");
    byte[] actual = getPdf(encrypted, "input_pw", "correcthorsebatterystaple", "output", "-");
    assertPdfEqualsAsSVG(expected, actual);
  }

  @Test
  public void no_password_fails() {
    String encrypted = tmpDirectory.getRoot().getPath()+"/encrypted.pdf";
    pdftk("test/files/blank.pdf", "output", encrypted, "encrypt_128bit", "user_pw", "correcthorsebatterystaple");
    pdftk_error(1, encrypted, "output", "-");
    assertThat(systemErr.getLog(), containsString("Bad password"));
  }

  @Test
  public void wrong_password_fails() {
    String encrypted = tmpDirectory.getRoot().getPath()+"/encrypted.pdf";
    pdftk("test/files/blank.pdf", "output", encrypted, "encrypt_128bit", "user_pw", "correcthorsebatterystaple");
    pdftk_error(1, encrypted, "input_pw", "Tr0ub4dor&3", "output", "-");
    assertThat(systemErr.getLog(), containsString("Bad password"));
  }

};
