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
import java.util.stream.Collectors;

/**
 * Represent documentation for a Listener.
 */
public class Listener extends BClass {

    @Expose
    public List<Function> lifeCycleMethods;

    public Listener(String name, String description, boolean isDeprecated, List<DefaultableVariable> fields,
            List<Function> methods) {
        super(name, description, isDeprecated, fields, methods);
        this.lifeCycleMethods = getLCMethods(methods);
        this.otherMethods = getOtherMethods(methods);
    }

    public List<Function> getLCMethods(List<Function> methods) {
        return methods.stream()
                .filter(function -> function.name.equals("__attach")
                    || function.name.equals("__start")
                    || function.name.equals("__stop"))
                .collect(Collectors.toList());
    }

    public List<Function> getOtherMethods(List<Function> methods) {
        return super.getOtherMethods(methods).stream()
                .filter(function -> !(function.name.equals("__attach")
                        || function.name.equals("__start")
                        || function.name.equals("__stop")))
                .collect(Collectors.toList());
    }
}
