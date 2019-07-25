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
            if (Files.notExists(targetDir)) {
                Files.createDirectories(targetDir);
            }
            
            
//            // create '<target>/cache/jar_cache' dir
//            Path jarCache = targetDir
//                    .resolve(ProjectDirConstants.CACHES_DIR_NAME)
//                    .resolve(ProjectDirConstants.JAR_CACHE_DIR_NAME);
//
//            if (!Files.exists(jarCache)) {
//                Files.createDirectories(jarCache);
//            }
//
//            buildContext.put(BuildContextField.JAR_CACHE_DIR, jarCache);
        } catch (IOException e) {
            throw new BLangCompilerException("error occurred in creating target directory: " + targetDir);
        }
    }
}
