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

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.WriteContext;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.model.types.JSONType;
import org.wso2.ballerina.core.model.types.StringType;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.model.values.JSONValue;
import org.wso2.ballerina.core.nativeimpl.AbstractNativeFunction;
import org.wso2.ballerina.core.nativeimpl.annotations.Argument;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaFunction;

/**
 * Set the value of a element that matches the given jsonpath.
 * If the jsonpath doesn't match any element, then this operation would have no effect.
 */
@BallerinaFunction(
        packageName = "ballerina.lang.json",
        functionName = "set",
        args = {@Argument(name = "json", type = JSONType.class),
                @Argument(name = "jsonPath", type = StringType.class),
                @Argument(name = "value", type = StringType.class)},
        isPublic = true
)
@Component(
        name = "func.lang.json_setString",
        immediate = true,
        service = AbstractNativeFunction.class
)
public class SetString extends AbstractJSONFunction {

    private static final Logger log = LoggerFactory.getLogger(SetString.class);

    @Override
    public BValue[] execute(Context ctx) {
        log.info("SetJSONElement Native Function Invoked.");
        // Accessing Parameters.
        JSONValue json = (JSONValue) getArgument(ctx, 0).getBValue();
        String jsonPath = getArgument(ctx, 1).getString();
        String value = getArgument(ctx, 2).getString();

        // Setting the value to JSON
        WriteContext jsonCtx = JsonPath.parse(json.getValue());
        jsonCtx.set(jsonPath, value);
        return VOID_RETURN;
    }
}
