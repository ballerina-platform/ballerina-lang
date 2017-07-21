/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * Native function ballerina.model.json:Parse.
 * Parses and gets a JSON from a string.
 * 
 * @since 0.90
 */
@BallerinaFunction(
        packageName = "ballerina.lang.jsons",
        functionName = "parse",
        args = {@Argument(name = "jsonStr", type = TypeEnum.STRING)},
        returnType = {@ReturnType(type = TypeEnum.JSON)},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = {@Attribute(name = "value",
        value = "Parses and gets a JSON from a string") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "jsonStr",
        value = "String representation of JSON") })
@BallerinaAnnotation(annotationName = "Return", attributes = {@Attribute(name = "json",
        value = "Parsed JSON") })
public class Parse extends AbstractJSONFunction {

    private static final Logger log = LoggerFactory.getLogger(Parse.class);

    @Override
    public BValue[] execute(Context ctx) {
        BJSON json = null;
        try {
            // Accessing Parameters.
            String jsonStr = getStringArgument(ctx, 0);

            json = new BJSON(jsonStr);
            if (log.isDebugEnabled()) {
                log.debug("Output JSON: " + json);
            }
        } catch (Throwable e) {
            ErrorHandler.handleJsonException("parse json", e);
        }

        return getBValues(json);
    }
}
