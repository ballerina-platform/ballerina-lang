/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.osgi;

import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.testng.listener.PaxExam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.container.CarbonContainerFactory;
import org.wso2.carbon.kernel.utils.CarbonServerInfo;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.CarbonMessageProcessor;
import org.wso2.carbon.messaging.Constants;
import org.wso2.carbon.messaging.Headers;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Stack;
import javax.inject.Inject;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ballerinalang.natives.connectors.http.Constants.HTTP_METHOD;
import static org.ballerinalang.natives.connectors.http.Constants.PROTOCOL;
import static org.ballerinalang.natives.connectors.http.Constants.PROTOCOL_HTTP;
import static org.ballerinalang.natives.connectors.http.Constants.SUB_PATH;
import static org.wso2.carbon.container.options.CarbonDistributionOption.copyDropinsBundle;
import static org.wso2.carbon.container.options.CarbonDistributionOption.copyFile;

@Listeners(PaxExam.class)
@ExamFactory(CarbonContainerFactory.class)
@ExamReactorStrategy(PerClass.class)
/**
 * Test class to test simple integration flow.
 * @Listeners,@ExamReactorStrategy : Mandatory class annotations for each and every test class in the
 * Pax-Exam test module.
 * @ExamFactory : Reactor strategy "PerClass" starts the test-distribution for each test class in the test suite.
 */
public class IntegrationFlowTest {

    private static final Logger log = LoggerFactory.getLogger(IntegrationFlowTest.class);

    public static final String SERVICE_CONTEXT = "SERVICE_CONTEXT";
    public static final String SERVICE_METHOD = "SERVICE_METHOD";
    public static final String VARIABLE_STACK = "VARIABLE_STACK";

    @Inject
    private CarbonServerInfo carbonServerInfo;

    @Inject
    private CarbonMessageProcessor carbonMessageProcessor;

    @Configuration
    public Option[] createConfiguration() {
        return new Option[] {
                copySampleOption(),
                copyDropinsBundle(maven().artifactId("mockito-all").groupId("org.mockito").versionAsInProject())
        };
    }

    @Test(description = "Test the scenario of integration flow which includes a respond mediator after a log mediator")
    public void testIntegrationFlow() throws Exception {
        log.info("Starting testIntegrationFlow ....");
        CarbonMessage carbonMessage = mock(CarbonMessage.class);
        CarbonCallback carbonCallback = mock(CarbonCallback.class);

        when(carbonMessage.getProperty(PROTOCOL)).thenReturn(PROTOCOL_HTTP);
        when(carbonMessage.getProperty(Constants.LISTENER_INTERFACE_ID)).thenReturn("default");
        when(carbonMessage.getProperty(Constants.TO)).thenReturn("/passthrough/simple");
        when(carbonMessage.getProperty(HTTP_METHOD)).thenReturn("GET");

        when(carbonMessage.getProperty(SERVICE_CONTEXT)).thenReturn("/passthrough");
        when(carbonMessage.getProperty(SUB_PATH)).thenReturn("/simple");
        when(carbonMessage.getProperty(SERVICE_METHOD)).thenReturn("GET");
        when(carbonMessage.getProperty(VARIABLE_STACK)).thenReturn(new Stack<Map<String, Object>>());
        when(carbonMessage.getHeaders()).thenReturn(new Headers());

        log.info("Invoke Simple flow");
        carbonMessageProcessor.receive(carbonMessage, carbonCallback);

        verify(carbonCallback, timeout(3000)).done(carbonMessage);
    }

    private Option copySampleOption() {
        String basedir = System.getProperty("basedir");
        if (basedir == null) {
            basedir = Paths.get(".").toString();
        }
        Path samplePath = Paths.get(basedir, "src", "test", "resources", "artifacts", "simpleflow.bal");
        return copyFile(samplePath, Paths.get("simpleflow.bal"));
    }
}
