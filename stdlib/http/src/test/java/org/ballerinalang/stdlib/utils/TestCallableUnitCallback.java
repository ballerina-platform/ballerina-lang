/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.stdlib.utils;

import io.ballerina.runtime.api.connector.CallableUnitCallback;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.services.ErrorHandlerUtils;
import org.ballerinalang.net.http.HttpUtil;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Test callback implementation for service tests.
 */
public class TestCallableUnitCallback implements CallableUnitCallback {

    private volatile Semaphore executionWaitSem;
    private HttpCarbonMessage requestMessage;
    private Object request;

    private HttpCarbonMessage responseMsg;
    private int timeOut = 120;

    public TestCallableUnitCallback(HttpCarbonMessage requestMessage) {
        executionWaitSem = new Semaphore(0);
        this.requestMessage = requestMessage;
    }

    @Override
    public void notifySuccess() {

    }

    @Override
    public void notifyFailure(BError error) {
        Integer carbonStatusCode = requestMessage.getHttpStatusCode();
        int statusCode = (carbonStatusCode == null) ? 500 : carbonStatusCode;
        String errorMsg = getAggregatedRootErrorMessages(error);
        ErrorHandlerUtils.printError("error: " + error.getPrintableStackTrace());
        this.responseMsg = HttpUtil.createErrorMessage(errorMsg, statusCode);
        this.executionWaitSem.release();
    }

    public void setRequestStruct(Object request) {
        this.request = request;
    }

    public void sync() {
        try {
            executionWaitSem.tryAcquire(timeOut, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            //ignore
        }
    }

    public void setResponseMsg(HttpCarbonMessage httpCarbonMessage) {
        this.responseMsg = httpCarbonMessage;
        executionWaitSem.release();
    }

    public HttpCarbonMessage getResponseMsg() {
        return responseMsg;
    }

    private static String getAggregatedRootErrorMessages(BError error) {
        return error.getErrorMessage().getValue();
    }
}
