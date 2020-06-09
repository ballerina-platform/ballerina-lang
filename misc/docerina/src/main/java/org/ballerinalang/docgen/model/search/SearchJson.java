/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.docgen.model.search;

import java.util.ArrayList;
import java.util.List;

/**
 * JSON to hold search data.
 */
public class SearchJson {
    private List<ModuleSearchJson> modules;
    private List<ConstructSearchJson> objects;
    private List<ConstructSearchJson> functions;
    private List<ConstructSearchJson> records;
    private List<ConstructSearchJson> constants;
    private List<ConstructSearchJson> errors;
    private List<ConstructSearchJson> types;

    public SearchJson(List<ModuleSearchJson> modules,
                      List<ConstructSearchJson> objects,
                      List<ConstructSearchJson> functions,
                      List<ConstructSearchJson> records,
                      List<ConstructSearchJson> constants,
                      List<ConstructSearchJson> errors,
                      List<ConstructSearchJson> types) {
        this.modules = modules;
        this.objects = objects;
        this.functions = functions;
        this.records = records;
        this.constants = constants;
        this.errors = errors;
        this.types = types;
    }

    public SearchJson() {
        this.modules = new ArrayList<>();
        this.objects = new ArrayList<>();
        this.functions = new ArrayList<>();
        this.records = new ArrayList<>();
        this.constants = new ArrayList<>();
        this.errors = new ArrayList<>();
        this.types = new ArrayList<>();
    }

    public List<ModuleSearchJson> getModules() {
        return modules;
    }

    public void setModules(List<ModuleSearchJson> modules) {
        this.modules = modules;
    }

    public List<ConstructSearchJson> getObjects() {
        return objects;
    }

    public void setObjects(List<ConstructSearchJson> objects) {
        this.objects = objects;
    }

    public List<ConstructSearchJson> getFunctions() {
        return functions;
    }

    public void setFunctions(List<ConstructSearchJson> functions) {
        this.functions = functions;
    }

    public List<ConstructSearchJson> getRecords() {
        return records;
    }

    public void setRecords(List<ConstructSearchJson> records) {
        this.records = records;
    }

    public List<ConstructSearchJson> getConstants() {
        return constants;
    }

    public void setConstants(List<ConstructSearchJson> constants) {
        this.constants = constants;
    }

    public List<ConstructSearchJson> getErrors() {
        return errors;
    }

    public void setErrors(List<ConstructSearchJson> errors) {
        this.errors = errors;
    }

    public List<ConstructSearchJson> getTypes() {
        return types;
    }

    public void setTypes(List<ConstructSearchJson> types) {
        this.types = types;
    }
}
