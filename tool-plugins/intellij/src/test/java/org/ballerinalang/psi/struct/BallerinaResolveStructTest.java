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

package org.ballerinalang.psi.struct;

import org.ballerinalang.psi.BallerinaResolveTestBase;

import java.io.IOException;

/**
 * Test struct resolving.
 */
public class BallerinaResolveStructTest extends BallerinaResolveTestBase {

    private String struct = "public struct /*def*/testStruct {\n    string a;\n}";

    @Override
    protected String getTestDataPath() {
        return getTestDataPath("psi/resolve/struct");
    }

    //    public void testStructInSameFileInAction() {
    //        doFileTest();
    //    }
    //
    //    public void testStructInSameFileInConnector() {
    //        doFileTest();
    //    }

    public void testStructInSameFileInFunction() {
        doFileTest();
    }

    //    public void testStructInSameFileInService() {
    //        doFileTest();
    //    }

    //    public void testStructInDifferentFileInAction() throws IOException {
    //        doFileTest(struct);
    //    }
    //
    //    public void testStructInDifferentFileInConnector() throws IOException {
    //        doFileTest(struct);
    //    }

    public void testStructInDifferentFileInFunction() throws IOException {
        doFileTest(struct);
    }

    public void testStructInDifferentFileInService() throws IOException {
        doFileTest(struct);
    }

    //    public void testStructInDifferentPackageInAction() throws IOException {
    //        doFileTest(struct, "org/test/test.bal");
    //    }
    //
    //    public void testStructInDifferentPackageInConnector() throws IOException {
    //        doFileTest(struct, "org/test/test.bal");
    //    }

    public void testStructInDifferentPackageInFunction() throws IOException {
        doFileTest(struct, "org/test/test.bal");
    }

    public void testStructInDifferentPackageInService() throws IOException {
        doFileTest(struct, "org/test/test.bal");
    }
}
