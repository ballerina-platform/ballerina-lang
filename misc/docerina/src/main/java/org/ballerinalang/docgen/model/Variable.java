/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.docgen.model;

/**
 * Variable which stores the name, type and description.
 */
public class Variable {
    public final String name;

    public final String dataType;

    public final String description;

    public final String href;

    /**
     * Constructor.
     *
     * @param name        variable name.
     * @param dataType    data type of the variable.
     * @param description description of the variable.
     * @param href        link of the data type.
     */
    public Variable(String name, String dataType, String description, String href) {
        this.name = name;
        this.dataType = dataType;
        this.description = description;
        this.href = href;
    }

    @Override
    public String toString() {
        if (name.length() == 0) {
            return dataType;
        }
        if  (dataType.length() == 0) {
            return name;
        }
        return dataType + " " + name;
    }
}
