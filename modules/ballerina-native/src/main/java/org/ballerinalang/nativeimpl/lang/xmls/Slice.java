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
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.exceptions.BallerinaException;

/**
 * Slice and return a subsequence of the an XML sequence.
 * 
 * @since 0.88
 */
@BallerinaFunction(
        packageName = "ballerina.lang.xmls",
        functionName = "slice",
        args = {@Argument(name = "x", type = TypeEnum.XML),
            @Argument(name = "startIndex", type = TypeEnum.INT),
            @Argument(name = "endIndex", type = TypeEnum.INT)},
        returnType = {@ReturnType(type = TypeEnum.XML)},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = {@Attribute(name = "value",
        value = "Slice and return a subsequence of the given XML sequence.") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "x",
        value = "An XML object") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "startIndex",
        value = "Start index, inclusive") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "endIndex",
        value = "End index, exclusive") })
@BallerinaAnnotation(annotationName = "Return", attributes = {@Attribute(name = "seq",
        value = "sliced sequence") })
public class Slice extends AbstractNativeFunction {

    private static final String OPERATION = "slice xml";

    @Override
    public BValue[] execute(Context ctx) {
        BValue result = null;
        try {
            // Accessing Parameters.
            BXML value = (BXML) getRefArgument(ctx, 0);
            long startIndex = getIntArgument(ctx, 0);
            long endIndex = getIntArgument(ctx, 1);
            
            if (startIndex < -1) {
                throw new BallerinaException("invalid start index: " + startIndex);
            }
            
            if (endIndex < -1) {
                throw new BallerinaException("invalid end index: " + endIndex);
            }
            
            result = value.slice(startIndex, endIndex);
        } catch (Throwable e) {
            ErrorHandler.handleXMLException(OPERATION, e);
        }
        
        // Setting output value.
        return getBValues(result);
    }
}
