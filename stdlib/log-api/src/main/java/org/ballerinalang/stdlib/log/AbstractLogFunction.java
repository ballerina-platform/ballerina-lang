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
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.logging.BLogManager;
import org.ballerinalang.logging.util.BLogLevel;
import org.ballerinalang.model.values.BClosure;
import org.ballerinalang.model.values.BFunctionPointer;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.observability.ObservabilityUtils;
import org.ballerinalang.util.program.BLangFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.logging.LogManager;

/**
 * Base class for the other log functions, containing a getter to retrieve the correct logger, given a package name.
 *
 * @since 0.95.0
 */
public abstract class AbstractLogFunction extends BlockingNativeCallableUnit {

    protected static final BLogManager LOG_MANAGER = (BLogManager) LogManager.getLogManager();

    private static final Logger ballerinaRootLogger = LoggerFactory.getLogger(BLogManager.BALLERINA_ROOT_LOGGER_NAME);

    protected Logger getLogger(String pkg) {
        if (".".equals(pkg) || pkg == null) {
            return ballerinaRootLogger;
        } else {
            // TODO: Refactor this later
            return LoggerFactory.getLogger(ballerinaRootLogger.getName() + "." + pkg);
        }
    }

    /**
     * Execute logging provided message.
     *
     * @param ctx      runtime context
     * @param logLevel log level
     * @param consumer log message consumer
     */
    protected void logMessage(Context ctx, BLogLevel logLevel, BiConsumer<String, String> consumer) {
        // Create a new log message supplier
        Supplier<String> logMessage = new Supplier<String>() {
            private String msg = null;

            @Override
            public String get() {
                // We should invoke the lambda only once, thus caching return value
                if (msg == null) {
                    BValue arg = ctx.getRefArgument(0);
                    // If it is a lambda; invoke it to get the log message
                    arg = (arg instanceof BFunctionPointer) ? invokeFunction((BFunctionPointer) arg)[0] : arg;
                    msg = arg.stringValue();
                }
                return msg;
            }
        };
        // Logging message
        String pkg = getPackagePath(ctx);
        boolean logEnabled = LOG_MANAGER.getPackageLogLevel(pkg).value() <= logLevel.value();
        if (logEnabled) {
            consumer.accept(pkg, logMessage.get());
        }
        ObservabilityUtils.logMessageToActiveSpan(ctx, logLevel.name(), logMessage, logLevel == BLogLevel.ERROR);
        ctx.setReturnValues();
    }

    /**
     * Invokes a callable function pointer.
     *
     * @param functionPointer function pointer
     * @return return values
     */
    protected BValue[] invokeFunction(BFunctionPointer functionPointer) {
        List<BValue> lambdaFunctionArgs = new ArrayList<>();
        for (BClosure closure : functionPointer.getClosureVars()) {
            lambdaFunctionArgs.add(closure.value());
        }
        return BLangFunctions.invokeCallable(functionPointer.value(), lambdaFunctionArgs.toArray(new BValue[0]));
    }

    //TODO merge below and above methods(below one new bvm)
    protected String getPackagePath(Context ctx) {
        return ctx.getParentWorkerExecutionContext().callableUnitInfo.getPackageInfo().getPkgPath();
    }
}
