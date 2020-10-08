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
import org.ballerinalang.toml.exceptions.TomlException;
import org.ballerinalang.toml.model.Dependency;
import org.ballerinalang.toml.model.DependencyMetadata;
import org.ballerinalang.toml.model.Library;
import org.ballerinalang.toml.model.LockFile;
import org.ballerinalang.toml.model.LockFileImport;
import org.ballerinalang.toml.model.Manifest;
import org.ballerinalang.toml.model.Module;
import org.ballerinalang.toml.model.Platform;
import org.ballerinalang.toml.parser.LockFileProcessor;
import org.ballerinalang.toml.parser.ManifestProcessor;
import org.wso2.ballerinalang.compiler.SourceDirectory;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;
import org.wso2.ballerinalang.compiler.util.ProjectDirs;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.ByteArrayInputStream;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
        try (FileSystem baloFS = createBaloArchive(baloFilePath)) {
            // Now lets put stuff in
            populateBaloArchive(baloFS, module);
            buildContext.out().println("\t" + projectDirectory.relativize(baloFilePath));
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

    private void populateBaloArchive(FileSystem baloFS, BLangPackage module) throws IOException {
        Path root = baloFS.getPath("/");
        Path projectDirectory = this.sourceDirectory.getPath();
        Path moduleSourceDir = projectDirectory.resolve(ProjectDirConstants.SOURCE_DIR_NAME)
                .resolve(module.packageID.name.value);
        String moduleName = module.packageID.name.value;
        Path manifest = projectDirectory.resolve(ProjectDirConstants.MANIFEST_FILE_NAME);
        // Now lets put stuff in according to spec
        // /
        // └─ metadata/
        //    └─ BALO.toml
        //    └─ MODULE.toml
        //    └─ Ballerina.toml
        // └─ src/
        // └─ resources/
        // └─ platform-libs/
        // └─ docs/
        //    └─ MODULE-DESC.md
        //    └─ api-docs/

        addMetaData(root, module, manifest);
        addModuleSource(root, baloFS, moduleSourceDir, moduleName);
        addResources(root, baloFS, moduleSourceDir);
        addModuleDoc(root, moduleSourceDir);
        // Add platform libs only if it is not a template module.
        if (!this.manifest.isTemplateModule(moduleName)) {
            addPlatformLibs(root, projectDirectory, moduleName);
        }
    }
    
    /**
     * Populate a toml with lock file dependencies and project module dependencies.
     *
     * @param module        The module with dependencies to add to toml.
     * @param manifestBytes The bytes of the manifest file.
     * @return The updated manifest bytes.
     */
    private byte[] populateTomlWithDependencies(BLangPackage module, byte[] manifestBytes) {
        try (ByteArrayInputStream tomlStream = new ByteArrayInputStream(manifestBytes)) {
            List<Dependency> dependenciesToAdd = new LinkedList<>();
            
            // collect dependencies from lock file for the given module. Not adding transitive dependencies.
            LockFile lockFile = LockFileProcessor.parseTomlContentAsStream(this.sourceDirectory.getLockFileContent());
            String moduleAlias = module.packageID.orgName.value + "/" + module.packageID.name.value;
            if (null != lockFile && null != lockFile.getImports() && lockFile.getImports().size() > 0 &&
                lockFile.getImports().containsKey(moduleAlias)) {
                List<LockFileImport> moduleImports = lockFile.getImports().get(moduleAlias);
                for (LockFileImport lockImport : moduleImports) {
                    Dependency dependency = new Dependency();
                    dependency.setModuleID(lockImport.getOrgName() + "/" + lockImport.getName());
                    DependencyMetadata depMeta = new DependencyMetadata();
                    depMeta.setVersion(lockImport.getVersion());
                    dependency.setMetadata(depMeta);
                    dependenciesToAdd.add(dependency);
                }
            }
            
            // collect dependencies from project dependencies
            Manifest manifest = ManifestProcessor.parseTomlContentAsStream(tomlStream);
            for (BLangImportPackage importz : module.imports) {
                // if import is from the same org as parent
                if (importz.symbol.pkgID.orgName.value.equals(module.packageID.orgName.value)) {
                    // if its from the same project
                    if (ProjectDirs.isModuleExist(this.sourceDirectory.getPath(), importz.symbol.pkgID.name.value)) {
                        // check if its not already there as an import.
                        Optional<Dependency> manifestDependency = manifest.getDependencies().stream()
                                .filter(dep -> dep.getOrgName().equals(importz.symbol.pkgID.orgName.value))
                                .filter(dep -> dep.getModuleName().equals(importz.symbol.pkgID.name.value))
                                .findAny();
                        
                        // if dependency is not mentioned in toml
                        if (!manifestDependency.isPresent()) {
                            // update manifest
                            Dependency dependency = new Dependency();
                            dependency.setModuleID(importz.symbol.pkgID.orgName.value + "/" +
                                                   importz.symbol.pkgID.name.value);
                            DependencyMetadata depMeta = new DependencyMetadata();
                            depMeta.setVersion(importz.symbol.pkgID.version.value);
                            dependency.setMetadata(depMeta);
                            dependenciesToAdd.add(dependency);
                        } else if (null != manifestDependency.get().getMetadata() &&
                                   null != manifestDependency.get().getMetadata().getVersion() &&
                                   !importz.symbol.pkgID.version.value.equals(
                                           manifestDependency.get().getMetadata().getVersion())) {
                            throw new BLangCompilerException("version specified for '" +
                                                             manifestDependency.get().toString() +
                                                             "' in Ballerina.toml should be '" +
                                                             importz.symbol.pkgID.toString() + "'.");
                        }
                    }
                }
            }
            
            // validate dependencies already in toml
            for (Dependency dependencyToAdd : dependenciesToAdd) {
                for (Dependency dependencyInManifest : manifest.getDependencies()) {
                    // if org name and module names are equal but versions are different, then throw an error.
                    if (dependencyToAdd.getOrgName().equals(dependencyInManifest.getOrgName()) &&
                        dependencyToAdd.getModuleName().equals(dependencyInManifest.getModuleName()) &&
                        null != dependencyToAdd.getMetadata() &&
                        null != dependencyInManifest.getMetadata() &&
                        null != dependencyToAdd.getMetadata().getVersion() &&
                        null != dependencyInManifest.getMetadata().getVersion() &&
                        !dependencyToAdd.getMetadata().getVersion().equals(
                                dependencyInManifest.getMetadata().getVersion())) {
                        throw new BLangCompilerException("version specified for '" +
                                                         dependencyInManifest.toString() +
                                                         "' in Ballerina.toml should be '" +
                                                         dependencyToAdd.getMetadata().getVersion() + "'. you can " +
                                                         "either update the Ballerina.toml or remove the " +
                                                         "Ballerina.lock file.");
                    }
                }
            }

            // validate platform dependencies
            Platform platform = manifest.getPlatform();
            if (platform.libraries != null) {
                for (Library platformLib : platform.libraries) {
                    if (platformLib.getPath() == null && (platformLib.getArtifactId() == null
                            || platformLib.getGroupId() == null || platformLib.getVersion() == null)) {
                        throw new BLangCompilerException("path or maven dependency properties are not specified for " +
                                "given platform library dependency.");
                    }
                }
            }

            if (dependenciesToAdd.size() > 0) {
                try (ByteArrayInputStream tomlStreamToUpdate = new ByteArrayInputStream(manifestBytes)) {
                    return ManifestProcessor.addDependenciesToManifest(tomlStreamToUpdate, dependenciesToAdd);
                }
            }
        } catch (TomlException | IOException e) {
            // ignore
        }
        return manifestBytes;
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
        List<Library> platformLibs = manifest.getPlatform().libraries;
        if (platformLibs == null) {
            return;
        }
        Path platformLibsDir = root.resolve(ProjectDirConstants.BALO_PLATFORM_LIB_DIR_NAME);
        Files.createDirectory(platformLibsDir);

        for (Library lib : platformLibs) {
            if ((lib.getModules() == null || Arrays.asList(lib.getModules()).contains(moduleName))
                    && lib.getScope() == null) {
                Path libPath = Paths.get(lib.getPath());
                Path nativeFile = projectDirectory.resolve(libPath);
                Path libFileName = libPath.getFileName();
                if (libFileName == null) {
                    continue;
                }
                Path targetPath = platformLibsDir.resolve(libFileName.toString());
                try {
                    Files.copy(nativeFile, targetPath, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    throw new BLangCompilerException("Dependency jar '" + libFileName +
                            "' is not found in the provided path.");
                }
            }
        }
    }

    private void addResources(Path root, FileSystem fs, Path moduleSourceDir) throws IOException {
        // create the resources directory in zip
        Path resourceDir = moduleSourceDir.resolve(ProjectDirConstants.RESOURCE_DIR_NAME);
        Path resourceDirInBalo = root.resolve(ProjectDirConstants.RESOURCE_DIR_NAME);
        Files.createDirectory(resourceDirInBalo);

        if (Files.exists(resourceDir)) {
            // copy resources file from module directory path in to zip
            PathMatcher filter = fs.getPathMatcher("glob:**");
            Files.walkFileTree(resourceDir, new Copy(resourceDir, resourceDirInBalo, filter, filter));
        }
    }

    private void addModuleSource(Path root, FileSystem fs, Path moduleSourceDir, String moduleName) throws IOException {
        // create the module directory in zip
        Path srcInBalo = root.resolve(ProjectDirConstants.SOURCE_DIR_NAME);
        Files.createDirectory(srcInBalo);
        Path moduleDirInBalo = srcInBalo.resolve(moduleName);
        Files.createDirectory(moduleDirInBalo);
        boolean isTemplate = this.manifest.isTemplateModule(moduleName);

        // copy only bal file from module directory path in to zip
        PathMatcher fileFilter = fs.getPathMatcher("glob:**/*" + ProjectDirConstants.BLANG_SOURCE_EXT);
        // exclude resources and tests directories
        PathMatcher dirFilter = path -> {
            String prefix = moduleDirInBalo
                    .resolve(ProjectDirConstants.RESOURCE_DIR_NAME).toString();

            // Skip resources directory
            if (fs.getPathMatcher("glob:" + prefix + "**").matches(path)) {
                return false;
            }
            // Skip tests directory
            prefix = moduleDirInBalo
                    .resolve(ProjectDirConstants.TEST_DIR_NAME).toString();
            // Skip test directory
            if (!isTemplate && fs.getPathMatcher("glob:" + prefix + "**").matches(path)) {
                return false;
            }
            return true;
        };
        Files.walkFileTree(moduleSourceDir, new Copy(moduleSourceDir, moduleDirInBalo, fileFilter, dirFilter));
    }

    private void addMetaData(Path root, BLangPackage module, Path manifestPath) throws IOException {
        Path metaDir = root.resolve(ProjectDirConstants.BALO_METADATA_DIR_NAME);
        Path baloMetaFile = metaDir.resolve(ProjectDirConstants.BALO_METADATA_FILE);
        Path moduleMetaFile = metaDir.resolve(ProjectDirConstants.BALO_MODULE_METADATA_FILE);
        Path baloManifest = metaDir.resolve(ProjectDirConstants.MANIFEST_FILE_NAME);

        Files.createDirectories(metaDir);

        TomlWriter writer = new TomlWriter();
        // Write to BALO.toml
        String baloToml = writer.write(new BaloToml());
        Files.write(baloMetaFile, baloToml.getBytes(Charset.defaultCharset()));

        // Write to MODULE.toml
        Module moduleObj = new Module();
        moduleObj.setModule_name(module.packageID.name.value);
        moduleObj.setModule_organization(this.manifest.getProject().getOrgName());
        moduleObj.setModule_version(this.manifest.getProject().getVersion());
        moduleObj.setModule_authors(this.manifest.getProject().getAuthors());
        moduleObj.setModule_keywords(this.manifest.getProject().getKeywords());
        moduleObj.setModule_source_repository(this.manifest.getProject().getRepository());
        moduleObj.setModule_licenses(this.manifest.getProject().getLicense());
        moduleObj.setPlatform(this.manifest.getTargetPlatform(module.packageID.name.value));
        moduleObj.setBallerina_version(RepoUtils.getBallerinaVersion());
        moduleObj.setTemplate(String.valueOf(
                manifest.getProject().getTemplates().contains(module.packageID.name.value)));
        String moduleToml = writer.write(moduleObj);
        Files.write(moduleMetaFile, moduleToml.getBytes(Charset.defaultCharset()));
        
        // Write Ballerina.toml
        byte[] manifestBytes = Files.readAllBytes(manifestPath);
        byte[] tomlBytes = populateTomlWithDependencies(module, manifestBytes);
        if (null != tomlBytes) {
            Files.write(baloManifest, tomlBytes);
        }
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
