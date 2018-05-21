/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.ballerinalang.test.balo;

import org.testng.ISuite;
import org.testng.ISuiteListener;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * Setup balo for the test cases.
 * 
 * @since 0.975.0
 */
public class BaloTestListener implements ISuiteListener {

    @Override
    public void onStart(ISuite suite) {
        try {
            BaloCreator.create(Paths.get("test-src", "balo", "test_project"), "foo");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onFinish(ISuite suite) {
    }
}
