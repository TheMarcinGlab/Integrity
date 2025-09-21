package main.util;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;

public final class XmlUtils {
    private XmlUtils() {}

    public static DocumentBuilderFactory secureDbf() {
        try {
            var dbf = DocumentBuilderFactory.newInstance();
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
            dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            dbf.setXIncludeAware(false);
            dbf.setExpandEntityReferences(false);
            dbf.setNamespaceAware(false);
            return dbf;
        } catch (Exception e) {
            throw new RuntimeException("Unable to configure secure XML parser", e);
        }
    }
}

