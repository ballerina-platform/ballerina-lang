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
package io.ballerina.projects;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Represents a Ballerina.toml file.
 *
 * @since 2.0.0
 */
public class PackageManifest {
    private final PackageDescriptor packageDesc;
    private final List<Dependency> dependencies;
    private final Map<String, Platform> platforms;

    // Other entries hold other key/value pairs available in the Ballerina.toml file.
    // These keys are not part of the Ballerina package specification.
    private final Map<String, Object> otherEntries;

    private PackageManifest(PackageDescriptor packageDesc,
                            List<Dependency> dependencies,
                            Map<String, Platform> platforms,
                            Map<String, Object> otherEntries) {
        this.packageDesc = packageDesc;
        this.dependencies = Collections.unmodifiableList(dependencies);
        this.platforms = Collections.unmodifiableMap(platforms);
        this.otherEntries = Collections.unmodifiableMap(otherEntries);
    }

    public static PackageManifest from(PackageDescriptor packageDesc) {
        return new PackageManifest(packageDesc, Collections.emptyList(),
                Collections.emptyMap(), Collections.emptyMap());
    }

    public static PackageManifest from(PackageDescriptor packageDesc,
                                       List<Dependency> dependencies,
                                       Map<String, Platform> platforms,
                                       Map<String, Object> otherEntries) {
        return new PackageManifest(packageDesc, dependencies, platforms, otherEntries);
    }

    public PackageName name() {
        return packageDesc.name();
    }

    public PackageOrg org() {
        return packageDesc.org();
    }

    public PackageVersion version() {
        return packageDesc.version();
    }

    public PackageDescriptor descriptor() {
        return packageDesc;
    }

    public List<Dependency> dependencies() {
        return dependencies;
    }

    public Platform platform(String platformCode) {
        return platforms.get(platformCode);
    }

    // TODO Do we need to custom key/value par mapping here
    public Object getValue(String key) {
        return otherEntries.get(key);
    }

    /**
     * Represents a dependency of a package.
     *
     * @since 2.0.0
     */
    public static class Dependency {
        private final PackageName packageName;
        private final PackageOrg packageOrg;
        private final PackageVersion semanticVersion;

        public Dependency(PackageName packageName, PackageOrg packageOrg, PackageVersion semanticVersion) {
            this.packageName = packageName;
            this.packageOrg = packageOrg;
            this.semanticVersion = semanticVersion;
        }

        public PackageName name() {
            return packageName;
        }

        public PackageOrg org() {
            return packageOrg;
        }

        public PackageVersion version() {
            return semanticVersion;
        }
    }

    /**
     * Represents the platform section in Ballerina.toml file.
     *
     * @since 2.0.0
     */
    public static class Platform {
        // We could eventually add more things to the platform
        private final List<Map<String, Object>> dependencies;

        public Platform(List<Map<String, Object>> dependencies) {
            this.dependencies = Collections.unmodifiableList(dependencies);
        }

        public List<Map<String, Object>> dependencies() {
            return dependencies;
        }
    }
}
