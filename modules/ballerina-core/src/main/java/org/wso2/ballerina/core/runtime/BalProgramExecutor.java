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
import org.wso2.ballerina.core.interpreter.BLangExecutor;
import org.wso2.ballerina.core.interpreter.CallableUnitInfo;
import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.interpreter.RuntimeEnvironment;
import org.wso2.ballerina.core.model.Resource;
import org.wso2.ballerina.core.model.Service;
import org.wso2.ballerina.core.model.SymbolName;
import org.wso2.ballerina.core.model.expressions.ResourceInvocationExpr;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;

/**
 * {@code BalProgramExecutor} is responsible for executing a BallerinaProgram.
 *
 * @since 0.8.0
 */
public class BalProgramExecutor {

    private static final Logger log = LoggerFactory.getLogger(BalProgramExecutor.class);

    public static void execute(CarbonMessage cMsg, CarbonCallback callback, Resource resource, Service service,
                               Context balContext) {
        SymbolName symbolName = service.getSymbolName();
        balContext.setServiceInfo(
                new CallableUnitInfo(symbolName.getName(), symbolName.getPkgPath(), service.getNodeLocation()));

        balContext.setBalCallback(new DefaultBalCallback(callback));

        // Create the interpreter and Execute
        RuntimeEnvironment runtimeEnv = resource.getApplication().getRuntimeEnv();
        BLangExecutor executor = new BLangExecutor(runtimeEnv, balContext);
        new ResourceInvocationExpr(resource).executeMultiReturn(executor);
    }
}
