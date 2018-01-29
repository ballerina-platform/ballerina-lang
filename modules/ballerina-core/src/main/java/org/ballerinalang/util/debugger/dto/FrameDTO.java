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
 *  DTO class representing Call Stack Frames and their variables.
 */
public class FrameDTO {

    private String packageName, frameName, fileName;
    private int lineID;
    private List<VariableDTO> variables = new ArrayList<>();

    public FrameDTO(String packageName, String frameName, String fileName, int lineID) {
        this.packageName = packageName;
        this.frameName = frameName;
        this.fileName = fileName;
        this.lineID = lineID;
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

    public List<VariableDTO> getVariables() {
        return variables;
    }

    public void addVariable(VariableDTO variableDTO) {
        this.variables.add(variableDTO);
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
}
