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
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Create the target directory.
 */
public class CreateTargetDirTask implements Task {
    
    @Override
    public void execute(BuildContext buildContext) {
        Path targetDir = buildContext.get(BuildContextField.TARGET_DIR);
        try {
            // create '<target>/cache/balo_cache' dir
            Path baloCacheDir = targetDir
                    .resolve(ProjectDirConstants.CACHES_DIR_NAME)
                    .resolve(ProjectDirConstants.BALO_CACHE_DIR_NAME);
            if (Files.notExists(baloCacheDir)) {
                Files.createDirectories(baloCacheDir);
            }
            
            // create '<target>/cache/bir_cache' dir
            Path birCacheDir = targetDir
                    .resolve(ProjectDirConstants.CACHES_DIR_NAME)
                    .resolve(ProjectDirConstants.BIR_CACHE_DIR_NAME);
            if (!Files.exists(birCacheDir)) {
                Files.createDirectories(birCacheDir);
            }
            
            // create '<target>/cache/jar_cache' dir
            Path jarCache = targetDir
                    .resolve(ProjectDirConstants.CACHES_DIR_NAME)
                    .resolve(ProjectDirConstants.JAR_CACHE_DIR_NAME);
            
            if (!Files.exists(jarCache)) {
                Files.createDirectories(jarCache);
            }
            
            buildContext.put(BuildContextField.BALO_CACHE_DIR, baloCacheDir);
            buildContext.put(BuildContextField.BIR_CACHE_DIR, birCacheDir);
            buildContext.put(BuildContextField.JAR_CACHE_DIR, jarCache);
        } catch (IOException e) {
            throw new BLangCompilerException("error occurred in creating artifacts output target path: " + targetDir);
        }
    }
}
