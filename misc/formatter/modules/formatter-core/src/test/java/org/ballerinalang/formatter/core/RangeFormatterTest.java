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

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * The abstract class that is extended by all range formatting test classes.
 *
 * @since 2.0.0
 */
public abstract class RangeFormatterTest {
    private final Path resourceDirectory = Paths.get("src").resolve("test").resolve("resources").toAbsolutePath();
    private static final String ASSERT_DIR = "assert";
    private static final String SOURCE_DIR = "source";

    private static final Gson gson = new Gson();

    @Test(dataProvider = "test-file-provider")
    public void test(String source, JsonArray positions)
            throws IOException, FormatterException {
        Path assertFilePath = Paths.get(resourceDirectory.toString(), this.getTestResourceDir(), ASSERT_DIR, source);
        Path sourceFilePath = Paths.get(resourceDirectory.toString(), this.getTestResourceDir(), SOURCE_DIR, source);
        String content = getSourceText(sourceFilePath);
        TextDocument textDocument = TextDocuments.from(content);
        SyntaxTree syntaxTree = SyntaxTree.from(textDocument, sourceFilePath.toString());
        for (JsonElement position : positions) {
            JsonObject start = position.getAsJsonObject().get("startPos").getAsJsonObject();
            LinePosition startPos = LinePosition.from(start.get("lineNo").getAsInt(), start.get("colNo").getAsInt());
            JsonObject end = position.getAsJsonObject().get("endPos").getAsJsonObject();
            LinePosition endPos = LinePosition.from(end.get("lineNo").getAsInt(), end.get("colNo").getAsInt());
            LineRange lineRange = LineRange.from(sourceFilePath.toString(), startPos, endPos);
            syntaxTree = Formatter.format(syntaxTree, lineRange);
        }
        Assert.assertEquals(syntaxTree.toSourceCode(), getSourceText(assertFilePath));
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

    public abstract String getConfigJsonFileName();

    protected Object[][] getConfigsList() {
        if (this.testSubset().length != 0) {
            return this.testSubset();
        }
        List<String> skippedTests = this.skipList();
        try {
            File jsonConfigFile = Paths.get(resourceDirectory.toString(), this.getTestResourceDir(),
                    this.getConfigJsonFileName()).toFile();
            JsonObject jsonObject = gson.fromJson(new FileReader(jsonConfigFile), JsonObject.class);
            String[] fileNames = jsonObject.keySet().toArray(new String[0]);
            int fileNameCount = fileNames.length;
            Object[][] objects = new Object[fileNameCount][];
            for (int i = 0; i < fileNameCount; i++) {
                String fileName = fileNames[i];
                if (!skippedTests.contains(fileName)) {
                    objects[i] = new Object[] {fileName, jsonObject.getAsJsonArray(fileName)};
                }
            }
            return objects;
        } catch (FileNotFoundException e) {
            return new Object[0][];
        }
    }

    private String getSourceText(Path sourceFilePath) throws IOException {
        return Files.readString(resourceDirectory.resolve(sourceFilePath));
    }
}
