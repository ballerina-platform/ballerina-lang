/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.transport.http.netty.config;

import java.util.Set;

/**
 * Configuration for the request size validation.
 */
public class RequestSizeValidationConfiguration {

    private static RequestSizeValidationConfiguration instance = new RequestSizeValidationConfiguration();

    private boolean requestSizeValidation = false;
    private int requestMaxSize = Integer.MAX_VALUE;
    private int requestRejectStatusCode = 401;
    private String requestRejectMessage = "Message is bigger than the valid size";
    private String requestRejectMsgContentType = "plain/text";

    private boolean headerSizeValidation = false;
    private int headerMaxRequestLineSize = 4096;
    private int headerMaxSize = 8192;
    private int maxChunkSize = 8192;
    private int headerRejectStatusCode = 401;
    private String headerRejectMessage = "Message header is bigger than the valid size";
    private String headerRejectMsgContentType = "plain/text";

    private RequestSizeValidationConfiguration() {
        Set<TransportProperty> transportProperties = YAMLTransportConfigurationBuilder.build().getTransportProperties();

         transportProperties.forEach(transportProperty -> {
            if (transportProperty.getName().equalsIgnoreCase("request.size.validation")) {
                requestSizeValidation = (Boolean) transportProperty.getValue();
            }
            if (transportProperty.getName().equalsIgnoreCase("request.size.validation.maximum.value")) {
                requestMaxSize = (Integer) transportProperty.getValue();
            }
            if (transportProperty.getName().equalsIgnoreCase("request.size.validation.reject.status.code")) {
                requestRejectStatusCode = (Integer) transportProperty.getValue();
            }
            if (transportProperty.getName().equalsIgnoreCase("request.size.validation.reject.message")) {
                requestRejectMessage = (String) transportProperty.getValue();
            }
            if (transportProperty.getName().equalsIgnoreCase("request.size.validation.reject.message.content.type")) {
                requestRejectMsgContentType = (String) transportProperty.getValue();
            }
            if (transportProperty.getName().equalsIgnoreCase("header.size.validation")) {
                headerSizeValidation = (Boolean) transportProperty.getValue();
            }
            if (transportProperty.getName().equalsIgnoreCase("header.validation.maximum.request.line")) {
                headerMaxRequestLineSize = (int) transportProperty.getValue();
            }
            if (transportProperty.getName().equalsIgnoreCase("header.validation.maximum.size")) {
                headerMaxSize = (int) transportProperty.getValue();
            }
            if (transportProperty.getName().equalsIgnoreCase("header.validation.maximum.chunk.size")) {
                maxChunkSize = (int) transportProperty.getValue();
            }
            if (transportProperty.getName().equalsIgnoreCase("header.validation.reject.status.code")) {
                headerRejectStatusCode = (int) transportProperty.getValue();
            }
            if (transportProperty.getName().equalsIgnoreCase("header.validation.reject.message")) {
                headerRejectMessage = (String) transportProperty.getValue();
            }
            if (transportProperty.getName().equalsIgnoreCase("header.validation.reject.message.content.type")) {
                headerRejectMsgContentType = (String) transportProperty.getValue();
            }
        });
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

    public int getRequestRejectStatusCode() {
        return requestRejectStatusCode;
    }

    public String getRequestRejectMessage() {
        return requestRejectMessage;
    }

    public String getRequestRejectMsgContentType() {
        return requestRejectMsgContentType;
    }

    public boolean isHeaderSizeValidation() {
        return headerSizeValidation;
    }

    public int getHeaderMaxRequestLineSize() {
        return headerMaxRequestLineSize;
    }

    public int getHeaderMaxSize() {
        return headerMaxSize;
    }

    public int getMaxChunkSize() {
        return maxChunkSize;
    }

    public int getHeaderRejectStatusCode() {
        return headerRejectStatusCode;
    }

    public String getHeaderRejectMessage() {
        return headerRejectMessage;
    }

    public String getHeaderRejectMsgContentType() {
        return headerRejectMsgContentType;
    }
}
