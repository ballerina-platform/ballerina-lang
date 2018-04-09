/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.lock;

import org.ballerinalang.launcher.util.BServiceUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.util.StringUtils;
import org.ballerinalang.test.services.testutils.HTTPTestRequest;
import org.ballerinalang.test.services.testutils.MessageUtils;
import org.ballerinalang.test.services.testutils.Services;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.transport.http.netty.message.HttpMessageDataStreamer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Test class Ballerina locks in services.
 *
 * @since 0.961.0
 */
public class LocksInServicesTest {

    CompileResult compileResult;
    private static final String MOCK_ENDPOINT_NAME = "echoEP";

    @BeforeClass()
    public void setup() {
        compileResult = BServiceUtil
                .setupProgramFile(this, "test-src/lock/locks-in-services.bal");
    }

    @Test(description = "Test locking service level variable basic", enabled = false)
    public void testServiceLvlVarLockBasic() {
        Semaphore semaphore = new Semaphore(-9999);

        ExecutorService executor = TestThreadPool.getInstance().getExecutor();

        for (int i = 0; i < 10000; i++) {
            executor.submit(new TestRequestSender(compileResult, semaphore, "/sample/echo"));
        }

        try {
            if (!semaphore.tryAcquire(20, TimeUnit.MINUTES)) {
                Assert.fail("request execution not finished within 2s");
            }
            String path = "/sample/getCount";
            HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
            HTTPCarbonMessage response = Services.invokeNew(compileResult, MOCK_ENDPOINT_NAME, cMsg);

            Assert.assertNotNull(response, "Response message not found");
            String responseMsgPayload = StringUtils.getStringFromInputStream(new HttpMessageDataStreamer(response)
                    .getInputStream());
            Assert.assertEquals(responseMsgPayload, "count - 10000", "incorrect request count");
        } catch (InterruptedException e) {
            Assert.fail("thread interrupted before request execution finished - " + e.getMessage(), e);
        }
    }


    @Test(description = "Test locking service level variable complex", enabled = false)
    public void testServiceLvlVarLockComplex() {
        Semaphore semaphore = new Semaphore(-11);

        ExecutorService executor = TestThreadPool.getInstance().getExecutor();

        for (int i = 0; i < 4; i++) {
            executor.submit(new TestRequestSender(compileResult, semaphore, "/sample1/echo"));
            executor.submit(new TestRequestSender(compileResult, semaphore, "/sample1/echo1"));
            executor.submit(new TestRequestSender(compileResult, semaphore, "/sample1/echo2"));
        }

        try {
            if (!semaphore.tryAcquire(10, TimeUnit.MINUTES)) {
                Assert.fail("request execution not finished within 2s");
            }
            String path = "/sample1/getResult";
            HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
            HTTPCarbonMessage response = Services.invokeNew(compileResult, MOCK_ENDPOINT_NAME, cMsg);

            Assert.assertNotNull(response, "Response message not found");
            String responseMsgPayload = StringUtils.getStringFromInputStream(new HttpMessageDataStreamer(response)
                    .getInputStream());
            Assert.assertEquals(responseMsgPayload, "3333333333331224.01455555555555513.026.0777777777777",
                    "incorrect request count");
        } catch (InterruptedException e) {
            Assert.fail("thread interrupted before request execution finished - " + e.getMessage(), e);
        }
    }

    @Test(description = "Test locking service level and package level variable complex", enabled = false)
    public void testServiceLvlPkgLvlVarLockComplex() {
        Semaphore semaphore = new Semaphore(-11);

        ExecutorService executor = TestThreadPool.getInstance().getExecutor();

        for (int i = 0; i < 4; i++) {
            executor.submit(new TestRequestSender(compileResult, semaphore, "/sample2/echo"));
            executor.submit(new TestRequestSender(compileResult, semaphore, "/sample2/echo1"));
            executor.submit(new TestRequestSender(compileResult, semaphore, "/sample2/echo2"));
        }

        try {
            if (!semaphore.tryAcquire(10, TimeUnit.MINUTES)) {
                Assert.fail("request execution not finished within 2s");
            }
            String path = "/sample2/getResult";
            HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
            HTTPCarbonMessage response = Services.invokeNew(compileResult, MOCK_ENDPOINT_NAME, cMsg);

            Assert.assertNotNull(response, "Response message not found");
            String responseMsgPayload = StringUtils.getStringFromInputStream(new HttpMessageDataStreamer(response)
                    .getInputStream());
            Assert.assertEquals(responseMsgPayload, "3333333333331224.01455555555555513.026.0777777777777",
                    "incorrect request count");
        } catch (InterruptedException e) {
            Assert.fail("thread interrupted before request execution finished - " + e.getMessage(), e);
        }
    }

    @Test(description = "Test throwing error inside lock statement", enabled = false)
    public void testThrowErrorInsideLock() {
        Semaphore semaphore = new Semaphore(0);

        ExecutorService executor = TestThreadPool.getInstance().getExecutor();

        executor.submit(new TestRequestSender(compileResult, semaphore, "/sample3/echo"));

        try {
            if (!semaphore.tryAcquire(500, TimeUnit.MILLISECONDS)) {
                Assert.fail("request execution not finished within 100ms");
            }
            String path = "/sample3/getMsg";
            HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
            HTTPCarbonMessage response = Services.invokeNew(compileResult, MOCK_ENDPOINT_NAME, cMsg);

            Assert.assertNotNull(response, "Response message not found");
            String responseMsgPayload = StringUtils.getStringFromInputStream(new HttpMessageDataStreamer(response)
                    .getInputStream());
            Assert.assertEquals(responseMsgPayload, "modified by second resource", "incorrect response msg");
        } catch (InterruptedException e) {
            Assert.fail("thread interrupted before request execution finished - " + e.getMessage(), e);
        }
    }

    private class TestRequestSender implements Runnable {

        private CompileResult compileResult;

        private Semaphore semaphore;

        private String path;

        TestRequestSender(CompileResult compileResult, Semaphore semaphore, String path) {
            this.compileResult = compileResult;
            this.semaphore = semaphore;
            this.path = path;
        }

        @Override
        public void run() {
            HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
            Services.invokeNew(compileResult,MOCK_ENDPOINT_NAME, cMsg);
            semaphore.release();
        }
    }
}
