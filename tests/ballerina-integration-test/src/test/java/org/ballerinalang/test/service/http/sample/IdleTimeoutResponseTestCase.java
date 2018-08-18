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
import org.ballerinalang.test.util.HttpClientRequest;
import org.ballerinalang.test.util.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Test idle timeout response for request timeout and server timeout.
 *
 * @since 0.975.1
 */
@Test(groups = "http-test")
public class IdleTimeoutResponseTestCase extends BaseTest {

    private static final Logger log = LoggerFactory.getLogger(IdleTimeoutResponseTestCase.class);
    /**
     * A larger client payload to be sent in chunks.
     */
    private static final String CLIENT_PAYLOAD = "POST /idle/timeout408 HTTP/1.1\r\n"
            + "Content-Type: text/xml\r\n"
            + "Transfer-Encoding: chunked\r\n"
            + "Connection: Keep-Alive\r\n\r\n"
            + "8ab\r\n"
            + "<?xml version='1.0' encoding='UTF-8'?>\n"
            +
            "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
            "xmlns:ejb3=\"http://ejb3.oms.dex.suva.ch\">\n"
            + "   <soapenv:Header/>\n"
            + "   <soapenv:Body>\n"
            + "      <ejb3:printReminderDocRequest>\n"
            + "         <JobId>test1</JobId>\n"
            + "         <JobId>testq</JobId>\n"
            + "         <JobId>testw</JobId>\n"
            + "         <JobId>testf</JobId>\n"
            + "         <JobId>tests</JobId>\n"
            + "         <JobId>testdec2d2s1d33</JobId>\n"
            + "         <JobId>testewc</JobId>\n"
            + "         <JobId>testt4</JobId>\n"
            + "         <JobId>testfr3</JobId>\n"
            + "         <JobId>testr3</JobId>\n"
            + "         <JobId>tev3vst</JobId>\n"
            + "         <JobId>terv3vst</JobId>\n"
            + "         <JobId>tevv3st</JobId>\n"
            + "         <JobId>te3vsvt</JobId>\n"
            + "         <JobId>tes3vt</JobId>\n"
            + "         <JobId>tevvcst</JobId>\n"
            + "         <JobId>tecdd st</JobId>\n"
            + "         <JobId>teef3st</JobId>\n"
            + "         <JobId>te3gst</JobId>\n"
            + "         <JobId>tet4st</JobId>\n"
            + "         <JobId>tesg5rt</JobId>\n"
            + "         <JobId>tvrest</JobId>\n"
            + "         <JobId>tervfst</JobId>\n"
            + "         <JobId>tvest</JobId>\n"
            + "         <JobId>tjyuest</JobId>\n"
            + "         <JobId>tejjst</JobId>\n"
            + "         <JobId>tfest</JobId>\n"
            + "         <JobId>teuyst</JobId>\n"
            + "         <JobId>tejyst</JobId>\n"
            + "         <JobId>tejgngst</JobId>\n"
            + "         <JobId>teyt6st</JobId>\n"
            + "         <JobId>tengst</JobId>\n"
            + "         <JobId>teh6st</JobId>\n"
            + "         <JobId>teh5st</JobId>\n"
            + "         <JobId>tesh4ht</JobId>\n"
            + "         <JobId>te44g4gg4g4st</JobId>\n"
            + "         <JobId>t5gest</JobId>\n"
            + "         <JobId>teu75st</JobId>\n"
            + "         <JobId>t4t45est</JobId>\n"
            + "         <JobId>th777778i6est</JobId>\n"
            + "         <JobId>tesu65u</JobId>\n"
            + "         <JobId>te5u5uu5st</JobId>\n"
            + "         <JobId>te5u56442ust</JobId>\n"
            + "         <JobId>te4223e23est</JobId>\n"
            + "         <JobId>tedd32ddst</JobId>\n"
            + "         <JobId>te3d3xcc2cst</JobId>\n"
            + "         <JobId>teecsxxt</JobId>\n"
            + "         <JobId>tx23d32eeeest</JobId>\n"
            + "         <JobId>te2e32e3d3st</JobId>\n"
            + "         <JobId>tedxwxxst</JobId>\n"
            + "         <JobId>te2d2xewxsst</JobId>\n"
            + "         <JobId>txcxexest</JobId>\n"
            + "         <JobId>teddst</JobId>\n"
            + "         <JobId>tewx2dst</JobId>\n"
            + "         <JobId>texsxqxxst</JobId>\n"
            + "         <JobId>texqxqxqxqst</JobId>\n"
            + "         <JobId>tews1s1s1st</JobId>\n"
            + "         <RemindId>test</RemindId>\n"
            + "      </ejb3:printReminderDocRequest>\n"
            + "   </soapenv:Body>\n"
            + "</soapenv:Envelope>\r\n"
            + "0\r\n"
            + "\r\n";
    private static final int BUFFER_SIZE = 1024;

    private final int servicePort = 9112;

    @Test(description = "Tests if 408 response is returned when the request times out. In this case a delay is " +
            "introduced between the first and second chunk.")
    public void test408Response() throws IOException, InterruptedException {
        SocketChannel clientSocket = connectToRemoteEndpoint();
        writeDelayedRequest(clientSocket);
        String expected = "HTTP/1.1 408 Request Timeout\r\n" +
                "content-length: 0\r\n" +
                "content-type: text/plain\r\n" +
                "connection: close";
        readAndAssertResponse(clientSocket, expected);
    }

    /**
     * Connects to the remote endpoint.
     *
     * @return the channel created from the connection.
     * @throws IOException if there's an error when connecting to remote endpoint.
     */
    private SocketChannel connectToRemoteEndpoint() throws IOException {
        InetSocketAddress remoteAddress = new InetSocketAddress("127.0.0.1", servicePort);

        SocketChannel clientSocket = SocketChannel.open();
        clientSocket.configureBlocking(true);
        clientSocket.socket().setReceiveBufferSize(BUFFER_SIZE);
        clientSocket.socket().setSendBufferSize(BUFFER_SIZE);
        clientSocket.connect(remoteAddress);

        if (!clientSocket.finishConnect()) {
            throw new Error("Cannot connect to server");
        }
        return clientSocket;
    }

    /**
     * Writes data as a chunk while keeping a delay between first and second chunks.
     *
     * @param socketChannel the channel to write to.
     * @throws IOException          if there's an error when writing.
     * @throws InterruptedException if the thread sleep is interrrupted.
     */
    private void writeDelayedRequest(SocketChannel socketChannel) throws IOException, InterruptedException {
        int numWritten = 0;
        int i = 0;
        ByteBuffer buf = ByteBuffer.allocate(BUFFER_SIZE);
        buf.clear();
        byte[] data = CLIENT_PAYLOAD.getBytes();
        while (i != data.length) {
            numWritten++;
            buf.clear();
            for (; data.length > i; i++) {
                if (buf.hasRemaining()) {
                    buf.put(data[i]);
                } else {
                    break;
                }
            }
            buf.flip();
            while (buf.hasRemaining()) {
                socketChannel.write(buf);
            }
            if (numWritten == 2) {
                Thread.sleep(2000);
            }
        }
    }

    /**
     * Reads the returned response and asserts it with the expected value.
     *
     * @param socketChannel the channel to read from.
     * @param expected      the expected response without the server header
     * @throws IOException if there's an error when reading the response or when closing the channel.
     */
    private void readAndAssertResponse(SocketChannel socketChannel, String expected) throws IOException {
        int count;
        ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
        StringBuilder inboundContent = new StringBuilder();

        count = socketChannel.read(buffer);
        Assert.assertTrue(count > 0);
        // Loop while data is available; channel is non-blocking
        while (count > 0) {
            // Make buffer readable
            buffer.flip();

            // Send the data; don't assume it goes all at once
            while (buffer.hasRemaining()) {
                inboundContent.append((char) buffer.get());
            }
            // Empty buffer
            buffer.clear();
            try {
                count = socketChannel.read(buffer);
            } catch (IOException e) {
                //Ignores this exception because the read cannot succeed if the connection is closed in the middle.
                log.warn("Cannot read more data when connection is closed", e);
            }
        }

        if (count < 0) {
            // Close channel on EOF, invalidates the key
            socketChannel.close();
        }

        String response = inboundContent.toString().trim();
        //Ignore the server header
        int newLineIndex = response.lastIndexOf("\r\n");
        Assert.assertEquals(response.substring(0, newLineIndex), expected.trim());
    }

    @Test(description = "Tests if 500 response is returned when the server times out. In this case a sleep is " +
            "introduced in the server.", enabled = false)
    public void test500Response() throws Exception {
        HttpResponse response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(servicePort,
                                                                                         "idle/timeout500"));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getResponseCode(), 500, "Response code mismatched");
    }
}
