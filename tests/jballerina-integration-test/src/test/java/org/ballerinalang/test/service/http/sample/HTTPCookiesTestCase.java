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

import io.netty.handler.codec.http.HttpHeaderNames;
import org.ballerinalang.test.service.http.HttpBaseTest;
import org.ballerinalang.test.util.HttpClientRequest;
import org.ballerinalang.test.util.HttpResponse;
import org.ballerinalang.test.util.TestConstant;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.ballerinalang.test.context.BMainInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import java.io.File;
import java.io.IOException;

/**
 * Test cases for HTTP cookies.
 */
@Test(groups = "http-test")
public class HTTPCookiesTestCase extends HttpBaseTest {

    @Test(description = "Test send requests by cookie client for first, second and third times")
    public void testRequestsByCookieClient() throws BallerinaTestException {
        String balFilepath = (new File("src" + File.separator + "test" + File.separator + "resources" +
                File.separator + "http" + File.separator + "src" + File.separator + "cookie")).getAbsolutePath();
        BMainInstance bMainInstance = new BMainInstance(balServer);
        String output = bMainInstance.runMainAndReadStdOut("run", new String[]{
                "cookieClient_01.bal" }, balFilepath);
        Assert.assertTrue(output.contains("SID003=895gd4dmnmsddd34; SID002=178gd4dmnmsddd34; SID001=239d4dmnmsddd34"));
    }

    @Test(description = "Test remove session cookie by client")
    public void testRemoveSessionCookieByClient() throws BallerinaTestException {
        String balFilepath = (new File("src" + File.separator + "test" + File.separator + "resources" +
                File.separator + "http" + File.separator + "src" + File.separator + "cookie")).getAbsolutePath();
        BMainInstance bMainInstance = new BMainInstance(balServer);
        String output = bMainInstance.runMainAndReadStdOut("run", new String[]{
                "cookieClient_02.bal" }, balFilepath);
        Assert.assertTrue(output.contains("SID003=895gd4dmnmsddd34"));
    }

    @Test(description = "Test sending similar session cookies in the response by server,old cookie is replaced by new cookie in the cookie store")
    public void testAddSimilarSessionCookie() throws BallerinaTestException {
        String balFilepath = (new File("src" + File.separator + "test" + File.separator + "resources" +
                File.separator + "http" + File.separator + "src" + File.separator + "cookie")).getAbsolutePath();
        BMainInstance bMainInstance = new BMainInstance(balServer);
        String output = bMainInstance.runMainAndReadStdOut("run", new String[]{
                "cookieClient_03.bal" }, balFilepath);
        Assert.assertTrue(output.contains("SID002=178gd4dmnmsddd34"));
    }

    @Test(description = "Test remove session cookie by server")
    public void testRemoveSessionCookieByServer() throws BallerinaTestException {
        String balFilepath = (new File("src" + File.separator + "test" + File.separator + "resources" +
                File.separator + "http" + File.separator + "src" + File.separator + "cookie")).getAbsolutePath();
        BMainInstance bMainInstance = new BMainInstance(balServer);
        String output = bMainInstance.runMainAndReadStdOut("run", new String[]{
                "cookieClient_04.bal" }, balFilepath);
        Assert.assertTrue(output.contains("SID002=178gd4dmnmsddd34"));
    }

    @Test(description = "Test send concurrent requests by cookie client")
    public void testSendConcurrentRequests() throws BallerinaTestException {
        String balFilepath = (new File("src" + File.separator + "test" + File.separator + "resources" +
                File.separator + "http" + File.separator + "src" + File.separator + "cookie")).getAbsolutePath();
        BMainInstance bMainInstance = new BMainInstance(balServer);
        String output = bMainInstance.runMainAndReadStdOut("run", new String[]{
                "cookieClient_05.bal" }, balFilepath);
        // Since same two cookies are sent for all concurrent requests, only two cookies are stored in the cookie store.
        Assert.assertTrue(output.contains("SID001") && output.contains("SID003") && output.contains("2"));
    }
}
