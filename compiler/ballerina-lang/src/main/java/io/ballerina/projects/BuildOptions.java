/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.projects;

/**
 * Build options of a project.
 */
public class BuildOptions {
    private Boolean testReport;
    private Boolean codeCoverage;
    private Boolean dumpBuildTime;
    private Boolean skipTests;
    private CompilationOptions compilationOptions;
    private String targetDir;

    BuildOptions(Boolean testReport, Boolean codeCoverage, Boolean dumpBuildTime, Boolean skipTests,
                 CompilationOptions compilationOptions, String targetPath) {
        this.testReport = testReport;
        this.codeCoverage = codeCoverage;
        this.dumpBuildTime = dumpBuildTime;
        this.skipTests = skipTests;
        this.compilationOptions = compilationOptions;
        this.targetDir = targetPath;
    }

    public boolean testReport() {
        return toBooleanDefaultIfNull(this.testReport);
    }

    public boolean codeCoverage() {
        return toBooleanDefaultIfNull(this.codeCoverage);
    }

    public boolean dumpBuildTime() {
        return toBooleanDefaultIfNull(this.dumpBuildTime);
    }

    public boolean skipTests() {
        // By default, the tests will be skipped
        return toBooleanTrueIfNull(this.skipTests);
    }

    public boolean offlineBuild() {
        return this.compilationOptions.offlineBuild();
    }

    public boolean sticky() {
        return this.compilationOptions.sticky();
    }

    public boolean experimental() {
        return this.compilationOptions.experimental();
    }

    public boolean observabilityIncluded() {
        return this.compilationOptions.observabilityIncluded();
    }

    public boolean listConflictedClasses() {
        return this.compilationOptions.listConflictedClasses();
    }

    public String cloud() {
        return this.compilationOptions.getCloud();
    }

    CompilationOptions compilationOptions() {
        return this.compilationOptions;
    }

    public boolean exportOpenAPI() {
        return this.compilationOptions.exportOpenAPI();
    }

    /**
     * Merge the given build options by favoring theirs if there are conflicts.
     *
     * @param theirOptions Build options to be merged
     * @return a new {@code BuildOptions} instance that contains our options and their options
     */
    public BuildOptions acceptTheirs(BuildOptions theirOptions) {
        BuildOptionsBuilder buildOptionsBuilder = new BuildOptionsBuilder();
        if (theirOptions.skipTests != null) {
            buildOptionsBuilder.setSkipTests(theirOptions.skipTests);
        } else {
            buildOptionsBuilder.setSkipTests(this.skipTests);
        }
        if (theirOptions.codeCoverage != null) {
            buildOptionsBuilder.setCodeCoverage(theirOptions.codeCoverage);
        } else {
            buildOptionsBuilder.setCodeCoverage(this.codeCoverage);
        }
        if (theirOptions.testReport != null) {
            buildOptionsBuilder.setTestReport(theirOptions.testReport);
        } else {
            buildOptionsBuilder.setTestReport(this.testReport);
        }
        if (theirOptions.dumpBuildTime != null) {
            buildOptionsBuilder.setDumpBuildTime(theirOptions.dumpBuildTime);
        } else {
            buildOptionsBuilder.setDumpBuildTime(this.dumpBuildTime);
        }
        if (theirOptions.targetDir != null) {
            buildOptionsBuilder.targetDir(theirOptions.targetDir);
        } else {
            buildOptionsBuilder.targetDir(this.targetDir);
        }

        CompilationOptions compilationOptions = this.compilationOptions.acceptTheirs(theirOptions.compilationOptions());
        buildOptionsBuilder.setOffline(compilationOptions.offlineBuild);
        buildOptionsBuilder.setExperimental(compilationOptions.experimental);
        buildOptionsBuilder.setObservabilityIncluded(compilationOptions.observabilityIncluded);
        buildOptionsBuilder.setDumpBir(compilationOptions.dumpBir);
        buildOptionsBuilder.setDumpBirFile(compilationOptions.dumpBirFile);
        buildOptionsBuilder.setDumpGraph(compilationOptions.dumpGraph);
        buildOptionsBuilder.setDumpRawGraphs(compilationOptions.dumpRawGraphs);
        buildOptionsBuilder.setCloud(compilationOptions.cloud);
        buildOptionsBuilder.setListConflictedClasses(compilationOptions.listConflictedClasses);
        buildOptionsBuilder.setSticky(compilationOptions.sticky);
        buildOptionsBuilder.setConfigSchemaGen(compilationOptions.configSchemaGen);
        buildOptionsBuilder.setExportOpenAPI(compilationOptions.exportOpenAPI);

        return buildOptionsBuilder.build();
    }

    public static BuildOptionsBuilder builder() {
        return new BuildOptionsBuilder();
    }

    private boolean toBooleanDefaultIfNull(Boolean bool) {
        if (bool == null) {
            return false;
        }
        return bool;
    }

    private boolean toBooleanTrueIfNull(Boolean bool) {
        if (bool == null) {
            return true;
        }
        return bool;
    }

    public String getTargetPath() {
        return targetDir;
    }

    /**
     * Enum to represent build options.
     */
    public enum OptionName {
        SKIP_TESTS("skipTests"),
        TEST_REPORT("testReport"),
        CODE_COVERAGE("codeCoverage"),
        DUMP_BUILD_TIME("dumpBuildTime"),
        TARGET_DIR("targetDir");

        private final String name;

        OptionName(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    /**
     * A builder for the {@code BuildOptions}.
     *
     * @since 2.0.0
     */
    public static class BuildOptionsBuilder {
        private Boolean testReport;
        private Boolean codeCoverage;
        private Boolean dumpBuildTime;
        private Boolean skipTests;
        private String targetPath;
        private final CompilationOptions.CompilationOptionsBuilder compilationOptionsBuilder;

        private BuildOptionsBuilder() {
            compilationOptionsBuilder = CompilationOptions.builder();
        }

        public BuildOptionsBuilder setTestReport(Boolean value) {
            testReport = value;
            return this;
        }

        public BuildOptionsBuilder setCodeCoverage(Boolean value) {
            codeCoverage = value;
            return this;
        }

        public BuildOptionsBuilder setDumpBuildTime(Boolean value) {
            dumpBuildTime = value;
            return this;
        }

        public BuildOptionsBuilder setSkipTests(Boolean value) {
            skipTests = value;
            return this;
        }

        public BuildOptionsBuilder setSticky(Boolean value) {
            compilationOptionsBuilder.setSticky(value);
            return this;
        }

        public BuildOptionsBuilder setListConflictedClasses(Boolean value) {
            compilationOptionsBuilder.setListConflictedClasses(value);
            return this;
        }

        public BuildOptionsBuilder setOffline(Boolean value) {
            compilationOptionsBuilder.setOffline(value);
            return this;
        }

        public BuildOptionsBuilder setExperimental(Boolean value) {
            compilationOptionsBuilder.setExperimental(value);
            return this;
        }

        public BuildOptionsBuilder setObservabilityIncluded(Boolean value) {
            compilationOptionsBuilder.setObservabilityIncluded(value);
            return this;
        }

        public BuildOptionsBuilder setCloud(String value) {
            compilationOptionsBuilder.setCloud(value);
            return this;
        }

        public BuildOptionsBuilder setDumpBir(Boolean value) {
            compilationOptionsBuilder.setDumpBir(value);
            return this;
        }

        public BuildOptionsBuilder setDumpBirFile(Boolean value) {
            compilationOptionsBuilder.setDumpBirFile(value);
            return this;
        }

        public BuildOptionsBuilder setDumpGraph(Boolean value) {
            compilationOptionsBuilder.setDumpGraph(value);
            return this;
        }

        public BuildOptionsBuilder setDumpRawGraphs(Boolean value) {
            compilationOptionsBuilder.setDumpRawGraphs(value);
            return this;
        }

        public BuildOptionsBuilder targetDir(String path) {
            targetPath = path;
            return this;
        }

        public BuildOptionsBuilder setConfigSchemaGen(Boolean value) {
            compilationOptionsBuilder.setConfigSchemaGen(value);
            return this;
        }

        public BuildOptionsBuilder setExportOpenAPI(Boolean value) {
            compilationOptionsBuilder.setExportOpenAPI(value);
            return this;
        }

        public BuildOptions build() {
            CompilationOptions compilationOptions = compilationOptionsBuilder.build();
            return new BuildOptions(testReport, codeCoverage, dumpBuildTime, skipTests, compilationOptions, targetPath);
        }
    }
}
