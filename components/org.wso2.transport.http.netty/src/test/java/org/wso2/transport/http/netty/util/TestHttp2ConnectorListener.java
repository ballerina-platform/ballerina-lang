/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.transport.http.netty.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.compression.ServerRespCompressionTestCase;

import java.util.concurrent.CountDownLatch;

/**
 * A connector listener for HTTP
 */
public class TestHttp2ConnectorListener implements Http2ConnectorListener {

    private static final Logger log = LoggerFactory.getLogger(ServerRespCompressionTestCase.class);

    private Http2Response http2Response;
    private Throwable throwable;
    private CountDownLatch latch;

    public TestHttp2ConnectorListener(CountDownLatch latch) {
        this.latch = latch;
    }

    @Override
    public void onMessage(Http2Response http2Response) {
        this.http2Response = http2Response;
        latch.countDown();
    }

    @Override
    public void onError(Throwable throwable) {
        this.throwable = throwable;
        latch.countDown();
    }

    public Http2Response getHttpResponseMessage() {
        return http2Response;
    }

    public Throwable getHttpErrorMessage() {
        return throwable;
    }
}
