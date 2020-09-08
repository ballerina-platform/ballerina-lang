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
import io.ballerina.projects.model.BallerinaToml;
import io.ballerina.projects.model.BaloJson;
import io.ballerina.projects.model.Dependency;
import io.ballerina.projects.model.PackageJson;
import io.ballerina.projects.model.PlatformLibrary;
import io.ballerina.projects.writers.exceptions.NoPermissionException;
import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.toml.model.Library;
import org.wso2.ballerinalang.compiler.util.FileUtils;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.File;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@code BaloWriter} writes a package to balo format.
 *
 * @since 2.0.0
 */
public class BaloWriter {

    private static final String MODULES = "modules";

    private BaloWriter() {}

    /**
     * Write a package to a .balo and return the created .balo path.
     *
     * @param pkg  Package to be written as a .balo.
     * @param path Directory where the .balo should be created.
     * @return Newly created balo path
     */
    public static Path write(Package pkg, Path path) {
        // todo check if the given package is compiled properly

        // Check if the path is a directory
        if (!path.toFile().isDirectory()) {
            throw new RuntimeException("Given path is not a directory: " + path);
        }

        // Check directory permissions
        if (!new File(String.valueOf(path)).canWrite()) {
            throw new NoPermissionException("No write access to create balo:" + path);
        }

        BallerinaToml ballerinaToml = pkg.ballerinaToml();
        Path packagePath = pkg.packagePath();
        Path balo = path.resolve(getBaloName(ballerinaToml));

        // Create the archive over write if exists
        try (FileSystem baloFS = createBaloArchive(balo)) {
            // Now lets put stuff in
            populateBaloArchive(baloFS, packagePath, ballerinaToml);
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

    private static String getBaloName(BallerinaToml ballerinaToml) {
        // <orgname>-<packagename>-<platform>-<version>.balo
        String platform = ballerinaToml.getPlatform().target;
        if (platform == null || "".equals(platform)) {
            platform = "any";
        }
        return ballerinaToml.getPackage().getOrg() + "-" + ballerinaToml.getPackage().getName() + "-"
                + platform + "-" + ballerinaToml.getPackage().getVersion() + ".balo";
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

    private static void populateBaloArchive(FileSystem baloFS, Path packagePath, BallerinaToml ballerinaToml)
            throws IOException {
        Path root = baloFS.getPath("/");
        String packageName = ballerinaToml.getPackage().getName();

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
        addPackageJson(root, ballerinaToml);
        addPackageDoc(root, packagePath, ballerinaToml.getPackage().getName());
        addPackageSource(root, baloFS, packagePath, packageName, ballerinaToml);
        // Add platform libs only if it is not a template module
        if (!ballerinaToml.isTemplateModule(packageName)) {
            addPlatformLibs(root, packagePath, ballerinaToml);
        }
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

    private static void addPackageJson(Path root, BallerinaToml ballerinaToml) {
        // TODO if value not there, remove the filed from json file
        PackageJson packageJson = new PackageJson(ballerinaToml.getPackage().getOrg(),
                ballerinaToml.getPackage().getName(), ballerinaToml.getPackage().getVersion());

        // Information extracted from Ballerina.toml
        packageJson.setLicenses(ballerinaToml.getPackage().getLicense());
        packageJson.setAuthors(ballerinaToml.getPackage().getAuthors());
        packageJson.setSourceRepository(ballerinaToml.getPackage().getRepository());
        packageJson.setKeywords(ballerinaToml.getPackage().getKeywords());
        packageJson.setExported(ballerinaToml.getPackage().getExported());

        // Distribution details
        packageJson.setBallerinaVersion(RepoUtils.getBallerinaVersion());
        // TODO Need to set platform, implementation_vendor & spec

        // Dependencies and platform libraries
        List<Dependency> dependencies = new ArrayList<>();
        List<PlatformLibrary> platformLibraries = new ArrayList<>();

        // TODO Need to get all the dependencies (Not mentioned in the toml)
        Map<String, Object> tomlDependencies = ballerinaToml.getDependencies();
        for (String key : tomlDependencies.keySet()) {
            Object dependency = tomlDependencies.get(key);
            // if String, then Dependency
            if (dependency instanceof String) {
                String[] keyParts = key.split("/");
                Dependency dep = new Dependency(keyParts[0], keyParts[1], (String) dependency);
                dependencies.add(dep);
            } else { // else, PlatformLibrary
                // TODO Need to set platform libraries
            }
        }

        Gson gson = new GsonBuilder().addSerializationExclusionStrategy(packageJson.strategy())
                .setPrettyPrinting().create();
        String baloJson = gson.toJson(packageJson);
        try {
            Files.write(root.resolve("package.json"), baloJson.getBytes(Charset.defaultCharset()));
        } catch (IOException e) {
            throw new RuntimeException("Failed to write 'package.json' file: " + e.getMessage(), e);
        }
    }

    private static void addPackageDoc(Path root, Path packageSourceDir, String pkgName) throws IOException {
        // Create the docs directory in zip
        final String packageMdFileName = "Package.md";
        final String moduleMdFileName = "Module.md";

        Path packageMd = packageSourceDir.resolve(packageMdFileName);
        Path docsDirInBalo = root.resolve("docs");
        Files.createDirectory(docsDirInBalo);
        Path packageMdInBalo = docsDirInBalo.resolve(packageMdFileName);

        if (packageMd.toFile().exists()) {
            Files.copy(packageMd, packageMdInBalo);
        }

        // Create module docs
        Path modulesDirInBaloDocs = docsDirInBalo.resolve(MODULES);
        Files.createDirectory(modulesDirInBaloDocs);

        // Add default module docs
        Path defaultModuleMd = packageSourceDir.resolve(moduleMdFileName);
        if (defaultModuleMd.toFile().exists()) {
            Path defaultModuleDirInBaloDocs = modulesDirInBaloDocs.resolve(pkgName);
            Files.createDirectory(defaultModuleDirInBaloDocs);
            Path defaultModuleMdInBaloDocs = modulesDirInBaloDocs.resolve(pkgName).resolve(moduleMdFileName);
            Files.copy(defaultModuleMd, defaultModuleMdInBaloDocs);
        }

        // Add other module docs
        File modulesSourceDir = new File(String.valueOf(packageSourceDir.resolve(MODULES)));
        File[] directoryListing = modulesSourceDir.listFiles();

        if (directoryListing != null) {
            for (File moduleDir : directoryListing) {
                if (moduleDir.isDirectory()) {
                    // Get `Module.md` path
                    Path otherModuleMd = packageSourceDir.resolve(MODULES).resolve(moduleDir.getName())
                            .resolve(moduleMdFileName);
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

    private static void addPackageSource(Path root, FileSystem fs, Path packageSourceDir, String defaultPackageName,
            BallerinaToml ballerinaToml) throws IOException {
        // create the module directory in zip
        Path packageInBalo = root.resolve(MODULES);
        Files.createDirectory(packageInBalo);

        // add default module
        Path defaultPkgDirInBalo = packageInBalo.resolve(defaultPackageName);
        Files.createDirectory(defaultPkgDirInBalo);
        boolean isTemplate = ballerinaToml.isTemplateModule(defaultPackageName);

        // TODO add .bal & resources only
        // copy resources directory
        Path resourcesDir = defaultPkgDirInBalo.resolve("resources");
        Path resourcesSrcDir = packageSourceDir.resolve("resources");
        Files.walkFileTree(resourcesSrcDir, new CopyResources(resourcesSrcDir, resourcesDir));


        // only add .bal files in the module root


        PathMatcher dirFilter = path -> {
            String prefix = defaultPkgDirInBalo.toString();
            if (fs.getPathMatcher("glob:" + prefix + "**").matches(path)) {
                return true;
            }
            return false;
        };
        PathMatcher fileFilter = path -> {
            String prefix = ".bal";
            if (fs.getPathMatcher("glob:**/*" + prefix).matches(path)) {
                return true;
            }
            return false;
        };

        Files.walkFileTree(packageSourceDir, new Copy(packageSourceDir, defaultPkgDirInBalo, dirFilter, fileFilter));


        // add other modules
//        File modulesSourceDir = new File(String.valueOf(packageSourceDir.resolve(MODULES)));
//        File[] directoryListing = modulesSourceDir.listFiles();
//
//        if (directoryListing != null) {
//            for (File moduleDir : directoryListing) {
//                if (moduleDir.isDirectory()) {
//                    // add module
//                    Path moduleDirInBalo = packageInBalo.resolve(defaultPackageName + "." + moduleDir.getName());
//                    Files.createDirectory(moduleDirInBalo);
//
//                    Files.walkFileTree(moduleDir.toPath(),
//                            new Copy(moduleDir.toPath(), moduleDirInBalo, dirFilter, null, true));
//                    Files.walkFileTree(moduleDir.toPath(),
//                            new Copy(moduleDir.toPath(), moduleDirInBalo, null, fileFilter, false));
//                }
//            }
//        }
    }

    private static void addPlatformLibs(Path root, Path projectDirectory, BallerinaToml ballerinaToml)
            throws IOException {
        //If platform libs are defined add them to balo
        List<Library> platformLibs = ballerinaToml.getPlatform().libraries;
        if (platformLibs == null) {
            return;
        }
        Path platformLibsDir = root.resolve("lib");
        Files.createDirectory(platformLibsDir);

        for (Library lib : platformLibs) {
            if (lib.getModules() == null && lib.getScope() == null) {
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
                    throw new BLangCompilerException("Dependency jar not found : " + lib.toString());
                }
            }
        }
    }

    static class Copy extends SimpleFileVisitor<Path> {
        private Path fromPath;
        private Path toPath;
        private StandardCopyOption copyOption;
        private PathMatcher dirFilter;
        private PathMatcher fileFilter;

        Copy(Path fromPath, Path toPath, PathMatcher dir, PathMatcher file) {
            this.fromPath = fromPath;
            this.toPath = toPath;
            this.copyOption = StandardCopyOption.REPLACE_EXISTING;
            this.dirFilter = dir;
            this.fileFilter = file;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            Path targetPath = toPath.resolve(fromPath.relativize(dir).toString());
            if (!dirFilter.matches(targetPath)) {
                // we do not visit the sub tree is the directory is filtered out
                return FileVisitResult.TERMINATE;
            } else {
                if (!Files.exists(targetPath)) {
                    Files.createDirectory(targetPath);
                }
                return FileVisitResult.CONTINUE;
            }
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            Path targetPath = toPath.resolve(fromPath.relativize(file).toString());
            if (fileFilter.matches(targetPath)) {
                Files.copy(file, targetPath, copyOption);
            }
            return FileVisitResult.CONTINUE;
        }
    }

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
