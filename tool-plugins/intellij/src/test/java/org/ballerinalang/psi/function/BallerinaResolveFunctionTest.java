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

package org.ballerinalang.psi.function;

import org.ballerinalang.psi.BallerinaResolveTestBase;

import java.io.IOException;

public class BallerinaResolveFunctionTest extends BallerinaResolveTestBase {

    private String function = "public function /*def*/fun ()(string) {\n    return \"\";\n}";

    @Override
    protected String getTestDataPath() {
        return getTestDataPath("psi/resolve/function");
    }

    public void testFunctionInSameFileInvokeInAction() {
        doFileTest();
    }

    public void testFunctionInSameFileInvokeInConnector() {
        doFileTest();
    }

    public void testFunctionInSameFileInvokeInFunction() {
        doFileTest();
    }

    public void testFunctionInSameFileInvokeInService() {
        doFileTest();
    }

    public void testFunctionInDifferentFileInvokeInAction() throws IOException {
        doFileTest(function);
    }

    public void testFunctionInDifferentFileInvokeInConnector() throws IOException {
        doFileTest(function);
    }

    public void testFunctionInDifferentFileInvokeInFunction() throws IOException {
        doFileTest(function);
    }

    public void testFunctionInDifferentFileInvokeInService() throws IOException {
        doFileTest(function);
    }

    public void testFunctionInDifferentPackageInvokeInAction() throws IOException {
        doFileTest(function, "org/test/test.bal");
    }

    public void testFunctionInDifferentPackageInvokeInConnector() throws IOException {
        doFileTest(function, "org/test/test.bal");
    }

    public void testFunctionInDifferentPackageInvokeInFunction() throws IOException {
        doFileTest(function, "org/test/test.bal");
    }

    public void testFunctionInDifferentPackageInvokeInService() throws IOException {
        doFileTest(function, "org/test/test.bal");
    }
}
