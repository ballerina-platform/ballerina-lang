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
import org.ballerinalang.test.util.JMSTestBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
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
    private Logger logger = LoggerFactory.getLogger(OrderProcessingServiceSampleTestCase.class);
    private List<String> messageList = new ArrayList<>();
    private String[] fileContent = {"Info--order1", "Info--order2", "Info--order3", "Info--order4", "Info--order5"};

    /**
     * Adding a message listener to listen to the queue, that the file content are being published
     *
     * @throws JMSException JMS Exception
     */
    @BeforeSuite
    public void setup() throws JMSException {
        ConnectionFactory connectionFactory = JMSTestBroker.getInstance().getConnectionFactory();
        QueueConnection queueConn = (QueueConnection) connectionFactory.createConnection();
        queueConn.start();
        QueueSession queueSession = queueConn.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue destination = queueSession.createQueue("order");
        MessageConsumer queueReceiver = queueSession.createConsumer(destination);
        queueReceiver.setMessageListener(new MessageListener() {
            @Override
            public void onMessage(Message message) {
               if (message instanceof TextMessage) {
                   try {
                       messageList.add(((TextMessage) message).getText());
                   } catch (JMSException e) {
                       logger.error("Error while getting the string from the JMS message.", e);
                   }
               }
            }
        });
    }

    /**
     * After the files has been successfully read, the relevant files need to be deleted from the relevant location.
     * This test case, tests this behaviour.
     *
     * @throws IOException IO Exception
     * @throws InterruptedException Interrupted Exception
     */
    @Test(description = "Test whether files have been deleted after reading the content")
    public void testFileDeletion() throws IOException, InterruptedException {
        /*
         * Wait till relevant contents are read and files are deleted, for slightly higher than polling time
         */
        Thread.sleep(12000);
        ClassLoader classLoader = getClass().getClassLoader();
        File folder = new File(classLoader.getResource(Constant.FTP_LOCATION + File.separator + "orders").getFile());
        File[] listOfFiles = folder.listFiles();
        Assert.assertEquals(listOfFiles.length, 0, "Files are not deleted after content is read from the files");
    }

    /**
     * In the relevant file service, relevant file content are being published to queue. This test cases tests
     * whether all the files are read and whether file contents matched.
     *
     * @throws IOException IO Exception
     * @throws InterruptedException Interrupted Exception
     */
    @Test(description = "Test whether the file content sent are matching with the actual file content.",
            dependsOnMethods = "testFileDeletion")
    public void testFileContent() throws IOException, InterruptedException {
        Assert.assertEquals(messageList.size(), fileContent.length, "All the files in the folder are not read. " +
                fileContent.length + " files found. But only " + messageList.size() + " files have been read "
                + "successfully");
        Assert.assertEqualsNoOrder(messageList.toArray(), fileContent, "The file content received in file service "
                + "does not match with actual file content");
    }
}
