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

import io.ballerina.projects.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    /**
     * Adds the code snippet from the source file highlighted with covered and missed lines.
     *
     * @param document     Source file
     * @param coveredLines List of lines covered
     * @param missedLines  List of lines missed
     * @param emptyLines   List of empty lines
     */
    public void addSourceFileCoverage(Document document, List<Integer> coveredLines,
                                      List<Integer> missedLines, List<Integer> emptyLines) {
        SourceFile sourceFile = new SourceFile(document, coveredLines, missedLines, emptyLines);
        this.sourceFiles.add(sourceFile);
        this.coveredLines += coveredLines.size();
        this.missedLines += missedLines.size();
        setCoveragePercentage();
    }

    /**
     * Check if given source file is already added to module coverage.
     *
     * @param fileName File name of the source file
     * @return    If source file is already added, return true. Otherwise, return false.
     */
    public boolean containsSourceFile(String fileName) {
        boolean isAvailable = false;
        for (SourceFile sourceFile : sourceFiles) {
            if (sourceFile.getName().equals(fileName)) {
                isAvailable = true;
                break;
            }
        }
        return isAvailable;
    }

    /**
     * Update coverage information for a given source file.
     *
     * @param document               Updated source file
     * @param coveredLines           Latest covered lines
     * @param missedLines            Latest missed lines
     * @param emptyLines             Latest empty lines
     * @param coveredMissedLineCount Latest covered line count for previously missed lines
     * @param coveredEmptyLineCount  Latest covered line count for previously empty lines
     * @param missedEmptyLineCount   Latest missed line count for previously empty lines
     */
    public void updateCoverage(Document document, List<Integer> coveredLines,
                               List<Integer> missedLines, List<Integer> emptyLines, int coveredMissedLineCount,
                               int coveredEmptyLineCount, int missedEmptyLineCount) {
        List<SourceFile> sourceFileList = new ArrayList<>(sourceFiles);
        for (SourceFile sourceFile : sourceFileList) {
            if (sourceFile.getName().equals(document.name())) {
                // Remove outdated source file and add updated sourceFile
                sourceFiles.remove(sourceFile);
                SourceFile newSourceFile = new SourceFile(document, coveredLines, missedLines, emptyLines);
                this.sourceFiles.add(newSourceFile);
                // Update coverage counts
                this.coveredLines += coveredMissedLineCount;
                this.coveredLines += coveredEmptyLineCount;
                this.missedLines -= coveredMissedLineCount;
                this.missedLines += missedEmptyLineCount;
                setCoveragePercentage();
            }
        }
    }

    /**
     * Replace coverage information for a given source file.
     *
     * @param document               Updated source file
     * @param coveredLines           Latest covered lines
     * @param missedLines            Latest missed lines
     * @param emptyLines             Latest empty lines
     */
    public void replaceCoverage(Document document, List<Integer> coveredLines,
                                List<Integer> missedLines, List<Integer> emptyLines) {
        List<SourceFile> sourceFileList = new ArrayList<>(sourceFiles);
        for (SourceFile sourceFile : sourceFileList) {
            if (sourceFile.getName().equals(document.name())) {
                // Get the covered old covered lines and old missed lines of the previous source file
                int oldCoveredLines = sourceFile.coveredLines.size();
                int oldMissedLines = sourceFile.missedLines.size();

                // Remove outdated source file and add updated sourceFile
                sourceFiles.remove(sourceFile);
                SourceFile newSourceFile = new SourceFile(document, coveredLines, missedLines, emptyLines);
                this.sourceFiles.add(newSourceFile);

                // Remove old covered and missed lines
                this.coveredLines -= oldCoveredLines;
                this.missedLines -= oldMissedLines;

                // Add new covered and missed lines
                this.coveredLines += coveredLines.size();
                this.missedLines += missedLines.size();

                setCoveragePercentage();
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
     * Get the missed lines list for a source file.
     *
     * @param sourceFileName File name of the source file
     * @return List of missed lines
     */
    public Optional<List<Integer>> getMissedLinesList(String sourceFileName) {
        for (SourceFile sourceFile : this.sourceFiles) {
            if (sourceFile.getName().equals(sourceFileName)) {
                return Optional.of(sourceFile.missedLines);
            }
        }
        return Optional.empty();
    }

    /**
     * Get the covered lines list for a source file.
     *
     * @param sourceFileName File name of the source file
     * @return List of covered lines
     */
    public Optional<List<Integer>> getCoveredLinesList(String sourceFileName) {
        for (SourceFile sourceFile : this.sourceFiles) {
            if (sourceFile.getName().equals(sourceFileName)) {
                return Optional.of(sourceFile.coveredLines);
            }
        }
        return Optional.empty();
    }

    /**
     * Get the covered lines list for a source file.
     *
     * @param sourceFileName File name of the source file
     * @return List of empty lines
     */
    public Optional<List<Integer>> getEmptyLinesList(String sourceFileName) {
        for (SourceFile sourceFile : this.sourceFiles) {
            if (sourceFile.getName().equals(sourceFileName)) {
                return Optional.of(sourceFile.emptyLines);
            }
        }
        return Optional.empty();
    }


    /**
     * Inner class for the SourceFile in Json.
     */
    private static class SourceFile {
        private String name;
        private List<Integer> coveredLines;
        private List<Integer> missedLines;
        private List<Integer> emptyLines;
        private float coveragePercentage;
        private String sourceCode;

        private SourceFile(Document document, List<Integer> coveredLines, List<Integer> missedLines) {
            this.name = document.name();
            this.coveredLines = coveredLines;
            this.missedLines = missedLines;
            setCoveragePercentage(coveredLines, missedLines);
            setSourceCode(document);
        }

        private SourceFile(Document document, List<Integer> coveredLines, List<Integer> missedLines,
                           List<Integer> emptyLines) {
            this.name = document.name();
            this.coveredLines = coveredLines;
            this.missedLines = missedLines;
            this.emptyLines = emptyLines;
            setCoveragePercentage(coveredLines, missedLines);
            setSourceCode(document);
        }

        private void setCoveragePercentage(List<Integer> coveredLines, List<Integer> missedLines) {
            float coverageVal = (float) coveredLines.size() / (coveredLines.size() + missedLines.size()) * 100;
            this.coveragePercentage = (float) (Math.round(coverageVal * 100.0) / 100.0);
        }

        private void setSourceCode(Document document) {
            this.sourceCode = document.textDocument().toString();
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
