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
package io.ballerina.projects;

import io.ballerina.projects.environment.PackageCache;
import io.ballerina.projects.environment.ProjectEnvironment;
import io.ballerina.projects.internal.DefaultDiagnosticResult;
import io.ballerina.projects.internal.PackageDiagnostic;
import io.ballerina.projects.internal.ProjectDiagnosticErrorCode;
import io.ballerina.projects.internal.jballerina.JarWriter;
import io.ballerina.projects.internal.model.Target;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.projects.util.ProjectUtils;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import org.apache.commons.compress.archivers.jar.JarArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntryPredicate;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.FilenameUtils;
import org.ballerinalang.maven.Dependency;
import org.ballerinalang.maven.MavenResolver;
import org.ballerinalang.maven.Utils;
import org.ballerinalang.maven.exceptions.MavenResolverException;
import org.wso2.ballerinalang.compiler.CompiledJarFile;
import org.wso2.ballerinalang.compiler.bir.codegen.CodeGenerator;
import org.wso2.ballerinalang.compiler.bir.codegen.interop.InteropValidator;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.util.Lists;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

import static io.ballerina.projects.util.FileUtils.getFileNameWithoutExtension;
import static io.ballerina.projects.util.ProjectConstants.BIN_DIR_NAME;
import static io.ballerina.projects.util.ProjectConstants.DOT;
import static io.ballerina.projects.util.ProjectUtils.getThinJarFileName;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.CLASS_FILE_SUFFIX;

/**
 * This class represents the Ballerina compiler backend that produces executables that runs on the JVM.
 *
 * @since 2.0.0
 */
// TODO move this class to a separate Java package. e.g. io.ballerina.projects.platform.jballerina
//    todo that, we would have to move PackageContext class into an internal package.
public class JBallerinaBackend extends CompilerBackend {

    private static final String JAR_FILE_EXTENSION = ".jar";
    private static final String TEST_JAR_FILE_NAME_SUFFIX = "-testable";
    private static final String JAR_FILE_NAME_SUFFIX = "";
    private static final HashSet<String> excludeExtensions = new HashSet<>(Lists.of("DSA", "SF"));
    private static final String OS = System.getProperty("os.name").toLowerCase(Locale.getDefault());

    private final PackageResolution pkgResolution;
    private final JvmTarget jdkVersion;
    private final PackageContext packageContext;
    private final PackageCache packageCache;
    private final CompilerContext compilerContext;
    private final CodeGenerator jvmCodeGenerator;
    private final InteropValidator interopValidator;
    private final JarResolver jarResolver;
    private final PackageCompilation packageCompilation;
    private DiagnosticResult diagnosticResult;
    private boolean codeGenCompleted;
    private final List<JarConflict> conflictedJars;

    public static JBallerinaBackend from(PackageCompilation packageCompilation, JvmTarget jdkVersion) {
        return from(packageCompilation, jdkVersion, true);
    }

    public static JBallerinaBackend from(PackageCompilation packageCompilation, JvmTarget jdkVersion, boolean shrink) {
        // Check if the project has write permissions
        if (packageCompilation.packageContext().project().kind().equals(ProjectKind.BUILD_PROJECT)) {
            try {
                new Target(packageCompilation.packageContext().project().targetDir());
            } catch (IOException e) {
                throw new ProjectException("error while checking permissions of target directory", e);
            }
        }
        return packageCompilation.getCompilerBackend(jdkVersion,
                (targetPlatform -> new JBallerinaBackend(packageCompilation, jdkVersion, shrink)));
    }

    private JBallerinaBackend(PackageCompilation packageCompilation, JvmTarget jdkVersion, boolean shrink) {
        this.jdkVersion = jdkVersion;
        this.packageCompilation = packageCompilation;
        this.packageContext = packageCompilation.packageContext();
        this.pkgResolution = packageContext.getResolution();
        this.jarResolver = new JarResolver(this, packageContext.getResolution());

        ProjectEnvironment projectEnvContext = this.packageContext.project().projectEnvironmentContext();
        this.packageCache = projectEnvContext.getService(PackageCache.class);
        this.compilerContext = projectEnvContext.getService(CompilerContext.class);
        this.interopValidator = InteropValidator.getInstance(compilerContext);
        this.jvmCodeGenerator = CodeGenerator.getInstance(compilerContext);
        this.conflictedJars = new ArrayList<>();
        performCodeGen(shrink);
    }

    PackageContext packageContext() {
        return this.packageContext;
    }

    private void performCodeGen(boolean shrink) {
        if (codeGenCompleted) {
            return;
        }

        List<Diagnostic> diagnostics = new ArrayList<>();
        // add package resolution diagnostics
        diagnostics.addAll(this.packageContext.getResolution().diagnosticResult().allDiagnostics);
        // add ballerina toml diagnostics
        diagnostics.addAll(this.packageContext.packageManifest().diagnostics().diagnostics());
        // collect compilation diagnostics
        List<Diagnostic> moduleDiagnostics = new ArrayList<>();
        for (ModuleContext moduleContext : pkgResolution.topologicallySortedModuleList()) {
            if (moduleContext.moduleId().packageId().equals(packageContext.packageId())) {
                if (packageCompilation.diagnosticResult().hasErrors()) {
                    for (Diagnostic diagnostic : moduleContext.diagnostics()) {
                        moduleDiagnostics.add(
                                new PackageDiagnostic(diagnostic, moduleContext.descriptor(), moduleContext.project()));
                    }
                    continue;
                }
            }
            // We can't generate backend code when one of its dependencies have errors.
            if (!this.packageContext.getResolution().diagnosticResult().hasErrors() && !hasErrors(moduleDiagnostics)) {
                moduleContext.generatePlatformSpecificCode(compilerContext, this);
            }
            for (Diagnostic diagnostic : moduleContext.diagnostics()) {
                moduleDiagnostics.add(
                        new PackageDiagnostic(diagnostic, moduleContext.descriptor(), moduleContext.project()));
            }

            if (shrink) {
                ModuleContext.shrinkDocuments(moduleContext);
            }
            if (moduleContext.project().kind() == ProjectKind.BALA_PROJECT) {
                moduleContext.cleanBLangPackage();
            }
        }
        // add compilation diagnostics
        diagnostics.addAll(moduleDiagnostics);
        // add plugin diagnostics
        diagnostics.addAll(this.packageContext.getPackageCompilation().pluginDiagnostics());

        this.diagnosticResult = new DefaultDiagnosticResult(diagnostics);
        codeGenCompleted = true;
    }

    private boolean hasErrors(List<Diagnostic> diagnostics) {
        for (Diagnostic diagnostic : diagnostics) {
            if (diagnostic.diagnosticInfo().severity() == DiagnosticSeverity.ERROR) {
                return true;
            }
        }
        return false;
    }

    public DiagnosticResult diagnosticResult() {
        return diagnosticResult;
    }

    public EmitResult emit(OutputType outputType, Path filePath) {
        Path generatedArtifact = null;

        if (diagnosticResult.hasErrors()) {
            return new EmitResult(false, new DefaultDiagnosticResult(new ArrayList<>()), generatedArtifact);
        }

        List<Diagnostic> emitResultDiagnostics = new ArrayList<>();
        generatedArtifact = switch (outputType) {
            case GRAAL_EXEC -> emitGraalExecutable(filePath, emitResultDiagnostics);
            case EXEC -> emitExecutable(filePath, emitResultDiagnostics);
            case BALA -> emitBala(filePath);
            default -> throw new RuntimeException("Unexpected output type: " + outputType);
        };

        ArrayList<Diagnostic> allDiagnostics = new ArrayList<>(diagnosticResult.allDiagnostics);
        // Add lifecycle plugin diagnostics.
        List<Diagnostic> pluginDiagnostics = packageCompilation.notifyCompilationCompletion(filePath);
        if (!pluginDiagnostics.isEmpty()) {
            emitResultDiagnostics.addAll(pluginDiagnostics);
        }
        // Add jar resolver diagnostics.
        emitResultDiagnostics.addAll(jarResolver().diagnosticResult().diagnostics());
        // JBallerinaBackend diagnostics contains all diagnostics.
        // EmitResult will only contain diagnostics related to emitting the executable.
        allDiagnostics.addAll(emitResultDiagnostics);
        diagnosticResult = new DefaultDiagnosticResult(allDiagnostics);

        // TODO handle the EmitResult properly
        return new EmitResult(true, new DefaultDiagnosticResult(emitResultDiagnostics), generatedArtifact);
    }

    private Path emitBala(Path filePath) {
        JBallerinaBalaWriter writer = new JBallerinaBalaWriter(this);
        return writer.write(filePath);
    }

    @Override
    public Collection<PlatformLibrary> platformLibraryDependencies(PackageId packageId) {
        return getPlatformLibraries(packageId);
    }

    @Override
    public Collection<PlatformLibrary> platformLibraryDependencies(PackageId packageId,
                                                                   PlatformLibraryScope scope) {

        return getPlatformLibraries(packageId)
                .stream()
                .filter(platformLibrary -> platformLibrary.scope() == scope)
                .collect(Collectors.toList());
    }

    private List<PlatformLibrary> getPlatformLibraries(PackageId packageId) {
        Package pkg = packageCache.getPackageOrThrow(packageId);
        Map<String, PackageManifest.Platform> platforms = pkg.manifest().platforms();
        List<PlatformLibrary> platformLibraries = new ArrayList<>();
        for (Map.Entry<String, PackageManifest.Platform> entry : platforms.entrySet()) {
            PackageManifest.Platform javaPlatform = entry.getValue();
            String platform = entry.getKey();
            if (javaPlatform == null || javaPlatform.dependencies().isEmpty()) {
                continue;
            }
            for (Map<String, Object> dependency : javaPlatform.dependencies()) {
                String artifactId = (String) dependency.get(JarLibrary.KEY_ARTIFACT_ID);
                String version = (String) dependency.get(JarLibrary.KEY_VERSION);
                String groupId = (String) dependency.get(JarLibrary.KEY_GROUP_ID);

                String dependencyFilePath = (String) dependency.get(JarLibrary.KEY_PATH);
                PlatformLibraryScope dependencyScope = getPlatformLibraryScope(dependency);

                // If dependencyFilePath does not exist, resolve it using MavenResolver
                if (dependencyFilePath == null || dependencyFilePath.isEmpty()) {
                    // if the dependency is transitive and has provided scope, check the current package's
                    // Ballerina.toml for provided platform dependencies
                    if (Objects.equals(dependencyScope, PlatformLibraryScope.PROVIDED)
                            && !Objects.equals(packageId, this.packageContext().packageId())) {
                        dependencyFilePath = getPlatformLibPathFromProvided(platform, groupId, artifactId, version);
                        Path jarPath = Paths.get(dependencyFilePath);
                        if (!jarPath.isAbsolute()) {
                            jarPath = this.packageContext().project().sourceRoot().resolve(jarPath);
                        }
                        dependencyFilePath = jarPath.toString();
                    } else {
                        dependencyFilePath = getPlatformLibPath(groupId, artifactId, version);
                    }
                    dependency.put(JarLibrary.KEY_PATH, dependencyFilePath);
                }
                // If the path is relative we will convert to absolute relative to Ballerina.toml file
                Path jarPath = Paths.get(dependencyFilePath);
                if (!jarPath.isAbsolute()) {
                    jarPath = pkg.project().sourceRoot().resolve(jarPath);
                }
                platformLibraries.add(new JarLibrary(jarPath, dependencyScope, artifactId, groupId, version,
                        pkg.packageOrg().value() + "/" + pkg.packageName().value()));
            }
        }
        return platformLibraries;
    }

    @Override
    public PlatformLibrary codeGeneratedLibrary(PackageId packageId, ModuleName moduleName) {
        return codeGeneratedLibrary(packageId, moduleName, PlatformLibraryScope.DEFAULT, JAR_FILE_NAME_SUFFIX);
    }

    @Override
    public PlatformLibrary codeGeneratedTestLibrary(PackageId packageId, ModuleName moduleName) {
        return codeGeneratedLibrary(packageId, moduleName, PlatformLibraryScope.DEFAULT,
                TEST_JAR_FILE_NAME_SUFFIX + JAR_FILE_NAME_SUFFIX);
    }

    @Override
    public PlatformLibrary runtimeLibrary() {
        return new JarLibrary(ProjectUtils.getBallerinaRTJarPath(), PlatformLibraryScope.DEFAULT);
    }

    @Override
    public TargetPlatform targetPlatform() {
        return jdkVersion;
    }

    // TODO This method should be moved to some other class owned by the JBallerinaBackend
    @Override
    public void performCodeGen(ModuleContext moduleContext, CompilationCache compilationCache) {
        BLangPackage bLangPackage = moduleContext.bLangPackage();
        interopValidator.validate(moduleContext.moduleId(), this, bLangPackage);
        if (bLangPackage.getErrorCount() > 0) {
            return;
        }
        CompiledJarFile compiledJarFile = jvmCodeGenerator.generate(bLangPackage);
        if (compiledJarFile == null) {
            throw new IllegalStateException("Missing generated jar, module: " + moduleContext.moduleName());
        }
        String jarFileName = getJarFileName(moduleContext) + JAR_FILE_NAME_SUFFIX;
        try {
            ByteArrayOutputStream byteStream = JarWriter.write(compiledJarFile, getResources(moduleContext));
            compilationCache.cachePlatformSpecificLibrary(this, jarFileName, byteStream);
        } catch (IOException e) {
            throw new ProjectException("Failed to cache generated jar, module: " + moduleContext.moduleName());
        }
        // skip generation of the test jar if --with-tests option is not provided
        if (moduleContext.project().buildOptions().skipTests()) {
            return;
        }

        if (!bLangPackage.hasTestablePackage()) {
            return;
        }

        String testJarFileName = jarFileName + TEST_JAR_FILE_NAME_SUFFIX;
        CompiledJarFile compiledTestJarFile = jvmCodeGenerator.generateTestModule(bLangPackage.testablePkgs.get(0));
        try {
            ByteArrayOutputStream byteStream = JarWriter.write(compiledTestJarFile, getAllResources(moduleContext));
            compilationCache.cachePlatformSpecificLibrary(this, testJarFileName, byteStream);
        } catch (IOException e) {
            throw new ProjectException("Failed to cache generated test jar, module: " + moduleContext.moduleName());
        }
    }

    @Override
    public String libraryFileExtension() {
        return JAR_FILE_EXTENSION;
    }

    public JarResolver jarResolver() {
        return jarResolver;
    }

    public List<JarConflict> conflictedJars() {
        return conflictedJars;
    }

    // TODO Can we move this method to Module.displayName()
    private String getJarFileName(ModuleContext moduleContext) {
        String jarName;
        if (moduleContext.project().kind() == ProjectKind.SINGLE_FILE_PROJECT) {
            DocumentId documentId = moduleContext.srcDocumentIds().iterator().next();
            String documentName = moduleContext.documentContext(documentId).name();
            jarName = getFileNameWithoutExtension(documentName);
        } else {
            jarName = getThinJarFileName(moduleContext.descriptor().org(),
                                         moduleContext.moduleName().toString(),
                                         moduleContext.descriptor().version());
        }

        return jarName;
    }

    private void assembleExecutableJar(Path executableFilePath,
                                       Manifest manifest,
                                       Collection<JarLibrary> jarLibraries) throws IOException {
        // Used to prevent adding duplicated entries during the final jar creation.
        HashMap<String, JarLibrary> copiedEntries = new HashMap<>();

        // Used to process SPI related metadata entries separately. The reason is unlike the other entry types,
        // service loader related information should be merged together in the final executable jar creation.
        HashMap<String, StringBuilder> serviceEntries = new HashMap<>();

        try (ZipArchiveOutputStream outStream = new ZipArchiveOutputStream(
                new BufferedOutputStream(new FileOutputStream(executableFilePath.toString())))) {
            writeManifest(manifest, outStream);

            // Sort jar libraries list to avoid inconsistent jar reporting
            List<JarLibrary> sortedJarLibraries = jarLibraries.stream()
                    .sorted(Comparator.comparing(jarLibrary -> jarLibrary.path().getFileName())).toList();

            // Copy all the jars
            for (JarLibrary library : sortedJarLibraries) {
                copyJar(outStream, library, copiedEntries, serviceEntries);
            }

            // Copy merged spi services.
            for (Map.Entry<String, StringBuilder> entry : serviceEntries.entrySet()) {
                String s = entry.getKey();
                StringBuilder service = entry.getValue();
                JarArchiveEntry e = new JarArchiveEntry(s);
                outStream.putArchiveEntry(e);
                outStream.write(service.toString().getBytes(StandardCharsets.UTF_8));
                outStream.closeArchiveEntry();
            }
        }
    }

    private void writeManifest(Manifest manifest, ZipArchiveOutputStream outStream) throws IOException {
        JarArchiveEntry e = new JarArchiveEntry(JarFile.MANIFEST_NAME);
        outStream.putArchiveEntry(e);
        manifest.write(new BufferedOutputStream(outStream));
        outStream.closeArchiveEntry();
    }

    private Manifest createManifest() {
        // Getting the jarFileName of the root module of this executable
        PlatformLibrary rootModuleJarFile = codeGeneratedLibrary(packageContext.packageId(),
                packageContext.defaultModuleContext().moduleName());

        String mainClassName;
        try (JarInputStream jarStream = new JarInputStream(Files.newInputStream(rootModuleJarFile.path()))) {
            Manifest mf = jarStream.getManifest();
            mainClassName = (String) mf.getMainAttributes().get(Attributes.Name.MAIN_CLASS);
        } catch (IOException e) {
            throw new RuntimeException("Generated jar file cannot be found for the module: " +
                    packageContext.defaultModuleContext().moduleName());
        }

        Manifest manifest = new Manifest();
        Attributes mainAttributes = manifest.getMainAttributes();
        mainAttributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        mainAttributes.put(Attributes.Name.MAIN_CLASS, mainClassName);
        return manifest;
    }

    /**
     * Copies a given jar file into the executable fat jar.
     *
     * @param outStream     Output stream of the final uber jar.
     * @param jarLibrary    jar library.
     * @param copiedEntries Entries set will be used to ignore duplicate files.
     * @param services      Services will be used to temporary hold merged spi files.
     * @throws IOException If jar file copying is failed.
     */
    private void copyJar(ZipArchiveOutputStream outStream, JarLibrary jarLibrary,
            HashMap<String, JarLibrary> copiedEntries, HashMap<String, StringBuilder> services) throws IOException {

        ZipFile zipFile = new ZipFile(jarLibrary.path().toFile());
        ZipArchiveEntryPredicate predicate = entry -> {
            String entryName = entry.getName();
            if (entryName.equals("META-INF/MANIFEST.MF")) {
                return false;
            }

            if (entryName.startsWith("META-INF/services")) {
                StringBuilder s = services.get(entryName);
                if (s == null) {
                    s = new StringBuilder();
                    services.put(entryName, s);
                }
                char c = '\n';

                int len;
                try (BufferedInputStream inStream = new BufferedInputStream(zipFile.getInputStream(entry))) {
                    while ((len = inStream.read()) != -1) {
                        c = (char) len;
                        s.append(c);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                if (c != '\n') {
                    s.append('\n');
                }

                // Its not required to copy SPI entries in here as we'll be adding merged SPI related entries
                // separately. Therefore the predicate should be set as false.
                return false;
            }

            // Skip already copied files or excluded extensions.
            if (isCopiedEntry(entryName, copiedEntries)) {
                addConflictedJars(jarLibrary, copiedEntries, entryName);
                return false;
            }
            if (isExcludedEntry(entryName)) {
                return false;
            }
            // SPIs will be merged first and then put into jar separately.
            copiedEntries.put(entryName, jarLibrary);
            return true;
        };

        // Transfers selected entries from this zip file to the output stream, while preserving its compression and
        // all the other original attributes.
        zipFile.copyRawEntries(outStream, predicate);
        zipFile.close();
    }

    private static boolean isCopiedEntry(String entryName, HashMap<String, JarLibrary> copiedEntries) {
        return copiedEntries.containsKey(entryName);
    }

    private static boolean isExcludedEntry(String entryName) {
        return excludeExtensions.contains(entryName.substring(entryName.lastIndexOf('.') + 1));
    }

    private PlatformLibrary codeGeneratedLibrary(PackageId packageId,
                                                 ModuleName moduleName,
                                                 PlatformLibraryScope scope,
                                                 String fileNameSuffix) {
        Package pkg = packageCache.getPackageOrThrow(packageId);
        ProjectEnvironment projectEnvironment = pkg.project().projectEnvironmentContext();
        CompilationCache compilationCache = projectEnvironment.getService(CompilationCache.class);
        String jarFileName = getJarFileName(pkg.packageContext().moduleContext(moduleName)) + fileNameSuffix;
        Optional<Path> platformSpecificLibrary = compilationCache.getPlatformSpecificLibrary(
                this, jarFileName);
        return new JarLibrary(platformSpecificLibrary.orElseThrow(
                () -> new IllegalStateException("Cannot find the generated jar library for module: " + moduleName)),
                scope);
    }

    private Path emitExecutable(Path executableFilePath, List<Diagnostic> emitResultDiagnostics) {
        Manifest manifest = createManifest();
        Collection<JarLibrary> jarLibraries = jarResolver.getJarFilePathsRequiredForExecution();
        // Add warning when provided platform dependencies are found
        addProvidedDependencyWarning(emitResultDiagnostics);
        try {
            assembleExecutableJar(executableFilePath, manifest, jarLibraries);
        } catch (IOException e) {
            throw new ProjectException("error while creating the executable jar file for package '" +
                    this.packageContext.packageName().toString() + "' : " + e.getMessage(), e);
        }
        return executableFilePath;
    }

    private Path emitGraalExecutable(Path executableFilePath, List<Diagnostic> emitResultDiagnostics) {
        // Run create executable
        emitExecutable(executableFilePath, emitResultDiagnostics);

        String nativeImageName;
        String[] command;
        Project project = this.packageContext().project();
        String nativeImageCommand = System.getenv("GRAALVM_HOME");

        if (nativeImageCommand == null) {
            throw new ProjectException("GraalVM installation directory not found. Set GRAALVM_HOME as an " +
                    "environment variable\nHINT: To install GraalVM, follow the link: " +
                    "https://ballerina.io/learn/build-the-executable-locally/#configure-graalvm");
        }
        nativeImageCommand += File.separator + BIN_DIR_NAME + File.separator
                + (OS.contains("win") ? "native-image.cmd" : "native-image");

        File commandExecutable = Paths.get(nativeImageCommand).toFile();
        if (!commandExecutable.exists()) {
            throw new ProjectException("cannot find '" + commandExecutable.getName() + "' in the GRAALVM_HOME/bin " +
                    "directory. Install it using: gu install native-image");
        }

        String graalVMBuildOptions = project.buildOptions().graalVMBuildOptions();
        List<String> nativeArgs = new ArrayList<>();
        Path nativeConfigPath = packageContext.project().targetDir().resolve("cache");

        if (project.kind().equals(ProjectKind.SINGLE_FILE_PROJECT)) {
            String fileName = project.sourceRoot().toFile().getName();
            nativeImageName = fileName.substring(0, fileName.lastIndexOf(DOT));
            nativeArgs.addAll(Arrays.asList(graalVMBuildOptions, "-jar",
                    executableFilePath.toString(),
                    "-H:Name=" + nativeImageName,
                    "--no-fallback"));
        } else {
            nativeImageName = project.currentPackage().packageName().toString();
            nativeArgs.addAll(Arrays.asList(graalVMBuildOptions, "-jar",
                    executableFilePath.toString(),
                    "-H:Name=" + nativeImageName,
                    "-H:Path=" + executableFilePath.getParent(),
                    "--no-fallback"));
        }

        if (!Files.exists(nativeConfigPath)) {
            try {
                Files.createDirectories(nativeConfigPath);
            } catch (IOException e) {
                throw new ProjectException("error while generating the necessary graalvm argument file", e);
            }
        }

        // There is a command line length limitations in Windows. Therefore, we need to write the arguments to a
        // file and use it as an argument.
        try (FileWriter nativeArgumentWriter = new FileWriter(nativeConfigPath.resolve("native-image-args.txt")
                .toString(), Charset.defaultCharset())) {
            nativeArgumentWriter.write(String.join(" ", nativeArgs));
            nativeArgumentWriter.flush();
        } catch (IOException e) {
            throw new ProjectException("error while generating the necessary graalvm argument file", e);
        }

        command = new String[]{
                nativeImageCommand,
                "@" + (nativeConfigPath.resolve("native-image-args.txt"))
        };

        try {
            ProcessBuilder builder = new ProcessBuilder();
            builder.command(command);
            builder.inheritIO();
            Process process = builder.start();

            if (process.waitFor() != 0) {
                throw new ProjectException("unable to create native image");
            }
        } catch (IOException e) {
            throw new ProjectException("unable to create native image : " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return Path.of(FilenameUtils.removeExtension(executableFilePath.toString()));
    }

    private Map<String, byte[]> getResources(ModuleContext moduleContext) {
        Map<String, byte[]> resourceMap = new HashMap<>();
        for (DocumentId documentId : moduleContext.resourceIds()) {
            String resourceName = ProjectConstants.RESOURCE_DIR_NAME + "/"
                    + moduleContext.descriptor().org().toString() + "/"
                    + moduleContext.moduleName().toString() + "/"
                    + moduleContext.descriptor().version().value().major() + "/"
                    + moduleContext.resourceContext(documentId).name();
            resourceMap.put(resourceName, moduleContext.resourceContext(documentId).content());
        }
        return resourceMap;
    }

    private Map<String, byte[]> getAllResources(ModuleContext moduleContext) {
        Map<String, byte[]> resourceMap = getResources(moduleContext);
        for (DocumentId documentId : moduleContext.testResourceIds()) {
            String resourceName = ProjectConstants.RESOURCE_DIR_NAME + "/"
                    + moduleContext.descriptor().org() + "/"
                    + moduleContext.moduleName().toString() + "/"
                    + moduleContext.descriptor().version().value().major() + "/"
                    + moduleContext.resourceContext(documentId).name();
            resourceMap.put(resourceName, moduleContext.resourceContext(documentId).content());
        }
        return resourceMap;
    }

    private PlatformLibraryScope getPlatformLibraryScope(Map<String, Object> dependency) {
        PlatformLibraryScope scope;
        String scopeValue = (String) dependency.get(JarLibrary.KEY_SCOPE);
        if (scopeValue == null || scopeValue.isEmpty()) {
            scope = PlatformLibraryScope.DEFAULT;
        } else if (PlatformLibraryScope.TEST_ONLY.getStringValue().equals(scopeValue)) {
            scope = PlatformLibraryScope.TEST_ONLY;
        } else if (PlatformLibraryScope.PROVIDED.getStringValue().equals(scopeValue)) {
            scope = PlatformLibraryScope.PROVIDED;
        } else {
            throw new ProjectException("Invalid scope '" + scopeValue + "' is defined with the " +
                    "platform-specific library path: " + dependency.get(JarLibrary.KEY_PATH));
        }
        return scope;
    }

    /**
     * Get platform lib path for given maven dependency.
     *
     * @param groupId    group id
     * @param artifactId artifact id
     * @param version    version
     * @return platform lib path
     */
    private String getPlatformLibPath(String groupId, String artifactId, String version) {
        String targetRepo =
                this.packageContext.project().targetDir().resolve(ProjectConstants.TARGET_DIR_NAME)
                        + File.separator + "platform" + "-libs";
        MavenResolver resolver = new MavenResolver(targetRepo);
        try {
            Dependency dependency = resolver.resolve(groupId, artifactId, version, false);
            return Utils.getJarPath(targetRepo, dependency);
        } catch (MavenResolverException e) {
            throw new ProjectException("cannot resolve " + artifactId + ": " + e.getMessage());
        }
    }

    /**
     * Get platform lib path for platform libs with provided scope in dependencies.
     *
     * @param platform java platform of the dependency
     * @param groupId group id
     * @param artifactId artifact id
     * @param version version
     * @return platform lib path provided by user
     */
    private String getPlatformLibPathFromProvided(String platform, String groupId, String artifactId, String version) {
        PackageManifest.Platform currentPlatform = this.packageContext().packageManifest().platform(platform);
        if (currentPlatform != null) {
            for (Map<String, Object> platformDep :
                    currentPlatform.dependencies()) {
                String depArtifactId = (String) platformDep.get(JarLibrary.KEY_ARTIFACT_ID);
                String depVersion = (String) platformDep.get(JarLibrary.KEY_VERSION);
                String depGroupId = (String) platformDep.get(JarLibrary.KEY_GROUP_ID);
                String depFilepath = (String) platformDep.get(JarLibrary.KEY_PATH);
                if (artifactId.equals(depArtifactId) && groupId.equals(depGroupId)
                        && version.equals(depVersion) && depFilepath != null && !depFilepath.isEmpty()) {
                    return depFilepath;
                }
            }
        }
        throw new ProjectException(String.format("cannot resolve '%s:%s:%s'. Dependencies with " +
                "'%s' scope need to be manually added to Ballerina.toml.", groupId, artifactId, version,
                PlatformLibraryScope.PROVIDED.getStringValue()));
    }

    /**
     * Enum to represent output types.
     */
    public enum OutputType {
        EXEC("exec"),
        BALA("bala"),
        GRAAL_EXEC("graal_exec")
        ;

        private String value;

        OutputType(String value) {
            this.value = value;
        }
    }

    JvmTarget jdkVersion() {
        return jdkVersion;
    }

    /**
     * Inner class to represent jar conflict.
     */
    public static class JarConflict {
        JarLibrary firstJarLibrary;
        JarLibrary secondJarLibrary;
        List<String> classes;

        JarConflict(JarLibrary firstJarLibrary, JarLibrary secondJarLibrary, List<String> classes) {
            this.firstJarLibrary = firstJarLibrary;
            this.secondJarLibrary = secondJarLibrary;
            this.classes = classes;
        }

        JarLibrary firstJarLibrary() {
            return firstJarLibrary;
        }

        void addClasses(String entry) {
            classes.add(entry);
        }

        public String getWarning(boolean listClasses) {
            String conflictedJarPkg1 = "";
            String conflictedJarPkg2 = "";
            if (firstJarLibrary.packageName().isPresent()) {
                conflictedJarPkg1 = " dependency of '" + firstJarLibrary.packageName().get() + "'";
            }
            if (secondJarLibrary.packageName().isPresent()) {
                conflictedJarPkg2 = " dependency of '" + secondJarLibrary.packageName().get() + "'";
            }

            StringBuilder warning = new StringBuilder(
                    "\t\t'" + firstJarLibrary.path().getFileName() + "'" + conflictedJarPkg1 + " conflict with '"
                            + secondJarLibrary.path().getFileName() + "'" + conflictedJarPkg2);

            if (listClasses) {
                for (String conflictedClass : classes) {
                    warning.append("\n\t\t\t").append(conflictedClass);
                }
            }
            return String.valueOf(warning);
        }
    }

    private void addConflictedJars(JarLibrary jarLibrary, HashMap<String, JarLibrary> copiedEntries, String entryName) {
        if (entryName.endsWith(CLASS_FILE_SUFFIX) && !entryName.endsWith("module-info.class")) {
            JarLibrary conflictingJar = copiedEntries.get(entryName);

            // Ignore if conflicting jars has same name
            Path jarFileName = jarLibrary.path().getFileName();
            Path conflictingJarFileName = conflictingJar.path().getFileName();
            if (jarFileName != null && conflictingJarFileName != null &&
                    !jarFileName.toString().equals(conflictingJarFileName.toString())) {
                JarConflict jarConflict = getJarConflict(conflictingJar);

                // If jar conflict already exists
                if (jarConflict != null) {
                    jarConflict.addClasses(entryName);
                } else { // New jar conflict
                    this.conflictedJars.add(new JarConflict(conflictingJar, jarLibrary,
                                                            new ArrayList<>(Collections.singletonList(entryName))));
                }
            }
        }
    }

    private JarConflict getJarConflict(JarLibrary conflictingJar) {
        for (JarConflict jarConflict: this.conflictedJars) {
            if (jarConflict.firstJarLibrary().path() == conflictingJar.path()) {
                return jarConflict;
            }
        }
        return null;
    }

    private void addProvidedDependencyWarning(List<Diagnostic> emitResultDiagnostics) {
        if (!jarResolver.providedPlatformLibs().isEmpty()) {
            DiagnosticInfo diagnosticInfo = new DiagnosticInfo(
                    ProjectDiagnosticErrorCode.PROVIDED_PLATFORM_JAR_IN_EXECUTABLE.diagnosticId(),
                    String.format("Detected platform dependencies with '%s' scope. Redistribution is discouraged" +
                            " due to potential license restrictions%n", PlatformLibraryScope.PROVIDED.getStringValue()),
                    DiagnosticSeverity.WARNING);
            emitResultDiagnostics.add(new PackageDiagnostic(diagnosticInfo,
                    this.packageContext().descriptor().name().toString()));
        }
    }
}
