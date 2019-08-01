/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.test;

import org.ballerinalang.test.context.BalServer;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.Constant;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

/**
 * Parent test class for all integration test cases which required to be tested with jballerina distribution.
 * TODO : We can get rid of this class once we move all test cases to run on jballerina distribution.
 *
 * @since 0.995.0
 */
public class JBallerinaBaseTest {

    protected static BalServer balServer;
//    protected static BMainInstance balClient;
    
    @BeforeSuite(alwaysRun = true)
    public void initialize() throws BallerinaTestException {
        balServer = new BalServer(System.getProperty(Constant.SYSTEM_PROP_JBALLERINA_SERVER_ZIP));
//        balClient = new BMainInstance(balServer);
    }

    @AfterSuite(alwaysRun = true)
    public void destroy() throws BallerinaTestException {
        balServer.cleanup();
    }
}
