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
package org.wso2.ballerinalang.programfile;

import java.util.Objects;

/**
 * {@code LineNumberInfo} contains metadata of a Line number in the Ballerina program file.
 *
 * LineNumber   StartInst   FileName
 *
 * @since 0.87
 */
@Deprecated
public class LineNumberInfo {

    private int lineNumber = -1;

    // Index to the UTF8CPEntry which is name of the the Ballerina file.
    private int fileNameCPIndex = -1;
    private String fileName;    // A cached value

    // First Instruction pointer of the line.
    private int ip = -1;

    // Below two, does not need to be serialized
    private boolean isDebugPoint = false;
    private int endIp;

    // Cache values.
    private PackageInfo packageInfo;

    public LineNumberInfo(int lineNumber, int fileNameCPIndex, String fileName, int ip) {
        this.lineNumber = lineNumber;
        this.fileNameCPIndex = fileNameCPIndex;
        this.fileName = fileName;
        this.ip = ip;
    }

    // CP Indexes.
    public int getFileNameCPIndex() {
        return fileNameCPIndex;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getFileName() {
        return fileName;
    }

    public PackageInfo getPackageInfo() {
        return packageInfo;
    }

    public void setPackageInfo(PackageInfo packageInfo) {
        this.packageInfo = packageInfo;
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

        return lineNumber == that.getLineNumber() && fileNameCPIndex == that.getFileNameCPIndex() &&
                packageInfo == that.getPackageInfo();
    }

    @Override
    public int hashCode() {
        return Objects.hash(lineNumber, fileNameCPIndex, packageInfo, ip);
    }

    @Override
    public String toString() {
        return "\t" + fileName + ":" + lineNumber + "\t\t" + ip;
    }

    public boolean isDebugPoint() {
        return isDebugPoint;
    }

    public void setDebugPoint(boolean debugPoint) {
        isDebugPoint = debugPoint;
    }

    public void setEndIp(int endIp) {
        this.endIp = endIp;
    }

    public boolean checkIpRangeForInstructionCode(Instruction[] codes, int matchingCode) {
        for (int i = ip; i < endIp; i++) {
            if (codes[i].getOpcode() == matchingCode) {
                return true;
            }
        }
        return false;
    }
}
