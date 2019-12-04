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

import org.ballerinalang.packerina.buildcontext.BuildContext;
import org.ballerinalang.packerina.buildcontext.BuildContextField;
import org.ballerinalang.packerina.buildcontext.sourcecontext.MultiModuleContext;
import org.ballerinalang.packerina.buildcontext.sourcecontext.SingleFileContext;
import org.ballerinalang.packerina.buildcontext.sourcecontext.SingleModuleContext;
import org.ballerinalang.packerina.buildcontext.sourcecontext.SourceType;
import org.ballerinalang.packerina.model.ExecutableJar;
import org.wso2.ballerinalang.compiler.Compiler;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.nio.file.Path;
import java.util.List;

import static org.ballerinalang.tool.LauncherUtils.createLauncherException;

/**
 * Task for compiling a package.
 */
public class CompileTask implements Task {
    
    @Override
    public void execute(BuildContext buildContext) {
        CompilerContext context = buildContext.get(BuildContextField.COMPILER_CONTEXT);
        
        Compiler compiler = Compiler.getInstance(context);
        compiler.setOutStream(buildContext.out());
        if (buildContext.getSourceType() == SourceType.SINGLE_BAL_FILE) {
            SingleFileContext singleFileContext = buildContext.get(BuildContextField.SOURCE_CONTEXT);
            Path balFile = singleFileContext.getBalFile().getFileName();
            if (null != balFile) {
                BLangPackage compiledModule = compiler.build(balFile.toString());
                singleFileContext.setModule(compiledModule);
                buildContext.moduleDependencyPathMap.put(compiledModule.packageID, new ExecutableJar());
            } else {
                throw createLauncherException("unable to find ballerina source");
            }
        } else if (buildContext.getSourceType() == SourceType.SINGLE_MODULE) {
            SingleModuleContext moduleContext = buildContext.get(BuildContextField.SOURCE_CONTEXT);
            BLangPackage compiledModule = compiler.build(moduleContext.getModuleName());
            moduleContext.setModule(compiledModule);
            buildContext.moduleDependencyPathMap.put(compiledModule.packageID, new ExecutableJar());
        } else {
            MultiModuleContext multiModuleContext = buildContext.get(BuildContextField.SOURCE_CONTEXT);
            List<BLangPackage> compiledModules = compiler.compilePackages(true);
            if (compiledModules.size() == 0) {
                throw createLauncherException("no modules found to compile.");
            }
            multiModuleContext.setModules(compiledModules);
            for (BLangPackage bLangPackage: compiledModules) {
                buildContext.moduleDependencyPathMap.put(bLangPackage.packageID, new ExecutableJar());
            }
        }
        
        // check if there are any build errors
        List<BLangPackage> modules = buildContext.getModules();
        for (BLangPackage module : modules) {
            if (module.diagCollector.hasErrors()) {
                throw createLauncherException("compilation contains errors");
            }
        }
        
        // update build context.
        buildContext.put(BuildContextField.COMPILER_CONTEXT, context);
    }
}
