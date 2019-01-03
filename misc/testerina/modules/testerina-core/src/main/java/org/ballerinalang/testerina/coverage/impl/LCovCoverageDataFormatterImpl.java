/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.testerina.coverage.impl;

import org.ballerinalang.config.ConfigRegistry;
import org.ballerinalang.testerina.core.entity.Test;
import org.ballerinalang.testerina.core.entity.TestSuite;
import org.ballerinalang.testerina.coverage.CoverageDataFormatter;
import org.ballerinalang.testerina.coverage.CoverageManager;
import org.ballerinalang.testerina.coverage.ExecutedInstruction;
import org.ballerinalang.testerina.coverage.LCovDA;
import org.ballerinalang.testerina.coverage.LCovData;
import org.ballerinalang.testerina.coverage.LCovSourceFile;
import org.ballerinalang.testerina.util.Constants;
import org.ballerinalang.util.BLangConstants;
import org.ballerinalang.util.BLangUtils;
import org.ballerinalang.util.codegen.LineNumberInfo;
import org.ballerinalang.util.debugger.ModuleLineNumberInfo;
import org.ballerinalang.util.debugger.ProjectLineNumberInfoHolder;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.wso2.ballerinalang.compiler.util.Names;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Writer;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is for converting the Ip coverage information to output format.
 * Format: lcov
 *
 * @since 0.985.0
 */
public class LCovCoverageDataFormatterImpl implements CoverageDataFormatter<LCovData> {

    private static PrintStream outStream = System.out;

    private Map<String, ProjectLineNumberInfoHolder> lineNumberInfoHolderForProject;

    // Coverage data file configs
    private String testName = null;
    private static final String COVERAGE_DATA_FILE_NAME = "coverage.data.file.name";
    private static final String COVERAGE_DATA_FILE_PATH = "coverage.data.file.path";
    private static final String COVERAGE_REPORT_FOLDER = "coverage";
    private static final String COVERAGE_REPORT_FILE = "coverage.info";
    private static final String COVERAGE_REPORT_TARGET_PATH = "/target";

    // lcov fields
    private static final String LCOV_TN = "TN:";
    private static final String LCOV_SF = "SF:";
    private static final String LCOV_DA = "DA:";
    private static final String LCOV_EOR = "end_of_record";

    public LCovCoverageDataFormatterImpl() {
        CoverageManager coverageManager = CoverageManager.getInstance();
        lineNumberInfoHolderForProject = coverageManager.getLineNumberInfoHolderForProject();
    }

    /**
     * Outputs lcov formatted coverage data for the Ip coverage data for each test suite for the project.
     *
     * @param executedInstructionOrderMap Ip coverage data map for each module of the project
     * @param testSuiteForProject         test suites map for each module of the project
     * @return lcov formatted data list for each test function
     */
    public List<LCovData> getFormattedCoverageData(Map<String, List<ExecutedInstruction>> executedInstructionOrderMap,
                                                   Map<String, TestSuite> testSuiteForProject) {

        List<LCovData> packageCoverageList = new ArrayList<>();

        testSuiteForProject.forEach((testPkgPath, suite) -> {

            String entryPkgPath = suite.getProgramFile().getEntryPackage().pkgPath;
            ProjectLineNumberInfoHolder projectLineNumberInfoHolderForPkg =
                    lineNumberInfoHolderForProject.get(entryPkgPath);
            ModuleLineNumberInfo entryPkgLineNumberInfo = projectLineNumberInfoHolderForPkg.getPackageInfoMap()
                    .get(entryPkgPath);

            // Calculating instrumented lines for each file
            Map<String, Integer> fileLineCoverage = new HashMap<>();
            String[] modulePathSlices = BLangUtils.getModulePathSlices(entryPkgPath);
            entryPkgLineNumberInfo.getLineNumbers().keySet().forEach(key -> {
                String fileName = key.split(BLangConstants.COLON)[0];

                // skipping module init function Ips which comes with modulename:lineNo
                if (!fileName.endsWith(BLangConstants.BLANG_SRC_FILE_SUFFIX)) {
                    return;
                }
                String fileNameWithModule = modulePathSlices[1] + File.separator + fileName;
                fileLineCoverage.put(fileNameWithModule,
                        fileLineCoverage.get(
                                fileNameWithModule) == null ? 1 : fileLineCoverage.get(fileNameWithModule) + 1);
            });

            List<ExecutedInstruction> executedInstructionOrder = executedInstructionOrderMap.get(entryPkgPath);
            // null check is mandatory for runtime executedInstructionOrder null scenarios.
            // E.g. tests with invalid test group where executedInstructionOrderMap size=0
            if (executedInstructionOrder != null) {
                for (ExecutedInstruction executedInstruction : executedInstructionOrder) {

                    //TODO: init should be covered later. init has declarions and global configs for functions
                    //TODO: start should be covered later. start has declarions and global configs for services
                    if (executedInstruction.getFunctionName()
                            .endsWith(Names.INIT_FUNCTION_SUFFIX.getValue()) ||
                            executedInstruction.getFunctionName()
                                    .endsWith(Names.START_FUNCTION_SUFFIX.getValue()) ||
                            executedInstruction.getFunctionName()
                                    .endsWith(Names.STOP_FUNCTION_SUFFIX.getValue())) {
                        continue;
                    }

                    // filter out the test source code Ips
                    if (skipTestFunctionIps(executedInstruction, suite)) {
                        continue;
                    }

                    // skipping module init function Ips which comes with modulename:lineNo
                    LineNumberInfo lineNumberInfo = entryPkgLineNumberInfo
                            .getLineNumberInfo(executedInstruction.getIp());
                    if (!lineNumberInfo.getFileName().endsWith(BLangConstants.BLANG_SRC_FILE_SUFFIX)) {
                        continue;
                    }

                    populateCoverageData(executedInstruction, packageCoverageList, lineNumberInfo, fileLineCoverage,
                            modulePathSlices);
                }
            }
        });

        return packageCoverageList;
    }

    // return true if the function comes from tests folder
    private boolean skipTestFunctionIps(ExecutedInstruction executedInstruction, TestSuite suite) {

        boolean skipTestFunctionIps = false;

        if (executedInstruction.getFunctionName().endsWith(Constants.TEST_INIT_FUNCTION_SUFFIX) ||
                executedInstruction.getFunctionName().endsWith(Constants.TEST_START_FUNCTION_SUFFIX) ||
                executedInstruction.getFunctionName().endsWith(Constants.TEST_STOP_FUNCTION_SUFFIX)) {

            skipTestFunctionIps = true;

        }  else if (executedInstruction.getModulePath().equals(Names.DEFAULT_PACKAGE.getValue())) {
            String functionName = executedInstruction.getFunctionName();

            if (suite.getBeforeSuiteFunctionNames().contains(functionName) ||
                    suite.getAfterSuiteFunctionNames().contains(functionName)) {
                skipTestFunctionIps = true;
            } else {
                for (Test test : suite.getTests()) {
                    if (functionName.equals(test.getBeforeTestFunction()) ||
                            functionName.equals(test.getAfterTestFunction()) ||
                            functionName.equals(test.getDataProvider())) {
                        skipTestFunctionIps = true;
                        break;
                    }
                    if (functionName.equals(test.getTestName())) {
                        testName = functionName;
                        skipTestFunctionIps = true;
                        break;
                    }
                }
            }

        } else if (executedInstruction.getFileName().startsWith("tests/")) {

            String functionName = executedInstruction.getFunctionName();
            for (Test test : suite.getTests()) {
                if (functionName.equals(test.getTestName())) {
                    testName = functionName;
                    break;
                }
            }

            skipTestFunctionIps = true;
        }

        return skipTestFunctionIps;
    }

    // populate coverage data list for the project
    private void populateCoverageData(ExecutedInstruction executedInstruction, List<LCovData> packageCoverageList,
                                      LineNumberInfo lineNumberInfo, Map<String, Integer> fileLineCoverage,
                                      String[] pkgPathSlices) {
        String sourceFilePath = pkgPathSlices[1] + File.separator
                + executedInstruction.getFileName();

        boolean lCovDataFound = false;
        for (LCovData lCovData : packageCoverageList) {

            if (lCovData.getTestName().equals(testName)) {
                lCovDataFound = true;
                populateCoverageDataSourceFileSection(lCovData, sourceFilePath, lineNumberInfo, fileLineCoverage);

                break;
            }
        }
        if (!lCovDataFound) {
            LCovDA lCovDA = new LCovDA(lineNumberInfo.getLineNumber(), 1, 0);

            LCovSourceFile lCovSourceFile = new LCovSourceFile(
                    sourceFilePath, 1,
                    fileLineCoverage.get(sourceFilePath));
            lCovSourceFile.getlCovDAList().add(lCovDA);

            LCovData lCovData = new LCovData(testName);
            lCovData.getlCovSourceFileList().add(lCovSourceFile);

            packageCoverageList.add(lCovData);
        }
    }

    // populate coverage data list for the source file section
    private void populateCoverageDataSourceFileSection(LCovData lCovData, String sourceFilePath,
                                      LineNumberInfo lineNumberInfo, Map<String, Integer> fileLineCoverage) {
        boolean lCovSourceFileFound = false;
        for (LCovSourceFile lCovSourceFile : lCovData.getlCovSourceFileList()) {

            if (lCovSourceFile.getSourceFilePath().equals(sourceFilePath)) {
                lCovSourceFileFound = true;
                populateCoverageDataLineSection(lCovSourceFile, lineNumberInfo);

                break;
            }
        }
        if (!lCovSourceFileFound) {

            LCovDA lCovDA = new LCovDA(lineNumberInfo.getLineNumber(), 1, 0);

            LCovSourceFile lCovSourceFile = new LCovSourceFile(
                    sourceFilePath, 1,
                    fileLineCoverage.get(sourceFilePath));
            lCovSourceFile.getlCovDAList().add(lCovDA);

            lCovData.getlCovSourceFileList().add(lCovSourceFile);
        }
    }

    // populate coverage data list for the source code line section for the given source file
    private void populateCoverageDataLineSection(LCovSourceFile lCovSourceFile, LineNumberInfo lineNumberInfo) {
        boolean lineNumFound = false;
        for (LCovDA lCovDA : lCovSourceFile.getlCovDAList()) {
            if (lineNumberInfo.getLineNumber() == lCovDA.getLineNumber()) {
                lineNumFound = true;

                lCovDA.setLineExecutionCount(
                        lCovDA.getLineExecutionCount() + 1);

                break;
            }
        }
        if (!lineNumFound) {
            LCovDA lCovDA = new LCovDA(lineNumberInfo.getLineNumber(), 1, 0);
            lCovSourceFile.getlCovDAList().add(lCovDA);

            lCovSourceFile.setNumOfLineExecuted(
                    lCovSourceFile.getNumOfLineExecuted() + 1);
        }
    }

    /**
     * write lcov formatted data to the coverage data file. coverage output file location and file name
     * can be changed with --config configuration file
     *
     * @param packageCovDataList lcov formatted coverage data list
     * @param sourceRoot          project source root folder location
     * @throws IOException throws when output writing to the file fails.
     */
    public void writeFormattedCovDataToFile(List<LCovData> packageCovDataList, String sourceRoot) throws IOException {

        ConfigRegistry configRegistry = ConfigRegistry.getInstance();
        String customCovReportname = configRegistry.getAsString(COVERAGE_DATA_FILE_NAME);
        String customCovReportPath = configRegistry.getAsString(COVERAGE_DATA_FILE_PATH);

        String projectOutputPath = sourceRoot + COVERAGE_REPORT_TARGET_PATH;
        StringBuffer lcovOutputStrBuf = new StringBuffer();

        String covReportFilePath;
        if (customCovReportPath == null) {
            covReportFilePath = Paths.get(projectOutputPath, COVERAGE_REPORT_FOLDER).toString();
        } else {
            covReportFilePath = Paths.get(customCovReportPath, COVERAGE_REPORT_FOLDER).toString();
        }

        String covReportFileName;
        if (customCovReportname == null) {
            covReportFileName = COVERAGE_REPORT_FILE;
        } else {
            covReportFileName = customCovReportname;
        }

        for (LCovData lCovData : packageCovDataList) {
            lcovOutputStrBuf.append(LCOV_TN).append(lCovData.getTestName()).append(Constants.COMMA)
                    .append(Constants.NEWLINE);

            for (LCovSourceFile lCovSourceFile : lCovData.getlCovSourceFileList()) {
                lcovOutputStrBuf.append(LCOV_SF).append(Paths.get(sourceRoot, lCovSourceFile.getSourceFilePath()))
                        .append(Constants.NEWLINE);
                for (LCovDA lCovDA : lCovSourceFile.getlCovDAList()) {
                    lcovOutputStrBuf.append(LCOV_DA).append(lCovDA.getLineNumber()).append(Constants.COMMA)
                            .append(lCovDA.getLineExecutionCount()).append(Constants.NEWLINE);
                }

                lcovOutputStrBuf.append(LCOV_EOR).append(Constants.NEWLINE);
                float fileCoveragePercentage = (((float) lCovSourceFile.getNumOfLineExecuted()) /
                        ((float) lCovSourceFile.getNumOfInstrumentedLines())) * 100;
                outStream.println("Coverage for " + lCovSourceFile.getSourceFilePath() + " is " +
                        fileCoveragePercentage + Constants.PERCENTAGE);
            }
        }

        // writing lcov coverage data file to file
        File covReportDir = new File(covReportFilePath);

        if (!covReportDir.exists()) {
            if (covReportDir.mkdirs()) {

                outStream.println("Coverage folder is created " + covReportFilePath);
                writeCoverageDataFile(covReportFilePath, covReportDir, covReportFileName, lcovOutputStrBuf.toString());
            }
        } else {
            writeCoverageDataFile(covReportFilePath, covReportDir, covReportFileName, lcovOutputStrBuf.toString());
        }
    }

    private void writeCoverageDataFile(String covReportFilePath, File covReportDir, String covReportFileName,
                                       String lcovOutput) throws IOException {

        BufferedWriter covReportFileBufWriter = null;
        try {

            File covReportFile = new File(covReportDir, covReportFileName);
            Writer covReportFileWriter = new OutputStreamWriter(new FileOutputStream(covReportFile),
                                                                                            BLangConstants.UTF8);
            covReportFileBufWriter = new BufferedWriter(covReportFileWriter);

            covReportFileBufWriter.write(lcovOutput);

            outStream.println("Coverage report is written to " +
                    covReportFilePath + File.separator + covReportFileName);

        } catch (IOException e) {

            throw new BallerinaException("Error in writing coverage report to " +
                    covReportFilePath + File.separator + covReportFileName);

        } finally {
            if (covReportFileBufWriter != null) {
                covReportFileBufWriter.close();
            }
        }
    }
}
