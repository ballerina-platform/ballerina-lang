/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.projects.model;

import org.ballerinalang.toml.model.BuildOptions;
import org.ballerinalang.toml.model.Dependency;
import org.ballerinalang.toml.model.DependencyMetadata;
import org.ballerinalang.toml.model.Platform;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Defines the `ballerina.toml` object which is created using the toml file configs.
 *
 * @since 2.0.0
 */
public class BallerinaToml {
    private Package pkg = new Package();
    private Map<String, Object> dependencies = new LinkedHashMap<>();
    private Platform platform = new Platform();
    private BuildOptions buildOptions;

    public Package getPackage() {
        return pkg;
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

    Map<String, Object> getDependenciesAsObjectMap() {
        return this.dependencies.entrySet().stream()
                .collect(Collectors.toMap(d -> d.getKey().replaceAll("^\"|\"$", ""), Map.Entry::getValue));
    }

    public Platform getPlatform() {
        return platform;
    }

    public BuildOptions getBuildOptions() {
        return buildOptions;
    }

    public void setPkg(Package pkg) {
        this.pkg = pkg;
    }

    public void setDependencies(Map<String, Object> dependencies) {
        this.dependencies = dependencies;
    }

    public void setPlatform(Platform platform) {
        this.platform = platform;
    }

    public void setBuildOptions(BuildOptions buildOptions) {
        this.buildOptions = buildOptions;
    }

    private DependencyMetadata convertObjectToDependencyMetadata(Object obj) {
        DependencyMetadata metadata = new DependencyMetadata();
        if (obj instanceof String) {
            metadata.setVersion((String) obj);
        } else if (obj instanceof Map) {
            Map metadataMap = (Map) obj;
            if (metadataMap.keySet().contains("version") && metadataMap.get("version") instanceof String) {
                metadata.setVersion((String) metadataMap.get("version"));
            }

            if (metadataMap.keySet().contains("path") && metadataMap.get("path") instanceof String) {
                metadata.setPath((String) metadataMap.get("path"));
            }
        }
        return metadata;
    }
}
