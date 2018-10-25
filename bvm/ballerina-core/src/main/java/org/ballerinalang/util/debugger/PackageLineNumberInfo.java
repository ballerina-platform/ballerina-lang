package org.ballerinalang.util.debugger;

import org.ballerinalang.util.codegen.LineNumberInfo;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class PackageLineNumberInfo {

    private LineNumberInfo[] ipLineNos;

    //key - fileName:ln, value - LineNumberInfo
    Map<String, LineNumberInfo> lineNumbers = new HashMap<>();

    public PackageLineNumberInfo(int instructionCount) {
        this.ipLineNos = new LineNumberInfo[instructionCount];
    }

    void populateLineNumbers(int beginIp, int endIp, LineNumberInfo lineNumberInfo) {
        addLineNumberInfo(beginIp, endIp, lineNumberInfo);
        String fileName = lineNumberInfo.getFileName();
        if (fileName.contains(File.separator)) {
            String[] pathArray = fileName.split(File.separatorChar == '\\' ? "\\\\" : File.separator);
            fileName = pathArray[pathArray.length - 1];
        }
        String fileNameAndNo = fileName + ":" + lineNumberInfo.getLineNumber();
        lineNumbers.put(fileNameAndNo, lineNumberInfo);
    }

    void addLineNumberInfo(int beginIp, int endIp, LineNumberInfo lineNumberInfo) {
        for (int i = beginIp; i < endIp; i++) {
            ipLineNos[i] = lineNumberInfo;
        }
        lineNumberInfo.setEndIp(endIp);
    }

    public LineNumberInfo getLineNumberInfo(int ip) {
        return ipLineNos[ip];
    }

    public Map<String, LineNumberInfo> getLineNumbers() {
        return lineNumbers;
    }
}
