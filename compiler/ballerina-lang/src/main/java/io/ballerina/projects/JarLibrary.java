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

import java.nio.file.Path;
import java.util.Optional;

/**
 * Represents the Jar library in JBallerinaBackend implementation.
 *
 * @since 2.0.0
 */
public class JarLibrary extends PlatformLibrary {
    static final String KEY_PATH = "path";
    static final String KEY_SCOPE = "scope";
    static final String KEY_ARTIFACT_ID = "artifactId";
    static final String KEY_GROUP_ID = "groupId";
    static final String KEY_VERSION = "version";

    private final String artifactId;
    private final String version;
    private final String groupId;
    private final Path path;
    private final String packageName;

    public JarLibrary(Path path, PlatformLibraryScope scope) {
        super(scope);
        this.path = path;
        artifactId = null;
        groupId = null;
        version = null;
        packageName = null;
    }

    public JarLibrary(Path path, PlatformLibraryScope scope, String artifactId, String groupId, String version,
            String packageName) {
        super(scope);
        this.path = path;
        this.packageName = (packageName == null || packageName.isEmpty()) ? null : packageName;
        this.artifactId = (artifactId == null || artifactId.isEmpty()) ? null : artifactId;
        this.groupId = (groupId == null || groupId.isEmpty()) ? null : groupId;
        this.version = (version == null || version.isEmpty()) ? null : version;
    }

    public JarLibrary(Path path, PlatformLibraryScope scope, String packageName) {
        super(scope);
        this.path = path;
        this.packageName = (packageName == null || packageName.isEmpty()) ? null : packageName;
        this.artifactId = null;
        this.groupId = null;
        this.version = null;
    }

    @Override
    public Path path() {
        return path;
    }

    public Optional<String> packageName() {
        return Optional.ofNullable(packageName);
    }

    public Optional<String> artifactId() {
        return Optional.ofNullable(artifactId);
    }

    public Optional<String> version() {
        return Optional.ofNullable(version);
    }

    public Optional<String> groupId() {
        return Optional.ofNullable(groupId);
    }

    @Override
    public boolean equals(Object other) {

        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        JarLibrary that = (JarLibrary) other;

        return path().toFile().toString().equals(that.path().toFile().toString());
    }

}
