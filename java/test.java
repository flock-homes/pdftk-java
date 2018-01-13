import org.apache.commons.lang3.StringEscapeUtils;

public class test {
  public static void main(String args[]) {
    System.out.println("aaa".matches("a*"));
    System.out.println(StringEscapeUtils.unescapeXml("&<'>&amp;&lt;<>"));
  }
}
