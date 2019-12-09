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

import org.ballerinalang.model.util.StringUtils;
import org.ballerinalang.test.services.testutils.HTTPTestRequest;
import org.ballerinalang.test.services.testutils.MessageUtils;
import org.ballerinalang.test.services.testutils.Services;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;
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
    private static final int MOCK_ENDPOINT_PORT = 9090;

    @BeforeClass()
    public void setup() {
        compileResult = BCompileUtil
                .compile(true, "test-src/lock/locks-in-services.bal");
    }

    @Test(description = "Test locking service level variable basic")
    public void testServiceLvlVarLockBasic() {
        Semaphore semaphore = new Semaphore(-999);

        ExecutorService executor = TestThreadPool.getInstance().getExecutor();

        for (int i = 0; i < 1000; i++) {
            executor.submit(new TestRequestSender(semaphore, "/sample/echo"));
        }

        try {
            if (!semaphore.tryAcquire(20, TimeUnit.MINUTES)) {
                Assert.fail("request execution not finished within 2s");
            }
            String path = "/sample/getCount";
            HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
            HttpCarbonMessage response = Services.invoke(MOCK_ENDPOINT_PORT, cMsg, false);

            Assert.assertNotNull(response, "Response message not found");
            String responseMsgPayload = StringUtils.getStringFromInputStream(new HttpMessageDataStreamer(response)
                    .getInputStream());
            Assert.assertEquals(responseMsgPayload, "count - 1000", "incorrect request count");
        } catch (InterruptedException e) {
            Assert.fail("thread interrupted before request execution finished - " + e.getMessage(), e);
        }
    }


    @Test(description = "Test locking service level variable complex")
    public void testServiceLvlVarLockComplex() {
        Semaphore semaphore = new Semaphore(-11);

        ExecutorService executor = TestThreadPool.getInstance().getExecutor();

        for (int i = 0; i < 4; i++) {
            executor.submit(new TestRequestSender(semaphore, "/sample1/echo"));
            executor.submit(new TestRequestSender(semaphore, "/sample1/echo1"));
            executor.submit(new TestRequestSender(semaphore, "/sample1/echo2"));
        }

        try {
            if (!semaphore.tryAcquire(10, TimeUnit.MINUTES)) {
                Assert.fail("request execution not finished within 2s");
            }
            String path = "/sample1/getResult";
            HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
            HttpCarbonMessage response = Services.invoke(MOCK_ENDPOINT_PORT, cMsg, false);

            Assert.assertNotNull(response, "Response message not found");
            String responseMsgPayload = StringUtils.getStringFromInputStream(new HttpMessageDataStreamer(response)
                    .getInputStream());
            Assert.assertEquals(responseMsgPayload, "3333333333331224.01455555555555513.026.0777777777777",
                    "incorrect request count");
        } catch (InterruptedException e) {
            Assert.fail("thread interrupted before request execution finished - " + e.getMessage(), e);
        }
    }

    @Test(description = "Test locking service level and package level variable complex")
    public void testServiceLvlPkgLvlVarLockComplex() {
        Semaphore semaphore = new Semaphore(-11);

        ExecutorService executor = TestThreadPool.getInstance().getExecutor();

        for (int i = 0; i < 4; i++) {
            executor.submit(new TestRequestSender(semaphore, "/sample2/echo"));
            executor.submit(new TestRequestSender(semaphore, "/sample2/echo1"));
            executor.submit(new TestRequestSender(semaphore, "/sample2/echo2"));
        }

        try {
            if (!semaphore.tryAcquire(10, TimeUnit.MINUTES)) {
                Assert.fail("request execution not finished within 2s");
            }
            String path = "/sample2/getResult";
            HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
            HttpCarbonMessage response = Services.invoke(MOCK_ENDPOINT_PORT, cMsg, false);

            Assert.assertNotNull(response, "Response message not found");
            String responseMsgPayload = StringUtils.getStringFromInputStream(new HttpMessageDataStreamer(response)
                    .getInputStream());
            Assert.assertEquals(responseMsgPayload, "3333333333331224.01455555555555513.026.0777777777777",
                    "incorrect request count");
        } catch (InterruptedException e) {
            Assert.fail("thread interrupted before request execution finished - " + e.getMessage(), e);
        }
    }

    @Test(description = "Test throwing error inside lock statement")
    public void testThrowErrorInsideLock() {
        Semaphore semaphore = new Semaphore(0);

        ExecutorService executor = TestThreadPool.getInstance().getExecutor();

        executor.submit(new TestRequestSender(semaphore, "/sample3/echo"));

        try {
            if (!semaphore.tryAcquire(500, TimeUnit.MILLISECONDS)) {
                Assert.fail("request execution not finished within 100ms");
            }
            String path = "/sample3/getMsg";
            HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
            HttpCarbonMessage response = Services.invoke(MOCK_ENDPOINT_PORT, cMsg, false);

            Assert.assertNotNull(response, "Response message not found");
            String responseMsgPayload = StringUtils.getStringFromInputStream(new HttpMessageDataStreamer(response)
                    .getInputStream());
            Assert.assertEquals(responseMsgPayload, "modified by second resource", "incorrect response msg");
        } catch (InterruptedException e) {
            Assert.fail("thread interrupted before request execution finished - " + e.getMessage(), e);
        }
    }

    @Test(description = "Test field locking in services")
    public void testFieldLock() {

        String path = "/sample4/echo";
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        HttpCarbonMessage response = Services.invoke(MOCK_ENDPOINT_PORT, cMsg, false);

        Assert.assertNotNull(response, "Response message not found");
        String responseMsgPayload =
                StringUtils.getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertTrue(responseMsgPayload.equals("1001000") || responseMsgPayload.equals("500500"),
                "incorrect response value");

    }

    private class TestRequestSender implements Runnable {

        private Semaphore semaphore;

        private String path;

        TestRequestSender(Semaphore semaphore, String path) {
            this.semaphore = semaphore;
            this.path = path;
        }

        @Override
        public void run() {
            HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "GET");
            Services.invoke(MOCK_ENDPOINT_PORT, cMsg, false);
            semaphore.release();
        }
    }
}
