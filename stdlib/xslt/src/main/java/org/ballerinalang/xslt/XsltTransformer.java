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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.XMLFactory;
import org.ballerinalang.jvm.XMLNodeType;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.XMLItem;
import org.ballerinalang.jvm.values.XMLSequence;
import org.ballerinalang.jvm.values.XMLValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringWriter;

import javax.xml.namespace.QName;
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
public class XsltTransformer {
    
    private static final Logger log = LoggerFactory.getLogger(XsltTransformer.class);
    private static final String XSLT_ERROR_RECORD = "XSLTError";
    private static final String XSLT_ERROR_CODE = "{ballerina/xslt}" + XSLT_ERROR_RECORD;
    private static final String OPERATION = "Failed to perform XSL transformation: ";

    public static Object transform(XMLValue xmlInput, XMLValue xslInput) {
        try {
            boolean unwrap = false;
            if (xmlInput.getNodeType() == XMLNodeType.SEQUENCE) {
                XMLItem wrapper = new XMLItem(new QName("root"), (XMLSequence) xmlInput);
                xmlInput = wrapper;
                unwrap = true;
            }
            String input = xmlInput.toString();
            // Remove <root></root> wrapper
            if (unwrap) {
                input = input.substring(6, input.length() - 7).trim();
            }

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
     */
    private static XMLSequence parseToXML(String xmlStr) throws XMLStreamException {
        return (XMLSequence) XMLFactory.parse(xmlStr);
    }

    private static ErrorValue createError(String reason, String errMsg) {
        return BallerinaErrors.createError(reason, errMsg);
    }
}
