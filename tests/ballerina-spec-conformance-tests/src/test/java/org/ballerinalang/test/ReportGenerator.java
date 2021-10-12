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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.ballerinalang.test.TestRunnerUtils.ABS_LINE_NUM;
import static org.ballerinalang.test.TestRunnerUtils.ACTUAL_LINE_NUM;
import static org.ballerinalang.test.TestRunnerUtils.ACTUAL_VALUE;
import static org.ballerinalang.test.TestRunnerUtils.BUILD_DIR;
import static org.ballerinalang.test.TestRunnerUtils.EXPECTED_LINE_NUM;
import static org.ballerinalang.test.TestRunnerUtils.EXPECTED_VALUE;
import static org.ballerinalang.test.TestRunnerUtils.FILENAME;
import static org.ballerinalang.test.TestRunnerUtils.KIND;
import static org.ballerinalang.test.TestRunnerUtils.TEST_DIR;

/**
 * Report generator for spec conformance tests.
 *
 * @since 2.0.0
 */
public class ReportGenerator {

    public static final Path REPORT_DIR = BUILD_DIR.resolve("reports");
    private static final String HTML_EXTENSION = ".html";
    public static final String ERROR_KIND_TESTS_REPORT = "error_kind_tests_report_template.html";
    public static final String SKIPPED_TESTS_REPORT = "skipped_tests_report_template.html";
    public static final String FAILED_TESTS_REPORT = "failed_tests_report_template.html";
    private static final String START_TABLE_ROW = "<tr class=\"active-row\">";
    private static final String END_TABLE_ROW  = "</tr>";
    private static List<Map<String, String>> detailsOfFailedTests = new ArrayList<>();
    private static List<Map<String, String>> detailsOfSkippedTests = new ArrayList<>();
    private static Map<String, List<Map<String, String>>> detailsOfErrorKindTests = new LinkedHashMap<>();

    public static void addDetailsOfSkippedTests(Map<String, String> detailsOfTests) {
        detailsOfSkippedTests.add(detailsOfTests);
    }

    public static void addDetailsOfFailedTests(Map<String, String> detailsOfTests) {
        detailsOfFailedTests.add(detailsOfTests);
    }

    public static void addDetailsOfErrorKindTests(Map<String, String> detailsOfTest) {
        String fileName = detailsOfTest.get(FILENAME);
        if (detailsOfErrorKindTests.containsKey(fileName)) {
            List<Map<String, String>> details = detailsOfErrorKindTests.get(fileName);
            details.add(detailsOfTest);
            return;
        }
        List<Map<String, String>> details = new ArrayList<>() {{ add(detailsOfTest); }};
        detailsOfErrorKindTests.put(fileName, details);
    }

    private static String reportTemplate(String templateFileName) throws IOException {
        String pathOfTemplate = TEST_DIR + "/src/test/resources/report/" + templateFileName;
        StringBuilder content = new StringBuilder();
        File templateFile = new File(pathOfTemplate);
        BufferedReader reader = new BufferedReader(new FileReader(templateFile));
        String line = reader.readLine();
        while (line != null) {
            content.append(line).append(System.lineSeparator());
            line = reader.readLine();
        }
        reader.close();
        return content.toString();
    }

    public static void generateReport() throws IOException {
        generateFailedTestsReports(reportTemplate(FAILED_TESTS_REPORT));
        generateErrorVerificationTestReports(reportTemplate(ERROR_KIND_TESTS_REPORT));
        generateSkippedTestReports(reportTemplate(SKIPPED_TESTS_REPORT));
    }

    private static void generateFailedTestsReports(String template) throws IOException {
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
        generateReport(template, "failed_tests_summary", detailsOfTests);
    }

    private static void generateErrorVerificationTestReports(String template) throws IOException {
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
            generateReport(template, file.substring(0, file.indexOf(".")),
                                                                                   detailsOfTests);
        }
    }

    private static void generateSkippedTestReports(String template) throws IOException {
        if (detailsOfSkippedTests.isEmpty()) {
            return;
        }
        StringBuilder detailsOfTests = new StringBuilder();
        for (Map<String, String> test : detailsOfSkippedTests) {
            detailsOfTests.append(generateSkippedTestsDetails(test.get(FILENAME), test.get(KIND),
                                  test.get(ABS_LINE_NUM)));
        }
        generateReport(template, "skipped_tests_summary", detailsOfTests);
    }

    private static void generateReport(String template, String filename, StringBuilder results)
                                       throws IOException {
        File file = new File(REPORT_DIR + "/" + filename + HTML_EXTENSION);
        String newContent = template.replaceAll("<td></td>", results.toString()).replaceAll("FileName", filename);
        FileWriter tempFileWriter = new FileWriter(file);
        tempFileWriter.write(newContent);
        tempFileWriter.close();
    }

    private static String generateFailedTestsDetails(String fileName, String testKind, String actualLineNum,
                                                     String expectedLineNum, String actualOutput,
                                                     String expectedOutput) {
        String tableRow = START_TABLE_ROW;
        tableRow  = tableRow + String.format("<td>%s</td>", fileName);
        tableRow  = tableRow + String.format("<td>%s</td>", testKind);
        tableRow  = tableRow + String.format("<td>%s</td>", expectedLineNum);
        tableRow  = tableRow + String.format("<td>%s</td>", actualLineNum);
        tableRow  = tableRow + String.format("<td>%s</td>", expectedOutput);
        tableRow  = tableRow + String.format("<td>%s</td>", actualOutput);
        tableRow = tableRow + END_TABLE_ROW;

        return tableRow;
    }

    private static String generateErrorDetails(String actualLineNum, String expectedLineNum, String actualErrorMsg,
                                               String errorDesc) {
        String tableRow = START_TABLE_ROW;
        tableRow  = tableRow + String.format("<td>%s</td>", expectedLineNum);
        tableRow  = tableRow + String.format("<td>%s</td>", actualLineNum);
        tableRow  = tableRow + String.format("<td>%s</td>", actualErrorMsg);
        tableRow  = tableRow + String.format("<td>%s</td>", errorDesc);
        tableRow = tableRow + END_TABLE_ROW;

        return tableRow;
    }

    private static String generateSkippedTestsDetails(String fileName, String testKind, String lineNum) {
        String tableRow = START_TABLE_ROW;
        tableRow  = tableRow + String.format("<td>%s</td>", fileName);
        tableRow  = tableRow + String.format("<td>%s</td>", testKind);
        tableRow  = tableRow + String.format("<td>%s</td>", lineNum);
        tableRow = tableRow + END_TABLE_ROW;

        return tableRow;
    }
}
