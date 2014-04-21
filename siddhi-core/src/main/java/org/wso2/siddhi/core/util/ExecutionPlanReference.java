/*
 * Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.siddhi.core.util;

import org.wso2.siddhi.core.stream.input.InputHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ExecutionPlanReference {
    private List<InputHandler> inputHandlerList = new ArrayList<InputHandler>();
    private List<String> queryReferenceList = new ArrayList<String>();
    private String executionPlanId;

    public ExecutionPlanReference() {
        this.executionPlanId = UUID.randomUUID().toString();
    }

    public String getExecutionPlanId() {
        return executionPlanId;
    }

    public List<InputHandler> getInputHandlerList() {
        return inputHandlerList;
    }

    public void addInputHandler(InputHandler inputHandler) {
        this.inputHandlerList.add(inputHandler);
    }

    public List<String> getQueryReferenceList() {
        return queryReferenceList;
    }

    public void addQueryReference(String queryReference) {
        this.queryReferenceList.add(queryReference);
    }
}
