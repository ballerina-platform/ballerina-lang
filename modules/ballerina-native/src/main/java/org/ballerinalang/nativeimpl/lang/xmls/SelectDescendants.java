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
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BXML;
import org.ballerinalang.nativeimpl.lang.utils.ErrorHandler;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

/**
 * Searches in children recursively for elements matching the name and returns a sequence containing them all.
 * Does not search within a matched result.
 * 
 * @since 0.92
 */
@BallerinaFunction(
        packageName = "ballerina.lang.xmls",
        functionName = "selectDescendants",
        args = {@Argument(name = "x", type = TypeEnum.XML),
                @Argument(name = "qname", type = TypeEnum.STRING)},
        returnType = {@ReturnType(type = TypeEnum.XML)},
        isPublic = true
)
public class SelectDescendants extends AbstractNativeFunction {

    private static final String OPERATION = "select descendants from xml";

    @Override
    public BValue[] execute(Context ctx) {
        BValue result = null;
        try {
            // Accessing Parameters.
            BXML<?> value = (BXML<?>) getRefArgument(ctx, 0);
            BString qname = new BString(getStringArgument(ctx, 0));
            result = value.descendants(qname);
        } catch (Throwable e) {
            ErrorHandler.handleXMLException(OPERATION, e);
        }
        
        // Setting output value.
        return getBValues(result);
    }
}
