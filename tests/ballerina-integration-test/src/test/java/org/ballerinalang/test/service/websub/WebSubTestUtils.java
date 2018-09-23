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
import org.ballerinalang.test.util.TestConstant;

import java.io.IOException;
import java.util.HashMap;

/**
 * Utils class for WebSub Tests.
 */
class WebSubTestUtils {

    static final String PUBLISHER_NOTIFY_URL = "http://localhost:8080/publisher/notify";
    static final String PUBLISHER_NOTIFY_URL_TWO = "http://localhost:8080/publisherTwo/notify";

    static final String HUB_MODE_INTERNAL = "internal";
    static final String HUB_MODE_REMOTE = "remote";

    static final String CONTENT_TYPE_JSON = "json";
    static final String CONTENT_TYPE_XML = "xml";
    static final String CONTENT_TYPE_STRING = "string";

    static void requestUpdate(String url, String mode, String contentType) throws BallerinaTestException {
        try {
            HashMap<String, String> headers = new HashMap<>(1);
            headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), TestConstant.CONTENT_TYPE_JSON);
            HttpClientRequest.doPost(url, "{\"mode\":\"" + mode + "\",\"content_type\":\"" + contentType + "\"}",
                                     headers);
        } catch (IOException e) {
            throw new BallerinaTestException("Error requesting content delivery");
        }
    }
}
