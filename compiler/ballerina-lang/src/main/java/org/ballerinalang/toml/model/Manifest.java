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
package org.ballerinalang.toml.model;

import org.ballerinalang.compiler.BLangCompilerException;
import org.wso2.ballerinalang.programfile.ProgramFileConstants;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Defines the manifest object which is created using the toml file configs.
 *
 * @since 0.964
 */
public class Manifest {
    private Project project = new Project();
    private Map<String, Object> dependencies = new LinkedHashMap<>();
    public Platform platform = new Platform();
    private BuildOptions buildOptions;

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public Map<String, Object> getDependenciesAsObjectMap() {
        return this.dependencies.entrySet().stream()
                .collect(Collectors.toMap(d -> d.getKey().replaceAll("^\"|\"$", ""), Map.Entry::getValue));
    }

    public List<Dependency> getDependencies() {
        return this.dependencies.entrySet().stream()
                .map(entry -> {
                    Dependency dependency = new Dependency();
                    // get rid of the double quotes
                    dependency.setModuleID(entry.getKey());
                    dependency.setMetadata(convertObjectToDependencyMetadata(entry.getValue()));
                    return dependency;
                })
                .collect(Collectors.toList());
    }

    private DependencyMetadata convertObjectToDependencyMetadata(Object obj) {
        DependencyMetadata metadata = new DependencyMetadata();
        if (obj instanceof String s) {
            metadata.setVersion(s);
        } else if (obj instanceof Map<?, ?> metadataMap) {
            if (metadataMap.keySet().contains("version") && metadataMap.get("version") instanceof String) {
                metadata.setVersion((String) metadataMap.get("version"));
            }

            if (metadataMap.keySet().contains("path") && metadataMap.get("path") instanceof String) {
                metadata.setPath((String) metadataMap.get("path"));
            }
        }
        return metadata;
    }

    public Platform getPlatform() {
        return platform;
    }

    public void setPlatform(Platform platform) {
        this.platform = platform;
    }

    public String getTargetPlatform(String moduleName) {
        // If module is a template we will return any
        if (isTemplateModule(moduleName)) {
            return ProgramFileConstants.ANY_PLATFORM;
        }
        // check if platform exists
        if (null != platform.libraries) {
            // Check if target exist return error if not
            if (null == platform.target) {
                throw new BLangCompilerException("Platform target is not specified in the Ballerina.toml");
            }
            // Check if it is a valid platform
            if (!(Arrays.stream(ProgramFileConstants.SUPPORTED_PLATFORMS).anyMatch(platform.getTarget()::equals))) {
                throw new BLangCompilerException("Platform target is not " +
                        "supported by installed Ballerina distribution." +
                        "\nSupported platforms : " + supportedPlatforms());
            }
            // Check if module have platform specific libraries
            List<Library> deps = platform.libraries.stream().filter(library -> library.getModules() == null ||
                Arrays.stream(library.getModules()).anyMatch(moduleName::equals)).toList();
            // If not return any
            if (!deps.isEmpty()) {
                return platform.target;
            }
        }
        // return any if not
        return ProgramFileConstants.ANY_PLATFORM;
    }

    private String supportedPlatforms() {
        String platforms = String.join(",", ProgramFileConstants.SUPPORTED_PLATFORMS);
        return platforms;
    }

    public boolean isTemplateModule(String moduleName) {
        return this.getProject().getTemplates().contains(moduleName);
    }

    public void setBuildOptions(BuildOptions buildOptions) {
        this.buildOptions = buildOptions;
    }

    public BuildOptions getBuildOptions() {
        return buildOptions;
    }
}
