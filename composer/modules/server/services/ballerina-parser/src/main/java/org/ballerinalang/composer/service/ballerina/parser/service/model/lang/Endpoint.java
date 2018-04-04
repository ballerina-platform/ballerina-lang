/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.composer.service.ballerina.parser.service.model.lang;

import java.util.ArrayList;
import java.util.List;

/**
 * Endpoint.
 */
public class Endpoint {
    private String name;
    private String packageName;
    private List<Function> actions;
    private String fileName;
    private List<StructField> fields;

    public Endpoint(String name) {
        this.name = name;
        this.actions = new ArrayList<>();
        this.fields = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }


    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public List<Function> getActions() {

        return actions;
    }

    public void setActions(List<Function> actions) {
        this.actions = actions;
    }

    public void addAction(Function action) {
        this.actions.add(action);
    }


    public List<StructField> getFields() {
        return fields;
    }

    public void setFields(List<StructField> fields) {
        this.fields = fields;
    }

    public void addField(StructField field) {
        this.fields.add(field);
    }
}
