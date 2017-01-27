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

package org.wso2.ballerina.nativeimpl.lang.json;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.model.types.TypeEnum;
import org.wso2.ballerina.core.model.values.BJSON;
import org.wso2.ballerina.core.model.values.BString;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.nativeimpl.annotations.Argument;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaFunction;
import org.wso2.ballerina.core.nativeimpl.annotations.ReturnType;
import org.wso2.ballerina.nativeimpl.lang.utils.ErrorHandler;

/**
 * Native function ballerina.lang.json:toString.
 */
@BallerinaFunction(
        packageName = "ballerina.lang.json",
        functionName = "toString",
        args = {@Argument(name = "json", type = TypeEnum.JSON)},
        returnType = {@ReturnType(type = TypeEnum.STRING)},
        isPublic = true
)
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
