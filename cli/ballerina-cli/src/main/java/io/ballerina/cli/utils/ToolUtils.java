/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.cli.utils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.ballerina.projects.BalToolsManifest;
import io.ballerina.projects.BalToolsToml;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.internal.BalToolsManifestBuilder;
import io.ballerina.projects.util.ProjectConstants;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

import static io.ballerina.projects.util.BalToolsUtil.BAL_TOOLS_TOML_PATH;
import static io.ballerina.projects.util.BalToolsUtil.isCompatibleWithPlatform;
import static io.ballerina.projects.util.ProjectConstants.BALA_DIR_NAME;
import static io.ballerina.projects.util.ProjectConstants.CENTRAL_REPOSITORY_CACHE_NAME;
import static io.ballerina.projects.util.ProjectConstants.LOCAL_REPOSITORY_NAME;
import static io.ballerina.projects.util.ProjectConstants.REPOSITORIES_DIR;

public class ToolUtils {

    private ToolUtils() {

    }

    public static Optional<BalToolsManifest.Tool> getToolFromLocalRepo(String toolId, String version) {
        JsonObject localToolJson;
        Gson gson = new Gson();
        Path localBalaPath = RepoUtils.createAndGetHomeReposPath().resolve(Path.of(REPOSITORIES_DIR,
                LOCAL_REPOSITORY_NAME, BALA_DIR_NAME));
        Path localToolJsonPath = localBalaPath.resolve(ProjectConstants.LOCAL_TOOLS_JSON);
        if (!Files.exists(localToolJsonPath)) {
            return Optional.empty();
        }
        try (BufferedReader bufferedReader = Files.newBufferedReader(localToolJsonPath, StandardCharsets.UTF_8)) {
            localToolJson = gson.fromJson(bufferedReader, JsonObject.class);
            JsonElement localTool = localToolJson.get(toolId);
            if (localTool == null) {
                return Optional.empty();
            }
            JsonObject pkgDesc = localTool.getAsJsonObject();
            if (pkgDesc.isEmpty()) {
                return Optional.empty();
            }

            String org = pkgDesc.get(ProjectConstants.ORG).getAsString();
            String name = pkgDesc.get(ProjectConstants.PACKAGE_NAME).getAsString();
            BalToolsManifest.Tool tool = new BalToolsManifest.Tool(toolId,
                   org, name, version, false, LOCAL_REPOSITORY_NAME);
            if (Files.exists(localBalaPath.resolve(org).resolve(name).resolve(version))) {
                return Optional.of(tool);
            }
        } catch (IOException e) {
            throw new ProjectException("Failed to read local-tools.json file: " + e.getMessage());
        }
        return Optional.empty();
    }

    public static boolean addToBalToolsToml(BalToolsManifest.Tool tool, PrintStream printStream) {
        BalToolsToml balToolsToml = BalToolsToml.from(BAL_TOOLS_TOML_PATH);
        BalToolsManifest balToolsManifest = BalToolsManifestBuilder.from(balToolsToml).build();

        String toolId = tool.id();
        String org = tool.org();
        String name = tool.name();
        String version = tool.version();
        String repository = tool.repository();

        boolean isCompatibleWithPlatform = isCompatibleWithPlatform(org, name, version, repository);
        if (!isCompatibleWithPlatform) {
            printStream.println("Tool '" + toolId + ":" + version + "' is not compatible with the current " +
                    "Ballerina distribution version. Run 'bal tool list' to see compatible versions.");
            return true;
        }
        balToolsManifest.addTool(toolId, org, name, version, true, repository);
        balToolsToml.modify(balToolsManifest);
        return false;
    }

    public static Optional<BalToolsManifest.Tool> getToolAvailableLocally(
            String toolId, String version, String repository) {
        if (version.equals(Names.EMPTY.getValue())) {
            return Optional.empty();
        }
        BalToolsToml balToolsToml = BalToolsToml.from(BAL_TOOLS_TOML_PATH);
        BalToolsManifest balToolsManifest = BalToolsManifestBuilder.from(balToolsToml).build();
        Optional<BalToolsManifest.Tool> toolOptional = balToolsManifest.getTool(toolId, version, repository);
        if (toolOptional.isEmpty()) {
            return Optional.empty();
        }
        BalToolsManifest.Tool tool = toolOptional.get();

        Path toolCacheDir = RepoUtils.createAndGetHomeReposPath()
                .resolve(REPOSITORIES_DIR).resolve(CENTRAL_REPOSITORY_CACHE_NAME)
                .resolve(BALA_DIR_NAME).resolve(tool.org()).resolve(tool.name());
        if (toolCacheDir.toFile().isDirectory()) {
            try (Stream<Path> versions = Files.list(toolCacheDir)) {
                if (versions.anyMatch(path -> Optional.of(path.getFileName()).get().toString().equals(version))) {
                    return toolOptional;
                }
            } catch (IOException e) {
                throw new ProjectException("Error while looking for locally available tools: " + e);
            }
        }
        return Optional.empty();
    }
}
