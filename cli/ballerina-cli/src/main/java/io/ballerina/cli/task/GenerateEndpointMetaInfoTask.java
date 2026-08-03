/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.cli.task;

import com.google.gson.Gson;
import io.ballerina.projects.Project;
import io.ballerina.projects.plugins.EndpointMetaInfo;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

import static io.ballerina.cli.launcher.LauncherUtils.createLauncherException;

/**
 * Generates endpoint metadata emitted by protocol compiler plugins.
 *
 * @since 2201.13.6
 */
public class GenerateEndpointMetaInfoTask implements Task {

    private static final String ARTIFACT_DIR = "artifact";
    private static final String ENDPOINTS_FILE = "endpoints.yaml";
    private static final String ENDPOINTS_KEY = "endpoints";
    private static final String NAME_KEY = "name";
    private static final String PORT_KEY = "port";
    private static final String BASE_PATH_KEY = "basePath";
    private static final String TYPE_KEY = "type";
    private static final String SCHEMA_PATH_KEY = "schemaPath";
    private static final Gson GSON = new Gson();

    @Override
    public void execute(Project project) {
        try {
            writeEndpointMetadata(project.targetDir().resolve(ARTIFACT_DIR), project.endpointMetadata());
        } catch (IOException e) {
            throw createLauncherException("unable to export endpoint metadata: " + e.getMessage());
        }
    }

    static void writeEndpointMetadata(Path artifactDir, List<EndpointMetaInfo> endpointMetadata) throws IOException {
        if (endpointMetadata.isEmpty()) {
            return;
        }
        Files.createDirectories(artifactDir);
        Files.writeString(artifactDir.resolve(ENDPOINTS_FILE), toYaml(endpointMetadata), StandardCharsets.UTF_8);
    }

    private static String toYaml(List<EndpointMetaInfo> endpoints) {
        StringBuilder content = new StringBuilder();
        content.append(ENDPOINTS_KEY).append(':').append('\n');
        for (EndpointMetaInfo endpoint : sorted(endpoints)) {
            content.append("  - ").append(NAME_KEY).append(": ").append(quote(endpoint.name()))
                    .append('\n');
            content.append("    ").append(PORT_KEY).append(": ").append(endpoint.port()).append('\n');
            content.append("    ").append(BASE_PATH_KEY).append(": ").append(quote(endpoint.basePath()))
                    .append('\n');
            content.append("    ").append(TYPE_KEY).append(": ").append(quote(endpoint.type()))
                    .append('\n');
            content.append("    ").append(SCHEMA_PATH_KEY).append(": ").append(quote(endpoint.schemaPath()))
                    .append('\n');
        }
        return content.toString();
    }

    private static List<EndpointMetaInfo> sorted(List<EndpointMetaInfo> endpoints) {
        return endpoints.stream()
                .sorted(Comparator.comparing(EndpointMetaInfo::name)
                        .thenComparing(EndpointMetaInfo::type)
                        .thenComparing(EndpointMetaInfo::basePath)
                        .thenComparingInt(EndpointMetaInfo::port)
                        .thenComparing(EndpointMetaInfo::schemaPath))
                .toList();
    }

    private static String quote(String value) {
        return GSON.toJson(value);
    }
}
