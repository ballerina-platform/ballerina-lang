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
import org.ballerinalang.packerina.buildcontext.sourcecontext.SingleFileContext;
import org.ballerinalang.packerina.utils.FileUtils;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import static org.ballerinalang.packerina.buildcontext.sourcecontext.SourceType.SINGLE_BAL_FILE;
import static org.ballerinalang.tool.LauncherUtils.createLauncherException;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BLANG_COMPILED_JAR_EXT;

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
                Path sourceRoot = buildContext.get(BuildContextField.SOURCE_ROOT);
                
                if (this.outputPath.isAbsolute()) {
                    if (Files.isDirectory(this.outputPath)) {
                        if (Files.notExists(this.outputPath)) {
                            Files.createDirectories(this.outputPath);
                        }
                        
                        Path executableFileName = executableFile.getFileName();
                        if (null != executableFileName) {
                            this.outputPath = this.outputPath.resolve(executableFileName.toString());
                        }
                    } else {
                        if (!this.outputPath.toString().endsWith(BLANG_COMPILED_JAR_EXT)) {
                            this.outputPath = Paths.get(this.outputPath.toString() + BLANG_COMPILED_JAR_EXT);
                        }
                    }
                } else {
                    if (FileUtils.hasExtension(this.outputPath)) {
                        this.outputPath = sourceRoot.resolve(this.outputPath);
                    } else {
                        this.outputPath = sourceRoot.resolve(this.outputPath + BLANG_COMPILED_JAR_EXT);
                    }
                }
                
                // copy the executable. replace the existing executable if exists.
                Files.copy(executableFile, this.outputPath, StandardCopyOption.REPLACE_EXISTING);
                
                // update executable location and target dir
                // this 'if' is to avoid spotbugs
                Path executableDir = this.outputPath.getParent();
                if (null != executableDir) {
                    buildContext.updateExecutableDir(executableDir);
                    if (SINGLE_BAL_FILE == buildContext.getSourceType()) {
                        SingleFileContext singleFileContext = buildContext.get(BuildContextField.SOURCE_CONTEXT);
                        singleFileContext.setExecutableFilePath(this.outputPath);
                    }
                }
            }
        } catch (IOException e) {
            throw createLauncherException("unable to copying executable: " + e.getMessage());
        }
    }
}
