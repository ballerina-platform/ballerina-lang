/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.commons.toml.visitor;

/**
 * Represents Toml TableArray in {@link TomlSchemaVisitor}.
 *
 * @since 2.0.0
 */
public class TableArray implements TomlNode {

    private String name;

    public TableArray(String name) {
        this.name = name;
    }

    @Override
    public String getTomlSyntax() {
        StringBuilder tomlSyntax = new StringBuilder();
        tomlSyntax.append("[[").append(name).append("]]");
        return tomlSyntax.toString();
    }

    @Override
    public String getKey() {
        return this.name;
    }

    @Override
    public TomlNodeType type() {
        return TomlNodeType.TABLE_ARRAY;
    }

    @Override
    public String toString() {
        return this.getKey();
    }
}
