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

package org.ballerinalang.util.debugger.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO class representing the messages sent to client from the debugger.
 *
 * @since 0.8.0
 */
public class MessageDTO {

    private String code;

    private String message;

    //TODO change to workerId
    private String threadId;

    private BreakPointDTO location;

    private List<FrameDTO> frames = new ArrayList<>();

    public MessageDTO(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getThreadId() {
        return threadId;
    }

    public void setThreadId(String threadId) {
        this.threadId = threadId;
    }

    public BreakPointDTO getLocation() {
        return location;
    }

    public void setLocation(BreakPointDTO location) {
        this.location = location;
    }

    public void addFrame(FrameDTO frameDTO) {
        frames.add(frameDTO);
    }

    public List<FrameDTO> getFrames() {
        return frames;
    }

    /**
     * Generates textual representation of the current debug point.
     *
     * @return textual representation.
     */
    @Override
    public String toString() {
        StringBuilder br = new StringBuilder();
        if(location != null) {
            br.append("====BreakPointInfo {").append(location.toString()).append("}====\n");
        }
        br.append("Frames ->\n");
        for (FrameDTO frame : frames) {
            br.append("    ").append(frame.toString()).append("\n");
        }
        br.append("Last Frame variables->\n");
        if (frames.size() > 0) {
            FrameDTO frame = frames.get(frames.size() - 1);
            for (VariableDTO var : frame.getVariables()) {
                br.append("    ").append(var.toString()).append("\n");
            }
        }
        br.append("====End Break Point Info====");
        return br.toString();
    }
}
