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

package org.ballerinalang.completion;

/**
 * Struct fields completion tests.
 */
public class BallerinaStructFieldCompletionTest extends BallerinaCompletionTestBase {

    @Override
    protected String getTestDataPath() {
        return getTestDataPath("completion/structFields");
    }

    //    public void testStructAsFunctionNamedReturn() {
    //        doTestFile("name", "age");
    //    }
    //
    //    public void testStructAsFunctionNamedReturnVarAssignment() {
    //        doTestFile("name", "age");
    //    }
    //
    //    public void testStructAsFunctionReturnParam() {
    //        doTestFile("name", "age");
    //    }
    //
    //    public void testStructAsFunctionReturnParamVarAssignment() {
    //        doTestFile("name", "age");
    //    }
    //
    //    public void testStructAsNamedReturn() {
    //        doTestFile("name", "age");
    //    }
    //
    //    public void testStructAsNamedReturn2() {
    //        doTestFile("name", "age");
    //    }
    //
    //    public void testStructAsNamedReturnPartialField() {
    //        doTestFile("name", "age");
    //    }

    public void testStructAsParam() {
        doTestFile("name", "age");
    }

    //    public void testStructAsParam2() {
    //        doTestFile("name", "age");
    //    }
    //
    //    public void testStructDeclarationReassigningAsVar() {
    //        doTestFile("name", "age");
    //    }
    //
    //    public void testStructDeclarationReassigningAsVar2() {
    //        doTestFile("name", "age");
    //    }
    //
    //    public void testStructVarReassigningAsVar() {
    //        doTestFile("name", "age");
    //    }
    //
    //    public void testStructVarReassigningAsVar2() {
    //        doTestFile("name", "age");
    //    }
    //
    //    public void testStructWithPrivateField() {
    //        doTestFile("name", "age");
    //    }
}
