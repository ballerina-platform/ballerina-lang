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
package org.wso2.ballerina.test.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This represents the Server context when Servers are started using a Docker backend.
 */
public class DockerServerInstance implements Server {
    private static final Logger logger = LoggerFactory.getLogger(DockerServerInstance.class);

    @Override
    public void start() throws Exception {
        logger.info("The Ballerina Docker container should be already started. Not executing start().");
    }

    @Override
    public void stop() throws Exception {
        logger.info("The Ballerina Docker container will be stopped in the tear down. Not executing stop().");
    }

    @Override
    public void restart() throws Exception {
        // TODO: May be restart container
    }

    @Override
    public boolean isRunning() {
        return true;
    }

    @Override
    public String getServerHttpUrl() {
        String ballerinaPort = System.getProperty("ballerina.port");
        return "http://localhost:" + ballerinaPort;
    }
}
