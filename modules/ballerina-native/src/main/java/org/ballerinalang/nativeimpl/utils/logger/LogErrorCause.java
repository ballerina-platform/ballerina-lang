/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.nativeimpl.utils.logger;

import org.ballerinalang.bre.Context;
import org.ballerinalang.logging.BLogRecord;
import org.ballerinalang.logging.BLogger;
import org.ballerinalang.logging.util.BLogLevel;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;

/**
 * Native function ballerina.model.utils:logger.
 *
 * @since 0.94
 */
@BallerinaFunction(
        packageName = "ballerina.utils.logger",
        functionName = "errorCause",
        args = {@Argument(name = "msg", type = TypeEnum.STRING), @Argument(name = "err", type = TypeEnum.STRUCT)},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description",
                     attributes = {@Attribute(name = "msg", value = "Logs the specified value at error level.")})
@BallerinaAnnotation(annotationName = "Param",
                     attributes = {
                             @Attribute(name = "msg", value = "The message to be logged."),
                             @Attribute(name = "err", value = "The error to be logged.")})
public class LogErrorCause extends AbstractLogFunction {

    private static final BLogger logger = new BLogger(LogErrorCause.class.getCanonicalName());

    public BValue[] execute(Context ctx) {
        BLogRecord logRecord = createLogRecord(ctx, BLogLevel.ERROR);
        BValue error = getRefArgument(ctx, 0);
        logRecord.setError(error.stringValue());
        logger.log(logRecord);
        return VOID_RETURN;
    }
}
