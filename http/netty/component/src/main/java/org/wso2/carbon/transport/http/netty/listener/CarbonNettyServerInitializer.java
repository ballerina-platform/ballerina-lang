/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.transport.http.netty.listener;

import io.netty.channel.socket.SocketChannel;

import java.util.Map;

/**
 * An implementation of this class is used for registering additional handlers to the channel pipeline.
 */
public interface CarbonNettyServerInitializer {

    /**
     * The setup method is called once to setup the  CarbonNettyServerInitializer.
     *
     * @param parameters Optional transport parameters. Key - parameter name, Value - parameter value
     */
    void setup(Map<String, String> parameters);

    /**
     * Initialize the Netty SocketChannel. This is called per connection.
     *
     * @param socketChannel Netty SocketChannel
     */
    void initChannel(SocketChannel socketChannel);
}
