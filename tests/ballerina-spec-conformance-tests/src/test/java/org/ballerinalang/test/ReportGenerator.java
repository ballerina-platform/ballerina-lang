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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    public static final String ERROR_KIND_TESTS_REPORT = "error_kind_tests_report_template.html";
    public static final String SKIPPED_TESTS_REPORT = "skipped_tests_report_template.html";
    public static final String FAILED_TESTS_REPORT = "failed_tests_report_template.html";
    private static final String START_TABLE_ROW  = "<tr class=\"active-row\">";
    private static final String END_TABLE_ROW  = "</tr>";
    private List<List<String>> detailsOfFailedTests;
    private List<List<String>> detailsOfSkippedTests;
    private Map<String, List<List<String>>> detailsOfErrorKindTests;

    public ReportGenerator() {
        this.detailsOfFailedTests = new ArrayList<>();
        this.detailsOfSkippedTests = new ArrayList<>();
        this.detailsOfErrorKindTests = new LinkedHashMap<>();
    }

    public void addDetailsOfSkippedTests(List<String> detailsOfTests) {
        detailsOfSkippedTests.add(detailsOfTests);
    }

    public void addDetailsOfFailedTests(List<String> detailsOfTests) {
        detailsOfFailedTests.add(detailsOfTests);
    }

    public void addDetailsOfErrorKindTests(List<String> detailsOfTests) {
        String fileName = detailsOfTests.get(0);
        if (detailsOfErrorKindTests.containsKey(fileName)) {
            detailsOfErrorKindTests.get(fileName).add(detailsOfTests);
        } else {
            List<List<String>> details = new ArrayList<>();
            details.add(detailsOfTests);
            detailsOfErrorKindTests.put(fileName, details);
        }
    }

    public void generateReport() throws IOException {
        generateFailedTestsReports();
        generateErrorKindTestReports();
        generateSkippedTestReports();
    }

    private void generateFailedTestsReports() throws IOException {
        if (detailsOfFailedTests.isEmpty()) {
            return;
        }
        String detailsOfTests = "";
        for (List<String> test : detailsOfFailedTests) {
            String diagnostics = test.get(3);
            if (diagnostics != null) {
                detailsOfTests = detailsOfTests + generateFailedTestsDetails(test.get(0), test.get(1), test.get(2),
                        null, diagnostics, null);
            } else {
                detailsOfTests = detailsOfTests + generateFailedTestsDetails(test.get(0), test.get(1), test.get(6),
                        test.get(7), test.get(4), test.get(5));
            }
        }
        generateReport(ReportGenerator.FAILED_TESTS_REPORT, "failed_tests_summary", detailsOfTests);
    }

    private void generateErrorKindTestReports() throws IOException {
        if (detailsOfErrorKindTests.isEmpty()) {
            return;
        }
        Set<String> files = detailsOfErrorKindTests.keySet();
        for (String file : files) {
            String detailsOfTests = "";
            List<List<String>> detailsOfErrorKindTest = detailsOfErrorKindTests.get(file);
            for (List<String> test : detailsOfErrorKindTest) {
                detailsOfTests = detailsOfTests + generateErrorDetails(test.get(6), test.get(7), test.get(4),
                                                                     test.get(5));
            }
            generateReport(ReportGenerator.ERROR_KIND_TESTS_REPORT, file.substring(0, file.indexOf(".")),
                                                                                   detailsOfTests);
        }
    }

    private void generateSkippedTestReports() throws IOException {
        if (detailsOfSkippedTests.isEmpty()) {
            return;
        }
        String detailsOfTests = "";
        for (List<String> test : detailsOfSkippedTests) {
            detailsOfTests = detailsOfTests + generateSkippedTestsDetails(test.get(0), test.get(1), test.get(2));
        }
        generateReport(ReportGenerator.SKIPPED_TESTS_REPORT, "skipped_tests_summary", detailsOfTests);
    }

    private void generateReport(String pathOfTemplateFile, String filename, String results) throws IOException {
        String pathOfTemplate = TEST_DIR + "/src/test/resources/report/" + pathOfTemplateFile;
        File file = new File(REPORT_DIR + "/" + filename + HTML_EXTENSION);

        String content = "";
        File templateFile = new File(pathOfTemplate);
        BufferedReader reader = new BufferedReader(new FileReader(templateFile));
        String line = reader.readLine();
        while (line != null) {
            content = content + line + System.lineSeparator();
            line = reader.readLine();
        }
        String newContent = content.replaceAll("<td></td>", results).replaceAll("FileName", filename);
        FileWriter tempFileWriter = new FileWriter(file);
        tempFileWriter.write(newContent);
        reader.close();
        tempFileWriter.close();
    }

    private String generateFailedTestsDetails(String fileName, String testKind, String actualLineNo,
                                              String expectLineNo, String actualOutput, String expectOutput) {
        String tableRow = START_TABLE_ROW;
        tableRow  = tableRow + String.format("<td>%s</td>", fileName);
        tableRow  = tableRow + String.format("<td>%s</td>", testKind);
        tableRow  = tableRow + String.format("<td>%s</td>", actualLineNo);
        tableRow  = tableRow + String.format("<td>%s</td>", expectLineNo);
        tableRow  = tableRow + String.format("<td>%s</td>", actualOutput);
        tableRow  = tableRow + String.format("<td>%s</td>", expectOutput);
        tableRow = tableRow + END_TABLE_ROW;

        return tableRow;
    }

    static String generateErrorDetails(String actualLineNo, String expectLineNo, String actualErrorMsg,
                                       String errorDesc) {
        String tableRow = START_TABLE_ROW;
        tableRow  = tableRow + String.format("<td>%s</td>", actualLineNo);
        tableRow  = tableRow + String.format("<td>%s</td>", expectLineNo);
        tableRow  = tableRow + String.format("<td>%s</td>", actualErrorMsg);
        tableRow  = tableRow + String.format("<td>%s</td>", errorDesc);
        tableRow = tableRow + END_TABLE_ROW;

        return tableRow;
    }

    static String generateSkippedTestsDetails(String fileName, String testKind, String lineNo) {
        String tableRow = START_TABLE_ROW;
        tableRow  = tableRow + String.format("<td>%s</td>", fileName);
        tableRow  = tableRow + String.format("<td>%s</td>", testKind);
        tableRow  = tableRow + String.format("<td>%s</td>", lineNo);
        tableRow = tableRow + END_TABLE_ROW;

        return tableRow;
    }
}
