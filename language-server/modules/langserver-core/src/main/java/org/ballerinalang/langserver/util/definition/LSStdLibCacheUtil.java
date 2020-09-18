/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.util.definition;

import org.ballerinalang.compiler.CompilerOptionName;
import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.compiler.LSCompilerUtil;
import org.ballerinalang.langserver.exception.LSStdlibCacheException;
import org.ballerinalang.model.elements.PackageID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerinalang.compiler.Compiler;
import org.wso2.ballerinalang.compiler.FileSystemProjectDirectory;
import org.wso2.ballerinalang.compiler.SourceDirectory;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.ballerinalang.compiler.CompilerOptionName.COMPILER_PHASE;
import static org.ballerinalang.compiler.CompilerOptionName.OFFLINE;
import static org.ballerinalang.compiler.CompilerOptionName.PRESERVE_WHITESPACE;
import static org.ballerinalang.compiler.CompilerOptionName.PROJECT_DIR;

/**
 * Utility operations for the standard library cache.
 *
 * @since 1.2.0
 */
public class LSStdLibCacheUtil {
    private static final String ZIPENTRY_FILE_SEPARATOR = "/";
    private static final String DIR_VALIDATION_PATTERN = ProjectDirConstants.SOURCE_DIR_NAME
            + ZIPENTRY_FILE_SEPARATOR + ProjectDirConstants.SOURCE_DIR_NAME + ZIPENTRY_FILE_SEPARATOR;
    private static final String TOML_CONTENT = "[project]\norg-name= \"lsbalorg\"\nversion=\"1.1.0\"\n\n[dependencies]";
    private static final Logger logger = LoggerFactory.getLogger(LSStdLibCacheUtil.class);
    static final Path STD_LIB_SOURCE_ROOT = Paths.get(CommonUtil.BALLERINA_HOME).resolve("lib").resolve("repo");

    private LSStdLibCacheUtil() {
    }

    protected static void extractSourceForImportModule(String orgName, BLangImportPackage module)
            throws LSStdlibCacheException {
        String moduleName = module.getPackageName().stream()
                .map(BLangIdentifier::getValue)
                .collect(Collectors.joining("."));
        String version = module.getPackageVersion().getValue().isEmpty()
                ? ProjectDirConstants.BLANG_PKG_DEFAULT_VERSION
                : module.getPackageVersion().getValue();
        String cacheableKey = getCacheableKey(module);
        Path baloPath = STD_LIB_SOURCE_ROOT.resolve(orgName).resolve(moduleName).resolve(version).resolve(moduleName
                + ProjectDirConstants.BLANG_COMPILED_PKG_EXT);
        Path destinationRoot = CommonUtil.LS_STDLIB_CACHE_DIR.resolve(cacheableKey)
                .resolve(ProjectDirConstants.SOURCE_DIR_NAME);
        extract(baloPath, destinationRoot, moduleName, cacheableKey);
    }

    protected static void extractSourceForModule(String orgName, String moduleName, String version)
            throws LSStdlibCacheException {
        /*
        Cacheable key format is as follows
        Eg: orgName_moduleName_dot.separated.version
         */
        Path modulePath = STD_LIB_SOURCE_ROOT.resolve(orgName).resolve(moduleName);
        String cacheableKey = orgName + "_" + moduleName + "_" + version;
        Path baloPath = modulePath.resolve(version).resolve(moduleName
                + ProjectDirConstants.BLANG_COMPILED_PKG_EXT);
        Path destinationRoot = CommonUtil.LS_STDLIB_CACHE_DIR.resolve(cacheableKey)
                .resolve(ProjectDirConstants.SOURCE_DIR_NAME);
        extract(baloPath, destinationRoot, moduleName, cacheableKey);
    }

    public static void extract(Path baloPath, Path destinationRoot, String moduleName, String cacheableKey)
            throws LSStdlibCacheException {
        Path rootParent = destinationRoot.getParent();
        if (rootParent == null) {
            return;
        }
        Path tomlFile = rootParent.resolve(ProjectDirConstants.MANIFEST_FILE_NAME);
        FileInputStream baloFileInputStream;
        if (Files.exists(tomlFile)) {
            // Already extracted
            return;
        }
        if (!Files.exists(baloPath)) {
            throw new LSStdlibCacheException("Invalid module source root provided for module: ["
                    + cacheableKey + "]");
        }

        // Create the new extract root
        Path destinationPath;
        try {
            createProjectForModule(cacheableKey);
            destinationPath = Files.createDirectories(destinationRoot.resolve(cacheableKey));
        } catch (IOException e) {
            throw new LSStdlibCacheException(e.getMessage(), e);
        }

        String moduleDirValidationPattern = DIR_VALIDATION_PATTERN + moduleName + ZIPENTRY_FILE_SEPARATOR;
        byte[] buffer = new byte[1024];
        try {
            baloFileInputStream = new FileInputStream(baloPath.toAbsolutePath().toString());
        } catch (FileNotFoundException e) {
            throw new LSStdlibCacheException(e.getMessage(), e);
        }

        try (ZipInputStream zis = new ZipInputStream(baloFileInputStream)) {
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                String fileName = zipEntry.getName().replace(moduleDirValidationPattern, "");
                if (zipEntry.isDirectory()) {
                    if (!zipEntry.getName().equals(moduleDirValidationPattern)
                            && zipEntry.getName().startsWith(moduleDirValidationPattern)) {
                        Files.createDirectories(destinationPath.resolve(fileName));
                    }
                    zipEntry = zis.getNextEntry();
                    continue;
                }
                if (!fileName.endsWith(ProjectDirConstants.BLANG_COMPILED_PKG_BIR_EXT)
                        && !fileName.endsWith(ProjectDirConstants.MODULE_MD_FILE_NAME)) {
                    File newFile = destinationPath.resolve(fileName).toFile();
                    int length;
                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        while ((length = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, length);
                        }
                        boolean readOnly = newFile.setReadOnly();
                        if (!readOnly) {
                            logger.warn("Failed to set the cached source read only");
                        }
                    }
                }
                zipEntry = zis.getNextEntry();
            }
        } catch (IOException e) {
            throw new LSStdlibCacheException(e.getMessage(), e);
        } finally {
            try {
                baloFileInputStream.close();
            } catch (IOException e) {
                // Ignore
            }
        }
    }

    /**
     * Get the Computed Cache Entry Name.
     *
     * @param module import module
     * @return {@link String} computed cache entry name
     */
    protected static synchronized String getCacheableKey(BLangImportPackage module) {
        String moduleName = module.getPackageName().stream()
                .map(BLangIdentifier::getValue)
                .collect(Collectors.joining("."));
        String version = module.getPackageVersion().getValue();
        String orgName = module.orgName.value;

        return getCacheableKey(orgName, moduleName, version);
    }

    /**
     * Get the cache entry name.
     *
     * @param packageID Package ID instance to evaluate
     * @return {@link String} computed cache entry name
     */
    protected static synchronized String getCacheableKey(PackageID packageID) {
        String moduleName = packageID.getNameComps().stream()
                .map(Name::getValue)
                .collect(Collectors.joining("."));
        String version = packageID.getPackageVersion().getValue();
        String orgName = packageID.orgName.value;

        return getCacheableKey(orgName, moduleName, version);
    }

    public static String getCacheableKey(String orgName, String moduleName, String version) {
        return orgName + "_" + moduleName + "_" +
                (version.isEmpty() ? ProjectDirConstants.BLANG_PKG_DEFAULT_VERSION : version);
    }

    /**
     * Create the project for the given module.
     *
     * @param projectDir this is the cacheable key
     * @throws IOException error while creating the project module
     */
    private static void createProjectForModule(String projectDir) throws IOException {
        Path cachedProjectPath = CommonUtil.LS_STDLIB_CACHE_DIR.resolve(projectDir);
        Path projectPath = Files.createDirectories(cachedProjectPath);
        Path manifestPath = projectPath.resolve(ProjectDirConstants.MANIFEST_FILE_NAME);
        Files.write(manifestPath, Collections.singletonList(TOML_CONTENT));
    }

    static String readModuleVersionFromDir(Path moduleDir) throws LSStdlibCacheException {
        String[] filesList = moduleDir.toFile().list();
        if (filesList == null || filesList.length > 1 || (filesList.length > 0
                && !filesList[0].matches("([0-9]*\\.[0-9]*\\.[0-9]*)"))) {
            throw new LSStdlibCacheException("Could not find a matching version directory");
        }

        return filesList[0];
    }

    private static CompilerContext createNewCompilerContext(String projectDir) {
        CompilerContext context = new CompilerContext();
        CompilerOptions options = CompilerOptions.getInstance(context);
        options.put(PROJECT_DIR, projectDir);
        options.put(COMPILER_PHASE, CompilerPhase.DESUGAR.toString());
        options.put(PRESERVE_WHITESPACE, Boolean.toString(false));
        options.put(OFFLINE, Boolean.toString(true));
        options.put(CompilerOptionName.EXPERIMENTAL_FEATURES_ENABLED, Boolean.toString(true));
        context.put(SourceDirectory.class, new FileSystemProjectDirectory(Paths.get(projectDir)));
        return context;
    }

    public static Compiler getCompiler(String projectDir) throws UnsupportedEncodingException {
        Compiler compiler = Compiler.getInstance(createNewCompilerContext(projectDir));
        compiler.setOutStream(new LSCompilerUtil.EmptyPrintStream());
        compiler.setErrorStream(new LSCompilerUtil.EmptyPrintStream());
        return compiler;
    }

    public static Compiler getCompiler(CompilerContext compilerContext) throws UnsupportedEncodingException {
        Compiler compiler = Compiler.getInstance(compilerContext);
        compiler.setOutStream(new LSCompilerUtil.EmptyPrintStream());
        compiler.setErrorStream(new LSCompilerUtil.EmptyPrintStream());
        return compiler;
    }
}
