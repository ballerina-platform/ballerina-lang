/*
 * Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.stdlib.socket.tcp;

import org.ballerinalang.model.values.BError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Socket callback for service resource execution.
 *
 * @since 0.995.0
 */
public class TCPSocketReadCallback extends TCPSocketCallback {

    private static final Logger log = LoggerFactory.getLogger(TCPSocketReadCallback.class);

    private SocketService socketService;

    TCPSocketReadCallback(SocketService socketService) {
        super(socketService);
        this.socketService = socketService;
    }

    @Override
    public void notifySuccess() {
        invokePendingReadReady();
        log.debug("Socket resource dispatch succeed.");
    }

    @Override
    public void notifyFailure(BError error) {
        invokePendingReadReady();
        super.notifyFailure(error);
    }

    private void invokePendingReadReady() {
        // Exiting from the resource, so no more further caller->read statements. Release the resource lock.
        socketService.getResourceLock().release();
        SelectorManager.getInstance().invokePendingReadReadyResources(socketService.getSocketChannel().hashCode());
    }
}
