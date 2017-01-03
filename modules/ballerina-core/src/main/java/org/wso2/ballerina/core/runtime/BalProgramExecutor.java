/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.ballerina.core.runtime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerina.core.exception.BallerinaException;
import org.wso2.ballerina.core.interpreter.BLangInterpreter;
import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.interpreter.NodeInfo;
import org.wso2.ballerina.core.interpreter.StackFrameType;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.BallerinaFunction;
import org.wso2.ballerina.core.model.Resource;
import org.wso2.ballerina.core.model.Service;
import org.wso2.ballerina.core.model.SymbolName;
import org.wso2.ballerina.core.model.invokers.MainInvoker;
import org.wso2.ballerina.core.model.invokers.ResourceInvoker;
import org.wso2.ballerina.core.runtime.errors.handler.ErrorHandlerUtils;
import org.wso2.ballerina.core.runtime.internal.RuntimeUtils;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;

/**
 * {@code BalProgramExecutor} is responsible for executing a BallerinaProgram
 *
 * @since 1.0.0
 */
public class BalProgramExecutor {

    private static final Logger log = LoggerFactory.getLogger(BalProgramExecutor.class);

    public static void execute(CarbonMessage cMsg, CarbonCallback callback, Resource resource, Service service,
            Context balContext) {
        try {
            SymbolName symbolName = service.getSymbolName();
            balContext.setServiceInfo(new NodeInfo(symbolName.getName(), StackFrameType.SERVICE, 
                    symbolName.getPkgName(), service.getServiceLocation()));
            balContext.setBalCallback(new DefaultBalCallback(callback));

            // Create the interpreter and Execute
            BLangInterpreter interpreter = new BLangInterpreter(balContext);
            new ResourceInvoker(resource).accept(interpreter);
        } catch (Throwable e) {
            throw new BallerinaException(e.getMessage(), balContext);
        }
    }

    /**
     * Execute a program in a Ballerina File
     *
     * @param balFile Ballerina File
     * @return whether the runtime should keep-alive after executing the program in the file
     */
    public static boolean execute(BallerinaFile balFile) {
        BallerinaFunction function = (BallerinaFunction) balFile.getFunctions().get(Constants.MAIN_FUNCTION_NAME);
        if (function != null) {
            Context ctx = new Context();
            try {
                BLangInterpreter interpreter = new BLangInterpreter(ctx);
                new MainInvoker(function).accept(interpreter);
            } catch (BallerinaException e) {
                String stackTrace = ErrorHandlerUtils.getMainFunctionStackTrace(ctx, balFile.getPackageName());
                log.error("Error while executing ballerina program. " + e.getMessage() + "\n" + stackTrace);
            } finally {
                RuntimeUtils.shutdownRuntime();
            }
            return false;
        } else if (balFile.getServices().size() == 0) {
            log.error("Unable to find Main function or any Ballerina Services.");
            RuntimeUtils.shutdownRuntime();
            return false;
        }
        return true;
    }

}
