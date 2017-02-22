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

package org.ballerinalang.nativeimpl.lang.jsons;

import com.fasterxml.jackson.databind.JsonNode;
import com.jayway.jsonpath.InvalidPathException;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.JsonPathException;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.ReadContext;
import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.nativeimpl.lang.utils.ErrorHandler;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.exceptions.BallerinaException;

/**
 * Evaluate jsonpath on a JSON object and returns the integer value.
 */
@BallerinaFunction(
        packageName = "ballerina.lang.jsons",
        functionName = "getFloat",
        args = {@Argument(name = "j", type = TypeEnum.JSON),
                @Argument(name = "jsonPath", type = TypeEnum.STRING)},
        returnType = {@ReturnType(type = TypeEnum.FLOAT)},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = {@Attribute(name = "value",
        value = "Evaluates the JSONPath on a JSON object and returns the integer value.") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "j",
        value = "A JSON object") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "jsonPath",
        value = "The path of the JSON element") })
@BallerinaAnnotation(annotationName = "Return", attributes = {@Attribute(name = "float",
        value = "The float element on the specified path") })
public class GetFloat extends AbstractJSONFunction {
    
    private static final String OPERATION = "get float from json";

    @Override
    public BValue[] execute(Context ctx) {
        String jsonPath = null;
        BValue result = null;
        
        try {
            // Accessing Parameters.
            BJSON json = (BJSON) getArgument(ctx, 0);
            jsonPath = getArgument(ctx, 1).stringValue();

            // Getting the value from JSON
            ReadContext jsonCtx = JsonPath.parse(json.value());
            JsonNode element = jsonCtx.read(jsonPath);
            if (element == null) {
                throw new BallerinaException("No matching element found for jsonpath: " + jsonPath);
            } else if (element.isValueNode()) {
                // if the resulting value is a primitive, return the respective primitive value object
                if (element.isNumber()) {
                    Number number = element.numberValue();
                    if (number instanceof Float || number instanceof Double) {
                        result = new BFloat(number.floatValue());
                    } else {
                        throw new BallerinaException("The element matching path: " + jsonPath + " is not a Float.");
                    }
                } else {
                    throw new BallerinaException("The element matching path: " + jsonPath + " is not a Float.");
                }
            } else {
                throw new BallerinaException("The element matching path: " + jsonPath + " is a JSON, not a Float.");
            }
        } catch (PathNotFoundException e) {
            ErrorHandler.handleNonExistingJsonpPath(OPERATION, jsonPath, e);
        } catch (InvalidPathException e) {
            ErrorHandler.handleInvalidJsonPath(OPERATION, e);
        } catch (JsonPathException e) {
            ErrorHandler.handleJsonPathException(OPERATION, e);
        } catch (Throwable e) {
            ErrorHandler.handleJsonPathException(OPERATION, e);
        }
        
        // Setting output value.
        return getBValues(result);
    }
}
