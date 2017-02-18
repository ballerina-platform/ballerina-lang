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
package org.ballerinalang.bre.nonblocking.debugger;

import org.ballerinalang.model.NodeLocation;

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
