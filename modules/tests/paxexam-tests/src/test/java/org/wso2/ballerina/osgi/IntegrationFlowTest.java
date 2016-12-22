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

package org.wso2.ballerina.osgi;

import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.testng.listener.PaxExam;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.container.CarbonContainerFactory;
import org.wso2.carbon.kernel.utils.CarbonServerInfo;
import org.wso2.carbon.messaging.CarbonMessageProcessor;

import javax.inject.Inject;

import static org.ops4j.pax.exam.CoreOptions.maven;
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

    @Inject
    private BundleContext bundleContext;

    @Inject
    private CarbonServerInfo carbonServerInfo;

    @Inject
    private CarbonMessageProcessor carbonMessageProcessor;


    @Configuration
    public Option[] createConfiguration() {
        return new Option[] {
                copyDropinsBundle(maven().artifactId("mockito-all").groupId("org.mockito").versionAsInProject())
        };
    }

    @Test(description = "Test the scenario of integration flow which includes a respond mediator after a log mediator")
    public void testIntegrationFlow() throws Exception {
        log.info("************   carbonMessageProcessor.getId()  = " + carbonMessageProcessor.getId());
    }
}
