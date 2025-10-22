/*
 *  Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com)
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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
package io.ballerina.projects;

import io.ballerina.projects.util.BalToolsUtil;
import io.ballerina.projects.util.ProjectUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.ballerina.projects.util.ProjectConstants.DISTRIBUTION_REPOSITORY_NAME;
import static io.ballerina.projects.util.ProjectConstants.LOCAL_REPOSITORY_NAME;

/**
 * This class is used to merge the local and distribution bal-tools.toml files.
 *
 * @since 2201.13.0
 */
public class BlendedBalToolsManifest {
    private final Map<String, Map<String, Map<String, BalToolsManifest.Tool>>> tools;

    public BlendedBalToolsManifest(Map<String, Map<String, Map<String, BalToolsManifest.Tool>>> tools) {
        this.tools = tools;
    }

    /**
     * Create a new BlendedBalToolsManifest from the local and distribution bal-tools.toml files.
     *
     * @param localBalToolManifest  The local bal-tools.toml file
     * @param distBalToolManifest   The distribution bal-tools.toml file
     * @return an instance of BlendedBalToolsManifest
     */
    public static BlendedBalToolsManifest from(
            BalToolsManifest localBalToolManifest, BalToolsManifest distBalToolManifest) {
        return mergeBalToolManifests(localBalToolManifest, distBalToolManifest);
    }

    private static BlendedBalToolsManifest mergeBalToolManifests(BalToolsManifest localBalToolsManifest,
            BalToolsManifest distBalToolsManifest) {

        Map<String, Map<String, Map<String, BalToolsManifest.Tool>>> mergedTools = new HashMap<>();

        for (Map.Entry<String, Map<String, Map<String, BalToolsManifest.Tool>>> mapEntry :
                localBalToolsManifest.tools().entrySet()) {
            mergedTools.put(mapEntry.getKey(), new HashMap<>(mapEntry.getValue()));
        }

        for (Map.Entry<String, Map<String, Map<String, BalToolsManifest.Tool>>> toolEntry : mergedTools.entrySet()) {
            String toolId = toolEntry.getKey();
            if (BalToolsUtil.getInBuiltToolCommands(distBalToolsManifest).contains(toolId)) {
                continue;
            }
            Optional<BalToolsManifest.Tool> activeTool = localBalToolsManifest.getActiveTool(toolId);
            if (activeTool.isPresent() && LOCAL_REPOSITORY_NAME.equals(activeTool.get().repository())) {
                // No active tool versions or the tool is set to use from the local repo. Prioritize this version
                continue;
            }

            if (activeTool.isPresent() && activeTool.get().force()) {
                // The active tool version is forced. Prioritize this version
                continue;
            }

            BalToolsManifest.Tool tool = mergedTools.get(toolId).values().stream()
                    .flatMap(map -> map.values().stream())
                    .findFirst().orElseThrow();

            String org = tool.org();
            String name = tool.name();
            String version = tool.version();

            // Set the active tool version if the current active version is incompatible with the distribution
            SemanticVersion.VersionCompatibilityResult versionCompatibilityResult =
                    BalToolsUtil.compareToolDistWithCurrentDist(org, name, version, tool.repository());

            if (versionCompatibilityResult.equals(SemanticVersion.VersionCompatibilityResult.INCOMPATIBLE)) {
                // Current active version is incompatible with the distribution
                continue;
            }

            if (!versionCompatibilityResult.equals(SemanticVersion.VersionCompatibilityResult.EQUAL)) {
                Optional<PackageVersion> highestVersion = getHighestCompatibleLocalVersion(localBalToolsManifest,
                        toolId, org, name);
                if (highestVersion.isEmpty()) {
                    continue;
                }
                mergedTools.get(toolId).get(version).get(null).setActive(false);
                mergedTools.get(toolId).get(highestVersion.get().toString()).get(null).setActive(true);
            }
        }

        // Handle active versions of distribution tools

        // 1. No tool versions in the local bal-tools.toml => set the version in the distribution as active
        // 2. No locally installed versions => => set the version in the distribution as active
        // 3. Locally active version is compatible but not the highest/incompatible
        // 3.1 Highest compatible version in the local bal-tools.toml < version in the distribution bal-tools.toml
        //                                      => set the version in the distribution as active
        // 3.2 Highest compatible version in the local bal-tools.toml > version in the distribution bal-tools.toml
        //                                      => set the version in the local bal-tools.toml as active

        for (String toolCommand : BalToolsUtil.getInBuiltToolCommands(distBalToolsManifest)) {
            Optional<BalToolsManifest.Tool> activeToolDist = distBalToolsManifest.getActiveTool(toolCommand);
            if (!mergedTools.containsKey(toolCommand)) {
                // 1. No tool versions in the local bal-tools.toml
                BalToolsManifest.Tool tool = activeToolDist.orElseThrow();
                BalToolsManifest.Tool toolNew = new BalToolsManifest.Tool(
                        tool.id(), tool.org(), tool.name(), tool.version(), true, DISTRIBUTION_REPOSITORY_NAME);
                mergedTools.put(tool.id(), Map.of(tool.version(), Map.of(DISTRIBUTION_REPOSITORY_NAME, toolNew)));
                continue;
            }

            // Check if the activeToolDist is already present in the local bal-tools.toml
            Optional<BalToolsManifest.Tool> sameToolInLocal = mergedTools.get(toolCommand).values().stream()
                    .flatMap(v -> v.values().stream())
                    .filter(tool1 -> tool1.id().equals(activeToolDist.orElseThrow().id()) &&
                            tool1.version().equals(activeToolDist.orElseThrow().version()))
                    .findFirst();
            if (sameToolInLocal.isEmpty()) {
                // Version not present locally. Add the version to the blended tool manifest
                BalToolsManifest.Tool toolDist = activeToolDist.orElseThrow();
                BalToolsManifest.Tool toolNew = new BalToolsManifest.Tool(
                        toolDist.id(), toolDist.org(), toolDist.name(), toolDist.version(), false, DISTRIBUTION_REPOSITORY_NAME);

                if (mergedTools.containsKey(toolDist.id())) {
                    mergedTools.get(toolDist.id()).put(toolDist.version(),
                            Map.of(DISTRIBUTION_REPOSITORY_NAME, toolNew));
                } else {
                    mergedTools.put(toolDist.id(), Map.of(toolDist.version(),
                            Map.of(DISTRIBUTION_REPOSITORY_NAME, toolNew)));
                }
            }

            Optional<BalToolsManifest.Tool> activeToolLocal = mergedTools.get(toolCommand).values().stream()
                    .flatMap(v -> v.values().stream()).filter(BalToolsManifest.Tool::active).findFirst();

            BalToolsManifest.Tool distTool = activeToolDist.orElseThrow();
            if (activeToolLocal.isEmpty()) {
                Optional<PackageVersion> highestVersion = getHighestCompatibleLocalVersion(localBalToolsManifest,
                        activeToolDist.get().id(), activeToolDist.get().org(), activeToolDist.get().name());
                if (highestVersion.isEmpty()) {
                    // 2. No locally installed versions => set the version in the distribution as active
                    BalToolsManifest.Tool toolNew = new BalToolsManifest.Tool(
                            distTool.id(), distTool.org(), distTool.name(), distTool.version(),
                            true, DISTRIBUTION_REPOSITORY_NAME);
                    if (mergedTools.containsKey(distTool.id())) {
                        mergedTools.get(distTool.id()).put(distTool.version(),
                                Map.of(DISTRIBUTION_REPOSITORY_NAME, toolNew));
                    } else {
                        mergedTools.put(distTool.id(), Map.of(distTool.version(),
                                Map.of(DISTRIBUTION_REPOSITORY_NAME, toolNew)));
                    }
                    continue;
                }
                activeToolLocal = mergedTools.get(toolCommand).values().stream()
                        .flatMap(v -> v.values().stream()).filter(tool -> tool.version().equals(
                                highestVersion.get().toString())).findFirst();
            }

            BalToolsManifest.Tool localTool = activeToolLocal.orElseThrow();
            if (LOCAL_REPOSITORY_NAME.equals(localTool.repository())) {
                // Tool is set to use from the local repo. Prioritize this version
                continue;
            }

            if (localTool.force()) {
                // The active tool version is forced. Prioritize this version
                continue;
            }

            // 3. Locally active version is compatible but not the highest/incompatible
            Optional<PackageVersion> highestVersion = getHighestCompatibleLocalVersion(localBalToolsManifest,
                    localTool.id(), localTool.org(), localTool.name());
            if (highestVersion.isEmpty() ||
                    SemanticVersion.from(highestVersion.get().toString()).lessThan(
                            SemanticVersion.from(activeToolDist.orElseThrow().version()))) {
                // 3.1 Highest compatible version is in the distribution
                mergedTools.get(localTool.id()).forEach((k, v) -> v.forEach((k1, v1) -> v1.setActive(false)));

                BalToolsManifest.Tool tool = activeToolDist.orElseThrow();
                BalToolsManifest.Tool toolNew = new BalToolsManifest.Tool(
                        tool.id(), tool.org(), tool.name(), tool.version(), true, DISTRIBUTION_REPOSITORY_NAME);
                if (mergedTools.containsKey(distTool.id())) {
                    mergedTools.get(distTool.id()).put(distTool.version(),
                            Map.of(DISTRIBUTION_REPOSITORY_NAME, toolNew));
                } else {
                    mergedTools.put(distTool.id(), Map.of(distTool.version(),
                            Map.of(DISTRIBUTION_REPOSITORY_NAME, toolNew)));
                }
                continue;
            }

            // 3.2 Highest compatible version in the local bal-tools.toml
            BalToolsManifest.Tool tool = new BalToolsManifest.Tool(
                    localTool.id(), localTool.org(), localTool.name(),
                    highestVersion.toString(), true, localTool.repository());
            mergedTools.get(localTool.id()).forEach((k, v) -> v.forEach((k1, v1) -> v1.setActive(false)));
            mergedTools.get(tool.id()).get(highestVersion.get().toString()).get(null).setActive(true);

            // Check if the activeToolDist is already present in the local bal-tools.toml
            Optional<BalToolsManifest.Tool> distToolInLocal = mergedTools.get(toolCommand).values().stream()
                    .flatMap(v -> v.values().stream())
                    .filter(tool1 -> tool1.id().equals(activeToolDist.orElseThrow().id()) &&
                            tool1.version().equals(activeToolDist.orElseThrow().version()))
                    .findFirst();
            if (distToolInLocal.isEmpty()) {
                // Version not present locally. Add the version to the blended tool manifest
                BalToolsManifest.Tool toolDist = activeToolDist.orElseThrow();
                BalToolsManifest.Tool toolNew = new BalToolsManifest.Tool(
                        toolDist.id(), toolDist.org(), toolDist.name(), toolDist.version(), false, DISTRIBUTION_REPOSITORY_NAME);

                if (mergedTools.containsKey(toolDist.id())) {
                    mergedTools.get(toolDist.id()).put(toolDist.version(),
                            Map.of(DISTRIBUTION_REPOSITORY_NAME, toolNew));
                } else {
                    mergedTools.put(toolDist.id(), Map.of(toolDist.version(),
                            Map.of(DISTRIBUTION_REPOSITORY_NAME, toolNew)));
                }
            }
        }

        return new BlendedBalToolsManifest(mergedTools);
    }

    private static Optional<PackageVersion> getHighestCompatibleLocalVersion(
            BalToolsManifest localBalToolsManifest, String toolId, String org, String name) {
        List<PackageVersion> toolVersions = new ArrayList<>(localBalToolsManifest.tools()
                .get(toolId).keySet().stream()
                .map(PackageVersion::from)
                .filter(version -> !localBalToolsManifest.tools().get(toolId).get(version.toString())
                        .containsKey(LOCAL_REPOSITORY_NAME))
                .toList());

        // Check if there are any compatible versions in the local bal-tools.toml
        toolVersions.removeIf(version -> {
            SemanticVersion.VersionCompatibilityResult versionCompatibilityResult = BalToolsUtil
                    .compareToolDistWithCurrentDist(org, name, version.toString(), null);
            return versionCompatibilityResult.equals(SemanticVersion.VersionCompatibilityResult.GREATER_THAN) ||
                    versionCompatibilityResult.equals(SemanticVersion.VersionCompatibilityResult.INCOMPATIBLE);
        });

        if (toolVersions.isEmpty()) {
            return Optional.empty();
        }
        PackageVersion highestVersion = toolVersions.stream().findFirst().orElseThrow();
        for (PackageVersion toolVersion : toolVersions) {
            highestVersion = ProjectUtils.getLatest(highestVersion, toolVersion);
        }
        return Optional.of(highestVersion);
    }

    /*
    * Returns the tools in the blended bal-tools.toml file.
    */
    public Map<String, Map<String, Map<String, BalToolsManifest.Tool>>> tools() {
        return tools;
    }

    /*
     * Returns the tools that are compatible with the current distribution.
     */
    public Map<String, Map<String, Map<String, BalToolsManifest.Tool>>> compatibleTools() {
        // Remove incompatible versions
        Map<String, Map<String, Map<String, BalToolsManifest.Tool>>> compatibleTools = new HashMap<>(tools);
        for (String toolId : tools.keySet()) {
            Map<String, Map<String, BalToolsManifest.Tool>> versions = compatibleTools.get(toolId);
            versions.keySet().removeIf(version -> !BalToolsUtil.isCompatibleWithPlatform(
                    versions.get(version).values().iterator().next().org(),
                    versions.get(version).values().iterator().next().name(), version,
                    versions.get(version).values().iterator().next().repository()));
        }

        return compatibleTools;
    }

    /*
     * Returns the versions of the specified tool that are compatible with the current distribution.
     *
     * @param toolId tool id
     * @return the list of compatible versions
     */
    public Map<String, Map<String, BalToolsManifest.Tool>> compatibleVersions(String toolId) {
        Map<String, Map<String, Map<String, BalToolsManifest.Tool>>> compatibleTools = new HashMap<>(tools);
        Map<String, Map<String, BalToolsManifest.Tool>> versions = compatibleTools.get(toolId);
        if (versions == null) {
            return new HashMap<>();
        }
        versions.keySet().removeIf(version -> !BalToolsUtil.isCompatibleWithPlatform(
                        versions.get(version).values().iterator().next().org(),
                        versions.get(version).values().iterator().next().name(), version,
                        versions.get(version).values().iterator().next().repository()));
        return versions;
    }

    /**
     * Retrieves a specific tool.
     *
     * @param id         tool id
     * @param version    tool version
     * @param repository repository to retrieve the tool from
     * @return an optional containing the tool if found, otherwise empty
     */
    public Optional<BalToolsManifest.Tool> getTool(String id, String version, String repository) {
        if (tools.containsKey(id) && tools.get(id).containsKey(version)) {
            if (LOCAL_REPOSITORY_NAME.equals(repository)) {
                return Optional.ofNullable(tools.get(id).get(version).get(repository));
            }
            return Optional.of(tools.get(id).get(version).entrySet().iterator().next().getValue());
        }
        return Optional.empty();
    }

    /**
     * Retrieves the active version of a specific tool.
     *
     * @param id         tool id
     * @return an optional containing the tool if found, otherwise empty
     */
    public Optional<BalToolsManifest.Tool> getActiveTool(String id) {
        if (tools.containsKey(id)) {
            Optional<BalToolsManifest.Tool> activeTool = tools.get(id).values().stream().flatMap(
                    v -> v.values().stream()).filter(BalToolsManifest.Tool::force).findFirst();
            if (activeTool.isEmpty()) {
                activeTool = tools.get(id).values().stream().flatMap(
                        v -> v.values().stream()).filter(BalToolsManifest.Tool::active).findFirst();
            }
            return activeTool;
        }
        return Optional.empty();
    }

    /**
     * Retrieves the highest compatible version of a specific tool that is not from the local repository.
     *
     * @param toolId tool id
     * @return the highest compatible tool version
     */
    public BalToolsManifest.Tool getHighestCompatibleToolVersion(String toolId) {
        Map<String, Map<String, Map<String, BalToolsManifest.Tool>>> compatibleTools = compatibleTools();
        List<PackageVersion> toolVersions = new ArrayList<>(compatibleTools
                .get(toolId).keySet().stream()
                .map(PackageVersion::from)
                .filter(version -> !compatibleTools.get(toolId).get(version.toString())
                        .containsKey(LOCAL_REPOSITORY_NAME))
                .toList());

        PackageVersion highestVersion = toolVersions.stream().findFirst().orElseThrow();
        for (PackageVersion toolVersion : toolVersions) {
            highestVersion = ProjectUtils.getLatest(highestVersion, toolVersion);
        }
        return compatibleTools.get(toolId).get(highestVersion.toString()).values().iterator().next();
    }
}
