/**
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 **/

package org.ballerinalang.nativeimpl.lang.xmls;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMContainer;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BXML;
import org.ballerinalang.nativeimpl.lang.utils.ErrorHandler;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.jaxen.JaxenException;
import org.jaxen.XPathSyntaxException;

import java.util.ArrayList;
import java.util.List;

/**
 * Set the string value of a element that matches the given xPath.
 * If the xPath matches to an existing element, this method will update the value of it.
 * If the xPath does not match to an existing element, this method will add a new element
 * to match the xPath.
 */
@BallerinaFunction(
        packageName = "ballerina.lang.xmls",
        functionName = "set",
        args = {@Argument(name = "x", type = TypeEnum.XML),
                @Argument(name = "xPath", type = TypeEnum.STRING),
//                @Argument(name = "nameSpaces", type = TypeEnum.MAP),
                @Argument(name = "value", type = TypeEnum.STRING)},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = {@Attribute(name = "value",
        value = "Sets the string value of an element that matches the given XPath. "
                + "If the XPath matches an existing element, that element's value will be updated."
                + " If the XPath does not match an existing element,"
                + " this operation will have no effect.") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "x",
        value = "An XML object") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "xPath",
        value = "An XPath") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "nameSpaces",
        value = "A string value") })
public class SetString extends AbstractNativeFunction {
    
    private static final String OPERATION = "set string in xml";

    @Override
    public BValue[] execute(Context ctx) {
        try {
            // Accessing Parameters.
            BXML xml = (BXML) getArgument(ctx, 0);
            String xPath = getArgument(ctx, 1).stringValue();
            // MapValue<String, String> nameSpaces = getArgument(ctx, 2).getMap();
            String value = getArgument(ctx, 2).stringValue();
            
            // Setting the value to XML
            AXIOMXPath axiomxPath = new AXIOMXPath(xPath);
            // set the namespaces
            /*if (nameSpaces != null && !nameSpaces.isEmpty()) {
                for (MapValue<String, String>.MapEntry<String, String> entry : nameSpaces.getValue()) {
                    axiomxPath.addNamespace(entry.getKey(), entry.getValue());

                }
            }*/
            Object ob = axiomxPath.evaluate(xml.value());
            if (ob instanceof ArrayList) {
                List list = (List) ob;
                for (Object obj : list) {
                    if (obj instanceof OMNode) {
                        OMNode omNode = (OMNode) obj;
                        if (omNode instanceof OMElement) {
                            OMElement ome = (OMElement) omNode;
                            ome.setText(value);
                        } else {
                            OMContainer omContainer = omNode.getParent();
                            omNode.detach();
                            OMAbstractFactory.getOMFactory().createOMText(omContainer, value);
                        }
                    } else if (obj instanceof OMAttribute) {
                        OMAttribute omAttribute = (OMAttribute) obj;
                        omAttribute.setAttributeValue(value);
                    } else {
                        ErrorHandler
                                .logWarn(OPERATION, "xPath '" + xPath + "' does not refers to a attribute or text.");
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
