/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.langserver.compiler;

import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentManager;
import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentManagerImpl;
import org.ballerinalang.langserver.compiler.workspace.repository.LSPathConverter;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.repository.CompiledPackage;
import org.wso2.ballerinalang.compiler.PackageCache;
import org.wso2.ballerinalang.compiler.SourceDirectory;
import org.wso2.ballerinalang.compiler.packaging.converters.Converter;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

import static org.ballerinalang.compiler.CompilerOptionName.COMPILER_PHASE;
import static org.ballerinalang.compiler.CompilerOptionName.OFFLINE;
import static org.ballerinalang.compiler.CompilerOptionName.PRESERVE_WHITESPACE;
import static org.ballerinalang.compiler.CompilerOptionName.PROJECT_DIR;

/**
 * This class will hold compiler contexts against the project directory.
 */
public class LSContextManager {
    private static final LSContextManager INSTANCE = new LSContextManager();
    private static final String BUILT_IN_PACKAGES_PROJ_DIR = "$builtInPackagesProjectDir";
    private static final int MAX_COMPILATION_COUNT = 200;
    private static volatile int compilationCounter = 0;

    private final Map<String, CompilerContext> contextMap;

    public static LSContextManager getInstance() {
        return INSTANCE;
    }

    private LSContextManager() {
        contextMap = new ConcurrentHashMap<>();
    }

    /**
     * Returns a unique compiler context for the project directory path.
     *
     * @param packageID       package ID or null
     * @param projectDir      project directory path
     * @param documentManager {@link WorkspaceDocumentManager} Document Manager
     * @return compiler context
     */
    public CompilerContext getCompilerContext(@Nullable PackageID packageID, String projectDir,
                                              WorkspaceDocumentManager documentManager) {
        CompilerContext compilerContext = contextMap.get(projectDir);

        // TODO: Remove this fix once proper compiler fix is introduced
        if (compilationCounter > MAX_COMPILATION_COUNT) {
            compilerContext = null;
            compilationCounter = 0;
            LSClientLogger.logTrace("CompilationContext for {projectRoot: '" + projectDir +
                    "'}, has been reinitialized");
        }

        // TODO: Remove the enable new parser check later.
        if (compilerContext == null) {
            synchronized (LSContextManager.class) {
                compilerContext = contextMap.get(projectDir);
                if (compilerContext == null) {
                    compilerContext = this.createNewCompilerContext(projectDir, documentManager);
                    contextMap.put(projectDir, compilerContext);
                }
            }
        }
        clearCurrentPackage(packageID, compilerContext);
        compilationCounter++; // Not needed to be atomic since the if-check is a range
        return compilerContext;
    }

    /**
     * Set compiler context for a given project directory.
     *
     * @param projectDir      project directory
     * @param compilerContext compiler context.
     */
    public void setCompilerContext(String projectDir, CompilerContext compilerContext) {
        contextMap.put(projectDir, compilerContext);
    }

    /**
     * Remove a compiler context by project directory.
     *
     * @param projectDir project directory
     */
    public void removeCompilerContext(String projectDir) {
        contextMap.remove(projectDir);
    }

    /**
     * Clear all compiler contexts.
     */
    public void clearAllContexts() {
        contextMap.clear();
    }

    /**
     * Returns a global singleton compiler context for the static builtin packages.
     *
     * @return compiler context
     */
    public CompilerContext getBuiltInPackagesCompilerContext() {
        //TODO: Revisit explicitly retrieving WorkspaceDocumentManagerImpl as doc manager
        return getCompilerContext(null, BUILT_IN_PACKAGES_PROJ_DIR, WorkspaceDocumentManagerImpl.getInstance());
    }

    public CompilerContext createNewCompilerContext(String projectDir, WorkspaceDocumentManager documentManager) {
        CompilerContext context = new CompilerContext();
        CompilerOptions options = CompilerOptions.getInstance(context);
        options.put(PROJECT_DIR, projectDir);
        options.put(COMPILER_PHASE, CompilerPhase.DESUGAR.toString());
        options.put(PRESERVE_WHITESPACE, Boolean.toString(true));
        options.put(OFFLINE, Boolean.toString(true));
        context.put(SourceDirectory.class, new NullSourceDirectory(Paths.get(projectDir), documentManager));
        return context;
    }

    private static void clearCurrentPackage(@Nullable PackageID packageID, CompilerContext context) {
        LSPackageCache instance = LSPackageCache.getInstance(context);
        //Remove current package from cache
        if (packageID != null) {
            instance.invalidate(packageID);
        }
        //Set the package local cache into current context
        PackageCache.setInstance(instance.getPackageCache(), context);
    }

    /**
     * Null source directory.
     */
    public static class NullSourceDirectory implements SourceDirectory {
        private final Path projectDir;
        private final WorkspaceDocumentManager documentManager;

        public NullSourceDirectory(Path projectDir, WorkspaceDocumentManager documentManager) {
            this.projectDir = projectDir;
            this.documentManager = documentManager;
        }

        @Override
        public boolean canHandle(Path dirPath) {
            return true;
        }

        @Override
        public Path getPath() {
            return null;
        }

        @Override
        public List<String> getSourceFileNames() {
            return Collections.emptyList();
        }

        @Override
        public List<String> getSourcePackageNames() {
            return Collections.emptyList();
        }

        @Override
        public InputStream getManifestContent() {
            return new ByteArrayInputStream("".getBytes(Charset.defaultCharset()));
        }

        @Override
        public InputStream getLockFileContent() {
            return new ByteArrayInputStream("".getBytes(Charset.defaultCharset()));
        }

        @Override
        public Path saveCompiledProgram(InputStream source, String fileName) {
            return null;
        }

        @Override
        public void saveCompiledPackage(CompiledPackage compiledPackage, Path dirPath, String fileName) throws
                IOException {

        }

        @Override
        public Converter<Path> getConverter() {
            return new LSPathConverter(projectDir, documentManager);
        }
    }
}
