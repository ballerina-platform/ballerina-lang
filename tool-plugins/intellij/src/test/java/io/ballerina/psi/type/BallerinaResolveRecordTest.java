/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.psi.type;

import io.ballerina.psi.BallerinaResolveTestBase;

import java.io.IOException;

/**
 * Test recordDefinition resolving.
 */
public class BallerinaResolveRecordTest extends BallerinaResolveTestBase {

    private String recordDefinition = "public type /*def*/testRecord record {\n    string a;\n}";

    @Override
    protected String getTestDataPath() {
        return getTestDataPath("psi/resolve/type");
    }

    public void testRecordInSameFileInFunction() {
        doFileTest();
    }

    public void testRecordInSameFileInService() {
        doFileTest();
    }

    public void testRecordInDifferentFileInFunction() throws IOException {
        doFileTest(recordDefinition);
    }

    public void testRecordInDifferentFileInService() throws IOException {
        doFileTest(recordDefinition);
    }

    //TODO: Uncomment after fixing package resolving issues
    //    public void testRecordInDifferentPackageInFunction() throws IOException {
    //        doFileTest(recordDefinition, "org/test/test.bal");
    //    }
    //
    //    public void testRecordInDifferentPackageInService() throws IOException {
    //        doFileTest(recordDefinition, "org/test/test.bal");
    //}
}
