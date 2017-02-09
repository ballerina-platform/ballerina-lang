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

import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.jaxen.JaxenException;
import org.jaxen.XPathSyntaxException;
import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.model.types.TypeEnum;
import org.wso2.ballerina.core.model.values.BMap;
import org.wso2.ballerina.core.model.values.BString;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.model.values.BXML;
import org.wso2.ballerina.core.nativeimpl.AbstractNativeFunction;
import org.wso2.ballerina.core.nativeimpl.annotations.Argument;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaFunction;
import org.wso2.ballerina.nativeimpl.lang.utils.ErrorHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Remove the element(s) that matches the given xPath. Namespaces are supported.
 */
@BallerinaFunction(
        packageName = "ballerina.lang.xml",
        functionName = "remove",
        args = {@Argument(name = "xml", type = TypeEnum.XML),
                @Argument(name = "xPath", type = TypeEnum.STRING),
                @Argument(name = "namespaces", type = TypeEnum.MAP)},
        isPublic = true
)
public class RemoveWithNamespaces extends AbstractNativeFunction {

    private static final String OPERATION = "remove element from xml";

    @Override
    public BValue[] execute(Context ctx) {
        try {
            // Accessing Parameters.
            BXML xml = (BXML) getArgument(ctx, 0);
            String xPath = getArgument(ctx, 1).stringValue();
            BMap<BString, BString> namespaces = (BMap) getArgument(ctx, 2);

            // Setting the value to XML
            AXIOMXPath axiomxPath = new AXIOMXPath(xPath);
            if (namespaces != null && !namespaces.isEmpty()) {
                for (BString entry : namespaces.keySet()) {
                    axiomxPath.addNamespace(entry.stringValue(), namespaces.get(entry).stringValue());
                }
            }
            Object ob = axiomxPath.evaluate(xml.value());
            if (ob instanceof ArrayList) {
                List<?> list = (List<?>) ob;

                for (Object obj : list) {
                    if (obj instanceof OMNode) {
                        OMNode omNode = (OMNode) obj;
                        omNode.detach();

                    }
                }
            }
        } catch (XPathSyntaxException e) {
            ErrorHandler.handleInvalidXPath(OPERATION, e);
        } catch (JaxenException e) {
            ErrorHandler.handleXPathException(OPERATION, e);
        } catch (Throwable e) {
            ErrorHandler.handleXPathException(OPERATION, e);
        }

        return VOID_RETURN;
    }
}
