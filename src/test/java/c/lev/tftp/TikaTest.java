package c.lev.tftp;

import org.apache.tika.Tika;
import org.junit.Test;

public class TikaTest {
    @Test
    public void testNotFoundMime() {
        byte[] buffer = new byte[512];
        System.out.println(new Tika().detect(buffer));
    }
}
