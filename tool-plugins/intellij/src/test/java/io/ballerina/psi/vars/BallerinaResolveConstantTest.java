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
 * Test constant resolving.
 */
public class BallerinaResolveConstantTest extends BallerinaResolveTestBase {

    private String constant = "@final int /*def*/a;";

    @Override
    protected String getTestDataPath() {
        return getTestDataPath("psi/resolve/vars/constant");
    }

    public void testConstantInSameFileInFunction() {
        doFileTest();
    }

    public void testConstantInSameFileInService() {
        doFileTest();
    }

    public void testConstantInSameFileInObject() {
        doFileTest();
    }

    public void testConstantInSameFileInRecord() {
        doFileTest();
    }

    public void testConstantInDifferentFileInFunction() throws IOException {
        doFileTest(constant);
    }

    public void testConstantInDifferentFileInService() throws IOException {
        doFileTest(constant);
    }

    public void testConstantInDifferentFileInObject() throws IOException {
        doFileTest(constant);
    }

    public void testConstantInDifferentFileInRecord() throws IOException {
        doFileTest(constant);
    }

    //TODO: Uncomment after fixing package resolving issue
    //    public void testConstantInDifferentPackageInFunction() throws IOException {
    //        doFileTest(constant, "org/test/test.bal");
    //    }
    //
    //    public void testConstantInDifferentPackageInService() throws IOException {
    //        doFileTest(constant, "org/test/test.bal");
    //    }
    //
    //    public void testConstantInDifferentPackageInObject() throws IOException {
    //        doFileTest(constant, "org/test/test.bal");
    //    }
    //
    //    public void testConstantInDifferentPackageInRecord() throws IOException {
    //        doFileTest(constant, "org/test/test.bal");
    //    }
}
