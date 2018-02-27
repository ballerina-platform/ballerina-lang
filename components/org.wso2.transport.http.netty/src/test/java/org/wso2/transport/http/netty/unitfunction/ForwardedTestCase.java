package org.wso2.transport.http.netty.unitfunction;

import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.common.Constants;
import org.wso2.transport.http.netty.common.Util;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;

/**
 * A unit test class for setForwardedHeader and TransformAndSetForwardedHeader functions.
 */
public class ForwardedTestCase {

    @Test(description = "Test setting headers to Http request with duplicate header keys")
    public void testSetForwardedHeaderAsFirstInterface() {
        HttpHeaders headers = new DefaultHttpHeaders();
        headers.set("Host", "https://example.com");
        HTTPCarbonMessage outboundRequestMsg = new HTTPCarbonMessage(new DefaultHttpRequest(HttpVersion.HTTP_1_1,
                HttpMethod.POST, "", headers));
        outboundRequestMsg.setProperty(Constants.TO, "/hello");
        Util.setForwardedHeader(outboundRequestMsg, "10.100.1.92");
        Assert.assertEquals(outboundRequestMsg.getHeader("Forwarded"),
                "by=10.100.1.92; host=https://example.com; proto=https");
    }

    @Test(description = "Test setting headers to Http request with duplicate header keys")
    public void testSetForwardedHeaderAsFirstInterfaceIPv6() {
        HttpHeaders headers = new DefaultHttpHeaders();
        headers.set("Host", "https://example.com");
        HTTPCarbonMessage outboundRequestMsg = new HTTPCarbonMessage(new DefaultHttpRequest(HttpVersion.HTTP_1_1,
                HttpMethod.POST, "", headers));
        outboundRequestMsg.setProperty(Constants.TO, "/hello");
        outboundRequestMsg.setProperty(Constants.PROTOCOL, Constants.HTTPS_SCHEME);
        Util.setForwardedHeader(outboundRequestMsg, "2001:db8:cafe::17");
        Assert.assertEquals(outboundRequestMsg.getHeader("Forwarded"),
                "by=\"[2001:db8:cafe::17]\"; host=https://example.com; proto=https");
    }

    @Test(description = "Test setting headers to Http request with duplicate header keys")
    public void testSetForwardedHeaderAsFirstInterfaceIPv6WithPort() {
        HttpHeaders headers = new DefaultHttpHeaders();
        headers.set("Host", "https://example.com");
        HTTPCarbonMessage outboundRequestMsg = new HTTPCarbonMessage(new DefaultHttpRequest(HttpVersion.HTTP_1_1,
                HttpMethod.POST, "", headers));
        outboundRequestMsg.setProperty(Constants.TO, "/hello");
        Util.setForwardedHeader(outboundRequestMsg, "[2001:db8:cafe::17]:900");
        Assert.assertEquals(outboundRequestMsg.getHeader("Forwarded"),
                "by=\"[2001:db8:cafe::17]:900\"; host=https://example.com; proto=http");
    }

    @Test(description = "Test setting headers to Http request with duplicate header keys")
    public void testSetForwardedHeaderWithPreviousForwardHeader() {
        HttpHeaders headers = new DefaultHttpHeaders();
        headers.set("Host", "http://abc.com");
        headers.set("Forwarded", "for=192.0.2.43, for=198.51.100.17;by=203.0.113.60;proto=http;host=example.com");
        HTTPCarbonMessage outboundRequestMsg = new HTTPCarbonMessage(new DefaultHttpRequest(HttpVersion.HTTP_1_1,
                HttpMethod.POST, "", headers));
        outboundRequestMsg.setProperty(Constants.TO, "/hello");
        Util.setForwardedHeader(outboundRequestMsg, "10.100.1.92");
        Assert.assertEquals(outboundRequestMsg.getHeader("Forwarded"),
                "for=192.0.2.43, for=198.51.100.17, for=203.0.113.60; by=10.100.1.92; host=example.com; proto=http");
    }

    @Test(description = "Test setting headers to Http request with duplicate header keys")
    public void testSetForwardedHeaderAsIntermediateProxyWithOnlyFor() {
        HttpHeaders headers = new DefaultHttpHeaders();
        headers.set("Host", "http://abc.com");
        headers.set("Forwarded", "for=192.0.2.43, for=198.51.100.17");
        HTTPCarbonMessage outboundRequestMsg = new HTTPCarbonMessage(new DefaultHttpRequest(HttpVersion.HTTP_1_1,
                HttpMethod.POST, "", headers));
        outboundRequestMsg.setProperty(Constants.TO, "/hello");
        Util.setForwardedHeader(outboundRequestMsg, "10.100.1.92");
        Assert.assertEquals(outboundRequestMsg.getHeader("Forwarded"),
                "for=192.0.2.43, for=198.51.100.17; by=10.100.1.92");
    }

    @Test(description = "Test setting headers to Http request with duplicate header keys")
    public void testSetForwardedHeaderAsIntermediateProxyWithBy() {
        HttpHeaders headers = new DefaultHttpHeaders();
        headers.set("Host", "http://abc.com");
        headers.set("Forwarded", "by=192.0.2.43, 129.9.0");
        HTTPCarbonMessage outboundRequestMsg = new HTTPCarbonMessage(new DefaultHttpRequest(HttpVersion.HTTP_1_1,
                HttpMethod.POST, "", headers));
        outboundRequestMsg.setProperty(Constants.TO, "/hello");
        Util.setForwardedHeader(outboundRequestMsg, "10.100.1.92");
        Assert.assertEquals(outboundRequestMsg.getHeader("Forwarded"),
                "for=192.0.2.43, for=198.51.100.17; by=10.100.1.92");
    }

    @Test(description = "Test setting headers to Http request with duplicate header keys")
    public void testTransformAndSetForwardedHeaderWithPreviousForwardHeader() {
        HttpHeaders headers = new DefaultHttpHeaders();
        headers.set("Forwarded", "for=192.0.2.43;by=203.0.113.60;proto=http;host=example.com");
        HTTPCarbonMessage outboundRequestMsg = new HTTPCarbonMessage(new DefaultHttpRequest(HttpVersion.HTTP_1_1,
                HttpMethod.POST, "", headers));
        outboundRequestMsg.setProperty(Constants.TO, "/hello");
        Util.transformAndSetForwardedHeader(outboundRequestMsg, "10.100.1.92");
        Assert.assertEquals(outboundRequestMsg.getHeader("Forwarded"),
                "for=192.0.2.43, for=203.0.113.60; by=10.100.1.92; host=example.com; proto=http");
    }

    @Test(description = "Test setting headers to Http request with duplicate header keys")
    public void testTransformAndSetForwardedHeaderAsFirstInterface() {
        HttpHeaders headers = new DefaultHttpHeaders();
        headers.set("Host", "http://bbc.com");
        HTTPCarbonMessage outboundRequestMsg = new HTTPCarbonMessage(new DefaultHttpRequest(HttpVersion.HTTP_1_1,
                HttpMethod.POST, "", headers));
        outboundRequestMsg.setProperty(Constants.TO, "/hello");
        Util.transformAndSetForwardedHeader(outboundRequestMsg, "10.100.1.92");
        Assert.assertEquals(outboundRequestMsg.getHeader("Forwarded"),
                "by=10.100.1.92; host=http://bbc.com; proto=http");
    }

    @Test(description = "Test setting headers to Http request with duplicate header keys")
    public void testTransformAndSetForwardedHeaderWithPreviousXForwardForHeader() {
        HttpHeaders headers = new DefaultHttpHeaders();
        headers.set("X-Forwarded-For", "192.0.2.43, 203.0.113.62");
        HTTPCarbonMessage outboundRequestMsg = new HTTPCarbonMessage(new DefaultHttpRequest(HttpVersion.HTTP_1_1,
                HttpMethod.POST, "", headers));
        outboundRequestMsg.setProperty(Constants.TO, "/hello");
        Util.transformAndSetForwardedHeader(outboundRequestMsg, "10.100.1.92");
        Assert.assertEquals(outboundRequestMsg.getHeader("Forwarded"), "");
        Assert.assertEquals(outboundRequestMsg.getHeader("X-Forwarded-For"),
                "for=192.0.2.43, for=203.0.113.62; by=10.100.1.92");
    }

    @Test(description = "Test setting headers to Http request with duplicate header keys")
    public void testTransformAndSetForwardedHeaderWithPreviousXForwardByHeader() {
        HttpHeaders headers = new DefaultHttpHeaders();
        headers.set("X-Forwarded-By", "192.0.2.43");
        HTTPCarbonMessage outboundRequestMsg = new HTTPCarbonMessage(new DefaultHttpRequest(HttpVersion.HTTP_1_1,
                HttpMethod.POST, "", headers));
        outboundRequestMsg.setProperty(Constants.TO, "/hello");
        Util.transformAndSetForwardedHeader(outboundRequestMsg, "10.100.1.92");
        Assert.assertEquals(outboundRequestMsg.getHeader("Forwarded"), "");
        Assert.assertEquals(outboundRequestMsg.getHeader("X-Forwarded-For"),
                "for=192.0.2.43; by=10.100.1.92");
    }

    @Test(description = "Test setting headers to Http request with duplicate header keys")
    public void testTransformAndSetForwardedHeaderWithPreviousXForwardForAndForwardedHeader() {
        HttpHeaders headers = new DefaultHttpHeaders();
        headers.set("Forwarded", "for=192.0.2.43;by=203.0.113.60;proto=http;host=example.com");
        headers.set("X-Forwarded-For", "192.0.2.44, 203.0.113.62");
        HTTPCarbonMessage outboundRequestMsg = new HTTPCarbonMessage(new DefaultHttpRequest(HttpVersion.HTTP_1_1,
                HttpMethod.POST, "", headers));
        outboundRequestMsg.setProperty(Constants.TO, "/hello");
        Util.transformAndSetForwardedHeader(outboundRequestMsg, "10.100.1.92");
        Assert.assertEquals(outboundRequestMsg.getHeader("Forwarded"), "");
        Assert.assertEquals(outboundRequestMsg.getHeader("X-Forwarded-For"), "");
    }

    @Test(description = "Test setting headers to Http request with duplicate header keys")
    public void testTransformAndSetForwardedHeaderWithPreviousXForwardForAndForwardedHeaderWithoutFor() {
        HttpHeaders headers = new DefaultHttpHeaders();
        headers.set("Forwarded", "by=203.0.113.60;proto=http;host=example.com");
        headers.set("X-Forwarded-For", "192.0.2.44, 203.0.113.62");
        HTTPCarbonMessage outboundRequestMsg = new HTTPCarbonMessage(new DefaultHttpRequest(HttpVersion.HTTP_1_1,
                HttpMethod.POST, "", headers));
        outboundRequestMsg.setProperty(Constants.TO, "/hello");
        Util.transformAndSetForwardedHeader(outboundRequestMsg, "10.100.1.92");
        Assert.assertEquals(outboundRequestMsg.getHeader("Forwarded"), "");
        Assert.assertEquals(outboundRequestMsg.getHeader("X-Forwarded-For"), "");
    }

    //TODO check this
    @Test(description = "Test setting headers to Http request with duplicate header keys")
    public void testTransformAndSetForwardedHeaderWithPreviousXForwardByAndForwardedHeader() {
        HttpHeaders headers = new DefaultHttpHeaders();
        headers.set("Forwarded", "for=192.0.2.43;by=203.0.113.60;proto=http;host=example.com");
        headers.set("X-Forwarded-By", "192.0.10.15");
        HTTPCarbonMessage outboundRequestMsg = new HTTPCarbonMessage(new DefaultHttpRequest(HttpVersion.HTTP_1_1,
                HttpMethod.POST, "", headers));
        outboundRequestMsg.setProperty(Constants.TO, "/hello");
        Util.transformAndSetForwardedHeader(outboundRequestMsg, "10.100.1.92");
        Assert.assertEquals(outboundRequestMsg.getHeader("Forwarded"), "");
        Assert.assertEquals(outboundRequestMsg.getHeader("X-Forwarded-For"), "");
    }


}
