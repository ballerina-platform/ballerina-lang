/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */

package org.ballerinalang.net.websub;

import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.net.http.BallerinaHTTPConnectorListener;
import org.ballerinalang.net.http.HttpDispatcher;
import org.ballerinalang.net.http.HttpResource;
import org.ballerinalang.net.http.HttpUtil;
import org.ballerinalang.net.http.serviceendpoint.FilterHolder;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;

import java.util.HashSet;

/**
 * HTTP Connection Listener for Ballerina WebSub services.
 */
public class BallerinaWebSubConnectionListener extends BallerinaHTTPConnectorListener {

    private WebSubServicesRegistry webSubServicesRegistry;

    public BallerinaWebSubConnectionListener(WebSubServicesRegistry webSubServicesRegistry,
                                             HashSet<FilterHolder> filterHolders) {
        super(webSubServicesRegistry, filterHolders);
        this.webSubServicesRegistry = webSubServicesRegistry;
    }

    @Override
    public void onMessage(HTTPCarbonMessage httpCarbonMessage) {
        try {
            HttpResource httpResource;
            if (accessed(httpCarbonMessage)) {
                httpResource = (HttpResource) httpCarbonMessage.getProperty(HTTP_RESOURCE);
                extractPropertiesAndStartResourceExecution(httpCarbonMessage, httpResource);
                return;
            }
            httpResource = WebSubDispatcher.findResource(webSubServicesRegistry, httpCarbonMessage);
            if (HttpDispatcher.shouldDiffer(httpResource, hasFilters())) {
                httpCarbonMessage.setProperty(HTTP_RESOURCE, httpResource);
                return;
            }
            extractPropertiesAndStartResourceExecution(httpCarbonMessage, httpResource);
        } catch (BallerinaException ex) {
            HttpUtil.handleFailure(httpCarbonMessage, new BallerinaConnectorException(ex.getMessage(), ex.getCause()));
        }
    }

}
