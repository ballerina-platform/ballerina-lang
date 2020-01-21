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

package org.ballerinalang.messaging.kafka.impl;

import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.connector.CallableUnitCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Semaphore;

/**
 * {@code KafkaPollCycleFutureListener} listener provides ability control poll cycle flow by notifications
 * received from Ballerina side.
 */
public class KafkaPollCycleFutureListener implements CallableUnitCallback {

    private static final Logger logger = LoggerFactory.getLogger(KafkaPollCycleFutureListener.class);

    // Introduced this semaphore to control polling cycle from Ballerina Engine.
    // Semaphore provides a source of communication once the BVM has completed the processing for one polling cycle.
    // This listener get notified and Semaphore is released, so that Kafka connector will move to Next polling cycle.
    private Semaphore sem;
    private String serviceId;

    /**
     * Future will get notified from the Ballerina engine when the Resource invocation
     * is over or when an error occurred.
     *
     * @param sem semaphore to handle futures
     * @param serviceId Service ID of the service handling the resource
     */
    public KafkaPollCycleFutureListener(Semaphore sem, String serviceId) {
        this.sem = sem;
        this.serviceId = serviceId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifySuccess() {
        sem.release();
        if (logger.isDebugEnabled()) {
            logger.debug("Ballerina engine has completed resource invocation successfully for service " + serviceId +
                         ". Semaphore is released to continue next polling cycle.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyFailure(ErrorValue error) {
        sem.release();
        logger.error("Ballerina engine has completed resource invocation with exception for service " + serviceId +
                     ". Semaphore is released to continue next polling cycle.", error.stringValue());
    }

}
