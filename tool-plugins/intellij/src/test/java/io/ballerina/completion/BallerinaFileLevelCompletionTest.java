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

/**
 * File level code completion tests.
 */
public class BallerinaFileLevelCompletionTest extends BallerinaCompletionTestBase {

    /**
     * Test file level lookups.
     */

    public void testEmptyFileImportKeyword() {
        doTestContains("i<caret>", "import");
    }

    public void testEmptyFileWithSpaceBeforeCaretImportKeyword() {
        doTestContains("\ni<caret>", "import");
    }

    public void testEmptyFileWithSpaceAfterCaretImportKeyword() {
        doTestContains("i<caret>\n", "import");
    }

    public void testEmptyFileWithSpacesImportKeyword() {
        doTestContains("\ni<caret>\n", "import");
    }

    public void testImportBeforeImport() {
        doTestContains("<caret>\nimport test; \nfunction A(){}", "import");
    }

    public void testImportBeforeImportPartialIdentifier() {
        doTestContains("i<caret>\nimport test; \nfunction A(){}", "public", "annotation", "import", "int", "string");
    }

    public void testImportAfterImport() {
        myFixture.addFileToProject("test/file.bal", "string s = \"\";");
        doTestContains("import test; \n<caret> \nfunction A(){}", "import");
    }

    public void testImportAfterImportPartialIdentifier() {
        doTestContains("import test; \ni<caret> \nfunction A(){}", "public", "annotation", "import", "int", "string");
    }

}
