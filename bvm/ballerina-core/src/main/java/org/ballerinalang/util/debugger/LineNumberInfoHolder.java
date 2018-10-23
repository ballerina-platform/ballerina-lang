package org.ballerinalang.util.debugger;

import org.ballerinalang.util.codegen.LineNumberInfo;

import java.io.File;

public class LineNumberInfoHolder {

    private LineNumberInfo[] ipLineNos;

    public LineNumberInfoHolder(int instructionCount) {
        this.ipLineNos = new LineNumberInfo[instructionCount];
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

}
