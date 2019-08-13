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
package org.ballerinalang.packerina.writer;

import com.moandjiezana.toml.TomlWriter;
import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.packerina.buildcontext.BuildContext;
import org.ballerinalang.packerina.buildcontext.BuildContextField;
import org.ballerinalang.packerina.model.BaloToml;
import org.ballerinalang.toml.model.Manifest;
import org.ballerinalang.toml.model.Module;
import org.ballerinalang.toml.parser.ManifestProcessor;
import org.wso2.ballerinalang.compiler.SourceDirectory;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;
import org.wso2.ballerinalang.compiler.util.ProjectDirs;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Generator and writer for module balo file.
 *
 * @since 1.0
 */
public class BaloFileWriter {
    private static final CompilerContext.Key<BaloFileWriter> MODULE_FILE_WRITER_KEY =
            new CompilerContext.Key<>();
    private final SourceDirectory sourceDirectory;
    private final Manifest manifest;
    private final BuildContext buildContext;
    
    public static BaloFileWriter getInstance(BuildContext buildContext) {
        CompilerContext context = buildContext.get(BuildContextField.COMPILER_CONTEXT);
        BaloFileWriter baloFileWriter = context.get(MODULE_FILE_WRITER_KEY);
        if (baloFileWriter == null) {
            baloFileWriter = new BaloFileWriter(buildContext);
        }
        return baloFileWriter;
    }

    private BaloFileWriter(BuildContext buildContext) {
        CompilerContext context = buildContext.get(BuildContextField.COMPILER_CONTEXT);
        context.put(MODULE_FILE_WRITER_KEY, this);
        this.buildContext = buildContext;
        this.sourceDirectory = context.get(SourceDirectory.class);
        if (this.sourceDirectory == null) {
            throw new IllegalArgumentException("source directory has not been initialized");
        }
        this.manifest = ManifestProcessor.getInstance(context).getManifest();
    }

    /**
     * Generate balo file for the given module.
     *
     * @param module ballerina module
     * @param baloFilePath path to the balo file
     */
    public void write(BLangPackage module, Path baloFilePath) {
        // Get the project directory
        Path projectDirectory = this.sourceDirectory.getPath();

        // Check if the module is part of the project
        String moduleName = module.packageID.name.value;
        if (!ProjectDirs.isModuleExist(projectDirectory, moduleName)) {
            return;
        }
        
        // Create the archive over write if exists
        try (FileSystem balo = createBaloArchive(baloFilePath)) {
            // Now lets put stuff in
            populateBaloArchive(balo, module);
            buildContext.out().println("Created " + projectDirectory.relativize(baloFilePath));
        } catch (IOException e) {
            // todo Check for permission
            throw new BLangCompilerException("Failed to create balo :" + e.getMessage(), e);
        } catch (BLangCompilerException be) {
            // clean up if an error occur
            try {
                Files.delete(baloFilePath);
            } catch (IOException e) {
                // We ignore this error and throw out the original blang compiler error to the user
            }
            throw be;
        }
    }

    private FileSystem createBaloArchive(Path path) throws IOException {
        // Remove if file already exists
        if (Files.exists(path)) {
            Files.delete(path);
        }
        // Define ZIP File System Properies
        Map<String, String> env = new HashMap<>();
        env.put("create", "true");
        env.put("encoding", "UTF-8");

        /* Locate File on disk for creation */
        URI zipDisk = URI.create("jar:" + path.toUri());
        /* Create ZIP file System */
        return FileSystems.newFileSystem(zipDisk, env);
    }

    private void populateBaloArchive(FileSystem balo, BLangPackage module) throws IOException {
        Path root = balo.getPath("/");
        Path projectDirectory = this.sourceDirectory.getPath();
        Path moduleSourceDir = projectDirectory.resolve(ProjectDirConstants.SOURCE_DIR_NAME)
                .resolve(module.packageID.name.value);
        String moduleName = module.packageID.name.value;
        // Now lets put stuff in according to spec
        // /
        // └─ metadata/
        //    └─ BALO.toml
        //    └─ MODULE.toml
        // └─ src/
        // └─ resources/
        // └─ platform-libs/
        // └─ docs/
        //    └─ MODULE-DESC.md
        //    └─ api-docs/

        addMetaData(root, moduleName);
        addModuleSource(root, moduleSourceDir, moduleName);
        addResources(root, moduleSourceDir);
        addModuleDoc(root, moduleSourceDir);
        addPlatformLibs(root, projectDirectory, moduleName);
    }

    private void addModuleDoc(Path root, Path moduleSourceDir) throws IOException {
        // create the docs directory in zip
        Path moduleMd = moduleSourceDir.resolve(ProjectDirConstants.MODULE_MD_FILE_NAME);
        Path docsDirInBalo = root.resolve(ProjectDirConstants.BALO_DOC_DIR_NAME);
        Path moduleMdInBalo = docsDirInBalo.resolve(ProjectDirConstants.MODULE_MD_FILE_NAME);
        Files.createDirectory(docsDirInBalo);

        if (Files.exists(moduleMd)) {
            Files.copy(moduleMd, moduleMdInBalo);
        }
    }

    private void addPlatformLibs(Path root, Path projectDirectory, String moduleName) throws IOException {
        //If platform libs are defined add them to balo
        if (null != manifest.getPlatform().libraries) {
            Path platformLibsDir = root.resolve(ProjectDirConstants.BALO_PLATFORM_LIB_DIR_NAME);
            Files.createDirectory(platformLibsDir);

            List<Path> libs = manifest.getPlatform().libraries.stream()
                    .filter(lib -> lib.getModules() == null || Arrays.asList(lib.getModules()).contains(moduleName))
                    .map(lib -> Paths.get(lib.getPath())).collect(Collectors.toList());

            for (Path lib : libs) {
                Path nativeFile = projectDirectory.resolve(lib);
                Path libFileName = lib.getFileName();
                if (null != libFileName) {
                    Path targetPath = platformLibsDir.resolve(libFileName.toString());
                    try {
                        Files.copy(nativeFile, targetPath, StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        throw new BLangCompilerException("Dependency jar not found : " + lib.toString());
                    }
                }
            }
        }
    }

    private void addResources(Path root, Path moduleSourceDir) throws IOException {
        // create the resources directory in zip
        Path resourceDir = moduleSourceDir.resolve(ProjectDirConstants.RESOURCE_DIR_NAME);
        Path resourceDirInBalo = root.resolve(ProjectDirConstants.RESOURCE_DIR_NAME);
        Files.createDirectory(resourceDirInBalo);

        if (Files.exists(resourceDir)) {
            // copy resources file from module directory path in to zip
            PathMatcher filter = FileSystems.getDefault().getPathMatcher("glob:**");
            Files.walkFileTree(resourceDir, new Copy(resourceDir, resourceDirInBalo, filter, filter));
        }
    }

    private void addModuleSource(Path root, Path moduleSourceDir, String moduleName) throws IOException {
        // create the module directory in zip
        Path srcInBalo = root.resolve(ProjectDirConstants.SOURCE_DIR_NAME);
        Files.createDirectory(srcInBalo);
        Path moduleDirInBalo = srcInBalo.resolve(moduleName);
        Files.createDirectory(moduleDirInBalo);

        // copy only bal file from module directory path in to zip
        PathMatcher fileFilter = FileSystems.getDefault()
                .getPathMatcher("glob:**/*" + ProjectDirConstants.BLANG_SOURCE_EXT);
        // exclude resources and tests directories
        PathMatcher dirFilter = path -> {
            FileSystem fd = FileSystems.getDefault();
            String prefix = moduleDirInBalo
                    .resolve(ProjectDirConstants.RESOURCE_DIR_NAME).toString();

            // Skip resources directory
            if (fd.getPathMatcher("glob:" + prefix + "**").matches(path)) {
                return false;
            }
            // Skip tests directory
            prefix = moduleDirInBalo
                    .resolve(ProjectDirConstants.TEST_DIR_NAME).toString();
            // Skip resources directory
            if (fd.getPathMatcher("glob:" + prefix + "**").matches(path)) {
                return false;
            }
            return true;
        };
        Files.walkFileTree(moduleSourceDir, new Copy(moduleSourceDir, moduleDirInBalo, fileFilter, dirFilter));
    }

    private void addMetaData(Path root, String moduleName) throws IOException {
        Path metaDir = root.resolve(ProjectDirConstants.BALO_METADATA_DIR_NAME);
        Path baloMetaFile = metaDir.resolve(ProjectDirConstants.BALO_METADATA_FILE);
        Path moduleMetaFile = metaDir.resolve(ProjectDirConstants.BALO_MODULE_METADATA_FILE);

        Files.createDirectories(metaDir);

        TomlWriter writer = new TomlWriter();
        // Write to BALO.toml
        String baloToml = writer.write(new BaloToml());
        Files.write(baloMetaFile, baloToml.getBytes(Charset.defaultCharset()));

        // Write to MODULE.toml
        Module moduleObj = new Module();
        moduleObj.setModule_name(moduleName);
        moduleObj.setModule_organization(manifest.getProject().getOrgName());
        moduleObj.setModule_version(manifest.getProject().getVersion());
        moduleObj.setModule_authors(manifest.getProject().getAuthors());
        moduleObj.setModule_keywords(manifest.getProject().getKeywords());
        moduleObj.setModule_source_repository(manifest.getProject().getRepository());
        moduleObj.setModule_licenses(manifest.getProject().getLicense());
        moduleObj.setPlatform(manifest.getTargetPlatform());
        moduleObj.setBallerina_version(RepoUtils.getBallerinaVersion());
        String moduleToml = writer.write(moduleObj);
        Files.write(moduleMetaFile, moduleToml.getBytes(Charset.defaultCharset()));
    }

    static class Copy extends SimpleFileVisitor<Path> {
        private Path fromPath;
        private Path toPath;
        private StandardCopyOption copyOption;
        private PathMatcher fileFilter;
        private PathMatcher dirFilter;

        public Copy(Path fromPath, Path toPath, PathMatcher file, PathMatcher dir) {
            this.fromPath = fromPath;
            this.toPath = toPath;
            this.copyOption = StandardCopyOption.REPLACE_EXISTING;
            this.fileFilter = file;
            this.dirFilter = dir;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                throws IOException {
            Path targetPath = toPath.resolve(fromPath.relativize(dir).toString());
            if (!dirFilter.matches(targetPath)) {
                // we do not visit the sub tree is the directory is filtered out
                return FileVisitResult.SKIP_SUBTREE;
            }
            if (!Files.exists(targetPath)) {
                Files.createDirectory(targetPath);
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                throws IOException {
            Path targetPath = toPath.resolve(fromPath.relativize(file).toString());
            if (fileFilter.matches(targetPath)) {
                Files.copy(file, targetPath, copyOption);
            }
            return FileVisitResult.CONTINUE;
        }
    }
}

