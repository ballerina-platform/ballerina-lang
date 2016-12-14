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

package org.wso2.ballerina.core.nativeimpl.lang.json;

import com.google.gson.JsonElement;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;

import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.model.types.TypeEnum;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.model.values.JSONValue;
import org.wso2.ballerina.core.nativeimpl.AbstractNativeFunction;
import org.wso2.ballerina.core.nativeimpl.annotations.Argument;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaFunction;

/**
 * Evaluate jsonpath on a JSON object and returns the matching JSON.
 */
@BallerinaFunction(
        packageName = "ballerina.lang.json",
        functionName = "getJson",
        args = {@Argument(name = "json", type = TypeEnum.JSON),
                @Argument(name = "jsonPath", type = TypeEnum.STRING)},
        returnType = {TypeEnum.STRING},
        isPublic = true
)
@Component(
        name = "func.lang.json_getJson",
        immediate = true,
        service = AbstractNativeFunction.class
)
public class GetJSON extends AbstractJSONFunction {

    private static final Logger log = LoggerFactory.getLogger(GetJSON.class);

    @Override
    public BValue<?>[] execute(Context ctx) {
        // Accessing Parameters.
        JSONValue json = (JSONValue) getArgument(ctx, 0).getBValue();
        String jsonPath = getArgument(ctx, 1).getString();
        
        // Getting the value from JSON
        ReadContext jsonCtx = JsonPath.parse(json.getValue());
        JsonElement element = jsonCtx.read(jsonPath);
        BValue<?> result = null;
        if (element == null) {
            log.error("No matching element found for jsonpath: " + jsonPath);
        } else if (element.isJsonPrimitive()) {
            String errorMsg = "The element matching: " + jsonPath + " is a primitive, not a JSON.";
            log.error(errorMsg);
            throw new RuntimeException(errorMsg);
        } else {
            // if the resulting value is a complex object, return is as a JSONType object
            result = new JSONValue(element);
        }
        
        // Setting output value.
        return getBValues(result);
    }
}
