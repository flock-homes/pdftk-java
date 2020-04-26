import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import java.io.IOException;

import com.gitlab.pdftk_java.pdftk;

public class DataTest extends BlackBox {
  @Test
  public void dump_data() throws IOException {
    pdftk("test/files/blank.pdf", "dump_data_utf8");
    String expectedData = slurp("test/files/blank.data");
    assertEquals(expectedData, systemOut.getLog());
  }

  @Test
  public void update_info_incomplete_record() {
    systemIn.provideLines("InfoBegin", "InfoKey: Title", " ","InfoBegin", "InfoKey: Author", " ");
    pdftk("test/files/blank.pdf", "update_info", "-", "output", "-");
    assertThat(systemErr.getLog(), containsString("data info record not valid"));
  }

  @Test
  public void update_page_labels_new() {
    String output = tmpDirectory.getRoot().getPath()+"/output.pdf";
    String[] data = {"PageLabelBegin",
                     "PageLabelNewIndex: 3",
                     "PageLabelStart: 3",
                     "PageLabelPrefix: p",
                     "PageLabelNumStyle: LowercaseRomanNumerals"};
    systemIn.provideLines(data);
    pdftk("test/files/refs.pdf", "update_info", "-", "output", output);
    pdftk(output, "dump_data_utf8");
    assertThat(systemOut.getLog(), containsString(String.join("\n",data)));
  }

  @Test
  public void update_page_labels_replace() {
    String output1 = tmpDirectory.getRoot().getPath()+"/output1.pdf";
    String output2 = tmpDirectory.getRoot().getPath()+"/output2.pdf";
    String[] data1 = {"PageLabelBegin",
                      "PageLabelNewIndex: 3",
                      "PageLabelStart: 3",
                      "PageLabelNumStyle: LowercaseRomanNumerals"};
    String[] data2 = {"PageLabelBegin",
                      "PageLabelNewIndex: 4",
                      "PageLabelStart: 4",
                      "PageLabelNumStyle: UppercaseRomanNumerals"};
    systemIn.provideLines(data1);
    pdftk("test/files/refs.pdf", "update_info", "-", "output", output1);
    systemIn.provideLines(data2);
    pdftk(output1, "update_info", "-", "output", output2);
    pdftk(output2, "dump_data_utf8");
    assertThat(systemOut.getLog(), not(containsString(String.join("\n",data1))));
    assertThat(systemOut.getLog(), containsString(String.join("\n",data2)));
  }

  @Test
  public void update_page_labels_badindex() {
    systemIn.provideLines("PageLabelBegin",
                          "PageLabelNewIndex: -1",
                          "PageLabelStart: 3",
                          "PageLabelNumStyle: LowercaseRomanNumerals");
    pdftk("test/files/refs.pdf", "update_info", "-", "output", "-");
    assertThat(systemErr.getLog(), containsString("page label record not valid"));
  }

  @Test
  public void update_page_labels_badstart() {
    systemIn.provideLines("PageLabelBegin",
                          "PageLabelNewIndex: 3",
                          "PageLabelStart: -1",
                          "PageLabelNumStyle: LowercaseRomanNumerals");
    pdftk("test/files/refs.pdf", "update_info", "-", "output", "-");
    assertThat(systemErr.getLog(), containsString("page label record not valid"));
  }

  @Test
  public void update_page_labels_badstyle() {
    systemIn.provideLines("PageLabelBegin",
                          "PageLabelNewIndex: 3",
                          "PageLabelStart: 3",
                          "PageLabelNumStyle: NotAStyle");
    pdftk("test/files/refs.pdf", "update_info", "-", "output", "-");
    assertThat(systemErr.getLog(), containsString("PageLabelNumStyle: invalid value NotAStyle"));
    assertThat(systemErr.getLog(), containsString("page label record not valid"));
  }

  @Test
  public void update_page_media_replace() {
    String output = tmpDirectory.getRoot().getPath()+"/output.pdf";
    String[] data = {"PageMediaBegin",
                     "PageMediaNumber: 3",
                     "PageMediaRotation: 90",
                     "PageMediaRect: 1 1 611 791",
                     "PageMediaCropRect: 2 2 610 792"};
    String[] expect = {data[0],data[1],data[2],data[3],"PageMediaDimensions: 610 790",data[4]};
    systemIn.provideLines(data);
    pdftk("test/files/refs.pdf", "update_info", "-", "output", output);
    pdftk(output, "dump_data_utf8");
    assertThat(systemOut.getLog(), containsString(String.join("\n",expect)));
  }

  @Test
  public void update_page_media_badpage() {
    systemIn.provideLines("PageMediaBegin",
                          "PageMediaNumber: 42",
                          "PageMediaRotation: 90",
                          "PageMediaRect: 1 1 611 791",
                          "PageMediaCropRect: 2 2 610 792");
    pdftk_error(3, "test/files/refs.pdf", "update_info", "-", "output", "-");
    assertThat(systemErr.getLog(), containsString("page 42 not found"));
  }

  @Test
  public void update_page_media_badrotation() {
    systemIn.provideLines("PageMediaBegin",
                          "PageMediaNumber: 3",
                          "PageMediaRotation: 45",
                          "PageMediaRect: 1 1 611 791",
                          "PageMediaCropRect: 2 2 610 792");
    pdftk("test/files/refs.pdf", "update_info", "-", "output", "-");
    assertThat(systemErr.getLog(), containsString("page media record not valid"));
  }

  @Test
  public void update_page_media_badrect() {
    systemIn.provideLines("PageMediaBegin",
                          "PageMediaNumber: 3",
                          "PageMediaRect: 1 1 611");
    pdftk("test/files/refs.pdf", "update_info", "-", "output", "-");
    assertThat(systemErr.getLog(), containsString("page media record not valid"));
  }

};
