/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.email.server;

import org.ballerinalang.jvm.BRuntime;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.stdlib.email.util.EmailConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Email connector listener for Ballerina.
 *
 * @since 1.3.0
 */
public class EmailListener {

    private static final Logger log = LoggerFactory.getLogger(EmailListener.class);

    private final BRuntime runtime;

    private Map<String, ObjectValue> registeredServices = new HashMap<>();

    /**
     * Constructor for listener class for email.
     * @param runtime Current Ballerina runtime
     */
    public EmailListener(BRuntime runtime) {
        this.runtime = runtime;
    }

    /**
     * Place an email in Ballerina when received.
     * @param emailEvent Email object to be received
     * @return If successful return true
     */
    public boolean onMessage(EmailEvent emailEvent) {
        Object email = emailEvent.getEmailObject();
        if (runtime != null) {
            for (String serviceKey : registeredServices.keySet()) {
                runtime.invokeMethodSync(registeredServices.get(serviceKey), EmailConstants.ON_MESSAGE, email,
                        true);
            }
        } else {
            log.error("Runtime should not be null.");
        }
        return true;
    }

    /**
     * Place an error in Ballerina when received.
     * @param error Email object to be received
     */
    public void onError(Object error) {
        log.error(((ErrorValue) error).getMessage());
        if (runtime != null) {
            for (String serviceKey : registeredServices.keySet()) {
                runtime.invokeMethodSync(registeredServices.get(serviceKey), EmailConstants.ON_ERROR, error,
                        true);
            }
        } else {
            log.error("Runtime should not be null.");
        }
    }

    protected void addService(ObjectValue service, String serviceName) {
        String serviceKey;
        if (serviceName.equals("")) {
            UUID uuid = UUID.randomUUID();
            serviceKey = uuid.toString();
        } else {
            serviceKey = serviceName;
        }
        if (registeredServices.containsKey(serviceKey)) {
            registeredServices.put(serviceKey, service);
        } else {
            registeredServices.put(serviceKey, service);
        }
    }

}
