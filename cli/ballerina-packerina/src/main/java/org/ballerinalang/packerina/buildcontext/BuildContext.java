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
import org.ballerinalang.packerina.buildcontext.cachecontext.ArtifactsCache;
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

import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.TARGET_DIR_NAME;
import static org.wso2.ballerinalang.util.RepoUtils.BALLERINA_INSTALL_DIR_PROP;


/**
 * Context to be passed to tasks when they get executed.
 */
public class BuildContext extends HashMap<BuildContextField, Object> {
    private static final long serialVersionUID = 6363519534259706585L;
    private SourceType srcType;
    
    
    /**
     * Create a build context with context fields.
     *
     * @param sourceRootPath The root of the source files. If its a project then its project root. If its a single bal
     *                       file then is it the parent directory of the bal file.
     * @param targetPath     The location of the target(build artifacts output path).
     * @param source         The name of the source file or the name of the module. Pass null to build all modules.
     */
    public BuildContext(Path sourceRootPath, Path targetPath, Path source) {
        if (Files.exists(sourceRootPath)) {
            // set source root
            this.put(BuildContextField.SOURCE_ROOT, sourceRootPath);
            
            // set target dir
            this.put(BuildContextField.TARGET_DIR, targetPath);
            
            // set build artifacts location
            this.put(BuildContextField.ARTIFACTS_CACHE, new ArtifactsCache(this));
            
            // set source context
            this.setSource(source);
        } else {
            throw new BLangCompilerException("location of the source root does not exists: " + sourceRootPath);
        }
    }
    
    /**
     * Create a build context with context fields.
     *
     * @param sourceRootPath The root of the source files. If its a project then its project root. If its a single bal
     *                       file then is it the parent directory of the bal file.
     */
    public BuildContext(Path sourceRootPath) {
        this(sourceRootPath, null);
    }
    
    /**
     * Create a build context with context fields.
     *
     * @param sourceRootPath The root of the source files. If its a project then its project root. If its a single bal
     *                       file then is it the parent directory of the bal file.
     * @param source         The name of the source file or the name of the module.
     */
    public BuildContext(Path sourceRootPath, Path source) {
        this(sourceRootPath, sourceRootPath.resolve(TARGET_DIR_NAME), source);
    }
    
    /**
     * Get value from the build context.
     *
     * @param key The key of the value.
     * @param <T> The return type.
     * @return The casted value.
     */
    public <T> T get(BuildContextField key) {
        return (T) super.get(key);
    }
    
    /**
     * Sets the type of the source and it's context.
     *
     * @param source The path of the source.
     *               Pass absolute path of the source to build a single file or a module.
     *               Pass null build all modules.
     */
    public void setSource(Path source) {
        if (source == null) {
            this.put(BuildContextField.SOURCE_CONTEXT, new MultiModuleContext());
            this.srcType = SourceType.ALL_MODULES;
            return;
        }
    
        Path sourceRootPath = this.get(BuildContextField.SOURCE_ROOT);
        Path absoluteSourcePath = RepoUtils.isBallerinaProject(sourceRootPath) ?
                                  sourceRootPath.toAbsolutePath().resolve(ProjectDirConstants.SOURCE_DIR_NAME)
                                          .resolve(source) :
                                  sourceRootPath.toAbsolutePath().resolve(source);
        
        if (Files.isRegularFile(absoluteSourcePath) &&
            source.toString().endsWith(BLangConstants.BLANG_SRC_FILE_SUFFIX)) {
        
            this.put(BuildContextField.SOURCE_CONTEXT, new SingleFileContext(source));
            this.srcType = SourceType.BAL_FILE;
        } else if (Files.isDirectory(absoluteSourcePath) &&
                   !source.toString().endsWith(BLangConstants.BLANG_SRC_FILE_SUFFIX)) {
            
            // this 'if' is to avoid spotbugs
            Path moduleNameAsPath = source.getFileName();
            if (null != moduleNameAsPath) {
                String moduleName = moduleNameAsPath.toString();
                this.put(BuildContextField.SOURCE_CONTEXT, new SingleModuleContext(moduleName));
                this.srcType = SourceType.SINGLE_MODULE;
            }
        } else {
            throw new BLangCompilerException("invalid source type found: '" + source + "' at: " + sourceRootPath);
        }
    }
    
    public SourceType getSourceType() {
        return this.srcType;
    }
}
