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

import org.ballerinalang.bre.nonblocking.debugger.FrameInfo;
import org.ballerinalang.model.NodeLocation;

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

    private BreakPointDTO location;

    private List<FrameDTO> frames = new ArrayList<FrameDTO>();

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public BreakPointDTO getLocation() {
        return location;
    }

    public void setLocation(NodeLocation location) {
        this.location = new BreakPointDTO(location.getFileName(), location.getLineNumber());
    }

    public void setLocation(BreakPointDTO location) {
        this.location = location;
    }

    public void setFrames(FrameInfo[] frameInfos) {
        for (FrameInfo frame: frameInfos) {
            frames.add(new FrameDTO(frame));
        }
        duplicateMainFix();
    }

    public List<FrameDTO> getFrames() {
        return frames;
    }

    /**
     *  This function will remove the extra main frame returned by the debugger.
     *  Todo : Properly fix this issue in the debugger implementation.
     */
    private void duplicateMainFix() {
        FrameDTO main1 = null;
        FrameDTO main2 = null;
        //check if there are two main frames
        for (FrameDTO frame: this.frames) {
            if ("main".equals(frame.getFrameName())) {
                if (main1 == null) {
                    main1 = frame;
                } else {
                    main2 = frame;
                }
            }
        }
        //remove the one without variables
        if (main1 != null && main2 != null) {
            if (main1.getVariables().isEmpty()) {
                this.frames.remove(main1);
            } else if (main2.getVariables().isEmpty()) {
                this.frames.remove(main2);
            } else {
                //or remove the first one
                this.frames.remove(main1);
            }
        }
    }
}
