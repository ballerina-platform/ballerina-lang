/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.launcher.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ISuite;
import org.testng.ISuiteListener;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.ballerinalang.util.BLangConstants.BALLERINA_HOME;

/**
 * A listener for loading ballerina standard libs prior to running the tests.
 *
 * @since 0.975.0
 */
public class BallerinaTestListener implements ISuiteListener {

    private static Logger log = LoggerFactory.getLogger(BallerinaTestListener.class);
    private Path stdlibs = Paths.get("target", "lib");
    private String ballerinaHome;

    @Override
    public void onStart(ISuite iSuite) {
        setBallerinaHome();
    }

    @Override
    public void onFinish(ISuite iSuite) {
        resetBallerinaHome();
    }

    private void setBallerinaHome() {
        ballerinaHome = System.getProperty(BALLERINA_HOME);
        System.setProperty(BALLERINA_HOME, stdlibs.getParent().toAbsolutePath().toString());
        log.info(BALLERINA_HOME + " is set to: " + System.getProperty(BALLERINA_HOME));
    }

    private void resetBallerinaHome() {
        if (ballerinaHome != null) {
            System.setProperty(BALLERINA_HOME, ballerinaHome);
        } else {
            System.clearProperty(BALLERINA_HOME);
        }
    }
}
