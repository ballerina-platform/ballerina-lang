/*
 * Copyright (c) 2019, WSO2 Inc. (http:www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http:www.apache.orglicensesLICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specif ic language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.service.http.sample;

import org.ballerinalang.test.context.BMainInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.service.http.HttpBaseTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;

/**
 * Test cases for HTTP cookies.
 */
@Test(groups = "http-test")
public class HTTPCookiesTestCase extends HttpBaseTest {

    @Test(description = "Test to send requests by cookie client for first, second and third times")
    public void testSendRequestsByCookieClient() throws BallerinaTestException {
        String balFilePath = (new File("src" + File.separator + "test" + File.separator + "resources" +
                                               File.separator + "http" + File.separator + "src" + File.separator +
                                               "cookie")).getAbsolutePath();
        BMainInstance bMainInstance = new BMainInstance(balServer);
        String output = bMainInstance.runMainAndReadStdOut("run", new String[]{
                "cookieClient_01.bal"}, balFilePath);
        Assert.assertTrue(output.contains("SID001=239d4dmnmsddd34; SID003=895gd4dmnmsddd34; SID002=178gd4dmnmsddd34"));
    }

    @Test(description = "Test to remove a session cookie by client")
    public void testRemoveSessionCookieByClient() throws BallerinaTestException {
        String balFilePath = (new File("src" + File.separator + "test" + File.separator + "resources" +
                                               File.separator + "http" + File.separator + "src" + File.separator +
                                               "cookie")).getAbsolutePath();
        BMainInstance bMainInstance = new BMainInstance(balServer);
        String output = bMainInstance.runMainAndReadStdOut("run", new String[]{
                "cookieClient_02.bal"}, balFilePath);
        Assert.assertTrue(output.contains("SID001=239d4dmnmsddd34"));
    }

    @Test(description = "Test sending similar session cookies in the response by server,old cookie is replaced by new" +
            " cookie in the cookie store")
    public void testAddSimilarSessionCookies() throws BallerinaTestException {
        String balFilePath = (new File("src" + File.separator + "test" + File.separator + "resources" +
                                               File.separator + "http" + File.separator + "src" + File.separator +
                                               "cookie")).getAbsolutePath();
        BMainInstance bMainInstance = new BMainInstance(balServer);
        String output = bMainInstance.runMainAndReadStdOut("run", new String[]{
                "cookieClient_03.bal"}, balFilePath);
        Assert.assertTrue(output.contains("SID002=178gd4dmnmsddd34"));
    }

    @Test(description = "Test to remove a session cookie by server")
    public void testRemoveSessionCookieByServer() throws BallerinaTestException {
        String balFilePath = (new File("src" + File.separator + "test" + File.separator + "resources" +
                                               File.separator + "http" + File.separator + "src" + File.separator +
                                               "cookie")).getAbsolutePath();
        BMainInstance bMainInstance = new BMainInstance(balServer);
        String output = bMainInstance.runMainAndReadStdOut("run", new String[]{
                "cookieClient_04.bal"}, balFilePath);
        Assert.assertTrue(output.contains("SID002=178gd4dmnmsddd34"));
    }

    @Test(description = "Test to send concurrent requests by cookie client")
    public void testSendConcurrentRequests() throws BallerinaTestException {
        String balFilePath = (new File("src" + File.separator + "test" + File.separator + "resources" +
                                               File.separator + "http" + File.separator + "src" + File.separator +
                                               "cookie")).getAbsolutePath();
        BMainInstance bMainInstance = new BMainInstance(balServer);
        String output = bMainInstance.runMainAndReadStdOut("run", new String[]{
                "cookieClient_05.bal"}, balFilePath);
        // Since same two cookies are sent for all concurrent requests, only two cookies are stored in the cookie store.
        Assert.assertTrue(output.contains("SID001") && output.contains("SID003") && output.contains("2"));
    }

    @Test(description = "Test to send requests by a client with Circuit Breaker, Retry and Cookie configurations are " +
            "enabled")
    public void testSendRequestsByClient() throws BallerinaTestException {
        String balFilePath = (new File("src" + File.separator + "test" + File.separator + "resources" +
                                               File.separator + "http" + File.separator + "src" + File.separator +
                                               "cookie")).getAbsolutePath();
        BMainInstance bMainInstance = new BMainInstance(balServer);
        String output = bMainInstance.runMainAndReadStdOut("run", new String[]{
                "cookieClient_06.bal"}, balFilePath);
        Assert.assertTrue(output.contains("SID001=239d4dmnmsddd34; SID003=895gd4dmnmsddd34; SID002=178gd4dmnmsddd34"));
    }

    @Test(description = "Test to remove a persistent cookie by the client")
    public void testRemovePersistentCookieByClient() throws BallerinaTestException {
        String balFilePath = (new File("src" + File.separator + "test" + File.separator + "resources" +
                                               File.separator + "http" + File.separator + "src" + File.separator +
                                               "cookie")).getAbsolutePath();
        BMainInstance bMainInstance = new BMainInstance(balServer);
        String output = bMainInstance.runMainAndReadStdOut("run", new String[]{
                "cookieClient_07.bal"}, balFilePath);
        Assert.assertTrue(output.contains("SID003=895gd4dmnmsddd34"));
    }

    @Test(description = "Test to send similar persistent cookies in the response by server. The old cookie is " +
            "replaced by the new cookie in the cookie store")
    public void testAddSimilarPersistentCookies() throws BallerinaTestException {
        String balFilePath = (new File("src" + File.separator + "test" + File.separator + "resources" +
                                               File.separator + "http" + File.separator + "src" + File.separator +
                                               "cookie")).getAbsolutePath();
        BMainInstance bMainInstance = new BMainInstance(balServer);
        String output = bMainInstance.runMainAndReadStdOut("run", new String[]{
                "cookieClient_08.bal"}, balFilePath);
        Assert.assertTrue(output.contains("SID001=895gd4dmnmsddd34"));
    }

    @Test(description = "Test to send a session cookie and a similar persistent cookie in the response by server. The" +
            " old session cookie is replaced by the new persistent cookie in the cookie store")
    public void testSendSimilarPersistentAndSessionCookies_1() throws BallerinaTestException {
        String balFilePath = (new File("src" + File.separator + "test" + File.separator + "resources" +
                                               File.separator + "http" + File.separator + "src" + File.separator +
                                               "cookie")).getAbsolutePath();
        BMainInstance bMainInstance = new BMainInstance(balServer);
        String output = bMainInstance.runMainAndReadStdOut("run", new String[]{
                "cookieClient_09.bal"}, balFilePath);
        Assert.assertTrue(output.contains("SID003=aeaa895gd4dmnmsddd34"));
    }

    @Test(description = "Test to send a persistent cookie and a similar session cookie in the response by the server." +
            " The old persistent cookie is replaced by the new session cookie in the cookie store")
    public void testSendSimilarPersistentAndSessionCookies_2() throws BallerinaTestException {
        String balFilePath = (new File("src" + File.separator + "test" + File.separator + "resources" +
                                               File.separator + "http" + File.separator + "src" + File.separator +
                                               "cookie")).getAbsolutePath();
        BMainInstance bMainInstance = new BMainInstance(balServer);
        String output = bMainInstance.runMainAndReadStdOut("run", new String[]{
                "cookieClient_10.bal"}, balFilePath);
        Assert.assertTrue(output.contains("SID003=895gd4dmnmsddd34"));
    }

    @Test(description = "Test to remove a persistent cookie by the server")
    public void testRemovePersistentCookieByServer() throws BallerinaTestException {
        String balFilePath = (new File("src" + File.separator + "test" + File.separator + "resources" +
                                               File.separator + "http" + File.separator + "src" + File.separator +
                                               "cookie")).getAbsolutePath();
        BMainInstance bMainInstance = new BMainInstance(balServer);
        String output = bMainInstance.runMainAndReadStdOut("run", new String[]{
                "cookieClient_11.bal"}, balFilePath);
        Assert.assertTrue(output.contains("SID002=178gd4dmnmsddd34"));
    }

    @Test(description = "Test to send persistent cookies when the persistentCookieHandler is not configured")
    public void testSendPersistentCookiesWithoutPersistentCookieHandler() throws BallerinaTestException {
        String balFilePath = (new File("src" + File.separator + "test" + File.separator + "resources" +
                                               File.separator + "http" + File.separator + "src" + File.separator +
                                               "cookie")).getAbsolutePath();
        BMainInstance bMainInstance = new BMainInstance(balServer);
        String output = bMainInstance.runMainAndReadStdOut("run", new String[]{
                "cookieClient_12.bal"}, balFilePath);
        Assert.assertTrue(output.contains("SID003=895gd4dmnmsddd34"));
    }
}
