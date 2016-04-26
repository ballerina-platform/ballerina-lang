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

package org.wso2.carbon.transport.http.netty.listener;

import org.wso2.carbon.transport.http.netty.config.TransportProperty;
import org.wso2.carbon.transport.http.netty.config.YAMLTransportConfigurationBuilder;

import java.util.Set;

/**
 * Configuration for the request size validation
 */
public class RequestSizeValidationConfiguration {

    private static RequestSizeValidationConfiguration instance = new RequestSizeValidationConfiguration();
    private boolean requestSizeValidation = false;
    private int requestMaxSize = Integer.MAX_VALUE;
    private int statusCode = 401;
    private String rejectMessage = "Message is bigger than the valid size";
    private String rejectMsgContentType = "plain/text";

    private RequestSizeValidationConfiguration() {
        Set<TransportProperty> transportProperties = YAMLTransportConfigurationBuilder.build().getTransportProperties();

        for (TransportProperty transportProperty : transportProperties) {
            if (transportProperty.getName().equalsIgnoreCase("request.size.validation")) {
                requestSizeValidation = (Boolean) transportProperty.getValue();
            }
            if (transportProperty.getName().equalsIgnoreCase("request.size.validation.maximum.value")) {
                requestMaxSize = (Integer) transportProperty.getValue();
            }
            if (transportProperty.getName().equalsIgnoreCase("request.size.validation.reject.status.code")) {
                statusCode = (Integer) transportProperty.getValue();
            }
            if (transportProperty.getName().equalsIgnoreCase("request.size.validation.reject.message")) {
                rejectMessage = (String) transportProperty.getValue();
            }
            if (transportProperty.getName().equalsIgnoreCase("request.size.validation.reject.message.content.type")) {
                rejectMsgContentType = (String) transportProperty.getValue();
            }
        }
    }

    public static RequestSizeValidationConfiguration getInstance() {
        return instance;
    }

    public boolean isRequestSizeValidation() {
        return requestSizeValidation;
    }

    public int getRequestMaxSize() {
        return requestMaxSize;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getRejectMessage() {
        return rejectMessage;
    }

    public String getRejectMsgContentType() {
        return rejectMsgContentType;
    }
}
