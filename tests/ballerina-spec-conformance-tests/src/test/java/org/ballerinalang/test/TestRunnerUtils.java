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
        Pattern pattern = Pattern.compile("(.*)//\\s*@\\s*(\\S+)\\s*(.*)");
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            String outputHeader = matcher.group(2).trim();
            if (!(kindOfTestCase.equals(outputHeader) ||
                    (kindOfTestCase.equals(PARSER_ERROR) && outputHeader.equals(ERROR)))) {
                reportDiagnostics("format of output is incorrect, It should be //@error, //@output and //@panic");
            }
            String output = matcher.group(3).trim();
            line = matcher.group(1);
            lineNumbers.add(relativeLineNum);
            if (!output.isEmpty()) {
                outputValues.add(output);
            }
        }
        return line;
    }

    private static String getKindOfTest(String kind) {
        if (kind == null) {
            reportDiagnostics("Kind of testcase is not defined");
        }
        if (kind.equals(OUTPUT) || kind.equals(ERROR) || kind.equals(PANIC) || kind.equals(PARSER_ERROR)) {
            return kind;
        }
        reportDiagnostics("incorrect test kind, expected kind of testcases are OUTPUT, ERROR, PANIC");
        return OTHER;
    }

    private static Map<String, String> readHeadersOfTest(String line, BufferedReader buffReader) throws IOException {
        ArrayList<String> requiredHeaders = new ArrayList<>(Arrays.asList(START_TEST_CASE, DESCRIPTION, LABELS));
        Map<String, String> headers = new HashMap<>();
        Pattern pattern = Pattern.compile("^(\\S+)\\s*:\\s*(.*)");

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
            } else if (matcher.find() && !matcher.group(1).equals("int")) { //todo: handle int:Unsigned32 kind cases
                if (key != null) {
                    requiredHeaders.remove(key);
                    headers.put(key, value);
                }
                key = matcher.group(1);
                if (!(key.equals(LABELS) || key.equals(DESCRIPTION) || key.equals(FAIL_ISSUE) ||
                                                                                        key.equals(START_TEST_CASE))) {
                    reportDiagnostics(String.format("%s header is not defined", key));
                }
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

    public static void validateFormatOfTest(String diagnostic) {
        if (diagnostic != null) {
            Assert.fail(diagnostic);
        }
    }

    public static void isSkippedTest(boolean isSkippedTestCase) {
        if (isSkippedTestCase) {
            throw new SkipException("Skip");
        }
    }

    public static void setDetailsOfTest(ITestContext context, String kind, String fileName, int absLineNum,
                                        String diagnostics) {
        context.setAttribute("Kind", kind);
        context.setAttribute("FileName", fileName);
        context.setAttribute("lineNo", Integer.toString(absLineNum));
        context.setAttribute("FileName", fileName);
        context.setAttribute("FormatErrors", diagnostics);
    }

    public static void validateOutputOfTest(String path, String kind, List<String> outputValues, boolean isKnownIssue,
                                            List<Integer> lineNumbers, String fileName, int absLineNum,
                                            ITestContext context) {
        try {
            CompileResult compileResult = BCompileUtil.compile(path);
            if (isKnownIssue) {
                return; // todo: need to add known issues to separate report
            }
            switch (kind) {
                case ERROR:
                case PANIC:
                case PARSER_ERROR:
                    Collection<Diagnostic> errors = compileResult.getDiagnosticResult().errors();
                    if (!errors.isEmpty()) {
                        validateError(kind, errors, lineNumbers, outputValues, absLineNum, context);
                    } else {
                        validateRuntimeErrorOrPanic(compileResult, lineNumbers, outputValues, absLineNum, context);
                    }
                    break;
                case OUTPUT:
                    validateOutput(compileResult, outputValues, lineNumbers, absLineNum, context);
                    break;
            }
        } catch (Exception e) {
            Assert.fail("failed to run spec conformance test: \"" + fileName + "\"", e);
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

    public static List<Object> getDetailsOfErrorKindTests(ITestContext context) {
        List<Object> results = new ArrayList<>();
        results.add(context.getAttribute("LineNumbers"));
        results.add(context.getAttribute("OutputValues"));
        results.add(context.getAttribute("Diagnostics"));

        return results;
    }

    public static void validateError(String kind, Collection<Diagnostic> diagnostics, List<Integer> lineNumbers,
                                     List<String> outputValues, int absLineNum, ITestContext context) {
        setDetailsOfErrorKindTest(context, lineNumbers, outputValues, diagnostics);
        if (kind.equals(PARSER_ERROR)) {
            return;
        }
        Iterator<Diagnostic> iterator = diagnostics.iterator();
        for (int i = 0; i < diagnostics.size(); i++) {
            Diagnostic diagnostic = iterator.next();
            String actualLineNum =
                    String.valueOf(diagnostic.location().lineRange().startLine().line() + 1 + absLineNum);
            String message = diagnostic.message().replace(CARRIAGE_RETURN_CHAR, EMPTY_STRING);
            String expLineNum;
            String expValue;
            try {
                expLineNum = String.valueOf(lineNumbers.get(i) + absLineNum);
                expValue = outputValues.get(i);
            } catch (IndexOutOfBoundsException e) {
                expLineNum = null;
                expValue = null;
            }
            setResultsAttributes(context, message, expValue, actualLineNum, expLineNum);
            Assert.assertEquals(actualLineNum, expLineNum);
        }
        if (outputValues.size() > diagnostics.size()) {
            setResultsAttributes(context, null, outputValues.get(diagnostics.size()), null,
                          String.valueOf(lineNumbers.get(diagnostics.size())));
            Assert.assertNull(iterator.next().toString());
        }
    }

    public static void validateOutput(CompileResult compileResult, List<String> outputValues, List<Integer> lineNumbers,
                                      int absLineNum, ITestContext context) {
        Collection<Diagnostic> diagnostics = compileResult.getDiagnosticResult().errors();
        if (!diagnostics.isEmpty()) {
            Diagnostic diagnostic = diagnostics.iterator().next();
            String outputVal = outputValues.get(0);
            String message = diagnostic.message().replace(CARRIAGE_RETURN_CHAR, EMPTY_STRING);
            String actualLineNum =
                   String.valueOf(diagnostic.location().lineRange().startLine().line() + absLineNum);
            setResultsAttributes(context, message, outputVal, actualLineNum,
                                 String.valueOf(lineNumbers.get(0) + absLineNum));
            Assert.assertEquals(outputVal, diagnostic.toString());
        }

        BRunUtil.ExitDetails exitDetails = BRunUtil.run(compileResult);
        if (!exitDetails.errorOutput.isEmpty()) {
            String outputVal = outputValues.get(0);
            setResultsAttributes(context, exitDetails.errorOutput, outputVal, null,
                                 String.valueOf(lineNumbers.get(0) + absLineNum));
            Assert.assertEquals(outputVal, exitDetails.errorOutput);
        }

        String consoleOutput = exitDetails.consoleOutput;
        String[] results = consoleOutput.split("\r\n|\r|\n");
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
            Assert.assertEquals(expectedOutput, actualOutput);
        }

        if (results.length > size) {
            setResultsAttributes(context, results[size - 1], null, null, null);
            Assert.assertNull(results[size - 1]);
        }
    }

    public static void validateRuntimeErrorOrPanic(CompileResult compileResult, List<Integer> lineNumbers,
                                                   List<String> outputValues, int absLineNum, ITestContext context) {
        BRunUtil.ExitDetails exitDetails = BRunUtil.run(compileResult);
        if (exitDetails.errorOutput.isEmpty()) {
            String outputVal = outputValues.get(0);
            setResultsAttributes(context, exitDetails.consoleOutput, outputVal, null,
                                 String.valueOf(lineNumbers.get(0) + absLineNum));
            Assert.assertNull(outputVal);
        }
        String errorOutput = BRunUtil.run(compileResult).errorOutput;
        Matcher matcherForErrorMsg = Pattern.compile("^(error)\\s*:\\s*(.*)").matcher(errorOutput);
        Matcher matcherForErrorLineNum = Pattern.compile(":(\\d+)\\)$").matcher(errorOutput);

        if (matcherForErrorMsg.find() && matcherForErrorLineNum.find()) {
            int lineNum = lineNumbers.get(0) + absLineNum;
            String errorLineNum = String.valueOf(Integer.parseInt(matcherForErrorLineNum.group(1)) + absLineNum);
            setResultsAttributes(context, matcherForErrorMsg.group(2), outputValues.get(0),
                                 errorLineNum, String.valueOf(lineNum));
            Assert.assertEquals(errorLineNum, String.valueOf(lineNum));
        }
    }

    public static void setResultsAttributes(ITestContext context, String actualValue, String expectedValue,
                                            String actualLineNo, String expectedLineNo) {
        context.setAttribute("ActualValue", actualValue);
        context.setAttribute("ExpectedValue", expectedValue);
        context.setAttribute("ActualLineNo", actualLineNo);
        context.setAttribute("ExpectedLineNo", expectedLineNo);
    }

    public static List<String> getDetailsOfTest(ITestContext context, ITestResult result) {
        List<String> results = new ArrayList<>();
        results.add((String) context.getAttribute("FileName"));
        results.add((String) context.getAttribute("Kind"));
        results.add((String) context.getAttribute("lineNo"));
        String formatErrors = (String) context.getAttribute("FormatErrors");
        results.add(formatErrors);
        if (result.getStatus() == ITestResult.SKIP || formatErrors != null) {
            return results;
        }
        results.add((String) context.getAttribute("ActualValue"));
        results.add((String) context.getAttribute("ExpectedValue"));
        results.add((String) context.getAttribute("ActualLineNo"));
        results.add((String) context.getAttribute("ExpectedLineNo"));
        return results;
    }

    public static void setDetailsOfErrorKindTests(ITestContext context, ReportGenerator reportGenerator,
                                                  List<String> detailsOfTest) {
        List<Object> detailsOfErrorKindTests = getDetailsOfErrorKindTests(context);

        boolean haveOnlyNulls = detailsOfErrorKindTests.stream().allMatch(x -> x == null);

        if (!haveOnlyNulls) {
            int absLineNo = Integer.parseInt(detailsOfTest.get(2));
            List<Integer> lineNumbers = (List<Integer>) detailsOfErrorKindTests.get(0);
            List<String> outputValues = (List<String>) detailsOfErrorKindTests.get(1);
            Collection<Diagnostic> diagnostics = (Collection<Diagnostic>) detailsOfErrorKindTests.get(2);

            Iterator<Diagnostic> iterator = diagnostics.iterator();
            for (int i = 0; i < diagnostics.size(); i++) {
                List<String> results = new ArrayList<>();
                results.add(detailsOfTest.get(0));
                results.add(detailsOfTest.get(1));
                results.add(detailsOfTest.get(2));
                results.add(detailsOfTest.get(3));
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
                results.add(message);
                results.add(expOutput);
                results.add(String.valueOf(actualLineNum));
                results.add(expLineNum);
                reportGenerator.addDetailsOfErrorKindTests(results);
            }

            for (int i = diagnostics.size(); i < outputValues.size(); i++) {
                List<String> results = new ArrayList<>();
                results.add(detailsOfTest.get(0));
                results.add(detailsOfTest.get(1));
                results.add(detailsOfTest.get(2));
                results.add(detailsOfTest.get(3));
                results.add(null);
                results.add(outputValues.get(i));
                results.add(null);
                results.add(String.valueOf(lineNumbers.get(i)));
                reportGenerator.addDetailsOfErrorKindTests(results);
            }
        } else {
            reportGenerator.addDetailsOfErrorKindTests(detailsOfTest);
        }
    }

    public static void setDetails(ITestContext context, ITestResult result, ReportGenerator reportGenerator) {
        List<String> detailsOfTest = getDetailsOfTest(context, result);
        if (result.getStatus() != ITestResult.SKIP && detailsOfTest.get(1).equals(ERROR)
                                                                                && detailsOfTest.get(3) == null) {
            setDetailsOfErrorKindTests(context, reportGenerator, detailsOfTest);
        }
        switch (result.getStatus()) {
            case ITestResult.SUCCESS:
                break;
            case ITestResult.FAILURE:
                reportGenerator.addDetailsOfFailedTests(detailsOfTest);
                break;
            case ITestResult.SKIP:
                reportGenerator.addDetailsOfSkippedTests(detailsOfTest);
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
