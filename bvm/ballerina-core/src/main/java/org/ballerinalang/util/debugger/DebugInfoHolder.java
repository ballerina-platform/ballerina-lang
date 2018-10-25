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
import java.util.ArrayList;
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

    private LineNumberInfoHolder lineNumberInfoHolder;

    DebugInfoHolder(ProgramFile programFile) {
        lineNumberInfoHolder = new LineNumberInfoHolder();
        this.init(programFile);
    }

    private void init(ProgramFile programFile) {
        processPkgInfos(programFile.getPackageInfoEntries());
    }

    private void processPkgInfos(PackageInfo[] pkgInfos) {
        this.lineNumberInfoHolder.processPkgInfo(pkgInfos);
    }

    /**
     * Adds a single debug point.
     *
     * @param breakPointDTO {@link BreakPointDTO}
     * @return True if success, False otherwise
     */
    private boolean addDebugPoint(BreakPointDTO breakPointDTO) {
        if (this.lineNumberInfoHolder.getPackageInfoMap().get(breakPointDTO.getPackagePath()) == null) {
            return false;
        }
        return DebuggerPkgInfo.markDebugPoint(breakPointDTO, this.lineNumberInfoHolder.getPackageInfoMap()
                .get(breakPointDTO.getPackagePath()));
    }

    /**
     * Adds a list of debug points.
     *
     * @param breakPointDTOS a list of {@link BreakPointDTO}
     * @return list of succeed {@link BreakPointDTO}
     */
    List<BreakPointDTO> addDebugPoints(List<BreakPointDTO> breakPointDTOS) {
        List<BreakPointDTO> deployedBreakPoints = new ArrayList<>();
        this.lineNumberInfoHolder.getPackageInfoMap().values().forEach(DebuggerPkgInfo::clearDebugPoints);
        for (BreakPointDTO nodeLocation : breakPointDTOS) {
            if (addDebugPoint(nodeLocation)) {
                deployedBreakPoints.add(nodeLocation);
            }
        }
        return deployedBreakPoints;
    }

    void clearDebugLocations() {
        this.lineNumberInfoHolder.getPackageInfoMap().values().forEach(DebuggerPkgInfo::clearDebugPoints);
    }

    LineNumberInfo getLineNumber(String packagePath, int ip) {
        return this.lineNumberInfoHolder.getPackageInfoMap().get(packagePath).getLineNumberInfo(ip);
    }

    public static class DebuggerPkgInfo {

        public static boolean markDebugPoint(BreakPointDTO breakPointDTO, PackageLineNumberInfo packageLineNumberInfo) {
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
            LineNumberInfo lineNumberInfo = packageLineNumberInfo.getLineNumbers().get(fileNameAndNo);
            if (lineNumberInfo == null) {
                return false;
            }
            lineNumberInfo.setDebugPoint(true);
            return true;
        }

        public static void clearDebugPoints(PackageLineNumberInfo packageLineNumberInfo) {
            packageLineNumberInfo.getLineNumbers().values().forEach(l -> l.setDebugPoint(false));
        }
    }
}
