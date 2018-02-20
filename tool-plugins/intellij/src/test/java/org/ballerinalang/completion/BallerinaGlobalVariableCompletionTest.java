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

import java.util.LinkedList;
import java.util.List;

/**
 * Global variable completion tests.
 */
public class BallerinaGlobalVariableCompletionTest extends BallerinaCompletionTestBase {

    /**
     * Test global variables.
     */
    public void testGlobalVariableIdentifier() {
        doTest("string <caret>");
    }

    public void testGlobalVariableFunctionValue() {
        doCheckResult("test.bal", "string test = g<caret> function getValue()(string){return \"\";}",
                "string test = getValue() function getValue()(string){return \"\";}", null);
    }

    public void testGlobalVariablePackageValue() {
        myFixture.addFileToProject("org/test/file.bal", "function getValue()(string){return \"\";}");
        doTest("import org.test; string s = <caret> ", "test", "true", "false", "null");
    }

    public void testGlobalVariablePackageValueCompletion() {
        myFixture.addFileToProject("org/test/file.bal", "function getValue()(string){return \"\";}");
        doCheckResult("test.bal", "import org.test; string s = te<caret> ", "import org.test; string s = test: ",
                null);
    }

    public void testGlobalVariablePackageValueDifferentPackage() {
        myFixture.addFileToProject("org/test/file.bal", "public function getValue()(string){return \"\";}");
        doTest("import org.test; string s = test:<caret> ", "getValue");
    }

    public void testGlobalVariablePackageValueCompletionDifferentPackage() {
        myFixture.addFileToProject("org/test/file.bal", "public function getValue()(string){return \"\";}");
        doCheckResult("test.bal", "import org.test; string s = test:g<caret> ",
                "import org.test; string s = test:getValue() ", null);
    }

    public void testGlobalVariablePackageValueDifferentFile() {
        myFixture.addFileToProject("file.bal", "function getValue()(string){return \"\";}");
        doTest("string s = <caret> ", "getValue", "true", "false", "null");
    }

    public void testGlobalVariablePackageValueDifferentFileCompletion() {
        myFixture.addFileToProject("file.bal", "function getValue()(string){return \"\";}");
        doCheckResult("test.bal", "string s = g<caret>", "string s = getValue()", null);
    }

    public void testGlobalVariableInSamePackageSameFile() {
        List<String> expectedLookups = new LinkedList<>();
        expectedLookups.addAll(DATA_TYPES);
        expectedLookups.addAll(OTHER_TYPES);
        expectedLookups.addAll(XMLNS_TYPE);
        expectedLookups.addAll(REFERENCE_TYPES);
        expectedLookups.addAll(COMMON_KEYWORDS);
        expectedLookups.addAll(FUNCTION_LEVEL_KEYWORDS);
        expectedLookups.add("S");
        expectedLookups.add("F");
        doTest("string S=\"\"; function F(){ <caret> }", expectedLookups.toArray(new String[expectedLookups.size()]));
    }

    public void testGlobalVariableInSamePackageDifferentFile() {
        myFixture.addFileToProject("file.bal", "string S=\"\";");
        List<String> expectedLookups = new LinkedList<>();
        expectedLookups.addAll(DATA_TYPES);
        expectedLookups.addAll(OTHER_TYPES);
        expectedLookups.addAll(XMLNS_TYPE);
        expectedLookups.addAll(REFERENCE_TYPES);
        expectedLookups.addAll(COMMON_KEYWORDS);
        expectedLookups.addAll(FUNCTION_LEVEL_KEYWORDS);
        expectedLookups.add("S");
        expectedLookups.add("F");
        doTest("function F(){ <caret> }", expectedLookups.toArray(new String[expectedLookups.size()]));
    }

    public void testGlobalVariableInDifferentPackage() {
        myFixture.addFileToProject("org/test/file.bal", "public string S=\"\";");
        doTest("import org.test; function F(){ test:<caret> }", "S");
    }

    public void testGlobalVariableInDifferentPackageImportedAsAlias() {
        myFixture.addFileToProject("org/test/file.bal", "public string S=\"\";");
        doTest("import org.test as utils; function F(){ utils:<caret> }", "S");
    }
}
