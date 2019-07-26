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
import org.ballerinalang.packerina.buildcontext.BuildContext;
import org.ballerinalang.packerina.buildcontext.BuildContextField;
import org.ballerinalang.packerina.buildcontext.sourcecontext.MultiModuleContext;
import org.ballerinalang.packerina.buildcontext.sourcecontext.SingleModuleContext;
import org.ballerinalang.packerina.buildcontext.sourcecontext.SourceType;
import org.ballerinalang.packerina.writer.BaloFileWriter;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

/**
 * Task for creating balo file. Balo file writer is meant for modules only and not for single files.
 */
public class CreateBaloTask implements Task {
    @Override
    public void execute(BuildContext buildContext) {
        List<BLangPackage> modules = new LinkedList<>();
        if (buildContext.getSourceType() == SourceType.BAL_FILE) {
            return;
        } else if (buildContext.getSourceType() == SourceType.SINGLE_MODULE) {
            SingleModuleContext moduleContext = buildContext.get(BuildContextField.SOURCE_CONTEXT);
            modules.add(moduleContext.getModule());
        } else {
            MultiModuleContext multiModuleContext = buildContext.get(BuildContextField.SOURCE_CONTEXT);
            modules = multiModuleContext.getModules();
        }
        
        CompilerContext context = buildContext.get(BuildContextField.COMPILER_CONTEXT);
        Path targetDir = buildContext.get(BuildContextField.TARGET_DIR);
        try {
            // create '<target>/cache/balo_cache' dir
            Path baloCacheDir =
                    targetDir.resolve(ProjectDirConstants.CACHES_DIR_NAME)
                            .resolve(ProjectDirConstants.BALO_CACHE_DIR_NAME);
            if (Files.notExists(baloCacheDir)) {
                Files.createDirectories(baloCacheDir);
            }
    
            // add balo_cache directory to build context.
            buildContext.put(BuildContextField.BALO_CACHE_DIR, baloCacheDir);
    
            // generate balo for each module.
            BaloFileWriter baloWriter = BaloFileWriter.getInstance(context);
            modules.forEach(module -> baloWriter.write(module, buildContext));
        } catch (IOException e) {
            throw new BLangCompilerException("error occurred creating balo_cache: " + targetDir);
        }
    }
}
