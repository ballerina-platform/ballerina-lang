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

package org.wso2.transport.http.netty.unitfunction;

import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.contract.Constants;
import org.wso2.transport.http.netty.contractimpl.sender.ForwardedHeaderUpdater;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

/**
 * Unit test cases for ForwardedHeaderUpdater class functions.
 */
public class ForwardedTestCase {

    @Test(description = "Test setting Forwarded header as first intermediate interface")
    public void testSetForwardedHeaderAsFirstInterface() {
        HttpCarbonMessage outboundRequestMsg = new HttpCarbonMessage(new DefaultHttpRequest(HttpVersion.HTTP_1_1,
                                                                                            HttpMethod.POST, ""));
        outboundRequestMsg.setProperty(Constants.TO, "/hello");
        outboundRequestMsg.setProperty(Constants.ORIGIN_HOST, "www.example.com");
        outboundRequestMsg.setProperty(Constants.PROTOCOL, Constants.HTTP_SCHEME);
        ForwardedHeaderUpdater headerUpdater = new ForwardedHeaderUpdater(outboundRequestMsg, "10.100.1.92");
        headerUpdater.setForwardedHeader();
        Assert.assertEquals(outboundRequestMsg.getHeader("Forwarded"),
                "by=10.100.1.92; host=www.example.com; proto=http");
    }

    @Test(description = "Test setting Forwarded header with IPv6 local address")
    public void testSetForwardedHeaderAsFirstInterfaceIPv6() {
        HttpCarbonMessage outboundRequestMsg = new HttpCarbonMessage(new DefaultHttpRequest(HttpVersion.HTTP_1_1,
                                                                                            HttpMethod.POST, ""));
        outboundRequestMsg.setProperty(Constants.TO, "/hello");
        outboundRequestMsg.setProperty(Constants.ORIGIN_HOST, "www.example.com");
        outboundRequestMsg.setProperty(Constants.PROTOCOL, Constants.HTTPS_SCHEME);
        ForwardedHeaderUpdater headerUpdater = new ForwardedHeaderUpdater(outboundRequestMsg, "2001:db8:cafe::17");
        headerUpdater.setForwardedHeader();
        Assert.assertEquals(outboundRequestMsg.getHeader("Forwarded"),
                "by=\"[2001:db8:cafe::17]\"; host=www.example.com; proto=https");
    }

    @Test(description = "Test setting Forwarded header with IPv6 host and port")
    public void testSetForwardedHeaderAsFirstInterfaceIPv6WithPort() {
        HttpCarbonMessage outboundRequestMsg = new HttpCarbonMessage(new DefaultHttpRequest(HttpVersion.HTTP_1_1,
                                                                                            HttpMethod.POST, ""));
        outboundRequestMsg.setProperty(Constants.TO, "/hello");
        outboundRequestMsg.setProperty(Constants.ORIGIN_HOST, "www.example.com");
        outboundRequestMsg.setProperty(Constants.PROTOCOL, Constants.HTTP_SCHEME);
        ForwardedHeaderUpdater updater = new ForwardedHeaderUpdater(outboundRequestMsg, "[2001:db8:cafe::17]:900");
        updater.setForwardedHeader();
        Assert.assertEquals(outboundRequestMsg.getHeader("Forwarded"),
                "by=\"[2001:db8:cafe::17]:900\"; host=www.example.com; proto=http");
    }

    @Test(description = "Test setting Forwarded header with Obfuscated Identifier")
    public void testSetForwardedHeaderAsFirstInterfaceWithObfuscatedIdentifier() {
        HttpCarbonMessage outboundRequestMsg = new HttpCarbonMessage(new DefaultHttpRequest(HttpVersion.HTTP_1_1,
                                                                                            HttpMethod.POST, ""));
        outboundRequestMsg.setProperty(Constants.TO, "/hello");
        outboundRequestMsg.setProperty(Constants.ORIGIN_HOST, "www.example.com");
        outboundRequestMsg.setProperty(Constants.PROTOCOL, Constants.HTTP_SCHEME);
        ForwardedHeaderUpdater updater = new ForwardedHeaderUpdater(outboundRequestMsg, "_SEVKISEK");
        updater.setForwardedHeader();
        Assert.assertEquals(outboundRequestMsg.getHeader("Forwarded"),
                "by=_SEVKISEK; host=www.example.com; proto=http");
    }

    @Test(description = "Test updating Forwarded header with existing forwarder values")
    public void testSetForwardedHeaderWithPreviousForwardHeader() {
        HttpHeaders headers = new DefaultHttpHeaders();
        headers.set("Forwarded", "for=192.0.2.43, for=198.51.100.17;by=203.0.113.60;proto=http;host=example.com");
        HttpCarbonMessage outboundRequestMsg = new HttpCarbonMessage(
                new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "", headers));
        outboundRequestMsg.setProperty(Constants.TO, "/hello");
        outboundRequestMsg.setProperty(Constants.ORIGIN_HOST, "www.example.com");
        outboundRequestMsg.setProperty(Constants.PROTOCOL, Constants.HTTP_SCHEME);
        ForwardedHeaderUpdater headerUpdater = new ForwardedHeaderUpdater(outboundRequestMsg, "10.100.1.92");
        headerUpdater.setForwardedHeader();
        Assert.assertEquals(outboundRequestMsg.getHeader("Forwarded"),
                "for=192.0.2.43, for=198.51.100.17, for=203.0.113.60; by=10.100.1.92; host=example.com; proto=http");
    }

    @Test(description = "Test setting de-facto Forwarded headers with existing XF headers")
    public void testSetDefactoForwardedHeadersWithPreviousXForwardHeader() {
        HttpHeaders headers = new DefaultHttpHeaders();
        headers.set("X-Forwarded-For", "192.0.2.43");
        headers.set("X-Forwarded-By", "200.35.130.97");
        HttpCarbonMessage outboundRequestMsg = new HttpCarbonMessage(
                new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "", headers));
        outboundRequestMsg.setProperty(Constants.TO, "/hello");
        outboundRequestMsg.setProperty(Constants.ORIGIN_HOST, "www.example.com");
        outboundRequestMsg.setProperty(Constants.PROTOCOL, Constants.HTTP_SCHEME);
        ForwardedHeaderUpdater headerUpdater = new ForwardedHeaderUpdater(outboundRequestMsg, "10.100.1.92");
        headerUpdater.setDefactoForwardedHeaders();
        Assert.assertNull(outboundRequestMsg.getHeader("Forwarded"));
        Assert.assertEquals(outboundRequestMsg.getHeader("X-Forwarded-For"), "192.0.2.43, 200.35.130.97");
        Assert.assertEquals(outboundRequestMsg.getHeader("X-Forwarded-By"), "10.100.1.92");
    }

    @Test(description = "Test setting de-facto Forwarded headers with IPv6")
    public void testSetDefactoForwardedHeadersWithIPv6() {
        HttpHeaders headers = new DefaultHttpHeaders();
        headers.set("X-Forwarded-By", "200.35.130.97");
        HttpCarbonMessage outboundRequestMsg = new HttpCarbonMessage(
                new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "", headers));
        outboundRequestMsg.setProperty(Constants.TO, "/hello");
        outboundRequestMsg.setProperty(Constants.ORIGIN_HOST, "www.example.com");
        outboundRequestMsg.setProperty(Constants.PROTOCOL, Constants.HTTP_SCHEME);
        ForwardedHeaderUpdater updater = new ForwardedHeaderUpdater(outboundRequestMsg, "2001:db8:cafe::17");
        updater.setDefactoForwardedHeaders();
        Assert.assertNull(outboundRequestMsg.getHeader("Forwarded"));
        Assert.assertEquals(outboundRequestMsg.getHeader("X-Forwarded-For"), "200.35.130.97");
        Assert.assertEquals(outboundRequestMsg.getHeader("X-Forwarded-By"), "2001:db8:cafe::17");
        Assert.assertNull(outboundRequestMsg.getHeader("X-Forwarded-Host"));
        Assert.assertNull(outboundRequestMsg.getHeader("X-Forwarded-Proto"));
    }

    @Test(description = "Test setting Forwarded headers with forward and XF headers available")
    public void testSetForwardedHeadersWhenForwardAndXHeadersAvailable() {
        HttpHeaders headers = new DefaultHttpHeaders();
        headers.set("Forwarded", "for=192.0.2.43;by=203.0.113.60;proto=http;host=example.com");
        headers.set("X-Forwarded-For", "198.0.2.49");
        headers.set("X-Forwarded-By", "200.35.130.97");
        HttpCarbonMessage outboundRequestMsg = new HttpCarbonMessage(
                new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "", headers));
        outboundRequestMsg.setProperty(Constants.TO, "/hello");
        outboundRequestMsg.setProperty(Constants.ORIGIN_HOST, "www.example.com");
        outboundRequestMsg.setProperty(Constants.PROTOCOL, Constants.HTTP_SCHEME);
        ForwardedHeaderUpdater headerUpdater = new ForwardedHeaderUpdater(outboundRequestMsg, "10.100.1.92");
        headerUpdater.setForwardedHeader();
        Assert.assertEquals(outboundRequestMsg.getHeader("Forwarded"),
                "for=192.0.2.43, for=203.0.113.60; by=10.100.1.92; host=example.com; proto=http");
        Assert.assertEquals(outboundRequestMsg.getHeader("X-Forwarded-For"), "198.0.2.49");
        Assert.assertEquals(outboundRequestMsg.getHeader("X-Forwarded-By"), "200.35.130.97");
    }

    @Test(description = "Test transforming headers to forwarded header")
    public void testTransformAndSetForwardedHeaderWithXHeader() {
        HttpHeaders headers = new DefaultHttpHeaders();
        headers.set("X-Forwarded-For", "198.0.2.49");
        headers.set("X-Forwarded-Host", "www.abc.com");
        headers.set("X-Forwarded-Proto", "http");
        HttpCarbonMessage outboundRequestMsg = new HttpCarbonMessage(
                new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "", headers));
        outboundRequestMsg.setProperty(Constants.TO, "/hello");
        outboundRequestMsg.setProperty(Constants.ORIGIN_HOST, "www.example.com");
        outboundRequestMsg.setProperty(Constants.PROTOCOL, Constants.HTTPS_SCHEME);
        ForwardedHeaderUpdater updater = new ForwardedHeaderUpdater(outboundRequestMsg, "[2001:db8:cafe::17]:900");
        updater.transformAndSetForwardedHeader();
        Assert.assertEquals(outboundRequestMsg.getHeader("Forwarded"),
                "for=198.0.2.49; by=\"[2001:db8:cafe::17]:900\"; host=www.abc.com; proto=http");
        Assert.assertNull(outboundRequestMsg.getHeader("X-Forwarded-For"));
        Assert.assertNull(outboundRequestMsg.getHeader("X-Forwarded-Host"));
        Assert.assertNull(outboundRequestMsg.getHeader("X-Forwarded-Proto"));
    }

    @Test(description = "Test transforming headers to forwarded header when XFF and XFB available")
    public void testTransformAndSetForwardedHeaderWithXFFAndXFBHeaders() {
        HttpHeaders headers = new DefaultHttpHeaders();
        headers.set("X-Forwarded-For", "198.0.2.49");
        headers.set("X-Forwarded-By", "192.10.2.19");
        headers.set("X-Forwarded-Proto", "http");
        HttpCarbonMessage outboundRequestMsg = new HttpCarbonMessage(
                new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "", headers));
        outboundRequestMsg.setProperty(Constants.TO, "/hello");
        outboundRequestMsg.setProperty(Constants.ORIGIN_HOST, "www.example.com");
        outboundRequestMsg.setProperty(Constants.PROTOCOL, Constants.HTTPS_SCHEME);
        ForwardedHeaderUpdater updater = new ForwardedHeaderUpdater(outboundRequestMsg, "[2001:db8:cafe::17]:900");
        updater.transformAndSetForwardedHeader();
        Assert.assertNull(outboundRequestMsg.getHeader("Forwarded"));
        Assert.assertNotNull(outboundRequestMsg.getHeader("X-Forwarded-For"));
    }

    @Test(description = "Test transforming headers to forwarded header when XFF and forwarded available")
    public void testTransformAndSetForwardedHeaderWithPreviousXForwardForAndForwardedHeader() {
        HttpHeaders headers = new DefaultHttpHeaders();
        headers.set("Forwarded", "by=203.0.113.60;proto=http;host=example.com");
        headers.set("X-Forwarded-For", "192.0.2.44, 203.0.113.62");
        HttpCarbonMessage outboundRequestMsg = new HttpCarbonMessage(
                new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "", headers));
        outboundRequestMsg.setProperty(Constants.TO, "/hello");
        outboundRequestMsg.setProperty(Constants.ORIGIN_HOST, "www.example.com");
        ForwardedHeaderUpdater headerUpdater = new ForwardedHeaderUpdater(outboundRequestMsg, "10.100.1.92");
        headerUpdater.setForwardedHeader();
        Assert.assertEquals(outboundRequestMsg.getHeader("Forwarded"),
                "for=203.0.113.60; by=10.100.1.92; host=example.com; proto=http");
        Assert.assertEquals(outboundRequestMsg.getHeader("X-Forwarded-For"), "192.0.2.44, 203.0.113.62");
    }
}
