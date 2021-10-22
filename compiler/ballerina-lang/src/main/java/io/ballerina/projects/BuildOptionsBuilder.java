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

/**
 * This class can be used to create an instance of {@code BuildOptions}.
 * <p>
 * Implements the builder pattern.
 *
 * @since 2.0.0
 */
public class BuildOptionsBuilder {
    private Boolean testReport;
    private Boolean codeCoverage;
    private Boolean dumpBuildTime;
    private Boolean skipTests;
    private final CompilationOptionsBuilder compilationOptionsBuilder;

    private Path targetPath;

    public BuildOptionsBuilder() {
        compilationOptionsBuilder = new CompilationOptionsBuilder();
    }

    public BuildOptionsBuilder testReport(Boolean value) {
        testReport = value;
        return this;
    }

    public BuildOptionsBuilder codeCoverage(Boolean value) {
        codeCoverage = value;
        return this;
    }

    public BuildOptionsBuilder dumpBuildTime(Boolean value) {
        dumpBuildTime = value;
        return this;
    }

    public BuildOptionsBuilder skipTests(Boolean value) {
        skipTests = value;
        return this;
    }

    public BuildOptionsBuilder targetPath(Path path) {
        targetPath = path;
        return this;
    }

    public BuildOptionsBuilder sticky(Boolean value) {
        compilationOptionsBuilder.sticky(value);
        return this;
    }

    public BuildOptionsBuilder listConflictedClasses(Boolean value) {
        compilationOptionsBuilder.listConflictedClasses(value);
        return this;
    }

    public BuildOptionsBuilder offline(Boolean value) {
        compilationOptionsBuilder.offline(value);
        return this;
    }

    public BuildOptionsBuilder experimental(Boolean value) {
        compilationOptionsBuilder.experimental(value);
        return this;
    }

    public BuildOptionsBuilder observabilityIncluded(Boolean value) {
        compilationOptionsBuilder.observabilityIncluded(value);
        return this;
    }

    public BuildOptionsBuilder cloud(String value) {
        compilationOptionsBuilder.cloud(value);
        return this;
    }

    public BuildOptionsBuilder dumpBir(Boolean value) {
        compilationOptionsBuilder.dumpBir(value);
        return this;
    }

    public BuildOptionsBuilder dumpBirFile(Boolean value) {
        compilationOptionsBuilder.dumpBirFile(value);
        return this;
    }

    public BuildOptionsBuilder dumpGraph(Boolean value) {
        compilationOptionsBuilder.dumpGraph(value);
        return this;
    }

    public BuildOptionsBuilder dumpRawGraphs(Boolean value) {
        compilationOptionsBuilder.dumpRawGraphs(value);
        return this;
    }

    public BuildOptions build() {
        CompilationOptions compilationOptions = compilationOptionsBuilder.build();
        return new BuildOptions(testReport, codeCoverage, dumpBuildTime, skipTests, compilationOptions, targetPath);
    }
}
