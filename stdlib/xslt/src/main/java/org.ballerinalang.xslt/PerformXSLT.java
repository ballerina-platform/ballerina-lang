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
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BLangVMErrors;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.model.values.BXMLItem;
import org.ballerinalang.model.values.BXMLSequence;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

import java.io.StringWriter;
import java.util.Iterator;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamResult;

import static org.ballerinalang.util.BLangConstants.BALLERINA_PACKAGE_PREFIX;

/**
 * Transforms XML to another XML/HTML/plain text using XSLT.
 *
 * @since 0.995.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "xslt",
        functionName = "performXSLT",
        args = {@Argument(name = "input", type = TypeKind.XML),
                @Argument(name = "xsl", type = TypeKind.XML)},
        returnType = {@ReturnType(type = TypeKind.XML)},
        isPublic = true
)
public class PerformXSLT extends BlockingNativeCallableUnit {

    private static final String XSLT_ERROR_RECORD = "XSLTError";
    private static final String XSLT_ERROR_CODE = "{ballerina/xslt}" + XSLT_ERROR_RECORD;
    private static final String PROTOCOL_PACKAGE_XSLT = BALLERINA_PACKAGE_PREFIX + "xslt";
    private static final String OPERATION = "Failed to perform XSL transformation: ";

    @Override
    public void execute(Context ctx) {
        BValue result;
        try {
            String input = ctx.getRefArgument(0).toString();
            String xsl = ctx.getRefArgument(1).toString();
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

            if (resultStr.isEmpty()) {
                ctx.setReturnValues();
            } else {
                result = parseToBXML(resultStr);
                ctx.setReturnValues(result);
            }

        } catch (ClassCastException e) {
            ctx.setReturnValues(createError(ctx, OPERATION + "invalid inputs(s)"));
        } catch (Exception e) {
            ctx.setReturnValues(createError(ctx, OPERATION + e.getMessage()));
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
    private BXMLSequence parseToBXML(String xmlStr) throws XMLStreamException {
        // Here we add a dummy enclosing tag, and send it to AXIOM to parse the XML.
        // This is to overcome the issue of AXIOM not allowing to parse XML-comments,
        // XML-text nodes, and PI nodes, without having a XML-element node.
        OMElement omElement = AXIOMUtil.stringToOM("<root>" + xmlStr + "</root>");
        Iterator<OMNode> children = omElement.getChildren();

        // Here we go through the iterator and add all the children nodes to a BRefValueArray.
        // The BRefValueArray is used to create a BXMLSequence object.
        BValueArray omNodeArray = new BValueArray();
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
            omNodeArray.add(omNodeIndex, new BXMLItem(omNode));
            omNodeIndex++;
        }
        return new BXMLSequence(omNodeArray);
    }

    private BError createError(Context context, String errMsg) {
        BMap<String, BValue> xsltErrorRecord = BLangConnectorSPIUtil.createBStruct(context, PROTOCOL_PACKAGE_XSLT,
                                                                                   XSLT_ERROR_RECORD);
        xsltErrorRecord.put("message", new BString(errMsg));
        return BLangVMErrors.createError(context, true, BTypes.typeError, XSLT_ERROR_CODE, xsltErrorRecord);
    }
}
