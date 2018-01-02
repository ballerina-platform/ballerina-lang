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

public class BallerinaFileLevelCompletionTest extends BallerinaCompletionTestBase {

    /**
     * Test file level lookups.
     */
    public void testEmptyFile() {
        List<String> expectedLookups = new LinkedList<>();
        expectedLookups.addAll(FILE_LEVEL_KEYWORDS);
        expectedLookups.addAll(OTHER_TYPES);
        expectedLookups.addAll(XMLNS_TYPE);
        expectedLookups.addAll(DATA_TYPES);
        expectedLookups.addAll(REFERENCE_TYPES);
        doTest("<caret>", expectedLookups.toArray(new String[expectedLookups.size()]));
    }

    public void testEmptyFilePackageKeyword() {
        doTest("p<caret>", "public", "import", "package", "map", "type");
    }

    public void testEmptyFileImportKeyword() {
        doTest("i<caret>", "public", "annotation", "function", "import", "service", "int", "string");
    }

    public void testEmptyFileWithSpaceBeforeCaret() {
        List<String> expectedLookups = new LinkedList<>();
        expectedLookups.addAll(FILE_LEVEL_KEYWORDS);
        expectedLookups.addAll(OTHER_TYPES);
        expectedLookups.addAll(XMLNS_TYPE);
        expectedLookups.addAll(DATA_TYPES);
        expectedLookups.addAll(REFERENCE_TYPES);
        doTest("\n<caret>", expectedLookups.toArray(new String[expectedLookups.size()]));
    }

    public void testEmptyFileWithSpaceBeforeCaretPackageKeyword() {
        doTest("\np<caret>", "public", "import", "package", "map", "type");
    }

    public void testEmptyFileWithSpaceBeforeCaretImportKeyword() {
        doTest("\ni<caret>", "public", "annotation", "function", "import", "service", "int", "string");
    }

    public void testEmptyFileWithSpaceAfterCaret() {
        List<String> expectedLookups = new LinkedList<>();
        expectedLookups.addAll(FILE_LEVEL_KEYWORDS);
        expectedLookups.addAll(OTHER_TYPES);
        expectedLookups.addAll(XMLNS_TYPE);
        expectedLookups.addAll(DATA_TYPES);
        expectedLookups.addAll(REFERENCE_TYPES);
        doTest("<caret>\n", expectedLookups.toArray(new String[expectedLookups.size()]));
    }

    public void testEmptyFileWithSpaceAfterCaretPackageKeyword() {
        doTest("p<caret>\n", "public", "import", "package", "map", "type");
    }

    public void testEmptyFileWithSpaceAfterCaretImportKeyword() {
        doTest("i<caret>\n", "public", "annotation", "function", "import", "service", "int", "string");
    }

    public void testEmptyFileWithSpaces() {
        List<String> expectedLookups = new LinkedList<>();
        expectedLookups.addAll(FILE_LEVEL_KEYWORDS);
        expectedLookups.addAll(OTHER_TYPES);
        expectedLookups.addAll(XMLNS_TYPE);
        expectedLookups.addAll(DATA_TYPES);
        expectedLookups.addAll(REFERENCE_TYPES);
        doTest("\n<caret>\n", expectedLookups.toArray(new String[expectedLookups.size()]));
    }

    public void testEmptyFileWithSpacesPackageKeyword() {
        doTest("\np<caret>\n", "public", "import", "package", "map", "type");
    }

    public void testEmptyFileWithSpacesImportKeyword() {
        doTest("\ni<caret>\n", "public", "annotation", "function", "import", "service", "int", "string");
    }

    public void testImportAfterPackage() {
        List<String> expectedLookups = new LinkedList<>();
        expectedLookups.addAll(OTHER_TYPES);
        expectedLookups.addAll(XMLNS_TYPE);
        expectedLookups.addAll(DATA_TYPES);
        expectedLookups.addAll(REFERENCE_TYPES);
        expectedLookups.add("public");
        expectedLookups.add("import");
        expectedLookups.add("const");
        expectedLookups.add("service");
        expectedLookups.add("function");
        expectedLookups.add("connector");
        expectedLookups.add("struct");
        expectedLookups.add("annotation");
        expectedLookups.add("enum");
        expectedLookups.add("transformer");
        doTest("package test; \n<caret>\n", expectedLookups.toArray(new String[expectedLookups.size()]));
    }

    public void testImportAfterPackagePartialIdentifier() {
        doTest("package test; \ni<caret>\n", "public", "annotation", "function", "import", "service", "int", "string");
    }

    public void testImportAfterPackageBeforeFunction() {
        List<String> expectedLookups = new LinkedList<>();
        expectedLookups.addAll(OTHER_TYPES);
        expectedLookups.addAll(XMLNS_TYPE);
        expectedLookups.addAll(DATA_TYPES);
        expectedLookups.addAll(REFERENCE_TYPES);
        expectedLookups.add("public");
        expectedLookups.add("import");
        expectedLookups.add("const");
        expectedLookups.add("service");
        expectedLookups.add("function");
        expectedLookups.add("connector");
        expectedLookups.add("struct");
        expectedLookups.add("annotation");
        expectedLookups.add("enum");
        expectedLookups.add("transformer");
        doTest("package test; \n<caret>\nfunction A(){}", expectedLookups.toArray(new String[expectedLookups.size()]));
    }

    public void testImportAfterPackageBeforeFunctionPartialIdentifier() {
        doTest("package test; \ni<caret>\nfunction A(){}", "public", "annotation", "function", "import", "service",
                "int", "string");
    }

    public void testPackageBeforeImport() {
        List<String> expectedLookups = new LinkedList<>();
        expectedLookups.addAll(FILE_LEVEL_KEYWORDS);
        expectedLookups.addAll(OTHER_TYPES);
        expectedLookups.addAll(XMLNS_TYPE);
        expectedLookups.addAll(DATA_TYPES);
        expectedLookups.addAll(REFERENCE_TYPES);
        doTest("<caret>\nimport test; \nfunction A(){}", expectedLookups.toArray(new String[expectedLookups.size()]));
    }

    public void testPackageBeforeImportPartialIdentifier() {
        doTest("p<caret>\nimport test; \nfunction A(){}", "public", "import", "package", "map", "type");
    }

    public void testImportBeforeImport() {
        List<String> expectedLookups = new LinkedList<>();
        expectedLookups.addAll(FILE_LEVEL_KEYWORDS);
        expectedLookups.addAll(OTHER_TYPES);
        expectedLookups.addAll(XMLNS_TYPE);
        expectedLookups.addAll(DATA_TYPES);
        expectedLookups.addAll(REFERENCE_TYPES);
        doTest("<caret>\nimport test; \nfunction A(){}", expectedLookups.toArray(new String[expectedLookups.size()]));
    }

    public void testImportBeforeImportPartialIdentifier() {
        doTest("i<caret>\nimport test; \nfunction A(){}", "public", "annotation", "function", "import", "service",
                "int", "string");
    }

    public void testImportAfterImport() {
        List<String> expectedLookups = new LinkedList<>();
        expectedLookups.addAll(OTHER_TYPES);
        expectedLookups.addAll(XMLNS_TYPE);
        expectedLookups.addAll(DATA_TYPES);
        expectedLookups.addAll(REFERENCE_TYPES);
        expectedLookups.add("public");
        expectedLookups.add("import");
        expectedLookups.add("const");
        expectedLookups.add("service");
        expectedLookups.add("function");
        expectedLookups.add("connector");
        expectedLookups.add("struct");
        expectedLookups.add("annotation");
        expectedLookups.add("enum");
        expectedLookups.add("test");
        expectedLookups.add("transformer");
        myFixture.addFileToProject("test/file.bal", "string s = \"\";");
        doTest("import test; \n<caret> \nfunction A(){}", expectedLookups.toArray(new String[expectedLookups.size()]));
    }

    public void testImportAfterImportPartialIdentifier() {
        doTest("import test; \ni<caret> \nfunction A(){}", "public", "annotation", "function", "import", "service",
                "int", "string");
    }
}
