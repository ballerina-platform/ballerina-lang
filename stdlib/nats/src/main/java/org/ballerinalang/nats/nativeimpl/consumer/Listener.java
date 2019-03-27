/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.nats.nativeimpl.consumer;

import io.nats.streaming.Message;
import io.nats.streaming.MessageHandler;
import org.ballerinalang.bre.bvm.BLangVMErrors;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.Executor;
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.nats.nativeimpl.Constants;
import org.ballerinalang.services.ErrorHandlerUtils;
import org.ballerinalang.util.codegen.ProgramFile;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;

/**
 * Handles incoming message for a given subscription.
 */
public class Listener implements MessageHandler {
    /**
     * Resource which the message should be dispatched.
     */
    private Resource resource;

    public Listener(Resource resource) {
        this.resource = resource;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onMessage(Message message) {
        ProgramFile programFile = resource.getResourceInfo().getPackageInfo().getProgramFile();
        BMap<String, BValue> msgObj = BLangConnectorSPIUtil.createBStruct(programFile, Constants.NATS_PACKAGE,
                Constants.NATS_MESSAGE_OBJ_NAME);
        msgObj.addNativeData(Constants.NATS_MSG, message);
        msgObj.put(Constants.MSG_CONTENT_NAME, new BString(new String(message.getData(), StandardCharsets.UTF_8)));
        Executor.submit(resource, new ResponseCallback(), new HashMap<>(), null, msgObj);
    }

    /**
     * Represents the callback which will be triggered upon submitting to resource.
     */
    private static class ResponseCallback implements CallableUnitCallback {
        /**
         * {@inheritDoc}
         */
        @Override
        public void notifySuccess() {
            // Nothing to do on success
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void notifyFailure(BError error) {
            ErrorHandlerUtils.printError("error: " + BLangVMErrors.getPrintableStackTrace(error));
        }
    }
}


