/*
 * Copyright (c) 2016, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.nativeimpl.lang.xmls;

import net.sf.saxon.om.Sequence;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.tree.tiny.TinyAttributeImpl;
import net.sf.saxon.tree.tiny.TinyElementImpl;
import net.sf.saxon.tree.tiny.TinyTextImpl;
import net.sf.saxon.value.EmptySequence;
import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BXML;
import org.ballerinalang.nativeimpl.lang.utils.ErrorHandler;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.exceptions.BallerinaException;

/**
 * Evaluate xPath on a XML object and returns the matching XML object. Namespaces are supported
 */
@BallerinaFunction(
        packageName = "ballerina.lang.xmls",
        functionName = "getXml",
        args = {@Argument(name = "x", type = TypeEnum.XML),
                @Argument(name = "xPath", type = TypeEnum.STRING),
                @Argument(name = "namespaces", type = TypeEnum.MAP)},
        returnType = {@ReturnType(type = TypeEnum.XML)},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = {@Attribute(name = "value",
        value = "Evaluates the XPath on an XML object and returns the matching XML object. Namespaces are supported") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "x",
        value = "An XML object") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "xPath",
        value = "An XPath") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "namespaces",
        value = "A map object consisting of namespaces") })
@BallerinaAnnotation(annotationName = "Return", attributes = {@Attribute(name = "xml",
        value = "Matching XML object") })
public class GetXMLWithNamespaces extends AbstractNativeFunction {

    private static final String OPERATION = "get element from xml";

    @Override
    public BValue[] execute(Context ctx) {
        BValue result = null;
        try {
            // Accessing Parameters.
            BXML xml = (BXML) getArgument(ctx, 0);
            String xPath = getArgument(ctx, 1).stringValue();
            BMap<BString, BString> namespaces = (BMap) getArgument(ctx, 2);

            // Getting the value from XML
            Processor processor = new Processor(false);
            XPathCompiler xPathCompiler = processor.newXPathCompiler();
            DocumentBuilder builder = processor.newDocumentBuilder();
            XdmNode doc = builder.build(xml.value().getSAXSource(true));
            if (namespaces != null && !namespaces.isEmpty()) {
                for (BString entry : namespaces.keySet()) {
                    xPathCompiler.declareNamespace(entry.stringValue(), namespaces.get(entry).stringValue());
                }
            }
            XPathSelector selector = xPathCompiler.compile(xPath).load();
            selector.setContextItem(doc);
            XdmValue xdmValue = selector.evaluate();
            Sequence sequence = xdmValue.getUnderlyingValue();

            if (sequence instanceof EmptySequence) {
                ErrorHandler.logWarn(OPERATION, "The xpath '" + xPath + "' does not match any XML element.");
            } else if (sequence instanceof TinyElementImpl || sequence.head() instanceof TinyElementImpl) {
                result = new BXML(xdmValue.toString());
            } else if (sequence instanceof TinyAttributeImpl || sequence.head() instanceof TinyAttributeImpl) {
                throw new BallerinaException("The element matching path '" + xPath + "' is an attribute, but not a " +
                        "XML element.");
            } else if (sequence instanceof TinyTextImpl || sequence.head() instanceof TinyTextImpl) {
                throw new BallerinaException("The element matching path '" + xPath + "' is a text, but not a XML " +
                        "element.");
            } else {
                throw new BallerinaException("The element matching path '" + xPath + "' is not a XML element.");
            }
        } catch (SaxonApiException e) {
            ErrorHandler.handleXPathException(OPERATION, e);
        } catch (Throwable e) {
            ErrorHandler.handleXPathException(OPERATION, e);
        }
        //TinyAttributeImpl
        // Setting output value.
        return getBValues(result);
    }
}
