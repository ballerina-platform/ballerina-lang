/**
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 **/

package org.wso2.ballerina.nativeimpl.lang.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.jayway.jsonpath.InvalidPathException;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.JsonPathException;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.WriteContext;
import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.model.types.TypeEnum;
import org.wso2.ballerina.core.model.values.BJSON;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.nativeimpl.annotations.Argument;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaFunction;
import org.wso2.ballerina.nativeimpl.lang.utils.ErrorHandler;

/**
 * Insert a named element to a JSON Object. This method will add a new JSON element with
 * the given name and value, to the location identified by the given jsonpath. If an
 * element with the same 'name' already exists, then it will update value of the existing
 * element.
 */
@BallerinaFunction(
        packageName = "ballerina.lang.json",
        functionName = "add",
        args = {@Argument(name = "json", type = TypeEnum.JSON),
                @Argument(name = "jsonPath", type = TypeEnum.STRING),
                @Argument(name = "key", type = TypeEnum.STRING),
                @Argument(name = "value", type = TypeEnum.JSON)},
        isPublic = true
)
public class AddJSONToObject extends AbstractJSONFunction {

    private static final String OPERATION = "add element to json object";

    @Override
    public BValue[] execute(Context ctx) {
        String jsonPath = null;
        try {
            // Accessing Parameters.
            BJSON json = (BJSON) getArgument(ctx, 0);
            jsonPath = getArgument(ctx, 1).stringValue();
            String key = getArgument(ctx, 2).stringValue();
            JsonNode value = ((BJSON) getArgument(ctx, 3)).value();

            // Adding the value to JSON Object
            WriteContext jsonCtx = JsonPath.parse(json.value());
            jsonCtx.put(jsonPath, key, value);
        } catch (PathNotFoundException e) {
            ErrorHandler.handleNonExistingJsonpPath(OPERATION, jsonPath, e);
        } catch (InvalidPathException e) {
            ErrorHandler.handleInvalidJsonPath(OPERATION, e);
        } catch (JsonPathException e) {
            ErrorHandler.handleJsonPathException(OPERATION, e);
        } catch (Throwable e) {
            ErrorHandler.handleJsonPathException(OPERATION, e);
        }

        return getBValues();
    }
}
