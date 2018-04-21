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
 * Parameter completion tests.
 */
public class BallerinaParameterCompletionTest extends BallerinaCompletionTestBase {

    /**
     * Test parameters.
     */
    public void testFunctionParamIdentifier() {
        doTest("function test(string <caret>)");
    }

//    public void testFunctionParamWithoutImports() {
//        List<String> expectedLookups = new LinkedList<>();
//        expectedLookups.addAll(DATA_TYPES);
//        expectedLookups.addAll(OTHER_TYPES);
//        expectedLookups.addAll(XMLNS_TYPE);
//        expectedLookups.addAll(REFERENCE_TYPES);
//        doTest("function test(<caret>)", expectedLookups.toArray(new String[expectedLookups.size()]));
//    }

//    public void testFunctionParamWithImports() {
//        List<String> expectedLookups = new LinkedList<>();
//        expectedLookups.addAll(DATA_TYPES);
//        expectedLookups.addAll(OTHER_TYPES);
//        expectedLookups.addAll(XMLNS_TYPE);
//        expectedLookups.addAll(REFERENCE_TYPES);
//        expectedLookups.add("test");
//        myFixture.addFileToProject("org/test/file.bal", "string s = \"\";");
//        doTest("import org.test; function B(<caret>)", expectedLookups.toArray(new String[expectedLookups.size()]));
//    }

//    public void testFunctionParamWithoutImportsAutoCompletion() {
//        doCheckResult("test.bal", "function test(st<caret>)", "function test(string )", null);
//    }

    public void testFunctionParamWithImportsAutoCompletion() {
        myFixture.addFileToProject("org/test/file.bal", "string s = \"\";");
        doCheckResult("test.bal", "import org.test; function B(te<caret>)",
                "import org.test; function B(test:)", null);
    }

//    public void testCaretAfterFunctionParamWithoutImports() {
//        List<String> expectedLookups = new LinkedList<>();
//        expectedLookups.addAll(DATA_TYPES);
//        expectedLookups.addAll(OTHER_TYPES);
//        expectedLookups.addAll(XMLNS_TYPE);
//        expectedLookups.addAll(REFERENCE_TYPES);
//        doTest("function test(string s,<caret>)", expectedLookups.toArray(new String[expectedLookups.size()]));
//    }

//    public void testCaretBeforeFunctionParamWithoutImports() {
//        List<String> expectedLookups = new LinkedList<>();
//        expectedLookups.addAll(DATA_TYPES);
//        expectedLookups.addAll(OTHER_TYPES);
//        expectedLookups.addAll(XMLNS_TYPE);
//        expectedLookups.addAll(REFERENCE_TYPES);
//        doTest("function test(<caret>string s)", expectedLookups.toArray(new String[expectedLookups.size()]));
//    }

//    public void testCaretAfterFunctionParamWithImports() {
//        List<String> expectedLookups = new LinkedList<>();
//        expectedLookups.addAll(DATA_TYPES);
//        expectedLookups.addAll(OTHER_TYPES);
//        expectedLookups.addAll(XMLNS_TYPE);
//        expectedLookups.addAll(REFERENCE_TYPES);
//        expectedLookups.add("test");
//        myFixture.addFileToProject("org/test/file.bal", "string s = \"\";");
//        doTest("import org.test; function test(string s,<caret>)",
//                expectedLookups.toArray(new String[expectedLookups.size()]));
//    }

//    public void testCaretBeforeFunctionParamWithImports() {
//        List<String> expectedLookups = new LinkedList<>();
//        expectedLookups.addAll(DATA_TYPES);
//        expectedLookups.addAll(OTHER_TYPES);
//        expectedLookups.addAll(XMLNS_TYPE);
//        expectedLookups.addAll(REFERENCE_TYPES);
//        expectedLookups.add("test");
//        myFixture.addFileToProject("org/test/file.bal", "string s = \"\";");
//        doTest("import org.test; function test(<caret>string s)",
//                expectedLookups.toArray(new String[expectedLookups.size()]));
//    }

//    public void testParamAnnotationsPackage() {
//        myFixture.addFileToProject("org/test/file.bal", "annotation TEST attach parameter {}");
//        doTest("import org.test; function A(@<caret>)", "test");
//    }
//
//    public void testParamAnnotationsPackageAutoCompletion() {
//        myFixture.addFileToProject("org/test/file.bal", "annotation TEST attach parameter {}");
//        doCheckResult("test.bal", "import org.test; function A(@te<caret>)",
//                "import org.test; function A(@test:)", null);
//    }
//
//    public void testParamAnnotationsFromAPackage() {
//        myFixture.addFileToProject("org/test/file.bal", "public annotation TEST attach parameter {}");
//        doTest("import org.test; function A(@test:<caret>)", "TEST");
//    }
//
//    public void testParamAnnotationsFromAPackageAutoCompletion() {
//        myFixture.addFileToProject("org/test/file.bal", "public annotation TEST attach parameter {}");
//        doCheckResult("test.bal", "import org.test; function A(@test:T<caret>)",
//                "import org.test; function A(@test:TEST {})", null);
//    }

    public void testPackageInvocationInParameter() {
        myFixture.addFileToProject("org/test/file.bal", "public struct test {}");
        doTest("import org.test; function A(test:<caret>)", "test");
    }

    public void testPackageInvocationInParameterAutoCompletion() {
        myFixture.addFileToProject("org/test/file.bal", "public struct test {}");
        doCheckResult("test.bal", "import org.test; function A(test:t<caret>)",
                "import org.test; function A(test:test )", null);
    }
}
