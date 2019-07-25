/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.packerina.task;

import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.packerina.BuildContext;
import org.ballerinalang.packerina.utils.CLIConstants;
import org.wso2.ballerinalang.compiler.Compiler;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;

import java.util.List;

import static org.ballerinalang.compiler.CompilerOptionName.COMPILER_PHASE;
import static org.ballerinalang.compiler.CompilerOptionName.EXPERIMENTAL_FEATURES_ENABLED;
import static org.ballerinalang.compiler.CompilerOptionName.LOCK_ENABLED;
import static org.ballerinalang.compiler.CompilerOptionName.OFFLINE;
import static org.ballerinalang.compiler.CompilerOptionName.PROJECT_DIR;
import static org.ballerinalang.compiler.CompilerOptionName.SIDDHI_RUNTIME_ENABLED;
import static org.ballerinalang.compiler.CompilerOptionName.SKIP_TESTS;
import static org.ballerinalang.compiler.CompilerOptionName.TEST_ENABLED;
import static org.ballerinalang.packerina.utils.CLIConstants.ENABLE_EXPERIMENTAL_FEATURES;
import static org.ballerinalang.packerina.utils.CLIConstants.ENABLE_SIDDHI_RUNTIME;
import static org.ballerinalang.packerina.utils.CLIConstants.LOCK_ENEABLED;
import static org.ballerinalang.packerina.utils.CLIConstants.OFFLINE_BUILD;

/**
 * Task for compiling a package.
 */
public class CompileTask implements Task {
    public static final String COMPILED_PACKAGES = "COMPILED PACKAGES";
    @Override
    public void execute(BuildContext buildContext) {
        CompilerPhase compilerPhase = CompilerPhase.BIR_GEN;
        CompilerContext context = new CompilerContext();
        CompilerOptions options = CompilerOptions.getInstance(context);
        options.put(PROJECT_DIR, buildContext.getSourceRoot().toString());
        options.put(OFFLINE, buildContext.getBuildData().get(OFFLINE_BUILD).toString());
        options.put(COMPILER_PHASE, compilerPhase.toString());
        options.put(LOCK_ENABLED, buildContext.getBuildData().get(LOCK_ENEABLED).toString());
        options.put(SKIP_TESTS, buildContext.getBuildData().get(CLIConstants.SKIP_TESTS).toString());
        options.put(TEST_ENABLED, "true");
        options.put(EXPERIMENTAL_FEATURES_ENABLED,
                buildContext.getBuildData().get(ENABLE_EXPERIMENTAL_FEATURES).toString());
        options.put(SIDDHI_RUNTIME_ENABLED, buildContext.getBuildData().get(ENABLE_SIDDHI_RUNTIME).toString());
    
        Compiler compiler = Compiler.getInstance(context);
        List<BLangPackage> packages = compiler.build();
        buildContext.getBuildData().put(COMPILED_PACKAGES, packages);
    }
}
