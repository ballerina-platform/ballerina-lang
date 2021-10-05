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

import static org.ballerinalang.test.TestRunnerUtils.ABS_LINE_NUM;
import static org.ballerinalang.test.TestRunnerUtils.ACTUAL_LINE_NUM;
import static org.ballerinalang.test.TestRunnerUtils.ACTUAL_VALUE;
import static org.ballerinalang.test.TestRunnerUtils.EXPECTED_LINE_NUM;
import static org.ballerinalang.test.TestRunnerUtils.EXPECTED_VALUE;
import static org.ballerinalang.test.TestRunnerUtils.FILENAME;
import static org.ballerinalang.test.TestRunnerUtils.KIND;

/**
 * Report generator for generate reports relate spec conformance tests.
 *
 * @since 2.0.0
 */
public class ReportGenerator {

    private static final Path BALLERINA_LANG_DIR = Paths.get("").toAbsolutePath().getParent().getParent();
    private static final Path TEST_DIR = BALLERINA_LANG_DIR.resolve("tests")
                                                           .resolve("ballerina-spec-conformance-tests");
    public static final Path REPORT_DIR = TEST_DIR.resolve("build").resolve("reports");
    private static final String HTML_EXTENSION = ".html";
    public static final String ERROR_KIND_TESTS_REPORT = "error_kind_tests_report_template.html";
    public static final String SKIPPED_TESTS_REPORT = "skipped_tests_report_template.html";
    public static final String FAILED_TESTS_REPORT = "failed_tests_report_template.html";
    private static final String START_TABLE_ROW  = "<tr class=\"active-row\">";
    private static final String END_TABLE_ROW  = "</tr>";
    private List<Map<String, String>> detailsOfFailedTests;
    private List<Map<String, String>> detailsOfSkippedTests;
    private Map<String, List<Map<String, String>>> detailsOfErrorKindTests;

    public ReportGenerator() {
        this.detailsOfFailedTests = new ArrayList<>();
        this.detailsOfSkippedTests = new ArrayList<>();
        this.detailsOfErrorKindTests = new LinkedHashMap<>();
    }

    public void addDetailsOfSkippedTests(Map<String, String> detailsOfTests) {
        detailsOfSkippedTests.add(detailsOfTests);
    }

    public void addDetailsOfFailedTests(Map<String, String> detailsOfTests) {
        detailsOfFailedTests.add(detailsOfTests);
    }

    public void addDetailsOfErrorKindTests(Map<String, String> detailsOfTest) {
        String fileName = detailsOfTest.get(FILENAME);
        if (detailsOfErrorKindTests.containsKey(fileName)) {
            detailsOfErrorKindTests.get(fileName).add(detailsOfTest);
        } else {
            List<Map<String, String>> details = new ArrayList<>();
            details.add(detailsOfTest);
            detailsOfErrorKindTests.put(fileName, details);
        }
    }

    public void generateReport() throws IOException {
        generateFailedTestsReports();
        generateErrorVerificationTestReports();
        generateSkippedTestReports();
    }

    private void generateFailedTestsReports() throws IOException {
        if (detailsOfFailedTests.isEmpty()) {
            return;
        }
        StringBuilder detailsOfTests = new StringBuilder();
        for (Map<String, String> test : detailsOfFailedTests) {
            String diagnostics = test.get(TestRunnerUtils.FORMAT_ERRORS);
            if (diagnostics != null) {
                detailsOfTests.append(generateFailedTestsDetails(test.get(FILENAME), test.get(KIND),
                                      test.get(ABS_LINE_NUM), null, diagnostics, null));
            } else {
                detailsOfTests.append(generateFailedTestsDetails(test.get(FILENAME), test.get(KIND),
                                      test.get(ACTUAL_LINE_NUM), test.get(EXPECTED_LINE_NUM), test.get(ACTUAL_VALUE),
                                      test.get(EXPECTED_VALUE)));
            }
        }
        generateReport(ReportGenerator.FAILED_TESTS_REPORT, "failed_tests_summary", detailsOfTests);
    }

    private void generateErrorVerificationTestReports() throws IOException {
        if (detailsOfErrorKindTests.isEmpty()) {
            return;
        }
        Set<String> files = detailsOfErrorKindTests.keySet();
        for (String file : files) {
            StringBuilder detailsOfTests = new StringBuilder();
            List<Map<String, String>> detailsOfErrorKindTest = detailsOfErrorKindTests.get(file);
            for (Map<String, String> test : detailsOfErrorKindTest) {
                detailsOfTests.append(generateErrorDetails(test.get(ACTUAL_LINE_NUM), test.get(EXPECTED_LINE_NUM),
                                                           test.get(ACTUAL_VALUE), test.get(EXPECTED_VALUE)));
            }
            generateReport(ReportGenerator.ERROR_KIND_TESTS_REPORT, file.substring(0, file.indexOf(".")),
                                                                                   detailsOfTests);
        }
    }

    private void generateSkippedTestReports() throws IOException {
        if (detailsOfSkippedTests.isEmpty()) {
            return;
        }
        StringBuilder detailsOfTests = new StringBuilder();
        for (Map<String, String> test : detailsOfSkippedTests) {
            detailsOfTests.append(generateSkippedTestsDetails(test.get(FILENAME), test.get(KIND),
                                  test.get(ABS_LINE_NUM)));
        }
        generateReport(ReportGenerator.SKIPPED_TESTS_REPORT, "skipped_tests_summary", detailsOfTests);
    }

    private void generateReport(String templateFileName, String filename, StringBuilder results) throws IOException {
        String pathOfTemplate = TEST_DIR + "/src/test/resources/report/" + templateFileName;
        File file = new File(REPORT_DIR + "/" + filename + HTML_EXTENSION);

        StringBuilder content = new StringBuilder();
        File templateFile = new File(pathOfTemplate);
        BufferedReader reader = new BufferedReader(new FileReader(templateFile));
        String line = reader.readLine();
        while (line != null) {
            content.append(line).append(System.lineSeparator());
            line = reader.readLine();
        }
        String newContent = content.toString().replaceAll("<td></td>", results.toString()).replaceAll("FileName",
                                                                                                            filename);
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
