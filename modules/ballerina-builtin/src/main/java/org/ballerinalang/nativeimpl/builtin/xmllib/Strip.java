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

package org.ballerinalang.nativeimpl.builtin.xmllib;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BXML;
import org.ballerinalang.nativeimpl.lang.utils.ErrorHandler;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

/**
 * Strips any text items from an XML sequence that are all whitespace.
 * 
 * @since 0.88
 */
@BallerinaFunction(
        packageName = "ballerina.builtin",
        functionName = "xml.strip",
        returnType = {@ReturnType(type = TypeKind.XML)},
        isPublic = true
)
public class Strip extends AbstractNativeFunction {

    private static final String OPERATION = "strip xml";

    @Override
    public BValue[] execute(Context ctx) {
        BValue result = null;
        try {
            // Accessing Parameters.
            BXML value = (BXML) getRefArgument(ctx, 0);
            result = value.strip();
        } catch (Throwable e) {
            ErrorHandler.handleXMLException(OPERATION, e);
        }
        
        // Setting output value.
        return getBValues(result);
    }
}
