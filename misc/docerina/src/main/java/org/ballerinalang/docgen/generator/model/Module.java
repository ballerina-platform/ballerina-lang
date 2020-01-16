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

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Ballerina Module.
 */
public class Module {

    public String id;
    public String summary;
    public String description;
    public String orgName;
    public String version;

    // constructs
    public List<Record> records = new ArrayList<>();
    public List<Object> objects = new ArrayList<>();
    public List<Client> clients = new ArrayList<>();
    public List<Listener> listeners = new ArrayList<>();
    public List<Function> functions = new ArrayList<>();
    public List<Constant> constants = new ArrayList<>();
    public List<Annotation> annotations = new ArrayList<>();
    public List<Error> errors = new ArrayList<>();
    public List<FiniteType> finiteTypes = new ArrayList<>();
    public List<UnionType> unionTypes = new ArrayList<>();
}
