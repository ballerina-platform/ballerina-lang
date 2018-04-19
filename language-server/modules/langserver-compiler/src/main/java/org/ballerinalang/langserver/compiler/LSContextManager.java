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
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.repository.CompiledPackage;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.ballerinalang.util.diagnostic.DiagnosticListener;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;

import static org.ballerinalang.compiler.CompilerOptionName.COMPILER_PHASE;
import static org.ballerinalang.compiler.CompilerOptionName.PRESERVE_WHITESPACE;
import static org.ballerinalang.compiler.CompilerOptionName.PROJECT_DIR;

/**
 * This class will hold compiler contexts against the project directory.
 */
public class LSContextManager {
    private static final LSContextManager INSTANCE = new LSContextManager();
    private static final String BUILT_IN_PACKAGES_PROJ_DIR = "$builtInPackagesProjectDir";

    private final Map<String, CompilerContext> contextMap;

    public static LSContextManager getInstance() {
        return INSTANCE;
    }

    private LSContextManager() {
        contextMap = new ConcurrentHashMap<>();
    }

    /**
     * Returns a unique compiler context for the provided project directory path.
     *
     * Use this method if you already have the current project ID. Built packages for the project Id will be removed
     * from the PackageCache from the compiler context.
     *
     * @param packageID  package ID
     * @param projectDir project directory path
     * @return compiler context
     */
    public CompilerContext getCompilerContext(PackageID packageID, String projectDir) {
        return getCompilerContext(packageID, projectDir, true);
    }

    /**
     * Returns a unique compiler context for the provided project directory path.
     *
     * Use this method if you don't have the current project ID.
     *
     * @param projectDir project directory path
     * @return compiler context
     */
    public CompilerContext getCompilerContext(String projectDir) {
        return getCompilerContext(null, projectDir, true);
    }

    /**
     * Returns a unique compiler context for the project directory path.
     *
     * @param packageID         package ID or null
     * @param projectDir        project directory path
     * @param createIfNotExists if true creates a new compiler context if not exists
     * @return compiler context
     */
    public CompilerContext getCompilerContext(@Nullable PackageID packageID, String projectDir,
                                              boolean createIfNotExists) {
        CompilerContext compilerContext = contextMap.get(projectDir);
        if (compilerContext == null && createIfNotExists) {
            synchronized (LSContextManager.class) {
                compilerContext = contextMap.get(projectDir);
                if (compilerContext == null) {
                    compilerContext = createNewCompilerContext(packageID);
                    contextMap.put(projectDir, compilerContext);
                }
            }
        }
        clearCurrentPackage(packageID, compilerContext);
        return compilerContext;
    }

    /**
     * Set compiler context for a given project directory
     *
     * @param projectDir        project directory
     * @param compilerContext   compiler context.
     */
    public void setCompilerContext(String projectDir, CompilerContext compilerContext) {
        contextMap.put(projectDir, compilerContext);
    }

    /**
     * Remove a compiler context by project directory.
     *
     * @param projectDir        project directory
     * @param compilerContext   compiler context
     */
    public void removeCompilerContext(String projectDir, CompilerContext compilerContext) {
        contextMap.remove(projectDir);
    }

    /**
     * Clear all compiler contexts.
     */
    public void clearAllContexts() {
        contextMap.clear();
    }

    /**
     * Returns an unique temporary compiler context.
     *
     * @return compiler context
     */
    public static CompilerContext createTempCompilerContext() {
        return createNewCompilerContext(null);
    }

    /**
     * Returns a global singleton compiler context for the static builtin packages.
     *
     * @return compiler context
     */
    public CompilerContext getBuiltInPackagesCompilerContext() {
        return getCompilerContext(null, BUILT_IN_PACKAGES_PROJ_DIR);
    }

    private static CompilerContext createNewCompilerContext(@Nullable PackageID packageID) {
        CompilerContext context = new CompilerContext();
        CompilerOptions options = CompilerOptions.getInstance(context);
        options.put(PROJECT_DIR, "");
        options.put(COMPILER_PHASE, CompilerPhase.DESUGAR.toString());
        options.put(PRESERVE_WHITESPACE, "false");
        context.put(SourceDirectory.class, new NullSourceDirectory());
        List<Diagnostic> balDiagnostics = new ArrayList<>();
        CollectDiagnosticListener diagnosticListener = new CollectDiagnosticListener(balDiagnostics);
        context.put(DiagnosticListener.class, diagnosticListener);
        return context;
    }

    private static void clearCurrentPackage(@Nullable PackageID packageID, CompilerContext context) {
        LSPackageCache instance = LSPackageCache.getInstance(context);
        //Remove current package from cache
        if (packageID != null) {
            instance.removePackage(packageID);
        }
        //Set the package local cache into current context
        PackageCache.setInstance(instance.getPackageCache(), context);
    }

    /**
     * Null source directory.
     */
    public static class NullSourceDirectory implements SourceDirectory {
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
            return null;
        }

        @Override
        public Path saveCompiledProgram(InputStream source, String fileName) {
            return null;
        }

        @Override
        public void saveCompiledPackage(CompiledPackage compiledPackage, Path dirPath, String fileName) throws IOException {

        }

        @Override
        public Converter<Path> getConverter() {
            return null;
        }
    }
}
