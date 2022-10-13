/*
 *  Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.ballerina.componentmodel.entitymodel.components;

import java.util.List;

/**
 * Represents field of a record.
 *
 * @since 2201.2.2
 */
public class Attribute {

    private final String name;
    private final String type;
    private final boolean optional;
    private final boolean nillable;
    private final String defaultValue;
    private final List<Association> associations; // can have multiple association when union is found

    public Attribute(String name, String type, boolean optional, boolean nillable, String defaultValue,
                     List<Association> associations) {

        this.name = name;
        this.type = type;
        this.optional = optional;
        this.nillable = nillable;
        this.defaultValue = defaultValue;
        this.associations = associations;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public boolean isOptional() {
        return optional;
    }

    public boolean isNillable() {
        return nillable;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public List<Association> getAssociations() {
        return associations;
    }

}
