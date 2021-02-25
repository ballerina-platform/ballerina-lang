/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.testerina.test.utils;

import org.ballerinalang.test.context.BallerinaTestException;

/**
 * Util class for test assertions.
 *
 * @since 2.0.0
 */
public class AssertionUtils {

    public static void assertForTestFailures(String programOutput, String errMessage) throws BallerinaTestException {
        if (programOutput.contains("error: there are test failures")) {
            throw new BallerinaTestException("Test failed due to " + errMessage + " in test framework");
        } else if (programOutput.contains("error: compilation contains errors")) {
            throw new BallerinaTestException("Test failed due to a compilation error with following output\n" +
                    programOutput);
        }
    }
}
