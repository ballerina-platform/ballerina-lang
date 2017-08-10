/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.transport.http.netty.contractimpl;

import org.wso2.carbon.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.carbon.transport.http.netty.contract.HTTPConnectorListener;
import org.wso2.carbon.transport.http.netty.contract.ServerConnector;
import org.wso2.carbon.transport.http.netty.common.Constants;
import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Main Test
 */
public class Main {
    public static void main(String[] args) {

        Executor executor = Executors.newSingleThreadExecutor();

        HTTPConnectorFactoryImpl httpConnectorFactory = new HTTPConnectorFactoryImpl();

        Map<String, String> props = new HashMap<>();
        props.put(Constants.HOST, "localhost");
        props.put(Constants.HTTP_PORT, "9009");

        ServerConnector serverConnector = httpConnectorFactory.getServerConnector(null, null);
        ServerConnectorFuture serverConnectorFuture = serverConnector.start();
        serverConnectorFuture.setHTTPConnectorListener(new HTTPConnectorListener() {
            @Override
            public void onMessage(HTTPCarbonMessage httpMessage) {
                executor.execute(new MainRunner(httpMessage, httpConnectorFactory));
            }

            @Override
            public void onError(Throwable throwable) {
                // Do something
            }
        });

        try {
            Thread.sleep(10000000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
