/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.xslt;

import org.apache.axiom.om.OMDocument;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.impl.llom.OMDocumentImpl;
import org.apache.axiom.om.util.AXIOMUtil;
import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ArrayValueImpl;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.XMLItem;
import org.ballerinalang.jvm.values.XMLSequence;
import org.ballerinalang.jvm.values.XMLValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringWriter;
import java.util.Iterator;

import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamResult;

/**
 * Transforms XML to another XML/HTML/plain text using XSLT.
 *
 * @since 0.995.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "xslt",
        functionName = "transform",
        args = {@Argument(name = "input", type = TypeKind.XML),
                @Argument(name = "xsl", type = TypeKind.XML)},
        returnType = {@ReturnType(type = TypeKind.XML)},
        isPublic = true
)
public class XsltTransformer {
    
    private static final Logger log = LoggerFactory.getLogger(XsltTransformer.class);
    private static final String XSLT_ERROR_RECORD = "XSLTError";
    private static final String XSLT_ERROR_CODE = "{ballerina/xslt}" + XSLT_ERROR_RECORD;
    private static final String OPERATION = "Failed to perform XSL transformation: ";

    public static Object transform(Strand strand, XMLValue xmlInput, XMLValue xslInput) {
        try {
            String input = xmlInput.toString();
            String xsl = xslInput.toString();
            OMElement omXML = AXIOMUtil.stringToOM(input);
            OMElement omXSL = AXIOMUtil.stringToOM(xsl);

            StAXSource xmlSource = new StAXSource(omXML.getXMLStreamReader());
            StAXSource xslSource = new StAXSource(omXSL.getXMLStreamReader());

            StringWriter stringWriter = new StringWriter();
            StreamResult streamResult = new StreamResult(stringWriter);

            Transformer transformer = TransformerFactory.newInstance().newTransformer(xslSource);
            transformer.setOutputProperty("omit-xml-declaration", "yes");
            transformer.transform(xmlSource, streamResult);

            String resultStr = stringWriter.getBuffer().toString().trim();
            if (log.isDebugEnabled()) {
                log.debug("Transformed result : {}", resultStr);
            }

            if (resultStr.isEmpty()) {
                return createError(XSLT_ERROR_CODE, OPERATION + "empty result");
            } else {
                return parseToXML(resultStr);
            }

        } catch (ClassCastException e) {
            return createError(XSLT_ERROR_CODE, OPERATION + "invalid inputs(s)");
        } catch (Exception e) {
            String errMsg = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
            return createError(XSLT_ERROR_CODE, OPERATION + errMsg);
        }
    }

    /**
     * Converts the given string to a BXMLSequence object.
     *
     * @param xmlStr The string to be converted
     * @return The result BXMLSequence object
     * @throws XMLStreamException When converting `xmlStr` to an Axiom OMElement
     */
    @SuppressWarnings("unchecked")
    private static XMLSequence parseToXML(String xmlStr) throws XMLStreamException {
        // Here we add a dummy enclosing tag, and send it to AXIOM to parse the XML.
        // This is to overcome the issue of AXIOM not allowing to parse XML-comments,
        // XML-text nodes, and PI nodes, without having a XML-element node.
        OMElement omElement = AXIOMUtil.stringToOM("<root>" + xmlStr + "</root>");
        Iterator<OMNode> children = omElement.getChildren();

        // Here we go through the iterator and add all the children nodes to a BRefValueArray.
        // The BRefValueArray is used to create a BXMLSequence object.
        ArrayValue omNodeArray = new ArrayValueImpl(new BArrayType(BTypes.typeXML));
        OMDocument omDocument;
        OMNode omNode;
        int omNodeIndex = 0;
        while (children.hasNext()) {
            omNode = children.next();
            // Here the OMNode is detached from the dummy root, and added to a document element.
            // This is to get the XPath working correctly.
            children.remove();
            omDocument = new OMDocumentImpl();
            omDocument.addChild(omNode);
            omNodeArray.add(omNodeIndex, new XMLItem(omNode));
            omNodeIndex++;
        }
        return new XMLSequence(omNodeArray);
    }

    private static ErrorValue createError(String reason, String errMsg) {
        return BallerinaErrors.createError(reason, errMsg);
    }
}
