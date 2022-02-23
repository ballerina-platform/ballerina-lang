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

    protected void addConstant(DataObject dataObject) {
        this.constants.add(dataObject);
    }

    protected void addService(DataObject dataObject) {
        this.services.add(dataObject);
    }

    protected void addRecord(DataObject dataObject) {
        this.records.add(dataObject);
    }

    protected void addObject(DataObject dataObject) {
        this.objects.add(dataObject);
    }

    protected void addClass(DataObject dataObject) {
        this.classes.add(dataObject);
    }

    protected void addType(DataObject dataObject) {
        this.types.add(dataObject);
    }

    protected void addDataObject(MapperObject mapperObject) {
        switch (mapperObject.getKey()) {
            case PackageServiceConstants.FUNCTIONS:
                this.addFunction(mapperObject.getDataObject());
                break;
            case PackageServiceConstants.SERVICES:
                this.addService(mapperObject.getDataObject());
                break;
            case PackageServiceConstants.CONSTANTS:
                this.addConstant(mapperObject.getDataObject());
                break;
            case PackageServiceConstants.RECORDS:
                this.addRecord(mapperObject.getDataObject());
                break;
            case PackageServiceConstants.OBJECTS:
                this.addObject(mapperObject.getDataObject());
                break;
            case PackageServiceConstants.CLASSES:
                this.addClass(mapperObject.getDataObject());
                break;
            case PackageServiceConstants.TYPES:
                this.addType(mapperObject.getDataObject());
                break;
            case PackageServiceConstants.ENUMS:
                this.enums.add(mapperObject.getDataObject());
                break;
            case PackageServiceConstants.LISTENERS:
                this.listeners.add(mapperObject.getDataObject());
                break;
            case PackageServiceConstants.MODULE_LEVEL_VARIABLE:
                this.moduleVariables.add(mapperObject.getDataObject());
                break;
            default:
                break;
        }
    }
}
