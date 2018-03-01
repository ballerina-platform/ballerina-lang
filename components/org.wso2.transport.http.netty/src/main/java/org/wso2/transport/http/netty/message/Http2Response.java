/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.transport.http.netty.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.common.Constants;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * {@code Http2Response} is the a holder for http2 responses associated with a single request
 * <p>
 * This contains intended Http2 Response, Push Promises and Server Push Responses
 */
public class Http2Response {

    private static final Logger log = LoggerFactory.getLogger(Http2Response.class);

    private BlockingQueue<HttpCarbonResponse> pushResponses;
    private BlockingQueue<Http2PushPromise> promises;
    private HttpCarbonResponse response;
    private int soTimeOut = Constants.ENDPOINT_TIMEOUT;

    private boolean allPromisesReceived = false;
    private int promisesCount = 0;
    private int pushResponsesCount = 0;

    private Lock promisesLock;
    private Lock pushResponsesLock;
    private Lock responseLock;
    private Condition promiseCondition;
    private Condition responseCondition;
    private Condition pushResponseCondition;

    public Http2Response() {
        pushResponses = new LinkedBlockingQueue();
        promises = new LinkedBlockingQueue();
        promisesLock = new ReentrantLock();
        pushResponsesLock = new ReentrantLock();
        responseLock = new ReentrantLock();
        this.promiseCondition = promisesLock.newCondition();
        this.responseCondition = responseLock.newCondition();
        this.pushResponseCondition = pushResponsesLock.newCondition();
    }

    public void setResponse(HttpCarbonResponse response) {
        allPromisesReceived = true;
        try {
            responseLock.lock();
            this.response = response;
            responseCondition.signalAll();
        } finally {
            responseLock.unlock();
        }
    }

    public void addPromise(Http2PushPromise pushPromise) {
        try {
            promisesLock.lock();
            promises.add(pushPromise);
            promisesCount++;
            promiseCondition.signalAll();
        } finally {
            promisesLock.unlock();
        }
    }

    public void addPushResponse(HttpCarbonResponse pushResponse) {
        try {
            pushResponsesLock.lock();
            pushResponses.add(pushResponse);
            pushResponseCondition.signalAll();
            pushResponsesCount++;
        } finally {
            pushResponsesLock.unlock();
        }
    }

    public HttpCarbonResponse getResponse() {
        try {
            responseLock.lock();
            waitForResponse();
        } catch (InterruptedException e) {
            log.error("Error while getting the Response");
        } finally {
            responseLock.unlock();
        }
        return response;
    }

    public boolean hasPromise() {
        if (allPromisesReceived) {
            return !promises.isEmpty();
        } else {
            try {
                promisesLock.lock();
                waitForPromise();
                return !promises.isEmpty();
            } catch (InterruptedException e) {
                log.error("Error while getting the Push Promises from the queue");
            } finally {
                promisesLock.unlock();
            }
        }
        return false;
    }

    public boolean hasPushResponse() {
        if (pushResponsesCount < promisesCount || !allPromisesReceived) {
            try {
                pushResponsesLock.lock();
                waitForPushResponse();
                return !pushResponses.isEmpty();
            } catch (InterruptedException e) {
                log.error("Error while getting the Push Responses from the queue");
            } finally {
                pushResponsesLock.unlock();
            }
        } else {
            return !pushResponses.isEmpty();
        }
        return false;
    }

    public Http2PushPromise getNextPromise() {

        Http2PushPromise promise = null;
        try {
            promisesLock.lock();
            waitForPromise();
            promise = promises.poll();
        } catch (InterruptedException e) {
            log.error("Error while getting the Push Promises from the queue");
        } finally {
            promisesLock.unlock();
        }
        return promise;
    }

    public HttpCarbonResponse getNextPushResponse() {

        HttpCarbonResponse pushMessage = null;
        try {
            pushResponsesLock.lock();
            waitForPushResponse();
            pushMessage = pushResponses.poll();
        } catch (InterruptedException e) {
            log.error("Error while getting the Push Promises from the queue");
        } finally {
            pushResponsesLock.unlock();
        }
        return pushMessage;
    }

    private void waitForPromise() throws InterruptedException {
        while (!allPromisesReceived && promises.isEmpty()) {
            if (!promiseCondition.await(soTimeOut, TimeUnit.SECONDS)) {
                break;
            }
        }
    }

    private void waitForPushResponse() throws InterruptedException {
        while ((pushResponsesCount < promisesCount || !allPromisesReceived) && pushResponses.isEmpty()) {
            if (!pushResponseCondition.await(soTimeOut, TimeUnit.SECONDS)) {
                break;
            }
        }
    }

    private void waitForResponse() throws InterruptedException {
        while (response == null) {
            if (!responseCondition.await(soTimeOut, TimeUnit.SECONDS)) {
                break;
            }
        }
    }

}
