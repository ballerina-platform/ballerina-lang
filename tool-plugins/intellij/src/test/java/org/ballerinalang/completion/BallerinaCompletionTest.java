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

import com.intellij.psi.PsiFile;

import java.util.LinkedList;
import java.util.List;

/**
 * Code completion test.
 */
public class BallerinaCompletionTest extends BallerinaCompletionTestBase {

    private static final String UTILS_PACKAGE_NAME = "org/test/utils.bal";
    private static final String SAMPLE_UTIL_FUNCTIONS = "package org.test; public function getA(){} public function " +
            "getB(){}";

    /**
     * Test package declaration level lookups.
     */
    public void testFirstLevelPackageLookups() {
        PsiFile testFile = myFixture.addFileToProject("org/test/file.bal", "package <caret>");
        myFixture.configureFromExistingVirtualFile(testFile.getVirtualFile());
        doTest(null, "org");
    }

    public void testFirstLevelPackageAutoCompletion() {
        doCheckResult("org/test/test.bal", "package o<caret>", "package org.", null);
    }

    public void testFirstLevelPackageInsertHandler() {
        PsiFile testFile = myFixture.addFileToProject("org/test/file.bal", "package org.<caret>");
        myFixture.configureFromExistingVirtualFile(testFile.getVirtualFile());
        doTest(null, "test");
    }

    public void testLastLevelPackageInsertHandler() {
        doCheckResult("org/test/test.bal", "package org.t<caret>", "package org.test;", null);
    }

    /**
     * Test import declaration level lookups.
     */
    //    public void testFirstLevelImportLookups() {
    //        myFixture.addFileToProject("org/test/file.bal", "package org.test; function A(){}");
    //        doTest("import <caret>", "org");
    //    }
    public void testLastLevelImportLookups() {
        myFixture.addFileToProject("org/test/file.bal", "package org.test; function A(){}");
        doTest("import org.<caret>", "test");
    }

    //    public void testFirstLevelImportAutoCompletion() {
    //        myFixture.addFileToProject("org/test/file.bal", "package org.test; function A(){}");
    //        doCheckResult("test.bal", "import o<caret>", "import org.", null);
    //    }

    public void testLastLevelImportAutoCompletion() {
        myFixture.addFileToProject("org/test/file.bal", "package org.test; function A(){}");
        doCheckResult("test.bal", "import org.t<caret>", "import org.test;", null);
    }

    /**
     * Test annotation level lookups.
     */
    //    public void testAnnotationIdentifier() {
    //        doTest("annotation <caret>");
    //    }
    //
    //    public void testAnnotationAttachKeyword() {
    //        doTest("annotation A <caret>", "attach");
    //    }
    //
    //    public void testAnnotationAttachmentPoints() {
    //        doTest("annotation A attach <caret>", "service", "connector", "action", "function", "struct", "const",
    //                "parameter", "annotation", "resource");
    //    }
    //
    //    public void testMultipleAnnotationAttachmentPoints() {
    //        doTest("annotation A attach service, <caret>", "service", "connector", "action", "function", "struct",
    //                "const", "parameter", "annotation", "resource");
    //    }

    /**
     * Test annotation field lookups.
     */
    //    public void testAnnotationFields() {
    //        doCheckResult("test.bal", "annotation TEST attach function { string key; string value;} " +
    //                "@TEST{<caret>} function A(){}", null, null, "key", "value");
    //    }
    //
    //    public void testAnnotationFieldsAutoCompletion() {
    //        doCheckResult("test.bal", "annotation TEST attach function { string key; string value;} " +
    //                "@TEST{k<caret>} function A(){}", "annotation TEST attach function { string key; string value;}
    // " +
    //                "@TEST{key:} function A(){}", null);
    //    }
    //
    //    public void testAnnotationFieldValues() {
    //        doCheckResult("test.bal", "annotation TEST attach function { string key; string value;} " +
    //                "@TEST{key:<caret>} function A(){}", null, null, "true", "false", "null");
    //    }
    //
    //    public void testAnnotationFieldsFromDifferentPackage() {
    //        myFixture.addFileToProject("org/test/file.bal", "public annotation TEST attach function { string key; " +
    //                "string value;}");
    //        doTest("import org.test; @test:TEST{<caret>} function A(){}", "key", "value");
    //    }
    //
    //    public void testAnnotationFieldsAutoCompletionFromDifferentPackage() {
    //        myFixture.addFileToProject("org/test/file.bal", "public annotation TEST attach function { string key;
    // string " +
    //                "value;}");
    //        doCheckResult("main.bal", "import org.test; @test:TEST{k<caret>} function A(){}",
    //                "import org.test; @test:TEST{key:<caret>} function A(){}", null);
    //    }
    //
    //    public void testAnnotationFieldValuesFromDifferentPackage() {
    //        myFixture.addFileToProject("org/test/file.bal", "annotation TEST attach function { string key; string
    // value;}");
    //        doTest("import org.test; @test:TEST{key:<caret>} function A(){}", "true", "false", "null");
    //    }

    /**
     * Test function level lookups.
     */
    //    public void testFunctionIdentifier() {
    //        doTest("function <caret>");
    //    }
    //
    //    public void testFunctionAnnotation() {
    //        doCheckResult("test.bal", "@<caret> function A(){}", null, '@');
    //    }
    //
    //    public void testFunctionAnnotationWithImports() {
    //        myFixture.addFileToProject("org/test/file.bal", "string s = \"\";");
    //        doCheckResult("test.bal", "import org.test; <caret>function A(){}", null, '@', "test");
    //    }
    //
    //    public void testFunctionAnnotationWithImportsNoAnnotationDefinitions() {
    //        myFixture.addFileToProject("org/test/file.bal", "function test(){}");
    //        doCheckResult("test.bal", "import org.test; @test<caret> function A(){}", null, ':');
    //    }
    //
    //    public void testFunctionAnnotationWithImportsWithNoMatchingAnnotationDefinitions() {
    //        myFixture.addFileToProject("org/test/file.bal", "annotation TEST attach test {}");
    //        doCheckResult("test.bal", "import org.test; @test:<caret> function A(){}", null, null);
    //    }
    //
    //    public void testFunctionAnnotationWithImportsWithMatchingAnnotationDefinitions() {
    //        myFixture.addFileToProject("org/test/file.bal", "package org.test; public annotation TEST attach
    // function {} " +
    //                "annotation TEST2 attach resource {}");
    //        doCheckResult("test.bal", "import org.test; @test:<caret> function A(){}", null, null, "TEST");
    //    }
    //
    //    public void testFunctionAnnotationWithImportsWithMatchingAnnotationDefinitionsAutoCompletion() {
    //        myFixture.addFileToProject("org/test/file.bal", "package org.test; public annotation TEST attach
    // function {}");
    //        doCheckResult("test.bal", "import org.test; @test:T<caret> function A(){}",
    //                "import org.test; @test:TEST {} function A(){}", null);
    //    }
    //
    //    public void testFunctionAnnotationInCurrentPackageSameFile() {
    //        doCheckResult("test.bal", "annotation TEST attach function {} <caret> function A(){}", null, '@', "TEST");
    //    }
    //
    //    public void testFunctionAnnotationInCurrentPackageSameFileAutoComplete() {
    //        doCheckResult("test.bal", "annotation TEST attach function {} @T<caret> function A(){}",
    //                "annotation TEST attach function {} @TEST {} function A(){}", null);
    //    }
    //
    //    public void testFunctionAnnotationInCurrentPackageDifferentFile() {
    //        myFixture.addFileToProject("file.bal", "annotation TEST attach function {}");
    //        doCheckResult("test.bal", "<caret> function A(){}", null, '@', "TEST");
    //    }
    //
    //    public void testFunctionAnnotationInCurrentPackageDifferentFileAutoComplete() {
    //        myFixture.addFileToProject("file.bal", "annotation TEST attach function {}");
    //        doCheckResult("test.bal", "@T<caret> function A(){}", "@TEST {} function A(){}", null);
    //    }
    //
    //    public void testFunctionAnnotationInCurrentPackageDifferentFileHasMoreDefinitionsAfter() {
    //        myFixture.addFileToProject("file.bal", "annotation TEST attach function {}");
    //        doCheckResult("test.bal", "<caret> function A(){} service R{}", null, '@', "TEST");
    //    }
    //
    //    public void testFunctionAnnotationInCurrentPackageSameFileHasMoreDefinitionsAfter() {
    //        doCheckResult("test.bal", "annotation TEST attach function {} <caret> function A(){} service R{}",
    // null, '@',
    //                "TEST");
    //    }
    //
    //    public void testFunctionBodyWithoutParamsAndImports() {
    //        List<String> functionLevelSuggestions = Collections.singletonList("test");
    //        List<String> expectedLookups = new LinkedList<>();
    //        expectedLookups.addAll(DATA_TYPES);
    //        expectedLookups.addAll(OTHER_TYPES);
    //        expectedLookups.addAll(XMLNS_TYPE);
    //        expectedLookups.addAll(REFERENCE_TYPES);
    //        expectedLookups.addAll(COMMON_KEYWORDS);
    //        expectedLookups.addAll(FUNCTION_LEVEL_KEYWORDS);
    //        expectedLookups.addAll(functionLevelSuggestions);
    //        doTest("function test () { <caret> }", expectedLookups.toArray(new String[expectedLookups.size()]));
    //    }
    //
    //    public void testFunctionBodyWithParams() {
    //        List<String> functionLevelSuggestions = Arrays.asList("test", "arg");
    //        List<String> expectedLookups = new LinkedList<>();
    //        expectedLookups.addAll(DATA_TYPES);
    //        expectedLookups.addAll(OTHER_TYPES);
    //        expectedLookups.addAll(XMLNS_TYPE);
    //        expectedLookups.addAll(REFERENCE_TYPES);
    //        expectedLookups.addAll(COMMON_KEYWORDS);
    //        expectedLookups.addAll(FUNCTION_LEVEL_KEYWORDS);
    //        expectedLookups.addAll(functionLevelSuggestions);
    //        doTest("function test (int arg) { <caret> }", expectedLookups.toArray(new String[expectedLookups.size()
    // ]));
    //    }
    //
    //    public void testFunctionBodyWithConst() {
    //        List<String> functionLevelSuggestions = Arrays.asList("test", "arg", "GREETING");
    //        List<String> expectedLookups = new LinkedList<>();
    //        expectedLookups.addAll(DATA_TYPES);
    //        expectedLookups.addAll(OTHER_TYPES);
    //        expectedLookups.addAll(XMLNS_TYPE);
    //        expectedLookups.addAll(REFERENCE_TYPES);
    //        expectedLookups.addAll(COMMON_KEYWORDS);
    //        expectedLookups.addAll(FUNCTION_LEVEL_KEYWORDS);
    //        expectedLookups.addAll(functionLevelSuggestions);
    //        doTest("const string GREETING = \"Hello\"; function test (int arg) { <caret> }",
    //                expectedLookups.toArray(new String[expectedLookups.size()]));
    //    }
    //
    //    public void testFunctionBodyWithImports() {
    //        List<String> expectedLookups = new LinkedList<>();
    //        expectedLookups.addAll(DATA_TYPES);
    //        expectedLookups.addAll(OTHER_TYPES);
    //        expectedLookups.addAll(XMLNS_TYPE);
    //        expectedLookups.addAll(REFERENCE_TYPES);
    //        expectedLookups.addAll(COMMON_KEYWORDS);
    //        expectedLookups.addAll(FUNCTION_LEVEL_KEYWORDS);
    //        expectedLookups.add("test");
    //        expectedLookups.add("test");
    //        myFixture.addFileToProject("org/test/file.bal", "string s = \"\";");
    //        doTest("import org.test; function test () { <caret> }",
    //                expectedLookups.toArray(new String[expectedLookups.size()]));
    //    }
    public void testFunctionBodyWithCommonKeywords() {
        List<String> expectedLookups = new LinkedList<>();
        expectedLookups.add("int");
        expectedLookups.add("if");
        expectedLookups.add("string");
        expectedLookups.add("join");
        expectedLookups.add("timeout");
        expectedLookups.add("transaction");
        expectedLookups.add("finally");
        expectedLookups.add("while");
        expectedLookups.add("failed");
        expectedLookups.add("in");
        doTest("function test () { i<caret> }", expectedLookups.toArray(new String[expectedLookups.size()]));
    }

    public void testFunctionBodyWithFunctionLevelKeywords() {
        doTest("function test () { r<caret> }", "return", "string", "fork", "worker", "transaction",
                "abort", "try", "break", "foreach", "throw", "var");
    }

    //    public void testInvokingFunctionInDifferentFile1() {
    //        myFixture.addFileToProject("file.bal", "function test(){}");
    //        doCheckResult("test.bal", "function main(string... args){ tes<caret> }",
    //                "function main(string... args){ test() }", null);
    //    }
    //
    //    public void testInvokingFunctionInDifferentFile2() {
    //        myFixture.addFileToProject("file.bal", "function test1(){} function test2(){}");
    //        doTest("function main(string... args){ tes<caret> }", "test1", "test2");
    //    }

    public void testVariable1() {
        doTest("function main(string... args){ int <caret> }");
    }

    //    public void testVariable2() {
    //        doTest("function main(string... args){ int a = <caret> }", "args", "main", "create", "false", "null",
    // "true",
    //                "lengthof", "typeof");
    //    }

    public void testCreateKeywordAutoCompletion() {
        doCheckResult("test.bal", "function main(string... args){ http:ClientConnector con = cr<caret> }",
                "function main(string... args){ http:ClientConnector con = create }", null);
    }

    //    public void testInvocationInFunctionWithTraileringCode() {
    //        List<String> expectedLookups = new LinkedList<>();
    //        expectedLookups.addAll(DATA_TYPES);
    //        expectedLookups.addAll(OTHER_TYPES);
    //        expectedLookups.addAll(XMLNS_TYPE);
    //        expectedLookups.addAll(REFERENCE_TYPES);
    //        expectedLookups.addAll(COMMON_KEYWORDS);
    //        expectedLookups.addAll(VALUE_KEYWORDS);
    //        expectedLookups.add("test");
    //        expectedLookups.add("main");
    //        expectedLookups.add("args");
    //        expectedLookups.add("return");
    //        myFixture.addFileToProject(UTILS_PACKAGE_NAME, SAMPLE_UTIL_FUNCTIONS);
    //        doTest("import org.test; function main(string... args){ <caret> \ntest:getA(); }",
    //                expectedLookups.toArray(new String[expectedLookups.size()]));
    //    }
    //
    //    public void testPackageInvocationInFunction() {
    //        myFixture.addFileToProject(UTILS_PACKAGE_NAME, SAMPLE_UTIL_FUNCTIONS);
    //        doTest("import org.test; function main(string... args){ test:<caret> }", "getA", "getB");
    //    }
    //
    //    public void testPackageInvocationInFunctionHasTraileringCode() {
    //        myFixture.addFileToProject(UTILS_PACKAGE_NAME, SAMPLE_UTIL_FUNCTIONS);
    //        doTest("import org.test; function main(string... args){ test:<caret> \ntest:getA();}", "getA", "getB");
    //    }
    //
    //    public void testPackageInvocationInFunctionWithPartialIdentifier() {
    //        myFixture.addFileToProject(UTILS_PACKAGE_NAME, SAMPLE_UTIL_FUNCTIONS);
    //        doTest("import org.test; function main(string... args){ test:g<caret> }", "getA", "getB");
    //    }
    //
    //    public void testPackageInvocationInFunctionWithPartialIdentifierHasTraileringCode() {
    //        myFixture.addFileToProject(UTILS_PACKAGE_NAME, SAMPLE_UTIL_FUNCTIONS);
    //        doTest("import org.test; function main(string... args){ test:g<caret> \ntest:getA(); }", "getA", "getB");
    //    }
    //
    //    public void testPackageInvocationAsParamInFunction() {
    //        myFixture.addFileToProject(UTILS_PACKAGE_NAME, SAMPLE_UTIL_FUNCTIONS);
    //        doTest("import org.test; function main(string... args){ test(test:<caret>) } function test(string s){}",
    //                "getA", "getB");
    //    }
    //
    //    public void testPackageInvocationAsParamInFunctionWithTraileringCode() {
    //        myFixture.addFileToProject(UTILS_PACKAGE_NAME, SAMPLE_UTIL_FUNCTIONS);
    //        doTest("import org.test; function main(string... args){ test(test:<caret>) \ntest:getA(); }" +
    //                " function test(string s){}", "getA", "getB");
    //    }
    //
    //    public void testPackageInvocationAsParamWithTraileringCodeInFunction() {
    //        myFixture.addFileToProject(UTILS_PACKAGE_NAME, SAMPLE_UTIL_FUNCTIONS);
    //        doTest("import org.test; function main(string... args){ test(test:<caret> test:getA()) }" +
    //                " function test(string s){}", "getA", "getB");
    //    }
    //
    //    public void testPackageInvocationAsParamWithTraileringCodeInFunctionWithTraileringCode() {
    //        myFixture.addFileToProject(UTILS_PACKAGE_NAME, SAMPLE_UTIL_FUNCTIONS);
    //        doTest("import org.test; function main(string... args){ test(test:<caret> test:getA()) \ntest:getA(); }" +
    //                " function test(string s){}", "getA", "getB");
    //    }
    //
    //    public void testPackageInvocationAsParamInFunctionWithPartialIdentifier() {
    //        myFixture.addFileToProject(UTILS_PACKAGE_NAME, SAMPLE_UTIL_FUNCTIONS);
    //        doTest("import org.test; function main(string... args){ test(test:g<caret>) } function test(string s){}",
    //                "getA", "getB");
    //    }
    //
    //    public void testPackageInvocationAsParamInFunctionWithPartialIdentifierWithTraileringCode() {
    //        myFixture.addFileToProject(UTILS_PACKAGE_NAME, SAMPLE_UTIL_FUNCTIONS);
    //        doTest("import org.test; function main(string... args){ test(test:g<caret>) \ntest:getA();} " +
    //                "function test(string s){}", "getA", "getB");
    //    }
    //
    //    public void testPackageInvocationAsParamWithTraileringCodeInFunctionWithPartialIdentifier() {
    //        myFixture.addFileToProject(UTILS_PACKAGE_NAME, SAMPLE_UTIL_FUNCTIONS);
    //        doTest("import org.test; function main(string... args){ test(test:g<caret> test:getA()) } " +
    //                "function test(string s){}", "getA", "getB");
    //    }
    //
    //    public void testPackageInvocationAsParamWithTraileringCodeInFunctionWithPartialIdentifierWithTraileringCode
    // () {
    //        myFixture.addFileToProject(UTILS_PACKAGE_NAME, SAMPLE_UTIL_FUNCTIONS);
    //        doTest("import org.test; function main(string... args){ test(test:g<caret> test:getA()) \ntest:getA(); }
    // " +
    //                "function test(string s){}", "getA", "getB");
    //    }
    //
    //    public void testFunctionFromPackageInvocation5() {
    //        myFixture.addFileToProject(UTILS_PACKAGE_NAME, SAMPLE_UTIL_FUNCTIONS);
    //        doTest("import org.test; function main(string... args){ test(\"TEST\"+test:<caret>) } " +
    //                "function test(string s){}", "getA", "getB");
    //    }
    //
    //    public void testFunctionFromPackageInvocation6() {
    //        myFixture.addFileToProject(UTILS_PACKAGE_NAME, SAMPLE_UTIL_FUNCTIONS);
    //        doTest("import org.test; function main(string... args){ test(\"TEST\"+test:g<caret>) } " +
    //                "function test(string s){}", "getA", "getB");
    //    }
    //
    //    public void testFunctionFromPackageInvocation7() {
    //        myFixture.addFileToProject(UTILS_PACKAGE_NAME, SAMPLE_UTIL_FUNCTIONS);
    //        doTest("import org.test; function main(string... args){ test(\"TEST\" + test:<caret>) } " +
    //                "function test(string s){}", "getA", "getB");
    //    }
    //
    //    public void testFunctionFromPackageInvocation8() {
    //        myFixture.addFileToProject(UTILS_PACKAGE_NAME, SAMPLE_UTIL_FUNCTIONS);
    //        doTest("import org.test; function main(string... args){ test(\"TEST\" + test:g<caret>) } " +
    //                "function test(string s){}", "getA", "getB");
    //    }
    //
    //    public void testFunctionFromPackageInvocation9() {
    //        myFixture.addFileToProject(UTILS_PACKAGE_NAME, SAMPLE_UTIL_FUNCTIONS);
    //        doTest("import org.test; function main(string... args){ test(\"TEST\"+test:<caret>+\"TEST\") } " +
    //                "function test(string s){}", "getA", "getB");
    //    }
    //
    //    public void testFunctionFromPackageInvocation10() {
    //        myFixture.addFileToProject(UTILS_PACKAGE_NAME, SAMPLE_UTIL_FUNCTIONS);
    //        doTest("import org.test; function main(string... args){ test(\"TEST\"+test:g<caret>+\"TEST\") } " +
    //                "function test(string s){}", "getA", "getB");
    //    }
    //
    //    public void testFunctionFromPackageInvocation11() {
    //        myFixture.addFileToProject(UTILS_PACKAGE_NAME, SAMPLE_UTIL_FUNCTIONS);
    //        doTest("import org.test; function main(string... args){ test(\"TEST\"+test:<caret> +\"TEST\") } " +
    //                "function test(string s){}", "getA", "getB");
    //    }
    //
    //    public void testFunctionFromPackageInvocation12() {
    //        myFixture.addFileToProject(UTILS_PACKAGE_NAME, SAMPLE_UTIL_FUNCTIONS);
    //        doTest("import org.test; function main(string... args){ test(\"TEST\"+test:g<caret> +\"TEST\") }",
    //                "getA", "getB");
    //    }
    //
    //    public void testFunctionFromPackageInvocation13() {
    //        myFixture.addFileToProject(UTILS_PACKAGE_NAME, SAMPLE_UTIL_FUNCTIONS);
    //        doTest("import org.test; function main(string... args){ string s =test:<caret> }",
    //                "getA", "getB");
    //    }
    //
    //    public void testFunctionFromPackageInvocation14() {
    //        myFixture.addFileToProject(UTILS_PACKAGE_NAME, SAMPLE_UTIL_FUNCTIONS);
    //        doTest("import org.test; function main(string... args){ string s =test:g<caret> }",
    //                "getA", "getB");
    //    }
    //
    //    public void testFunctionFromPackageInvocation15() {
    //        myFixture.addFileToProject(UTILS_PACKAGE_NAME, SAMPLE_UTIL_FUNCTIONS);
    //        doTest("import org.test; function main(string... args){ string s = test:<caret> }",
    //                "getA", "getB");
    //    }
    //
    //    public void testFunctionFromPackageInvocation16() {
    //        myFixture.addFileToProject(UTILS_PACKAGE_NAME, SAMPLE_UTIL_FUNCTIONS);
    //        doTest("import org.test; function main(string... args){ string s = test:g<caret> }",
    //                "getA", "getB");
    //    }
    //
    //    public void testFunctionFromPackageInvocation17() {
    //        myFixture.addFileToProject(UTILS_PACKAGE_NAME, SAMPLE_UTIL_FUNCTIONS);
    //        doTest("import org.test; function main(string... args){ string s = test:<caret>+\"TEST\"; }",
    //                "getA", "getB");
    //    }
    //
    //    public void testFunctionFromPackageInvocation18() {
    //        myFixture.addFileToProject(UTILS_PACKAGE_NAME, SAMPLE_UTIL_FUNCTIONS);
    //        doTest("import org.test; function main(string... args){ string s = test:g<caret>+\"TEST\"; }",
    //                "getA", "getB");
    //    }
    //
    //    public void testFunctionFromPackageInvocation19() {
    //        myFixture.addFileToProject(UTILS_PACKAGE_NAME, SAMPLE_UTIL_FUNCTIONS);
    //        doTest("import org.test; function main(string... args){ string s = \"TEST\"+test:<caret>; }",
    //                "getA", "getB");
    //    }
    //
    //    public void testFunctionFromPackageInvocation20() {
    //        myFixture.addFileToProject(UTILS_PACKAGE_NAME, SAMPLE_UTIL_FUNCTIONS);
    //        doTest("import org.test; function main(string... args){ string s = \"TEST\"+test:g<caret>; }",
    //                "getA", "getB");
    //    }
    //
    //    public void testFunctionFromPackageInvocation21() {
    //        myFixture.addFileToProject(UTILS_PACKAGE_NAME, SAMPLE_UTIL_FUNCTIONS);
    //        doTest("import org.test; function main(string... args){ string s = test:getA()+test:<caret> \"TEST\"; }",
    //                "getA", "getB");
    //    }
    //
    //    public void testFunctionFromPackageInvocation22() {
    //        myFixture.addFileToProject(UTILS_PACKAGE_NAME, SAMPLE_UTIL_FUNCTIONS);
    //        doTest("import org.test; function main(string... args){ string s = test:getA()+test:g<caret> \"TEST\"; }",
    //                "getA", "getB");
    //    }
    //
    //    public void testFunctionFromPackageInvocation23() {
    //        myFixture.addFileToProject(UTILS_PACKAGE_NAME, SAMPLE_UTIL_FUNCTIONS);
    //        doTest("import org.test; function main(string... args){ string s = test:getA()+<caret> \"TEST\"; }",
    //                "args", "main", "test", "true", "false", "null");
    //    }

    public void testFunctionFromPackageInvocation24() {
        myFixture.addFileToProject(UTILS_PACKAGE_NAME, SAMPLE_UTIL_FUNCTIONS);
        doTest("import org.test; function main(string... args){ string s = test:getA()+g<caret> \"TEST\"; }",
                "args");
    }

    //    public void testFunctionFromPackageInvocation25() {
    //        myFixture.addFileToProject(UTILS_PACKAGE_NAME, SAMPLE_UTIL_FUNCTIONS);
    //        doTest("import org.test; function main(string... args){ test:<caret> \ntest:geA();}", "getA", "getB");
    //    }
    //
    //    public void testFunctionFromPackageInvocation26() {
    //        myFixture.addFileToProject(UTILS_PACKAGE_NAME, SAMPLE_UTIL_FUNCTIONS);
    //        doTest("import org.test; function main(string... args){ test:g<caret> \ntest:getA();}", "getA", "getB");
    //    }
    //
    //    public void testVarDefinition() {
    //        myFixture.addFileToProject(UTILS_PACKAGE_NAME, SAMPLE_UTIL_FUNCTIONS);
    //        doTest("import org.test; function main(string... args){ string s = <caret> }",
    //                "args", "main", "test", "create", "false", "null", "true", "lengthof", "typeof");
    //    }
    //
    //    public void testVarDefinitionWithTraileringCode() {
    //        myFixture.addFileToProject(UTILS_PACKAGE_NAME, SAMPLE_UTIL_FUNCTIONS);
    //        doTest("import org.test; function main(string... args){ string s = \"TEST\" + <caret> }",
    //                "args", "main", "test", "true", "false", "null");
    //    }
    //
    //    public void testVarDefinitionWithLeadingCode() {
    //        myFixture.addFileToProject(UTILS_PACKAGE_NAME, SAMPLE_UTIL_FUNCTIONS);
    //        doTest("import org.test; function main(string... args){ string s = <caret> + \"TEST\" }",
    //                "args", "main", "test", "create", "lengthof", "typeof", "true", "false", "null");
    //    }

    public void testConnectorInit() {
        myFixture.addFileToProject("org/test/con.bal", "public connector TestConnector{}");
        doTest("import org.test; function A(){ test:<caret> }", "TestConnector");
    }

    public void testConnectorAutoCompletion() {
        myFixture.addFileToProject("org/test/con.bal", "public connector TestConnector{}");
        doCheckResult("file.bal", "import org.test; function A(){ test:T<caret> }",
                "import org.test; function A(){ test:TestConnector }", null);
    }

    //    public void testConnectorInitCreateKeyword() {
    //        myFixture.addFileToProject("org/test/con.bal", "connector TestConnector{}");
    //        doTest("import org.test; function A(){ test:TestConnector c = <caret> }", "create", "test", "A", "false",
    //                "null", "true", "lengthof", "typeof");
    //    }

    public void testConnectorInitCreateKeywordAutoCompletion() {
        myFixture.addFileToProject("org/test/con.bal", "connector TestConnector{}");
        doCheckResult("test.bal", "import org.test; function A(){ test:TestConnector c = c<caret> }",
                "import org.test; function A(){ test:TestConnector c = create }", null);
    }

    //    public void testConnectorCreation() {
    //        myFixture.addFileToProject("org/test/con.bal", "connector TestConnector{}");
    //        doTest("import org.test; function A(){ test:TestConnector c = create <caret> }", "test");
    //    }

    //    public void testConnectorCreationPackageAutoCompletion() {
    //        myFixture.addFileToProject("org/test/con.bal", "connector TestConnector{}");
    //        doCheckResult("test.bal", "import org.test; function A(){ test:TestConnector con = create " +
    //                "tes<caret> }", "import org.test; function A(){ test:TestConnector con = create test: }", null);
    //    }
    //
    //    public void testConnectorCreationCreateKeyword() {
    //        myFixture.addFileToProject("org/test/con.bal", "connector TestConnector{}");
    //        doTest("import org.test; function A(){ test:TestConnector c = <caret> test:TestConnector() }",
    //                "create", "A", "test", "lengthof", "typeof", "true", "false", "null");
    //    }
    //
    //    public void testVariablesInitializationAfterDeclaration() {
    //        List<String> expectedLookups = new LinkedList<>();
    //        expectedLookups.addAll(DATA_TYPES);
    //        expectedLookups.addAll(OTHER_TYPES);
    //        expectedLookups.addAll(XMLNS_TYPE);
    //        expectedLookups.addAll(REFERENCE_TYPES);
    //        expectedLookups.addAll(COMMON_KEYWORDS);
    //        expectedLookups.addAll(VALUE_KEYWORDS);
    //        expectedLookups.addAll(FUNCTION_LEVEL_KEYWORDS);
    //        expectedLookups.add("s");
    //        expectedLookups.add("A");
    //        doTest("function A(){ string s; <caret> }", expectedLookups.toArray(new String[expectedLookups.size()]));
    //    }
    //
    //    public void testVariablesLaterInitialization() {
    //        List<String> expectedLookups = new LinkedList<>();
    //        expectedLookups.addAll(DATA_TYPES);
    //        expectedLookups.addAll(OTHER_TYPES);
    //        expectedLookups.addAll(XMLNS_TYPE);
    //        expectedLookups.addAll(REFERENCE_TYPES);
    //        expectedLookups.addAll(COMMON_KEYWORDS);
    //        expectedLookups.addAll(VALUE_KEYWORDS);
    //        expectedLookups.addAll(FUNCTION_LEVEL_KEYWORDS);
    //        expectedLookups.add("s");
    //        expectedLookups.add("s1");
    //        expectedLookups.add("A");
    //        doTest("function A(){ string s; string s1; <caret> }",
    //                expectedLookups.toArray(new String[expectedLookups.size()]));
    //    }
    //
    //    public void testVariablesNoVarsAvailable() {
    //        doTest("function A(){ string s1 = <caret> }", "A", "create", "false", "null", "true", "lengthof",
    // "typeof");
    //    }
    //
    //    public void testVariablesWhenSingleVariableAvailable() {
    //        doTest("function A(){ string s1 = \"Test\"; string s2 = <caret> }", "s1", "A", "create", "false", "null",
    //                "true", "lengthof", "typeof");
    //    }

    public void testVariablesWhenSingleVariableAvailableWithPartialIdentifier() {
        doCheckResult("test.bal", "function A(){ string abc = \"Test\"; string def = ab<caret> }",
                "function A(){ string abc = \"Test\"; string def = abc }", null);
    }

    //    public void testVariablesWhenMultipleVariablesAvailable() {
    //        doTest("function A(){ string s1 = \"Test\"; string s2 = \"Test\"; string s3 = <caret> }",
    //                "s1", "s2", "A", "create", "false", "null", "true", "lengthof", "typeof");
    //    }
    //
    //    public void testVariablesWhenMultipleVariablesAvailableAfterLeafElement() {
    //        doTest("function A(){ string s1 = \"Test\"; string s2 = \"Test\"; string s3 = s1 + <caret> }",
    //                "s1", "s2", "A", "true", "false", "null");
    //    }
    //
    //    public void testVariablesWhenMultipleVariablesAvailableBeforeLeafElement() {
    //        doTest("function A(){ string s1 = \"Test\"; string s2 = \"Test\"; string s3 = <caret> + s2; }",
    //                "s1", "s2", "A", "create", "lengthof", "typeof", "true", "false", "null");
    //    }
    //
    //    public void testVariablesInNewLineWhenMultipleVariablesAvailable() {
    //        List<String> expectedLookups = new LinkedList<>();
    //        expectedLookups.addAll(DATA_TYPES);
    //        expectedLookups.addAll(OTHER_TYPES);
    //        expectedLookups.addAll(XMLNS_TYPE);
    //        expectedLookups.addAll(REFERENCE_TYPES);
    //        expectedLookups.addAll(COMMON_KEYWORDS);
    //        expectedLookups.addAll(VALUE_KEYWORDS);
    //        expectedLookups.addAll(FUNCTION_LEVEL_KEYWORDS);
    //        expectedLookups.add("s1");
    //        expectedLookups.add("s2");
    //        expectedLookups.add("A");
    //        doTest("function A(){ string s1 = \"Test\"; string s2 = \"Test\";\n <caret> }",
    //                expectedLookups.toArray(new String[expectedLookups.size()]));
    //    }
    //
    //    public void testVariablesInNewLineWhenMultipleVariablesAvailableWithVariablesAfter() {
    //        List<String> expectedLookups = new LinkedList<>();
    //        expectedLookups.addAll(DATA_TYPES);
    //        expectedLookups.addAll(OTHER_TYPES);
    //        expectedLookups.addAll(XMLNS_TYPE);
    //        expectedLookups.addAll(REFERENCE_TYPES);
    //        expectedLookups.addAll(COMMON_KEYWORDS);
    //        expectedLookups.addAll(VALUE_KEYWORDS);
    //        expectedLookups.addAll(FUNCTION_LEVEL_KEYWORDS);
    //        expectedLookups.add("s1");
    //        expectedLookups.add("s2");
    //        expectedLookups.add("A");
    //        doTest("function A(){ string s1 = \"Test\"; string s2 = \"Test\";\n <caret> \nstring s4 = \"\";}",
    //                expectedLookups.toArray(new String[expectedLookups.size()]));
    //    }
    //
    //    public void testTypesAfterRBRACE() {
    //        List<String> expectedLookups = new LinkedList<>();
    //        expectedLookups.add("else");
    //        expectedLookups.add("json");
    //        expectedLookups.add("message");
    //        expectedLookups.add("string");
    //        expectedLookups.add("test");
    //        expectedLookups.add("transaction");
    //        expectedLookups.addAll(XMLNS_TYPE);
    //        doTest("function test(){ if(a==a){}\n s<caret> \nint a; }",
    //                expectedLookups.toArray(new String[expectedLookups.size()]));
    //    }

    /**
     * Test statement level lookups.
     */
    public void testStrings() {
        doTest("function test() { int a; system:println(\"<caret>\") }");
    }

    /**
     * Test service level lookups.
     */
    public void testServiceIdentifier() {
        doTest("service <caret>");
    }

    public void testServiceBody() {
        List<String> expectedLookups = new LinkedList<>();
        expectedLookups.addAll(DATA_TYPES);
        expectedLookups.addAll(OTHER_TYPES);
        expectedLookups.addAll(XMLNS_TYPE);
        expectedLookups.addAll(REFERENCE_TYPES);
        expectedLookups.add("resource");
        doTest("service<http> S{<caret>}", expectedLookups.toArray(new String[expectedLookups.size()]));
    }

    //    public void testServiceBodyAfterAnnotation() {
    //        doTest("service<http> S{ @http:GET {} <caret>}", "resource");
    //    }
    //
    //    public void testServiceAnnotation() {
    //        doCheckResult("test.bal", "@<caret> service S{}", null, '@');
    //    }
    //
    //    public void testServiceAnnotationWithImports() {
    //        myFixture.addFileToProject("org/test/file.bal", "string s = \"\";");
    //        doCheckResult("test.bal", "import org.test; <caret>service S{}", null, '@', "test");
    //    }
    //
    //    public void testServiceAnnotationWithImportsNoAnnotationDefinitions() {
    //        myFixture.addFileToProject("org/test/file.bal", "function test(){}");
    //        doCheckResult("test.bal", "import org.test; @test<caret> service S{}", null, ':');
    //    }
    //
    //    public void testServiceAnnotationWithImportsWithNoMatchingAnnotationDefinitions() {
    //        myFixture.addFileToProject("org/test/file.bal", "annotation TEST attach test {}");
    //        doCheckResult("test.bal", "import org.test; @test:<caret> service S{}", null, null);
    //    }
    //
    //    public void testServiceAnnotationWithImportsWithMatchingAnnotationDefinitions() {
    //        myFixture.addFileToProject("org/test/file.bal", "package org.test; public annotation TEST attach
    // service {} " +
    //                "annotation TEST2 attach resource {}");
    //        doCheckResult("test.bal", "import org.test; @test:<caret> service S{}", null, null, "TEST");
    //    }
    //
    //    public void testServiceAnnotationWithImportsWithMatchingAnnotationDefinitionsAutoCompletion() {
    //        myFixture.addFileToProject("org/test/file.bal", "package org.test; public annotation TEST attach
    // service {}");
    //        doCheckResult("test.bal", "import org.test; @test:T<caret> service S{}",
    //                "import org.test; @test:TEST {} service S{}", null);
    //    }
    //
    //    public void testServiceAnnotationInCurrentPackageSameFile() {
    //        doCheckResult("test.bal", "annotation TEST attach service {} <caret> service S{}", null, '@', "TEST");
    //    }
    //
    //    public void testServiceAnnotationInCurrentPackageSameFileAutoComplete() {
    //        doCheckResult("test.bal", "annotation TEST attach service {} @T<caret> service S{}",
    //                "annotation TEST attach service {} @TEST {} service S{}", null);
    //    }
    //
    //    public void testServiceAnnotationInCurrentPackageDifferentFile() {
    //        myFixture.addFileToProject("file.bal", "annotation TEST attach service {}");
    //        doCheckResult("test.bal", "<caret> service S{}", null, '@', "TEST");
    //    }
    //
    //    public void testServiceAnnotationInCurrentPackageDifferentFileAutoComplete() {
    //        myFixture.addFileToProject("file.bal", "annotation TEST attach service {}");
    //        doCheckResult("test.bal", "@T<caret> service S{}", "@TEST {} service S{}", null);
    //    }
    //
    //    public void testServiceAnnotationInCurrentPackageDifferentFileHasMoreDefinitionsAfter() {
    //        myFixture.addFileToProject("file.bal", "annotation TEST attach service {}");
    //        doCheckResult("test.bal", "<caret> service S{} service R{}", null, '@', "TEST");
    //    }
    //
    //    public void testServiceAnnotationInCurrentPackageSameFileHasMoreDefinitionsAfter() {
    //        doCheckResult("test.bal", "annotation TEST attach service {} <caret> service S{} service R{}", null, '@',
    //                "TEST");
    //    }

    public void testServiceSourceNotation() {
        myFixture.addFileToProject("org/test/file.bal", "string s = \"\";");
        doTest("import org.test; service<<caret>> S {}", "test");
    }

    //    public void testServiceSourceNotationAutoCompletion() {
    //        myFixture.addFileToProject("org/test/file.bal", "string s = \"\";");
    //        doCheckResult("test.bal", "import org.test; service<t<caret>> S {}",
    //                "import org.test; service<test> S {}", null);
    //    }

    //    /**
    //     * Test resource level lookups.
    //     */
    //    public void testResourceIdentifier() {
    //        doTest("service S { resource <caret> ");
    //    }
    //
    //    public void testResourceAnnotation() {
    //        doCheckResult("test.bal", "service S{<caret>}", null, '@');
    //    }
    //
    //    public void testResourceAnnotationWithImports() {
    //        myFixture.addFileToProject("org/test/file.bal", "string s = \"\";");
    //        doCheckResult("test.bal", "import org.test; service<http> S{<caret>}", null, '@', "test");
    //    }
    //
    //    public void testResourceAnnotationWithImportsWithMatchingAnnotationDefinitions() {
    //        myFixture.addFileToProject("org/test/file.bal", "package org.test; public annotation TEST attach
    // resource {} " +
    //                "annotation TEST2 attach service {}");
    //        doCheckResult("test.bal", "import org.test; service<http> S{@test:<caret>}", null, null, "TEST");
    //    }
    //
    //    public void testResourceAnnotationWithImportsWithMatchingAnnotationDefinitionsAutoCompletion() {
    //        myFixture.addFileToProject("org/test/file.bal", "package org.test; public annotation TEST attach
    // resource {}");
    //        doCheckResult("test.bal", "import org.test; service<http> S{@test:T<caret>}",
    //                "import org.test; service<http> S{@test:TEST {}}", null);
    //    }
    //
    //    public void testResourceAnnotationInCurrentPackageSameFile() {
    //        doCheckResult("test.bal", "annotation TEST attach resource {} service<http> S{<caret>}", null, '@',
    // "TEST");
    //    }
    //
    //    public void testResourceAnnotationInCurrentPackageSameFileAutoComplete() {
    //        doCheckResult("test.bal", "annotation TEST attach resource {} service<http> S{@T<caret>}",
    //                "annotation TEST attach resource {} service<http> S{@TEST {}}", null);
    //    }
    //
    //    public void testResourceAnnotationInCurrentPackageDifferentFile() {
    //        myFixture.addFileToProject("file.bal", "annotation TEST attach resource {}");
    //        doCheckResult("test.bal", "service<http> S{<caret>}", null, '@', "TEST");
    //    }
    //
    //    public void testResourceAnnotationInCurrentPackageDifferentFileAutoComplete() {
    //        myFixture.addFileToProject("file.bal", "annotation TEST attach resource {}");
    //        doCheckResult("test.bal", "service<http> S{@T<caret>}", "service<http> S{@TEST {}}", null);
    //    }
    //
    //    public void testResourceAnnotationInCurrentPackageDifferentFileHasMoreDefinitionsAfter() {
    //        myFixture.addFileToProject("file.bal", "annotation TEST attach resource {}");
    //        doCheckResult("test.bal", "service<http> S{<caret>} service R{}", null, '@', "TEST");
    //    }
    //
    //    public void testResourceAnnotationInCurrentPackageSameFileHasMoreDefinitionsAfter() {
    //        doCheckResult("test.bal", "annotation TEST attach resource {} service<http> S{<caret>} service R{}",
    // null, '@',
    //                "TEST");
    //    }

    /**
     * Test resource parameter level lookups.
     */
    public void testResourceParamIdentifier() {
        doTest("service S { resource R(string <caret>)");
    }

    //    public void testResourceParamWithoutImports() {
    //        List<String> expectedLookups = new LinkedList<>();
    //        expectedLookups.addAll(DATA_TYPES);
    //        expectedLookups.addAll(OTHER_TYPES);
    //        expectedLookups.addAll(XMLNS_TYPE);
    //        expectedLookups.addAll(REFERENCE_TYPES);
    //        doTest("service<http> S { resource R(<caret>)", expectedLookups.toArray(new String[expectedLookups.size
    // ()]));
    //    }

    //    public void testResourceParamWithImports() {
    //        List<String> expectedLookups = new LinkedList<>();
    //        expectedLookups.addAll(DATA_TYPES);
    //        expectedLookups.addAll(OTHER_TYPES);
    //        expectedLookups.addAll(XMLNS_TYPE);
    //        expectedLookups.addAll(REFERENCE_TYPES);
    //        expectedLookups.add("test");
    //        myFixture.addFileToProject("org/test/file.bal", "string s = \"\";");
    //        doTest("import org.test; service<http> S { resource R(<caret>)",
    //                expectedLookups.toArray(new String[expectedLookups.size()]));
    //    }

    //    public void testResourceParamWithoutImportsAutoCompletion() {
    //        doCheckResult("test.bal", "service<http> S { resource R(st<caret>)", "service<http> S { resource R
    // (string )",
    //                null);
    //    }
    //
    //    public void testResourceParamWithImportsAutoCompletion() {
    //        myFixture.addFileToProject("org/test/file.bal", "string s = \"\";");
    //        doCheckResult("test.bal", "import org.test; service<http> S { resource R(te<caret>)",
    //                "import org.test; service<http> S { resource R(test:)", null);
    //    }

    //    public void testCaretAfterResourceParamWithoutImports() {
    //        List<String> expectedLookups = new LinkedList<>();
    //        expectedLookups.addAll(DATA_TYPES);
    //        expectedLookups.addAll(OTHER_TYPES);
    //        expectedLookups.addAll(XMLNS_TYPE);
    //        expectedLookups.addAll(REFERENCE_TYPES);
    //        doTest("service<http> S { resource R(string s,<caret>)",
    //                expectedLookups.toArray(new String[expectedLookups.size()]));
    //    }

    //    public void testCaretBeforeResourceParamWithoutImports() {
    //        List<String> expectedLookups = new LinkedList<>();
    //        expectedLookups.addAll(DATA_TYPES);
    //        expectedLookups.addAll(OTHER_TYPES);
    //        expectedLookups.addAll(XMLNS_TYPE);
    //        expectedLookups.addAll(REFERENCE_TYPES);
    //        doTest("service<http> S { resource R(<caret>string s)",
    //                expectedLookups.toArray(new String[expectedLookups.size()]));
    //    }

    //    public void testCaretAfterResourceParamWithImports() {
    //        List<String> expectedLookups = new LinkedList<>();
    //        expectedLookups.addAll(DATA_TYPES);
    //        expectedLookups.addAll(OTHER_TYPES);
    //        expectedLookups.addAll(XMLNS_TYPE);
    //        expectedLookups.addAll(REFERENCE_TYPES);
    //        expectedLookups.add("test");
    //        myFixture.addFileToProject("org/test/file.bal", "string s = \"\";");
    //        doTest("import org.test; service<http> S { resource R(string s,<caret>)",
    //                expectedLookups.toArray(new String[expectedLookups.size()]));
    //    }

    //    public void testCaretBeforeResourceParamWithImports() {
    //        List<String> expectedLookups = new LinkedList<>();
    //        expectedLookups.addAll(DATA_TYPES);
    //        expectedLookups.addAll(OTHER_TYPES);
    //        expectedLookups.addAll(XMLNS_TYPE);
    //        expectedLookups.addAll(REFERENCE_TYPES);
    //        expectedLookups.add("test");
    //        myFixture.addFileToProject("org/test/file.bal", "string s = \"\";");
    //        doTest("import org.test; service<http> S { resource R(<caret>string s)",
    //                expectedLookups.toArray(new String[expectedLookups.size()]));
    //    }

    /**
     * Test connector level lookups.
     */
    public void testConnectorIdentifier() {
        doTest("connector <caret>");
    }

    //    public void testConnectorAnnotation() {
    //        doCheckResult("test.bal", "@<caret> connector A(){}", null, '@');
    //    }
    //
    //    public void testConnectorAnnotationWithImports() {
    //        myFixture.addFileToProject("org/test/file.bal", "string s = \"\";");
    //        doCheckResult("test.bal", "import org.test; <caret>connector A(){}", null, '@', "test");
    //    }
    //
    //    public void testConnectorAnnotationWithImportsNoAnnotationDefinitions() {
    //        myFixture.addFileToProject("org/test/file.bal", "function test(){}");
    //        doCheckResult("test.bal", "import org.test; @test<caret> connector A(){}", null, ':');
    //    }
    //
    //    public void testConnectorAnnotationWithImportsWithNoMatchingAnnotationDefinitions() {
    //        myFixture.addFileToProject("org/test/file.bal", "annotation TEST attach test {}");
    //        doCheckResult("test.bal", "import org.test; @test:<caret> connector A(){}", null, null);
    //    }
    //
    //    public void testConnectorAnnotationWithImportsWithMatchingAnnotationDefinitions() {
    //        myFixture.addFileToProject("org/test/file.bal", "package org.test; public annotation TEST attach
    // connector {}" +
    //                " " +
    //                "annotation TEST2 attach resource {}");
    //        doCheckResult("test.bal", "import org.test; @test:<caret> connector A(){}", null, null, "TEST");
    //    }
    //
    //    public void testConnectorAnnotationWithImportsWithMatchingAnnotationDefinitionsAutoCompletion() {
    //        myFixture.addFileToProject("org/test/file.bal", "package org.test; public annotation TEST attach
    // connector {}");
    //        doCheckResult("test.bal", "import org.test; @test:T<caret> connector A(){}",
    //                "import org.test; @test:TEST {} connector A(){}", null);
    //    }
    //
    //    public void testConnectorAnnotationInCurrentPackageSameFile() {
    //        doCheckResult("test.bal", "annotation TEST attach connector {} <caret> connector A(){}", null, '@',
    // "TEST");
    //    }
    //
    //    public void testConnectorAnnotationInCurrentPackageSameFileAutoComplete() {
    //        doCheckResult("test.bal", "annotation TEST attach connector {} @T<caret> connector A(){}",
    //                "annotation TEST attach connector {} @TEST {} connector A(){}", null);
    //    }
    //
    //    public void testConnectorAnnotationInCurrentPackageDifferentFile() {
    //        myFixture.addFileToProject("file.bal", "annotation TEST attach connector {}");
    //        doCheckResult("test.bal", "<caret> connector A(){}", null, '@', "TEST");
    //    }
    //
    //    public void testConnectorAnnotationInCurrentPackageDifferentFileAutoComplete() {
    //        myFixture.addFileToProject("file.bal", "annotation TEST attach connector {}");
    //        doCheckResult("test.bal", "@T<caret> connector A(){}", "@TEST {} connector A(){}", null);
    //    }
    //
    //    public void testConnectorAnnotationInCurrentPackageDifferentFileHasMoreDefinitionsAfter() {
    //        myFixture.addFileToProject("file.bal", "annotation TEST attach connector {}");
    //        doCheckResult("test.bal", "<caret> connector A(){} service R{}", null, '@', "TEST");
    //    }
    //
    //    public void testConnectorAnnotationInCurrentPackageSameFileHasMoreDefinitionsAfter() {
    //        doCheckResult("test.bal", "annotation TEST attach connector {} <caret> connector A(){} service R{}", null,
    //                '@', "TEST");
    //    }

    //    public void testConnectorBody() {
    //        List<String> expectedLookups = new LinkedList<>();
    //        expectedLookups.addAll(DATA_TYPES);
    //        expectedLookups.addAll(OTHER_TYPES);
    //        expectedLookups.addAll(XMLNS_TYPE);
    //        expectedLookups.addAll(REFERENCE_TYPES);
    //        expectedLookups.add("C");
    //        expectedLookups.add("action");
    //        doTest("connector C(){ <caret> }", expectedLookups.toArray(new String[expectedLookups.size()]));
    //    }

    //    public void testConnectorBodyAfterAnnotation() {
    //        doTest("connector C(){ @test:test{} <caret> }", "action", "C");
    //    }
    //
    //    public void testConnectorBodyVariableDeclarationPackage() {
    //        myFixture.addFileToProject("org/test/file.bal", "package org.test; public struct TEST {}");
    //        doCheckResult("test.bal", "import org.test; connector C(){ te<caret> }",
    //                "import org.test; connector C(){ test: }", null);
    //    }
    //
    //    public void testConnectorBodyVariableDeclarationPackageInvocation() {
    //        myFixture.addFileToProject("org/test/file.bal", "package org.test; public connector TEST () {}");
    //        doTest("import org.test; connector C(){ test:<caret> }", "TEST");
    //    }
    //
    //    public void testConnectorBodyVariableDeclarationPackageInvocationAutoCompletion() {
    //        myFixture.addFileToProject("org/test/file.bal", "package org.test; public connector TEST () {}");
    //        doCheckResult("test.bal", "import org.test; connector C(){ test:T<caret> }",
    //                "import org.test; connector C(){ test:TEST }", null);
    //    }

    public void testConnectorBodyVariableDeclarationIdentifier() {
        myFixture.addFileToProject("org/test/file.bal", "package org.test; public connector TEST () {}");
        doTest("import org.test; connector C(){ test:TEST <caret> }");
    }

    //    public void testConnectorBodyVariableInitialization() {
    //        myFixture.addFileToProject("org/test/file.bal", "package org.test; public connector TEST () {}");
    //        doTest("import org.test; connector C(){ test:TEST t = <caret> }", "create", "C", "test", "lengthof",
    //                "typeof", "true", "false", "null");
    //    }

    //    public void testConnectorBodyVariableInitializationCreateKeyword() {
    //        myFixture.addFileToProject("org/test/file.bal", "package org.test; public connector TEST () {}");
    //        doTest("import org.test; connector C(){ test:TEST t = create <caret> }", "C", "test");
    //    }
    //
    //    public void testConnectorBodyVariableInitializationPackageInvocation() {
    //        myFixture.addFileToProject("org/test/file.bal", "package org.test; public connector TEST () {}");
    //        doCheckResult("test.bal", "import org.test; connector C(){ test:TEST t = create t<caret> }",
    //                "import org.test; connector C(){ test:TEST t = create test: }", null);
    //    }

    //    public void testConnectorBodyVariableInitializationPackageAutoCompletion() {
    //        myFixture.addFileToProject("org/test/file.bal", "package org.test; public connector TEST () {}");
    //        doTest("import org.test; connector C(){ test:TEST t = create test:<caret> }", "TEST");
    //    }

    //    public void testConnectorBodyVariableInitializationPackageInvocationAutoCompletion() {
    //        myFixture.addFileToProject("org/test/file.bal", "package org.test; public connector TEST () {}");
    //        doCheckResult("test.bal", "import org.test; connector C(){ test:TEST t = create test:T<caret> }",
    //                "import org.test; connector C(){ test:TEST t = create test:TEST }", null);
    //    }

    /**
     * Test action level lookups.
     */
    //    public void testActionIdentifier() {
    //        doTest("connector C(){ action <caret>}");
    //    }
    //
    //    public void testActionAnnotation() {
    //        doCheckResult("test.bal", "connector C(){ @<caret> action A()(message) {} }", null, '@');
    //    }
    //
    //    public void testActionAnnotationWithImports() {
    //        myFixture.addFileToProject("org/test/file.bal", "string s = \"\";");
    //        doCheckResult("test.bal", "import org.test; connector C(){ <caret> action A()(message) {} }", null, '@',
    //                "test");
    //    }
    //
    //    public void testActionAnnotationWithImportsNoAnnotationDefinitions() {
    //        myFixture.addFileToProject("org/test/file.bal", "function test(){}");
    //        doCheckResult("test.bal", "import org.test; connector C(){ @tes<caret> action A()(message) {} }",
    //                "import org.test; connector C(){ @test: action A()(message) {} }", null);
    //    }
    //
    //    public void testActionAnnotationWithImportsWithNoMatchingAnnotationDefinitions() {
    //        myFixture.addFileToProject("org/test/file.bal", "annotation TEST attach test {}");
    //        doCheckResult("test.bal", "import org.test; connector C(){ @test:<caret> action A()(message) {} }", null,
    //                null);
    //    }
    //
    //    public void testActionAnnotationWithImportsWithMatchingAnnotationDefinitions() {
    //        myFixture.addFileToProject("org/test/file.bal", "package org.test; public annotation TEST attach action
    // {} " +
    //                "public annotation TEST2 attach resource {}");
    //        doCheckResult("test.bal", "import org.test; connector C(){ @test:<caret> action A()(message) {} }", null,
    //                null, "TEST");
    //    }
    //
    //    public void testActionAnnotationWithImportsWithMatchingAnnotationDefinitionsAutoCompletion() {
    //        myFixture.addFileToProject("org/test/file.bal", "package org.test; public annotation TEST attach action
    // {}");
    //        doCheckResult("test.bal", "import org.test; connector C(){ @test:T<caret> action A()(message) {} }",
    //                "import org.test; connector C(){ @test:TEST {} action A()(message) {} }", null);
    //    }
    //
    //    public void testActionAnnotationInCurrentPackageSameFile() {
    //        doCheckResult("test.bal", "annotation TEST attach action {} connector C(){ <caret> action A()(message) " +
    //                "{} }", null, '@', "TEST");
    //    }
    //
    //    public void testActionAnnotationInCurrentPackageSameFileAutoComplete() {
    //        doCheckResult("test.bal", "annotation TEST attach action {} connector C(){ @T<caret> action A()
    // (message) {}" +
    //                " }", "annotation TEST attach action {} connector C(){ @TEST {} action A()(message) {} }", null);
    //    }
    //
    //    public void testActionAnnotationInCurrentPackageDifferentFile() {
    //        myFixture.addFileToProject("file.bal", "annotation TEST attach action {}");
    //        doCheckResult("test.bal", "connector C(){ <caret> action A()(message) {} }", null, '@', "TEST");
    //    }
    //
    //    public void testActionAnnotationInCurrentPackageDifferentFileAutoComplete() {
    //        myFixture.addFileToProject("file.bal", "annotation TEST attach action {}");
    //        doCheckResult("test.bal", "connector C(){ @T<caret> action A()(message) {} }",
    //                "connector C(){ @TEST {} action A()(message) {} }", null);
    //    }
    //
    //    public void testActionAnnotationInCurrentPackageDifferentFileHasMoreDefinitionsAfter() {
    //        myFixture.addFileToProject("file.bal", "annotation TEST attach action {}");
    //        doCheckResult("test.bal", "connector C(){ @<caret> action A()(message) {} } service R{}", null, null,
    //                "TEST");
    //    }
    //
    //    public void testActionAnnotationInCurrentPackageSameFileHasMoreDefinitionsAfter() {
    //        doCheckResult("test.bal", "annotation TEST attach action {} connector C(){ @<caret> action A()(message)
    // {} " +
    //                "}" + " service R{}", null, null, "TEST");
    //    }

    /**
     * Test struct level lookups.
     */
    public void testStructIdentifier() {
        doTest("struct <caret>");
    }

    public void testStructAnnotation() {
        doCheckResult("test.bal", "@<caret> struct S{}", null, '@');
    }

    //    public void testStructAnnotationWithImports() {
    //        myFixture.addFileToProject("org/test/file.bal", "string s = \"\";");
    //        doCheckResult("test.bal", "import org.test; <caret>struct S{}", null, '@', "test");
    //    }
    //
    //    public void testStructAnnotationWithImportsNoAnnotationDefinitions() {
    //        myFixture.addFileToProject("org/test/file.bal", "function test(){}");
    //        doCheckResult("test.bal", "import org.test; @test<caret> struct S{}", null, ':');
    //    }
    //
    //    public void testStructAnnotationWithImportsWithNoMatchingAnnotationDefinitions() {
    //        myFixture.addFileToProject("org/test/file.bal", "annotation TEST attach test {}");
    //        doCheckResult("test.bal", "import org.test; @test:<caret> struct S{}", null, null);
    //    }
    //
    //    public void testStructAnnotationWithImportsWithMatchingAnnotationDefinitions() {
    //        myFixture.addFileToProject("org/test/file.bal", "package org.test; public annotation TEST attach struct
    // {} " +
    //                "annotation TEST2 attach resource {}");
    //        doCheckResult("test.bal", "import org.test; @test:<caret> struct S{}", null, null, "TEST");
    //    }
    //
    //    public void testStructAnnotationWithImportsWithMatchingAnnotationDefinitionsAutoCompletion() {
    //        myFixture.addFileToProject("org/test/file.bal", "package org.test; public annotation TEST attach struct
    // {}");
    //        doCheckResult("test.bal", "import org.test; @test:T<caret> struct S{}",
    //                "import org.test; @test:TEST {} struct S{}", null);
    //    }
    //
    //    public void testStructAnnotationInCurrentPackageSameFile() {
    //        doCheckResult("test.bal", "annotation TEST attach struct {} <caret> struct S{}", null, '@', "TEST");
    //    }
    //
    //    public void testStructAnnotationInCurrentPackageSameFileAutoComplete() {
    //        doCheckResult("test.bal", "annotation TEST attach struct {} @T<caret> struct S{}",
    //                "annotation TEST attach struct {} @TEST {} struct S{}", null);
    //    }
    //
    //    public void testStructAnnotationInCurrentPackageDifferentFile() {
    //        myFixture.addFileToProject("file.bal", "annotation TEST attach struct {}");
    //        doCheckResult("test.bal", "<caret> struct S{}", null, '@', "TEST");
    //    }
    //
    //    public void testStructAnnotationInCurrentPackageDifferentFileAutoComplete() {
    //        myFixture.addFileToProject("file.bal", "annotation TEST attach struct {}");
    //        doCheckResult("test.bal", "@T<caret> struct S{}", "@TEST {} struct S{}", null);
    //    }
    //
    //    public void testStructAnnotationInCurrentPackageDifferentFileHasMoreDefinitionsAfter() {
    //        myFixture.addFileToProject("file.bal", "annotation TEST attach struct {}");
    //        doCheckResult("test.bal", "<caret> struct S{} service R{}", null, '@', "TEST");
    //    }
    //
    //    public void testStructAnnotationInCurrentPackageSameFileHasMoreDefinitionsAfter() {
    //        doCheckResult("test.bal", "annotation TEST attach struct {} <caret> struct S{} service R{}", null,
    //                '@', "TEST");
    //    }

    public void testSingleLevelStructInSameFile() {
        doCheckResult("test.bal", "struct Name { string firstName; } function test(){ Name name = { f<caret> }; }",
                "struct Name { string firstName; } function test(){ Name name = { firstName: }; }", null);
    }

    //    public void testSingleLevelStructInSameFileValue() {
    //        doTest("struct Name { string firstName; } function test(){ string firstName=\"\"; Name name = { " +
    //                "firstName:<caret> }; }", "firstName", "test");
    //    }

    public void testMultiLevelStructInSameFile() {
        doCheckResult("test.bal", "struct Name { string firstName; } struct User { Name name; }" +
                " function test(){ User user = { n<caret> }; }", "struct Name { string firstName; } struct User { " +
                "Name name; } function test(){ User user = { name: }; }", null);
    }

    public void testMultiLevelStructInSameFileLevel1() {
        doTest("struct Name { string firstName; } struct User { Name name; }" +
                " function test(){ User user = { }; user.<caret> }", "name");
    }

    public void testMultiLevelStructInSameFileLevel2() {
        doTest("struct Name { string firstName; } struct User { Name name; } function test(){ User user = { }; user" +
                ".name.<caret> }", "firstName");
    }

    /**
     * Keywords in statements.
     */
    public void testKeywordAfterStatement() {
        doCheckResult("test.bal", "function test(){ int a; if<caret> }", "function test(){ int a; if () {\n    " +
                "\n} }", null);
    }

    public void testKeywordBeforeStatement() {
        doCheckResult("test.bal", "function test(){ if<caret> int a; }", "function test(){ if () {\n    \n} int a; }",
                null);
    }

    public void testKeywordBetweenStatements() {
        doCheckResult("test.bal", "function test(){ int a; if<caret> int b; }", "function test(){ int a; if () {\n" +
                "    \n} int b; }", null);
    }

    //    public void testCommonKeywordsAfterStatement() {
    //        List<String> expectedLookups = new LinkedList<>();
    //        expectedLookups.addAll(DATA_TYPES);
    //        expectedLookups.addAll(OTHER_TYPES);
    //        expectedLookups.addAll(XMLNS_TYPE);
    //        expectedLookups.addAll(REFERENCE_TYPES);
    //        expectedLookups.addAll(COMMON_KEYWORDS);
    //        expectedLookups.addAll(VALUE_KEYWORDS);
    //        expectedLookups.addAll(FUNCTION_LEVEL_KEYWORDS);
    //        expectedLookups.add("a");
    //        expectedLookups.add("test");
    //        doTest("function test(){ int a; <caret> }", expectedLookups.toArray(new String[expectedLookups.size()]));
    //    }
    //
    //    public void testCommonKeywordsBeforeStatement() {
    //        List<String> expectedLookups = new LinkedList<>();
    //        expectedLookups.addAll(DATA_TYPES);
    //        expectedLookups.addAll(OTHER_TYPES);
    //        expectedLookups.addAll(XMLNS_TYPE);
    //        expectedLookups.addAll(REFERENCE_TYPES);
    //        expectedLookups.addAll(COMMON_KEYWORDS);
    //        expectedLookups.add("test");
    //        expectedLookups.add("return");
    //        doTest("function test(){ <caret> int a; }", expectedLookups.toArray(new String[expectedLookups.size()]));
    //    }
    //
    //    public void testCommonKeywordsBetweenStatements() {
    //        List<String> expectedLookups = new LinkedList<>();
    //        expectedLookups.addAll(DATA_TYPES);
    //        expectedLookups.addAll(OTHER_TYPES);
    //        expectedLookups.addAll(XMLNS_TYPE);
    //        expectedLookups.addAll(REFERENCE_TYPES);
    //        expectedLookups.addAll(COMMON_KEYWORDS);
    //        expectedLookups.addAll(VALUE_KEYWORDS);
    //        expectedLookups.addAll(FUNCTION_LEVEL_KEYWORDS);
    //        expectedLookups.add("a");
    //        expectedLookups.add("test");
    //        doTest("function test(){ int a; <caret> int b; }", expectedLookups.toArray(new String[expectedLookups
    // .size()]));
    //    }
    //
    //    public void testSuggestionsInTransformStatement() {
    //        List<String> expectedLookups = new LinkedList<>();
    //        expectedLookups.addAll(DATA_TYPES);
    //        expectedLookups.addAll(OTHER_TYPES);
    //        expectedLookups.addAll(XMLNS_TYPE);
    //        expectedLookups.addAll(REFERENCE_TYPES);
    //        expectedLookups.addAll(COMMON_KEYWORDS);
    //        expectedLookups.addAll(VALUE_KEYWORDS);
    //        expectedLookups.addAll(FUNCTION_LEVEL_KEYWORDS);
    //        expectedLookups.add("a");
    //        expectedLookups.add("b");
    //        expectedLookups.add("g");
    //        expectedLookups.add("test");
    //        doTest("int g = 1; function test(){ int a = 10; transform { int b = 5; <caret> } }",
    //                expectedLookups.toArray(new String[expectedLookups.size()]));
    //    }

    // todo -  test resource level specific keywords

}
