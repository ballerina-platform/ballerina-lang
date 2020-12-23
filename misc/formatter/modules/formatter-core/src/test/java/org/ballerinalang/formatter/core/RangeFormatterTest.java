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
    public void test(Path sourceFilePath, Path assertFilePath, ArrayList<LineRange> lineRanges) throws IOException,
            FormatterException {
        String content = getFileContent(sourceFilePath);
        TextDocument textDocument = TextDocuments.from(content);
        SyntaxTree syntaxTree = SyntaxTree.from(textDocument);
        for (LineRange lineRange : lineRanges) {
            syntaxTree = Formatter.format(syntaxTree, lineRange);
        }
        Assert.assertEquals(syntaxTree.toSourceCode(), getFileContent(assertFilePath));
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
            String testResourceDir = getTestResourceDir();
            File jsonConfigFile = Paths.get(resourceDirectory.toString(), getTestResourceDir(),
                    getConfigJsonFileName()).toFile();
            JsonArray jsonArray = gson.fromJson(new FileReader(jsonConfigFile), JsonArray.class);
            int jsonArraySize = jsonArray.size();
            Object[][] objects = new Object[jsonArraySize][];
            for (int i = 0; i < jsonArray.size(); i++) {
                JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
                String fileName = jsonObject.get("filename").getAsString();
                if (skippedTests.contains(fileName)) {
                    continue;
                }
                Path assertFilePath = Paths.get(resourceDirectory.toString(), testResourceDir, ASSERT_DIR, fileName);
                Path sourceFilePath = Paths.get(resourceDirectory.toString(), testResourceDir, SOURCE_DIR, fileName);
                ArrayList<LineRange> lineRanges = new ArrayList<>();
                for (JsonElement positionElement : jsonObject.getAsJsonArray("positions")) {
                    JsonObject position = positionElement.getAsJsonObject();
                    JsonObject start = position.getAsJsonObject().get("startPos").getAsJsonObject();
                    LinePosition startPos = LinePosition.from(start.get("lineNo").getAsInt(),
                            start.get("colNo").getAsInt());
                    JsonObject end = position.getAsJsonObject().get("endPos").getAsJsonObject();
                    LinePosition endPos = LinePosition.from(end.get("lineNo").getAsInt(), end.get("colNo").getAsInt());
                    LineRange lineRange = LineRange.from(sourceFilePath.toString(), startPos, endPos);
                    lineRanges.add(lineRange);
                }
                objects[i] = new Object[] {sourceFilePath, assertFilePath, lineRanges};
            }
            return objects;
        } catch (FileNotFoundException e) {
            return new Object[0][];
        }
    }

    private String getFileContent(Path sourceFilePath) throws IOException {
        return Files.readString(resourceDirectory.resolve(sourceFilePath));
    }
}
