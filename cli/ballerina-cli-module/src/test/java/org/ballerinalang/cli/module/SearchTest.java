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

package org.ballerinalang.cli.module;

import io.ballerina.runtime.JSONParser;
import io.ballerina.runtime.api.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.values.ArrayValue;
import io.ballerina.runtime.values.MapValue;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.ballerinalang.cli.module.Search.printModules;

/**
 * Unit tests for Search class.
 *
 * @since 1.2.0
 */
public class SearchTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private static final PrintStream originalOut = System.out;
    private static final PrintStream errStream = System.err;

    @DataProvider(name = "searchResultsWithTerminalWidth")
    public Object[][] provideSearchResultsWithTerminalWidth()
            throws IOException {
        Path searchResultsFilePath = Paths
                .get("src", "test", "resources", "test-resources", "search", "search-results.json");
        MapValue arr = (MapValue) JSONParser
                .parse(new String(Files.readAllBytes(searchResultsFilePath), StandardCharsets.UTF_8));
        BArray modules = arr.getArrayValue(StringUtils.fromString("modules"));
        return new Object[][] {
                { modules, "100", "search-output-100.txt" },
                { modules, "150", "search-output-150.txt" },
                { modules, "200", "search-output-200.txt" },
                { modules, "250", "search-output-250.txt" }
        };
    }

    @BeforeMethod
    public void setupStream() {
        System.setOut(new PrintStream(outContent));
    }

    @Test(description = "Test printing search results table for different terminal widths",
            dataProvider = "searchResultsWithTerminalWidth")
    public void testPrintModules(ArrayValue modules, String terminalWidth, String outputFile) throws IOException {
        printModules(modules, terminalWidth);
        Path outputFilePath = Paths.get("src", "test", "resources", "test-resources", "search", outputFile);
        String expectedOutput = new String(Files.readAllBytes(outputFilePath), StandardCharsets.UTF_8);

        if (!(outContent.toString().contains(expectedOutput))) {
            errStream.println("expected output not contains in output stream");
            errStream.println("expected output:\n" + expectedOutput);
            errStream.println("print stream:" + outContent.toString());
            Assert.fail();
        }
    }

    @AfterMethod
    public void closeStream() throws IOException {
        System.setOut(originalOut);
        outContent.close();
    }
}
