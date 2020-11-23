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

import java.util.Objects;

/**
 * Build options of a project.
 */
public class BuildOptions {
    private Boolean testReport;
    private Boolean codeCoverage;
    private CompilationOptions compilationOptions;

    BuildOptions(Boolean testReport,
                 Boolean codeCoverage,
                 CompilationOptions compilationOptions) {
        this.testReport = testReport;
        this.codeCoverage = codeCoverage;
        this.compilationOptions = compilationOptions;
    }

    public boolean testReport() {
        return toBooleanDefaultIfNull(testReport);
    }

    public boolean codeCoverage() {
        return toBooleanDefaultIfNull(codeCoverage);
    }

    public boolean skipTests() {
        return this.compilationOptions.skipTests();
    }

    public boolean offlineBuild() {
        return this.compilationOptions.offlineBuild();
    }

    public boolean experimental() {
        return this.compilationOptions.experimental();
    }

    public boolean observabilityIncluded() {
        return this.compilationOptions.observabilityIncluded();
    }

    CompilationOptions compilationOptions() {
        return compilationOptions;
    }

    /**
     * Merge the given build options by favoring theirs if there are conflicts.
     *
     * @param theirOptions Build options to be merged
     * @return a new {@code BuildOptions} instance that contains our options and their options
     */
    public BuildOptions acceptTheirs(BuildOptions theirOptions) {

        this.codeCoverage = Objects.requireNonNullElseGet(
                theirOptions.codeCoverage, () -> toBooleanDefaultIfNull(this.codeCoverage));
        this.testReport = Objects.requireNonNullElseGet(
                theirOptions.testReport, () -> toBooleanDefaultIfNull(this.testReport));
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
        TEST_REPORT("testReport"),
        CODE_COVERAGE("codeCoverage"),
        B7A_CONFIG_FILE("b7aConfigFile")
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
