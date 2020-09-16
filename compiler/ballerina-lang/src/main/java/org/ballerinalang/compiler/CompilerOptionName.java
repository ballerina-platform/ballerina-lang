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

    SOURCE_TYPE("sourceType"),

    PRESERVE_WHITESPACE("preserveWhitespace"),

    OFFLINE("offline"),

    SKIP_TESTS("skipTests"),

    COMPILER_PHASE("compilerPhase"),

    TRANSACTION_EXISTS("transactionBlockExists"),

    BUILD_COMPILED_MODULE("buildCompiledModule"),

    GEN_NATIVE_BINARY("genNativeBinary"),

    TEST_ENABLED("testEnabled"),

    TARGET_BINARY_PATH("targetBinaryPath"),

    BALO_GENERATION("genBalo"),

    SKIP_MODULE_DEPENDENCIES("skipModuleDependencies"),

    DUMP_BIR("dumpBIR"),

    DUMP_BIR_FILE("dumpBIRFile"),

    LOCK_ENABLED("lockEnabled"),

    EXPERIMENTAL_FEATURES_ENABLED("experimentalFeaturesEnabled"),

    TOOLING_COMPILATION("toolingCompilation");

    public final String name;

    CompilerOptionName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
