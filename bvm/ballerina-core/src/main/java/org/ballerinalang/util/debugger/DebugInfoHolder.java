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
import org.ballerinalang.util.debugger.dto.BreakPointDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link DebugInfoHolder} holds information relevant to debug points for program file.
 *
 * @since 0.88
 */
public class DebugInfoHolder {

    private ProjectLineNumberInfoHolder projectLineNumberInfoHolder;

    DebugInfoHolder(ProgramFile programFile) {
        projectLineNumberInfoHolder = new ProjectLineNumberInfoHolder();
        this.init(programFile);
    }

    private void init(ProgramFile programFile) {
        processPkgInfos(programFile.getPackageInfoEntries());
    }

    private void processPkgInfos(PackageInfo[] pkgInfos) {
        this.projectLineNumberInfoHolder.processPkgInfo(pkgInfos);
    }

    /**
     * Adds a single debug point.
     *
     * @param breakPointDTO {@link BreakPointDTO}
     * @return True if success, False otherwise
     */
    private boolean addDebugPoint(BreakPointDTO breakPointDTO) {
        if (this.projectLineNumberInfoHolder.getPackageInfoMap().get(breakPointDTO.getPackagePath()) == null) {
            return false;
        }
        return DebuggerPkgInfo.markDebugPoint(breakPointDTO, this.projectLineNumberInfoHolder.getPackageInfoMap()
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
        this.projectLineNumberInfoHolder.getPackageInfoMap().values().forEach(DebuggerPkgInfo::clearDebugPoints);
        for (BreakPointDTO nodeLocation : breakPointDTOS) {
            if (addDebugPoint(nodeLocation)) {
                deployedBreakPoints.add(nodeLocation);
            }
        }
        return deployedBreakPoints;
    }

    void clearDebugLocations() {

        this.projectLineNumberInfoHolder.getPackageInfoMap().values().forEach(DebuggerPkgInfo::clearDebugPoints);
    }

    LineNumberInfo getLineNumber(String packagePath, int ip) {
        return this.projectLineNumberInfoHolder.getPackageInfoMap().get(packagePath).getLineNumberInfo(ip);
    }

    /**
     * Module line number info processor and line number debug info processor.
     */
    public static class DebuggerPkgInfo {

        public static boolean markDebugPoint(BreakPointDTO breakPointDTO, ModuleLineNumberInfo moduleLineNumberInfo) {
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
            LineNumberInfo lineNumberInfo = moduleLineNumberInfo.getLineNumbers().get(fileNameAndNo);
            if (lineNumberInfo == null) {
                return false;
            }
            lineNumberInfo.setDebugPoint(true);
            return true;
        }

        public static void clearDebugPoints(ModuleLineNumberInfo moduleLineNumberInfo) {

            moduleLineNumberInfo.getLineNumbers().values().forEach(l -> l.setDebugPoint(false));
        }
    }
}
