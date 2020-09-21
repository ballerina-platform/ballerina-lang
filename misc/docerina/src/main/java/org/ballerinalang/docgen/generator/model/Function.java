/*
 * Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.docgen.generator.model;

import com.google.gson.annotations.Expose;

import java.util.List;

/**
 * Represent documentation for a Function.
 */
public class Function extends Construct {
    @Expose
    public boolean isIsolated;
    @Expose
    public boolean isRemote;
    @Expose
    public boolean isExtern;
    @Expose
    public List<DefaultableVariable> parameters;
    @Expose
    public List<Variable> returnParameters;

    public Function(String name, String description, boolean isRemote, boolean isExtern, boolean isDeprecated,
                    boolean isIsolated, List<DefaultableVariable> parameters, List<Variable> returnParameters) {
        super(name, description, isDeprecated);
        this.isRemote = isRemote;
        this.isExtern = isExtern;
        this.parameters = parameters;
        this.returnParameters = returnParameters;
        this.isIsolated = isIsolated;
    }
}
