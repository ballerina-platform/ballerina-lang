/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

/**
 * Set the children of an XML if its a singleton. Error otherwise.
 * Any existing children will be removed.
 * 
 * @since 0.88
 */
@BallerinaFunction(
        packageName = "ballerina.lang.xmls",
        functionName = "setChildren",
        args = {@Argument(name = "x", type = TypeEnum.XML),
                @Argument(name = "children", type = TypeEnum.XML)},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = {@Attribute(name = "value",
        value = "Set the children of this XML. Any existing children will be removed") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "x",
        value = "An XML object") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "children",
        value = "XML Sequence to set as the children") })
public class SetChildren extends AbstractNativeFunction {

    private static final String OPERATION = "set children to xml element";

    @Override
    public BValue[] execute(Context ctx) {
        try {
            BXML xml = (BXML) getRefArgument(ctx, 0);
            BXML children = (BXML) getRefArgument(ctx, 1);
            xml.setChildren(children);
        } catch (Throwable e) {
            ErrorHandler.handleXMLException(OPERATION, e);
        }
        
        // Setting output value.
        return VOID_RETURN;
    }
}
