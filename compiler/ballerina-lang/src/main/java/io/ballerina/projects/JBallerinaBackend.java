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
import io.ballerina.projects.internal.jballerina.JarWriter;
import io.ballerina.projects.internal.model.Target;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.projects.util.ProjectUtils;
import io.ballerina.tools.diagnostics.Diagnostic;
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
import org.wso2.ballerinalang.compiler.semantics.analyzer.ObservabilitySymbolCollectorRunner;
import org.wso2.ballerinalang.compiler.spi.ObservabilitySymbolCollector;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.util.Lists;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
        // Check if the project has write permissions
        if (packageCompilation.packageContext().project().kind().equals(ProjectKind.BUILD_PROJECT)) {
            try {
                new Target(packageCompilation.packageContext().project().targetDir());
            } catch (IOException e) {
                throw new ProjectException("error while checking permissions of target directory", e);
            }
        }
        return packageCompilation.getCompilerBackend(jdkVersion,
                (targetPlatform -> new JBallerinaBackend(packageCompilation, jdkVersion)));
    }

    private JBallerinaBackend(PackageCompilation packageCompilation, JvmTarget jdkVersion) {
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
        // TODO: Move to a compiler extension once Compiler revamp is complete
        if (packageContext.compilationOptions().observabilityIncluded()) {
            ObservabilitySymbolCollector observabilitySymbolCollector
                    = ObservabilitySymbolCollectorRunner.getInstance(compilerContext);
            observabilitySymbolCollector.process(packageContext.project());
        }
        this.conflictedJars = new ArrayList<>();
        performCodeGen();
    }

    PackageContext packageContext() {
        return this.packageContext;
    }

    private void performCodeGen() {
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
            // If modules from the current package are being processed
            // we do an overall check on the diagnostics of the package
            if (moduleContext.moduleId().packageId().equals(packageContext.packageId())) {
                if (packageCompilation.diagnosticResult().hasErrors()) {
                    moduleDiagnostics.addAll(packageCompilation.diagnosticResult().diagnostics());
                    break;
                }
            }

            // We can't generate backend code when one of its dependencies have errors.
            if (hasNoErrors(moduleDiagnostics)) {
                moduleContext.generatePlatformSpecificCode(compilerContext, this);
            }
            for (Diagnostic diagnostic : moduleContext.diagnostics()) {
                moduleDiagnostics.add(
                        new PackageDiagnostic(diagnostic, moduleContext.descriptor(), moduleContext.project()));
            }
        }
        // add compilation diagnostics
        diagnostics.addAll(moduleDiagnostics);
        // add plugin diagnostics
        diagnostics.addAll(this.packageContext.getPackageCompilation().pluginDiagnostics());

        diagnostics = diagnostics.stream().distinct().collect(Collectors.toList());

        this.diagnosticResult = new DefaultDiagnosticResult(diagnostics);
        codeGenCompleted = true;
    }

    private boolean hasNoErrors(List<Diagnostic> diagnostics) {
        for (Diagnostic diagnostic : diagnostics) {
            if (diagnostic.diagnosticInfo().severity() == DiagnosticSeverity.ERROR) {
                return false;
            }
        }
        return true;
    }

    public DiagnosticResult diagnosticResult() {
        return diagnosticResult;
    }

    // TODO EmitResult should not contain compilation diagnostics.
    public EmitResult emit(OutputType outputType, Path filePath) {
        Path generatedArtifact = null;

        if (diagnosticResult.hasErrors()) {
            return new EmitResult(false, diagnosticResult, generatedArtifact);
        }

        switch (outputType) {
            case GRAAL_EXEC:
                generatedArtifact = emitGraalExecutable(filePath);
                break;
            case EXEC:
                generatedArtifact = emitExecutable(filePath);
                break;
            case BALA:
                generatedArtifact = emitBala(filePath);
                break;
            default:
                throw new RuntimeException("Unexpected output type: " + outputType);
        }

        List<Diagnostic> pluginDiagnostics = packageCompilation.notifyCompilationCompletion(filePath);
        if (!pluginDiagnostics.isEmpty()) {
            ArrayList<Diagnostic> diagnostics = new ArrayList<>(diagnosticResult.allDiagnostics);
            diagnostics.addAll(pluginDiagnostics);

            diagnosticResult = new DefaultDiagnosticResult(diagnostics);
        }

        // TODO handle the EmitResult properly
        return new EmitResult(true, diagnosticResult, generatedArtifact);
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
        PackageManifest.Platform javaPlatform = pkg.manifest().platform(jdkVersion.code());
        if (javaPlatform == null || javaPlatform.dependencies().isEmpty()) {
            return Collections.emptyList();
        }

        List<PlatformLibrary> platformLibraries = new ArrayList<>();
        for (Map<String, Object> dependency : javaPlatform.dependencies()) {
            String artifactId = (String) dependency.get(JarLibrary.KEY_ARTIFACT_ID);
            String version = (String) dependency.get(JarLibrary.KEY_VERSION);
            String groupId = (String) dependency.get(JarLibrary.KEY_GROUP_ID);

            String dependencyFilePath = (String) dependency.get(JarLibrary.KEY_PATH);
            // If dependencyFilePath does not exists, resolve it using MavenResolver
            if (dependencyFilePath == null || dependencyFilePath.isEmpty()) {
                dependencyFilePath = getPlatformLibPath(groupId, artifactId, version);
            }

            // If the path is relative we will covert to absolute relative to Ballerina.toml file
            Path jarPath = Paths.get(dependencyFilePath);
            if (!jarPath.isAbsolute()) {
                jarPath = pkg.project().sourceRoot().resolve(jarPath);
            }

            PlatformLibraryScope scope = getPlatformLibraryScope(dependency);
            platformLibraries.add(new JarLibrary(jarPath, scope, artifactId, groupId, version,
                                                 pkg.packageOrg().value() + "/" + pkg.packageName().value()));
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
                    .sorted(Comparator.comparing(jarLibrary -> jarLibrary.path().getFileName()))
                    .collect(Collectors.toList());

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
        return copiedEntries.keySet().contains(entryName);
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

    private Path emitExecutable(Path executableFilePath) {
        Manifest manifest = createManifest();
        Collection<JarLibrary> jarLibraries = jarResolver.getJarFilePathsRequiredForExecution();

        try {
            assembleExecutableJar(executableFilePath, manifest, jarLibraries);
        } catch (IOException e) {
            throw new ProjectException("error while creating the executable jar file for package '" +
                    this.packageContext.packageName().toString() + "' : " + e.getMessage(), e);
        }
        return executableFilePath;
    }

    private Path emitGraalExecutable(Path executableFilePath) {
        // Run create executable
        emitExecutable(executableFilePath);

        String nativeImageName;
        String[] command;
        Project project = this.packageContext().project();
        String nativeImageCommand = System.getenv("GRAALVM_HOME");

        if (nativeImageCommand == null) {
            throw new ProjectException("GraalVM installation directory not found. Set GRAALVM_HOME as an " +
                    "environment variable");
        }
        nativeImageCommand += File.separator + BIN_DIR_NAME + File.separator
                + (OS.contains("win") ? "native-image.cmd" : "native-image");

        File commandExecutable = Paths.get(nativeImageCommand).toFile();
        if (!commandExecutable.exists()) {
            throw new ProjectException("Cannot find '" + commandExecutable.getName() + "' in the GRAALVM_HOME. " +
                    "Install it using: gu install native-image");
        }

        if (project.kind().equals(ProjectKind.SINGLE_FILE_PROJECT)) {
            String fileName = project.sourceRoot().toFile().getName();
            nativeImageName = fileName.substring(0, fileName.lastIndexOf(DOT));
            command = new String[] {
                    nativeImageCommand,
                    "-jar",
                    executableFilePath.toString(),
                    "-H:Name=" + nativeImageName,
                    "--no-fallback"
            };
        } else {
            nativeImageName = project.currentPackage().packageName().toString();
            command = new String[]{
                    nativeImageCommand,
                    "-jar",
                    executableFilePath.toString(),
                    "-H:Name=" + nativeImageName,
                    "-H:Path=" + executableFilePath.getParent(),
                    "--no-fallback"
            };
        }

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

        Path graalexectablepath = Path.of(FilenameUtils.removeExtension(executableFilePath.toString()));
        return graalexectablepath;
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
        } else if (scopeValue.equals(PlatformLibraryScope.TEST_ONLY.getStringValue())) {
            scope = PlatformLibraryScope.TEST_ONLY;
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
                this.packageContext.project().targetDir().resolve(ProjectConstants.TARGET_DIR_NAME).toString()
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
        if (entryName.endsWith(".class") && !entryName.endsWith("module-info.class")) {
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
}
