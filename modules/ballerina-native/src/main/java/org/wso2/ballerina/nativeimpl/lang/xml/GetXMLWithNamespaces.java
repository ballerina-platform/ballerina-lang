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
package org.wso2.ballerina.nativeimpl.lang.xml;

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
import org.wso2.ballerina.core.exception.BallerinaException;
import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.model.types.TypeEnum;
import org.wso2.ballerina.core.model.values.BMap;
import org.wso2.ballerina.core.model.values.BString;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.model.values.BXML;
import org.wso2.ballerina.core.nativeimpl.AbstractNativeFunction;
import org.wso2.ballerina.core.nativeimpl.annotations.Argument;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaFunction;
import org.wso2.ballerina.core.nativeimpl.annotations.ReturnType;
import org.wso2.ballerina.nativeimpl.lang.utils.ErrorHandler;

/**
 * Evaluate xPath on a XML object and returns the matching XML object. Namespaces are supported
 */
@BallerinaFunction(
        packageName = "ballerina.lang.xml",
        functionName = "getXml",
        args = {@Argument(name = "e", type = TypeEnum.XML),
                @Argument(name = "xPath", type = TypeEnum.STRING),
                @Argument(name = "namespaces", type = TypeEnum.MAP)},
        returnType = {@ReturnType(type = TypeEnum.XML)},
        isPublic = true
)
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
