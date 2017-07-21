/*
*   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.ballerinalang.util.codegen;

import org.ballerinalang.model.NodeLocation;
import org.ballerinalang.util.codegen.cpentries.UTF8CPEntry;

import java.util.Objects;

/**
 * {@code LineNumberInfo} contains metadata of a Line number in the Ballerina program file.
 *
 * LineNumber   StartInst   FileName
 *
 * @since 0.87
 */
public class LineNumberInfo {

    private int lineNumber = -1;

    // Index to the UTF8CPEntry which is name of the the Ballerina file.
    private int fileIndex = -1;
    private String fileName;    // A cached value

    // First Instruction pointer of the line.
    private int ip = -1;

    // Cache values.
    private PackageInfo packageInfo;

    public LineNumberInfo(int lineNumber, int fileIndex, int ip) {
        this.lineNumber = lineNumber;
        this.fileIndex = fileIndex;
        this.ip = ip;
    }

    // CP Indexes.

    public int getFileIndex() {
        return fileIndex;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getFileName() {
        if (fileName == null) {
            fileName = ((UTF8CPEntry) packageInfo.getConstPool()[fileIndex]).getValue();
        }
        return fileName;
    }

    public PackageInfo getPackageInfo() {
        return packageInfo;
    }

    public LineNumberInfo setPackageInfo(PackageInfo packageInfo) {
        this.packageInfo = packageInfo;
        return this;
    }

    public int getIp() {
        return ip;
    }

    public void setIp(int ip) {
        this.ip = ip;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LineNumberInfo)) {
            return false;
        }

        LineNumberInfo that = (LineNumberInfo) o;

        return lineNumber == that.getLineNumber() && fileIndex == that.getFileIndex() &&
                packageInfo == that.getPackageInfo();
    }

    @Override
    public int hashCode() {
        return Objects.hash(lineNumber, fileIndex, packageInfo);
    }

    /**
     * Utility Class for creating LineNumber from AST.
     */
    public static class Factory {

        /**
         * Create LineNumberInfo instance from given NodeLocation.
         *
         * @param nodeLocation nodeLocation instance to be converted
         * @param packageInfo  package where {@link LineNumberInfo} belongs to
         * @param ip           start instruction pointer
         * @return LineNumberInfo instance.
         */
        public static LineNumberInfo create(NodeLocation nodeLocation, PackageInfo packageInfo, int ip) {
            if (nodeLocation == null) {
                return null;
            }
            UTF8CPEntry fileNameUTF8CPEntry = new UTF8CPEntry(nodeLocation.getFileName());
            int fileNameCPEntryIndex = packageInfo.addCPEntry(fileNameUTF8CPEntry);

            LineNumberInfo lineNumberInfo = new LineNumberInfo(nodeLocation.getLineNumber(), fileNameCPEntryIndex, ip);
            lineNumberInfo.setPackageInfo(packageInfo);
            lineNumberInfo.setIp(ip);
            return lineNumberInfo;
        }

        /**
         * Get LineNumberInfo instance for given NodeLocation and ProgramFile.
         *
         * @param nodeLocation nodeLocation instance to be converted.
         * @param packageInfo  packageInfo instance where LineNumberInfo should be created.
         * @return LineNumberInfo instance.
         */
        public static LineNumberInfo get(NodeLocation nodeLocation, PackageInfo packageInfo) {
            if (nodeLocation == null) {
                return null;
            }
            UTF8CPEntry fileNameUTF8CPEntry = new UTF8CPEntry(nodeLocation.getFileName());
            int fileNameCPEntryIndex = packageInfo.getCPEntryIndex(fileNameUTF8CPEntry);
            if (fileNameCPEntryIndex < 0) {
                return null;
            }
            return packageInfo.getLineNumberInfo(new LineNumberInfo(nodeLocation.getLineNumber(),
                    fileNameCPEntryIndex, -1).setPackageInfo(packageInfo));
        }
    }

    @Override
    public String toString() {
        return "\t" + getFileName() + ":" + lineNumber + "\t\t" + ip;
    }
}
