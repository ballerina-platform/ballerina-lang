/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.siddhi.extension.input.transport.tcp;

import org.wso2.siddhi.tcp.transport.TCPNettyServer;
import org.wso2.siddhi.tcp.transport.callback.StreamListener;
import org.wso2.siddhi.tcp.transport.config.ServerConfig;

public class TCPServer {
    private static TCPServer ourInstance = new TCPServer();

    private static TCPNettyServer tcpNettyServer = null;

    public static TCPServer getInstance() {
        return ourInstance;
    }

    private TCPServer() {
    }

    public synchronized void start() {
        if (tcpNettyServer == null) {
            tcpNettyServer = new TCPNettyServer();
            tcpNettyServer.bootServer(new ServerConfig());
        }
    }

    public synchronized void stop() {
        if (tcpNettyServer != null) {
            if (tcpNettyServer.getNoOfRegisteredStreamListeners() == 0) {
                tcpNettyServer.shutdownGracefully();
            }
            tcpNettyServer = null;
        }
    }


    public synchronized void addStreamListener(StreamListener streamListener) {
        start();
        tcpNettyServer.addStreamListener(streamListener);
    }

    public synchronized void removeStreamListener(String streamId) {
        if (tcpNettyServer != null) {
            tcpNettyServer.removeStreamListener(streamId);
        }
    }

    public void isPaused(boolean paused) {
        tcpNettyServer.isPaused(paused);
    }
}
