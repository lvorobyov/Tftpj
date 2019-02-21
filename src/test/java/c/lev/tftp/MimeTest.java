package c.lev.tftp;

import org.clapper.util.misc.MIMETypeUtil;
import org.junit.Test;

public class MimeTest {
    @Test
    public void testMimeToExtension() {
        String[] mime = new String[] {
                "video/mpeg",
                "video/mp4",
                "video/ogg",
                "video/quicktime",
                "video/webm",
                "video/x-ms-wmv",
                "video/x-flv",
                "video/3gpp",
                "video/3gpp2"
        };
        for (String m: mime) {
            System.out.println(m + " = " + MIMETypeUtil.fileExtensionForMIMEType(m));
        }
    }
}
