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

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.net.http.BallerinaHTTPConnectorListener;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.net.http.HttpDispatcher;
import org.ballerinalang.net.http.HttpResource;
import org.ballerinalang.net.http.HttpUtil;
import org.ballerinalang.net.http.serviceendpoint.FilterHolder;
import org.ballerinalang.net.uri.URIUtil;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
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
                if (httpCarbonMessage.getProperty(HTTP_RESOURCE) instanceof String
                    && httpCarbonMessage.getProperty(HTTP_RESOURCE).equals(WebSubSubscriberConstants.ANNOTATED_TOPIC)) {
                    autoRespondToIntentVerification(httpCarbonMessage);
                    return;
                }
                httpResource = (HttpResource) httpCarbonMessage.getProperty(HTTP_RESOURCE);
                extractPropertiesAndStartResourceExecution(httpCarbonMessage, httpResource);
                return;
            }
            httpResource = WebSubDispatcher.findResource(webSubServicesRegistry, httpCarbonMessage);
            if (httpCarbonMessage.getProperty(HTTP_RESOURCE) == null
                    && HttpDispatcher.shouldDiffer(httpResource, hasFilters())) {
                httpCarbonMessage.setProperty(HTTP_RESOURCE, httpResource);
                return;
            }
            extractPropertiesAndStartResourceExecution(httpCarbonMessage, httpResource);
        } catch (BallerinaException ex) {
            HttpUtil.handleFailure(httpCarbonMessage, new BallerinaConnectorException(ex.getMessage(), ex.getCause()));
        }
    }

    /**
     * Method to automatically respond to intent verification requests for subscriptions/unsubscriptions if a resource
     * named {@link WebSubSubscriberConstants#RESOURCE_NAME_VERIFY_INTENT} is not specified.
     *
     * @param httpCarbonMessage the message/request received
     */
    private void autoRespondToIntentVerification(HTTPCarbonMessage httpCarbonMessage) {
        String annotatedTopic = httpCarbonMessage.getProperty(WebSubSubscriberConstants.ANNOTATED_TOPIC).toString();
        PrintStream console = System.out;
        if (httpCarbonMessage.getProperty(HttpConstants.QUERY_STR) != null) {
            String queryString = (String) httpCarbonMessage.getProperty(HttpConstants.QUERY_STR);
            BMap<String, BString> params = new BMap<>();
            try {
                HTTPCarbonMessage response = HttpUtil.createHttpCarbonMessage(false);
                response.waitAndReleaseAllEntities();
                URIUtil.populateQueryParamMap(queryString, params);
                String mode = params.get(WebSubSubscriberConstants.PARAM_HUB_MODE).stringValue();
                if ((WebSubSubscriberConstants.SUBSCRIBE.equals(mode)
                             || WebSubSubscriberConstants.UNSUBSCRIBE.equals(mode))
                        && annotatedTopic.equals(
                             params.get(WebSubSubscriberConstants.PARAM_HUB_TOPIC).stringValue())) {
                    String challenge = params.get(
                    WebSubSubscriberConstants.PARAM_HUB_CHALLENGE).stringValue();
                    response.addHttpContent(new DefaultLastHttpContent(Unpooled.wrappedBuffer(
                            challenge.getBytes(StandardCharsets.UTF_8))));
                    response.setProperty(HttpConstants.HTTP_STATUS_CODE, 202);
                    console.println("ballerina: Intent Verification agreed - Mode [" + mode + "], Topic ["
                                            + annotatedTopic + "], Lease Seconds ["
                                            + params.get(WebSubSubscriberConstants.PARAM_HUB_LEASE_SECONDS) + "]");
                } else {
                    console.println("ballerina: Intent Verification denied - Mode [" + mode + "] for Incorrect Topic ["
                                            + annotatedTopic + "]");
                    response.setProperty(HttpConstants.HTTP_STATUS_CODE, 404);
                }
                HttpUtil.sendOutboundResponse(httpCarbonMessage, response);
            } catch (UnsupportedEncodingException e) {
                throw new BallerinaException("Error responding to intent verification request: " + e.getMessage());
            }
        }
    }

}
