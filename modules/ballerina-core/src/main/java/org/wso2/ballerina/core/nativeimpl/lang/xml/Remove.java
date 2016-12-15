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

package org.wso2.ballerina.core.nativeimpl.lang.xml;

import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.jaxen.JaxenException;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.model.types.TypeEnum;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.model.values.MapValue;
import org.wso2.ballerina.core.model.values.XMLValue;
import org.wso2.ballerina.core.nativeimpl.AbstractNativeFunction;
import org.wso2.ballerina.core.nativeimpl.annotations.Argument;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaFunction;

import java.util.ArrayList;
import java.util.List;

/**
 * Remove the element(s) that matches the given xPath.
 */
@BallerinaFunction(
        packageName = "ballerina.lang.xml",
        functionName = "remove",
        args = {@Argument(name = "xml", type = TypeEnum.XML),
                @Argument(name = "xPath", type = TypeEnum.STRING),
                @Argument(name = "nameSpaces", type = TypeEnum.MAP)},
        isPublic = true
)
@Component(
        name = "func.lang.xml_remove",
        immediate = true,
        service = AbstractNativeFunction.class
)
public class Remove extends AbstractNativeFunction {

    private static final Logger log = LoggerFactory.getLogger(Remove.class);

    @Override
    public BValue<?>[] execute(Context ctx) {
        // Accessing Parameters.
        XMLValue xml = (XMLValue) getArgument(ctx, 0).getBValue();
        String xPath = getArgument(ctx, 1).getString();
        MapValue<String, String> nameSpaces = getArgument(ctx, 2).getMap();
        
        // Setting the value to XML
        try {
            AXIOMXPath axiomxPath = new AXIOMXPath(xPath);
            if (nameSpaces != null && !nameSpaces.isEmpty()) {
                for (MapValue<String, String>.MapEntry<String, String> entry : nameSpaces.getValue()) {
                    axiomxPath.addNamespace(entry.getKey(), entry.getValue());

                }
            }
            Object ob = axiomxPath.evaluate(xml.getValue());
            if (ob instanceof ArrayList) {
                List<?> list = (List<?>) ob;

                for (Object obj : list) {
                    if (obj instanceof OMNode) {
                        OMNode omNode = (OMNode) obj;
                        omNode.detach();

                    }
                }
            }
        } catch (JaxenException e) {
            log.error("Cannot evaluate XPath: " + e.getMessage(), e);
        }
        
        return VOID_RETURN;
    }
}
