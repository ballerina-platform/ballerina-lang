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

import org.ballerinalang.compiler.plugins.CompilerPlugin;
import org.ballerinalang.packerina.buildcontext.BuildContext;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;

import java.util.ServiceLoader;

/**
 * Task for running compiler plugins.
 */
public class RunCompilerPluginTask implements Task {
    @Override
    public void execute(BuildContext buildContext) {
        ServiceLoader<CompilerPlugin> processorServiceLoader = ServiceLoader.load(CompilerPlugin.class);
        for (BLangPackage module : buildContext.getModules()) {
            for (CompilerPlugin plugin : processorServiceLoader) {
                plugin.codeGenerated(module.packageID, buildContext.getExecutablePathFromTarget(module.packageID));
            }
        }
    }
}
