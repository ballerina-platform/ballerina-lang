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
    private Boolean buildOffline;
    private Boolean experimental;
    private Boolean observabilityIncluded;
    private Boolean dumpBir;
    private String dumpBirFile;
    private String cloud;
    private Boolean listConflictedClasses;
    private Boolean sticky;

    public CompilationOptionsBuilder() {
    }

    public CompilationOptionsBuilder buildOffline(Boolean value) {
        buildOffline = value;
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

    CompilationOptionsBuilder dumpBirFile(String value) {
        dumpBirFile = value;
        return this;
    }

    CompilationOptionsBuilder listConflictedClasses(Boolean value) {
        listConflictedClasses = value;
        return this;
    }

    public CompilationOptions build() {
        return new CompilationOptions(buildOffline, experimental, observabilityIncluded, dumpBir,
                dumpBirFile, cloud, listConflictedClasses, sticky);
    }

    void sticky(Boolean value) {
        sticky = value;
    }
}
