/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.util.debugger;

import org.ballerinalang.util.codegen.LineNumberInfo;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.attributes.AttributeInfo;
import org.ballerinalang.util.codegen.attributes.LineNumberTableAttributeInfo;
import org.ballerinalang.util.debugger.dto.BreakPointDTO;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * {@link DebugInfoHolder} holds information relevant to debug points for program file.
 *
 * @since 0.88
 */
class DebugInfoHolder {

    private Map<String, DebuggerPkgInfo> packageInfoMap = new HashMap<>();

    DebugInfoHolder(ProgramFile programFile) {
        this.init(programFile);
    }

    private void init(ProgramFile programFile) {
        processPkgInfos(programFile.getPackageInfoEntries());
    }

    private void processPkgInfos(PackageInfo[] pkgInfos) {
        Arrays.stream(pkgInfos).forEach(this::processPkgInfo);
    }

    /**
     * Process and build information required for debugging the package.
     *
     * @param packageInfo   To extract relevant information.
     */
    private void processPkgInfo(PackageInfo packageInfo) {
        DebuggerPkgInfo debuggerPkgInfo = new DebuggerPkgInfo(packageInfo.getInstructionCount());

        LineNumberTableAttributeInfo lineNumberTableAttributeInfo = (LineNumberTableAttributeInfo) packageInfo
                .getAttributeInfo(AttributeInfo.Kind.LINE_NUMBER_TABLE_ATTRIBUTE);

        List<LineNumberInfo> lineNumberInfos = lineNumberTableAttributeInfo.getLineNumberInfoList().stream().sorted(
                Comparator.comparing(LineNumberInfo::getIp)).collect(Collectors.toList());

        LineNumberInfo currentLineNoInfo = null;
        for (LineNumberInfo lineNoInfo : lineNumberInfos) {
            if (currentLineNoInfo == null) {
                currentLineNoInfo = lineNoInfo;
                continue;
            }
            debuggerPkgInfo.addLineNumberInfo(currentLineNoInfo.getIp(), lineNoInfo.getIp(), currentLineNoInfo);
            currentLineNoInfo = lineNoInfo;
        }
        if (currentLineNoInfo != null) {
            debuggerPkgInfo.addLineNumberInfo(currentLineNoInfo.getIp(),
                    packageInfo.getInstructionCount(), currentLineNoInfo);
        }
        packageInfoMap.put(packageInfo.getPkgPath(), debuggerPkgInfo);
    }

    private void addDebugPoint(BreakPointDTO breakPointDTO) {
        if (packageInfoMap.get(breakPointDTO.getPackagePath()) == null) {
            return;
        }
        packageInfoMap.get(breakPointDTO.getPackagePath()).markDebugPoint(breakPointDTO);
    }

    void addDebugPoints(List<BreakPointDTO> breakPointDTOS) {
        packageInfoMap.values().forEach(DebuggerPkgInfo::clearDebugPoints);
        for (BreakPointDTO nodeLocation : breakPointDTOS) {
            addDebugPoint(nodeLocation);
        }
    }

    void clearDebugLocations() {
        packageInfoMap.values().forEach(DebuggerPkgInfo::clearDebugPoints);
    }

    LineNumberInfo getLineNumber(String packagePath, int ip) {
        return packageInfoMap.get(packagePath).getLineNumberInfo(ip);
    }

    static class DebuggerPkgInfo {
        LineNumberInfo[] ipLineNos;
        //key - fileName:ln, value - LineNumberInfo
        Map<String, LineNumberInfo> lineNumbers = new HashMap<>();

        DebuggerPkgInfo(int instructionCount) {
            this.ipLineNos = new LineNumberInfo[instructionCount];
        }

        void addLineNumberInfo(int beginIp, int endIp, LineNumberInfo lineNumberInfo) {
            for (int i = beginIp; i < endIp; i++) {
                ipLineNos[i] = lineNumberInfo;
            }
            lineNumberInfo.setEndIp(endIp);
            String fileName = lineNumberInfo.getFileName();
            if (fileName.contains(File.separator)) {
                String[] pathArray = fileName.split(File.separatorChar == '\\' ? "\\\\" : File.separator);
                fileName = pathArray[pathArray.length - 1];
            }
            String fileNameAndNo = fileName + ":" + lineNumberInfo.getLineNumber();
            lineNumbers.put(fileNameAndNo, lineNumberInfo);
        }

        void markDebugPoint(BreakPointDTO breakPointDTO) {
            // TODO: Need to improve/change this logic.
            String fileName = breakPointDTO.getFileName();
            if (fileName.contains("/")) {
                String[] pathArray = fileName.split("/");
                fileName = pathArray[pathArray.length - 1];
            } else if (fileName.contains("\\")) {
                String[] pathArray = fileName.split("\\\\");
                fileName = pathArray[pathArray.length - 1];
            }
            String fileNameAndNo = fileName + ":" + breakPointDTO.getLineNumber();
            LineNumberInfo lineNumberInfo = lineNumbers.get(fileNameAndNo);
            if (lineNumberInfo == null) {
                return;
            }
            lineNumberInfo.setDebugPoint(true);
        }

        void clearDebugPoints() {
            lineNumbers.values().forEach(l -> l.setDebugPoint(false));
        }

        LineNumberInfo getLineNumberInfo(int ip) {
            return ipLineNos[ip];
        }
    }
}
