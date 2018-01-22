/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.net.http.transactions;

import org.ballerinalang.bre.BallerinaTransactionManager;
import org.ballerinalang.bre.Context;
import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.launcher.LauncherUtils;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.util.JsonNode;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.net.http.Constants;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;

/**
 * {@code MicroTransactionCoordinator} represents the coordinator internal service.
 *
 *  @since 0.96.0
 */
public class MicroTransactionCoordinator {

    private Context txnCoordinatorContext = null;
    private ProgramFile txnCoordinatorProgramFile = null;
    private static MicroTransactionCoordinator instance = new MicroTransactionCoordinator();

    public static MicroTransactionCoordinator getInstance() {
        return instance;
    }

    public void initAndRegister(Context context, HTTPCarbonMessage httpRequestMsg) {
        BValue createdContext;
        BallerinaTransactionManager balTransactionManager = context.getBallerinaTransactionManager();
        //init coordinator service
        if (!isProgramFileAvailable()) {
            synchronized (this) {
                if (!isProgramFileAvailable()) {
                    compileAndRunCoordinatorService(context);
                }
            }
        }
        //register initiator
        if (balTransactionManager.getTransportTransactionManager() == null) {
            createdContext = BRunUtil.invokeStateful(getTxnCoordinatorProgramFile(), "transactions.coordinator"
                    , "beginTransaction", getTxnCoordinatorContext())[0];
            balTransactionManager.setTransportTransactionManager(new HttpTransactionManager(createdContext));
        } else {
            createdContext = balTransactionManager.getTransportTransactionManager().getCreatedContext();
        }
        //set transaction headers to transport message
        if (!(createdContext instanceof BJSON)) {
            throw new BallerinaException("Invalid transaction context: Failed to set transaction headers");
        }
        JsonNode jsonNode = ((BJSON) createdContext).value();
        httpRequestMsg.setHeader(Constants.X_XID_HEADER, jsonNode.get(Constants.X_XID_JSON_FIELD).asText());
        httpRequestMsg.setHeader(Constants.X_REGISTER_AT_URL_HEADER,
                jsonNode.get(Constants.X_REGISTER_AT_URL_JSON_FIELD).asText());
    }

    private void compileAndRunCoordinatorService(Context context) {
        CompileResult compileResult = BCompileUtil
                .compileInternalPackage("ballerina/net/http/", "transactions.coordinator");
        BRunUtil.invokePackageInit(compileResult);
        if (compileResult.getProgFile() == null) {
            resetCoordinatorState();
            throw new BallerinaException("Failed to deploy transaction coordinator service");
        }
        this.txnCoordinatorProgramFile = compileResult.getProgFile();
        this.txnCoordinatorContext = compileResult.getContext();
        try {
            LauncherUtils.runInternalServices(getTxnCoordinatorProgramFile(), context);
        } catch (BallerinaConnectorException ex) {
            resetCoordinatorState();
            throw new BallerinaException("Failed to start coordinator service: Address already in use");
        }
    }

    private void resetCoordinatorState() {
        this.txnCoordinatorProgramFile = null;
        this.txnCoordinatorContext = null;
    }

    private boolean isProgramFileAvailable() {
        return getTxnCoordinatorProgramFile() != null;
    }

    public ProgramFile getTxnCoordinatorProgramFile() {
        return this.txnCoordinatorProgramFile;
    }

    public Context getTxnCoordinatorContext() {
        return this.txnCoordinatorContext;
    }
}

