/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.log;

import org.ballerinalang.bre.Context;
import org.ballerinalang.logging.util.BLogLevel;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;

/**
 * Extern function ballerina.log:printTrace.
 *
 * @since 0.89
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "log",
        functionName = "printTrace",
        args = {@Argument(name = "msg", type = TypeKind.ANY)},
        isPublic = true
)
public class LogTrace extends AbstractLogFunction {

    public void execute(Context ctx) {
        logMessage(ctx, BLogLevel.TRACE, (pkg, message) -> {
            getLogger(pkg).trace(message);
        });
    }
}
