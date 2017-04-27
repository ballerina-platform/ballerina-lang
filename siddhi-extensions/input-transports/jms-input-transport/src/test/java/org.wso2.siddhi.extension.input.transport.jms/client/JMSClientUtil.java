/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.siddhi.extension.input.transport.jms.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JMSClientUtil {
    private static Log log = LogFactory.getLog(JMSClientUtil.class);

    /**
     * Messages will be read from the given filepath and stored in the array list (messagesList)
     *
     * @param filePath Text file to be read
     */
    public static List<String> readFile(String filePath) {
        BufferedReader bufferedReader = null;
        StringBuffer message = new StringBuffer("");
        final String asterixLine = "*****";
        List<String> messagesList = new ArrayList<String>();
        try {
            String line;
            bufferedReader = new BufferedReader(new FileReader(filePath));
            while ((line = bufferedReader.readLine()) != null) {
                if ((line.equals(asterixLine.trim()) && !"".equals(message.toString().trim()))) {
                    messagesList.add(message.toString());
                    message = new StringBuffer("");
                } else {
                    message = message.append(String.format("\n%s", line));
                }
            }
            if (!"".equals(message.toString().trim())) {
                messagesList.add(message.toString());
            }
        } catch (FileNotFoundException e) {
            log.error("Error in reading file " + filePath, e);
        } catch (IOException e) {
            log.error("Error in reading file " + filePath, e);
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (IOException e) {
                log.error("Error occurred when closing the file : " + e.getMessage(), e);
            }
        }
        return messagesList;
    }

    /**
     * Each message will be divided into groups and create the map message
     *
     * @param producer     Used for sending messages to a destination
     * @param session      Used to produce the messages to be sent
     * @param messagesList List of messages to be sent
     *                     individual message event data should be in
     *                     "attributeName(attributeType):attributeValue" format
     */
    public static void publishMapMessage(MessageProducer producer, Session session, List<String> messagesList)
            throws IOException, JMSException {
        String regexPattern = "(.*)\\((.*)\\):(.*)";
        Pattern pattern = Pattern.compile(regexPattern);
        for (String message : messagesList) {
            MapMessage mapMessage = session.createMapMessage();
            for (String line : message.split("\\n")) {
                if (line != null && !line.equalsIgnoreCase("")) {
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.find()) {
                        mapMessage.setObject(matcher.group(1), parseAttributeValue(matcher.group(2), matcher.group(3)));
                    }
                }
            }
            producer.send(mapMessage);
        }
    }

    /**
     * Each message will be divided into groups and create the map message
     *
     * @param producer     Used for sending messages to a destination
     * @param session      Used to produce the messages to be sent
     * @param messagesList List of messages to be sent
     */
    public static void publishTextMessage(MessageProducer producer, Session session, List<String> messagesList)
            throws JMSException {
        for (String message : messagesList) {
            TextMessage jmsMessage = session.createTextMessage();
            jmsMessage.setText(message);
            producer.send(jmsMessage);
        }
    }

    private static Object parseAttributeValue(String type, String value) {
        switch (type) {
            case "bool":
                return Boolean.parseBoolean(value);
            case "int":
                return Integer.parseInt(value);
            case "long":
                return Long.parseLong(value);
            case "float":
                return Float.parseFloat(value);
            case "double":
                return Double.parseDouble(value);
        }
        return value;
    }

}
