/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.ballerina.projects.writers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.ballerina.projects.Package;
import io.ballerina.projects.model.BaloJson;
import io.ballerina.projects.model.PackageJson;
import io.ballerina.projects.model.adaptors.JsonCollectionsAdaptor;
import io.ballerina.projects.model.adaptors.JsonStringsAdaptor;
import io.ballerina.projects.utils.ProjectConstants;
import org.ballerinalang.compiler.BLangCompilerException;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * {@code BaloWriter} writes a package to balo format.
 *
 * @since 2.0.0
 */
public class BaloWriter {

    private BaloWriter() {}

    /**
     * Write a package to a .balo and return the created .balo path.
     *
     * @param pkg  Package to be written as a .balo.
     * @param path Directory where the .balo should be created.
     * @return Newly created balo path
     */
    public static Path write(Package pkg, Path path) throws AccessDeniedException {
        // todo check if the given package is compiled properly

        // Check if the path is a directory
        if (!path.toFile().isDirectory()) {
            throw new RuntimeException("Given path is not a directory: " + path);
        }

        if (!path.toFile().canWrite()) {
            throw new AccessDeniedException("No write access to create balo:" + path);
        }

        Path balo = path.resolve(
                getBaloName(pkg.packageOrg().toString(), pkg.packageName().toString(), pkg.packageVersion().toString(),
                        null));

        // Create the archive over write if exists
        try (FileSystem baloFS = createBaloArchive(balo)) {
            // Now lets put stuff in
            populateBaloArchive(baloFS, pkg);
        } catch (IOException e) {
            throw new BLangCompilerException("Failed to create balo :" + e.getMessage(), e);
        } catch (BLangCompilerException be) {
            // clean up if an error occur
            try {
                Files.delete(balo);
            } catch (IOException e) {
                // We ignore this error and throw out the original blang compiler error to the user
            }
            throw be;
        }
        return balo;
    }

    private static String getBaloName(String org, String pkgName, String version, String platform) {
        // <orgname>-<packagename>-<platform>-<version>.balo
        if (platform == null || "".equals(platform)) {
            platform = "any";
        }
        return org + "-" + pkgName + "-" + platform + "-" + version + ".balo";
    }

    private static FileSystem createBaloArchive(Path path) throws IOException {
        // TODO check apache commons zip file writer
        // Remove if file already exists
        if (path.toFile().exists()) {
            Files.delete(path);
        }
        // Define ZIP File System Properties
        Map<String, String> env = new HashMap<>();
        env.put("create", "true");
        env.put("encoding", "UTF-8");

        /* Locate File on disk for creation */
        URI zipDisk = URI.create("jar:" + path.toUri());
        /* Create ZIP file System */
        return FileSystems.newFileSystem(zipDisk, env);
    }

    private static void populateBaloArchive(FileSystem baloFS, Package pkg)
            throws IOException {
        Path root = baloFS.getPath("/");

        //   Add spec directory structure items
        //
        //   org-foo-any-1.0.0.balo
        //    ├── balo.json             ---> Details about balo
        //    ├── package.json          ---> Details about package within balo
        //    ├── docs/
        //    │	   ├── Package.md       ---> MD file describing the package
        //    │	   ├── modules/
        //    │	   │	├── foo/
        //    │	   │	│	 └── Module.md
        //    │	   │    ├── foo.bar/
        //    │	   │    └── foo.baz/
        //    │    └── api-docs.json    ---> API Docs json file
        //    ├── modules/
        //    │	   ├── foo/             ---> content of default module
        //    │    ├── foo.bar/         ---> content of sub module
        //    │    └── foo.baz/         ---> content of sub module
        //    │		    ├── resources/
        //    │	        ├── first.bal
        //    │	        ├── second.bal
        //    │         └── third.bal
        //    ├── lib/                  ---> Platform Libraries
        //    │	   ├──
        //    │	   └── third-party.jar
        //    └── ext/
        //         ├── datamapper/
        //         └── ext2/

        addBaloJson(root);
        addPackageJson(root, pkg);
        addPackageDoc(root, pkg.packagePath(), pkg.packageName().toString());
        addPackageSource(root, pkg.packagePath(), pkg.packageName().toString());
        // Add platform libs only if it is not a template module
//        if (!ballerinaToml.isTemplateModule(packageName)) {
//            addPlatformLibs(root, packagePath, ballerinaToml);
//        }
    }

    private static void addBaloJson(Path root) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String baloJson = gson.toJson(new BaloJson());
        try {
            Files.write(root.resolve("balo.json"), baloJson.getBytes(Charset.defaultCharset()));
        } catch (IOException e) {
            throw new RuntimeException("Failed to write 'balo.json' file: " + e.getMessage(), e);
        }
    }

    private static void addPackageJson(Path root, Package pkg) {
//        io.ballerina.projects.model.Package pkg = ballerinaToml.getPackage();
        PackageJson packageJson = new PackageJson(pkg.packageOrg().toString(), pkg.packageName().toString(),
                pkg.packageVersion().toString());

//        // Information extracted from Ballerina.toml
//        packageJson.setLicenses(pkg.getLicense());
//        packageJson.setAuthors(pkg.getAuthors());
//        packageJson.setSourceRepository(pkg.getRepository());
//        packageJson.setKeywords(pkg.getKeywords());
//        packageJson.setExported(pkg.getExported());
//
//        // Distribution details
//        packageJson.setBallerinaVersion(getBallerinaVersion());
//        // TODO Need to set platform, implementation_vendor & spec
//
//        // Dependencies and platform libraries
//        List<Dependency> dependencies = new ArrayList<>();
//        List<PlatformLibrary> platformLibraries = new ArrayList<>();
//
//        // TODO Need to get all the dependencies (Not mentioned in the toml)
//        Map<String, Object> tomlDependencies = ballerinaToml.getDependencies();
//        for (String key : tomlDependencies.keySet()) {
//            Object dependency = tomlDependencies.get(key);
//            // if String, then Dependency
//            if (dependency instanceof String) {
//                String[] keyParts = key.split("/");
//                Dependency dep = new Dependency(keyParts[0], keyParts[1], (String) dependency);
//                dependencies.add(dep);
//            } else { // else, PlatformLibrary
//                // TODO Need to set platform libraries
//            }
//        }

        // Remove fields with empty values from `package.json`
        Gson gson = new GsonBuilder()
                .registerTypeHierarchyAdapter(Collection.class, new JsonCollectionsAdaptor())
                .registerTypeHierarchyAdapter(String.class, new JsonStringsAdaptor())
                .setPrettyPrinting()
                .create();

        String baloJson = gson.toJson(packageJson);
        try {
            Files.write(root.resolve("package.json"), baloJson.getBytes(Charset.defaultCharset()));
        } catch (IOException e) {
            throw new RuntimeException("Failed to write 'package.json' file: " + e.getMessage(), e);
        }
    }

    private static void addPackageDoc(Path root, Path packageSourceDir, String pkgName) throws IOException {
        final String packageMdFileName = "Package.md";
        final String moduleMdFileName = "Module.md";

        Path packageMd = packageSourceDir.resolve(packageMdFileName);
        Path docsDirInBalo = root.resolve("docs");

        // If `Package.md` exists, create the docs directory & add `Package.md`
        if (packageMd.toFile().exists()) {
            Files.createDirectory(docsDirInBalo);
            Path packageMdInBalo = docsDirInBalo.resolve(packageMdFileName);
            Files.copy(packageMd, packageMdInBalo);
        }

        // If `Module.md` of default module exists, create `docs/modules` directory & add `Module.md`
        Path defaultModuleMd = packageSourceDir.resolve(moduleMdFileName);
        Path modulesDirInBaloDocs = docsDirInBalo.resolve(ProjectConstants.MODULES_ROOT);

        if (defaultModuleMd.toFile().exists()) {
            Files.createDirectory(modulesDirInBaloDocs);

            Path defaultModuleDirInBaloDocs = modulesDirInBaloDocs.resolve(pkgName);
            Files.createDirectory(defaultModuleDirInBaloDocs);
            Path defaultModuleMdInBaloDocs = modulesDirInBaloDocs.resolve(pkgName).resolve(moduleMdFileName);
            Files.copy(defaultModuleMd, defaultModuleMdInBaloDocs);
        }

        // Add other module docs
        File modulesSourceDir = new File(String.valueOf(packageSourceDir.resolve(ProjectConstants.MODULES_ROOT)));
        File[] directoryListing = modulesSourceDir.listFiles();

        if (directoryListing != null) {
            for (File moduleDir : directoryListing) {
                if (moduleDir.isDirectory()) {
                    // Get `Module.md` path
                    Path otherModuleMd = packageSourceDir.resolve(ProjectConstants.MODULES_ROOT)
                            .resolve(moduleDir.getName()).resolve(moduleMdFileName);
                    // Create `package.module` folder, if `Module.md` path exists
                    if (otherModuleMd.toFile().exists()) {
                        Path otherModuleDirInBaloDocs = modulesDirInBaloDocs
                                .resolve(pkgName + "." + moduleDir.getName());
                        Files.createDirectory(otherModuleDirInBaloDocs);
                        Path otherModuleMdInBaloDocs = modulesDirInBaloDocs
                                .resolve(pkgName + "." + moduleDir.getName()).resolve(moduleMdFileName);
                        Files.copy(otherModuleMd, otherModuleMdInBaloDocs);
                    }
                }
            }
        }
    }

    private static void addPackageSource(Path root, Path packageSourceDir, String defaultPackageName)
            throws IOException {
        // create the module directory in zip
        Path packageInBalo = root.resolve(ProjectConstants.MODULES_ROOT);
        Files.createDirectory(packageInBalo);

        // add default module
        Path defaultPkgDirInBalo = packageInBalo.resolve(defaultPackageName);
        Files.createDirectory(defaultPkgDirInBalo);

        // copy resources directory
        copyResourcesDir(packageSourceDir, defaultPkgDirInBalo);
        // only add .bal files in the package root
        copyBallerinaSrcFiles(packageSourceDir, defaultPkgDirInBalo);

        // add other modules
        File modulesSourceDir = new File(String.valueOf(packageSourceDir.resolve(ProjectConstants.MODULES_ROOT)));
        File[] directoryListing = modulesSourceDir.listFiles();

        if (directoryListing != null) {
            for (File moduleDir : directoryListing) {
                if (moduleDir.isDirectory()) {
                    // add module
                    Path moduleDirInBalo = packageInBalo.resolve(defaultPackageName + "." + moduleDir.getName());
                    Files.createDirectory(moduleDirInBalo);

                    // copy resources directory
                    copyResourcesDir(moduleDir.toPath(), moduleDirInBalo);
                    // only add .bal files in the module root
                    copyBallerinaSrcFiles(moduleDir.toPath(), moduleDirInBalo);
                }
            }
        }
    }

    private static void copyResourcesDir(Path sourceDir, Path sourceDirInBalo) throws IOException {
        Path resourcesDir = sourceDirInBalo.resolve(ProjectConstants.RESOURCE_DIR_NAME);
        Path resourcesSrcDir = sourceDir.resolve(ProjectConstants.RESOURCE_DIR_NAME);

        // if resources not exists ignore copying
        if (resourcesSrcDir.toFile().exists()) {
            File[] resourceFiles = new File(String.valueOf(resourcesSrcDir)).listFiles();
            if (resourceFiles != null && resourceFiles.length > 0) {
                Files.walkFileTree(resourcesSrcDir, new CopyResources(resourcesSrcDir, resourcesDir));
            }
        }
    }

    private static void copyBallerinaSrcFiles(Path sourceDirPath, Path targetPath) throws IOException {
        File[] defaultModuleFiles = new File(String.valueOf(sourceDirPath)).listFiles();
        if (defaultModuleFiles != null) {
            for (File file : defaultModuleFiles) {
                if (file.isFile() && file.getName().endsWith(ProjectConstants.BLANG_SOURCE_EXT)) {
                    Files.copy(file.toPath(), targetPath.resolve(file.getName()), StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }

//    private static void addPlatformLibs(Path root, Path projectDirectory, BallerinaToml ballerinaToml)
//            throws IOException {
//        //If platform libs are defined add them to balo
//        List<Library> platformLibs = ballerinaToml.getPlatform().libraries;
//        if (platformLibs == null) {
//            return;
//        }
//        Path platformLibsDir = root.resolve(ProjectConstants.LIB_DIR);
//        Files.createDirectory(platformLibsDir);
//
//        for (Library lib : platformLibs) {
//            if (lib.getModules() == null && lib.getScope() == null) {
//                Path libPath = Paths.get(lib.getPath());
//                Path nativeFile = projectDirectory.resolve(libPath);
//                Path libFileName = libPath.getFileName();
//                if (libFileName == null) {
//                    continue;
//                }
//                Path targetPath = platformLibsDir.resolve(libFileName.toString());
//                try {
//                    Files.copy(nativeFile, targetPath, StandardCopyOption.REPLACE_EXISTING);
//                } catch (IOException e) {
//                    throw new BLangCompilerException(
//                            "Error while trying to add platform library to the BALO: " + lib.toString(), e);
//                }
//            }
//        }
//    }

    static class CopyResources extends SimpleFileVisitor<Path> {
        private Path fromPath;
        private Path toPath;
        private StandardCopyOption copyOption;

        CopyResources(Path fromPath, Path toPath) {
            this.fromPath = fromPath;
            this.toPath = toPath;
            this.copyOption = StandardCopyOption.REPLACE_EXISTING;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            Path targetPath = toPath.resolve(fromPath.relativize(dir).toString());
            if (!Files.exists(targetPath)) {
                Files.createDirectory(targetPath);
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            Path targetPath = toPath.resolve(fromPath.relativize(file).toString());
            Files.copy(file, targetPath, copyOption);
            return FileVisitResult.CONTINUE;
        }
    }
}
