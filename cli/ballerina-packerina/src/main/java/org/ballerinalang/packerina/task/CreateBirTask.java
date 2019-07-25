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
import org.ballerinalang.packerina.writer.BirFileWriter;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Task for creating bir.
 */
public class CreateBirTask implements Task {
    @Override
    public void execute(BuildContext buildContext) {
        CompilerContext context = buildContext.get(BuildContextField.COMPILER_CONTEXT);
        List<BLangPackage> modules = buildContext.get(BuildContextField.COMPILED_MODULES);
        Path targetDir = buildContext.get(BuildContextField.TARGET_DIR);
        
        try {
            // create '<target>/cache/bir_cache' dir
            Path birCacheDir = targetDir
                    .resolve(ProjectDirConstants.CACHES_DIR_NAME)
                    .resolve(ProjectDirConstants.BIR_CACHE_DIR_NAME);
            
            if (!Files.exists(birCacheDir)) {
                Files.createDirectories(birCacheDir);
            }
            
            // add bir_cache directory to build context
            buildContext.put(BuildContextField.BIR_CACHE_DIR, birCacheDir);
            
            // generate bir for modules
            BirFileWriter birFileWriter = BirFileWriter.getInstance(context);
            modules.forEach(birFileWriter::write);
        } catch (IOException e) {
            throw new BLangCompilerException("error occurred creating bir_cache: " + targetDir);
        }
    }
}
