/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.transport.http.netty.unitfunction;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;
import org.wso2.transport.http.netty.util.TestUtil;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * A unit test class for BlockingEntityCollector class functions.
 */
public class BlockingEntityCollectorTestCase {

    @Test(description = "Test count message length till a give limit")
    public void testCountMessageLengthTill() {
        HttpCarbonMessage msg = new HttpCarbonMessage(
                new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, ""));
        ByteBuffer byteBuffer = ByteBuffer.wrap(TestUtil.largeEntity.getBytes(Charset.forName("UTF-8")));
        msg.addHttpContent(new DefaultLastHttpContent(Unpooled.wrappedBuffer(byteBuffer)));

        long count = 0;
        String exceptionMessage = "";
        try {
            count = msg.getBlockingEntityCollector().countMessageLengthTill(15);
        } catch (IllegalStateException e) {
            exceptionMessage = e.getMessage();
        }
        // TODO: Verify logic as countMessageLengthTill returns complete length regardless of the given max length
        Assert.assertEquals(count, 9342);
        Assert.assertEquals(exceptionMessage, "");
    }

    @Test(description = "Test countMessageLengthTill for poll timeout")
    public void testCountMessageLengthTillPollTimeout() {
        HttpCarbonMessage msg = new HttpCarbonMessage(
                new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, ""), 10000, null);
        ByteBuffer byteBuffer = ByteBuffer.wrap("".getBytes(Charset.forName("UTF-8")));
        msg.addHttpContent(new DefaultHttpContent(Unpooled.wrappedBuffer(byteBuffer)));

        long count = 0;
        String exceptionMessage = "";
        try {
            count = msg.getBlockingEntityCollector().countMessageLengthTill(3);
        } catch (IllegalStateException e) {
            exceptionMessage = e.getMessage();
        }
        Assert.assertEquals(count, 0);
        Assert.assertEquals(exceptionMessage, "poll timeout expired");
    }
}
