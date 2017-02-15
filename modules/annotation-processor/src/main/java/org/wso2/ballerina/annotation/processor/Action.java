/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.ballerina.annotation.processor;

import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaAction;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaAnnotation;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO to hold Ballerina Action
 */
public class Action {
    
    BallerinaAction action;
    private String actionClassName;
    private List<Annotation> annotations;
    
    public Action(BallerinaAction action, String className) {
        this.action = action;
        this.actionClassName = className;
        annotations = new ArrayList<>();
    }
    
    public String getClassName() {
        return actionClassName;
    }
    
    public BallerinaAction getBalAction() {
        return action;
    }

    public void setAnnotations(BallerinaAnnotation[] annotations) {
        this.annotations = Utils.getAnnotations(annotations);
    }

    public List<Annotation> getAnnotations() {
        return annotations;
    }
}
