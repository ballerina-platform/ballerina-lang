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
public class Module {
    @Expose
    public String id;
    @Expose
    public String summary;
    @Expose
    public String description;
    @Expose
    public String orgName;
    @Expose
    public String version;

    // constructs
    @Expose
    public List<Record> records = new ArrayList<>();
    @Expose
    public List<BClass> classes = new ArrayList<>();
    @Expose
    public List<BAbstractObject> abstractObjects = new ArrayList<>();
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
    public List<FiniteType> finiteTypes = new ArrayList<>();
    @Expose
    public List<UnionType> unionTypes = new ArrayList<>();

    @Expose
    public List<Path> resources = new ArrayList<>();

}
