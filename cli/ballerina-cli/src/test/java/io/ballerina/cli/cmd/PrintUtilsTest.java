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
package io.ballerina.cli.cmd;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import io.ballerina.cli.utils.PrintUtils;
import org.ballerinalang.central.client.model.Package;
import org.ballerinalang.central.client.model.PackageSearchResult;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Unit tests for PrintUtils class used in Search command.
 *
 * @since 2.0.0
 */
public class PrintUtilsTest {
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private static final PrintStream originalOut = System.out;
    private static final PrintStream errStream = System.err;
    private static final Path PRINT_UTILS_OUTPUT_DIR = Paths
            .get("src", "test", "resources", "test-resources", "print-utils-output");

    @DataProvider(name = "searchResultsWithTerminalWidth")
    public Object[][] provideSearchResultsWithTerminalWidth()
            throws IOException {
        Path searchResultsFilePath = PRINT_UTILS_OUTPUT_DIR.resolve("search-results.json");
        JsonReader reader = new JsonReader(new FileReader(String.valueOf(searchResultsFilePath)));
        PackageSearchResult packageSearchResult = new Gson().fromJson(reader, PackageSearchResult.class);
        List<Package> packages = packageSearchResult.getPackages();
        return new Object[][] {
                { packages, "100", "search-output-100.txt" },
                { packages, "150", "search-output-150.txt" },
                { packages, "200", "search-output-200.txt" },
                { packages, "250", "search-output-250.txt" }
        };
    }

    @BeforeMethod
    public void setupStream() {
        System.setOut(new PrintStream(outContent));
    }

    @Test(description = "Test printing search results table for different terminal widths",
            dataProvider = "searchResultsWithTerminalWidth")
    public void testPrintModules(List<Package> packages, String terminalWidth, String outputFile) throws IOException {
        PrintUtils.printPackages(packages, terminalWidth);
        Path outputFilePath = PRINT_UTILS_OUTPUT_DIR.resolve(outputFile);
        String expectedOutput = Files.readString(outputFilePath);

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
