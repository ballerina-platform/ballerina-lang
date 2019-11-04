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

package org.ballerinalang.test.service.websub;

import io.netty.handler.codec.http.HttpHeaderNames;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.util.HttpClientRequest;
import org.ballerinalang.test.util.HttpResponse;
import org.ballerinalang.test.util.TestConstant;

import java.io.IOException;
import java.util.HashMap;

/**
 * Utils class for WebSub Tests.
 */
public class WebSubTestUtils {

    public static final String PUBLISHER_NOTIFY_URL = "http://localhost:23080/publisher/notify";
    public static final String PUBLISHER_NOTIFY_URL_TWO = "http://localhost:23080/publisherTwo/notify";
    public static final String PUBLISHER_NOTIFY_URL_THREE = "http://localhost:23080/publisherThree/notify";

    public static final String HUB_MODE_INTERNAL = "internal";
    public static final String HUB_MODE_REMOTE = "remote";

    public static final String CONTENT_TYPE_JSON = "json";
    public static final String CONTENT_TYPE_XML = "xml";
    public static final String CONTENT_TYPE_STRING = "string";
    public static final String PATH_SEPARATOR = "/";

    public static HttpResponse requestUpdateAndGetResponse(String url, String mode, String contentType)
            throws BallerinaTestException {
        try {
            HashMap<String, String> headers = new HashMap<>(1);
            headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), TestConstant.CONTENT_TYPE_JSON);
            return HttpClientRequest.doPost(url, "{\"mode\":\"" + mode + "\",\"content_type\":\"" + contentType + "\"}",
                                     headers);
        } catch (IOException e) {
            throw new BallerinaTestException("Error requesting content delivery");
        }
    }

    public static void requestUpdate(String url, String mode, String contentType) throws BallerinaTestException {
        requestUpdateAndGetResponse(url, mode, contentType);
    }

    public static void requestUpdateWithContent(String url, String content) throws BallerinaTestException {
        try {
            HashMap<String, String> headers = new HashMap<>(1);
            headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), TestConstant.CONTENT_TYPE_JSON);
            HttpClientRequest.doPost(url, content, headers);
        } catch (IOException e) {
            throw new BallerinaTestException("Error requesting content delivery");
        }
    }
}
