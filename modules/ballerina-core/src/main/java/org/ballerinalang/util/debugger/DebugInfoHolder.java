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
public class DebugInfoHolder {

    private Map<String, DebuggerPkgInfo> packageInfoMap = new HashMap<>();

    public void init(ProgramFile programFile) {
        processPkgInfos(programFile.getPackageInfoEntries());
    }

    private void processPkgInfos(PackageInfo[] pkgInfos) {
        Arrays.stream(pkgInfos).forEach(p -> processPkgInfo(p));
    }

    /**
     * Process and build information required for debugging the package.
     *
     * @param packageInfo   To extract relevant information.
     */
    public void processPkgInfo(PackageInfo packageInfo) {
        DebuggerPkgInfo debuggerPkgInfo = new DebuggerPkgInfo();

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
        debuggerPkgInfo.addLineNumberInfo(currentLineNoInfo.getIp(),
                packageInfo.getInstructionCount(), currentLineNoInfo);
        packageInfoMap.put(packageInfo.getPkgPath(), debuggerPkgInfo);
    }

    private void addDebugPoint(BreakPointDTO breakPointDTO) {
        if (packageInfoMap.get(breakPointDTO.getPackagePath()) == null) {
            return;
        }
        packageInfoMap.get(breakPointDTO.getPackagePath()).markDebugPoint(breakPointDTO);
    }

    public void addDebugPoints(List<BreakPointDTO> breakPointDTOS) {
        packageInfoMap.values().stream().forEach(p -> p.clearDebugPoints());
        for (BreakPointDTO nodeLocation : breakPointDTOS) {
            addDebugPoint(nodeLocation);
        }
    }

    public void clearDebugLocations() {
        packageInfoMap.values().stream().forEach(p -> p.clearDebugPoints());
    }

    public LineNumberInfo getLineNumber(String packagePath, int ip) {
        return packageInfoMap.get(packagePath).getLineNumberInfo(ip);
    }

    class DebuggerPkgInfo {
        //key - ip, value - ipRange
        Map<Integer, IpRange> ipRangeMap = new HashMap<>();
        //key - ipRange, value linenumber info
        Map<IpRange, LineNumberInfo> rangeLineNoMap = new HashMap<>();
        //key - fileName:ln, value - ipRange
        Map<String, IpRange> lineNumRangeMap = new HashMap<>();

        public void addLineNumberInfo(int beginIp, int endIp, LineNumberInfo lineNumberInfo) {
            IpRange ipRange = new IpRange(beginIp, endIp);
            for (int i = beginIp; i < endIp; i++) {
                ipRangeMap.put(i, ipRange);
            }
            lineNumberInfo.setEndIp(endIp);
            rangeLineNoMap.put(ipRange, lineNumberInfo);
            String fileName = lineNumberInfo.getFileName();
            if (fileName.contains(File.separator)) {
                String[] pathArray = fileName.split(File.separatorChar == '\\' ? "\\\\" : File.separator);
                fileName = pathArray[pathArray.length - 1];
            }
            String fileNameAndNo = fileName + ":" + lineNumberInfo.getLineNumber();
            lineNumRangeMap.put(fileNameAndNo, ipRange);
        }

        public void markDebugPoint(BreakPointDTO breakPointDTO) {
            String fileName = breakPointDTO.getFileName();
            if (fileName.contains("/")) {
                String[] pathArray = fileName.split("/");
                fileName = pathArray[pathArray.length - 1];
            } else if (fileName.contains("\\")) {
                String[] pathArray = fileName.split("\\\\");
                fileName = pathArray[pathArray.length - 1];
            }
            String fileNameAndNo = fileName + ":" + breakPointDTO.getLineNumber();
            IpRange range = lineNumRangeMap.get(fileNameAndNo);
            if (range == null) {
                return;
            }
            LineNumberInfo lineNumberInfo = rangeLineNoMap.get(range);
            if (lineNumberInfo == null) {
                return;
            }
            lineNumberInfo.setDebugPoint(true);
        }

        public void clearDebugPoints() {
            rangeLineNoMap.values().stream().forEach(l -> l.setDebugPoint(false));
        }

        public LineNumberInfo getLineNumberInfo(int ip) {
            return rangeLineNoMap.get(ipRangeMap.get(ip));
        }

    }

    class IpRange {
        int fromIp;
        int toIp;
        public IpRange(int fromIp, int toIp) {
            this.fromIp = fromIp;
            this.toIp = toIp;
        }

    }
}
