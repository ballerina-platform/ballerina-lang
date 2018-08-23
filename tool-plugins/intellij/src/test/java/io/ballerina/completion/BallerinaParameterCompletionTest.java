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

package io.ballerina.completion;

import java.util.LinkedList;
import java.util.List;

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

    public void testFunctionParamWithoutImports() {
        List<String> expectedLookups = new LinkedList<>();
        expectedLookups.addAll(DATA_TYPES);
        expectedLookups.addAll(OTHER_TYPES);
        expectedLookups.addAll(REFERENCE_TYPES);
        doTestContains("function test(<caret>)", expectedLookups.toArray(new String[expectedLookups.size()]));
    }

    public void testFunctionParamWithImports() {
        List<String> expectedLookups = new LinkedList<>();
        expectedLookups.addAll(DATA_TYPES);
        expectedLookups.addAll(OTHER_TYPES);
        expectedLookups.addAll(REFERENCE_TYPES);
        expectedLookups.add("org");
        myFixture.addFileToProject("org/test/file.bal", "string s = \"\";");
        doTestContains("import org; function B(<caret>)", expectedLookups.toArray(new String[expectedLookups.size()]));
    }

    public void testFunctionParamWithoutImportsAutoCompletion() {
        doCheckResult("test.bal", "function test(stri<caret>)", "function test(string )", null);
    }

    public void testFunctionParamWithImportsAutoCompletion() {
        myFixture.addFileToProject("org/test/file.bal", "string s = \"\";");
        doCheckResult("test.bal", "import org; function B(or<caret>)", "import org; function B(org:)", null);
    }

    public void testCaretAfterFunctionParamWithoutImports() {
        List<String> expectedLookups = new LinkedList<>();
        expectedLookups.addAll(DATA_TYPES);
        expectedLookups.addAll(OTHER_TYPES);
        expectedLookups.addAll(REFERENCE_TYPES);
        doTestContains("function test(string s,<caret>)", expectedLookups.toArray(new String[expectedLookups.size()]));
    }

    public void testCaretAfterFunctionParamWithImports() {
        List<String> expectedLookups = new LinkedList<>();
        expectedLookups.addAll(DATA_TYPES);
        expectedLookups.addAll(OTHER_TYPES);
        expectedLookups.addAll(REFERENCE_TYPES);
        expectedLookups.add("org");
        myFixture.addFileToProject("org/test/file.bal", "string s = \"\";");
        doTestContains("import org; function test(string s,<caret>)",
                expectedLookups.toArray(new String[expectedLookups.size()]));
    }

    public void testCaretBeforeFunctionParamWithoutImports() {
        List<String> expectedLookups = new LinkedList<>();
        expectedLookups.addAll(DATA_TYPES);
        expectedLookups.addAll(OTHER_TYPES);
        expectedLookups.addAll(REFERENCE_TYPES);
        doTestContains("function test(<caret>string s)", expectedLookups.toArray(new String[expectedLookups.size()]));
    }

    public void testCaretBeforeFunctionParamWithImports() {
        List<String> expectedLookups = new LinkedList<>();
        expectedLookups.addAll(DATA_TYPES);
        expectedLookups.addAll(OTHER_TYPES);
        expectedLookups.addAll(REFERENCE_TYPES);
        expectedLookups.add("org");
        myFixture.addFileToProject("org/test/file.bal", "string s = \"\";");
        doTestContains("import org; function test(<caret>string s)",
                expectedLookups.toArray(new String[expectedLookups.size()]));
    }

    //TODO: Add annotation attachment related tests after fixing
}
