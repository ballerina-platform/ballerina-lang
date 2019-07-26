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

import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.compiler.plugins.CompilerPlugin;
import org.ballerinalang.packerina.BuilderUtils;
import org.ballerinalang.packerina.buildcontext.BuildContext;
import org.ballerinalang.packerina.buildcontext.BuildContextField;
import org.ballerinalang.packerina.buildcontext.sourcecontext.MultiModuleContext;
import org.ballerinalang.packerina.buildcontext.sourcecontext.SingleFileContext;
import org.ballerinalang.packerina.buildcontext.sourcecontext.SingleModuleContext;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;

import java.nio.file.Path;
import java.util.ServiceLoader;

/**
 * Task for running compiler plugins.
 */
public class RunCompilerPluginTask implements Task {
    @Override
    public void execute(BuildContext buildContext) {
        ServiceLoader<CompilerPlugin> processorServiceLoader = ServiceLoader.load(CompilerPlugin.class);
        switch (buildContext.getSourceType()) {
            case BAL_FILE:
                SingleFileContext singleFileContext = buildContext.get(BuildContextField.SOURCE_CONTEXT);
                Path singleBalExecutableFile = BuilderUtils.resolveExecutablePath(buildContext,
                        singleFileContext.getModule().packageID);
                
                processorServiceLoader.forEach(plugin ->
                        plugin.codeGenerated(singleFileContext.getModule().packageID, singleBalExecutableFile));
                break;
            case SINGLE_MODULE:
                SingleModuleContext moduleContext = buildContext.get(BuildContextField.SOURCE_CONTEXT);
                Path singleModuleExecutableFile = BuilderUtils.resolveExecutablePath(buildContext,
                        moduleContext.getModule().packageID);
                processorServiceLoader.forEach(plugin ->
                        plugin.codeGenerated(moduleContext.getModule().packageID, singleModuleExecutableFile));
                break;
            case ALL_MODULES:
                MultiModuleContext multiModuleContext = buildContext.get(BuildContextField.SOURCE_CONTEXT);
                for (BLangPackage module : multiModuleContext.getModules()) {
                    Path moduleExecutableFile = BuilderUtils.resolveExecutablePath(buildContext, module.packageID);
                    processorServiceLoader.forEach(plugin ->
                            plugin.codeGenerated(module.packageID, moduleExecutableFile));
                }
                break;
            default:
                throw new BLangCompilerException("unable to run compiler plugins for build source");
        }
    }
}
