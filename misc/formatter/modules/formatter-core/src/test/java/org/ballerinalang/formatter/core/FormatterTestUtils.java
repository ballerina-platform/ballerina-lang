/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.formatter.core;

import io.ballerinalang.compiler.syntax.tree.SyntaxTree;
import io.ballerinalang.compiler.text.TextDocument;
import io.ballerinalang.compiler.text.TextDocuments;
import org.testng.Assert;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Util class used by the formatting test classes.
 *
 * @since 2.0.0
 */
public abstract class FormatterTestUtils {

    private static final Path RESOURCE_DIRECTORY = Paths.get("src/test/resources/");

    /**
     * Test formatting a valid source.
     *
     * @param sourceFilePath Path to the ballerina file
     * @param assertFilePath File to assert the resulting tree after formatting
     */
    public static void test(Path sourceFilePath, Path assertFilePath) {

        String content = getSourceText(sourceFilePath);
        TextDocument textDocument = TextDocuments.from(content);
        SyntaxTree syntaxTree = SyntaxTree.from(textDocument);
        SyntaxTree newSyntaxTree = Formatter.format(syntaxTree, null);
        Assert.assertEquals(getSourceText(assertFilePath), newSyntaxTree.toSourceCode());
    }

    private static String getSourceText(Path sourceFilePath) {
        try {
            return new String(Files.readAllBytes(RESOURCE_DIRECTORY.resolve(sourceFilePath)), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
