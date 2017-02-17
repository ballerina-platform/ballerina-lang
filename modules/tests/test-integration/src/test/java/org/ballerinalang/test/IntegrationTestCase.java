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
package org.ballerinalang.test;

import org.ballerinalang.test.context.ServerInstance;
import org.ballerinalang.test.listener.TestExecutionListener;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

/**
 * Parent test class of all integration test and this will provide basic functionality for integration test.
 */
public abstract class IntegrationTestCase {
    private ServerInstance serverInstance;

    @BeforeClass(alwaysRun = true)
    public void init() {
        // assigning the running server instance started by TestExecutionListener
        serverInstance = (ServerInstance) TestExecutionListener.getServerInstance();
    }

    @AfterClass(alwaysRun = true)
    public void destroy() {
        serverInstance = null;
    }

    public ServerInstance getServerInstance() {
        return serverInstance;
    }

    public String getServiceURLHttp(String servicePath) {
        return serverInstance.getServiceURLHttp(servicePath);
    }

}
