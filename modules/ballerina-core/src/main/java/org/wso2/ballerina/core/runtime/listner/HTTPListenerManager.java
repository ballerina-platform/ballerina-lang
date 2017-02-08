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

package org.wso2.ballerina.core.runtime.listner;

import org.wso2.carbon.messaging.TransportListener;
import org.wso2.carbon.messaging.TransportListenerManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * {@code HTTPListenerManager} is responsible for managing http transport listener.
 * <p>
 * This is the bridge between transport listener and source of the {@code Service} for deployment tasks.
 */
@Deprecated
public class HTTPListenerManager implements TransportListenerManager {

    private volatile TransportListener transportListener;

    private List<String> pendingInterfaces = new ArrayList();

    private Lock lock = new ReentrantLock();

    private static HTTPListenerManager instance = new HTTPListenerManager();

    private HTTPListenerManager(){}

    public static HTTPListenerManager getInstance() {
        return instance;
    }

    @Override
    public TransportListener getTransportListener() {
        return transportListener;
    }

    @Override
    public void registerTransportListener(TransportListener transportListener) {
        lock.lock();
        try {
            this.transportListener = transportListener;
            // Bind pending interface list
            pendingInterfaces.forEach(interfaceId -> {
                bindInterface(interfaceId);
            });
            pendingInterfaces.clear();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Bind Ballerina engine to a particular http listener interface.
     *
     * @param interfaceId Listener interface id
     */
    public void bindInterface(String interfaceId) {
        lock.lock();
        try {
            if (transportListener == null) {        // If the listener is not ready, add to pending list
                pendingInterfaces.add(interfaceId);
            } else {
                transportListener.bind(interfaceId);
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Unbind Ballerina from a particular http listener interface.
     *
     * @param interfaceId Listener interface id
     */
    public void unbindInterface(String interfaceId) {
        lock.lock();
        try {
            if (transportListener == null) {
                pendingInterfaces.remove(interfaceId);
            } else {
                transportListener.unBind(interfaceId);
            }
        } finally {
            lock.unlock();
        }
    }

}
