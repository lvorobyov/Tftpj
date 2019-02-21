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
                "video/3gpp2",

                "video/x-matroska",
                "video/x-msvideo",

                "image/gif",
                "image/jpeg",
                "image/pjpeg",
                "image/png",
                "image/svg+xml",
                "image/tiff",
                "image/vnd.microsoft.icon",
                "image/vnd.wap.wbmp",
                "image/webp",

                "audio/basic",
                "audio/L24",
                "audio/mp4",
                "audio/aac",
                "audio/mpeg",
                "audio/ogg",
                "audio/vorbis",
                "audio/x-ms-wma",
                "audio/x-ms-wax",
                "audio/vnd.rn-realaudio",
                "audio/vnd.wave",
                "audio/webm"
        };
        for (String m: mime) {
            System.out.println(m + " = " + MIMETypeUtil.fileExtensionForMIMEType(m));
        }
    }
}
