/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.packerina;

import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.compiler.plugins.CompilerPlugin;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.testerina.util.TesterinaUtils;
import org.ballerinalang.toml.parser.ManifestProcessor;
import org.ballerinalang.util.BootstrapRunner;
import org.wso2.ballerinalang.compiler.BIRFileWriter;
import org.wso2.ballerinalang.compiler.Compiler;
import org.wso2.ballerinalang.compiler.LockFileWriter;
import org.wso2.ballerinalang.compiler.ModuleFileWriter;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;
import org.wso2.ballerinalang.compiler.util.ProjectDirs;
import org.wso2.ballerinalang.programfile.CompiledBinaryFile;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

import static org.ballerinalang.compiler.CompilerOptionName.BUILD_COMPILED_MODULE;
import static org.ballerinalang.compiler.CompilerOptionName.COMPILER_PHASE;
import static org.ballerinalang.compiler.CompilerOptionName.EXPERIMENTAL_FEATURES_ENABLED;
import static org.ballerinalang.compiler.CompilerOptionName.LOCK_ENABLED;
import static org.ballerinalang.compiler.CompilerOptionName.OFFLINE;
import static org.ballerinalang.compiler.CompilerOptionName.PROJECT_DIR;
import static org.ballerinalang.compiler.CompilerOptionName.SIDDHI_RUNTIME_ENABLED;
import static org.ballerinalang.compiler.CompilerOptionName.SKIP_TESTS;
import static org.ballerinalang.compiler.CompilerOptionName.TEST_ENABLED;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BLANG_COMPILED_PKG_BIR_EXT;
import static org.wso2.ballerinalang.util.RepoUtils.BALLERINA_INSTALL_DIR_PROP;

/**
 * This class provides util methods for building Ballerina programs and packages.
 *
 * @since 0.95.2
 */
public class BuilderUtils {
    private static final String BALLERINA_HOME = "BALLERINA_HOME";
    private static PrintStream outStream = System.out;

    private static ModuleFileWriter moduleFileWriter;
    private static BIRFileWriter birFileWriter;
    private static LockFileWriter lockFileWriter;


    public static void compileWithTestsAndWrite(Path sourceRootPath,
                                                String packagePath,
                                                String targetPath,
                                                boolean buildCompiledPkg,
                                                boolean offline,
                                                boolean lockEnabled,
                                                boolean skipTests,
                                                boolean enableExperimentalFeatures,
                                                boolean siddhiRuntimeEnabled,
                                                boolean jvmTarget) {
        CompilerContext context = getCompilerContext(sourceRootPath, jvmTarget, buildCompiledPkg, offline,
                lockEnabled, skipTests, enableExperimentalFeatures, siddhiRuntimeEnabled);

        Compiler compiler = Compiler.getInstance(context);
        BLangPackage bLangPackage = compiler.build(packagePath);

        if (skipTests) {
            outStream.println();
            compiler.write(bLangPackage, targetPath);
        } else {
            runTests(compiler, sourceRootPath, Collections.singletonList(bLangPackage));
            compiler.write(bLangPackage, targetPath);
        }
    }

    public static void compileWithTestsAndWrite(Path sourceRootPath,
                                                String packageName,
                                                String targetPath,
                                                boolean buildCompiledPkg,
                                                boolean offline,
                                                boolean lockEnabled,
                                                boolean skiptests,
                                                boolean enableExperimentalFeatures,
                                                boolean siddhiRuntimeEnabled,
                                                boolean jvmTarget,
                                                boolean dumpBIR,
                                                boolean genExecutables) {
        CompilerContext context = getCompilerContext(sourceRootPath, jvmTarget, buildCompiledPkg, offline,
                lockEnabled, skiptests, enableExperimentalFeatures, siddhiRuntimeEnabled);

        Compiler compiler = Compiler.getInstance(context);
        BLangPackage bLangPackage = compiler.build(packageName);
        boolean isSingleFile = packageName.endsWith(ProjectDirConstants.BLANG_SOURCE_EXT);

        try {
            //TODO: replace with actual target dir
            Path targetDirectory = Files.createTempDirectory("ballerina-compile").toAbsolutePath();
            String balHome = Objects.requireNonNull(System.getProperty("ballerina.home"),
                    "ballerina.home is not set");

            String targetDir = Files.isDirectory(Paths.get(targetPath)) ? targetPath : ".";

            BootstrapRunner.createClassLoaders(bLangPackage, Paths.get(balHome).resolve("bir-cache"),
                    targetDirectory, Optional.of(Paths.get(targetDir)), dumpBIR);

            // If package is a ballerina file do not write executables.
            // Create executable jar files.
            if (bLangPackage.symbol.entryPointExists && !isSingleFile) {
                outStream.println();
                outStream.println("Generating Executables");
                assembleExecutable(bLangPackage, sourceRootPath);
            } else {
                if (!isSingleFile) {
                    throw new BLangCompilerException("package `" + packageName + "` do not have an entry point");
                }
            }
    
            ServiceLoader<CompilerPlugin> processorServiceLoader = ServiceLoader.load(CompilerPlugin.class);
            processorServiceLoader.forEach(plugin -> {
                String execJarName;
                Path execFilePath;
                if (!isSingleFile) {
                    execJarName = bLangPackage.packageID.name.value + ProjectDirConstants.EXEC_SUFFIX +
                                  ProjectDirConstants.BLANG_COMPILED_JAR_EXT;
                    execFilePath = sourceRootPath
                            .resolve(ProjectDirConstants.TARGET_DIR_NAME)
                            .resolve(ProjectDirConstants.BIN_DIR_NAME)
                            .resolve(execJarName);
                } else {
                    execFilePath = sourceRootPath.resolve(packageName.replaceAll(".bal", "") +
                                                          ProjectDirConstants.BLANG_COMPILED_JAR_EXT);
                }

                plugin.codeGenerated(bLangPackage.packageID, execFilePath);
            });
    
        } catch (IOException e) {
            throw new BLangCompilerException("error invoking jballerina backend", e);
        }
    }


    public static void compileWithTestsAndWrite(Path sourceRootPath, boolean offline, boolean lockEnabled,
                                                boolean skiptests, boolean enableExperimentalFeatures,
                                                boolean siddhiRuntimeEnabled, boolean jvmTarget, boolean dumpBir,
                                                boolean genExecutables) {
        CompilerPhase compilerPhase = jvmTarget ? CompilerPhase.BIR_GEN : CompilerPhase.CODE_GEN;
        CompilerContext context = new CompilerContext();
        CompilerOptions options = CompilerOptions.getInstance(context);
        options.put(PROJECT_DIR, sourceRootPath.toString());
        options.put(OFFLINE, Boolean.toString(offline));
        options.put(COMPILER_PHASE, compilerPhase.toString());
        options.put(LOCK_ENABLED, Boolean.toString(lockEnabled));
        options.put(SKIP_TESTS, Boolean.toString(skiptests));
        options.put(TEST_ENABLED, "true");
        options.put(EXPERIMENTAL_FEATURES_ENABLED, Boolean.toString(enableExperimentalFeatures));
        options.put(SIDDHI_RUNTIME_ENABLED, Boolean.toString(siddhiRuntimeEnabled));

        Compiler compiler = Compiler.getInstance(context);
        List<BLangPackage> packages = compiler.build();

        prepareTargetDirectory(sourceRootPath);
        // Path jarCache = target.resolve(ProjectDirConstants.CACHES_DIR_NAME)
        //        .resolve(ProjectDirConstants.JAR_CACHE_DIR_NAME);
        // TODO fix below properly (add testing as well)
        if (jvmTarget) {
            outStream.println();
            outStream.println("Generating artifacts");
            // compiler.write(packages);
            generateModuleArtafacts(packages, context);

            try {
                generateJars(packages, sourceRootPath, dumpBir);
            } catch (IOException e) {
                throw new BLangCompilerException("error invoking jballerina backend", e);
            }

            // Create executable jar files.
            List<BLangPackage> entryPackages = packages.stream().filter(p -> p.symbol.entryPointExists)
                    .collect(Collectors.toList());
            if (genExecutables && !entryPackages.isEmpty()) {
                outStream.println();
                outStream.println("Generating executables");
                entryPackages.forEach(p -> assembleExecutable(p, sourceRootPath));
            }

            // Run annotation processor codeGenerated phase.
            ServiceLoader<CompilerPlugin> processorServiceLoader = ServiceLoader.load(CompilerPlugin.class);
            for (BLangPackage p: packages) {
                processorServiceLoader.forEach(plugin -> {
                    String execJarName = p.packageID.name.value + ProjectDirConstants.EXEC_SUFFIX
                            + ProjectDirConstants.BLANG_COMPILED_JAR_EXT;
                    Path execFilePath = sourceRootPath
                            .resolve(ProjectDirConstants.TARGET_DIR_NAME)
                            .resolve(ProjectDirConstants.BIN_DIR_NAME)
                            .resolve(execJarName);
                    plugin.codeGenerated(p.packageID, execFilePath);
                });
            }
            return;
        }


        if (skiptests) {
            if (packages.size() == 0) {
                throw new BLangCompilerException("no ballerina source files found to compile");
            }
            outStream.println();
            compiler.write(packages);
        } else {
            if (packages.size() == 0) {
                throw new BLangCompilerException("no ballerina source files found to compile");
            }
            runTests(compiler, sourceRootPath, packages);
            compiler.write(packages);
        }
    }

    private static void generateModuleArtafacts(List<BLangPackage> packages, CompilerContext context) {
        // TODO: need to place the follow in a better place. I took these out from the compiler -
        // to separate them from the compiler write. I am unable to refactor the compiler write ATM
        // since the build rely on output of that. We need to fix the build and refactor this code.
        moduleFileWriter = ModuleFileWriter.getInstance(context);
        birFileWriter = BIRFileWriter.getInstance(context);
        // todo put the lock file writer in a seperate package.
        lockFileWriter = LockFileWriter.getInstance(context);
        lockFileWriter.writeLockFile(ManifestProcessor.getInstance(context).getManifest());
        packages.forEach(moduleFileWriter::write);
        packages.forEach(birFileWriter::write);
        packages.forEach(bLangPackage -> lockFileWriter.addEntryPkg(bLangPackage.symbol));
    }

    /**
     * Generate jars for given package.
     *
     * @param packages package
     * @param sourceRoot source root
     * @param dumpBir Flag indicating to dump the bir
     * @throws IOException for IO errors
     */
    public static void generateJars(List<BLangPackage> packages, Path sourceRoot, boolean dumpBir) throws IOException {
        // Path target = sourceRoot.resolve(ProjectDirConstants.TARGET_DIR_NAME);
        // construct BIR cache directories
        // - Project BIR cache
        Path projectBIRcache = sourceRoot.resolve(ProjectDirConstants.TARGET_DIR_NAME)
                .resolve(ProjectDirConstants.CACHES_DIR_NAME)
                .resolve(ProjectDirConstants.BIR_CACHE_DIR_NAME);
        // - Home BIR cache
        Path homeBIRCache = RepoUtils.createAndGetHomeReposPath().resolve(ProjectDirConstants.BIR_CACHE_DIR_NAME);
        // - System BIR cache
        Path systemBIRCache = Paths.get(System.getProperty(BALLERINA_INSTALL_DIR_PROP)).resolve("bir-cache");

        // construct JAR cache directories
        // - Project JAR cache
        Path projectJARcache = sourceRoot.resolve(ProjectDirConstants.TARGET_DIR_NAME)
                .resolve(ProjectDirConstants.CACHES_DIR_NAME)
                .resolve(ProjectDirConstants.JAR_CACHE_DIR_NAME);
        // - Home JAR cache
        // Path homeJARCache = RepoUtils.createAndGetHomeReposPath().resolve(ProjectDirConstants.JAR_CACHE_DIR_NAME);

        for (BLangPackage bpackage : packages) {
            Path moduleFragment = Paths.get(bpackage.packageID.orgName.value,
                    bpackage.packageID.name.value, bpackage.packageID.version.value);

            // Iterate the imports and decide where to save.
            // - If it is a current project we save to Project
            // - If is is an import we save to Home
            writeImportJar(bpackage.symbol.imports, sourceRoot, dumpBir,
                    projectBIRcache.toString(), homeBIRCache.toString(), systemBIRCache.toString());
            // Generate the jar of the package.
            Files.createDirectories(projectJARcache.resolve(moduleFragment));
            Path entryBir = projectBIRcache.resolve(moduleFragment)
                    .resolve(bpackage.packageID.name.value + ProjectDirConstants.BLANG_COMPILED_PKG_BIR_EXT);
            Path jarOutput = projectJARcache.resolve(moduleFragment)
                    .resolve(bpackage.packageID.name.value + ProjectDirConstants.BLANG_COMPILED_JAR_EXT);
            BootstrapRunner.generateJarBinary(entryBir.toString(), jarOutput.toString(), dumpBir,
                    projectBIRcache.toString(), homeBIRCache.toString(), systemBIRCache.toString());
        }
    }

    private static void writeImportJar(List<BPackageSymbol> imports, Path sourceRoot, boolean dumpBir, String... reps) {
        for (BPackageSymbol bimport : imports) {
            PackageID id = bimport.pkgID;
            Path projectJarCache = RepoUtils.createAndGetHomeReposPath().resolve(ProjectDirConstants.JAR_CACHE_DIR_NAME)
                    .resolve(id.orgName.value).resolve(id.name.value).resolve(id.version.value);
            Path homeJarCache = RepoUtils.createAndGetHomeReposPath().resolve(ProjectDirConstants.JAR_CACHE_DIR_NAME)
                    .resolve(id.orgName.value).resolve(id.name.value).resolve(id.version.value);
            Path projectBirCache = RepoUtils.createAndGetHomeReposPath().resolve(ProjectDirConstants.BIR_CACHE_DIR_NAME)
                    .resolve(id.orgName.value).resolve(id.name.value).resolve(id.version.value);
            Path homeBirCache = RepoUtils.createAndGetHomeReposPath().resolve(ProjectDirConstants.BIR_CACHE_DIR_NAME)
                    .resolve(id.orgName.value).resolve(id.name.value).resolve(id.version.value);

            try {
                if (id.orgName.value.equals("ballerina") || id.orgName.value.equals("ballerinax")) {
                    continue;
                }
                Path jarCache;
                Path birCache;
                // If the module is part of the project write it to project jar cache check if file exist
                // If not write it to home jar cache
                // skip ballerina and ballerinax
                if (ProjectDirs.isModuleExist(sourceRoot, id.name.value)) {
                    jarCache = projectJarCache;
                    birCache = projectBirCache;
                } else {
                    jarCache = homeJarCache;
                    birCache = homeBirCache;
                }
                Path jarFile = jarCache.resolve(id.name.value + ProjectDirConstants.BLANG_COMPILED_JAR_EXT);
                Path birFile = birCache.resolve(id.name.value + BLANG_COMPILED_PKG_BIR_EXT);
                if (!Files.exists(jarFile)) {
                    Files.createDirectories(jarCache);
                    BootstrapRunner.generateJarBinary(birFile.toString(), jarFile.toString(), false,
                            reps);
                }
                writeImportJar(bimport.imports, sourceRoot, dumpBir);
            } catch (IOException e) {
                String msg = "error writing the compiled module(jar) of '" +
                        id.name.value + "' to '" + homeJarCache + "': " + e.getMessage();
                throw new BLangCompilerException(msg, e);
            }
        }
    }

    /**
     * Run tests in the build.
     *
     * @param compiler       compiler instance
     * @param sourceRootPath source root path
     * @param packageList    list of compiled packages
     */
    private static void runTests(Compiler compiler, Path sourceRootPath, List<BLangPackage> packageList) {
        Map<BLangPackage, CompiledBinaryFile.ProgramFile> programFileMap = new HashMap<>();
        // Only tests in packages are executed so default packages i.e. single bal files which has the package name
        // as "." are ignored. This is to be consistent with the "ballerina test" command which only executes tests
        // in packages.
        packageList.stream().filter(bLangPackage -> !bLangPackage.packageID.getName().equals(Names.DEFAULT_PACKAGE))
                .forEach(bLangPackage -> {
                    CompiledBinaryFile.ProgramFile programFile;
                    if (bLangPackage.containsTestablePkg()) {
                        programFile = compiler.getExecutableProgram(bLangPackage.getTestablePkg());
                    } else {
                        // In this package there are no tests to be executed. But we need to say to the users that
                        // there are no tests found in the package to be executed as :
                        // Running tests
                        //     <org-name>/<package-name>:<version>
                        //         No tests found
                        programFile = compiler.getExecutableProgram(bLangPackage);
                    }

                    programFileMap.put(bLangPackage, programFile);
                });

        if (programFileMap.size() > 0) {
            TesterinaUtils.executeTests(sourceRootPath, programFileMap);
        }
    }

    private static CompilerContext getCompilerContext(Path sourceRootPath,
                                                      boolean jvmTarget,
                                                      boolean buildCompiledPkg,
                                                      boolean offline,
                                                      boolean lockEnabled,
                                                      boolean skipTests,
                                                      boolean enableExperimentalFeatures,
                                                      boolean siddhiRuntimeEnabled) {
        CompilerPhase compilerPhase = jvmTarget ? CompilerPhase.BIR_GEN : CompilerPhase.CODE_GEN;
        CompilerContext context = new CompilerContext();
        CompilerOptions options = CompilerOptions.getInstance(context);
        options.put(PROJECT_DIR, sourceRootPath.toString());
        options.put(COMPILER_PHASE, compilerPhase.toString());
        options.put(BUILD_COMPILED_MODULE, Boolean.toString(buildCompiledPkg));
        options.put(OFFLINE, Boolean.toString(offline));
        options.put(LOCK_ENABLED, Boolean.toString(lockEnabled));
        options.put(SKIP_TESTS, Boolean.toString(skipTests));
        options.put(TEST_ENABLED, Boolean.toString(true));
        options.put(EXPERIMENTAL_FEATURES_ENABLED, Boolean.toString(enableExperimentalFeatures));
        options.put(SIDDHI_RUNTIME_ENABLED, Boolean.toString(siddhiRuntimeEnabled));
        return context;
    }

    /**
     * Prepare target directory before the compile.
     *
     * @param sourceRoot source root of the ballerina file or project
     * @return target path
     */
    private static Path prepareTargetDirectory(Path sourceRoot) {
        // Check if the source root is a project
        Path target;
        if (ProjectDirs.isProject(sourceRoot)) {
            // If source root is a project create and return target inside it.
            target = sourceRoot.resolve(ProjectDirConstants.TARGET_DIR_NAME);
            // Before creating lets see if the target exists.
            if (!Files.exists(target)) {
                try {
                    Files.createDirectory(target);
                } catch (IOException e) {
                    throw new BLangCompilerException("unable to create target directory");
                }
            }
            createCacheDirectory(target);
        } else {
            // If it is not a project create a tmp directory as target
            try {
                target = Files.createTempDirectory("b7a-compiler");
            } catch (IOException e) {
                throw new BLangCompilerException("unable to create target directory");
            }
            createCacheDirectory(target);
        }
        return target;
    }

    /**
     * Prepare cache directory before the compile.
     *
     * @param target source root of the ballerina file or project
     * @return target path
     */
    private static void createCacheDirectory(Path target) {
        Path cacheDir = target.resolve(ProjectDirConstants.CACHES_DIR_NAME);
        if (!Files.exists(cacheDir)) {
            try {
                Files.createDirectory(cacheDir);
            } catch (IOException e) {
                throw new BLangCompilerException("unable to create target cache directory");
            }
        }
        Path birCacheDir = cacheDir.resolve(ProjectDirConstants.BIR_CACHE_DIR_NAME);
        if (!Files.exists(birCacheDir)) {
            try {
                Files.createDirectory(birCacheDir);
            } catch (IOException e) {
                throw new BLangCompilerException("unable to create target bir cache directory");
            }
        }
    }

    private static void assembleExecutable(BLangPackage bLangPackage, Path project) {
        try {
            final Path target = project.resolve(ProjectDirConstants.TARGET_DIR_NAME);
            final Path bin = target.resolve(ProjectDirConstants.BIN_DIR_NAME);
            final Path jarCache = target.resolve(ProjectDirConstants.CACHES_DIR_NAME)
                    .resolve(ProjectDirConstants.JAR_CACHE_DIR_NAME);
            String moduleJarName = bLangPackage.packageID.name.value
                    + ProjectDirConstants.BLANG_COMPILED_JAR_EXT;
            String execJarName = bLangPackage.packageID.name.value + ProjectDirConstants.EXEC_SUFFIX
                    + ProjectDirConstants.BLANG_COMPILED_JAR_EXT;
            // Copy the jar from cache to bin directory
            Path uberJar = bin.resolve(execJarName);
            Path moduleJar = jarCache
                    .resolve(bLangPackage.packageID.orgName.value)
                    .resolve(bLangPackage.packageID.name.value)
                    .resolve(bLangPackage.packageID.version.value)
                    .resolve(moduleJarName);

            // Check if the package has an entry point.
            if (bLangPackage.symbol.entryPointExists) {
                // Create bin directory if it is not there.
                Files.createDirectories(bin);

                Files.copy(moduleJar, uberJar, StandardCopyOption.REPLACE_EXISTING);
                // Get the fs handle to the jar file

                // Iterate through the imports and copy dependencies.
                for (BPackageSymbol importz : bLangPackage.symbol.imports) {
                    Path importJar = findImportJarPath(importz, project);

                    if (importJar != null && Files.exists(importJar)) {
                        copyFromJarToJar(importJar, uberJar);
                    }
                }
            }
            // Copy dependency jar
            // Copy dependency libraries
            // Executable is created at give location.
            outStream.println(project.relativize(uberJar).toString());
            // If no entry point is found we do nothing.
        } catch (IOException e) {
            throw new BLangCompilerException("Unable to create the executable :" + e.getMessage());
        }
    }

    private static Path findImportJarPath(BPackageSymbol importz, Path project) {
        // Get the jar paths
        PackageID id = importz.pkgID;
        Path projectJarCache = RepoUtils.createAndGetHomeReposPath().resolve(ProjectDirConstants.JAR_CACHE_DIR_NAME)
                .resolve(id.orgName.value).resolve(id.name.value).resolve(id.version.value);
        Path homeJarCache = RepoUtils.createAndGetHomeReposPath().resolve(ProjectDirConstants.JAR_CACHE_DIR_NAME)
                .resolve(id.orgName.value).resolve(id.name.value).resolve(id.version.value);
        // Skip ballerina and ballerinax
        if (id.orgName.value.equals("ballerina") || id.orgName.value.equals("ballerinax")) {
            return null;
        }
        // Look if it is a project module.
        if (ProjectDirs.isModuleExist(project, id.name.value)) {
            // If so fetch from project jar cache
            return projectJarCache.resolve(id.name.value + ProjectDirConstants.BLANG_COMPILED_JAR_EXT);
        } else {
            // If not fetch from home jar cache.
            return homeJarCache.resolve(id.name.value + ProjectDirConstants.BLANG_COMPILED_JAR_EXT);
        }
        // return the path
    }


    private static void copyFromJarToJar(Path fromJar, Path toJar) throws IOException {
        URI uberJarUri = URI.create("jar:" + toJar.toUri().toString());
        // Load the to jar to a file system
        try (FileSystem toFs = FileSystems.newFileSystem(uberJarUri, Collections.emptyMap())) {
            Path to = toFs.getRootDirectories().iterator().next();
            URI moduleJarUri = URI.create("jar:" + fromJar.toUri().toString());
            // Load the from jar to a file system.
            try (FileSystem fromFs = FileSystems.newFileSystem(moduleJarUri, Collections.emptyMap())) {
                Path from = fromFs.getRootDirectories().iterator().next();
                // Walk and copy the files.
                Files.walkFileTree(from, new Copy(from, to));
            }
        }
    }


    static class Copy extends SimpleFileVisitor<Path> {
        private Path fromPath;
        private Path toPath;
        private StandardCopyOption copyOption;


        public Copy(Path fromPath, Path toPath, StandardCopyOption copyOption) {
            this.fromPath = fromPath;
            this.toPath = toPath;
            this.copyOption = copyOption;
        }

        public Copy(Path fromPath, Path toPath) {
            this(fromPath, toPath, StandardCopyOption.REPLACE_EXISTING);
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                throws IOException {

            Path targetPath = toPath.resolve(fromPath.relativize(dir).toString());
            if (!Files.exists(targetPath)) {
                Files.createDirectory(targetPath);
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                throws IOException {
            Path toFile = toPath.resolve(fromPath.relativize(file).toString());
            if (!Files.exists(toFile)) {
                Files.copy(file, toFile, copyOption);
            }
            return FileVisitResult.CONTINUE;
        }
    }
}
