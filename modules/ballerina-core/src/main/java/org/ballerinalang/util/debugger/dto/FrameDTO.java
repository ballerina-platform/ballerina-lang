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
import org.ballerinalang.bre.nonblocking.debugger.VariableInfo;

import java.util.ArrayList;
import java.util.List;

/**
 *  DTO class representing Call Stack Frames and their variables.
 */
public class FrameDTO {

    private String packageName, frameName, fileName;
    private int lineID;
    private List<VariableDTO> variables = new ArrayList<VariableDTO>();

    public FrameDTO(){

    }

    public FrameDTO(String packageName, String frameName, String fileName, int lineID) {
        this.packageName = packageName;
        this.frameName = frameName;
        this.fileName = fileName;
        this.lineID = lineID;
    }

    public FrameDTO(FrameInfo frame) {
        this.packageName = frame.getPackageName();
        this.frameName = frame.getFrameName();
        this.fileName = frame.getFileName();
        this.lineID = frame.getLineID();
        for (VariableInfo vinfo: frame.getVariableInfo()) {
            variables.add(new VariableDTO(vinfo));
        }
    }


    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getFrameName() {
        return frameName;
    }

    public void setFrameName(String frameName) {
        this.frameName = frameName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getLineID() {
        return lineID;
    }

    public void setLineID(int lineID) {
        this.lineID = lineID;
    }

    public List<VariableDTO> getVariables() {
        return variables;
    }
}
