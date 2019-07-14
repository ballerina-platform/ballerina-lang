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

import org.ballerinalang.jvm.util.exceptions.BallerinaConnectorException;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.net.http.HttpDispatcher;
import org.ballerinalang.net.http.HttpResource;
import org.ballerinalang.net.http.HttpService;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import java.net.URI;

import static org.ballerinalang.net.http.HttpConstants.TO;
import static org.ballerinalang.net.http.HttpDispatcher.getValidatedURI;

/**
 * Dispatches incoming HTTP requests for WebSub Subscriber services to the correct resource.
 *
 * @since 0.965.0
 */
class WebSubDispatcher {

    /**
     * This method finds the matching resource for the incoming request.
     *
     * @param servicesRegistry  the WebSubServicesRegistry including registered WebSub Services
     * @param inboundMessage incoming message.
     * @return matching resource.
     */
     static HttpResource findResource(WebSubServicesRegistry servicesRegistry, HttpCarbonMessage inboundMessage) {
        String protocol = (String) inboundMessage.getProperty(HttpConstants.PROTOCOL);
        if (protocol == null) {
            throw new BallerinaConnectorException("protocol not defined in the incoming request");
        }
        try {
            HttpService service = HttpDispatcher.findService(servicesRegistry, inboundMessage);
            if (service == null) {
                throw new BallerinaConnectorException("no service found to handle the service request");
                // Finer details of the errors are thrown from the dispatcher itself, ideally we shouldn't get here.
            }

            // TODO: 8/9/18 remove dependency on HTTP dispatcher - this check may not be needed here
            URI validatedUri = getValidatedURI((String) inboundMessage.getProperty(TO));
            if (!validatedUri.getPath().equals(service.getBasePath())) {
                throw new BallerinaConnectorException("no matching service found for path : "
                                                              + validatedUri.getRawPath());
            }
            return WebSubResourceDispatcher.findResource(service, inboundMessage, servicesRegistry);
        } catch (Exception e) {
            throw new BallerinaConnectorException(e.getMessage());
        }
    }

    private WebSubDispatcher() {
    }
}
