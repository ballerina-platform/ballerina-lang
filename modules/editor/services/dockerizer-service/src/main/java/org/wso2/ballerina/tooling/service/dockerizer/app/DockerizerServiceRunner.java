/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.wso2.ballerina.tooling.service.dockerizer.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerina.containers.docker.BallerinaDockerClient;
import org.wso2.ballerina.containers.docker.impl.DefaultBallerinaDockerClient;
import org.wso2.ballerina.tooling.service.dockerizer.Constants;
import org.wso2.ballerina.tooling.service.dockerizer.rest.DockerizerService;
import org.wso2.msf4j.MicroservicesRunner;

/**
 * A service that will create Docker images for a given set of Ballerina
 * services.
 */
public class DockerizerServiceRunner {
    private static final Logger logger = LoggerFactory.getLogger(DockerizerServiceRunner.class);

    public static void main(String[] args) {
        String balHome = System.getProperty(Constants.SYS_BAL_HOME);
        if (balHome == null) {
            balHome = System.getenv(Constants.SYS_BAL_HOME);
        }
        if (balHome == null) {
            logger.error("BALLERINA_HOME is not set. Please set ballerina.home system variable.");
            return;
        }

        BallerinaDockerClient dockerClient = new DefaultBallerinaDockerClient();
        new MicroservicesRunner(
                Integer.getInteger(Constants.SYS_DOCKERIZER_PORT, Constants.DEFAULT_DOCKERIZER_PORT))
                .deploy(new DockerizerService(dockerClient))
                .start();
    }
}
