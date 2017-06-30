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

package org.ballerinalang.runtime.threadpool;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BLangVM;
import org.ballerinalang.bre.bvm.BLangVMErrors;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.natives.connectors.BalConnectorCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;

/**
 * Worker Thread which is responsible for response processing.
 */
public class ResponseWorkerThread extends WorkerThread {

    private static final Logger logger = LoggerFactory.getLogger(ResponseWorkerThread.class);

    public ResponseWorkerThread(CarbonMessage cMsg, CarbonCallback callback) {
        super(cMsg, callback);
    }

    public void run() {
//        // TODO : Fix this properly. Handle workers.
//        // Connector callback's done method is called from different locations, i.e: MessageProcessor, from Netty etc.
//        // Because of this we have to start new thread from the callback, if non-blocking is enabled.
        BalConnectorCallback connectorCallback = (BalConnectorCallback) this.callback;
        Context context = connectorCallback.getContext();
        BLangVM bLangVM = new BLangVM(context.getProgramFile());
        try {
            connectorCallback.getNativeAction().validate(connectorCallback);
        } catch (Exception e) {
            logger.error("non-blocking action invocation validation failed. ", e);
            BStruct err = BLangVMErrors.createError(context, context.getStartIP() - 1, e.getMessage());
            context.setError(err);
        }
        // TODO : Fix error handling
        try {
            bLangVM.run(context);
        } catch (Exception e) {
            logger.error("unhandled exception ", e);
        }
    }
}
