package c.lev.tftp;

import org.junit.Test;

import javax.xml.parsers.SAXParserFactory;

public class SAXTest {
    @Test
    public void printSAXFactoryName() {
        System.out.println(SAXParserFactory.newInstance().getClass().getName());
    }
}
