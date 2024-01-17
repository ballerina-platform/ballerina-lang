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
import io.ballerina.projects.BalToolsToml;
import io.ballerina.projects.PackageVersion;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.TomlDocument;
import io.ballerina.projects.util.FileUtils;
import io.ballerina.toml.semantic.TomlType;
import io.ballerina.toml.semantic.ast.TomlBooleanValueNode;
import io.ballerina.toml.semantic.ast.TomlKeyValueNode;
import io.ballerina.toml.semantic.ast.TomlTableArrayNode;
import io.ballerina.toml.semantic.ast.TomlTableNode;
import io.ballerina.toml.semantic.ast.TomlValueNode;
import io.ballerina.toml.semantic.ast.TopLevelNode;
import io.ballerina.toml.validator.TomlValidator;
import io.ballerina.toml.validator.schema.Schema;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static io.ballerina.projects.internal.ManifestUtils.getStringFromTomlTableNode;

/**
 * {@code BalToolsManifestBuilder} processes the bal-tools toml file parsed
 * and populate a {@link BalToolsManifest}.
 *
 * @since 2201.6.0
 */
public class BalToolsManifestBuilder {
    private final Optional<TomlDocument> balToolsToml;
    private final BalToolsManifest balToolsManifest;
    private final Map<String, OldTool> oldTools;

    private BalToolsManifestBuilder(TomlDocument balToolsToml) {
        oldTools = new HashMap();
        this.balToolsToml = Optional.ofNullable(balToolsToml);
        this.balToolsManifest = parseAsBalToolsManifest();
    }

    public static BalToolsManifestBuilder from(TomlDocument balToolsToml) {
        return new BalToolsManifestBuilder(balToolsToml);
    }

    public static BalToolsManifestBuilder from(BalToolsToml balToolsToml) {
        return new BalToolsManifestBuilder(balToolsToml.tomlDocument());
    }

    public BalToolsManifest getBalToolsManifest() {
        return balToolsManifest;
    }

    public Map<String, OldTool> getOldTools() {
        return oldTools;
    }

    public BalToolsManifestBuilder removeTool(String id) {
        balToolsManifest.removeTool(id);
        return this;
    }

    private BalToolsManifest parseAsBalToolsManifest() {
        if (balToolsToml.isEmpty() || balToolsToml.get().toml().rootNode().entries().isEmpty()) {
            return BalToolsManifest.from();
        }
        validateBalToolsTomlAgainstSchema();
        Map<String, Map<String, Map<String, BalToolsManifest.Tool>>> tools = getTools();
        return BalToolsManifest.from(tools);
    }

    private void validateBalToolsTomlAgainstSchema() {
        if (balToolsToml.isEmpty()) {
            return;
        }
        TomlValidator balToolsTomlValidator;
        try {
            balToolsTomlValidator = new TomlValidator(
                    Schema.from(FileUtils.readFileAsString("bal-tools-toml-schema.json")));
        } catch (IOException e) {
            throw new ProjectException("Failed to read the bal-tools.toml validator schema file.");
        }
        balToolsTomlValidator.validate(balToolsToml.get().toml());
    }

    private Map<String, Map<String, Map<String, BalToolsManifest.Tool>>> getTools() {
        if (balToolsToml.isEmpty()) {
            return new HashMap<>();
        }

        TomlTableNode tomlTableNode = balToolsToml.get().toml().rootNode();

        if (tomlTableNode.entries().isEmpty()) {
            return new HashMap<>();
        }

        TopLevelNode toolEntries = tomlTableNode.entries().get("tool");
        if (toolEntries == null || toolEntries.kind() == TomlType.NONE) {
            return new HashMap<>();
        }

        Map<String, Map<String, Map<String, BalToolsManifest.Tool>>> tools = new HashMap<>();
        if (toolEntries.kind() == TomlType.TABLE_ARRAY) {
            TomlTableArrayNode toolTableArray = (TomlTableArrayNode) toolEntries;

            for (TomlTableNode toolNode : toolTableArray.children()) {
                String id = getStringValueFromToolNode(toolNode, "id");
                String org = getStringValueFromToolNode(toolNode, "org");
                String name = getStringValueFromToolNode(toolNode, "name");
                String version = getStringValueFromToolNode(toolNode, "version");
                Optional<Boolean> active = getBooleanFromToolNode(toolNode, "active");
                String repository = getStringValueFromToolNode(toolNode, "repository");

                // If id, org or name, one of the value is null, ignore tool record
                if (id == null || org == null || name == null) {
                    continue;
                }

                if (version == null || active.isEmpty()) {
                    oldTools.put(id, new OldTool(id, org, name));
                    continue;
                }

                try {
                    PackageVersion.from(version);
                } catch (ProjectException ignore) {
                    continue;
                }

                if (!tools.containsKey(id)) {
                    tools.put(id, new HashMap<>());
                }
                if (!tools.get(id).containsKey(version)) {
                    tools.get(id).put(version, new HashMap<>());
                }
                tools.get(id).get(version).put(repository, new BalToolsManifest.Tool(id, org, name, version,
                        active.get(), repository));
            }
        }
        return tools;
    }

    private String getStringValueFromToolNode(TomlTableNode pkgNode, String key) {
        TopLevelNode topLevelNode = pkgNode.entries().get(key);
        if (topLevelNode == null) {
            return null;
        }
        return getStringFromTomlTableNode(topLevelNode);
    }

    Optional<Boolean> getBooleanFromToolNode(TomlTableNode tableNode, String key) {
        TopLevelNode topLevelNode = tableNode.entries().get(key);
        if (topLevelNode == null || topLevelNode.kind() == TomlType.NONE) {
            return Optional.empty();
        }

        if (topLevelNode.kind() == TomlType.KEY_VALUE) {
            TomlKeyValueNode keyValueNode = (TomlKeyValueNode) topLevelNode;
            TomlValueNode value = keyValueNode.value();
            if (value.kind() == TomlType.BOOLEAN) {
                TomlBooleanValueNode tomlBooleanValueNode = (TomlBooleanValueNode) value;
                return Optional.ofNullable(tomlBooleanValueNode.getValue());
            }
        }
        return Optional.empty();
    }

    public BalToolsManifest build() {
        return this.balToolsManifest;
    }

    /**
     * Represents a tool saved in an older update 6 and 7 bal-tools.toml.
     * The tool record in the older bal-tools.toml file does not contain the version and active fields.
     * @param id   tool id
     * @param org  tool org
     * @param name tool name
     *
     * @since 2201.8.0
     */
    public record OldTool(String id, String org, String name) {
    }
}
