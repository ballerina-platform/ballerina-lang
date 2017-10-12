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
package org.ballerinalang.nativeimpl.lang.system;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaConstant;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Native function ballerina.model.system:log.
 */
@BallerinaFunction(
        packageName = "ballerina.lang.system",
        functionName = "log",
        args = {@Argument(name = "logLevel", type = TypeKind.INT),
                @Argument(name = "value", type = TypeKind.ANY)},
        isPublic = true,
        consts = {
                @BallerinaConstant(identifier = "LOG_LEVEL_TRACE", type = TypeKind.INT, value = "1",
                        argumentRefs = {"logLevel"}),
                @BallerinaConstant(identifier = "LOG_LEVEL_DEBUG", type = TypeKind.INT, value = "2",
                        argumentRefs = {"logLevel"}),
                @BallerinaConstant(identifier = "LOG_LEVEL_INFO", type = TypeKind.INT, value = "3",
                        argumentRefs = {"logLevel"}),
                @BallerinaConstant(identifier = "LOG_LEVEL_WARN", type = TypeKind.INT, value = "4",
                        argumentRefs = {"logLevel"}),
                @BallerinaConstant(identifier = "LOG_LEVEL_ERROR", type = TypeKind.INT, value = "5",
                        argumentRefs = {"logLevel"})
        }
)
public class LogAny extends AbstractNativeFunction {

    private static final Logger logger = LoggerFactory.getLogger(LogAny.class);

    public BValue[] execute(Context ctx) {
        //here we cast second parameter to int as anyway it only has few log levels
        LogUtil.log(logger, getIntArgument(ctx, 0), getRefArgument(ctx, 0).stringValue());
        return VOID_RETURN;
    }
}
