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

package org.ballerinalang.net.websub.hub;

import org.ballerinalang.BLangProgramRunner;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.BrokerUtils;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.ProgramFileReader;
import org.ballerinalang.util.program.BLangFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * The Ballerina WebSub Hub.
 *
 * @since 0.965.0
 */
public class Hub {
    private static final Logger logger = LoggerFactory.getLogger(Hub.class);

    private static Hub instance = new Hub();
    private String hubUrl;
    private volatile boolean started = false;
    private ProgramFile hubProgramFile;

    private List<HubSubscriber> subscribers = new ArrayList<>();

    public static Hub getInstance() {
        return instance;
    }

    private Hub() {
    }

    public String retrieveHubUrl() {
        return hubUrl;
    }

    /**
     * Method to add a subscription to the topic on MB.
     *
     * @param topic     the topic to which the subscription should be added
     * @param callback  the callback registered for the particular subscription
     */
    public void registerSubscription(String topic, String callback, BStruct subscriptionDetails) {
        if (!started) {
            logger.error("Hub Service not started: subscription failed.");
            return;
        }
        if (subscribers.contains(new HubSubscriber("", topic, callback, null))) {
            unregisterSubscription(topic, callback);
        }
        String queue = UUID.randomUUID().toString();

        //Temporary workaround - expected secret to be "" if not specified but got null
        if (BLangConnectorSPIUtil.toStruct(subscriptionDetails).getStringField("secret") == null) {
            subscriptionDetails.setStringField(2, "");
        }
        HubSubscriber subscriberToAdd = new HubSubscriber(queue, topic, callback, subscriptionDetails);
        BrokerUtils.addSubscription(topic, subscriberToAdd);
        subscribers.add(subscriberToAdd);
    }

    /**
     * Method to remove a subscription to the topic on MB.
     *
     * @param topic     the topic to which the subscription should was added
     * @param callback  the callback registered for the particular subscription
     */
    public void unregisterSubscription(String topic, String callback) {
        if (!started) {
            logger.error("Hub Service not started: unsubscription failed.");
            return;
        }
        HubSubscriber subscriberToUnregister = new HubSubscriber("", topic, callback, null);
        if (!subscribers.contains(subscriberToUnregister)) {
            return;
        } else {
            for (HubSubscriber subscriber:subscribers) {
                if (subscriber.equals(subscriberToUnregister)) {
                    subscriberToUnregister = subscriber;
                    break;
                }
            }
        }
        BrokerUtils.removeSubscription(subscriberToUnregister);
        subscribers.remove(subscriberToUnregister);
    }

    /**
     * Method to publish to a topic on MB.
     *
     * @param topic             the topic to which the update should happen
     * @param stringPayload     the update payload
     */
    public void publish(String topic, String stringPayload) {
        if (!started) {
            logger.error("Hub Service not started: publish failed.");
            return;
        }
        byte[] payload = stringPayload.getBytes(StandardCharsets.UTF_8);
        BrokerUtils.publish(topic, payload);
    }

    public boolean isStarted() {
        return started;
    }

    /**
     * Method to compile and start up the default Ballerina WebSub Hub.
     */
    public String startUpHubService() {
        synchronized (this) {
            if (!isStarted()) {
                URI balxPath = URI.create(String.valueOf(Hub.class.getClassLoader().getResource
                        ("net.websub.hub.balx")));
                ProgramFile hubProgramFile = readExecutableProgram(Paths.get(balxPath));
                PackageInfo hubPackageInfo = hubProgramFile.getPackageInfo("net.websub.hub");
                if (hubPackageInfo != null) {
                    hubPackageInfo.setProgramFile(hubProgramFile);
                    BLangProgramRunner.runService(hubProgramFile);
                    BValue[] args = {};
                    BLangFunctions.invokeCallable(hubPackageInfo.getFunctionInfo("setupOnStartup"), args);
                    String webSubHubUrl = (BLangFunctions.invokeCallable(
                            hubPackageInfo.getFunctionInfo("getHubUrl"), args)[0])
                            .stringValue();
                    logger.info("Default Ballerina WebSub Hub started up at " + webSubHubUrl);
                    PrintStream console = System.out;
                    console.println("ballerina: Default Ballerina WebSub Hub started up at " + webSubHubUrl);
                    hubUrl = webSubHubUrl;
                    setHubProgramFile(hubProgramFile);
                    started = true;
                }
            }
        }
        return hubUrl;
    }

    /**
     * Method to set the program file for the WebSub Hub service.
     *
     * @param programFile the program file result to set
     */
    private void setHubProgramFile(ProgramFile programFile) {
        hubProgramFile = programFile;
    }

    /**
     * Method to retrieve the program file for the WebSub Hub service.
     *
     * @return the program file returned when compiling the package at Hub start up
     */
    ProgramFile getHubProgramFile() {
        return hubProgramFile;
    }

    /**
     * Get program file after reading the executable program i.e. balo file.
     *
     * @param baloFilePath path of the balo file
     * @return program file
     */
    private static ProgramFile readExecutableProgram(Path baloFilePath) {
        ByteArrayInputStream byteIS = null;
        try {
            byte[] byteArray = Files.readAllBytes(baloFilePath);
            ProgramFileReader reader = new ProgramFileReader();
            byteIS = new ByteArrayInputStream(byteArray);
            return reader.readProgram(byteIS);
        } catch (IOException ignore) {
        } finally {
            if (byteIS != null) {
                try {
                    byteIS.close();
                } catch (IOException ignore) {
                }
            }
        }
        return null;
    }

}
