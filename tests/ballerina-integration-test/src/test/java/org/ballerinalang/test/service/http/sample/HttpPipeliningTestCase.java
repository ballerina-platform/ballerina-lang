/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.test.service.http.sample;

import org.ballerinalang.test.BaseTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import static org.ballerinalang.test.util.TestUtils.connectToRemoteEndpoint;

/**
 * Test case for HTTP pipelining.
 *
 * @since 0.981.2
 */
@Test(groups = "http-test")
public class HttpPipeliningTestCase extends BaseTest {
    private static final Logger log = LoggerFactory.getLogger(HttpPipeliningTestCase.class);

    private static final int BUFFER_SIZE = 221;
    private final int servicePort = 9220;

    @Test(description = "Test whether the response order matches the request order when HTTP pipelining is used")
    public void testResponseOrder() throws IOException, InterruptedException {
        SocketChannel clientSocket = connectToRemoteEndpoint("127.0.0.1", servicePort, BUFFER_SIZE);
        sendPipelinedRequests(clientSocket);
        String expected = "HTTP/1.1 200 OK\n" +
                "message-id: response-one\n" +
                "content-type: text/plain\n" +
                "content-length: 6\n" +
                "server: ballerina/0.981.2-SNAPSHOT\n" +
                "date: Mon, 27 Aug 2018 12:16:59 +0530\n" +
                "\n" +
                "Hello1HTTP/1.1 200 OK\n" +
                "message-id: response-two\n" +
                "content-type: text/plain\n" +
                "content-length: 6\n" +
                "server: ballerina/0.981.2-SNAPSHOT\n" +
                "date: Mon, 27 Aug 2018 12:16:59 +0530\n" +
                "\n" +
                "Hello2HTTP/1.1 200 OK\n" +
                "message-id: response-three\n" +
                "content-type: text/plain\n" +
                "content-length: 6\n" +
                "server: ballerina/0.981.2-SNAPSHOT\n" +
                "date: Mon, 27 Aug 2018 12:16:59 +0530";
        readAndAssertResponse(clientSocket, expected);
    }

    private void sendPipelinedRequests(SocketChannel socketChannel) throws IOException, InterruptedException {

        String request1 = "GET /pipeliningTest/responseOrder HTTP/1.1\r\n" +
                "message-id: request-one\r\n" +
                "Connection: Keep-Alive\r\n\r\n";

        String request2 = "GET /pipeliningTest/responseOrder HTTP/1.1\r\n" +
                "message-id: request-two\r\n" +
                "Connection: Keep-Alive\r\n\r\n";

        String request3 = "GET /pipeliningTest/responseOrder HTTP/1.1\r\n" +
                "message-id: request-three\r\n" +
                "Connection: Keep-Alive\r\n\r\n";

        String[] requests = new String[]{request1, request2, request3};
        for (String request : requests) {
            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
            buffer.put(request.getBytes());
            buffer.flip();
            socketChannel.write(buffer);
            buffer.clear();
        }
        log.info("Finished writing requests");
    }

    /**
     * Reads the returned response and asserts it with the expected value.
     *
     * @param socketChannel the channel to read from.
     * @param expected      the expected response without the server header
     * @throws IOException if there's an error when reading the response or when closing the channel.
     */
    private void readAndAssertResponse(SocketChannel socketChannel, String expected) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
        StringBuilder inboundContent = new StringBuilder();
        while (socketChannel.read(buffer) > 0) {
            log.info("Reading response...");
            buffer.flip();
            while (buffer.hasRemaining()) {
                inboundContent.append((char) buffer.get());
            }
            buffer.clear();
        }
        socketChannel.close();
        String response = inboundContent.toString().trim();
        log.info(response);
    }
}
