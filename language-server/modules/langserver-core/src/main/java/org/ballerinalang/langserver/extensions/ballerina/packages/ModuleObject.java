/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.extensions.ballerina.packages;

import java.util.ArrayList;
import java.util.List;

/**
 * Object that holds module data.
 */
public class ModuleObject {
    private final List<DataObject> functions = new ArrayList<>();
    private final List<DataObject> services = new ArrayList<>();
    private final List<DataObject> records = new ArrayList<>();
    private final List<DataObject> objects = new ArrayList<>();
    private final List<DataObject> classes = new ArrayList<>();
    private final List<DataObject> types = new ArrayList<>();
    private final List<DataObject> constants = new ArrayList<>();
    private final List<DataObject> enums = new ArrayList<>();
    private final List<DataObject> listeners = new ArrayList<>();
    private final List<DataObject> moduleVariables = new ArrayList<>();
    private final List<DataObject> configurableVariables = new ArrayList<>();
    private final List<DataObject> automations = new ArrayList<>();

    private String name;

    protected void setName(String name) {
        this.name = name;
    }

    protected String getName() {
        return this.name;
    }

    protected void addFunction(DataObject dataObject) {
        this.functions.add(dataObject);
    }

    private void addConstant(DataObject dataObject) {
        this.constants.add(dataObject);
    }

    private void addService(DataObject dataObject) {
        this.services.add(dataObject);
    }

    private void addRecord(DataObject dataObject) {
        this.records.add(dataObject);
    }

    private void addObject(DataObject dataObject) {
        this.objects.add(dataObject);
    }

    private void addClass(DataObject dataObject) {
        this.classes.add(dataObject);
    }

    private void addType(DataObject dataObject) {
        this.types.add(dataObject);
    }

    private void addAutomation(DataObject dataObject) {
        this.automations.add(dataObject);
        this.functions.add(dataObject); // The main function
    }

    private void addModuleVariable(DataObject dataObject) {
        this.moduleVariables.add(dataObject);
    }

    private void addConfigurableVariable(DataObject dataObject) {
        this.configurableVariables.add(dataObject);
        this.moduleVariables.add(dataObject);   // Configurable variable is also a module variable
    }

    private void addListener(DataObject dataObject) {
        this.listeners.add(dataObject);
    }

    private void addEnum(DataObject dataObject) {
        this.enums.add(dataObject);
    }

    protected void addDataObject(MapperObject mapperObject) {
        switch (mapperObject.getKey()) {
            case PackageServiceConstants.FUNCTIONS -> this.addFunction(mapperObject.getDataObject());
            case PackageServiceConstants.SERVICES -> this.addService(mapperObject.getDataObject());
            case PackageServiceConstants.CONSTANTS -> this.addConstant(mapperObject.getDataObject());
            case PackageServiceConstants.RECORDS -> this.addRecord(mapperObject.getDataObject());
            case PackageServiceConstants.OBJECTS -> this.addObject(mapperObject.getDataObject());
            case PackageServiceConstants.CLASSES -> this.addClass(mapperObject.getDataObject());
            case PackageServiceConstants.TYPES -> this.addType(mapperObject.getDataObject());
            case PackageServiceConstants.ENUMS -> this.addEnum(mapperObject.getDataObject());
            case PackageServiceConstants.LISTENERS -> this.addListener(mapperObject.getDataObject());
            case PackageServiceConstants.MODULE_LEVEL_VARIABLE ->
                    this.addModuleVariable(mapperObject.getDataObject());
            case PackageServiceConstants.CONFIGURABLE_VARIABLES ->
                    this.addConfigurableVariable(mapperObject.getDataObject());
            case PackageServiceConstants.AUTOMATIONS -> this.addAutomation(mapperObject.getDataObject());
            default -> {
            }
        }
    }
}
