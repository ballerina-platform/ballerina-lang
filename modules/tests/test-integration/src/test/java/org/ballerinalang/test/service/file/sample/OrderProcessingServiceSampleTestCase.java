/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.test.service.file.sample;

import org.ballerinalang.test.context.Constant;
import org.ballerinalang.test.server.JMSTestBroker;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;

/**
 * Testing the service sample located in
 * other_samples/fileServiceWithActiveMq/testFileService.bal
 */
public class OrderProcessingServiceSampleTestCase {
    private String[] fileContent = { "Info--order1", "Info--order2", "Info--order3", "Info--order4", "Info--order5" };
    private MessageConsumer queueReceiver;

    /**
     * Adding a message listener to listen to the queue, that the file content are being published
     *
     * @throws JMSException JMS Exception
     */
    @BeforeClass
    public void setup() throws JMSException, IOException {
        ConnectionFactory connectionFactory = JMSTestBroker.getInstance().getConnectionFactory();
        QueueConnection queueConn = (QueueConnection) connectionFactory.createConnection();
        queueConn.start();
        QueueSession queueSession = queueConn.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue destinationQueue = queueSession.createQueue("order");
        queueReceiver = queueSession.createConsumer(destinationQueue);
    }

    /**
     * In the relevant file service, relevant file content are being published to queue. This test cases tests
     * whether all the files are read and whether file contents matched and whether files are deleted after that.
     *
     * @throws IOException          IO Exception
     * @throws InterruptedException Interrupted Exception
     */
    @Test(description = "Test whether the file content sent are matching with the actual file content.")
    public void testFileContent() throws IOException, InterruptedException, JMSException {
        for (int index = 0; index < 5; index++) {
            Message message = queueReceiver.receive(1000);

            if (message instanceof TextMessage) {
                Assert.assertTrue(Arrays.asList(fileContent).contains(((TextMessage) message).getText()), "File "
                        + "content does not match with the message received.");
            }
        }
        ClassLoader classLoader = getClass().getClassLoader();
        File folder = new File(classLoader.getResource(Constant.FTP_LOCATION + File.separator + "orders").getFile());

        long currentTime = System.currentTimeMillis();
        long timeOutInterval = 10000;
        while (System.currentTimeMillis() - currentTime < timeOutInterval) {
            File[] listOfFiles = folder.listFiles();
            if (listOfFiles.length == 0) {
                break;
            }
        }
        File[] listOfFiles = folder.listFiles();
        Assert.assertEquals(listOfFiles.length, 0, "Files are not deleted after content is read from the files");
    }
}
