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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Ballerina Module.
 */
public class Module extends ModuleMetaData {

    @Expose
    public List<ModuleMetaData> relatedModules = new ArrayList<>();

    // constructs
    @Expose
    public List<Record> records = new ArrayList<>();
    @Expose
    public List<BClass> classes = new ArrayList<>();
    @Expose
    public List<BObjectType> objectTypes = new ArrayList<>();
    @Expose
    public List<BObjectType> serviceTypes = new ArrayList<>();
    @Expose
    public List<Client> clients = new ArrayList<>();
    @Expose
    public List<Listener> listeners = new ArrayList<>();
    @Expose
    public List<Function> functions = new ArrayList<>();
    @Expose
    public List<Constant> constants = new ArrayList<>();
    @Expose
    public List<Annotation> annotations = new ArrayList<>();
    @Expose
    public List<Error> errors = new ArrayList<>();
    @Expose
    public List<BType> types = new ArrayList<>();
    @Expose
    public List<BType> unionTypes = new ArrayList<>();
    @Expose
    public List<BType> simpleNameReferenceTypes = new ArrayList<>();
    @Expose
    public List<BType> tupleTypes = new ArrayList<>();
    @Expose
    public List<TableType> tableTypes = new ArrayList<>();
    @Expose
    public List<MapType> mapTypes = new ArrayList<>();
    @Expose
    public List<BType> intersectionTypes = new ArrayList<>();
    @Expose
    public List<BType> typeDescriptorTypes = new ArrayList<>();
    @Expose
    public List<BType> functionTypes = new ArrayList<>();
    @Expose
    public List<BType> streamTypes = new ArrayList<>();
    @Expose
    public List<BType> arrayTypes = new ArrayList<>();
    @Expose
    public List<BType> anyDataTypes = new ArrayList<>();
    @Expose
    public List<BType> anyTypes = new ArrayList<>();
    @Expose
    public List<BType> stringTypes = new ArrayList<>();
    @Expose
    public List<BType> integerTypes = new ArrayList<>();
    @Expose
    public List<BType> decimalTypes = new ArrayList<>();
    @Expose
    public List<BType> xmlTypes = new ArrayList<>();
    @Expose
    public List<Enum> enums = new ArrayList<>();
    @Expose
    public List<DefaultableVariable> variables = new ArrayList<>();
    @Expose
    public List<DefaultableVariable> configurables = new ArrayList<>();
    @Expose
    public List<Path> resources = new ArrayList<>();
}
