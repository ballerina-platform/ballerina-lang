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

import java.nio.file.Path;
import java.util.List;

/**
 * Represents a Ballerina Module.
 */
public class Module {

    public String id;
    public Path moduleMD;
    public String description;

    // constructs
    public List<Record> records;
    public List<Object> objects;
    public List<Client> clients;
    public List<Listener> listeners;
    public List<Function> functions;
    public List<Constant> constants;
    public List<Annotation> annotations;
    public List<Error> errors;
}
