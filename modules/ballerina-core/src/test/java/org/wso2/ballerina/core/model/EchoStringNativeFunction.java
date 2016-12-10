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


package org.wso2.ballerina.core.model;

import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.model.types.StringType;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.model.values.StringValue;
import org.wso2.ballerina.core.nativeimpl.AbstractNativeFunction;
import org.wso2.ballerina.core.nativeimpl.annotations.Argument;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaFunction;

/**
 * Test Class for Testing Ballerina Service.
 */
@BallerinaFunction(
        packageName = "ballerina.lang.echo",
        functionName = "echoString",
        args = {@Argument(name = "echoString", type = StringType.class)},
        returnType = {StringType.class},
        isPublic = true
)
@Component(
        name = "func.lang.echo_echoString",
        immediate = true,
        service = AbstractNativeFunction.class
)
public class EchoStringNativeFunction extends AbstractNativeFunction {

    private static final Logger log = LoggerFactory.getLogger(EchoStringNativeFunction.class);

    public BValue[] execute(Context ctx) {
        log.info("EchoString Native Function Invoked.");
        // Accessing First Parameter Value.
        String param = getArgument(ctx, 0).getString();
        // Return values.
        return getBValues(new StringValue(param));
    }

}
