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

import io.ballerina.projects.environment.PackageResolver;
import io.ballerina.projects.environment.ProjectEnvironment;
import io.ballerina.projects.internal.DefaultDiagnosticResult;
import io.ballerina.projects.internal.jballerina.JarWriter;
import io.ballerina.projects.util.ProjectUtils;
import io.ballerina.tools.diagnostics.Diagnostic;
import org.apache.commons.compress.archivers.jar.JarArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntryPredicate;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.wso2.ballerinalang.compiler.CompiledJarFile;
import org.wso2.ballerinalang.compiler.bir.codegen.CodeGenerator;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.util.Lists;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

import static io.ballerina.projects.util.FileUtils.getFileNameWithoutExtension;

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

    private final PackageCompilation pkgCompilation;
    private final JdkVersion jdkVersion;
    private final PackageContext packageContext;
    private final PackageResolver packageResolver;
    private final CompilerContext compilerContext;
    private final CodeGenerator jvmCodeGenerator;

    private DiagnosticResult diagnosticResult;
    private boolean codeGenCompleted;
    private List<Path> allJarFilePaths;
    private ClassLoader classLoaderWithAllJars;

    public static JBallerinaBackend from(PackageCompilation packageCompilation, JdkVersion jdkVersion) {
        return new JBallerinaBackend(packageCompilation, jdkVersion);
    }

    private JBallerinaBackend(PackageCompilation packageCompilation, JdkVersion jdkVersion) {
        this.pkgCompilation = packageCompilation;
        this.jdkVersion = jdkVersion;
        this.packageContext = packageCompilation.packageContext();

        ProjectEnvironment projectEnvContext = this.packageContext.project().projectEnvironmentContext();
        this.packageResolver = projectEnvContext.getService(PackageResolver.class);
        this.compilerContext = projectEnvContext.getService(CompilerContext.class);
        this.jvmCodeGenerator = CodeGenerator.getInstance(compilerContext);
        performCodeGen();
    }

    private void performCodeGen() {
        if (codeGenCompleted) {
            return;
        }

        List<Diagnostic> diagnostics = new ArrayList<>();
        for (ModuleContext moduleContext : pkgCompilation.sortedModuleContextList()) {
            moduleContext.generatePlatformSpecificCode(compilerContext, this);
            diagnostics.addAll(moduleContext.diagnostics());
        }

        this.diagnosticResult = new DefaultDiagnosticResult(diagnostics);
        codeGenCompleted = true;
    }

    public DiagnosticResult diagnosticResult() {
        return diagnosticResult;
    }

    // TODO EmitResult should not contain compilation diagnostics.
    public EmitResult emit(OutputType outputType, Path filePath) {
        if (diagnosticResult.hasErrors()) {
            return new EmitResult(false, diagnosticResult);
        }

        switch (outputType) {
            case EXEC:
                emitExecutable(filePath);
                break;
            case BALO:
                emitBalo(filePath);
                break;
            default:
                throw new RuntimeException("Unexpected output type: " + outputType);
        }
        // TODO handle the EmitResult properly
        return new EmitResult(true, diagnosticResult);
    }

    private void emitBalo(Path filePath) {
        JBallerinaBaloWriter writer = new JBallerinaBaloWriter(jdkVersion);
        writer.write(packageResolver.getPackage(packageContext.packageId()), filePath);
    }

    @Override
    public Collection<PlatformLibrary> platformLibraryDependencies(PackageId packageId) {
        Package pkg = packageResolver.getPackage(packageId);
        if (pkg == null) {
            // TODO Proper error handling
            throw new IllegalStateException("Cannot find a Package for the given PackageId: " + packageId);
        }
        PackageDescriptor.Platform javaPlatform = pkg.packageDescriptor().platform(jdkVersion.code());
        if (javaPlatform == null || javaPlatform.dependencies().isEmpty()) {
            return Collections.emptyList();
        }

        Collection<PlatformLibrary> platformLibraries = new ArrayList<>();
        for (Map<String, Object> dependency : javaPlatform.dependencies()) {
            String dependencyFilePath = (String) dependency.get(JarLibrary.KEY_PATH);
            // if the path is relative we will covert to absolute relative to Ballerina.toml file
            Path jarPath = Paths.get(dependencyFilePath);
            if (!jarPath.isAbsolute()) {
                jarPath = pkg.project().sourceRoot().resolve(jarPath);
            }
            platformLibraries.add(new JarLibrary(jarPath));
        }

        // TODO Where can we cache this collection
        return platformLibraries;
    }

    @Override
    public PlatformLibrary codeGeneratedLibrary(PackageId packageId, ModuleName moduleName) {
        return codeGeneratedLibrary(packageId, moduleName, JAR_FILE_NAME_SUFFIX);
    }

    @Override
    public PlatformLibrary codeGeneratedTestLibrary(PackageId packageId, ModuleName moduleName) {
        return codeGeneratedLibrary(packageId, moduleName, TEST_JAR_FILE_NAME_SUFFIX);
    }

    @Override
    public PlatformLibrary runtimeLibrary() {
        return new JarLibrary(ProjectUtils.getBallerinaRTJarPath());
    }

    @Override
    public TargetPlatform targetPlatform() {
        return jdkVersion;
    }

    // TODO This method should be moved to some other class owned by the JBallerinaBackend
    @Override
    public void performCodeGen(ModuleContext moduleContext, CompilationCache compilationCache) {
        BLangPackage bLangPackage = moduleContext.bLangPackage();
        CompiledJarFile compiledJarFile = jvmCodeGenerator.generate(moduleContext.moduleId(), this, bLangPackage);
        String jarFileName = getJarFileName(moduleContext) + JAR_FILE_NAME_SUFFIX;
        try {
            ByteArrayOutputStream byteStream = JarWriter.write(compiledJarFile);
            compilationCache.cachePlatformSpecificLibrary(this, jarFileName, byteStream);
        } catch (IOException e) {
            throw new RuntimeException("Failed to cache generated jar, module: " + moduleContext.moduleName());
        }

        // TODO Check whether the tests have been skipped
        //  Use the CompilationOptions
//        if (skipTests) {
//            return;
//        }
        if (!bLangPackage.hasTestablePackage()) {
            return;
        }

        String testJarFileName = jarFileName + TEST_JAR_FILE_NAME_SUFFIX;
        CompiledJarFile compiledTestJarFile = jvmCodeGenerator.generateTestModule(
                moduleContext.moduleId(), this, bLangPackage.testablePkgs.get(0));
        try {
            ByteArrayOutputStream byteStream = JarWriter.write(compiledTestJarFile);
            compilationCache.cachePlatformSpecificLibrary(this, testJarFileName, byteStream);
        } catch (IOException e) {
            throw new RuntimeException("Failed to cache generated test jar, module: " + moduleContext.moduleName());
        }
    }

    @Override
    public String libraryFileExtension() {
        return JAR_FILE_EXTENSION;
    }

    public ClassLoader getClassLoader() {
        if (classLoaderWithAllJars != null) {
            return classLoaderWithAllJars;
        }

        if (diagnosticResult.hasErrors()) {
            throw new IllegalStateException("Cannot create a ClassLoader: this compilation has errors.");
        }

        List<Path> allJarFilePaths = getAllJarLibraryPaths();
        URL[] urls = new URL[allJarFilePaths.size()];
        for (int index = 0; index < allJarFilePaths.size(); index++) {
            Path jarFilePath = allJarFilePaths.get(index);
            try {
                urls[index] = jarFilePath.toUri().toURL();
            } catch (MalformedURLException e) {
                // This path cannot get executed
                throw new RuntimeException("Failed to create classloader with all jar files", e);
            }
        }

        // TODO use the ClassLoader.getPlatformClassLoader() here
        classLoaderWithAllJars = AccessController.doPrivileged(
                (PrivilegedAction<URLClassLoader>) () -> new URLClassLoader(urls, ClassLoader.getSystemClassLoader())
        );

        return classLoaderWithAllJars;
    }

    // TODO Should we move these jar related methods to the JBallerinaBackend.jarResolver()
    public Collection<Path> allJars() {
        return null;
    }

    // We want to run tests in this module: ModuleName
    // We need the test-jar of MouleName
    public List<Path> allJarsWithTestsJars(ModuleName moduleName) {
        return null;
    }

    // TODO Can we move this method to Module.displayName()
    private String getJarFileName(ModuleContext moduleContext) {
        String jarName;
        if (moduleContext.project().kind() == ProjectKind.SINGLE_FILE_PROJECT) {
            DocumentId documentId = moduleContext.srcDocumentIds().iterator().next();
            String documentName = moduleContext.documentContext(documentId).name();
            jarName = getFileNameWithoutExtension(documentName);
        } else {
            ModuleName moduleName = moduleContext.moduleName();
            if (moduleName.isDefaultModuleName()) {
                jarName = moduleName.packageName().toString();
            } else {
                jarName = moduleName.moduleNamePart();
            }
        }

        return jarName;
    }

    private void assembleExecutableJar(Path executableFilePath,
                                       Manifest manifest,
                                       List<Path> jarFilePaths) throws IOException {
        // Used to prevent adding duplicated entries during the final jar creation.
        HashSet<String> copiedEntries = new HashSet<>();

        // Used to process SPI related metadata entries separately. The reason is unlike the other entry types,
        // service loader related information should be merged together in the final executable jar creation.
        HashMap<String, StringBuilder> serviceEntries = new HashMap<>();

        try (ZipArchiveOutputStream outStream = new ZipArchiveOutputStream(
                new BufferedOutputStream(new FileOutputStream(executableFilePath.toString())))) {
            writeManifest(manifest, outStream);

            // Copy all the jars
            for (Path jarFilePath : jarFilePaths) {
                copyJar(outStream, jarFilePath, copiedEntries, serviceEntries);
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
     * @param jarFilePath   Path of the source jar file.
     * @param copiedEntries Entries set will be used to ignore duplicate files.
     * @param services      Services will be used to temporary hold merged spi files.
     * @throws IOException If jar file copying is failed.
     */
    private void copyJar(ZipArchiveOutputStream outStream, Path jarFilePath, HashSet<String> copiedEntries,
                         HashMap<String, StringBuilder> services) throws IOException {

        ZipFile zipFile = new ZipFile(jarFilePath.toFile());
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
            if (isCopiedOrExcludedEntry(entryName, copiedEntries)) {
                return false;
            }
            // SPIs will be merged first and then put into jar separately.
            copiedEntries.add(entryName);
            return true;
        };

        // Transfers selected entries from this zip file to the output stream, while preserving its compression and
        // all the other original attributes.
        zipFile.copyRawEntries(outStream, predicate);
        zipFile.close();
    }

    private static boolean isCopiedOrExcludedEntry(String entryName, HashSet<String> copiedEntries) {
        return copiedEntries.contains(entryName) ||
                excludeExtensions.contains(entryName.substring(entryName.lastIndexOf(".") + 1));
    }

    private List<Path> getAllJarLibraryPaths() {
        if (allJarFilePaths != null) {
            return allJarFilePaths;
        }

        allJarFilePaths = new ArrayList<>();
        List<PackageId> sortedPackageIds = pkgCompilation.packageDependencyGraph().toTopologicallySortedList();
        for (PackageId packageId : sortedPackageIds) {
            PackageContext packageContext = packageResolver.getPackage(packageId).packageContext();
            for (ModuleId moduleId : packageContext.moduleIds()) {
                ModuleContext moduleContext = packageContext.moduleContext(moduleId);
                PlatformLibrary generatedJarLibrary = codeGeneratedLibrary(packageId, moduleContext.moduleName());
                allJarFilePaths.add(generatedJarLibrary.path());
            }

            // Add all the jar library dependencies of current package (packageId)
            // TODO Filter our test scope dependencies
            Collection<PlatformLibrary> otherJarDependencies = platformLibraryDependencies(packageId);
            otherJarDependencies.forEach(platformLibrary -> allJarFilePaths.add(platformLibrary.path()));
        }

        // Add the runtime library path
        allJarFilePaths.add(runtimeLibrary().path());
        return allJarFilePaths;
    }

    private PlatformLibrary codeGeneratedLibrary(PackageId packageId,
                                                 ModuleName moduleName,
                                                 String fileNameSuffix) {
        Package pkg = packageResolver.getPackage(packageId);
        if (pkg == null) {
            // TODO Proper error handling
            throw new IllegalStateException("Cannot find a Package for the given PackageId: " + packageId);
        }

        ProjectEnvironment projectEnvironment = pkg.project().projectEnvironmentContext();
        CompilationCache compilationCache = projectEnvironment.getService(CompilationCache.class);
        String jarFileName = getJarFileName(pkg.packageContext().moduleContext(moduleName)) + fileNameSuffix;
        Optional<Path> platformSpecificLibrary = compilationCache.getPlatformSpecificLibrary(
                this, jarFileName);
        return new JarLibrary(platformSpecificLibrary.orElseThrow(
                () -> new IllegalStateException("Cannot find the generated jar library for module: " + moduleName)));
    }

    private void emitExecutable(Path executableFilePath) {
        if (!this.packageContext.defaultModuleContext().entryPointExists()) {
            // TODO Improve error handling
            throw new RuntimeException("no entrypoint found in package: " + this.packageContext.packageName());
        }

        Manifest manifest = createManifest();
        List<Path> jarLibraryPaths = getAllJarLibraryPaths();

        try {
            assembleExecutableJar(executableFilePath, manifest, jarLibraryPaths);
        } catch (IOException e) {
            throw new RuntimeException("error while creating the executable jar file for package: " +
                    this.packageContext.packageName(), e);
        }
    }

    /**
     * Enum to represent output types.
     */
    public enum OutputType {
        EXEC("exec"),
        BALO("balo"),
        ;

        private String value;

        OutputType(String value) {
            this.value = value;
        }
    }
}
