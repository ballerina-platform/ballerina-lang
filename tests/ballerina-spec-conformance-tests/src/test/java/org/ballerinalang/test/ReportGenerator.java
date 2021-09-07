/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Report generator for generate reports relate spec conformance tests.
 *
 * @since 2.0.0
 */
public class ReportGenerator {

    private static final Path ballerinaLangDir = Paths.get("").toAbsolutePath().getParent().getParent();
    private static final Path TEST_DIR = ballerinaLangDir.resolve("tests").resolve("ballerina-spec-conformance-tests");
    public static final Path REPORT_DIR = TEST_DIR.resolve("build").resolve("reports");
    private static final String HTML_EXTENSION = ".html";
    public static final String ERROR_REPORT = "error_report_template.html";
    public static final String TESTS_REPORT = "tests_report_template.html";
    private static final String START_TABLE_ROW  = "<tr class=\"active-row\">";
    private static final String END_TABLE_ROW  = "</tr>";


    static void generateReport(String pathOfTemplateFile, String filename, String results) throws IOException {
        String pathOfTemplate = TEST_DIR.toString() + "/src/test/resources/report/" + pathOfTemplateFile;
        File file = new File(REPORT_DIR.toString() + "/" + filename + HTML_EXTENSION);

        String oldContent = "";
        File fileToBeModified = new File(pathOfTemplate);
        BufferedReader reader = new BufferedReader(new FileReader(fileToBeModified));
        String line = reader.readLine();

        while (line != null) {
            oldContent = oldContent + line + System.lineSeparator();
            line = reader.readLine();
        }
        String newContent = oldContent.replaceAll("<td></td>", results)
                                      .replace("numOfTests", String.valueOf(TestRunnerUtils.totalTest))
                                      .replace("numFailedTests", String.valueOf(TestRunnerUtils.totalTest -
                                                                                      TestRunnerUtils.totalTestPassed))
                                      .replace("numOfSuccess", String.valueOf(TestRunnerUtils.totalTestPassed));
        FileWriter tempFileWriter = new FileWriter(file);
        tempFileWriter.write(newContent);
        reader.close();
        tempFileWriter.close();
    }

    static String generateErrorDetails(String fileName, String testKind, String lineNo, String description,
                                       String errorMsg, String result) {
        String tableRow = START_TABLE_ROW;
        tableRow  = tableRow + String.format("<td>%s</td>", fileName);
        tableRow  = tableRow + String.format("<td>%s</td>", testKind);
        tableRow  = tableRow + String.format("<td>%s</td>", lineNo);
        tableRow  = tableRow + String.format("<td>%s</td>", description);
        tableRow  = tableRow + String.format("<td>%s</td>", errorMsg);
        tableRow  = tableRow + String.format("<td>%s</td>", result);
        tableRow = tableRow + END_TABLE_ROW;

        return tableRow;
    }

    static String generateTestDetails(String fileName, String testKind, String lineNo, String result) {
        String tableRow = START_TABLE_ROW;
        tableRow  = tableRow + String.format("<td>%s</td>", fileName);
        tableRow  = tableRow + String.format("<td>%s</td>", testKind);
        tableRow  = tableRow + String.format("<td>%s</td>", lineNo);
        tableRow  = tableRow + String.format("<td>%s</td>", result);
        tableRow = tableRow + END_TABLE_ROW;

        return tableRow;
    }
}
