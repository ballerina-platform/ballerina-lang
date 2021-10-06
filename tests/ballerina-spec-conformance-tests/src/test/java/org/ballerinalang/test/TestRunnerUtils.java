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

import io.ballerina.tools.diagnostics.Diagnostic;
import org.ballerinalang.core.util.exceptions.BLangRuntimeException;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.SkipException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Test util class for all spec conformance test cases.
 *
 * @since 2.0.0
 */
public class TestRunnerUtils {

    private static final String START_TEST_CASE = "Test-Case";
    private static final String DESCRIPTION = "Description";
    private static final String FAIL_ISSUE = "Fail-Issue";
    private static final String LABELS = "Labels";
    public static final String OUTPUT = "output";
    public static final String PANIC = "panic";
    public static final String ERROR = "error";
    public static final String PARSER_ERROR = "parser-error";
    public static final String OTHER = "other";
    private static final String EMPTY_STRING = "";
    private static final String CARRIAGE_RETURN_CHAR = "\\r";
    public static final String UNDERSCORE = "_";
    private static final String NEW_LINE_CHARACTER = "\n";
    private static final String BAL_EXTENSION = ".bal";
    public static final String RESOURCE_DIR = "src/test/resources/";
    public static final String TEMP_DIR = "test-src/";
    private static final String IMPORT_BALLERINAI = "import ballerinai/io;";
    public static final String FILENAME = "filename";
    public static final String KIND = "kind";
    public static final String ABS_LINE_NUM = "abs-line-num";
    public static final String FORMAT_ERRORS = "format-errors";
    public static final String ACTUAL_VALUE = "actual-value";
    public static final String EXPECTED_VALUE = "expected-value";
    public static final String ACTUAL_LINE_NUM = "actual-line-num";
    public static final String EXPECTED_LINE_NUM = "expected-line-num";
    private static int absLineNum;
    public static String diagnostics;

    public static void readTestFile(String fileName, String path, List<Object[]> testCases, Set<String> labels)
                                                                                                    throws IOException {
        absLineNum = 1;
        String subPath = path.split("/conformance/")[1];
        subPath = subPath.substring(0, subPath.lastIndexOf("/") + 1);

        File testFile = new File(path);
        FileReader fileReader = new FileReader(testFile);
        BufferedReader buffReader = new BufferedReader(fileReader);

        String line = buffReader.readLine();
        while (line != null) {
            line = readTestFile(subPath, line, fileName, buffReader, testCases, labels);
        }
        buffReader.close();
    }

    private static String readTestFile(String path, String line, String fileName, BufferedReader buffReader,
                                       List<Object[]>  testCases, Set<String> selectedLabels) throws IOException {
        diagnostics = null;
        String tempDir = RESOURCE_DIR + TEMP_DIR + path;
        new File(tempDir).mkdirs();
        String tempFileName = fileName.substring(0, fileName.indexOf(".")) + UNDERSCORE + absLineNum;

        Map<String, String> headersOfTestCase = readHeadersOfTest(line, buffReader);
        String kindOfTestCase = getKindOfTest(headersOfTestCase.get(START_TEST_CASE));
        boolean isSkippedTestCase = isSkippedTestCase(selectedLabels, headersOfTestCase.get(LABELS));

        Object[] testCase = new Object[9];
        testCase[0] = kindOfTestCase;
        testCase[1] = TEMP_DIR + path + tempFileName + BAL_EXTENSION;
        testCase[4] = fileName;
        testCase[5] = absLineNum;
        testCase[6] = isSkippedTestCase;
        testCase[8] = headersOfTestCase.containsKey(FAIL_ISSUE);

        return writeToBalFile(testCases, testCase, kindOfTestCase, tempDir, tempFileName, buffReader);
    }

    private static String writeToBalFile(List<Object[]>  testCases, Object[] testCase, String kindOfTestCase,
                                         String tempDir, String tempFileName, BufferedReader buffReader)
                                         throws IOException {
        List<String> outputValues = new ArrayList<>();
        List<Integer> lineNumbers = new ArrayList<>();
        File tempBalFile = new File(tempDir + tempFileName + BAL_EXTENSION);
        FileWriter tempFileWriter = new FileWriter(tempBalFile);
        if (kindOfTestCase.equals(OUTPUT)) {
            tempFileWriter.write(IMPORT_BALLERINAI + NEW_LINE_CHARACTER);
        }
        String line = buffReader.readLine();
        int relativeLineNum = 1;
        while (line != null) {
            if (line.startsWith(START_TEST_CASE)) {
                break;
            }
            line = getExpectedValues(kindOfTestCase, line, relativeLineNum, lineNumbers, outputValues);
            tempFileWriter.write(line + NEW_LINE_CHARACTER);
            line = buffReader.readLine();
            relativeLineNum = relativeLineNum + 1;
        }

        tempFileWriter.close();
        absLineNum = absLineNum + relativeLineNum;
        testCase[2] = outputValues;
        testCase[3] = lineNumbers;
        testCase[7] = diagnostics;

        testCases.add(testCase);
        return line;
    }

    private static String getExpectedValues(String kindOfTestCase, String line, int relativeLineNum,
                                            List<Integer> lineNumbers, List<String> outputValues) {
        Pattern pattern = Pattern.compile("(.*)//\\s*@\\s*(\\S+)\\s?(.*)");
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            String outputHeader = matcher.group(2).trim();
            if (!(kindOfTestCase.equals(outputHeader) ||
                    (kindOfTestCase.equals(PARSER_ERROR) && outputHeader.equals(ERROR)))) {
                reportDiagnostics("format of output is incorrect, It should be //@error, //@output and //@panic");
            }
            String output = matcher.group(3);
            line = matcher.group(1);
            lineNumbers.add(relativeLineNum);
            outputValues.add(output);
        }
        return line;
    }

    private static String getKindOfTest(String kind) {
        if (kind == null) {
            reportDiagnostics("Testcase kind is not defined");
        }
        if (kind.equals(OUTPUT) || kind.equals(ERROR) || kind.equals(PANIC) || kind.equals(PARSER_ERROR)) {
            return kind;
        }
        reportDiagnostics(String.format("Incorrect test kind, expected testcase kind to be %s, %s, %s or %s",
                                        OUTPUT, PANIC, ERROR, PARSER_ERROR));
        return OTHER;
    }

    private static Map<String, String> readHeadersOfTest(String line, BufferedReader buffReader) throws IOException {
        ArrayList<String> requiredHeaders = new ArrayList<>(Arrays.asList(START_TEST_CASE, DESCRIPTION, LABELS));
        Map<String, String> headers = new HashMap<>();
        Pattern pattern = Pattern.compile(String.format("\\s*^(%s|%s|%s|%s)\\s*:\\s*(.*)",
                                                        START_TEST_CASE, DESCRIPTION, FAIL_ISSUE, LABELS));
        String key = null;
        String value = null;
        while (line != null) {
            String header = line.strip();
            Matcher matcher = pattern.matcher(header);
            if (header.equals(EMPTY_STRING)) {
                if (key == null) {
                    line = buffReader.readLine();
                    absLineNum++;
                    continue;
                }
                requiredHeaders.remove(key);
                headers.put(key, value);
                break;
            } else if (matcher.find()) {
                if (key != null) {
                    requiredHeaders.remove(key);
                    headers.put(key, value);
                }
                key = matcher.group(1);
                value = matcher.group(2);
            } else if (key != null) {
                value = value + " " + header.trim();
            } else {
                reportDiagnostics("incorrect header format");
            }
            line = buffReader.readLine();
            absLineNum++;
        }

        if (!requiredHeaders.isEmpty()) {
            reportDiagnostics("incorrect header format");
        }
        return headers;
    }

    public static void reportDiagnostics(String msg) {
        if (diagnostics != null) {
            return;
        }
        diagnostics = msg;
    }

    public static HashMap<String, HashSet<String>> readLabels(String testDir) {
        try {
            File file = new File(testDir + "/" + RESOURCE_DIR + "labels.csv");
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            String line = br.readLine();
            String[] tempArr;
            HashMap<String, HashSet<String>> labels = new HashMap<>();
            while ((line = br.readLine()) != null) {
                tempArr = line.split(",");
                if (!labels.containsKey(tempArr[0])) {
                    labels.put(tempArr[0], new HashSet<>());
                }
                if (tempArr.length == 2) {
                    String x = tempArr[1];
                    if (!labels.containsKey(x)) {
                        labels.put(x, new HashSet<>());
                    }
                    labels.get(x).add(tempArr[0]);
                }
            }
            br.close();
            return labels;
        } catch (IOException ioe) {
            throw new AssertionError("can't resolve labels.csv file");
        }
    }

    private static boolean isSkippedTestCase(Set<String> selectedLabels, String labels) {
        if (selectedLabels.isEmpty() || labels == null) {
            return false;
        }
        for (String label : labels.split("\\s*,\\s*")) {
            if (selectedLabels.contains(label)) {
                return false;
            }
        }
        return true;
    }

    public static void validateTestFormat(String diagnostic) {
        if (diagnostic != null) {
            Assert.fail(diagnostic);
        }
    }

    public static void handleTestSkip(boolean isSkippedTestCase) {
        if (isSkippedTestCase) {
            throw new SkipException("Skip");
        }
    }

    public static void setDetailsOfTest(ITestContext context, String kind, String fileName, int absLineNum,
                                        String diagnostics) {
        context.setAttribute(KIND, kind);
        context.setAttribute(FILENAME, fileName);
        context.setAttribute(ABS_LINE_NUM, Integer.toString(absLineNum));
        context.setAttribute(FORMAT_ERRORS, diagnostics);
    }

    public static void validateTestOutput(String path, String kind, List<String> outputValues, boolean isKnownIssue,
                                          List<Integer> lineNumbers, String fileName, int absLineNum,
                                          ITestContext context) {
        try {
            if (isKnownIssue) {
                return; // todo: need to add known issues to separate report
            }
            CompileResult compileResult = BCompileUtil.compile(path);
            switch (kind) {
                case ERROR:
                case PANIC:
                case PARSER_ERROR:
                    Collection<Diagnostic> errors = compileResult.getDiagnosticResult().errors();
                    validateError(kind, errors, lineNumbers, outputValues, absLineNum, context);
                    break;
                case OUTPUT:
                    validateOutput(compileResult, outputValues, lineNumbers, absLineNum, context);
                    break;
            }
        } catch (BLangRuntimeException exception) {
            validateRuntimeErrorOrPanic(exception.getMessage(), lineNumbers, outputValues, absLineNum, context);
        }
    }

    public static void getAllLabels(HashSet<String> allLabels, HashSet<String> impliesLabelSet, HashMap<String,
                                    HashSet<String>> definedLabels,  String label) {
        allLabels.add(label);
        for (String impliesLabel : impliesLabelSet) {
            getAllLabels(allLabels, definedLabels.get(impliesLabel), definedLabels, impliesLabel);
        }
    }

    public static void setDetailsOfErrorKindTest(ITestContext context, List<Integer> lineNumbers,
                                                 List<String> outputValues, Collection<Diagnostic> diagnostics) {
        context.setAttribute("LineNumbers", lineNumbers);
        context.setAttribute("OutputValues", outputValues);
        context.setAttribute("Diagnostics", diagnostics);
    }

    public static Map<String, Object> getDetailsOfErrorKindTests(ITestContext context) {
        Map<String, Object> results = new HashMap<>();
        results.put("LineNumbers", context.getAttribute("LineNumbers"));
        results.put("OutputValues", context.getAttribute("OutputValues"));
        results.put("Diagnostics", context.getAttribute("Diagnostics"));

        return results;
    }

    private static void getDetailsOfDiagnostics(Collection<Diagnostic> diagnostics, List<String> actualLineNumbers,
                                                List<String> actualErrorMessages, int absLineNum) {
        Iterator<Diagnostic> iterator = diagnostics.iterator();
        for (int i = 0; i < diagnostics.size(); i++) {
            Diagnostic diagnostic = iterator.next();
            String actualLineNum =
                    String.valueOf(diagnostic.location().lineRange().startLine().line() + 1 + absLineNum);
            String message = diagnostic.message().replace(CARRIAGE_RETURN_CHAR, EMPTY_STRING);
            if (actualLineNumbers.contains(actualLineNum)) {
                int index = actualLineNumbers.indexOf(actualLineNum);
                actualErrorMessages.set(index, actualErrorMessages.get(index) + ", " + message);
            } else {
                actualErrorMessages.add(message);
                actualLineNumbers.add(actualLineNum);
            }
        }
    }

    public static void validateError(String kind, Collection<Diagnostic> diagnostics, List<Integer> lineNumbers,
                                     List<String> outputValues, int absLineNum, ITestContext context) {
        if (diagnostics.isEmpty()) {
            String outputVal = outputValues.get(0);
            setResultsAttributes(context, null, outputVal, null,
                    String.valueOf(lineNumbers.get(0) + absLineNum));
            Assert.assertNull(outputVal);
        }
        List<String> actualLineNumbers = new ArrayList<>();
        List<String> actualErrorMessages = new ArrayList<>();
        getDetailsOfDiagnostics(diagnostics, actualLineNumbers, actualErrorMessages, absLineNum);

        setDetailsOfErrorKindTest(context, lineNumbers, outputValues, diagnostics);
        if (kind.equals(PARSER_ERROR)) {
            return;
        }
        Iterator<Diagnostic> iterator = diagnostics.iterator();
        int noOfDiagnostics = actualErrorMessages.size();
        for (int i = 0; i < noOfDiagnostics; i++) {
            String actualLineNum = actualLineNumbers.get(i);
            String expectedLineNum;
            String expValue;
            try {
                expectedLineNum = String.valueOf(lineNumbers.get(i) + absLineNum);
                expValue = outputValues.get(i);
            } catch (IndexOutOfBoundsException e) {
                expectedLineNum = null;
                expValue = null;
            }
            setResultsAttributes(context, actualErrorMessages.get(i), expValue, actualLineNum, expectedLineNum);
            Assert.assertEquals(actualLineNum, expectedLineNum);
        }
        if (outputValues.size() > noOfDiagnostics) {
            setResultsAttributes(context, null, outputValues.get(noOfDiagnostics), null,
                                 String.valueOf(lineNumbers.get(noOfDiagnostics)));
            Assert.assertNull(iterator.next().toString());
        }
    }

    public static void validateOutput(CompileResult compileResult, List<String> outputValues, List<Integer> lineNumbers,
                                      int absLineNum, ITestContext context) {
        Collection<Diagnostic> diagnostics = compileResult.getDiagnosticResult().errors();
        if (!diagnostics.isEmpty()) {
            Diagnostic diagnostic = diagnostics.iterator().next();
            String message = diagnostic.message().replace(CARRIAGE_RETURN_CHAR, EMPTY_STRING);
            String actualLineNum =
                   String.valueOf(diagnostic.location().lineRange().startLine().line() + absLineNum);
            setResultsAttributes(context, message, null, actualLineNum,
                                 String.valueOf(lineNumbers.get(0) + absLineNum));
            Assert.assertNull(diagnostic.toString());
        }

        BRunUtil.ExitDetails exitDetails = BRunUtil.run(compileResult);
        if (!exitDetails.errorOutput.isEmpty()) {
            String actualOutput = exitDetails.errorOutput;
            String expectedOutput = outputValues.get(0);
            setResultsAttributes(context, actualOutput, expectedOutput, null,
                                 String.valueOf(lineNumbers.get(0) + absLineNum));
            Assert.assertEquals(actualOutput, expectedOutput);
        }

        String consoleOutput = exitDetails.consoleOutput;
        String[] results = consoleOutput.split("(\r\n|\r|\n)", -1);
        int size = outputValues.size();
        for (int i = 0; i < size; i++) {
            String expectedOutput = outputValues.get(i);
            String actualOutput;
            try {
                actualOutput = results[i];
            } catch (ArrayIndexOutOfBoundsException e) {
                actualOutput = null;
            }
            String lineNo = String.valueOf(lineNumbers.get(i) + absLineNum);
            setResultsAttributes(context, actualOutput, expectedOutput, lineNo, lineNo);
            Assert.assertEquals(actualOutput, expectedOutput);
        }

        if (results.length > size) {
            setResultsAttributes(context, results[size], null, null, null);
            Assert.assertNull(results[size]);
        }
    }

    public static void validateRuntimeErrorOrPanic(String errorMsg, List<Integer> lineNumbers,
                                                   List<String> outputValues, int absLineNum, ITestContext context) {
        Matcher matcherForErrorMsg = Pattern.compile("^(error)\\s*:\\s*(.*)").matcher(errorMsg);
        Matcher matcherForErrorLineNum = Pattern.compile(":(\\d+)\\)$").matcher(errorMsg);

        if (matcherForErrorMsg.find() && matcherForErrorLineNum.find()) {
            String actualLineNum = String.valueOf(Integer.parseInt(matcherForErrorLineNum.group(1)) + absLineNum);
            String expectedLineNum = String.valueOf(lineNumbers.get(0) + absLineNum);
            setResultsAttributes(context, matcherForErrorMsg.group(2), outputValues.get(0), actualLineNum,
                                 expectedLineNum);
            Assert.assertEquals(actualLineNum, expectedLineNum);
        }
    }

    public static void setResultsAttributes(ITestContext context, String actualValue, String expectedValue,
                                            String actualLineNo, String expectedLineNo) {
        context.setAttribute(ACTUAL_VALUE, actualValue);
        context.setAttribute(EXPECTED_VALUE, expectedValue);
        context.setAttribute(ACTUAL_LINE_NUM, actualLineNo);
        context.setAttribute(EXPECTED_LINE_NUM, expectedLineNo);
    }

    public static Map<String, String> getDetailsOfTest(ITestContext context, ITestResult result) {
        Map<String, String> testDetails = new HashMap<>();
        testDetails.put(FILENAME, (String) context.getAttribute(FILENAME));
        testDetails.put(KIND, (String) context.getAttribute(KIND));
        testDetails.put(ABS_LINE_NUM, (String) context.getAttribute(ABS_LINE_NUM));
        String formatErrors = (String) context.getAttribute(FORMAT_ERRORS);
        testDetails.put(FORMAT_ERRORS, formatErrors);
        if (result.getStatus() == ITestResult.SKIP || formatErrors != null) {
            return testDetails;
        }
        testDetails.put(ACTUAL_VALUE, (String) context.getAttribute(ACTUAL_VALUE));
        testDetails.put(EXPECTED_VALUE, (String) context.getAttribute(EXPECTED_VALUE));
        testDetails.put(ACTUAL_LINE_NUM, (String) context.getAttribute(ACTUAL_LINE_NUM));
        testDetails.put(EXPECTED_LINE_NUM, (String) context.getAttribute(EXPECTED_LINE_NUM));
        return testDetails;
    }

    public static void setDetailsOfErrorKindTests(ITestContext context, ReportGenerator reportGenerator,
                                                  Map<String, String> detailsOfTest) {
        Map<String, Object> detailsOfErrorKindTests = getDetailsOfErrorKindTests(context);

        boolean haveOnlyNulls = detailsOfErrorKindTests.values().stream().allMatch(Objects::isNull);;

        if (!haveOnlyNulls) {
            int absLineNo = Integer.parseInt(detailsOfTest.get(ABS_LINE_NUM));
            List<Integer> lineNumbers = (List<Integer>) detailsOfErrorKindTests.get("LineNumbers");
            List<String> outputValues = (List<String>) detailsOfErrorKindTests.get("OutputValues");
            Collection<Diagnostic> diagnostics = (Collection<Diagnostic>) detailsOfErrorKindTests.get("Diagnostics");

            Iterator<Diagnostic> iterator = diagnostics.iterator();
            for (int i = 0; i < diagnostics.size(); i++) {
                Map<String, String> results = new HashMap<>();
                results.put(FILENAME, detailsOfTest.get(FILENAME));
                results.put(KIND, detailsOfTest.get(KIND));
                results.put(ABS_LINE_NUM, detailsOfTest.get(ABS_LINE_NUM));
                results.put(FORMAT_ERRORS, detailsOfTest.get(FORMAT_ERRORS));
                Diagnostic diagnostic = iterator.next();
                int actualLineNum = diagnostic.location().lineRange().startLine().line() + 1 + absLineNo;
                String message = diagnostic.message().replace(CARRIAGE_RETURN_CHAR, EMPTY_STRING);
                String expLineNum;
                String expOutput;
                try {
                    expLineNum = String.valueOf(lineNumbers.get(i) + absLineNo);
                    expOutput = outputValues.get(i);
                } catch (IndexOutOfBoundsException e) {
                    expLineNum = null;
                    expOutput = null;
                }
                results.put(ACTUAL_VALUE, message);
                results.put(EXPECTED_VALUE, expOutput);
                results.put(ACTUAL_LINE_NUM, String.valueOf(actualLineNum));
                results.put(EXPECTED_LINE_NUM, expLineNum);
                reportGenerator.addDetailsOfErrorKindTests(results);
            }

            for (int i = diagnostics.size(); i < outputValues.size(); i++) {
                Map<String, String> results = new HashMap<>();
                results.put(FILENAME, detailsOfTest.get(FILENAME));
                results.put(KIND, detailsOfTest.get(KIND));
                results.put(ABS_LINE_NUM, detailsOfTest.get(ABS_LINE_NUM));
                results.put(FORMAT_ERRORS, detailsOfTest.get(FORMAT_ERRORS));
                results.put(ACTUAL_VALUE, null);
                results.put(EXPECTED_VALUE, outputValues.get(i));
                results.put(ACTUAL_LINE_NUM, null);
                results.put(EXPECTED_LINE_NUM, String.valueOf(lineNumbers.get(i)));
                reportGenerator.addDetailsOfErrorKindTests(results);
            }
        } else {
            reportGenerator.addDetailsOfErrorKindTests(detailsOfTest);
        }
    }

    public static void setDetails(ITestContext context, ITestResult result, ReportGenerator reportGenerator) {
        Map<String, String> testDetails = getDetailsOfTest(context, result);
        if (result.getStatus() != ITestResult.SKIP && testDetails.get(KIND).equals(ERROR)
                                                   && testDetails.get(FORMAT_ERRORS) == null) {
            setDetailsOfErrorKindTests(context, reportGenerator, testDetails);
        }
        switch (result.getStatus()) {
            case ITestResult.SUCCESS:
                break;
            case ITestResult.FAILURE:
                reportGenerator.addDetailsOfFailedTests(testDetails);
                break;
            case ITestResult.SKIP:
                reportGenerator.addDetailsOfSkippedTests(testDetails);
                break;
            default:
                throw new RuntimeException("Invalid status");
        }
    }

    public static void deleteFilesWithinDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (!file.isDirectory()) {
                file.delete();
            }
        }
    }

    public static boolean deleteDirectory(String directoryPath) {
        File directory = new File(directoryPath);
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File f : files) {
                    boolean success = deleteDirectory(f.toPath().toString());
                    if (!success) {
                        return false;
                    }
                }
            }

        }
        return directory.delete();
    }
}
