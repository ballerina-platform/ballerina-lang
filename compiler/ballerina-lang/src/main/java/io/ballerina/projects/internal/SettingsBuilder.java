/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import io.ballerina.projects.DiagnosticResult;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.Settings;
import io.ballerina.projects.TomlDocument;
import io.ballerina.projects.internal.model.Central;
import io.ballerina.projects.internal.model.Proxy;
import io.ballerina.projects.internal.model.Repository;
import io.ballerina.projects.util.FileUtils;
import io.ballerina.toml.semantic.TomlType;
import io.ballerina.toml.semantic.ast.TomlKeyValueNode;
import io.ballerina.toml.semantic.ast.TomlLongValueNode;
import io.ballerina.toml.semantic.ast.TomlStringValueNode;
import io.ballerina.toml.semantic.ast.TomlTableArrayNode;
import io.ballerina.toml.semantic.ast.TomlTableNode;
import io.ballerina.toml.semantic.ast.TomlValueNode;
import io.ballerina.toml.semantic.ast.TopLevelNode;
import io.ballerina.toml.validator.TomlValidator;
import io.ballerina.toml.validator.schema.Schema;
import io.ballerina.tools.diagnostics.Diagnostic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * {@code SettingsBuilder} processes the settings toml file parsed and populate a {@link Settings}.
 *
 * @since 0.964
 */
public class SettingsBuilder {

    public static final String NAME = "name";
    public static final String MAVEN = "maven";
    private static final String CONNECT_TIMEOUT = "connectTimeout";
    private static final String READ_TIMEOUT = "readTimeout";
    private static final String WRITE_TIMEOUT = "writeTimeout";
    private static final String CALL_TIMEOUT = "callTimeout";
    private TomlDocument settingsToml;
    private Settings settings;
    private DiagnosticResult diagnostics;
    private List<Diagnostic> diagnosticList;

    private static final String PROXY = "proxy";
    private static final String CENTRAL = "central";
    private static final String HOST = "host";
    private static final String PORT = "port";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String ACCESS_TOKEN = "accesstoken";
    private static final String REPOSITORY = "repository";
    private static final String ID = "id";
    private static final String URL = "url";
    private static final int DEFAULT_CONNECT_TIMEOUT = 60;
    private static final int DEFAULT_READ_TIMEOUT = 60;
    private static final int DEFAULT_WRITE_TIMEOUT = 60;
    private static final int DEFAULT_CALL_TIMEOUT = 0;

    private SettingsBuilder(TomlDocument settingsToml) {
        this.diagnosticList = new ArrayList<>();
        this.settingsToml = settingsToml;
        this.settings = parseAsSettings();
    }

    public static SettingsBuilder from(TomlDocument settingsToml) {
        return new SettingsBuilder(settingsToml);
    }

    public Settings settings() {
        return this.settings;
    }

    public DiagnosticResult diagnostics() {
        if (diagnostics != null) {
            return diagnostics;
        }

        // Add toml syntax diagnostics
        this.diagnosticList.addAll(settingsToml.toml().diagnostics());
        diagnostics = new DefaultDiagnosticResult(this.diagnosticList);
        return diagnostics;
    }

    private Settings parseAsSettings() {
        TomlValidator settingsTomlValidator;
        try {
            settingsTomlValidator = new TomlValidator(
                    Schema.from(FileUtils.readFileAsString("settings-toml-schema.json")));
        } catch (IOException e) {
            throw new ProjectException("Failed to read the Settings.toml validator schema file.");
        }

        // Validate settingsToml using ballerina toml schema
        settingsTomlValidator.validate(settingsToml.toml());

        TomlTableNode tomlAstNode = settingsToml.toml().rootNode();

        String host = "";
        int port = 0;
        String proxyUsername = "";
        String proxyPassword = "";
        String accessToken = "";
        int connectTimeout = DEFAULT_CONNECT_TIMEOUT;
        int readTimeout = DEFAULT_READ_TIMEOUT;
        int writeTimeout = DEFAULT_WRITE_TIMEOUT;
        int callTimeout = DEFAULT_CALL_TIMEOUT;
        String url = "";
        String id = "";
        String repoName = "";
        String repositoryUsername = "";
        String repositoryPassword = "";
        ArrayList<Repository> repositories = new ArrayList<>();

        if (!tomlAstNode.entries().isEmpty()) {
            TomlTableNode proxyNode = (TomlTableNode) tomlAstNode.entries().get(PROXY);
            if (proxyNode != null && proxyNode.kind() != TomlType.NONE && proxyNode.kind() == TomlType.TABLE) {
                host = getStringOrDefaultFromTomlTableNode(proxyNode, HOST, "");
                port = getIntValueFromProxyNode(proxyNode, PORT, 0);
                proxyUsername = getStringOrDefaultFromTomlTableNode(proxyNode, USERNAME, "");
                proxyPassword = getStringOrDefaultFromTomlTableNode(proxyNode, PASSWORD, "");
            }

            TomlTableNode centralNode = (TomlTableNode) tomlAstNode.entries().get(CENTRAL);
            if (centralNode != null && centralNode.kind() != TomlType.NONE && centralNode.kind() == TomlType.TABLE) {
                accessToken = getStringOrDefaultFromTomlTableNode(centralNode, ACCESS_TOKEN, "");
                connectTimeout = getIntValueFromProxyNode(centralNode, CONNECT_TIMEOUT, DEFAULT_CONNECT_TIMEOUT);
                readTimeout = getIntValueFromProxyNode(centralNode, READ_TIMEOUT, DEFAULT_READ_TIMEOUT);
                writeTimeout = getIntValueFromProxyNode(centralNode, WRITE_TIMEOUT, DEFAULT_WRITE_TIMEOUT);
                callTimeout = getIntValueFromProxyNode(centralNode, CALL_TIMEOUT, DEFAULT_CALL_TIMEOUT);
            }

            TomlTableNode repository = (TomlTableNode) tomlAstNode.entries().get(REPOSITORY);
            if (repository != null && repository.kind() != TomlType.NONE && repository.kind() == TomlType.TABLE) {
                Map<String, TopLevelNode> repoEntries = repository.entries();
                for (Map.Entry<String, TopLevelNode> entry : repoEntries.entrySet()) {
                    String repoKey = entry.getKey();
                    TopLevelNode repoValue = entry.getValue();
                    if (!MAVEN.equals(repoKey)) {
                        continue;
                    }
                    List<TomlTableNode> repositoryNodes = ((TomlTableArrayNode) (repoValue)).children();
                    for (TomlTableNode repositoryNode : repositoryNodes) {
                        url = getStringOrDefaultFromTomlTableNode(repositoryNode, URL, "");
                        id = getStringOrDefaultFromTomlTableNode(repositoryNode, ID, "");
                        repositoryUsername = getStringOrDefaultFromTomlTableNode(repositoryNode, USERNAME, "");
                        repositoryPassword = getStringOrDefaultFromTomlTableNode(repositoryNode, ACCESS_TOKEN, "");
                        repositories.add(Repository.from(id, url, repositoryUsername, repositoryPassword));
                    }
                }
            }
        }

        return Settings.from(Proxy.from(host, port, proxyUsername, proxyPassword), Central.from(accessToken,
                connectTimeout, readTimeout, writeTimeout, callTimeout), diagnostics(),
                repositories.toArray(new Repository[0]));
    }

    private String getStringOrDefaultFromTomlTableNode(TomlTableNode pkgNode, String key, String defaultValue) {
        TopLevelNode topLevelNode = pkgNode.entries().get(key);
        if (topLevelNode == null || topLevelNode.kind() == TomlType.NONE) {
            // return default value
            return defaultValue;
        }
        String value = getStringFromTomlTableNode(topLevelNode);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    private int getIntValueFromProxyNode(TomlTableNode pkgNode, String key, int defaultValue) {
        TopLevelNode topLevelNode = pkgNode.entries().get(key);
        if (topLevelNode == null || topLevelNode.kind() == TomlType.NONE) {
            // return default value
            return defaultValue;
        }
        int value = getIntFromTomlTableNode(topLevelNode);
        if (value == 0) {
            return defaultValue;
        }
        return value;
    }

    private String getStringFromTomlTableNode(TopLevelNode topLevelNode) {
        if (topLevelNode.kind() == TomlType.KEY_VALUE) {
            TomlKeyValueNode keyValueNode = (TomlKeyValueNode) topLevelNode;
            TomlValueNode value = keyValueNode.value();
            if (value.kind() == TomlType.STRING) {
                TomlStringValueNode stringValueNode = (TomlStringValueNode) value;
                return stringValueNode.getValue();
            }
        }
        return null;
    }

    private int getIntFromTomlTableNode(TopLevelNode topLevelNode) {
        if (topLevelNode.kind() == TomlType.KEY_VALUE) {
            TomlKeyValueNode keyValueNode = (TomlKeyValueNode) topLevelNode;
            TomlValueNode value = keyValueNode.value();
            if (value.kind() == TomlType.INTEGER) {
                TomlLongValueNode longValueNode = (TomlLongValueNode) value;
                return Math.toIntExact(longValueNode.getValue());
            }
        }
        return 0;
    }
}
