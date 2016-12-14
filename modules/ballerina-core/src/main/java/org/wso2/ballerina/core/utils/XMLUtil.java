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
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.jaxen.JaxenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerina.core.model.types.XMLType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A Util class which has XML processing utility methods
 */
public class XMLUtil {

    private static Processor processor = new Processor(false);

    private static final Logger LOG = LoggerFactory.getLogger(Processor.class);

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

            XdmNode doc = builder.build(xmlType.getOmElement().getSAXSource(true));

            if (nameSpaces != null && !nameSpaces.isEmpty()) {
                for (Map.Entry<String, String> entry : nameSpaces.entrySet()) {
                    xPathCompiler.declareNamespace(entry.getKey(), entry.getValue());
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
     *
     * @param xmlType    original message
     * @param xpath      xpath location for set the value
     * @param nameSpaces namespaces to be added
     * @param value      String value to be set
     */
    public static void set(XMLType xmlType, String xpath, Map<String, String> nameSpaces, String value) {
        try {
            AXIOMXPath axiomxPath = new AXIOMXPath(xpath);
            if (nameSpaces != null && !nameSpaces.isEmpty()) {
                for (Map.Entry<String, String> entry : nameSpaces.entrySet()) {
                    axiomxPath.addNamespace(entry.getKey(), entry.getValue());

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
            LOG.error("Cannot evaluate XPath , may be syntax of xpath is wrong", e);
        }

    }

    /**
     * Method for modify XML messages
     *
     * @param xmlType    original message
     * @param xpath      xpath location for set the value
     * @param nameSpaces namespaces to be added
     * @param value      String value to be set
     */
    public static void set(XMLType xmlType, String xpath, Map<String, String> nameSpaces, XMLType value) {
        try {
            AXIOMXPath axiomxPath = new AXIOMXPath(xpath);
            if (nameSpaces != null && !nameSpaces.isEmpty()) {
                for (Map.Entry<String, String> entry : nameSpaces.entrySet()) {
                    axiomxPath.addNamespace(entry.getKey(), (entry.getValue()));

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
            LOG.error("Cannot evaluate XPath , may be syntax of xpath is wrong", e);
        }

    }

    public static void remove(XMLType xmlType, String xpath, Map<String, String> nameSpaces) {
        try {
            AXIOMXPath axiomxPath = new AXIOMXPath(xpath);
            if (nameSpaces != null && !nameSpaces.isEmpty()) {
                for (Map.Entry<String, String> entry : nameSpaces.entrySet()) {
                    axiomxPath.addNamespace(entry.getKey(), entry.getValue());

                }
            }
            Object ob = axiomxPath.evaluate(xmlType.getOmElement());
            if (ob instanceof ArrayList) {
                List list = (List) ob;

                for (Object obj : list) {
                    if (obj instanceof OMNode) {
                        OMNode omNode = (OMNode) obj;
                        omNode.detach();

                    }
                }
            }
        } catch (JaxenException e) {
            LOG.error("Cannot evaluate XPath , may be syntax of xpath is wrong", e);
        }

    }
}
