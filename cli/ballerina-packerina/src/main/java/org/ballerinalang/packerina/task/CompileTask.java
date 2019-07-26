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
import org.ballerinalang.packerina.buildcontext.BuildContext;
import org.ballerinalang.packerina.buildcontext.BuildContextField;
import org.ballerinalang.packerina.buildcontext.sourcecontext.MultiModuleContext;
import org.ballerinalang.packerina.buildcontext.sourcecontext.SingleFileContext;
import org.ballerinalang.packerina.buildcontext.sourcecontext.SingleModuleContext;
import org.ballerinalang.packerina.buildcontext.sourcecontext.SourceType;
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

/**
 * Task for compiling a package.
 */
public class CompileTask implements Task {

    @Override
    public void execute(BuildContext buildContext) {
        CompilerPhase compilerPhase = CompilerPhase.BIR_GEN;
        CompilerContext context = new CompilerContext();
        CompilerOptions options = CompilerOptions.getInstance(context);
        options.put(PROJECT_DIR, buildContext.get(BuildContextField.SOURCE_ROOT));
        options.put(OFFLINE, buildContext.get(BuildContextField.OFFLINE_BUILD));
        options.put(COMPILER_PHASE, compilerPhase.toString());
        options.put(LOCK_ENABLED, buildContext.get(BuildContextField.LOCK_ENABLED));
        options.put(SKIP_TESTS, buildContext.get(BuildContextField.SKIP_TESTS));
        options.put(TEST_ENABLED, "true");
        options.put(EXPERIMENTAL_FEATURES_ENABLED, buildContext.get(BuildContextField.ENABLE_EXPERIMENTAL_FEATURES));
        options.put(SIDDHI_RUNTIME_ENABLED, buildContext.get(BuildContextField.ENABLE_SIDDHI_RUNTIME));
    
        Compiler compiler = Compiler.getInstance(context);
        
        if (buildContext.getSourceType() == SourceType.BAL_FILE) {
            SingleFileContext singleFileContext = buildContext.get(BuildContextField.SOURCE_CONTEXT);
            BLangPackage compiledModule = compiler.build(singleFileContext.getBalFileName().toString());
            singleFileContext.setModule(compiledModule);
        } else if (buildContext.getSourceType() == SourceType.SINGLE_MODULE) {
            SingleModuleContext moduleContext = buildContext.get(BuildContextField.SOURCE_CONTEXT);
            BLangPackage compiledModule = compiler.build(moduleContext.getModuleName());
            moduleContext.setBLangModule(compiledModule);
        } else {
            MultiModuleContext multiModuleContext = buildContext.get(BuildContextField.SOURCE_CONTEXT);
            List<BLangPackage> compiledModules = compiler.build();
            multiModuleContext.setModules(compiledModules);
        }
        
        // update build context.
        buildContext.put(BuildContextField.COMPILER_CONTEXT, context);
    }
}
