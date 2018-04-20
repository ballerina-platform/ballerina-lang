/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.compiler;

/**
 * List of all available command-line options.
 *
 * @since 0.94
 */
public enum CompilerOptionName {

    PROJECT_DIR("projectDirectory"),

    PRESERVE_WHITESPACE("preserveWhitespace"),

    OFFLINE("offline"),

    COMPILER_PHASE("compilerPhase"),

    TRANSACTION_EXISTS("transactionBlockExists"),

    BUILD_COMPILED_PACKAGE("buildCompiledPackage"),

    LIST_PKG("listPkg"),

    DRY_RUN("dryRun"),

    TEST_ENABLED("testEnabled"),

    TARGET_BINARY_PATH("targetBinaryPath");

    public final String name;

    CompilerOptionName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
