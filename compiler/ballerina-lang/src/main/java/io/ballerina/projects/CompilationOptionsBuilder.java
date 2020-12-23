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
class CompilationOptionsBuilder {
    private Boolean skipTests;
    private Boolean buildOffline;
    private Boolean experimental;
    private Boolean observabilityIncluded;
    private Boolean dumpBir;
    private String dumpBirFile;

    public CompilationOptionsBuilder() {
    }

    public CompilationOptionsBuilder skipTests(Boolean value) {
        skipTests = value;
        return this;
    }

    public CompilationOptionsBuilder buildOffline(Boolean value) {
        buildOffline = value;
        return this;
    }

    public CompilationOptionsBuilder experimental(Boolean value) {
        experimental = value;
        return this;
    }

    public CompilationOptionsBuilder observabilityIncluded(Boolean value) {
        observabilityIncluded = value;
        return this;
    }

    public CompilationOptionsBuilder dumpBir(Boolean value) {
        dumpBir = value;
        return this;
    }

    public CompilationOptionsBuilder dumpBirFile(String value) {
        dumpBirFile = value;
        return this;
    }

    public CompilationOptions build() {
        return new CompilationOptions(skipTests, buildOffline, experimental, observabilityIncluded, dumpBir,
                                      dumpBirFile);
    }


}
