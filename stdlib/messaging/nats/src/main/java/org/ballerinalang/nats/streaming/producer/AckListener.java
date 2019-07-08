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
package org.ballerinalang.nats.streaming.producer;

import io.nats.streaming.AckHandler;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.connector.NonBlockingCallback;
import org.ballerinalang.nats.Utils;

/**
 * {@link AckHandler} implementation to listen to message acknowledgements from NATS streaming server.
 */
public class AckListener implements AckHandler {
    private NonBlockingCallback nonBlockingCallback;

    public AckListener(NonBlockingCallback nonBlockingCallback) {
        this.nonBlockingCallback = nonBlockingCallback;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onAck(String nuid, Exception ex) {
      if (ex == null) {
          nonBlockingCallback.setReturnValues(nuid);
      } else {
          ErrorValue error = Utils.createNatsError(nuid, ex.getMessage());
          nonBlockingCallback.setReturnValues(error);
      }
      nonBlockingCallback.notifySuccess();
    }
}
