/*
 *  Copyright (c) 2023, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.projects.internal;

import io.ballerina.projects.BalToolsManifest;
import io.ballerina.projects.TomlDocument;
import io.ballerina.toml.semantic.TomlType;
import io.ballerina.toml.semantic.ast.TomlBooleanValueNode;
import io.ballerina.toml.semantic.ast.TomlKeyValueNode;
import io.ballerina.toml.semantic.ast.TomlTableNode;
import io.ballerina.toml.semantic.ast.TomlValueNode;
import io.ballerina.toml.semantic.ast.TopLevelNode;

import java.util.Optional;

import static io.ballerina.projects.internal.ManifestUtils.getStringFromTomlTableNode;

/**
 * {@code BalToolsManifestBuilder} processes the bal-tools toml file parsed
 * and populate a {@link BalToolsManifest}.
 *
 * @since 2201.6.0
 */
public abstract class BalToolsManifestBuilder {
    private final Optional<TomlDocument> balToolsToml;
    private final BalToolsManifest balToolsManifest;

    public BalToolsManifestBuilder(TomlDocument balToolsToml) {
        this.balToolsToml = Optional.ofNullable(balToolsToml);
        this.balToolsManifest = parseAsBalToolsManifest();
    }

    public Optional<TomlDocument> getBalToolsToml() {
        return balToolsToml;
    }

    public BalToolsManifest getBalToolsManifest() {
        return balToolsManifest;
    }

    abstract BalToolsManifest parseAsBalToolsManifest();

    String getStringValueFromToolNode(TomlTableNode pkgNode, String key) {
        TopLevelNode topLevelNode = pkgNode.entries().get(key);
        if (topLevelNode == null) {
            return null;
        }
        return getStringFromTomlTableNode(topLevelNode);
    }

    boolean getBooleanFromTemplateNode(TomlTableNode tableNode, String key) {
        TopLevelNode topLevelNode = tableNode.entries().get(key);
        if (topLevelNode == null || topLevelNode.kind() == TomlType.NONE) {
            return false;
        }

        if (topLevelNode.kind() == TomlType.KEY_VALUE) {
            TomlKeyValueNode keyValueNode = (TomlKeyValueNode) topLevelNode;
            TomlValueNode value = keyValueNode.value();
            if (value.kind() == TomlType.BOOLEAN) {
                TomlBooleanValueNode tomlBooleanValueNode = (TomlBooleanValueNode) value;
                return tomlBooleanValueNode.getValue();
            }
        }
        return false;
    }

    public BalToolsManifest build() {
        return this.balToolsManifest;
    }
}
