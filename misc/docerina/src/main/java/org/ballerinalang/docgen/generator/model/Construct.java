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

/**
 * Represents a language construct in a Ballerina module.
 */
public class Construct {
    @Expose
    public String name;
    @Expose
    public String description;
    @Expose
    public List<String> descriptionSections;
    @Expose
    public boolean isDeprecated;
    @Expose
    public Type inclusionType = null;
    @Expose
    public boolean isReadOnly;

    public Construct(String name, String description, boolean isDeprecated) {
        this.name = name;
        this.description = description;
        this.isDeprecated = isDeprecated;
    }

    public Construct(String name, String description, List<String> descriptionSections, boolean isDeprecated) {
        this.name = name;
        this.description = description;
        this.descriptionSections = descriptionSections;
        this.isDeprecated = isDeprecated;
    }

    public Construct(Type inclusionType) {
        this.inclusionType = inclusionType;
    }
}
