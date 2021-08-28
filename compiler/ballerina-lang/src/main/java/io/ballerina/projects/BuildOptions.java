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
        return toBooleanDefaultIfNull(this.skipTests);
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

    /**
     * Merge the given build options by favoring theirs if there are conflicts.
     *
     * @param theirOptions Build options to be merged
     * @return a new {@code BuildOptions} instance that contains our options and their options
     */
    public BuildOptions acceptTheirs(BuildOptions theirOptions) {
        if (theirOptions.skipTests != null) {
            this.skipTests = theirOptions.skipTests;
        }
        if (theirOptions.codeCoverage != null) {
            this.codeCoverage = theirOptions.codeCoverage;
        }
        if (theirOptions.testReport != null) {
            this.testReport = theirOptions.testReport;
        }
        if (theirOptions.dumpBuildTime != null) {
            this.dumpBuildTime = theirOptions.dumpBuildTime;
        }
        this.compilationOptions = compilationOptions.acceptTheirs(theirOptions.compilationOptions());
        return this;
    }

    private boolean toBooleanDefaultIfNull(Boolean bool) {
        if (bool == null) {
            return false;
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

        private String name;

        OptionName(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
