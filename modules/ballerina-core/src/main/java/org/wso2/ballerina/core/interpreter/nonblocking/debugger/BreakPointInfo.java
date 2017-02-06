/*
*   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*/
package org.wso2.ballerina.core.interpreter.nonblocking.debugger;

import org.wso2.ballerina.core.model.NodeLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link BreakPointInfo} represents information about current BreakPoint.
 */
public class BreakPointInfo {

    private final NodeLocation haltLocation;
    private List<FrameInfo> frames;

    public BreakPointInfo(NodeLocation haltLocation) {
        this.haltLocation = haltLocation;
        frames = new ArrayList<>();
    }

    public void addFrameInfo(FrameInfo frameInfo) {
        this.frames.add(frameInfo);
    }

    /**
     * Returns al FrameInfo objects up to current halted location.
     *
     * @return FrameInfo objects up to current halted location.
     */
    public FrameInfo[] getCurrentFrames() {
        return frames.toArray(new FrameInfo[frames.size()]);
    }

    /**
     * Get Current NodeLocation where debugger halt.
     *
     * @return halted location.
     */
    public NodeLocation getHaltLocation() {
        return haltLocation;
    }

    /**
     * Generates textual representation of the current debug point.
     *
     * @return textual representation.
     */
    @Override
    public String toString() {
        StringBuilder br = new StringBuilder();
        br.append("====BreakPointInfo {").append(haltLocation.toString()).append("}====\n");
        br.append("Frames ->\n");
        for (FrameInfo info : frames) {
            br.append("    ").append(info.toString()).append("\n");
        }
        br.append("Last Frame variables->\n");
        if (frames.size() > 0) {
            FrameInfo frameInfo = frames.get(frames.size() - 1);
            for (VariableInfo var : frameInfo.getVariableInfo()) {
                br.append("    ").append(var.toString()).append("\n");
            }
        }
        br.append("====End Break Point Info====");
        return br.toString();
    }
}
