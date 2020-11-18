/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.runtime.entity;

import com.google.gson.Gson;
import org.ballerinalang.test.runtime.CoverageMain;
import org.ballerinalang.test.runtime.util.TesterinaConstants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Coverage analysis of a specific module used to generate the Json object.
 *
 * @since 1.2.0
 */
public class ModuleCoverage {

    private String name;
    private int coveredLines;
    private int missedLines;
    private float coveragePercentage;
    private List<SourceFile> sourceFiles = new ArrayList<>();

    private static PrintStream errStream = System.err;

    private static ModuleCoverage instance = new ModuleCoverage();

    public static ModuleCoverage getInstance() {
        return instance;
    }

    /**
     * Adds the code snippet from the source file highlighted with covered and missed lines.
     *
     * @param moduleName name of the module that the source file belongs to
     * @param fileName source file name
     * @param coveredLines list of lines covered
     * @param missedLines list of lines missed
     */
    public void addSourceFileCoverage (String moduleName, String fileName, List<Integer> coveredLines,
                                       List<Integer> missedLines) {
        SourceFile sourceFile = new SourceFile(moduleName, fileName, coveredLines, missedLines);
        this.sourceFiles.add(sourceFile);
        this.coveredLines += coveredLines.size();
        this.missedLines += missedLines.size();
        setCoveragePercentage();
    }

    public void updateSourceFileCoverage(String jsonCachePath, String moduleName, String fileName,
                                         List<Integer> coveredLines, List<Integer> missedLines) {
        Path jsonTmpSummaryPath = Paths.get(jsonCachePath, TesterinaConstants.COVERAGE_FILE);

        // Calculate the coverage percentage
        float coverageVal = (float) coveredLines.size() / (coveredLines.size() + missedLines.size()) * 100;
        float coveragePercentage = (float) (Math.round(coverageVal * 100.0) / 100.0);

        if (jsonTmpSummaryPath.toFile().exists()) {
            try (BufferedReader bufferedReader = Files.newBufferedReader(jsonTmpSummaryPath, StandardCharsets.UTF_8)) {
                boolean isUpdated = false;
                Gson gson = new Gson();
                ModuleCoverage moduleCoverage = gson.fromJson(bufferedReader, ModuleCoverage.class);
                ModuleCoverage newCoverage = new ModuleCoverage();

                // Look for the source in the existing JSON
                for (SourceFile sourceFile : moduleCoverage.sourceFiles) {
                    if (sourceFile.name.equals(fileName)) {
                        if (sourceFile.coveragePercentage <= coveragePercentage) {
                            // Since the percentages are different we need to update the source file
                            newCoverage.addSourceFileCoverage(moduleName, fileName, coveredLines, missedLines);
                            isUpdated = true;
                        }
                    } else {
                        newCoverage.addSourceFileCoverage(sourceFile.moduleName, sourceFile.name,
                                sourceFile.coveredLines, sourceFile.missedLines);
                    }
                }

                // Rewrite only if the file has been updated
                if (isUpdated) {
                    CoverageMain.writeCoverageToJsonFile(newCoverage, jsonTmpSummaryPath);
                }
            } catch (IOException e) {
                errStream.println("error while updating source file coverage : " + e);
            }
        } else {
            Path jsonCache = Paths.get(jsonCachePath);

            if (!jsonCache.toFile().exists()) {
                try {
                    Files.createDirectories(jsonCache);
                    ModuleCoverage moduleCoverage = new ModuleCoverage();
                    moduleCoverage.addSourceFileCoverage(moduleName, fileName, coveredLines, missedLines);
                    CoverageMain.writeCoverageToJsonFile(moduleCoverage, jsonTmpSummaryPath);
                } catch (Exception e) {
                    errStream.println("error while updating source file coverage : " + e);
                }
            }
        }
    }

    private void setCoveragePercentage() {
        float coverageVal = (float) this.coveredLines / (this.coveredLines + this.missedLines) * 100;
        this.coveragePercentage = (float) (Math.round(coverageVal * 100.0) / 100.0);
    }

    public float getCoveragePercentage() {
        return coveragePercentage;
    }

    public int getCoveredLines() {
        return coveredLines;
    }

    public int getMissedLines() {
        return missedLines;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    /**
     * Inner class for the SourceFile in Json.
     */
    private static class SourceFile {
        private String name;
        private String moduleName;
        private List<Integer> coveredLines;
        private List<Integer> missedLines;
        private float coveragePercentage;
        private String sourceCode;

        private SourceFile(String moduleName, String fileName, List<Integer> coveredLines, List<Integer> missedLines) {
            this.name = fileName;
            this.moduleName = moduleName;
            this.coveredLines = coveredLines;
            this.missedLines = missedLines;
            setCoveragePercentage(coveredLines, missedLines);
            setSourceCode(moduleName, fileName);
        }

        private void setCoveragePercentage(List<Integer> coveredLines, List<Integer> missedLines) {
            float coverageVal = (float) coveredLines.size() / (coveredLines.size() + missedLines.size()) * 100;
            this.coveragePercentage = (float) (Math.round(coverageVal * 100.0) / 100.0);
        }

        private void setSourceCode(String moduleName, String fileName) {
            Path sourceFile;
            if (TesterinaConstants.DOT.equals(moduleName)) {
                sourceFile = Paths.get(fileName);
            } else {
                sourceFile = Paths.get(TesterinaConstants.SRC_DIR).resolve(moduleName).resolve(fileName);
            }
            StringBuilder contentBuilder = new StringBuilder();
            try (Stream<String> stream = Files.lines(sourceFile, StandardCharsets.UTF_8)) {
                stream.forEach(s -> contentBuilder.append(s).append("\n"));
            } catch (IOException e) {
                errStream.println("error while analyzing code coverage source files : " + e);
                Runtime.getRuntime().exit(1);
            }
            this.sourceCode = contentBuilder.toString();
        }

        public float getCoveragePercentage() {
            return this.coveragePercentage;
        }

        public String getSourceCode() {
            return this.sourceCode;
        }

        public List<Integer> getCoveredLines() {
            return this.coveredLines;
        }

        public List<Integer> getMissedLines() {
            return this.missedLines;
        }

        public String getName() {
            return name;
        }
    }
}
