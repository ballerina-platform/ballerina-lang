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

import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class BallerinaCompletionTest extends LightPlatformCodeInsightFixtureTestCase {

    private static final List<String> FILE_LEVEL_KEYWORDS = Arrays.asList("package", "import", "const", "service",
            "function", "connector", "struct", "typemapper", "annotation");
    private static final List<String> DATA_TYPES = Arrays.asList("boolean", "int", "float", "string");
    private static final List<String> REFERENCE_TYPES = Arrays.asList("message", "xml", "json", "exception", "map",
            "datatable");
    private static final List<String> COMMON_KEYWORDS = Arrays.asList("if", "else");
    private static final List<String> FUNCTION_LEVEL_KEYWORDS = Collections.singletonList("return");

    private static final String UTILS_PACKAGE_NAME = "org/test/utils.bal";
    private static final String SAMPLE_UTIL_FUNCTIONS = "package org.test; function getA(){} function getB(){}";

    @Override
    protected String getTestDataPath() {
        return "src/test/resources/testData/completion";
    }

    /**
     * Test file level lookups.
     */
    public void testEmptyFile() {
        doTest("<caret>", FILE_LEVEL_KEYWORDS.toArray(new String[FILE_LEVEL_KEYWORDS.size()]));
    }

    public void testEmptyFileWithSpaceBeforeCaret() {
        doTest("\n<caret>", FILE_LEVEL_KEYWORDS.toArray(new String[FILE_LEVEL_KEYWORDS.size()]));
    }

    public void testEmptyFileWithSpaceAfterCaret() {
        doTest("<caret>\n", FILE_LEVEL_KEYWORDS.toArray(new String[FILE_LEVEL_KEYWORDS.size()]));
    }

    public void testEmptyFileWithSpaces() {
        doTest("\n<caret>\n", FILE_LEVEL_KEYWORDS.toArray(new String[FILE_LEVEL_KEYWORDS.size()]));
    }

    /**
     * Test package declaration level lookups.
     */
    public void testFirstLevelPackageLookups() {
        PsiFile testFile = myFixture.addFileToProject("org/test/file1.bal", "package <caret>");
        myFixture.configureFromExistingVirtualFile(testFile.getVirtualFile());
        doTest(null, "org");
    }

    public void testFirstLevelPackageAutoCompletion() {
        doCheckResult("org/test/test.bal", "package o<caret>", "package org.", null);
    }

    public void testFirstLevelPackageInsertHandler() {
        PsiFile testFile = myFixture.addFileToProject("org/test/file1.bal", "package org.<caret>");
        myFixture.configureFromExistingVirtualFile(testFile.getVirtualFile());
        doTest(null, "test");
    }

    public void testLastLevelPackageInsertHandler() {
        doCheckResult("org/test/test.bal", "package org.t<caret>", "package org.test;", null);
    }

    /**
     * Test import declaration level lookups.
     */
    public void testFirstLevelImportLookups() {
        myFixture.addFileToProject("org/test/file.bal", "package org.test; function A(){}");
        doTest("import <caret>", "org");
    }

    public void testLastLevelImportLookups() {
        myFixture.addFileToProject("org/test/file.bal", "package org.test; function A(){}");
        doTest("import org.<caret>", "test");
    }

    public void testFirstLevelImportAutoCompletion() {
        myFixture.addFileToProject("org/test/file.bal", "package org.test; function A(){}");
        doCheckResult("test.bal", "import o<caret>", "import org.", null);
    }

    public void testLastLevelImportAutoCompletion() {
        myFixture.addFileToProject("org/test/file.bal", "package org.test; function A(){}");
        doCheckResult("test.bal", "import org.t<caret>", "import org.test;", null);
    }

    /**
     * Test constant declaration level lookups.
     */

    public void testConstTypes() {
        doTest("const <caret>", DATA_TYPES.toArray(new String[DATA_TYPES.size()]));
    }

    public void testConstIdentifier() {
        doTest("const boolean <caret>");
    }

    public void testConstValues() {
        doTest("const string NAME = <caret>");
    }

    // todo - add annotation tests

    /**
     * Test annotation level lookups.
     */
    // todo - add tests

    /**
     * Test function level lookups.
     */
    public void testFunctionIdentifier() {
        doTest("function <caret>");
    }

    public void testFunctionBodyWithoutParamsAndImports() {
        List<String> FUNCTION_LEVEL_SUGGESTIONS = Collections.singletonList("test");
        List<String> expectedLookups = new LinkedList<>();
        expectedLookups.addAll(DATA_TYPES);
        expectedLookups.addAll(REFERENCE_TYPES);
        expectedLookups.addAll(COMMON_KEYWORDS);
        expectedLookups.addAll(FUNCTION_LEVEL_KEYWORDS);
        expectedLookups.addAll(FUNCTION_LEVEL_SUGGESTIONS);
        doTest("function test () { <caret> }", expectedLookups.toArray(new String[expectedLookups.size()]));
    }

    public void testFunctionBodyWithParams() {
        List<String> FUNCTION_LEVEL_SUGGESTIONS = Arrays.asList("test", "arg");
        List<String> expectedLookups = new LinkedList<>();
        expectedLookups.addAll(DATA_TYPES);
        expectedLookups.addAll(REFERENCE_TYPES);
        expectedLookups.addAll(COMMON_KEYWORDS);
        expectedLookups.addAll(FUNCTION_LEVEL_KEYWORDS);
        expectedLookups.addAll(FUNCTION_LEVEL_SUGGESTIONS);
        doTest("function test (int arg) { <caret> }", expectedLookups.toArray(new String[expectedLookups.size()]));
    }

    public void testFunctionBodyWithConst() {
        List<String> FUNCTION_LEVEL_SUGGESTIONS = Arrays.asList("test", "arg", "GREETING");
        List<String> expectedLookups = new LinkedList<>();
        expectedLookups.addAll(DATA_TYPES);
        expectedLookups.addAll(REFERENCE_TYPES);
        expectedLookups.addAll(COMMON_KEYWORDS);
        expectedLookups.addAll(FUNCTION_LEVEL_KEYWORDS);
        expectedLookups.addAll(FUNCTION_LEVEL_SUGGESTIONS);
        doTest("const string GREETING = \"Hello\"; function test (int arg) { <caret> }",
                expectedLookups.toArray(new String[expectedLookups.size()]));
    }

    public void testFunctionBodyWithImports() {
        List<String> expectedLookups = new LinkedList<>();
        expectedLookups.addAll(DATA_TYPES);
        expectedLookups.addAll(REFERENCE_TYPES);
        expectedLookups.addAll(COMMON_KEYWORDS);
        expectedLookups.addAll(FUNCTION_LEVEL_KEYWORDS);
        expectedLookups.add("pack");
        expectedLookups.add("test");
        doTest("import org.pack; function test () { <caret> }",
                expectedLookups.toArray(new String[expectedLookups.size()]));
    }

    public void testFunctionAnnotation() {
        doCheckResult("test.bal", "@<caret> function A(){}", null, '@');
    }

    public void testFunctionAnnotationWithImports() {
        doCheckResult("test.bal", "import org.test; <caret>function A(){}", null, '@', "test");
    }

    public void testFunctionAnnotationWithImportsNoAnnotationDefinitions() {
        myFixture.addFileToProject("org/test/file.bal", "function test(){}");
        doCheckResult("test.bal", "import org.test; @test<caret> function A(){}", null, ':');
    }

    public void testFunctionAnnotationWithImportsWithNoMatchingAnnotationDefinitions() {
        myFixture.addFileToProject("org/test/file.bal", "annotation TEST attach test {}");
        doCheckResult("test.bal", "import org.test; @test:<caret> function A(){}", null, null);
    }

    public void testFunctionAnnotationWithImportsWithMatchingAnnotationDefinitions() {
        myFixture.addFileToProject("org/test/file.bal", "package org.test; annotation TEST attach function {} " +
                "annotation TEST2 attach resource {}");
        doCheckResult("test.bal", "import org.test; @test:<caret> function A(){}", null, null, "TEST");
    }

    public void testFunctionAnnotationWithImportsWithMatchingAnnotationDefinitionsAutoCompletion() {
        myFixture.addFileToProject("org/test/file.bal", "package org.test; annotation TEST attach function {}");
        doCheckResult("test.bal", "import org.test; @test:T<caret> function A(){}",
                "import org.test; @test:TEST {} function A(){}", null);
    }

    public void testFunctionAnnotationInCurrentPackageSameFile() {
        doCheckResult("test.bal", "annotation TEST attach function {} <caret> function A(){}", null, '@', "TEST");
    }

    public void testFunctionAnnotationInCurrentPackageSameFileAutoComplete() {
        doCheckResult("test.bal", "annotation TEST attach function {} @T<caret> function A(){}",
                "annotation TEST attach function {} @TEST {} function A(){}", null);
    }

    public void testFunctionAnnotationInCurrentPackageDifferentFile() {
        myFixture.addFileToProject("file.bal", "annotation TEST attach function {}");
        doCheckResult("test.bal", "<caret> function A(){}", null, '@', "TEST");
    }

    public void testFunctionAnnotationInCurrentPackageDifferentFileAutoComplete() {
        myFixture.addFileToProject("file.bal", "annotation TEST attach function {}");
        doCheckResult("test.bal", "@T<caret> function A(){}", "@TEST {} function A(){}", null);
    }

    public void testFunctionAnnotationInCurrentPackageDifferentFileHasMoreDefinitionsAfter() {
        myFixture.addFileToProject("file.bal", "annotation TEST attach function {}");
        doCheckResult("test.bal", "<caret> function A(){} service R{}", null, '@', "TEST");
    }

    public void testFunctionAnnotationInCurrentPackageSameFileHasMoreDefinitionsAfter() {
        doCheckResult("test.bal", "annotation TEST attach function {} <caret> function A(){} service R{}", null, '@',
                "TEST");
    }

    /**
     * Test function parameter level lookups.
     */
    public void testFunctionParamIdentifier() {
        doTest("function test(string <caret>)");
    }

    public void testFunctionParamWithoutImports() {
        List<String> expectedLookups = new LinkedList<>();
        expectedLookups.addAll(DATA_TYPES);
        expectedLookups.addAll(REFERENCE_TYPES);
        doTest("function test(<caret>)", expectedLookups.toArray(new String[expectedLookups.size()]));
    }

    public void testFunctionParamWithImports() {
        List<String> expectedLookups = new LinkedList<>();
        expectedLookups.addAll(DATA_TYPES);
        expectedLookups.addAll(REFERENCE_TYPES);
        expectedLookups.add("test");
        doTest("import org.test; function B(<caret>)", expectedLookups.toArray(new String[expectedLookups.size()]));
    }

    public void testFunctionParamWithoutImportsAutoCompletion() {
        doCheckResult("test.bal", "function test(st<caret>)", "function test(string )", null);
    }

    public void testFunctionParamWithImportsAutoCompletion() {
        doCheckResult("test.bal", "import org.test; function B(te<caret>)",
                "import org.test; function B(test:)", null);
    }

    public void testCaretAfterFunctionParamWithoutImports() {
        List<String> expectedLookups = new LinkedList<>();
        expectedLookups.addAll(DATA_TYPES);
        expectedLookups.addAll(REFERENCE_TYPES);
        doTest("function test(string s,<caret>)", expectedLookups.toArray(new String[expectedLookups.size()]));
    }

    public void testCaretBeforeFunctionParamWithoutImports() {
        List<String> expectedLookups = new LinkedList<>();
        expectedLookups.addAll(DATA_TYPES);
        expectedLookups.addAll(REFERENCE_TYPES);
        doTest("function test(<caret>string s)", expectedLookups.toArray(new String[expectedLookups.size()]));
    }

    public void testCaretAfterFunctionParamWithImports() {
        List<String> expectedLookups = new LinkedList<>();
        expectedLookups.addAll(DATA_TYPES);
        expectedLookups.addAll(REFERENCE_TYPES);
        expectedLookups.add("test");
        doTest("import org.test; function test(string s,<caret>)",
                expectedLookups.toArray(new String[expectedLookups.size()]));
    }

    public void testCaretBeforeFunctionParamWithImports() {
        List<String> expectedLookups = new LinkedList<>();
        expectedLookups.addAll(DATA_TYPES);
        expectedLookups.addAll(REFERENCE_TYPES);
        expectedLookups.add("test");
        doTest("import org.test; function test(<caret>string s)",
                expectedLookups.toArray(new String[expectedLookups.size()]));
    }

    /**
     * Test service level lookups.
     */
    public void testServiceIdentifier() {
        doTest("service <caret>");
    }

    public void testServiceBody() {
        doTest("service S{<caret>}", "resource");
    }

    public void testServiceAnnotation() {
        doCheckResult("test.bal", "@<caret> service S{}", null, '@');
    }

    public void testServiceAnnotationWithImports() {
        doCheckResult("test.bal", "import org.test; <caret>service S{}", null, '@', "test");
    }

    public void testServiceAnnotationWithImportsNoAnnotationDefinitions() {
        myFixture.addFileToProject("org/test/file.bal", "function test(){}");
        doCheckResult("test.bal", "import org.test; @test<caret> service S{}", null, ':');
    }

    public void testServiceAnnotationWithImportsWithNoMatchingAnnotationDefinitions() {
        myFixture.addFileToProject("org/test/file.bal", "annotation TEST attach test {}");
        doCheckResult("test.bal", "import org.test; @test:<caret> service S{}", null, null);
    }

    public void testServiceAnnotationWithImportsWithMatchingAnnotationDefinitions() {
        myFixture.addFileToProject("org/test/file.bal", "package org.test; annotation TEST attach service {} " +
                "annotation TEST2 attach resource {}");
        doCheckResult("test.bal", "import org.test; @test:<caret> service S{}", null, null, "TEST");
    }

    public void testServiceAnnotationWithImportsWithMatchingAnnotationDefinitionsAutoCompletion() {
        myFixture.addFileToProject("org/test/file.bal", "package org.test; annotation TEST attach service {}");
        doCheckResult("test.bal", "import org.test; @test:T<caret> service S{}",
                "import org.test; @test:TEST {} service S{}", null);
    }

    public void testServiceAnnotationInCurrentPackageSameFile() {
        doCheckResult("test.bal", "annotation TEST attach service {} <caret> service S{}", null, '@', "TEST");
    }

    public void testServiceAnnotationInCurrentPackageSameFileAutoComplete() {
        doCheckResult("test.bal", "annotation TEST attach service {} @T<caret> service S{}",
                "annotation TEST attach service {} @TEST {} service S{}", null);
    }

    public void testServiceAnnotationInCurrentPackageDifferentFile() {
        myFixture.addFileToProject("file.bal", "annotation TEST attach service {}");
        doCheckResult("test.bal", "<caret> service S{}", null, '@', "TEST");
    }

    public void testServiceAnnotationInCurrentPackageDifferentFileAutoComplete() {
        myFixture.addFileToProject("file.bal", "annotation TEST attach service {}");
        doCheckResult("test.bal", "@T<caret> service S{}", "@TEST {} service S{}", null);
    }

    public void testServiceAnnotationInCurrentPackageDifferentFileHasMoreDefinitionsAfter() {
        myFixture.addFileToProject("file.bal", "annotation TEST attach service {}");
        doCheckResult("test.bal", "<caret> service S{} service R{}", null, '@', "TEST");
    }

    public void testServiceAnnotationInCurrentPackageSameFileHasMoreDefinitionsAfter() {
        doCheckResult("test.bal", "annotation TEST attach service {} <caret> service S{} service R{}", null, '@',
                "TEST");
    }

    /**
     * Test resource level lookups.
     */
    public void testResourceIdentifier() {
        doTest("service S { resource <caret> ");
    }

    public void testResourceAnnotation() {
        doCheckResult("test.bal", "service S{<caret>}", null, '@');
    }

    public void testResourceAnnotationWithImports() {
        doCheckResult("test.bal", "import org.test; service S{<caret>}", null, '@', "test");
    }

    public void testResourceAnnotationWithImportsNoAnnotationDefinitions() {
        myFixture.addFileToProject("org/test/file.bal", "function test(){}");
        doCheckResult("test.bal", "import org.test; service S{@test<caret>}", null, ':');
    }

    public void testResourceAnnotationWithImportsWithNoMatchingAnnotationDefinitions() {
        myFixture.addFileToProject("org/test/file.bal", "annotation TEST attach test {}");
        doCheckResult("test.bal", "import org.test; service S{@test:<caret>}", null, null);
    }

    public void testResourceAnnotationWithImportsWithMatchingAnnotationDefinitions() {
        myFixture.addFileToProject("org/test/file.bal", "package org.test; annotation TEST attach resource {} " +
                "annotation TEST2 attach service {}");
        doCheckResult("test.bal", "import org.test; service S{@test:<caret>}", null, null, "TEST");
    }

    public void testResourceAnnotationWithImportsWithMatchingAnnotationDefinitionsAutoCompletion() {
        myFixture.addFileToProject("org/test/file.bal", "package org.test; annotation TEST attach resource {}");
        doCheckResult("test.bal", "import org.test; service S{@test:T<caret>}",
                "import org.test; service S{@test:TEST {}}", null);
    }

    public void testResourceAnnotationInCurrentPackageSameFile() {
        doCheckResult("test.bal", "annotation TEST attach resource {} service S{<caret>}", null, '@', "TEST");
    }

    public void testResourceAnnotationInCurrentPackageSameFileAutoComplete() {
        doCheckResult("test.bal", "annotation TEST attach resource {} service S{@T<caret>}",
                "annotation TEST attach resource {} service S{@TEST {}}", null);
    }

    public void testResourceAnnotationInCurrentPackageDifferentFile() {
        myFixture.addFileToProject("file.bal", "annotation TEST attach resource {}");
        doCheckResult("test.bal", "service S{<caret>}", null, '@', "TEST");
    }

    public void testResourceAnnotationInCurrentPackageDifferentFileAutoComplete() {
        myFixture.addFileToProject("file.bal", "annotation TEST attach resource {}");
        doCheckResult("test.bal", "service S{@T<caret>}", "service S{@TEST {}}", null);
    }

    public void testResourceAnnotationInCurrentPackageDifferentFileHasMoreDefinitionsAfter() {
        myFixture.addFileToProject("file.bal", "annotation TEST attach resource {}");
        doCheckResult("test.bal", "service S{<caret>} service R{}", null, '@', "TEST");
    }

    public void testResourceAnnotationInCurrentPackageSameFileHasMoreDefinitionsAfter() {
        doCheckResult("test.bal", "annotation TEST attach resource {} service S{<caret>} service R{}", null, '@',
                "TEST");
    }

    /**
     * Test resource parameter level lookups.
     */
    public void testResourceParamIdentifier() {
        doTest("service S { resource R(string <caret>)");
    }

    public void testResourceParamWithoutImports() {
        List<String> expectedLookups = new LinkedList<>();
        expectedLookups.addAll(DATA_TYPES);
        expectedLookups.addAll(REFERENCE_TYPES);
        doTest("service S { resource R(<caret>)", expectedLookups.toArray(new String[expectedLookups.size()]));
    }

    public void testResourceParamWithImports() {
        List<String> expectedLookups = new LinkedList<>();
        expectedLookups.addAll(DATA_TYPES);
        expectedLookups.addAll(REFERENCE_TYPES);
        expectedLookups.add("test");
        doTest("import org.test; service S { resource R(<caret>)",
                expectedLookups.toArray(new String[expectedLookups.size()]));
    }

    public void testResourceParamWithoutImportsAutoCompletion() {
        doCheckResult("test.bal", "service S { resource R(st<caret>)", "service S { resource R(string )", null);
    }

    public void testResourceParamWithImportsAutoCompletion() {
        doCheckResult("test.bal", "import org.test; service S { resource R(te<caret>)",
                "import org.test; service S { resource R(test:)", null);
    }

    public void testCaretAfterResourceParamWithoutImports() {
        List<String> expectedLookups = new LinkedList<>();
        expectedLookups.addAll(DATA_TYPES);
        expectedLookups.addAll(REFERENCE_TYPES);
        doTest("service S { resource R(string s,<caret>)", expectedLookups.toArray(new String[expectedLookups.size()]));
    }

    public void testCaretBeforeResourceParamWithoutImports() {
        List<String> expectedLookups = new LinkedList<>();
        expectedLookups.addAll(DATA_TYPES);
        expectedLookups.addAll(REFERENCE_TYPES);
        doTest("service S { resource R(<caret>string s)", expectedLookups.toArray(new String[expectedLookups.size()]));
    }

    public void testCaretAfterResourceParamWithImports() {
        List<String> expectedLookups = new LinkedList<>();
        expectedLookups.addAll(DATA_TYPES);
        expectedLookups.addAll(REFERENCE_TYPES);
        expectedLookups.add("test");
        doTest("import org.test; service S { resource R(string s,<caret>)",
                expectedLookups.toArray(new String[expectedLookups.size()]));
    }

    public void testCaretBeforeResourceParamWithImports() {
        List<String> expectedLookups = new LinkedList<>();
        expectedLookups.addAll(DATA_TYPES);
        expectedLookups.addAll(REFERENCE_TYPES);
        expectedLookups.add("test");
        doTest("import org.test; service S { resource R(<caret>string s)",
                expectedLookups.toArray(new String[expectedLookups.size()]));
    }

    //todo - query param annotations

    /**
     * Test connector level lookups.
     */
    public void testConnectorIdentifier() {
        doTest("connector <caret>");
    }

    public void testConnectorAnnotation() {
        doCheckResult("test.bal", "@<caret> connector A{}", null, '@');
    }

    public void testConnectorAnnotationWithImports() {
        doCheckResult("test.bal", "import org.test; <caret>connector A{}", null, '@', "test");
    }

    public void testConnectorAnnotationWithImportsNoAnnotationDefinitions() {
        myFixture.addFileToProject("org/test/file.bal", "function test(){}");
        doCheckResult("test.bal", "import org.test; @test<caret> connector A{}", null, ':');
    }

    public void testConnectorAnnotationWithImportsWithNoMatchingAnnotationDefinitions() {
        myFixture.addFileToProject("org/test/file.bal", "annotation TEST attach test {}");
        doCheckResult("test.bal", "import org.test; @test:<caret> connector A{}", null, null);
    }

    public void testConnectorAnnotationWithImportsWithMatchingAnnotationDefinitions() {
        myFixture.addFileToProject("org/test/file.bal", "package org.test; annotation TEST attach connector {} " +
                "annotation TEST2 attach resource {}");
        doCheckResult("test.bal", "import org.test; @test:<caret> connector A{}", null, null, "TEST");
    }

    public void testConnectorAnnotationWithImportsWithMatchingAnnotationDefinitionsAutoCompletion() {
        myFixture.addFileToProject("org/test/file.bal", "package org.test; annotation TEST attach connector {}");
        doCheckResult("test.bal", "import org.test; @test:T<caret> connector A{}",
                "import org.test; @test:TEST {} connector A{}", null);
    }

    public void testConnectorAnnotationInCurrentPackageSameFile() {
        doCheckResult("test.bal", "annotation TEST attach connector {} <caret> connector A{}", null, '@', "TEST");
    }

    public void testConnectorAnnotationInCurrentPackageSameFileAutoComplete() {
        doCheckResult("test.bal", "annotation TEST attach connector {} @T<caret> connector A{}",
                "annotation TEST attach connector {} @TEST {} connector A{}", null);
    }

    public void testConnectorAnnotationInCurrentPackageDifferentFile() {
        myFixture.addFileToProject("file.bal", "annotation TEST attach connector {}");
        doCheckResult("test.bal", "<caret> connector A{}", null, '@', "TEST");
    }

    public void testConnectorAnnotationInCurrentPackageDifferentFileAutoComplete() {
        myFixture.addFileToProject("file.bal", "annotation TEST attach connector {}");
        doCheckResult("test.bal", "@T<caret> connector A{}", "@TEST {} connector A{}", null);
    }

    public void testConnectorAnnotationInCurrentPackageDifferentFileHasMoreDefinitionsAfter() {
        myFixture.addFileToProject("file.bal", "annotation TEST attach connector {}");
        doCheckResult("test.bal", "<caret> connector A{} service R{}", null, '@', "TEST");
    }

    public void testConnectorAnnotationInCurrentPackageSameFileHasMoreDefinitionsAfter() {
        doCheckResult("test.bal", "annotation TEST attach connector {} <caret> connector A{} service R{}", null,
                '@', "TEST");
    }

    /**
     * Test action level lookups.
     */
    // todo - add tests

    /**
     * Test struct level lookups.
     */
    // todo - add tests

    /**
     * Test typemapper level lookups.
     */
    // todo - add tests

    /**
     *
     */
    public void testInvokingFunctionInDifferentFile1() {
        myFixture.addFileToProject("file1.bal", "function test(){}");
        myFixture.configureByText("file2.bal", "function main(string[] args){ tes<caret> }");
        doCheckResult("test.bal", "function main(string[] args){ te<caret> }",
                "function main(string[] args){ test() }", null);
    }

    public void testInvokingFunctionInDifferentFile2() {
        myFixture.addFileToProject("file1.bal", "function test1(){} function test2(){}");
        doTest("function main(string[] args){ tes<caret> }", "test1", "test2");
    }

    public void testVariable1() {
        doTest("function main(string[] args){ int <caret> }");
    }

    public void testVariable2() {
        doTest("function main(string[] args){ int a = <caret> }", "args", "main");
    }

    public void testFunctionFromPackageInvocation1() {
        myFixture.addFileToProject(UTILS_PACKAGE_NAME, SAMPLE_UTIL_FUNCTIONS);
        doTest("import org.test; function main(string[] args){ test:<caret> }", "getA", "getB");
    }

    public void testFunctionFromPackageInvocation2() {
        myFixture.addFileToProject(UTILS_PACKAGE_NAME, SAMPLE_UTIL_FUNCTIONS);
        doTest("import org.test; function main(string[] args){ test:g<caret> }", "getA", "getB");
    }

    public void testFunctionFromPackageInvocation3() {
        myFixture.addFileToProject(UTILS_PACKAGE_NAME, SAMPLE_UTIL_FUNCTIONS);
        doTest("import org.test; function main(string[] args){ test(test:<caret>) } function test(string s){}",
                "getA", "getB");
    }

    public void testFunctionFromPackageInvocation4() {
        myFixture.addFileToProject(UTILS_PACKAGE_NAME, SAMPLE_UTIL_FUNCTIONS);
        doTest("import org.test; function main(string[] args){ test(test:g<caret>) } function test(string s){}",
                "getA", "getB");
    }

    public void testFunctionFromPackageInvocation5() {
        myFixture.addFileToProject(UTILS_PACKAGE_NAME, SAMPLE_UTIL_FUNCTIONS);
        doTest("import org.test; function main(string[] args){ test(\"TEST\"+test:<caret>) } " +
                "function test(string s){}", "getA", "getB");
    }

    public void testFunctionFromPackageInvocation6() {
        myFixture.addFileToProject(UTILS_PACKAGE_NAME, SAMPLE_UTIL_FUNCTIONS);
        doTest("import org.test; function main(string[] args){ test(\"TEST\"+test:g<caret>) } " +
                "function test(string s){}", "getA", "getB");
    }

    public void testFunctionFromPackageInvocation7() {
        myFixture.addFileToProject(UTILS_PACKAGE_NAME, SAMPLE_UTIL_FUNCTIONS);
        doTest("import org.test; function main(string[] args){ test(\"TEST\" + test:<caret>) } " +
                "function test(string s){}", "getA", "getB");
    }

    public void testFunctionFromPackageInvocation8() {
        myFixture.addFileToProject(UTILS_PACKAGE_NAME, SAMPLE_UTIL_FUNCTIONS);
        doTest("import org.test; function main(string[] args){ test(\"TEST\" + test:g<caret>) } " +
                "function test(string s){}", "getA", "getB");
    }

    public void testFunctionFromPackageInvocation9() {
        myFixture.addFileToProject(UTILS_PACKAGE_NAME, SAMPLE_UTIL_FUNCTIONS);
        doTest("import org.test; function main(string[] args){ test(\"TEST\"+test:<caret>+\"TEST\") } " +
                "function test(string s){}", "getA", "getB");
    }

    public void testFunctionFromPackageInvocation10() {
        myFixture.addFileToProject(UTILS_PACKAGE_NAME, SAMPLE_UTIL_FUNCTIONS);
        doTest("import org.test; function main(string[] args){ test(\"TEST\"+test:g<caret>+\"TEST\") } " +
                "function test(string s){}", "getA", "getB");
    }

    public void testFunctionFromPackageInvocation11() {
        myFixture.addFileToProject(UTILS_PACKAGE_NAME, SAMPLE_UTIL_FUNCTIONS);
        doTest("import org.test; function main(string[] args){ test(\"TEST\"+test:<caret> +\"TEST\") } " +
                "function test(string s){}", "getA", "getB");
    }

    public void testFunctionFromPackageInvocation12() {
        myFixture.addFileToProject(UTILS_PACKAGE_NAME, SAMPLE_UTIL_FUNCTIONS);
        doTest("import org.test; function main(string[] args){ test(\"TEST\"+test:g<caret> +\"TEST\") }",
                "getA", "getB");
    }

    public void testFunctionFromPackageInvocation13() {
        myFixture.addFileToProject(UTILS_PACKAGE_NAME, SAMPLE_UTIL_FUNCTIONS);
        doTest("import org.test; function main(string[] args){ string s =test:<caret> }",
                "getA", "getB");
    }

    public void testFunctionFromPackageInvocation14() {
        myFixture.addFileToProject(UTILS_PACKAGE_NAME, SAMPLE_UTIL_FUNCTIONS);
        doTest("import org.test; function main(string[] args){ string s =test:g<caret> }",
                "getA", "getB");
    }

    public void testFunctionFromPackageInvocation15() {
        myFixture.addFileToProject(UTILS_PACKAGE_NAME, SAMPLE_UTIL_FUNCTIONS);
        doTest("import org.test; function main(string[] args){ string s = test:<caret> }",
                "getA", "getB");
    }

    public void testFunctionFromPackageInvocation16() {
        myFixture.addFileToProject(UTILS_PACKAGE_NAME, SAMPLE_UTIL_FUNCTIONS);
        doTest("import org.test; function main(string[] args){ string s = test:g<caret> }",
                "getA", "getB");
    }

    public void testFunctionFromPackageInvocation17() {
        myFixture.addFileToProject(UTILS_PACKAGE_NAME, SAMPLE_UTIL_FUNCTIONS);
        doTest("import org.test; function main(string[] args){ string s = test:<caret>+\"TEST\"; }",
                "getA", "getB");
    }

    public void testFunctionFromPackageInvocation18() {
        myFixture.addFileToProject(UTILS_PACKAGE_NAME, SAMPLE_UTIL_FUNCTIONS);
        doTest("import org.test; function main(string[] args){ string s = test:g<caret>+\"TEST\"; }",
                "getA", "getB");
    }

    public void testFunctionFromPackageInvocation19() {
        myFixture.addFileToProject(UTILS_PACKAGE_NAME, SAMPLE_UTIL_FUNCTIONS);
        doTest("import org.test; function main(string[] args){ string s = \"TEST\"+test:<caret>; }",
                "getA", "getB");
    }

    public void testFunctionFromPackageInvocation20() {
        myFixture.addFileToProject(UTILS_PACKAGE_NAME, SAMPLE_UTIL_FUNCTIONS);
        doTest("import org.test; function main(string[] args){ string s = \"TEST\"+test:g<caret>; }",
                "getA", "getB");
    }

    public void testFunctionFromPackageInvocation21() {
        myFixture.addFileToProject(UTILS_PACKAGE_NAME, SAMPLE_UTIL_FUNCTIONS);
        doTest("import org.test; function main(string[] args){ string s = test:getA()+test:<caret> \"TEST\"; }",
                "getA", "getB");
    }

    public void testFunctionFromPackageInvocation22() {
        myFixture.addFileToProject(UTILS_PACKAGE_NAME, SAMPLE_UTIL_FUNCTIONS);
        doTest("import org.test; function main(string[] args){ string s = test:getA()+test:g<caret> \"TEST\"; }",
                "getA", "getB");
    }

    public void testFunctionFromPackageInvocation23() {
        myFixture.addFileToProject(UTILS_PACKAGE_NAME, SAMPLE_UTIL_FUNCTIONS);
        doTest("import org.test; function main(string[] args){ string s = test:getA()+<caret> \"TEST\"; }",
                "args", "main", "test");
    }

    public void testFunctionFromPackageInvocation24() {
        myFixture.addFileToProject(UTILS_PACKAGE_NAME, SAMPLE_UTIL_FUNCTIONS);
        doTest("import org.test; function main(string[] args){ string s = test:getA()+g<caret> \"TEST\"; }",
                "args");
    }

    private void doTest(String fileContent, String... expectedLookups) {
        if (fileContent != null) {
            myFixture.configureByText("test.bal", fileContent);
        }
        myFixture.complete(CompletionType.BASIC, 1);
        List<String> lookupElementStrings = myFixture.getLookupElementStrings();
        assertNotNull(lookupElementStrings);
        assertSameElements(lookupElementStrings, expectedLookups);
    }

    private void doCheckResult(@NotNull String relativePath, @NotNull String before, String after,
                               @Nullable Character c, String... expectedLookups) {
        PsiFile testFile = myFixture.addFileToProject(relativePath, before);
        myFixture.configureFromExistingVirtualFile(testFile.getVirtualFile());
        myFixture.completeBasic();
        if (c != null) {
            myFixture.type(c);
        }
        if (after != null) {
            myFixture.checkResult(after);
        } else {
            doTest(null, expectedLookups);
        }
    }
}
