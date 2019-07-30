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
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Task to copy the executable to a given location. This requires the {@link CreateExecutableTask} to be completed.
 */
public class CopyExecutableTask implements Task {
    private Path outputFileOrDirectoryName;
    
    /**
     * Creates task to copy the executable.
     *
     * @param outputFileOrDirectoryName Location of the executable to be copied.
     */
    public CopyExecutableTask(Path outputFileOrDirectoryName) {
        this.outputFileOrDirectoryName = outputFileOrDirectoryName;
    }
    
    @Override
    public void execute(BuildContext buildContext) {
        try {
            for (BLangPackage module : buildContext.getModules()) {
                Path executableFile = buildContext.getExecutablePathFromTarget(module.packageID);
                
                // if the given output path is a directory, copy the executable to the given directory. name of the
                // executable is not changed.
                if (Files.isDirectory(this.outputFileOrDirectoryName)) {
                    // create output directory if it does not exists
                    if (Files.notExists(this.outputFileOrDirectoryName)) {
                        Files.createDirectories(this.outputFileOrDirectoryName);
                    }
    
                    // this 'if' is to avoid spot bugs
                    Path executableFileName = executableFile.getFileName();
                    if (null != executableFileName) {
                        this.outputFileOrDirectoryName = this.outputFileOrDirectoryName.resolve(
                                executableFileName.toString());
                    }
                }
                
                // check if the output path is a file
                if (Files.isRegularFile(this.outputFileOrDirectoryName)) {
                    // check if the output file has the .jar extension.
                    if (!this.outputFileOrDirectoryName.toString().endsWith(
                            ProjectDirConstants.BLANG_COMPILED_JAR_EXT)) {
                        throw new BLangCompilerException("output executable should end with '" +
                                                         ProjectDirConstants.BLANG_COMPILED_JAR_EXT + "' extension.");
                    }
                    
                    // if the given path is not an absolute path. copy the executable to the source root.
                    if (!this.outputFileOrDirectoryName.isAbsolute()) {
                        Path sourceRoot = buildContext.get(BuildContextField.SOURCE_ROOT);
                        this.outputFileOrDirectoryName = sourceRoot.resolve(this.outputFileOrDirectoryName);
                    }
                }
                
                // copy the executable. replace the existing executable if exists.
                Files.copy(executableFile, this.outputFileOrDirectoryName, StandardCopyOption.REPLACE_EXISTING);
                
                // update executable location and target dir
                // this is to avoid spotbugs
                Path executableDir = this.outputFileOrDirectoryName.getParent();
                if (null != executableDir) {
                    buildContext.updateExecutableDir(executableDir);
                }
            }
        } catch (IOException e) {
            throw new BLangCompilerException("error occurred copying executable: " + e.getMessage());
        }
    }
}
