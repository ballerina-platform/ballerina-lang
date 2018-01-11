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

import org.ballerinalang.bre.Context;
import org.ballerinalang.launcher.LauncherUtils;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.util.JsonNode;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.net.http.Constants;
import org.ballerinalang.util.program.BLangFunctions;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;

/**
 * {@code MicroTransactionManager} represents the request of create-context.
 *
 *  @since 0.95
 */
public class MicroTransactionManager {

    public static void registerWithCoordinator(Context context, HTTPCarbonMessage httpRequestMsg) {
        CompileResult txnManager;
        BValue[] txnContext;
        if (!isCoordinatorExist(context)) {
            txnManager = BCompileUtil
                    .compileInternalPackage("ballerina/net/http/", "transactions.coordinator");
            LauncherUtils.runInternalServices(txnManager.getProgFile(), context);
            context.setProperty(Constants.TXN_MANAGER, txnManager);
        } else {
            txnManager = (CompileResult) context.getProperty(Constants.TXN_MANAGER);
        }

        if (!isTxnContextExist(context)) {
            txnContext = BLangFunctions.invokeNew(txnManager.getProgFile(), "transactions.coordinator", "beginTransaction");
            context.setProperty(Constants.TXN_ID, context.getBallerinaTransactionManager().getTransactionId());
            context.setProperty(Constants.TXN_CONTEXT, txnContext);
        } else {
            txnContext = (BValue[]) context.getProperty(Constants.TXN_CONTEXT);
        }
        //set transaction headers to transport message
        if (txnContext[0] instanceof BJSON) {
            JsonNode jsonNode = ((BJSON) txnContext[0]).value();
            httpRequestMsg.setHeader(Constants.X_XID_HEADER, jsonNode.get(Constants.X_XID_JSON_FIELD).asText());
            httpRequestMsg.setHeader(Constants.X_REGISTER_AT_URL_HEADER, jsonNode.get(Constants.X_REGISTER_AT_URL_JSON_FIELD).asText());
        }
    }


    private static boolean isCoordinatorExist(Context context) {
        return context.getProperty(Constants.TXN_MANAGER) != null;
    }

    private static boolean isTxnContextExist(Context context) {
        Object txnId = context.getProperty(Constants.TXN_ID);
        return txnId != null && context.getProperty(Constants.TXN_CONTEXT) != null
                && txnId.equals(context.getBallerinaTransactionManager().getTransactionId());
    }
}
