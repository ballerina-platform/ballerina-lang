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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;

/**
 * This class contains the utils methods that related to test usage.
 */
class TestSocketUtils {

    private static final Logger log = LoggerFactory.getLogger(TestSocketUtils.class);

    /**
     * Attempts to establish a connection with the test server.
     *
     * @param hostName        hostname of the server.
     * @param numberOfRetries number of retry attempts.
     * @return true if the connection is established successfully.
     */
    static boolean isConnected(String hostName, int port, int numberOfRetries) {
        Socket temporarySocketConnection = null;
        boolean isConnected = false;
        final int retryInterval = 1000;
        final int initialRetryCount = 0;
        for (int retryCount = initialRetryCount; retryCount < numberOfRetries && !isConnected; retryCount++) {
            try {
                //Attempts to establish a connection with the server
                temporarySocketConnection = new Socket(hostName, port);
                isConnected = true;
            } catch (IOException e) {
                log.error("Error occurred while establishing a connection with test server", e);
                sleep(retryInterval);
            } finally {
                if (null != temporarySocketConnection) {
                    //We close the connection once completed.
                    close(temporarySocketConnection);
                }
            }
        }
        return isConnected;
    }

    /**
     * Closes a provided socket connection.
     *
     * @param socket socket which should be closed.
     */
    private static void close(Socket socket) {
        try {
            socket.close();
        } catch (IOException e) {
            log.error("Error occurred while closing the Socket connection", e);
        }
    }

    /**
     * Will enforce to sleep the thread for the provided time.
     *
     * @param retryInterval the time in milliseconds the thread should sleep
     */
    private static void sleep(int retryInterval) {
        try {
            Thread.sleep(retryInterval);
        } catch (InterruptedException ignore) {
            Thread.currentThread().interrupt();
        }
    }
}
