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

import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.psi.PsiFile;
import org.ballerinalang.BallerinaCodeInsightFixtureTestCase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class BallerinaCompletionTestBase extends BallerinaCodeInsightFixtureTestCase {

    static final List<String> FILE_LEVEL_KEYWORDS = Arrays.asList("public", "package", "import", "const",
            "service", "function", "connector", "struct", "annotation", "enum", "transformer");
    static final List<String> DATA_TYPES = Arrays.asList("boolean", "int", "float", "string", "blob");
    static final List<String> REFERENCE_TYPES = Arrays.asList("message", "map", "xml", "json", "datatable");
    static final List<String> XMLNS_TYPE = Collections.singletonList("xmlns");
    static final List<String> OTHER_TYPES = Arrays.asList("any", "type", "var");
    static final List<String> COMMON_KEYWORDS = Arrays.asList("if", "else", "fork", "join", "timeout", "worker",
            "transaction", "failed", "abort", "try", "catch", "finally", "while", "next", "break", "throw",
            "foreach", "in", "lock");
    static final List<String> VALUE_KEYWORDS = Arrays.asList("true", "false", "null");
    static final List<String> FUNCTION_LEVEL_KEYWORDS = Collections.singletonList("return");

    void doTestFile(String... expectedLookups) {
        String testName = getTestName(false);
        myFixture.configureByFile(testName + ".bal");
        myFixture.complete(CompletionType.BASIC, 1);
        List<String> lookupElementStrings = myFixture.getLookupElementStrings();
        assertNotNull(lookupElementStrings);
        assertSameElements(lookupElementStrings, expectedLookups);
    }

    void doTest(String fileContent, String... expectedLookups) {
        if (fileContent != null) {
            myFixture.configureByText("test.bal", fileContent);
        }
        myFixture.complete(CompletionType.BASIC, 1);
        List<String> lookupElementStrings = myFixture.getLookupElementStrings();
        assertNotNull(lookupElementStrings);
        assertSameElements(lookupElementStrings, expectedLookups);
    }

    void doCheckResult(@NotNull String relativePath, @NotNull String before, String after,
                       @Nullable Character c, String... expectedLookups) {
        PsiFile testFile = myFixture.addFileToProject(relativePath, before);
        myFixture.configureFromExistingVirtualFile(testFile.getVirtualFile());
        myFixture.complete(CompletionType.BASIC, 0);
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
