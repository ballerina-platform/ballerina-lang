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
import org.wso2.ballerina.core.interpreter.ControlStack;
import org.wso2.ballerina.core.interpreter.StackFrame;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.BallerinaFunction;
import org.wso2.ballerina.core.model.Parameter;
import org.wso2.ballerina.core.model.Resource;
import org.wso2.ballerina.core.model.ResourceInvoker;
import org.wso2.ballerina.core.model.VariableDcl;
import org.wso2.ballerina.core.model.types.BType;
import org.wso2.ballerina.core.model.util.BValueUtils;
import org.wso2.ballerina.core.model.values.BInteger;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.runtime.internal.RuntimeUtils;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;

import static org.wso2.ballerina.core.runtime.Constants.SYSTEM_PROP_BAL_ARGS;

/**
 * {@code BalProgramExecutor} is responsible for executing a BallerinaProgram
 *
 * @since 1.0.0
 */
public class BalProgramExecutor {

    private static final Logger log = LoggerFactory.getLogger(BalProgramExecutor.class);

    public static void execute(CarbonMessage cMsg, CarbonCallback callback, Resource resource) {

        // Create the Ballerina Context
        Context balContext = new Context(cMsg);
        balContext.setBalCallback(new DefaultBalCallback(callback));

        // Create the interpreter and Execute
        BLangInterpreter interpreter = new BLangInterpreter(balContext);
        new ResourceInvoker(resource).accept(interpreter);
    }

    /**
     * Execute a program in a Ballerina File
     *
     * @param balFile Ballerina File
     * @return whether the runtime should keep-alive after executing the program in the file
     */
    public static boolean execute(BallerinaFile balFile) {
        BallerinaFunction function =
                (BallerinaFunction) balFile.getFunctions().get(Constants.MAIN_FUNCTION_NAME);

        if (function != null) {
            try {
                BalProgramExecutor.execute(function);
            } finally {
                if (balFile.getServices().size() == 0) {
                    if (log.isDebugEnabled()) {
                        log.debug("Services not found. Hence shutting down after Main function execution.. ");
                    }
                    RuntimeUtils.shutdownRuntime();
                    return false;
                }
            }
        } else if (balFile.getServices().size() == 0) {
            log.warn("Unable to find Main function or any Ballerina Services. Bye..!");
            RuntimeUtils.shutdownRuntime();
            return false;
        }
        return true;
    }

    private static void execute(BallerinaFunction function) {

        // Check whether this is a standard main function with one integer argument
        // This will be changed to string[] args once we have the array support
        Parameter[] parameters = function.getParameters();
        if (parameters.length != 1 || parameters[0].getType() != BType.INT_TYPE) {
            throw new BallerinaException("Main function does not comply with standard main function in ballerina");
        }

        // Execute main function
        // Create control stack and the stack frame
        Context ctx = new Context();
        ControlStack controlStack = ctx.getControlStack();
        int sizeOfValueArray = function.getStackFrameSize();
        BValue[] values = new BValue[sizeOfValueArray];
        int i = 0;

        // Main function only have one input parameter
        // Read from command line arguments
        String balArgs = System.getProperty(SYSTEM_PROP_BAL_ARGS);

        // Only integers allowed at the moment
        if (balArgs != null) {
            int intValue = Integer.parseInt(balArgs);
            values[i++] = new BInteger(intValue);
        } else {
            values[i++] = new BInteger(0);
        }

        // Create default values for all declared local variables
        VariableDcl[] variableDcls = function.getVariableDcls();
        for (VariableDcl variableDcl : variableDcls) {
            values[i] = BValueUtils.getDefaultValue(variableDcl.getType());
            i++;
        }

        BValue[] returnVals = new BValue[function.getReturnTypes().length];
        StackFrame stackFrame = new StackFrame(values, returnVals);
        controlStack.pushFrame(stackFrame);
        BLangInterpreter interpreter = new BLangInterpreter(ctx);
        function.accept(interpreter);
    }

}
