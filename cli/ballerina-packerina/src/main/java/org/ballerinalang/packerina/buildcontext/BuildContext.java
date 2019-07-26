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

package org.ballerinalang.packerina.buildcontext;

import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.packerina.buildcontext.sourcecontext.MultiModuleContext;
import org.ballerinalang.packerina.buildcontext.sourcecontext.SingleFileContext;
import org.ballerinalang.packerina.buildcontext.sourcecontext.SingleModuleContext;
import org.ballerinalang.packerina.buildcontext.sourcecontext.SourceType;
import org.ballerinalang.util.BLangConstants;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;
import org.wso2.ballerinalang.util.RepoUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

import static org.wso2.ballerinalang.util.RepoUtils.BALLERINA_INSTALL_DIR_PROP;


/**
 * Context to be passed to tasks when they get executed.
 */
public class BuildContext extends HashMap<BuildContextField, Object> {
    private static final long serialVersionUID = 6363519534259706585L;
    private SourceType srcType;
    
    public BuildContext(Path sourceRootPath, String source) {
        if (Files.exists(sourceRootPath)) {
    
            // set source root
            this.put(BuildContextField.SOURCE_ROOT, sourceRootPath);
    
            // set home repo to build context
            this.put(BuildContextField.HOME_REPO, RepoUtils.createAndGetHomeReposPath());
            
            this.put(BuildContextField.HOME_BIR_CACHE_REPO, RepoUtils.createAndGetHomeReposPath()
                    .resolve(ProjectDirConstants.BIR_CACHE_DIR_NAME));
    
            this.put(BuildContextField.HOME_JAR_CACHE_REPO, RepoUtils.createAndGetHomeReposPath()
                    .resolve(ProjectDirConstants.JAR_CACHE_DIR_NAME));
            
            this.put(BuildContextField.SYSTEM_BIR_CACHE, Paths.get(System.getProperty(BALLERINA_INSTALL_DIR_PROP))
                    .resolve("bir-cache"));
            
    
            // set source context
            this.setSource(source);
        } else {
            throw new BLangCompilerException("location of the source root does not exists: " + sourceRootPath);
        }
    }
    
    public <T> T get(BuildContextField key) {
        return (T) super.get(key);
    }
    
    /**
     * Sets the type of the source and it's context.
     *
     * @param source The path of the source. If null it is considered as a project where all modules are built.
     */
    public void setSource(String source) {
        if (source == null) {
            this.put(BuildContextField.SOURCE_CONTEXT, new MultiModuleContext());
            this.srcType = SourceType.ALL_MODULES;
            return;
        }
    
        Path sourceRootPath = this.get(BuildContextField.SOURCE_ROOT);
        Path absoluteSourcePath = sourceRootPath.toAbsolutePath().resolve(source);
        if (Files.isRegularFile(absoluteSourcePath) &&
            source.endsWith(BLangConstants.BLANG_SRC_FILE_SUFFIX) &&
            !RepoUtils.isBallerinaProject(absoluteSourcePath)) {
        
            this.put(BuildContextField.SOURCE_CONTEXT, new SingleFileContext(Paths.get(source)));
            this.srcType = SourceType.BAL_FILE;
        }
        
        this.put(BuildContextField.SOURCE_CONTEXT, new SingleModuleContext(source));
        this.srcType = SourceType.BAL_FILE;
    }
    
    public SourceType getSourceType() {
        return this.srcType;
    }
}
