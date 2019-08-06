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
import org.wso2.ballerinalang.compiler.tree.BLangPackage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static org.ballerinalang.tool.LauncherUtils.createLauncherException;

/**
 * Task to copy the executable to a given location. This requires the {@link CreateExecutableTask} to be completed.
 */
public class CopyExecutableTask implements Task {
    private Path outputPath;
    
    /**
     * Creates task to copy the executable.
     *
     * @param outputPath Location of the executable to be copied.
     */
    public CopyExecutableTask(Path outputPath) {
        this.outputPath = outputPath;
    }
    
    @Override
    public void execute(BuildContext buildContext) {
        try {
            for (BLangPackage module : buildContext.getModules()) {
                Path executableFile = buildContext.getExecutablePathFromTarget(module.packageID);
                
                // if the given output path is a directory, copy the executable to the given directory. name of the
                // executable is not changed.
                if (Files.isDirectory(this.outputPath)) {
                    // create output directory if it does not exists
                    if (Files.notExists(this.outputPath)) {
                        Files.createDirectories(this.outputPath);
                    }
    
                    // this 'if' is to avoid spot bugs
                    Path executableFileName = executableFile.getFileName();
                    if (null != executableFileName) {
                        this.outputPath = this.outputPath.resolve(
                                executableFileName.toString());
                    }
                }
                
                // check if the output path is a file
                if (Files.isRegularFile(this.outputPath)) {
                    // if the given path is not an absolute path. copy the executable to the source root.
                    if (!this.outputPath.isAbsolute()) {
                        Path sourceRoot = buildContext.get(BuildContextField.SOURCE_ROOT);
                        this.outputPath = sourceRoot.resolve(this.outputPath);
                    }
                }
                
                // copy the executable. replace the existing executable if exists.
                Files.copy(executableFile, this.outputPath, StandardCopyOption.REPLACE_EXISTING);
                
                // update executable location and target dir
                // this 'if' is to avoid spotbugs
                Path executableDir = this.outputPath.getParent();
                if (null != executableDir) {
                    buildContext.updateExecutableDir(executableDir);
                }
            }
        } catch (IOException e) {
            throw createLauncherException("unable to copying executable: " + e.getMessage());
        }
    }
}
