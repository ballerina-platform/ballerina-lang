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

package io.ballerina.psi.field;

import io.ballerina.psi.BallerinaResolveTestBase;

import java.io.IOException;

/**
 * Test resolving record fields.
 */
public class BallerinaResolveRecordFieldTest extends BallerinaResolveTestBase {

    private String record1 = "public type testRecord record {\n    string /*def*/s;\n}";
    private String record2 =
            "public type Name record {\n    string firstName;\n}\npublic type Person record " + "{\n    Name "
                    + "/*def*/name;\n}";

    @Override
    protected String getTestDataPath() {
        return getTestDataPath("psi/resolve/field/record");
    }

    public void testSingleLevelFieldAccessInFunction1() {
        doFileTest();
    }

    public void testSingleLevelFieldAccessInFunction2() {
        doFileTest();
    }

    public void testSingleLevelFieldAccessInService1() {
        doFileTest();
    }

    public void testSingleLevelFieldAccessInService2() {
        doFileTest();
    }

    public void testMultiLevelFieldAccessInFunction1() {
        doFileTest();
    }

    public void testMultiLevelFieldAccessInFunction2() {
        doFileTest();
    }

    public void testMultiLevelFieldAccessInService1() {
        doFileTest();
    }

    public void testMultiLevelFieldAccessInService2() {
        doFileTest();
    }

    public void testSingleLevelFieldInDifferentFileAccessInFunction1() throws IOException {
        doFileTest(record1);
    }

    public void testSingleLevelFieldInDifferentFileAccessInFunction2() throws IOException {
        doFileTest(record1);
    }

    public void testSingleLevelFieldInDifferentFileAccessInService1() throws IOException {
        doFileTest(record1);
    }

    public void testSingleLevelFieldInDifferentFileAccessInService2() throws IOException {
        doFileTest(record1);
    }

    public void testMultiLevelFieldInDifferentFileAccessInFunction1() throws IOException {
        doFileTest(record2);
    }

    public void testMultiLevelFieldInDifferentFileAccessInFunction2() throws IOException {
        doFileTest(record2);
    }

    public void testMultiLevelFieldInDifferentFileAccessInService1() throws IOException {
        doFileTest(record2);
    }

    public void testMultiLevelFieldInDifferentFileAccessInService2() throws IOException {
        doFileTest(record2);
    }

    //TODO: Uncomment after fixing
//        public void testSingleLevelFieldInDifferentPackageAccessInFunction1() throws IOException {
    //            doFileTest(record2, "org/test/test.bal");
    //        }
    //
    //        public void testSingleLevelFieldInDifferentPackageAccessInFunction2() throws IOException {
    //            doFileTest(record2, "org/test/test.bal");
    //        }
    //
    //        public void testSingleLevelFieldInDifferentPackageAccessInService1() throws IOException {
    //            doFileTest(record2, "org/test/test.bal");
    //        }
    //
    //        public void testSingleLevelFieldInDifferentPackageAccessInService2() throws IOException {
    //            doFileTest(record2, "org/test/test.bal");
    //        }
    //
    //        public void testMultiLevelFieldInDifferentPackageAccessInFunction1() throws IOException {
    //            doFileTest(record2, "org/test/test.bal");
    //        }
    //
    //        public void testMultiLevelFieldInDifferentPackageAccessInFunction2() throws IOException {
    //            doFileTest(record2, "org/test/test.bal");
    //        }
    //
    //        public void testMultiLevelFieldInDifferentPackageAccessInService1() throws IOException {
    //            doFileTest(record2, "org/test/test.bal");
    //        }
    //
    //        public void testMultiLevelFieldInDifferentPackageAccessInService2() throws IOException {
    //            doFileTest(record2, "org/test/test.bal");
    //        }
}
