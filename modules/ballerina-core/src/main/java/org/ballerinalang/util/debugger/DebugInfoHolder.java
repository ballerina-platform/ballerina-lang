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

import org.ballerinalang.bre.nonblocking.debugger.DebugSessionObserver;
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
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

/**
 * {@link DebugInfoHolder} holds information relevant to current debugging session.
 *
 * @since 0.88
 */
public class DebugInfoHolder {
    private volatile Semaphore executionSem;
    private DebugSessionObserver debugSessionObserver;
    private DebugCommand currentCommand;

    private Map<String, DebuggerPkgInfo> packageInfoMap = new HashMap<>();
    private LineNumberInfo lastLine;
    private int fp;

    public DebugInfoHolder() {
        this.executionSem = new Semaphore(0);
    }

    public void init(ProgramFile programFile) {
        Arrays.stream(programFile.getPackageInfoEntries()).forEach(p -> processPkgInfo(p));
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

        //TODO remove above line if already sorted
//        List<LineNumberInfo> lineNumberInfos = lineNumberTableAttributeInfo.getLineNumberInfoEntries();
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

    public void waitTillDebuggeeResponds() {
        try {
            executionSem.acquire();
        } catch (InterruptedException e) {
            //TODO error handle
        }
    }

    /**
     * Get semaphore queue length.
     *
     * @return  Queue length.
     */
    public int getSemaphorQueueLength() {
        return executionSem.getQueueLength();
    }

    /**
     * Return whether there are queued threads or not.
     *
     * @return  Queued threads exist or not.
     */
    public boolean hasQueuedThreads() {
        return executionSem.hasQueuedThreads();
    }

    public void releaseLock() {
        executionSem.release();
    }

    private void addDebugPoint(BreakPointDTO breakPointDTO) {
        //TODO remove below line later
//        breakPointDTO.setPackagePath(".");
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

    public void setDebugSessionObserver(DebugSessionObserver debugSessionObserver) {
        this.debugSessionObserver = debugSessionObserver;
    }

    public DebugSessionObserver getDebugSessionObserver() {
        return debugSessionObserver;
    }

    public DebugCommand getCurrentCommand() {
        return currentCommand;
    }

    public void setCurrentCommand(DebugCommand currentCommand) {
        this.currentCommand = currentCommand;
    }

    public LineNumberInfo getLastLine() {
        return lastLine;
    }

    public int getFp() {
        return fp;
    }

    public void setFp(int fp) {
        this.fp = fp;
    }

    public void setLastLine(LineNumberInfo lastLine) {
        this.lastLine = lastLine;
    }

    public void resume() {
        currentCommand = DebugCommand.RESUME;
        releaseLock();
    }

    public void stepIn() {
        currentCommand = DebugCommand.STEP_IN;
        releaseLock();
    }

    public void stepOver() {
        currentCommand = DebugCommand.STEP_OVER;
        releaseLock();
    }

    public void stepOut() {
        currentCommand = DebugCommand.STEP_OUT;
        releaseLock();
    }

    /**
     * Debugging steps.
     */
    public enum DebugCommand {
        STEP_IN,
        STEP_OVER,
        STEP_OVER_INTMDT,
        STEP_OUT,
        STEP_OUT_INTMDT,
        RESUME,
        STOP
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
                String[] pathArray = fileName.split(File.separator);
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
