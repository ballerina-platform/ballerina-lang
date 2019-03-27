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

package org.ballerinalang.nats.nativeimpl.producer;

import io.nats.streaming.AckHandler;
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.nats.nativeimpl.Constants;
import org.ballerinalang.nats.nativeimpl.Utils;

/**
 * Represents an acknowledgement which will be received for a produced message.
 */
public class Acknowledgment implements AckHandler {
    /**
     * Context of the message which should be acknowledged.
     */
    private Context context;

    /**
     * Callback which should be notified upon acknowledgement.
     */
    private CallableUnitCallback callback;

    Acknowledgment(Context context, CallableUnitCallback callback) {
        this.context = context;
        this.callback = callback;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onAck(String guid, Exception err) {
        if (null != err) {
            context.setReturnValues(Utils.createError(context, Constants.NATS_ERROR_CODE, err.getMessage()));
        } else {
            context.setReturnValues(new BString(guid));
        }
        callback.notifySuccess();
    }
}
