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

/**
 * This class can be used to create an instance of {@code CompilationOptions}.
 * <p>
 * Implements the builder pattern.
 *
 * @since 2.0.0
 */
public class CompilationOptionsBuilder {
    private Boolean offline;
    private Boolean experimental;
    private Boolean observabilityIncluded;
    private Boolean dumpBir;
    private Boolean dumpBirFile;
    private String cloud;
    private Boolean listConflictedClasses;
    private Boolean sticky;
    private Boolean dumpGraph;
    private Boolean dumpRawGraph;
    private Boolean withCodeGenerators;

    public CompilationOptionsBuilder() {
    }

    public CompilationOptionsBuilder offline(Boolean value) {
        offline = value;
        return this;
    }

    public CompilationOptionsBuilder sticky(Boolean value) {
        sticky = value;
        return this;
    }

    CompilationOptionsBuilder experimental(Boolean value) {
        experimental = value;
        return this;
    }

    CompilationOptionsBuilder observabilityIncluded(Boolean value) {
        observabilityIncluded = value;
        return this;
    }

    CompilationOptionsBuilder dumpBir(Boolean value) {
        dumpBir = value;
        return this;
    }

    CompilationOptionsBuilder cloud(String value) {
        cloud = value;
        return this;
    }

    CompilationOptionsBuilder dumpBirFile(Boolean value) {
        dumpBirFile = value;
        return this;
    }

    CompilationOptionsBuilder dumpGraph(Boolean value) {
        dumpGraph = value;
        return this;
    }

    CompilationOptionsBuilder dumpRawGraphs(Boolean value) {
        dumpRawGraph = value;
        return this;
    }

    CompilationOptionsBuilder listConflictedClasses(Boolean value) {
        listConflictedClasses = value;
        return this;
    }

    CompilationOptionsBuilder withCodeGenerators(Boolean value) {
        withCodeGenerators = value;
        return this;
    }

    public CompilationOptions build() {
        return new CompilationOptions(offline, experimental, observabilityIncluded, dumpBir,
                dumpBirFile, cloud, listConflictedClasses, sticky,
                dumpGraph, dumpRawGraph, withCodeGenerators);
    }
}
