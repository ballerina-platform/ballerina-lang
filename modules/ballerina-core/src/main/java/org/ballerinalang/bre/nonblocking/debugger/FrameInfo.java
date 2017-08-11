/*
*   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*/
package org.ballerinalang.bre.nonblocking.debugger;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link FrameInfo} contains debug information about a StackFrame.
 */
public class FrameInfo {

    private final String packageName, frameName, fileName;
    private final int lineID;
    private List<VariableInfo> variableInfos;


    public FrameInfo(String packageName, String frameName, String fileName, int lineID) {
        variableInfos = new ArrayList<>();
        this.packageName = packageName;
        this.frameName = frameName;
        this.fileName = fileName;
        this.lineID = lineID;
    }

    /**
     * Generates textual representation of the frame.
     *
     * @return textual representation.
     */
    @Override
    public String toString() {
        return frameName + "(" + packageName + ") " + fileName + ":" + lineID;
    }

    public void addVariableInfo(VariableInfo variableInfo) {
        this.variableInfos.add(variableInfo);
    }

    public VariableInfo[] getVariableInfo() {
        return variableInfos.toArray(new VariableInfo[variableInfos.size()]);
    }

    public String getPackageName() {
        return packageName;
    }

    public String getFrameName() {
        return frameName;
    }

    public String getFileName() {
        return fileName;
    }

    public int getLineID() {
        return lineID;
    }
}
