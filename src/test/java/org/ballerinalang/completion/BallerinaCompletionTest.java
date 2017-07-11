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

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class BallerinaCompletionTest extends BallerinaCompletionTestBase {

    private static final List<String> FILE_LEVEL_KEYWORDS = Arrays.asList("package", "import", "const", "service",
            "function", "connector", "struct", "typemapper", "annotation");
    private static final List<String> ANY_TYPE = Collections.singletonList("any");
    private static final List<String> XMLNS_TYPE = Collections.singletonList("xmlns");
    private static final List<String> DATA_TYPES = Arrays.asList("boolean", "int", "float", "string", "blob");
    private static final List<String> REFERENCE_TYPES = Arrays.asList("message", "map", "xml", "xmlDocument", "json",
            "datatable");
    private static final List<String> COMMON_KEYWORDS = Arrays.asList("if", "else", "fork", "join", "timeout",
            "worker", "transform", "transaction", "abort", "aborted", "committed", "try", "catch", "finally", "iterate",
            "while", "continue", "break", "throw");
    private static final List<String> VALUE_KEYWORDS = Arrays.asList("true", "false", "null");
    private static final List<String> FUNCTION_LEVEL_KEYWORDS = Collections.singletonList("return");

    private static final String UTILS_PACKAGE_NAME = "org/test/utils.bal";
    private static final String SAMPLE_UTIL_FUNCTIONS = "package org.test; function getA(){} function getB(){}";

    @Override
    protected String getTestDataPath() {
        return getTestDataPath("completion");
    }

    /**
     * Test file level lookups.
     */
    public void testEmptyFile() {
        List<String> expectedLookups = new LinkedList<>();
        expectedLookups.addAll(FILE_LEVEL_KEYWORDS);
        expectedLookups.addAll(ANY_TYPE);
        expectedLookups.addAll(XMLNS_TYPE);
        expectedLookups.addAll(DATA_TYPES);
        expectedLookups.addAll(REFERENCE_TYPES);
        doTest("<caret>", expectedLookups.toArray(new String[expectedLookups.size()]));
    }

    public void testEmptyFilePackageKeyword() {
        doTest("p<caret>", "import", "package", "typemapper", "map");
    }

    public void testEmptyFileImportKeyword() {
        doTest("i<caret>", "annotation", "function", "import", "service", "int", "string");
    }

    public void testEmptyFileWithSpaceBeforeCaret() {
        List<String> expectedLookups = new LinkedList<>();
        expectedLookups.addAll(FILE_LEVEL_KEYWORDS);
        expectedLookups.addAll(ANY_TYPE);
        expectedLookups.addAll(XMLNS_TYPE);
        expectedLookups.addAll(DATA_TYPES);
        expectedLookups.addAll(REFERENCE_TYPES);
        doTest("\n<caret>", expectedLookups.toArray(new String[expectedLookups.size()]));
    }

    public void testEmptyFileWithSpaceBeforeCaretPackageKeyword() {
        doTest("\np<caret>", "import", "package", "typemapper", "map");
    }

    public void testEmptyFileWithSpaceBeforeCaretImportKeyword() {
        doTest("\ni<caret>", "annotation", "function", "import", "service", "int", "string");
    }

    public void testEmptyFileWithSpaceAfterCaret() {
        List<String> expectedLookups = new LinkedList<>();
        expectedLookups.addAll(FILE_LEVEL_KEYWORDS);
        expectedLookups.addAll(ANY_TYPE);
        expectedLookups.addAll(XMLNS_TYPE);
        expectedLookups.addAll(DATA_TYPES);
        expectedLookups.addAll(REFERENCE_TYPES);
        doTest("<caret>\n", expectedLookups.toArray(new String[expectedLookups.size()]));
    }

    public void testEmptyFileWithSpaceAfterCaretPackageKeyword() {
        doTest("p<caret>\n", "import", "package", "typemapper", "map");
    }

    public void testEmptyFileWithSpaceAfterCaretImportKeyword() {
        doTest("i<caret>\n", "annotation", "function", "import", "service", "int", "string");
    }

    public void testEmptyFileWithSpaces() {
        List<String> expectedLookups = new LinkedList<>();
        expectedLookups.addAll(FILE_LEVEL_KEYWORDS);
        expectedLookups.addAll(ANY_TYPE);
        expectedLookups.addAll(XMLNS_TYPE);
        expectedLookups.addAll(DATA_TYPES);
        expectedLookups.addAll(REFERENCE_TYPES);
        doTest("\n<caret>\n", expectedLookups.toArray(new String[expectedLookups.size()]));
    }

    public void testEmptyFileWithSpacesPackageKeyword() {
        doTest("\np<caret>\n", "import", "package", "typemapper", "map");
    }

    public void testEmptyFileWithSpacesImportKeyword() {
        doTest("\ni<caret>\n", "annotation", "function", "import", "service", "int", "string");
    }

    public void testImportAfterPackage() {
        List<String> expectedLookups = new LinkedList<>();
        expectedLookups.addAll(ANY_TYPE);
        expectedLookups.addAll(XMLNS_TYPE);
        expectedLookups.addAll(DATA_TYPES);
        expectedLookups.addAll(REFERENCE_TYPES);
        expectedLookups.add("import");
        expectedLookups.add("const");
        expectedLookups.add("service");
        expectedLookups.add("function");
        expectedLookups.add("connector");
        expectedLookups.add("struct");
        expectedLookups.add("typemapper");
        expectedLookups.add("annotation");
        doTest("package test; \n<caret>\n", expectedLookups.toArray(new String[expectedLookups.size()]));
    }

    public void testImportAfterPackagePartialIdentifier() {
        doTest("package test; \ni<caret>\n", "annotation", "function", "import", "service", "int", "string");
    }

    public void testImportAfterPackageBeforeFunction() {
        List<String> expectedLookups = new LinkedList<>();
        expectedLookups.addAll(ANY_TYPE);
        expectedLookups.addAll(XMLNS_TYPE);
        expectedLookups.addAll(DATA_TYPES);
        expectedLookups.addAll(REFERENCE_TYPES);
        expectedLookups.add("import");
        expectedLookups.add("const");
        expectedLookups.add("service");
        expectedLookups.add("function");
        expectedLookups.add("connector");
        expectedLookups.add("struct");
        expectedLookups.add("typemapper");
        expectedLookups.add("annotation");
        doTest("package test; \n<caret>\nfunction A(){}", expectedLookups.toArray(new String[expectedLookups.size()]));
    }

    public void testImportAfterPackageBeforeFunctionPartialIdentifier() {
        doTest("package test; \ni<caret>\nfunction A(){}", "annotation", "function", "import", "service", "int",
                "string");
    }

    public void testPackageBeforeImport() {
        List<String> expectedLookups = new LinkedList<>();
        expectedLookups.addAll(FILE_LEVEL_KEYWORDS);
        expectedLookups.addAll(ANY_TYPE);
        expectedLookups.addAll(XMLNS_TYPE);
        expectedLookups.addAll(DATA_TYPES);
        expectedLookups.addAll(REFERENCE_TYPES);
        doTest("<caret>\nimport test; \nfunction A(){}", expectedLookups.toArray(new String[expectedLookups.size()]));
    }

    public void testPackageBeforeImportPartialIdentifier() {
        doTest("p<caret>\nimport test; \nfunction A(){}", "import", "package", "typemapper", "map");
    }

    public void testImportBeforeImport() {
        List<String> expectedLookups = new LinkedList<>();
        expectedLookups.addAll(FILE_LEVEL_KEYWORDS);
        expectedLookups.addAll(ANY_TYPE);
        expectedLookups.addAll(XMLNS_TYPE);
        expectedLookups.addAll(DATA_TYPES);
        expectedLookups.addAll(REFERENCE_TYPES);
        doTest("<caret>\nimport test; \nfunction A(){}", expectedLookups.toArray(new String[expectedLookups.size()]));
    }

    public void testImportBeforeImportPartialIdentifier() {
        doTest("i<caret>\nimport test; \nfunction A(){}", "annotation", "function", "import", "service", "int",
                "string");
    }

    public void testImportAfterImport() {
        List<String> expectedLookups = new LinkedList<>();
        expectedLookups.addAll(ANY_TYPE);
        expectedLookups.addAll(XMLNS_TYPE);
        expectedLookups.addAll(DATA_TYPES);
        expectedLookups.addAll(REFERENCE_TYPES);
        expectedLookups.add("import");
        expectedLookups.add("const");
        expectedLookups.add("service");
        expectedLookups.add("function");
        expectedLookups.add("connector");
        expectedLookups.add("struct");
        expectedLookups.add("typemapper");
        expectedLookups.add("annotation");
        expectedLookups.add("test");
        myFixture.addFileToProject("test/file.bal", "string s = \"\";");
        doTest("import test; \n<caret> \nfunction A(){}", expectedLookups.toArray(new String[expectedLookups.size()]));
    }

    public void testImportAfterImportPartialIdentifier() {
        doTest("import test; \ni<caret> \nfunction A(){}", "annotation", "function", "import", "service", "int",
                "string");
    }

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
        myFixture.addFileToProject("org/test/file.bal", "annotation TEST attach service {}");
        doCheckResult("test.bal", "import org.test; @test:<caret> const string S=\"\";", null, null);
    }

    public void testConstantAnnotationWithImportsWithMatchingAnnotationDefinitions() {
        myFixture.addFileToProject("org/test/file.bal", "package org.test; annotation TEST attach const {} " +
                "annotation TEST2 attach resource {}");
        doCheckResult("test.bal", "import org.test; @test:<caret> const string S=\"\";", null, null, "TEST");
    }

    public void testConstantAnnotationWithImportsWithMatchingAnnotationDefinitionsAutoCompletion() {
        myFixture.addFileToProject("org/test/file.bal", "package org.test; annotation TEST attach const {}");
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

    /**
     * Test annotation level lookups.
     */
    public void testAnnotationIdentifier() {
        doTest("annotation <caret>");
    }

    public void testAnnotationAttachKeyword() {
        doTest("annotation A <caret>", "attach");
    }

    public void testAnnotationAttachmentPoints() {
        doTest("annotation A attach <caret>", "service", "connector", "action", "function", "typemapper", "struct",
                "const", "parameter", "annotation", "resource");
    }

    public void testMultipleAnnotationAttachmentPoints() {
        doTest("annotation A attach service, <caret>", "service", "connector", "action", "function", "typemapper",
                "struct", "const", "parameter", "annotation", "resource");
    }

    /**
     * Test annotation field lookups.
     */
    public void testAnnotationFields() {
        doCheckResult("test.bal", "annotation TEST attach function { string key; string value;} " +
                "@TEST{<caret>} function A(){}", null, null, "key", "value");
    }

    public void testAnnotationFieldsAutoCompletion() {
        doCheckResult("test.bal", "annotation TEST attach function { string key; string value;} " +
                "@TEST{k<caret>} function A(){}", "annotation TEST attach function { string key; string value;} " +
                "@TEST{key:} function A(){}", null);
    }

    public void testAnnotationFieldValues() {
        doCheckResult("test.bal", "annotation TEST attach function { string key; string value;} " +
                "@TEST{key:<caret>} function A(){}", null, null, "true", "false", "null");
    }

    public void testAnnotationFieldsFromDifferentPackage() {
        myFixture.addFileToProject("org/test/file.bal", "annotation TEST attach function { string key; string value;}");
        doTest("import org.test; @test:TEST{<caret>} function A(){}", "key", "value");
    }

    public void testAnnotationFieldsAutoCompletionFromDifferentPackage() {
        myFixture.addFileToProject("org/test/file.bal", "annotation TEST attach function { string key; string value;}");
        doCheckResult("main.bal", "import org.test; @test:TEST{k<caret>} function A(){}",
                "import org.test; @test:TEST{key:<caret>} function A(){}", null);
    }

    public void testAnnotationFieldValuesFromDifferentPackage() {
        myFixture.addFileToProject("org/test/file.bal", "annotation TEST attach function { string key; string value;}");
        doTest("import org.test; @test:TEST{key:<caret>} function A(){}", "true", "false", "null");
    }

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
        doTest("import org.test; string s = <caret> ", "test");
    }

    public void testGlobalVariablePackageValueCompletion() {
        myFixture.addFileToProject("org/test/file.bal", "function getValue()(string){return \"\";}");
        doCheckResult("test.bal", "import org.test; string s = t<caret> ", "import org.test; string s = test: ", null);
    }

    public void testGlobalVariablePackageValueDifferentPackage() {
        myFixture.addFileToProject("org/test/file.bal", "function getValue()(string){return \"\";}");
        doTest("import org.test; string s = test:<caret> ", "getValue");
    }

    public void testGlobalVariablePackageValueCompletionDifferentPackage() {
        myFixture.addFileToProject("org/test/file.bal", "function getValue()(string){return \"\";}");
        doCheckResult("test.bal", "import org.test; string s = test:g<caret> ",
                "import org.test; string s = test:getValue() ", null);
    }

    public void testGlobalVariablePackageValueDifferentFile() {
        myFixture.addFileToProject("file.bal", "function getValue()(string){return \"\";}");
        doTest("string s = <caret> ", "getValue");
    }

    public void testGlobalVariablePackageValueDifferentFileCompletion() {
        myFixture.addFileToProject("file.bal", "function getValue()(string){return \"\";}");
        doCheckResult("test.bal", "string s = g<caret>", "string s = getValue()", null);
    }

    public void testGlobalVariableInSamePackageSameFile() {
        List<String> expectedLookups = new LinkedList<>();
        expectedLookups.addAll(DATA_TYPES);
        expectedLookups.addAll(ANY_TYPE);
        expectedLookups.addAll(XMLNS_TYPE);
        expectedLookups.addAll(REFERENCE_TYPES);
        expectedLookups.addAll(COMMON_KEYWORDS);
        expectedLookups.addAll(VALUE_KEYWORDS);
        expectedLookups.addAll(FUNCTION_LEVEL_KEYWORDS);
        expectedLookups.add("S");
        expectedLookups.add("F");
        doTest("string S=\"\"; function F(){ <caret> }", expectedLookups.toArray(new String[expectedLookups.size()]));
    }

    public void testGlobalVariableInSamePackageDifferentFile() {
        myFixture.addFileToProject("file.bal", "string S=\"\";");
        List<String> expectedLookups = new LinkedList<>();
        expectedLookups.addAll(DATA_TYPES);
        expectedLookups.addAll(ANY_TYPE);
        expectedLookups.addAll(XMLNS_TYPE);
        expectedLookups.addAll(REFERENCE_TYPES);
        expectedLookups.addAll(COMMON_KEYWORDS);
        expectedLookups.addAll(VALUE_KEYWORDS);
        expectedLookups.addAll(FUNCTION_LEVEL_KEYWORDS);
        expectedLookups.add("S");
        expectedLookups.add("F");
        doTest("function F(){ <caret> }", expectedLookups.toArray(new String[expectedLookups.size()]));
    }

    public void testGlobalVariableInDifferentPackage() {
        myFixture.addFileToProject("org/test/file.bal", "string S=\"\";");
        doTest("import org.test; function F(){ test:<caret> }", "S");
    }


    /**
     * Test function level lookups.
     */
    public void testFunctionIdentifier() {
        doTest("function <caret>");
    }

    public void testFunctionAnnotation() {
        doCheckResult("test.bal", "@<caret> function A(){}", null, '@');
    }

    public void testFunctionAnnotationWithImports() {
        myFixture.addFileToProject("org/test/file.bal", "string s = \"\";");
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

    public void testFunctionBodyWithoutParamsAndImports() {
        List<String> FUNCTION_LEVEL_SUGGESTIONS = Collections.singletonList("test");
        List<String> expectedLookups = new LinkedList<>();
        expectedLookups.addAll(DATA_TYPES);
        expectedLookups.addAll(ANY_TYPE);
        expectedLookups.addAll(XMLNS_TYPE);
        expectedLookups.addAll(REFERENCE_TYPES);
        expectedLookups.addAll(COMMON_KEYWORDS);
        expectedLookups.addAll(VALUE_KEYWORDS);
        expectedLookups.addAll(FUNCTION_LEVEL_KEYWORDS);
        expectedLookups.addAll(FUNCTION_LEVEL_SUGGESTIONS);
        doTest("function test () { <caret> }", expectedLookups.toArray(new String[expectedLookups.size()]));
    }

    public void testFunctionBodyWithParams() {
        List<String> FUNCTION_LEVEL_SUGGESTIONS = Arrays.asList("test", "arg");
        List<String> expectedLookups = new LinkedList<>();
        expectedLookups.addAll(DATA_TYPES);
        expectedLookups.addAll(ANY_TYPE);
        expectedLookups.addAll(XMLNS_TYPE);
        expectedLookups.addAll(REFERENCE_TYPES);
        expectedLookups.addAll(COMMON_KEYWORDS);
        expectedLookups.addAll(VALUE_KEYWORDS);
        expectedLookups.addAll(FUNCTION_LEVEL_KEYWORDS);
        expectedLookups.addAll(FUNCTION_LEVEL_SUGGESTIONS);
        doTest("function test (int arg) { <caret> }", expectedLookups.toArray(new String[expectedLookups.size()]));
    }

    public void testFunctionBodyWithConst() {
        List<String> FUNCTION_LEVEL_SUGGESTIONS = Arrays.asList("test", "arg", "GREETING");
        List<String> expectedLookups = new LinkedList<>();
        expectedLookups.addAll(DATA_TYPES);
        expectedLookups.addAll(ANY_TYPE);
        expectedLookups.addAll(XMLNS_TYPE);
        expectedLookups.addAll(REFERENCE_TYPES);
        expectedLookups.addAll(COMMON_KEYWORDS);
        expectedLookups.addAll(VALUE_KEYWORDS);
        expectedLookups.addAll(FUNCTION_LEVEL_KEYWORDS);
        expectedLookups.addAll(FUNCTION_LEVEL_SUGGESTIONS);
        doTest("const string GREETING = \"Hello\"; function test (int arg) { <caret> }",
                expectedLookups.toArray(new String[expectedLookups.size()]));
    }

    public void testFunctionBodyWithImports() {
        List<String> expectedLookups = new LinkedList<>();
        expectedLookups.addAll(DATA_TYPES);
        expectedLookups.addAll(ANY_TYPE);
        expectedLookups.addAll(XMLNS_TYPE);
        expectedLookups.addAll(REFERENCE_TYPES);
        expectedLookups.addAll(COMMON_KEYWORDS);
        expectedLookups.addAll(VALUE_KEYWORDS);
        expectedLookups.addAll(FUNCTION_LEVEL_KEYWORDS);
        expectedLookups.add("test");
        expectedLookups.add("test");
        myFixture.addFileToProject("org/test/file.bal", "string s = \"\";");
        doTest("import org.test; function test () { <caret> }",
                expectedLookups.toArray(new String[expectedLookups.size()]));
    }

    public void testFunctionBodyWithCommonKeywords() {
        List<String> expectedLookups = new LinkedList<>();
        expectedLookups.add("int");
        expectedLookups.add("if");
        expectedLookups.add("string");
        expectedLookups.add("join");
        expectedLookups.add("timeout");
        expectedLookups.add("transaction");
        expectedLookups.add("finally");
        expectedLookups.add("continue");
        expectedLookups.add("iterate");
        expectedLookups.add("while");
        expectedLookups.add("committed");
        doTest("function test () { i<caret> }", expectedLookups.toArray(new String[expectedLookups.size()]));
    }

    public void testFunctionBodyWithFunctionLevelKeywords() {
        doTest("function test () { r<caret> }", "return", "string", "fork", "worker", "transform", "transaction",
                "abort", "aborted", "try", "true", "break", "iterate", "throw");
    }

    public void testInvokingFunctionInDifferentFile1() {
        myFixture.addFileToProject("file.bal", "function test(){}");
        doCheckResult("test.bal", "function main(string[] args){ tes<caret> }",
                "function main(string[] args){ test() }", null);
    }

    public void testInvokingFunctionInDifferentFile2() {
        myFixture.addFileToProject("file.bal", "function test1(){} function test2(){}");
        doTest("function main(string[] args){ tes<caret> }", "test1", "test2");
    }

    public void testVariable1() {
        doTest("function main(string[] args){ int <caret> }");
    }

    public void testVariable2() {
        doTest("function main(string[] args){ int a = <caret> }", "args", "main", "create", "false", "null", "true");
    }

    public void testCreateKeywordAutoCompletion() {
        doCheckResult("test.bal", "function main(string[] args){ http:ClientConnector con = cr<caret> }",
                "function main(string[] args){ http:ClientConnector con = create }", null);
    }

    public void testInvocationInFunctionWithTraileringCode() {
        List<String> expectedLookups = new LinkedList<>();
        expectedLookups.addAll(DATA_TYPES);
        expectedLookups.addAll(ANY_TYPE);
        expectedLookups.addAll(XMLNS_TYPE);
        expectedLookups.addAll(REFERENCE_TYPES);
        expectedLookups.addAll(COMMON_KEYWORDS);
        expectedLookups.addAll(VALUE_KEYWORDS);
        expectedLookups.addAll(FUNCTION_LEVEL_KEYWORDS);
        expectedLookups.add("args");
        expectedLookups.add("test");
        expectedLookups.add("main");
        myFixture.addFileToProject(UTILS_PACKAGE_NAME, SAMPLE_UTIL_FUNCTIONS);
        doTest("import org.test; function main(string[] args){ <caret> \ntest:getA(); }",
                expectedLookups.toArray(new String[expectedLookups.size()]));
    }

    public void testPackageInvocationInFunction() {
        myFixture.addFileToProject(UTILS_PACKAGE_NAME, SAMPLE_UTIL_FUNCTIONS);
        doTest("import org.test; function main(string[] args){ test:<caret> }", "getA", "getB");
    }

    public void testPackageInvocationInFunctionHasTraileringCode() {
        myFixture.addFileToProject(UTILS_PACKAGE_NAME, SAMPLE_UTIL_FUNCTIONS);
        doTest("import org.test; function main(string[] args){ test:<caret> \ntest:getA();}", "getA", "getB");
    }

    public void testPackageInvocationInFunctionWithPartialIdentifier() {
        myFixture.addFileToProject(UTILS_PACKAGE_NAME, SAMPLE_UTIL_FUNCTIONS);
        doTest("import org.test; function main(string[] args){ test:g<caret> }", "getA", "getB");
    }

    public void testPackageInvocationInFunctionWithPartialIdentifierHasTraileringCode() {
        myFixture.addFileToProject(UTILS_PACKAGE_NAME, SAMPLE_UTIL_FUNCTIONS);
        doTest("import org.test; function main(string[] args){ test:g<caret> \ntest:getA(); }", "getA", "getB");
    }

    public void testPackageInvocationAsParamInFunction() {
        myFixture.addFileToProject(UTILS_PACKAGE_NAME, SAMPLE_UTIL_FUNCTIONS);
        doTest("import org.test; function main(string[] args){ test(test:<caret>) } function test(string s){}",
                "getA", "getB");
    }

    public void testPackageInvocationAsParamInFunctionWithTraileringCode() {
        myFixture.addFileToProject(UTILS_PACKAGE_NAME, SAMPLE_UTIL_FUNCTIONS);
        doTest("import org.test; function main(string[] args){ test(test:<caret>) \ntest:getA(); }" +
                " function test(string s){}", "getA", "getB");
    }

    public void testPackageInvocationAsParamWithTraileringCodeInFunction() {
        myFixture.addFileToProject(UTILS_PACKAGE_NAME, SAMPLE_UTIL_FUNCTIONS);
        doTest("import org.test; function main(string[] args){ test(test:<caret> test:getA()) }" +
                " function test(string s){}", "getA", "getB");
    }

    public void testPackageInvocationAsParamWithTraileringCodeInFunctionWithTraileringCode() {
        myFixture.addFileToProject(UTILS_PACKAGE_NAME, SAMPLE_UTIL_FUNCTIONS);
        doTest("import org.test; function main(string[] args){ test(test:<caret> test:getA()) \ntest:getA(); }" +
                " function test(string s){}", "getA", "getB");
    }

    public void testPackageInvocationAsParamInFunctionWithPartialIdentifier() {
        myFixture.addFileToProject(UTILS_PACKAGE_NAME, SAMPLE_UTIL_FUNCTIONS);
        doTest("import org.test; function main(string[] args){ test(test:g<caret>) } function test(string s){}",
                "getA", "getB");
    }

    public void testPackageInvocationAsParamInFunctionWithPartialIdentifierWithTraileringCode() {
        myFixture.addFileToProject(UTILS_PACKAGE_NAME, SAMPLE_UTIL_FUNCTIONS);
        doTest("import org.test; function main(string[] args){ test(test:g<caret>) \ntest:getA();} " +
                "function test(string s){}", "getA", "getB");
    }

    public void testPackageInvocationAsParamWithTraileringCodeInFunctionWithPartialIdentifier() {
        myFixture.addFileToProject(UTILS_PACKAGE_NAME, SAMPLE_UTIL_FUNCTIONS);
        doTest("import org.test; function main(string[] args){ test(test:g<caret> test:getA()) } " +
                "function test(string s){}", "getA", "getB");
    }

    public void testPackageInvocationAsParamWithTraileringCodeInFunctionWithPartialIdentifierWithTraileringCode() {
        myFixture.addFileToProject(UTILS_PACKAGE_NAME, SAMPLE_UTIL_FUNCTIONS);
        doTest("import org.test; function main(string[] args){ test(test:g<caret> test:getA()) \ntest:getA(); } " +
                "function test(string s){}", "getA", "getB");
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
                "args", "main", "test", "false", "null", "true");
    }

    public void testFunctionFromPackageInvocation24() {
        myFixture.addFileToProject(UTILS_PACKAGE_NAME, SAMPLE_UTIL_FUNCTIONS);
        doTest("import org.test; function main(string[] args){ string s = test:getA()+g<caret> \"TEST\"; }",
                "args");
    }

    public void testFunctionFromPackageInvocation25() {
        myFixture.addFileToProject(UTILS_PACKAGE_NAME, SAMPLE_UTIL_FUNCTIONS);
        doTest("import org.test; function main(string[] args){ test:<caret> \ntest:geA();}", "getA", "getB");
    }

    public void testFunctionFromPackageInvocation26() {
        myFixture.addFileToProject(UTILS_PACKAGE_NAME, SAMPLE_UTIL_FUNCTIONS);
        doTest("import org.test; function main(string[] args){ test:g<caret> \ntest:getA();}", "getA", "getB");
    }

    public void testVarDefinition() {
        myFixture.addFileToProject(UTILS_PACKAGE_NAME, SAMPLE_UTIL_FUNCTIONS);
        doTest("import org.test; function main(string[] args){ string s = <caret> }",
                "args", "main", "test", "create", "false", "null", "true");
    }

    public void testVarDefinitionWithTraileringCode() {
        myFixture.addFileToProject(UTILS_PACKAGE_NAME, SAMPLE_UTIL_FUNCTIONS);
        doTest("import org.test; function main(string[] args){ string s = \"TEST\" + <caret> }",
                "args", "main", "test", "false", "null", "true");
    }

    public void testVarDefinitionWithLeadingCode() {
        myFixture.addFileToProject(UTILS_PACKAGE_NAME, SAMPLE_UTIL_FUNCTIONS);
        doTest("import org.test; function main(string[] args){ string s = <caret> + \"TEST\" }",
                "args", "main", "test", "create", "false", "null", "true");
    }

    public void testConnectorInit() {
        myFixture.addFileToProject("org/test/con.bal", "connector TestConnector{}");
        doTest("import org.test; function A(){ test:<caret> }", "TestConnector");
    }

    public void testConnectorAutoCompletion() {
        myFixture.addFileToProject("org/test/con.bal", "connector TestConnector{}");
        doCheckResult("file.bal", "import org.test; function A(){ test:T<caret> }",
                "import org.test; function A(){ test:TestConnector }", null);
    }

    public void testConnectorInitCreateKeyword() {
        myFixture.addFileToProject("org/test/con.bal", "connector TestConnector{}");
        doTest("import org.test; function A(){ test:TestConnector c = <caret> }", "create", "test", "A", "false",
                "null", "true");
    }

    public void testConnectorInitCreateKeywordAutoCompletion() {
        myFixture.addFileToProject("org/test/con.bal", "connector TestConnector{}");
        doCheckResult("test.bal", "import org.test; function A(){ test:TestConnector c = c<caret> }",
                "import org.test; function A(){ test:TestConnector c = create }", null);
    }

    public void testConnectorCreation() {
        myFixture.addFileToProject("org/test/con.bal", "connector TestConnector{}");
        doTest("import org.test; function A(){ test:TestConnector c = create <caret> }", "A", "test", "false",
                "null", "true");
    }

    public void testConnectorCreationPackageAutoCompletion() {
        myFixture.addFileToProject("org/test/con.bal", "connector TestConnector{}");
        doCheckResult("test.bal", "import org.test; function A(){ test:TestConnector con = create " +
                "tes<caret> }", "import org.test; function A(){ test:TestConnector con = create test: }", null);
    }

    public void testConnectorCreationCreateKeyword() {
        myFixture.addFileToProject("org/test/con.bal", "connector TestConnector{}");
        // todo - remove c
        doTest("import org.test; function A(){ test:TestConnector c = <caret> test:TestConnector() }",
                "create", "A", "c", "test", "false", "null", "true");
    }

    public void testVariablesInitializationAfterDeclaration() {
        List<String> expectedLookups = new LinkedList<>();
        expectedLookups.addAll(DATA_TYPES);
        expectedLookups.addAll(ANY_TYPE);
        expectedLookups.addAll(XMLNS_TYPE);
        expectedLookups.addAll(REFERENCE_TYPES);
        expectedLookups.addAll(COMMON_KEYWORDS);
        expectedLookups.addAll(VALUE_KEYWORDS);
        expectedLookups.addAll(FUNCTION_LEVEL_KEYWORDS);
        expectedLookups.add("s");
        expectedLookups.add("A");
        doTest("function A(){ string s; <caret> }", expectedLookups.toArray(new String[expectedLookups.size()]));
    }

    public void testVariablesLaterInitialization() {
        List<String> expectedLookups = new LinkedList<>();
        expectedLookups.addAll(DATA_TYPES);
        expectedLookups.addAll(ANY_TYPE);
        expectedLookups.addAll(XMLNS_TYPE);
        expectedLookups.addAll(REFERENCE_TYPES);
        expectedLookups.addAll(COMMON_KEYWORDS);
        expectedLookups.addAll(VALUE_KEYWORDS);
        expectedLookups.addAll(FUNCTION_LEVEL_KEYWORDS);
        expectedLookups.add("s");
        expectedLookups.add("s1");
        expectedLookups.add("A");
        doTest("function A(){ string s; string s1; <caret> }",
                expectedLookups.toArray(new String[expectedLookups.size()]));
    }

    public void testVariablesNoVarsAvailable() {
        doTest("function A(){ string s1 = <caret> }", "A", "create", "false", "null", "true");
    }

    public void testVariablesWhenSingleVariableAvailable() {
        doTest("function A(){ string s1 = \"Test\"; string s2 = <caret> }", "s1", "A", "create", "false", "null",
                "true");
    }

    public void testVariablesWhenSingleVariableAvailableWithPartialIdentifier() {
        doCheckResult("test.bal", "function A(){ string abc = \"Test\"; string def = ab<caret> }",
                "function A(){ string abc = \"Test\"; string def = abc }", null);
    }

    public void testVariablesWhenMultipleVariablesAvailable() {
        doTest("function A(){ string s1 = \"Test\"; string s2 = \"Test\"; string s3 = <caret> }",
                "s1", "s2", "A", "create", "false", "null", "true");
    }

    public void testVariablesWhenMultipleVariablesAvailableAfterLeafElement() {
        doTest("function A(){ string s1 = \"Test\"; string s2 = \"Test\"; string s3 = s1 + <caret> }",
                "s1", "s2", "A", "false", "null", "true");
    }

    public void testVariablesWhenMultipleVariablesAvailableBeforeLeafElement() {
        doTest("function A(){ string s1 = \"Test\"; string s2 = \"Test\"; string s3 = <caret> + s2; }",
                "s1", "s2", "A", "create", "false", "null", "true");
    }

    public void testVariablesInNewLineWhenMultipleVariablesAvailable() {
        List<String> expectedLookups = new LinkedList<>();
        expectedLookups.addAll(DATA_TYPES);
        expectedLookups.addAll(ANY_TYPE);
        expectedLookups.addAll(XMLNS_TYPE);
        expectedLookups.addAll(REFERENCE_TYPES);
        expectedLookups.addAll(COMMON_KEYWORDS);
        expectedLookups.addAll(VALUE_KEYWORDS);
        expectedLookups.addAll(FUNCTION_LEVEL_KEYWORDS);
        expectedLookups.add("s1");
        expectedLookups.add("s2");
        expectedLookups.add("A");
        doTest("function A(){ string s1 = \"Test\"; string s2 = \"Test\";\n <caret> }",
                expectedLookups.toArray(new String[expectedLookups.size()]));
    }

    public void testVariablesInNewLineWhenMultipleVariablesAvailableWithVariablesAfter() {
        List<String> expectedLookups = new LinkedList<>();
        expectedLookups.addAll(DATA_TYPES);
        expectedLookups.addAll(ANY_TYPE);
        expectedLookups.addAll(XMLNS_TYPE);
        expectedLookups.addAll(REFERENCE_TYPES);
        expectedLookups.addAll(COMMON_KEYWORDS);
        expectedLookups.addAll(VALUE_KEYWORDS);
        expectedLookups.addAll(FUNCTION_LEVEL_KEYWORDS);
        expectedLookups.add("s1");
        expectedLookups.add("s2");
        expectedLookups.add("A");
        doTest("function A(){ string s1 = \"Test\"; string s2 = \"Test\";\n <caret> \nstring s4 = \"\";}",
                expectedLookups.toArray(new String[expectedLookups.size()]));
    }

    public void testConnectorInvocationInDifferentPackage() {
        myFixture.addFileToProject("org/test/con.bal", "connector TestConnector(){ action get(){} action post(){}}");
        doTest("import org.test; function A(){ test:TestConnector.<caret> }", "get", "post");
    }

    public void testConnectorInvocationInSamePackage() {
        doTest("function A(){ TestConnector.<caret> } connector TestConnector(){ action get(){} action post(){}}",
                "get", "post");
    }

    public void testTypesAfterRBRACE() {
        List<String> expectedLookups = new LinkedList<>();
        expectedLookups.add("else");
        expectedLookups.add("json");
        expectedLookups.add("message");
        expectedLookups.add("string");
        expectedLookups.add("test");
        expectedLookups.add("transaction");
        expectedLookups.add("transform");
        expectedLookups.add("false");
        expectedLookups.addAll(XMLNS_TYPE);
        doTest("function test(){ if(a==a){}\n s<caret> \nint a; }",
                expectedLookups.toArray(new String[expectedLookups.size()]));
    }

    /**
     * Test statement level lookups
     */
    public void testStrings() {
        doTest("function test() { int a; system:println(\"<caret>\") }");
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
        expectedLookups.addAll(ANY_TYPE);
        expectedLookups.addAll(XMLNS_TYPE);
        expectedLookups.addAll(REFERENCE_TYPES);
        doTest("function test(<caret>)", expectedLookups.toArray(new String[expectedLookups.size()]));
    }

    public void testFunctionParamWithImports() {
        List<String> expectedLookups = new LinkedList<>();
        expectedLookups.addAll(DATA_TYPES);
        expectedLookups.addAll(ANY_TYPE);
        expectedLookups.addAll(XMLNS_TYPE);
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
        expectedLookups.addAll(ANY_TYPE);
        expectedLookups.addAll(XMLNS_TYPE);
        expectedLookups.addAll(REFERENCE_TYPES);
        doTest("function test(string s,<caret>)", expectedLookups.toArray(new String[expectedLookups.size()]));
    }

    public void testCaretBeforeFunctionParamWithoutImports() {
        List<String> expectedLookups = new LinkedList<>();
        expectedLookups.addAll(DATA_TYPES);
        expectedLookups.addAll(ANY_TYPE);
        expectedLookups.addAll(XMLNS_TYPE);
        expectedLookups.addAll(REFERENCE_TYPES);
        doTest("function test(<caret>string s)", expectedLookups.toArray(new String[expectedLookups.size()]));
    }

    public void testCaretAfterFunctionParamWithImports() {
        List<String> expectedLookups = new LinkedList<>();
        expectedLookups.addAll(DATA_TYPES);
        expectedLookups.addAll(ANY_TYPE);
        expectedLookups.addAll(XMLNS_TYPE);
        expectedLookups.addAll(REFERENCE_TYPES);
        expectedLookups.add("test");
        doTest("import org.test; function test(string s,<caret>)",
                expectedLookups.toArray(new String[expectedLookups.size()]));
    }

    public void testCaretBeforeFunctionParamWithImports() {
        List<String> expectedLookups = new LinkedList<>();
        expectedLookups.addAll(DATA_TYPES);
        expectedLookups.addAll(ANY_TYPE);
        expectedLookups.addAll(XMLNS_TYPE);
        expectedLookups.addAll(REFERENCE_TYPES);
        expectedLookups.add("test");
        doTest("import org.test; function test(<caret>string s)",
                expectedLookups.toArray(new String[expectedLookups.size()]));
    }

    public void testParamAnnotationsPackage() {
        myFixture.addFileToProject("org/test/file.bal", "annotation TEST attach parameter {}");
        doTest("import org.test; function A(@<caret>)", "test");
    }

    public void testParamAnnotationsPackageAutoCompletion() {
        myFixture.addFileToProject("org/test/file.bal", "annotation TEST attach parameter {}");
        doCheckResult("test.bal", "import org.test; function A(@te<caret>)",
                "import org.test; function A(@test:)", null);
    }

    public void testParamAnnotationsFromAPackage() {
        myFixture.addFileToProject("org/test/file.bal", "annotation TEST attach parameter {}");
        doTest("import org.test; function A(@test:<caret>)", "TEST");
    }

    public void testParamAnnotationsFromAPackageAutoCompletion() {
        myFixture.addFileToProject("org/test/file.bal", "annotation TEST attach parameter {}");
        doCheckResult("test.bal", "import org.test; function A(@test:T<caret>)",
                "import org.test; function A(@test:TEST {})", null);
    }

    public void testPackageInvocationInParameter() {
        myFixture.addFileToProject("org/test/file.bal", "struct test {}");
        doTest("import org.test; function A(test:<caret>)", "test");
    }

    public void testPackageInvocationInParameterAutoCompletion() {
        myFixture.addFileToProject("org/test/file.bal", "struct test {}");
        doCheckResult("test.bal", "import org.test; function A(test:t<caret>)",
                "import org.test; function A(test:test)", null);
    }

    // todo - query param annotations

    /**
     * Test service level lookups.
     */
    public void testServiceIdentifier() {
        doTest("service <caret>");
    }

    public void testServiceBody() {
        List<String> expectedLookups = new LinkedList<>();
        expectedLookups.addAll(DATA_TYPES);
        expectedLookups.addAll(ANY_TYPE);
        expectedLookups.addAll(XMLNS_TYPE);
        expectedLookups.addAll(REFERENCE_TYPES);
        expectedLookups.add("resource");
        doTest("service<http> S{<caret>}", expectedLookups.toArray(new String[expectedLookups.size()]));
    }

    public void testServiceBodyAfterAnnotation() {
        doTest("service<http> S{ @http:GET {} <caret>}", "resource");
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

    public void testServiceSourceNotation() {
        doTest("import org.test; service<<caret>> S {}", "test");
    }

    public void testServiceSourceNotationAutoCompletion() {
        doCheckResult("test.bal", "import org.test; service<t<caret>> S {}",
                "import org.test; service<test> S {}", null);
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
        doCheckResult("test.bal", "import org.test; service<http> S{<caret>}", null, '@', "test");
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
        doCheckResult("test.bal", "import org.test; service<http> S{@test:<caret>}", null, null, "TEST");
    }

    public void testResourceAnnotationWithImportsWithMatchingAnnotationDefinitionsAutoCompletion() {
        myFixture.addFileToProject("org/test/file.bal", "package org.test; annotation TEST attach resource {}");
        doCheckResult("test.bal", "import org.test; service<http> S{@test:T<caret>}",
                "import org.test; service<http> S{@test:TEST {}}", null);
    }

    public void testResourceAnnotationInCurrentPackageSameFile() {
        doCheckResult("test.bal", "annotation TEST attach resource {} service<http> S{<caret>}", null, '@', "TEST");
    }

    public void testResourceAnnotationInCurrentPackageSameFileAutoComplete() {
        doCheckResult("test.bal", "annotation TEST attach resource {} service<http> S{@T<caret>}",
                "annotation TEST attach resource {} service<http> S{@TEST {}}", null);
    }

    public void testResourceAnnotationInCurrentPackageDifferentFile() {
        myFixture.addFileToProject("file.bal", "annotation TEST attach resource {}");
        doCheckResult("test.bal", "service<http> S{<caret>}", null, '@', "TEST");
    }

    public void testResourceAnnotationInCurrentPackageDifferentFileAutoComplete() {
        myFixture.addFileToProject("file.bal", "annotation TEST attach resource {}");
        doCheckResult("test.bal", "service<http> S{@T<caret>}", "service<http> S{@TEST {}}", null);
    }

    public void testResourceAnnotationInCurrentPackageDifferentFileHasMoreDefinitionsAfter() {
        myFixture.addFileToProject("file.bal", "annotation TEST attach resource {}");
        doCheckResult("test.bal", "service<http> S{<caret>} service R{}", null, '@', "TEST");
    }

    public void testResourceAnnotationInCurrentPackageSameFileHasMoreDefinitionsAfter() {
        doCheckResult("test.bal", "annotation TEST attach resource {} service<http> S{<caret>} service R{}", null, '@',
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
        expectedLookups.addAll(ANY_TYPE);
        expectedLookups.addAll(XMLNS_TYPE);
        expectedLookups.addAll(REFERENCE_TYPES);
        doTest("service<http> S { resource R(<caret>)", expectedLookups.toArray(new String[expectedLookups.size()]));
    }

    public void testResourceParamWithImports() {
        List<String> expectedLookups = new LinkedList<>();
        expectedLookups.addAll(DATA_TYPES);
        expectedLookups.addAll(ANY_TYPE);
        expectedLookups.addAll(XMLNS_TYPE);
        expectedLookups.addAll(REFERENCE_TYPES);
        expectedLookups.add("test");
        doTest("import org.test; service<http> S { resource R(<caret>)",
                expectedLookups.toArray(new String[expectedLookups.size()]));
    }

    public void testResourceParamWithoutImportsAutoCompletion() {
        doCheckResult("test.bal", "service<http> S { resource R(st<caret>)", "service<http> S { resource R(string )",
                null);
    }

    public void testResourceParamWithImportsAutoCompletion() {
        doCheckResult("test.bal", "import org.test; service<http> S { resource R(te<caret>)",
                "import org.test; service<http> S { resource R(test:)", null);
    }

    public void testCaretAfterResourceParamWithoutImports() {
        List<String> expectedLookups = new LinkedList<>();
        expectedLookups.addAll(DATA_TYPES);
        expectedLookups.addAll(ANY_TYPE);
        expectedLookups.addAll(XMLNS_TYPE);
        expectedLookups.addAll(REFERENCE_TYPES);
        doTest("service<http> S { resource R(string s,<caret>)", expectedLookups.toArray(new String[expectedLookups
                .size()]));
    }

    public void testCaretBeforeResourceParamWithoutImports() {
        List<String> expectedLookups = new LinkedList<>();
        expectedLookups.addAll(DATA_TYPES);
        expectedLookups.addAll(ANY_TYPE);
        expectedLookups.addAll(XMLNS_TYPE);
        expectedLookups.addAll(REFERENCE_TYPES);
        doTest("service<http> S { resource R(<caret>string s)", expectedLookups.toArray(new String[expectedLookups
                .size()]));
    }

    public void testCaretAfterResourceParamWithImports() {
        List<String> expectedLookups = new LinkedList<>();
        expectedLookups.addAll(DATA_TYPES);
        expectedLookups.addAll(ANY_TYPE);
        expectedLookups.addAll(XMLNS_TYPE);
        expectedLookups.addAll(REFERENCE_TYPES);
        expectedLookups.add("test");
        doTest("import org.test; service<http> S { resource R(string s,<caret>)",
                expectedLookups.toArray(new String[expectedLookups.size()]));
    }

    public void testCaretBeforeResourceParamWithImports() {
        List<String> expectedLookups = new LinkedList<>();
        expectedLookups.addAll(DATA_TYPES);
        expectedLookups.addAll(ANY_TYPE);
        expectedLookups.addAll(XMLNS_TYPE);
        expectedLookups.addAll(REFERENCE_TYPES);
        expectedLookups.add("test");
        doTest("import org.test; service<http> S { resource R(<caret>string s)",
                expectedLookups.toArray(new String[expectedLookups.size()]));
    }

    /**
     * Test connector level lookups.
     */
    public void testConnectorIdentifier() {
        doTest("connector <caret>");
    }

    public void testConnectorAnnotation() {
        doCheckResult("test.bal", "@<caret> connector A(){}", null, '@');
    }

    public void testConnectorAnnotationWithImports() {
        doCheckResult("test.bal", "import org.test; <caret>connector A(){}", null, '@', "test");
    }

    public void testConnectorAnnotationWithImportsNoAnnotationDefinitions() {
        myFixture.addFileToProject("org/test/file.bal", "function test(){}");
        doCheckResult("test.bal", "import org.test; @test<caret> connector A(){}", null, ':');
    }

    public void testConnectorAnnotationWithImportsWithNoMatchingAnnotationDefinitions() {
        myFixture.addFileToProject("org/test/file.bal", "annotation TEST attach test {}");
        doCheckResult("test.bal", "import org.test; @test:<caret> connector A(){}", null, null);
    }

    public void testConnectorAnnotationWithImportsWithMatchingAnnotationDefinitions() {
        myFixture.addFileToProject("org/test/file.bal", "package org.test; annotation TEST attach connector {} " +
                "annotation TEST2 attach resource {}");
        doCheckResult("test.bal", "import org.test; @test:<caret> connector A(){}", null, null, "TEST");
    }

    public void testConnectorAnnotationWithImportsWithMatchingAnnotationDefinitionsAutoCompletion() {
        myFixture.addFileToProject("org/test/file.bal", "package org.test; annotation TEST attach connector {}");
        doCheckResult("test.bal", "import org.test; @test:T<caret> connector A(){}",
                "import org.test; @test:TEST {} connector A(){}", null);
    }

    public void testConnectorAnnotationInCurrentPackageSameFile() {
        doCheckResult("test.bal", "annotation TEST attach connector {} <caret> connector A(){}", null, '@', "TEST");
    }

    public void testConnectorAnnotationInCurrentPackageSameFileAutoComplete() {
        doCheckResult("test.bal", "annotation TEST attach connector {} @T<caret> connector A(){}",
                "annotation TEST attach connector {} @TEST {} connector A(){}", null);
    }

    public void testConnectorAnnotationInCurrentPackageDifferentFile() {
        myFixture.addFileToProject("file.bal", "annotation TEST attach connector {}");
        doCheckResult("test.bal", "<caret> connector A(){}", null, '@', "TEST");
    }

    public void testConnectorAnnotationInCurrentPackageDifferentFileAutoComplete() {
        myFixture.addFileToProject("file.bal", "annotation TEST attach connector {}");
        doCheckResult("test.bal", "@T<caret> connector A(){}", "@TEST {} connector A(){}", null);
    }

    public void testConnectorAnnotationInCurrentPackageDifferentFileHasMoreDefinitionsAfter() {
        myFixture.addFileToProject("file.bal", "annotation TEST attach connector {}");
        doCheckResult("test.bal", "<caret> connector A(){} service R{}", null, '@', "TEST");
    }

    public void testConnectorAnnotationInCurrentPackageSameFileHasMoreDefinitionsAfter() {
        doCheckResult("test.bal", "annotation TEST attach connector {} <caret> connector A(){} service R{}", null,
                '@', "TEST");
    }

    public void testConnectorBody() {
        List<String> expectedLookups = new LinkedList<>();
        expectedLookups.addAll(DATA_TYPES);
        expectedLookups.addAll(ANY_TYPE);
        expectedLookups.addAll(XMLNS_TYPE);
        expectedLookups.addAll(REFERENCE_TYPES);
        expectedLookups.add("C");
        expectedLookups.add("action");
        doTest("connector C(){ <caret> }", expectedLookups.toArray(new String[expectedLookups.size()]));
    }

    public void testConnectorBodyAfterAnnotation() {
        doTest("connector C(){ @test:test{} <caret> }", "action");
    }

    public void testConnectorBodyVariableDeclarationPackage() {
        myFixture.addFileToProject("org/test/file.bal", "package org.test; struct TEST {}");
        doCheckResult("test.bal", "import org.test; connector C(){ te<caret> }",
                "import org.test; connector C(){ test: }", null);
    }

    public void testConnectorBodyVariableDeclarationPackageInvocation() {
        myFixture.addFileToProject("org/test/file.bal", "package org.test; connector TEST () {}");
        doTest("import org.test; connector C(){ test:<caret> }", "TEST");
    }

    public void testConnectorBodyVariableDeclarationPackageInvocationAutoCompletion() {
        myFixture.addFileToProject("org/test/file.bal", "package org.test; connector TEST () {}");
        doCheckResult("test.bal", "import org.test; connector C(){ test:T<caret> }",
                "import org.test; connector C(){ test:TEST }", null);
    }

    public void testConnectorBodyVariableDeclarationIdentifier() {
        myFixture.addFileToProject("org/test/file.bal", "package org.test; connector TEST () {}");
        doTest("import org.test; connector C(){ test:TEST <caret> }");
    }

    public void testConnectorBodyVariableInitialization() {
        myFixture.addFileToProject("org/test/file.bal", "package org.test; connector TEST () {}");
        doTest("import org.test; connector C(){ test:TEST t = <caret> }", "create", "C", "test");
    }

    public void testConnectorBodyVariableInitializationCreateKeyword() {
        myFixture.addFileToProject("org/test/file.bal", "package org.test; connector TEST () {}");
        doTest("import org.test; connector C(){ test:TEST t = create <caret> }", "C", "test");
    }

    public void testConnectorBodyVariableInitializationPackageInvocation() {
        myFixture.addFileToProject("org/test/file.bal", "package org.test; connector TEST () {}");
        doCheckResult("test.bal", "import org.test; connector C(){ test:TEST t = create t<caret> }",
                "import org.test; connector C(){ test:TEST t = create test: }", null);
    }

    public void testConnectorBodyVariableInitializationPackageAutoCompletion() {
        myFixture.addFileToProject("org/test/file.bal", "package org.test; connector TEST () {}");
        doTest("import org.test; connector C(){ test:TEST t = create test:<caret> }", "TEST");
    }

    public void testConnectorBodyVariableInitializationPackageInvocationAutoCompletion() {
        myFixture.addFileToProject("org/test/file.bal", "package org.test; connector TEST () {}");
        doCheckResult("test.bal", "import org.test; connector C(){ test:TEST t = create test:T<caret> }",
                "import org.test; connector C(){ test:TEST t = create test:TEST }", null);
    }

    /**
     * Test action level lookups.
     */
    public void testActionIdentifier() {
        doTest("connector C(){ action <caret>}");
    }

    public void testActionAnnotation() {
        doCheckResult("test.bal", "connector C(){ @<caret> action A()(message) {} }", null, '@');
    }

    public void testActionAnnotationWithImports() {
        doCheckResult("test.bal", "import org.test; connector C(){ <caret> action A()(message) {} }", null, '@',
                "test");
    }

    public void testActionAnnotationWithImportsNoAnnotationDefinitions() {
        myFixture.addFileToProject("org/test/file.bal", "function test(){}");
        doCheckResult("test.bal", "import org.test; connector C(){ @tes<caret> action A()(message) {} }",
                "import org.test; connector C(){ @test: action A()(message) {} }", null);
    }

    public void testActionAnnotationWithImportsWithNoMatchingAnnotationDefinitions() {
        myFixture.addFileToProject("org/test/file.bal", "annotation TEST attach test {}");
        doCheckResult("test.bal", "import org.test; connector C(){ @test:<caret> action A()(message) {} }", null,
                null);
    }

    public void testActionAnnotationWithImportsWithMatchingAnnotationDefinitions() {
        myFixture.addFileToProject("org/test/file.bal", "package org.test; annotation TEST attach action {} " +
                "annotation TEST2 attach resource {}");
        doCheckResult("test.bal", "import org.test; connector C(){ @test:<caret> action A()(message) {} }", null,
                null, "TEST");
    }

    public void testActionAnnotationWithImportsWithMatchingAnnotationDefinitionsAutoCompletion() {
        myFixture.addFileToProject("org/test/file.bal", "package org.test; annotation TEST attach action {}");
        doCheckResult("test.bal", "import org.test; connector C(){ @test:T<caret> action A()(message) {} }",
                "import org.test; connector C(){ @test:TEST {} action A()(message) {} }", null);
    }

    public void testActionAnnotationInCurrentPackageSameFile() {
        doCheckResult("test.bal", "annotation TEST attach action {} connector C(){ <caret> action A()(message) " +
                "{} }", null, '@', "TEST");
    }

    public void testActionAnnotationInCurrentPackageSameFileAutoComplete() {
        doCheckResult("test.bal", "annotation TEST attach action {} connector C(){ @T<caret> action A()(message) {}" +
                " }", "annotation TEST attach action {} connector C(){ @TEST {} action A()(message) {} }", null);
    }

    public void testActionAnnotationInCurrentPackageDifferentFile() {
        myFixture.addFileToProject("file.bal", "annotation TEST attach action {}");
        doCheckResult("test.bal", "connector C(){ <caret> action A()(message) {} }", null, '@', "TEST");
    }

    public void testActionAnnotationInCurrentPackageDifferentFileAutoComplete() {
        myFixture.addFileToProject("file.bal", "annotation TEST attach action {}");
        doCheckResult("test.bal", "connector C(){ @T<caret> action A()(message) {} }",
                "connector C(){ @TEST {} action A()(message) {} }", null);
    }

    public void testActionAnnotationInCurrentPackageDifferentFileHasMoreDefinitionsAfter() {
        myFixture.addFileToProject("file.bal", "annotation TEST attach action {}");
        doCheckResult("test.bal", "connector C(){ @<caret> action A()(message) {} } service R{}", null, null,
                "TEST");
    }

    public void testActionAnnotationInCurrentPackageSameFileHasMoreDefinitionsAfter() {
        doCheckResult("test.bal", "annotation TEST attach action {} connector C(){ @<caret> action A()(message) {} " +
                "}" + " service R{}", null, null, "TEST");
    }

    /**
     * Test struct level lookups.
     */
    public void testStructIdentifier() {
        doTest("struct <caret>");
    }

    public void testStructAnnotation() {
        doCheckResult("test.bal", "@<caret> struct S{}", null, '@');
    }

    public void testStructAnnotationWithImports() {
        doCheckResult("test.bal", "import org.test; <caret>struct S{}", null, '@', "test");
    }

    public void testStructAnnotationWithImportsNoAnnotationDefinitions() {
        myFixture.addFileToProject("org/test/file.bal", "function test(){}");
        doCheckResult("test.bal", "import org.test; @test<caret> struct S{}", null, ':');
    }

    public void testStructAnnotationWithImportsWithNoMatchingAnnotationDefinitions() {
        myFixture.addFileToProject("org/test/file.bal", "annotation TEST attach test {}");
        doCheckResult("test.bal", "import org.test; @test:<caret> struct S{}", null, null);
    }

    public void testStructAnnotationWithImportsWithMatchingAnnotationDefinitions() {
        myFixture.addFileToProject("org/test/file.bal", "package org.test; annotation TEST attach struct {} " +
                "annotation TEST2 attach resource {}");
        doCheckResult("test.bal", "import org.test; @test:<caret> struct S{}", null, null, "TEST");
    }

    public void testStructAnnotationWithImportsWithMatchingAnnotationDefinitionsAutoCompletion() {
        myFixture.addFileToProject("org/test/file.bal", "package org.test; annotation TEST attach struct {}");
        doCheckResult("test.bal", "import org.test; @test:T<caret> struct S{}",
                "import org.test; @test:TEST {} struct S{}", null);
    }

    public void testStructAnnotationInCurrentPackageSameFile() {
        doCheckResult("test.bal", "annotation TEST attach struct {} <caret> struct S{}", null, '@', "TEST");
    }

    public void testStructAnnotationInCurrentPackageSameFileAutoComplete() {
        doCheckResult("test.bal", "annotation TEST attach struct {} @T<caret> struct S{}",
                "annotation TEST attach struct {} @TEST {} struct S{}", null);
    }

    public void testStructAnnotationInCurrentPackageDifferentFile() {
        myFixture.addFileToProject("file.bal", "annotation TEST attach struct {}");
        doCheckResult("test.bal", "<caret> struct S{}", null, '@', "TEST");
    }

    public void testStructAnnotationInCurrentPackageDifferentFileAutoComplete() {
        myFixture.addFileToProject("file.bal", "annotation TEST attach struct {}");
        doCheckResult("test.bal", "@T<caret> struct S{}", "@TEST {} struct S{}", null);
    }

    public void testStructAnnotationInCurrentPackageDifferentFileHasMoreDefinitionsAfter() {
        myFixture.addFileToProject("file.bal", "annotation TEST attach struct {}");
        doCheckResult("test.bal", "<caret> struct S{} service R{}", null, '@', "TEST");
    }

    public void testStructAnnotationInCurrentPackageSameFileHasMoreDefinitionsAfter() {
        doCheckResult("test.bal", "annotation TEST attach struct {} <caret> struct S{} service R{}", null,
                '@', "TEST");
    }

    public void testSingleLevelStructInSameFile() {
        doCheckResult("test.bal", "struct Name { string firstName; } function test(){ Name name = { f<caret> }; }",
                "struct Name { string firstName; } function test(){ Name name = { firstName: }; }", null);
    }

    public void testSingleLevelStructInSameFileValue() {
        doTest("struct Name { string firstName; } function test(){ string name=\"\"; Name name = { " +
                "firstName:<caret> }; }", "name", "test", "false", "null", "true");
    }

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
     * Test typemapper level lookups.
     */
    public void testTypeMapperIdentifier() {
        doTest("typemapper <caret>");
    }

    public void testTypeMapperAnnotation() {
        doCheckResult("test.bal", "@<caret> typemapper T(int)(string) {}", null, '@');
    }

    public void testTypeMapperAnnotationWithImports() {
        doCheckResult("test.bal", "import org.test; <caret>typemapper T(int)(string) {}", null, '@', "test");
    }

    public void testTypeMapperAnnotationWithImportsNoAnnotationDefinitions() {
        myFixture.addFileToProject("org/test/file.bal", "function test(){}");
        doCheckResult("test.bal", "import org.test; @test<caret> typemapper T(int)(string) {}", null, ':');
    }

    public void testTypeMapperAnnotationWithImportsWithNoMatchingAnnotationDefinitions() {
        myFixture.addFileToProject("org/test/file.bal", "annotation TEST attach test {}");
        doCheckResult("test.bal", "import org.test; @test:<caret> typemapper T(int)(string) {}", null, null);
    }

    public void testTypeMapperAnnotationWithImportsWithMatchingAnnotationDefinitions() {
        myFixture.addFileToProject("org/test/file.bal", "package org.test; annotation TEST attach typemapper {} " +
                "annotation TEST2 attach resource {}");
        doCheckResult("test.bal", "import org.test; @test:<caret> typemapper T(int)(string) {}", null, null, "TEST");
    }

    public void testTypeMapperAnnotationWithImportsWithMatchingAnnotationDefinitionsAutoCompletion() {
        myFixture.addFileToProject("org/test/file.bal", "package org.test; annotation TEST attach typemapper {}");
        doCheckResult("test.bal", "import org.test; @test:T<caret> typemapper T(int)(string) {}",
                "import org.test; @test:TEST {} typemapper T(int)(string) {}", null);
    }

    public void testTypeMapperAnnotationInCurrentPackageSameFile() {
        doCheckResult("test.bal", "annotation TEST attach typemapper {} <caret> typemapper T(int)(string) {}", null,
                '@', "TEST");
    }

    public void testTypeMapperAnnotationInCurrentPackageSameFileAutoComplete() {
        doCheckResult("test.bal", "annotation TEST attach typemapper {} @T<caret> typemapper T(int)(string) {}",
                "annotation TEST attach typemapper {} @TEST {} typemapper T(int)(string) {}", null);
    }

    public void testTypeMapperAnnotationInCurrentPackageDifferentFile() {
        myFixture.addFileToProject("file.bal", "annotation TEST attach typemapper {}");
        doCheckResult("test.bal", "<caret> typemapper T(int)(string) {}", null, '@', "TEST");
    }

    public void testTypeMapperAnnotationInCurrentPackageDifferentFileAutoComplete() {
        myFixture.addFileToProject("file.bal", "annotation TEST attach typemapper {}");
        doCheckResult("test.bal", "@T<caret> typemapper T(int)(string) {}", "@TEST {} typemapper T(int)(string) {}",
                null);
    }

    public void testTypeMapperAnnotationInCurrentPackageDifferentFileHasMoreDefinitionsAfter() {
        myFixture.addFileToProject("file.bal", "annotation TEST attach typemapper {}");
        doCheckResult("test.bal", "<caret> typemapper T(int)(string) {} service R{}", null, '@', "TEST");
    }

    public void testTypeMapperAnnotationInCurrentPackageSameFileHasMoreDefinitionsAfter() {
        doCheckResult("test.bal", "annotation TEST attach typemapper {} <caret> typemapper T(int)(string) {} " +
                "service R{}", null, '@', "TEST");
    }

    /**
     * Hidden templates.
     */
    public void testPackageKeyword() {
        doCheckResult("test.bal", "package<caret>", "package ", null);
    }

    public void testImportKeyword() {
        doCheckResult("test.bal", "import<caret>", "import ", null);
    }

    public void testIfKeyword() {
        doCheckResult("test.bal", "function test(){ if<caret> }", "function test(){ if () {\n    \n} }", null);
    }

    public void testElseKeyword() {
        doCheckResult("test.bal", "function test(){ else<caret> }", "function test(){ else {\n    \n} }", null);
    }

    public void testForkKeyword() {
        doCheckResult("test.bal", "function test(){ fork<caret> }", "function test(){ fork {\n    \n} }", null);
    }

    public void testJoinKeyword() {
        doCheckResult("test.bal", "function test(){ join<caret> }", "function test(){ join () (message[] ) {\n    \n}" +
                " }", null);
    }

    public void testTimeoutKeyword() {
        doCheckResult("test.bal", "function test(){ timeout<caret> }", "function test(){ timeout () (message[] ) " +
                "{\n    \n} }", null);
    }

    public void testWorkerKeyword() {
        doCheckResult("test.bal", "function test(){ worker<caret> }", "function test(){ worker {\n    \n} }", null);
    }

    public void testTransformKeyword() {
        doCheckResult("test.bal", "function test(){ transform<caret> }", "function test(){ transform {\n    \n} }",
                null);
    }

    public void testTransactionKeyword() {
        doCheckResult("test.bal", "function test(){ transaction<caret> }", "function test(){ transaction {\n    \n} " +
                "}", null);
    }

    public void testAbortedKeyword() {
        doCheckResult("test.bal", "function test(){ aborted<caret> }", "function test(){ aborted {\n    \n} }", null);
    }

    public void testCommittedKeyword() {
        doCheckResult("test.bal", "function test(){ committed<caret> }", "function test(){ committed {\n    \n} }",
                null);
    }

    public void testTryKeyword() {
        doCheckResult("test.bal", "function test(){ try<caret> }", "function test(){ try {\n    \n} }", null);
    }

    public void testCatchKeyword() {
        doCheckResult("test.bal", "function test(){ catch<caret> }", "function test(){ catch () {\n    \n} }",
                null);
    }

    public void testFinallyKeyword() {
        doCheckResult("test.bal", "function test(){ finally<caret> }", "function test(){ finally {\n    \n} }", null);
    }

    public void testIterateKeyword() {
        doCheckResult("test.bal", "function test(){ iterate<caret> }", "function test(){ iterate ( : ) {\n    \n} }",
                null);
    }

    public void testWhileKeyword() {
        doCheckResult("test.bal", "function test(){ while<caret> }", "function test(){ while () {\n    \n} }",
                null);
    }

    public void testContinueKeyword() {
        doCheckResult("test.bal", "function test(){ continue<caret> }", "function test(){ continue; }", null);
    }

    public void testBreakKeyword() {
        doCheckResult("test.bal", "function test(){ break<caret> }", "function test(){ break; }", null);
    }

    public void testThrowKeyword() {
        doCheckResult("test.bal", "function test(){ throw<caret> }", "function test(){ throw ; }", null);
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

    public void testCommonKeywordsAfterStatement() {
        List<String> expectedLookups = new LinkedList<>();
        expectedLookups.addAll(DATA_TYPES);
        expectedLookups.addAll(ANY_TYPE);
        expectedLookups.addAll(XMLNS_TYPE);
        expectedLookups.addAll(REFERENCE_TYPES);
        expectedLookups.addAll(COMMON_KEYWORDS);
        expectedLookups.addAll(VALUE_KEYWORDS);
        expectedLookups.addAll(FUNCTION_LEVEL_KEYWORDS);
        expectedLookups.add("a");
        expectedLookups.add("test");
        doTest("function test(){ int a; <caret> }", expectedLookups.toArray(new String[expectedLookups.size()]));
    }

    public void testCommonKeywordsBeforeStatement() {
        List<String> expectedLookups = new LinkedList<>();
        expectedLookups.addAll(DATA_TYPES);
        expectedLookups.addAll(ANY_TYPE);
        expectedLookups.addAll(XMLNS_TYPE);
        expectedLookups.addAll(REFERENCE_TYPES);
        expectedLookups.addAll(COMMON_KEYWORDS);
        expectedLookups.addAll(VALUE_KEYWORDS);
        expectedLookups.addAll(FUNCTION_LEVEL_KEYWORDS);
        expectedLookups.add("test");
        doTest("function test(){ <caret> int a; }", expectedLookups.toArray(new String[expectedLookups.size()]));
    }

    public void testCommonKeywordsBetweenStatements() {
        List<String> expectedLookups = new LinkedList<>();
        expectedLookups.addAll(DATA_TYPES);
        expectedLookups.addAll(ANY_TYPE);
        expectedLookups.addAll(XMLNS_TYPE);
        expectedLookups.addAll(REFERENCE_TYPES);
        expectedLookups.addAll(COMMON_KEYWORDS);
        expectedLookups.addAll(VALUE_KEYWORDS);
        expectedLookups.addAll(FUNCTION_LEVEL_KEYWORDS);
        expectedLookups.add("a");
        expectedLookups.add("test");
        doTest("function test(){ int a; <caret> int b; }", expectedLookups.toArray(new String[expectedLookups.size()]));
    }

    public void testSuggestionsInTransformStatement() {
        List<String> expectedLookups = new LinkedList<>();
        expectedLookups.addAll(DATA_TYPES);
        expectedLookups.addAll(ANY_TYPE);
        expectedLookups.addAll(XMLNS_TYPE);
        expectedLookups.addAll(REFERENCE_TYPES);
        expectedLookups.addAll(COMMON_KEYWORDS);
        expectedLookups.addAll(VALUE_KEYWORDS);
        expectedLookups.addAll(FUNCTION_LEVEL_KEYWORDS);
        expectedLookups.add("a");
        expectedLookups.add("b");
        expectedLookups.add("g");
        expectedLookups.add("test");
        doTest("int g = 1; function test(){ int a = 10; transform { int b = 5; <caret> } }",
                expectedLookups.toArray(new String[expectedLookups.size()]));
    }

    // todo -  test resource level specific keywords

}
