/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.diagramutil.connector.models.connector;

import com.google.gson.annotations.Expose;

import java.util.List;
import java.util.Map;

/**
 * Function model.
 */
public class Function {
    @Expose
    public String name;
    @Expose
    public List<Type> parameters;
    @Expose
    public Type returnType;
    @Expose
    public boolean isRemote;
    @Expose
    public String documentation;
    @Expose
    public Map<String, String> displayAnnotation;

    public Function(String name, List<Type> parameters, Type returnType, Map<String, String> displayAnnotation,
                    boolean isRemote, String documentation) {
        this.name = name;
        this.parameters = parameters;
        this.returnType = returnType;
        this.displayAnnotation = displayAnnotation;
        this.isRemote = isRemote;
        this.documentation = documentation;
    }
}
