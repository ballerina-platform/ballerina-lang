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

public class BallerinaConstantCompletionTest extends BallerinaCompletionTestBase {

    /**
     * Test constants.
     */
    public void testConstTypes() {
        doTest("const <caret>", DATA_TYPES.toArray(new String[DATA_TYPES.size()]));
    }

    public void testConstIdentifier() {
        doTest("const boolean <caret>");
    }

    public void testConstValues() {
        doTest("const string NAME = <caret>", "true", "false", "null");
    }

    public void testConstantAnnotation() {
        doCheckResult("test.bal", "@<caret> const string S=\"\";", null, '@');
    }

    public void testConstantAnnotationWithImports() {
        myFixture.addFileToProject("org/test/file.bal", "string s = \"\";");
        doCheckResult("test.bal", "import org.test; <caret> const string S=\"\";", null, '@', "test");
    }

    public void testConstantAnnotationWithImportsNoAnnotationDefinitions() {
        myFixture.addFileToProject("org/test/file.bal", "function test(){}");
        doCheckResult("test.bal", "import org.test; @test<caret> const string S=\"\";", null, ':');
    }

    public void testConstantAnnotationWithImportsWithNoMatchingAnnotationDefinitions() {
        myFixture.addFileToProject("org/test/file.bal", "public annotation TEST attach service {}");
        doCheckResult("test.bal", "import org.test; @test:<caret> const string S=\"\";", null, null);
    }

    public void testConstantAnnotationWithImportsWithMatchingAnnotationDefinitions() {
        myFixture.addFileToProject("org/test/file.bal", "package org.test; public annotation TEST attach const {} " +
                "annotation TEST2 attach resource {}");
        doCheckResult("test.bal", "import org.test; @test:<caret> const string S=\"\";", null, null, "TEST");
    }

    public void testConstantAnnotationWithImportsWithMatchingAnnotationDefinitionsAutoCompletion() {
        myFixture.addFileToProject("org/test/file.bal", "package org.test; public annotation TEST attach const {}");
        doCheckResult("test.bal", "import org.test; @test:T<caret> const string S=\"\";",
                "import org.test; @test:TEST {} const string S=\"\";", null);
    }

    public void testConstantAnnotationInCurrentPackageSameFile() {
        doCheckResult("test.bal", "annotation TEST attach const {} <caret> const string S=\"\";", null, '@',
                "TEST");
    }

    public void testConstantAnnotationInCurrentPackageSameFileAutoComplete() {
        doCheckResult("test.bal", "annotation TEST attach const {} @T<caret> const string S=\"\";",
                "annotation TEST attach const {} @TEST {} const string S=\"\";", null);
    }

    public void testConstantAnnotationInCurrentPackageDifferentFile() {
        myFixture.addFileToProject("file.bal", "annotation TEST attach const {}");
        doCheckResult("test.bal", "<caret> const string S=\"\";", null, '@', "TEST");
    }

    public void testConstantAnnotationInCurrentPackageDifferentFileAutoComplete() {
        myFixture.addFileToProject("file.bal", "annotation TEST attach function {}");
        doCheckResult("test.bal", "@T<caret> function A(){}", "@TEST {} function A(){}", null);
    }

    public void testConstantAnnotationInCurrentPackageDifferentFileHasMoreDefinitionsAfter() {
        myFixture.addFileToProject("file.bal", "annotation TEST attach const {}");
        doCheckResult("test.bal", "<caret> const string S=\"\"; service R{}", null, '@', "TEST");
    }

    public void testConstantAnnotationInCurrentPackageSameFileHasMoreDefinitionsAfter() {
        doCheckResult("test.bal", "annotation TEST attach const {} <caret> const string S=\"\"; service R{}", null, '@',
                "TEST");
    }

    public void testConstantTypesBeforeGlobalVarDef() {
        doTest("const <caret>\n string test =\"\";", DATA_TYPES.toArray(new String[DATA_TYPES.size()]));
    }

    public void testConstantInSamePackageSameFile() {
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

    public void testConstantInSamePackageDifferentFile() {
        myFixture.addFileToProject("file.bal", "const string S=\"\";");
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

    public void testConstantInDifferentPackage() {
        myFixture.addFileToProject("org/test/file.bal", "public const string S=\"\";");
        doTest("import org.test; function F(){ test:<caret> }", "S");
    }

    public void testConstantInDifferentPackageImportedAsAlias() {
        myFixture.addFileToProject("org/test/file.bal", "public cont string S=\"\";");
        doTest("import org.test as utils; function F(){ utils:<caret> }", "S");
    }
}
