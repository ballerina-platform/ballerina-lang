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
package org.ballerinalang.testerina.coverage;

import org.ballerinalang.bre.coverage.*;
import org.ballerinalang.config.ConfigRegistry;
import org.ballerinalang.testerina.core.entity.Test;
import org.ballerinalang.testerina.core.entity.TestSuite;
import org.ballerinalang.util.codegen.LineNumberInfo;
import org.ballerinalang.util.codegen.PackageInfo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CoverageDataFormatter {

    private String testName = null;

    public List<LCovData> getFormattedCoverageData(Map<String, List<ExecutedInstruction>> executedInstructionOrderMap,
                                                   TestSuite suite) {

        PackageInfo entryPackageInfo = suite.getProgramFile().getEntryPackage();
        String entryPkgPath = entryPackageInfo.pkgPath;

        List<LCovData> packageCoverageList = new ArrayList<>();

        executedInstructionOrderMap.forEach((packagePath, executedInstructionOrder) -> {

            // get only current project's module
            if(packagePath.equals(entryPkgPath)) {

                for(ExecutedInstruction executedInstruction : executedInstructionOrder) {

                    //TODO: init should be covered later. init has declarions and configs
                    if(!((packagePath + ".<init>").equals(executedInstruction.getFunctionName()) ||
                            (packagePath + ".<start>").equals(executedInstruction.getFunctionName()))) {

                        // filter out the source code Ips
                        if(skipTestFunctionIps(executedInstruction, suite)) {
                            continue;
                        }

                        boolean lCovDataFound = false;
                        for(LCovData lCovData : packageCoverageList) {

                            if(lCovData.getTestName().equals(testName)) {
                                lCovDataFound = true;

                                boolean lCovSourceFileFound = false;
                                for(LCovSourceFile lCovSourceFile : lCovData.getlCovSourceFileList()) {

                                    if(lCovSourceFile.getSourceFilePath().equals(packagePath + "/" + executedInstruction.getFileName())) {
                                        lCovSourceFileFound = true;

                                        boolean lineNumFound = false;
                                        LineNumberInfo lineNumberInfo = entryPackageInfo.getLineNumberInfo(executedInstruction.getIp());
                                        for(LCovDA lCovDA : lCovSourceFile.getlCovDAList()) {
                                            if(lineNumberInfo.getLineNumber() == lCovDA.getLineNumber()) {
                                                lineNumFound = true;

                                                lCovDA.setLineExecutionCount(lCovDA.getLineExecutionCount() + 1);

                                                break;
                                            }
                                        }
                                        if(!lineNumFound) {
                                            LCovDA lCovDA = new LCovDA(lineNumberInfo.getLineNumber(), 1, 0);
                                            lCovSourceFile.getlCovDAList().add(lCovDA);
                                        }

                                        break;
                                    }
                                }
                                if(!lCovSourceFileFound) {

                                    LineNumberInfo lineNumberInfo = entryPackageInfo.getLineNumberInfo(executedInstruction.getIp());
                                    LCovDA lCovDA = new LCovDA(lineNumberInfo.getLineNumber(), 1, 0);

                                    LCovSourceFile lCovSourceFile = new LCovSourceFile(
                                            //TODO: package path should be package folder path
                                            packagePath + "/" + executedInstruction.getFileName(), 1, 1);
                                    lCovSourceFile.getlCovDAList().add(lCovDA);

                                    lCovData.getlCovSourceFileList().add(lCovSourceFile);

                                }

                                break;
                            }


                        }
                        if(!lCovDataFound) {

                            LineNumberInfo lineNumberInfo = entryPackageInfo.getLineNumberInfo(executedInstruction.getIp());
                            LCovDA lCovDA = new LCovDA(lineNumberInfo.getLineNumber(), 1, 0);

                            LCovSourceFile lCovSourceFile = new LCovSourceFile(
                                    //TODO: package path should be package folder path
                                    packagePath + "/" + executedInstruction.getFileName(), 1, 1);
                            lCovSourceFile.getlCovDAList().add(lCovDA);

                            LCovData lCovData = new LCovData(testName);
                            lCovData.getlCovSourceFileList().add(lCovSourceFile);

                            packageCoverageList.add(lCovData);
                        }

                    }

                }

            }

        });

        return packageCoverageList;

    }

    private boolean skipTestFunctionIps(ExecutedInstruction executedInstruction, TestSuite suite) {

        boolean skipTestFunctionIps = false;

        if(suite.getBeforeSuiteFunctionNames().contains(executedInstruction.getFunctionName())) {
            skipTestFunctionIps = true;
        }

        for(Test test : suite.getTests()) {
            if(executedInstruction.getFunctionName().equals(test.getBeforeTestFunction())) {
                skipTestFunctionIps = true;
                break;
            }

            if(executedInstruction.getFunctionName().equals(test.getTestName())) {
                testName = executedInstruction.getFunctionName();
                skipTestFunctionIps = true;
                break;
            }

            if(executedInstruction.getFunctionName().equals(test.getAfterTestFunction())) {
                skipTestFunctionIps = true;
                break;
            }
        }

        if(suite.getAfterSuiteFunctionNames().contains(executedInstruction.getFunctionName())) {
           skipTestFunctionIps = true;
        }

        return skipTestFunctionIps;
    }

    public void writeFormattedCovDataToFile(List<LCovData> packageLCovDataList, String sourceRoot) throws IOException {

        ConfigRegistry configRegistry = ConfigRegistry.getInstance();
        String customCovReportname = configRegistry.getAsString("coverage.report.name");
        String customCovReportPath = configRegistry.getAsString("coverage.report.path");

        String covReportFolderName = "coverage";
        String defaultCovReportFilename = "coverage.info";
        String projectOutputPath = sourceRoot + "/target";
        StringBuffer lcovOutputStrBuf = new StringBuffer();

        String covReportFilePath;
        if(customCovReportPath ==  null) {
            covReportFilePath = projectOutputPath + "/" + covReportFolderName;
        } else {
            covReportFilePath = customCovReportPath + "/" + covReportFolderName;
        }

        String covReportFileName;
        if(customCovReportname ==  null) {
            covReportFileName = defaultCovReportFilename;
        } else {
            covReportFileName = customCovReportname;
        }

        for(LCovData lCovData : packageLCovDataList) {
            lcovOutputStrBuf.append("TN:").append(lCovData.getTestName()).append("\n");

            for(LCovSourceFile lCovSourceFile : lCovData.getlCovSourceFileList()) {
                lcovOutputStrBuf.append("SF:").append(sourceRoot).append("/").append(lCovSourceFile.getSourceFilePath())
                        .append("\n");
                for(LCovDA lCovDA : lCovSourceFile.getlCovDAList()) {
                    lcovOutputStrBuf.append("DA:").append(lCovDA.getLineNumber()).append(",")
                            .append(lCovDA.getLineExecutionCount()).append("\n");
                }

                lcovOutputStrBuf.append("end_of_record");
            }
        }

        BufferedWriter covReportFileBufWriter = null;
        try {

            File covReportDir = new File(covReportFilePath);
            if(covReportDir.exists()) {
                covReportDir.delete();
            }
            covReportDir.mkdirs();

            File covReportFile = new File(covReportDir, covReportFileName);
            FileWriter covReportFileWriter = new FileWriter(covReportFile);
            covReportFileBufWriter = new BufferedWriter(covReportFileWriter);

            covReportFileBufWriter.write(lcovOutputStrBuf.toString());

            System.out.println("Coverage report is written to " + covReportFilePath + "/" + covReportFileName);

        } catch (IOException e) {

            e.printStackTrace();

        } finally {
            if(covReportFileBufWriter != null) {
                covReportFileBufWriter.close();
            }
        }
    }
}
