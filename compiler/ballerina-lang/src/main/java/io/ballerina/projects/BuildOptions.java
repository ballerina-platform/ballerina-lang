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

    BuildOptions(Boolean testReport, Boolean codeCoverage, Boolean dumpBuildTime, Boolean skipTests,
                 CompilationOptions compilationOptions) {
        this.testReport = testReport;
        this.codeCoverage = codeCoverage;
        this.dumpBuildTime = dumpBuildTime;
        this.skipTests = skipTests;
        this.compilationOptions = compilationOptions;
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

    public boolean semtype() {
        return this.compilationOptions.semtype();
    }

    CompilationOptions compilationOptions() {
        return this.compilationOptions;
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
            buildOptionsBuilder.skipTests(theirOptions.skipTests);
        } else {
            buildOptionsBuilder.skipTests(this.skipTests);
        }
        if (theirOptions.codeCoverage != null) {
            buildOptionsBuilder.codeCoverage(theirOptions.codeCoverage);
        } else {
            buildOptionsBuilder.codeCoverage(this.codeCoverage);
        }
        if (theirOptions.testReport != null) {
            buildOptionsBuilder.testReport(theirOptions.testReport);
        } else {
            buildOptionsBuilder.testReport(this.testReport);
        }
        if (theirOptions.dumpBuildTime != null) {
            buildOptionsBuilder.dumpBuildTime(theirOptions.dumpBuildTime);
        } else {
            buildOptionsBuilder.dumpBuildTime(this.dumpBuildTime);
        }

        CompilationOptions compilationOptions = this.compilationOptions.acceptTheirs(theirOptions.compilationOptions());
        buildOptionsBuilder.offline(compilationOptions.offlineBuild);
        buildOptionsBuilder.experimental(compilationOptions.experimental);
        buildOptionsBuilder.observabilityIncluded(compilationOptions.observabilityIncluded);
        buildOptionsBuilder.dumpBir(compilationOptions.dumpBir);
        buildOptionsBuilder.dumpBirFile(compilationOptions.dumpBirFile);
        buildOptionsBuilder.dumpGraph(compilationOptions.dumpGraph);
        buildOptionsBuilder.dumpRawGraphs(compilationOptions.dumpRawGraphs);
        buildOptionsBuilder.cloud(compilationOptions.cloud);
        buildOptionsBuilder.listConflictedClasses(compilationOptions.listConflictedClasses);
        buildOptionsBuilder.sticky(compilationOptions.sticky);
buildOptionsBuilder.semType(compilationOptions.semtype());

        return buildOptionsBuilder.build();
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

    /**
     * Enum to represent build options.
     */
    public enum OptionName {
        SKIP_TESTS("skipTests"),
        TEST_REPORT("testReport"),
        CODE_COVERAGE("codeCoverage"),
        DUMP_BUILD_TIME("dumpBuildTime")
        ;

        private final String name;

        OptionName(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
