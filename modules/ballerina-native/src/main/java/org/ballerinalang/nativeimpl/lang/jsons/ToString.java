/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.nativeimpl.lang.jsons;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.nativeimpl.lang.utils.ErrorHandler;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Native function ballerina.model.json:toString.
 */
@BallerinaFunction(
        packageName = "ballerina.lang.jsons",
        functionName = "toString",
        args = {@Argument(name = "j", type = TypeEnum.JSON)},
        returnType = {@ReturnType(type = TypeEnum.STRING)},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = {@Attribute(name = "value",
        value = "Converts a JSON object to a string representation") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "j",
        value = "A JSON object") })
@BallerinaAnnotation(annotationName = "Return", attributes = {@Attribute(name = "string",
        value = "String value of the converted JSON") })
public class ToString extends AbstractJSONFunction {

    private static final Logger log = LoggerFactory.getLogger(ToString.class);

    @Override
    public BValue[] execute(Context ctx) {
        String jsonStr = null;
        try {
            // Accessing Parameters.
            BJSON json = (BJSON) getArgument(ctx, 0);

            jsonStr = json.stringValue();
            if (log.isDebugEnabled()) {
                log.debug("Output JSON: " + jsonStr);
            }
        } catch (Throwable e) {
            ErrorHandler.handleJsonException("convert json to string", e);
        }

        return getBValues(new BString(jsonStr));
    }
}
