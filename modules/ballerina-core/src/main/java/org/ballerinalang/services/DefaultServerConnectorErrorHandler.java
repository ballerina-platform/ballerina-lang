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

package org.ballerinalang.services;

import org.ballerinalang.util.exceptions.BallerinaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.ServerConnectorErrorHandler;

/**
 * {@code DefaultServerConnectorErrorHandler} is the default error handler implementation.
 */
public class DefaultServerConnectorErrorHandler implements ServerConnectorErrorHandler {

    private static final Logger log = LoggerFactory.getLogger(DefaultServerConnectorErrorHandler.class);

    private DefaultServerConnectorErrorHandler(){}

    private static DefaultServerConnectorErrorHandler instance = new DefaultServerConnectorErrorHandler();

    public static DefaultServerConnectorErrorHandler getInstance() {
        return instance;
    }

    @Override
    public void handleError(Exception exception, CarbonMessage cMsg, CarbonCallback callback) throws Exception {
        throw new BallerinaException(exception);
    }

    @Override
    public String getProtocol() {
        return null;
    }


}
