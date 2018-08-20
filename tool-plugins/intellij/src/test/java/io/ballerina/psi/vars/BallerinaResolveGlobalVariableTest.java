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

package io.ballerina.psi.vars;

import io.ballerina.psi.BallerinaResolveTestBase;

import java.io.IOException;

/**
 * Test global variable resolving.
 */
public class BallerinaResolveGlobalVariableTest extends BallerinaResolveTestBase {

    private String globalVariable = "public int /*def*/a;";

    @Override
    protected String getTestDataPath() {
        return getTestDataPath("psi/resolve/vars/global");
    }

    public void testGlobalVariableInSameFileInFunction() {
        doFileTest();
    }

    public void testGlobalVariableInSameFileInService() {
        doFileTest();
    }

    public void testGlobalVariableInSameFileInObject() {
        doFileTest();
    }

    public void testGlobalVariableInSameFileInRecord() {
        doFileTest();
    }

    public void testGlobalVariableInDifferentFileInFunction() throws IOException {
        doFileTest(globalVariable);
    }

    public void testGlobalVariableInDifferentFileInService() throws IOException {
        doFileTest(globalVariable);
    }

    public void testGlobalVariableInDifferentFileInObject() throws IOException {
        doFileTest(globalVariable);
    }

    public void testGlobalVariableInDifferentFileInRecord() throws IOException {
        doFileTest(globalVariable);
    }

    //TODO: Uncomment after fixing package resolving issue
    //    public void testGlobalVariableInDifferentPackageInFunction() throws IOException {
    //        doFileTest(globalVariable, "org/test/test.bal");
    //    }
    //
    //    public void testGlobalVariableInDifferentPackageInService() throws IOException {
    //        doFileTest(globalVariable, "org/test/test.bal");
    //    }
    //
    //    public void testGlobalVariableInDifferentPackageInObject() throws IOException {
    //        doFileTest(globalVariable, "org/test/test.bal");
    //    }
    //
    //    public void testGlobalVariableInDifferentPackageInRecord() throws IOException {
    //        doFileTest(globalVariable, "org/test/test.bal");
    //    }
}
