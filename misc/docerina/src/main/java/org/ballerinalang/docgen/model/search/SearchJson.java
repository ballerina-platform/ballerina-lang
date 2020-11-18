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
    private List<ConstructSearchJson> classes;
    private List<ConstructSearchJson> functions;
    private List<ConstructSearchJson> records;
    private List<ConstructSearchJson> constants;
    private List<ConstructSearchJson> errors;
    private List<ConstructSearchJson> types;
    private List<ConstructSearchJson> clients;
    private List<ConstructSearchJson> listeners;
    private List<ConstructSearchJson> annotations;
    private List<ConstructSearchJson> abstractObjects;


    public SearchJson(List<ModuleSearchJson> modules,
                      List<ConstructSearchJson> classes,
                      List<ConstructSearchJson> functions,
                      List<ConstructSearchJson> records,
                      List<ConstructSearchJson> constants,
                      List<ConstructSearchJson> errors,
                      List<ConstructSearchJson> types,
                      List<ConstructSearchJson> clients,
                      List<ConstructSearchJson> listeners,
                      List<ConstructSearchJson> annotations,
                      List<ConstructSearchJson> abstractObjects) {
        this.modules = modules;
        this.classes = classes;
        this.functions = functions;
        this.records = records;
        this.constants = constants;
        this.errors = errors;
        this.types = types;
        this.clients = clients;
        this.listeners = listeners;
        this.annotations = annotations;
        this.abstractObjects = abstractObjects;
    }

    public SearchJson() {
        this.modules = new ArrayList<>();
        this.classes = new ArrayList<>();
        this.functions = new ArrayList<>();
        this.records = new ArrayList<>();
        this.constants = new ArrayList<>();
        this.errors = new ArrayList<>();
        this.types = new ArrayList<>();
        this.clients = new ArrayList<>();
        this.listeners = new ArrayList<>();
        this.annotations = new ArrayList<>();
        this.abstractObjects = new ArrayList<>();
    }

    public List<ModuleSearchJson> getModules() {
        return modules;
    }

    public void setModules(List<ModuleSearchJson> modules) {
        this.modules = modules;
    }

    public List<ConstructSearchJson> getClasses() {
        return classes;
    }

    public void setClasses(List<ConstructSearchJson> classes) {
        this.classes = classes;
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

    public List<ConstructSearchJson> getClients() {
        return clients;
    }

    public void setClients(List<ConstructSearchJson> clients) {
        this.clients = clients;
    }

    public List<ConstructSearchJson> getListeners() {
        return listeners;
    }

    public void setListeners(List<ConstructSearchJson> listeners) {
        this.listeners = listeners;
    }

    public List<ConstructSearchJson> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(List<ConstructSearchJson> annotations) {
        this.annotations = annotations;
    }

    public List<ConstructSearchJson> getAbstractObjects() {
        return abstractObjects;
    }

    public void setAbstractObjects(List<ConstructSearchJson> abstractObjects) {
        this.abstractObjects = abstractObjects;
    }
}
