/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.ballerinalang.folding;

import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;

public class BallerinaFoldingTest extends LightPlatformCodeInsightFixtureTestCase {

    @Override
    protected String getTestDataPath() {
        return "src/test/resources/testData/folding";
    }

    public void testConnector() {
        doTest();
    }

    public void testFunction() {
        doTest();
    }

    public void testService() {
        doTest();
    }

    public void testStruct() {
        doTest();
    }

    public void testImport() {
        doTest();
    }

    private void doTest() {
        myFixture.testFolding(getTestDataPath() + "/" + getTestName(false) + ".bal");
    }
}
