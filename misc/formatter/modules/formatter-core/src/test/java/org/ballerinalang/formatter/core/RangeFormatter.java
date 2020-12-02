/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.formatter.core;

import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * The abstract class that is extended by all range formatting test classes.
 *
 * @since 1.2.10
 */
public abstract class RangeFormatter {
    private final Path resourceDirectory = Paths.get("src").resolve("test").resolve("resources").toAbsolutePath();
    private Path buildDirectory = Paths.get("build").toAbsolutePath().normalize();
    private static final String ASSERT_DIR = "assert";
    private static final String SOURCE_DIR = "source";

    @Test(dataProvider = "test-file-provider")
    public void test(String source, int[][] positions)
            throws IOException, FormatterException {
        Path assertFilePath = Paths.get(resourceDirectory.toString(), this.getTestResourceDir(), ASSERT_DIR, source);
        Path sourceFilePath = Paths.get(resourceDirectory.toString(), this.getTestResourceDir(), SOURCE_DIR, source);

        String content = getSourceText(sourceFilePath);
        TextDocument textDocument = TextDocuments.from(content);
        SyntaxTree syntaxTree = SyntaxTree.from(textDocument, sourceFilePath.toString());
        for (int[] position : positions) {
            LinePosition startPos = LinePosition.from(position[0], position[1]);
            LinePosition endPos = LinePosition.from(position[2], position[3]);
            LineRange lineRange = LineRange.from(null, startPos, endPos);
            syntaxTree = Formatter.format(syntaxTree, lineRange);
        }
        Assert.assertEquals(syntaxTree.toSourceCode(), getSourceText(assertFilePath));
    }

    /**
     * Test the formatting functionality for parser test cases.
     *
     * @param sourcePath Source path of the parser test
     */
    public void testParserResources(String sourcePath) throws IOException, FormatterException {
        Path filePath = Paths.get(sourcePath);
        String content = getSourceText(filePath);
        TextDocument textDocument = TextDocuments.from(content);
        SyntaxTree syntaxTree = SyntaxTree.from(textDocument);
        if (!syntaxTree.hasDiagnostics()) {
            SyntaxTree newSyntaxTree = Formatter.format(syntaxTree);
            Assert.assertEquals(newSyntaxTree.toSourceCode(), getSourceText(filePath));
        }
    }

    /**
     * Defines the data provider object for test execution.
     *
     * @return Data provider for tests
     */
    @DataProvider(name = "test-file-provider")
    public abstract Object[][] dataProvider();

    /**
     * List of file names configured to be skipped during the test execution.
     *
     * @return Skipped test file list
     */
    public List<String> skipList() {
        return new ArrayList<>();
    }

    /**
     * Specify the file names to be tested during the test execution.
     *
     * @return Test scenarios for execution
     */
    public Object[][] testSubset() {
        return new Object[0][];
    }

    /**
     * Returns the directory path inside resources which holds the test files.
     *
     * @return Directory path of test files
     */
    public abstract String getTestResourceDir();

    protected Object[][] getConfigsList() {
        if (this.testSubset().length != 0) {
            return this.testSubset();
        }
        List<String> skippedTests = this.skipList();
        try {
            return Files.walk(this.resourceDirectory.resolve(this.getTestResourceDir()).resolve(ASSERT_DIR))
                    .filter(path -> {
                        File file = path.toFile();
                        return file.isFile() && file.getName().endsWith(".bal")
                                && !skippedTests.contains(file.getName());
                    })
                    .map(path -> new Object[]{path.toFile().getName(), this.getTestResourceDir()})
                    .toArray(size -> new Object[size][2]);
        } catch (IOException e) {
            return new Object[0][];
        }
    }

    private String getSourceText(Path sourceFilePath) throws IOException {
        return Files.readString(resourceDirectory.resolve(sourceFilePath));
    }
}
