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
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.net.http.HttpDispatcher;
import org.ballerinalang.net.http.HttpResource;
import org.ballerinalang.net.http.HttpService;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;

/**
 * Dispatches incoming HTTP requests for WebSub Subscriber services to the correct resource.
 *
 * @since 0.965.0
 */
class WebSubDispatcher extends HttpDispatcher {

    /**
     * This method finds the matching resource for the incoming request.
     *
     * @param servicesRegistry  the WebSubServicesRegistry including registered WebSub Services
     * @param httpCarbonMessage incoming message.
     * @return matching resource.
     */
     static HttpResource findResource(WebSubServicesRegistry servicesRegistry,
                                            HTTPCarbonMessage httpCarbonMessage) {
        HttpResource resource = null;
        String protocol = (String) httpCarbonMessage.getProperty(HttpConstants.PROTOCOL);
        if (protocol == null) {
            throw new BallerinaConnectorException("protocol not defined in the incoming request");
        }

        try {
            HttpService service = HttpDispatcher.findService(servicesRegistry,
                                                             httpCarbonMessage);
            if (service == null) {
                throw new BallerinaConnectorException("no service found to handle the service request");
                // Finer details of the errors are thrown from the dispatcher itself, ideally we shouldn't get here.
            }

            resource = WebSubResourceDispatcher.findResource(service, httpCarbonMessage,
                                                             servicesRegistry.getTopicHeader(),
                                                             servicesRegistry.getTopicResourceMap());
        } catch (Throwable throwable) {
            handleError(httpCarbonMessage, throwable);
        }
        return resource;
    }

}
