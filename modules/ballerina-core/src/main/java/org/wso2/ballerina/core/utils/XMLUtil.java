package org.wso2.ballerina.core.utils;

import net.sf.saxon.om.Sequence;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.tree.tiny.TinyElementImpl;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMContainer;
import org.apache.axiom.om.OMDocument;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.impl.jaxp.OMSource;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.jaxen.JaxenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerina.core.model.types.XMLType;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A Util class which has XML processing utility methods
 */
public class XMLUtil {

    private static Processor processor = new Processor(false);

    private static final Logger LOG = LoggerFactory.getLogger(Processor.class);

    public static void main(String[] args) {
        String x = "<bookstore>\n" + "\n" + "<book>\n" + "  <title lang=\"eng\">Harry Potter</title>\n"
                + "  <price>29.99</price>\n" + "</book>\n" + "\n" + "<book>\n"
                + "  <title lang=\"eng\">Learning XML</title>\n" + "  <price>39.95</price>\n" + "</book>\n" + "\n"
                + "</bookstore>";
        byte[] bytes = x.getBytes();
        InputStream inputStream = new ByteArrayInputStream(bytes);
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();

        try {
            XMLStreamReader streamReader = xmlInputFactory.createXMLStreamReader(inputStream, "UTF-8");
            StAXOMBuilder builder = new StAXOMBuilder(streamReader);
            OMDocument om = builder.getDocument();

            XMLType xmlType = new XMLType(om.getOMDocumentElement());

            set(xmlType, "/*/book[1]/title/@lang", null, "test");

            //            System.out.println(get(xmlType, "/A/B", null));
            //            System.out.println(get(xmlType, "/A/B/C/text()", null));

        } catch (XMLStreamException e) {
            e.printStackTrace();
        }

    }

    /**
     * Evaluate xpath using Saxon-HE library.
     *
     * @param xmlType    Original message which needs to be used for evaluate xpath
     * @param xpath      xpath String to be used for evaluation
     * @param nameSpaces namespaces map
     * @return Object this can be XMLType or String
     */
    public static Object get(XMLType xmlType, String xpath, Map<String, String> nameSpaces) {
        try {

            XPathCompiler xPathCompiler = processor.newXPathCompiler();

            DocumentBuilder builder = processor.newDocumentBuilder();

            XdmNode doc = builder.build(new OMSource(xmlType.getOmElement()));

            if (nameSpaces != null && !nameSpaces.isEmpty()) {
                for (String key : nameSpaces.keySet()) {
                    xPathCompiler.declareNamespace(key, nameSpaces.get(key));
                }
            }
            XPathSelector selector = xPathCompiler.compile(xpath).load();

            selector.setContextItem(doc);

            XdmValue xdmValue = selector.evaluate();
            Sequence sequence = xdmValue.getUnderlyingValue();

            if (!(sequence instanceof TinyElementImpl)) {
                return xdmValue.toString();
            } else {
                return new XMLType(xdmValue.toString());
            }
        } catch (SaxonApiException e) {
            LOG.error("Cannot evaluate XPath , may be syntax of xpath is wrong", e);

        }
        return null;
    }

    /**
     * Method for modify XML messages
     * @param xmlType original message
     * @param xpath xpath location for set the value
     * @param nameSpaces namespaces to be added
     * @param value String value to be set
     */
    public static void set(XMLType xmlType, String xpath, Map<String, String> nameSpaces, String value) {
        try {
            AXIOMXPath axiomxPath = new AXIOMXPath(xpath);
            if (nameSpaces != null && !nameSpaces.isEmpty()) {
                for (String key : nameSpaces.keySet()) {
                    axiomxPath.addNamespace(key, nameSpaces.get(key));

                }
            }
            Object ob = axiomxPath.evaluate(xmlType.getOmElement());
            if (ob instanceof ArrayList) {
                List list = (List) ob;

                for (Object obj : list) {
                    if (obj instanceof OMNode) {
                        OMNode omNode = (OMNode) obj;
                        OMContainer omContainer = omNode.getParent();
                        omNode.detach();
                        OMAbstractFactory.getOMFactory().createOMText(omContainer, value);
                    } else if (obj instanceof OMAttribute) {
                        OMAttribute omAttribute = (OMAttribute) obj;
                        omAttribute.setAttributeValue(value);
                    }
                }
            }
        } catch (JaxenException e) {
            e.printStackTrace();
        }

    }

    /**
     *  Method for modify XML messages
     * @param xmlType original message
     * @param xpath xpath location for set the value
     * @param nameSpaces namespaces to be added
     * @param value String value to be set
     */
    public static void set(XMLType xmlType, String xpath, Map<String, String> nameSpaces, XMLType value) {
        try {
            AXIOMXPath axiomxPath = new AXIOMXPath(xpath);
            if (nameSpaces != null && !nameSpaces.isEmpty()) {
                for (String key : nameSpaces.keySet()) {
                    axiomxPath.addNamespace(key, nameSpaces.get(key));

                }
            }
            Object ob = axiomxPath.evaluate(xmlType.getOmElement());
            if (ob instanceof ArrayList) {
                List list = (List) ob;

                for (Object obj : list) {
                    if (obj instanceof OMNode) {
                        OMNode omNode = (OMNode) obj;
                        OMContainer omContainer = omNode.getParent();
                        omNode.detach();
                        omContainer.addChild(value.getOmElement());
                    }
                }
            }
        } catch (JaxenException e) {
            e.printStackTrace();
        }

    }
}
