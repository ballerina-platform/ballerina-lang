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

import org.ballerinalang.langserver.commons.toml.common.TomlSyntaxTreeUtil;

/**
 * Represents Toml KeyValuePair node in {@link TomlSchemaVisitor}.
 *
 * @since 2.0.0
 */
public class KeyValuePair implements TomlNode {

    private String key;
    private String defaultValue;
    private int id;
    private ValueType type;

    public KeyValuePair(String key, String defaultValue, int id, ValueType type) {
        this.key = key;
        this.defaultValue = defaultValue;
        this.id = id;
        this.type = type;
    }

    public KeyValuePair(String key, ValueType type) {
        this.key = key;
        this.type = type;
        this.id = 1;
        this.defaultValue =
                TomlSyntaxTreeUtil.getDefaultValueForType(type);
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public TomlNodeType type() {
        return TomlNodeType.KEY_VALUE;
    }

    @Override
    public String getTomlSyntax() {
        return key + "=" + type.getStartingSeparator() + "${" + id + ":" + defaultValue + "}" +
                type.getEndingSeparator();
    }
}
