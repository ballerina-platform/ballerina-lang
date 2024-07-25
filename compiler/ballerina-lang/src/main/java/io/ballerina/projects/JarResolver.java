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

import io.ballerina.projects.internal.DefaultDiagnosticResult;
import io.ballerina.projects.internal.PackageDiagnostic;
import io.ballerina.projects.internal.ProjectDiagnosticErrorCode;
import io.ballerina.projects.util.CodegenOptimizationUtils;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.projects.util.ProjectUtils;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil;
import org.wso2.ballerinalang.compiler.util.CompilerUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import static io.ballerina.identifier.Utils.encodeNonFunctionIdentifier;
import static io.ballerina.projects.util.ProjectConstants.ANON_ORG;
import static io.ballerina.projects.util.ProjectConstants.DOT;

// TODO move this class to a separate Java package. e.g. io.ballerina.projects.platform.jballerina
//    todo that, we would have to move PackageContext class into an internal package.

/**
 * This class works closely with JBallerinaBackend to provide various codeGenerated jars
 * and class loaders required run Ballerina programs.
 *
 * @since 2.0.0
 */
public class JarResolver {
    private final JBallerinaBackend jBalBackend;
    private final PackageResolution pkgResolution;
    private final PackageContext rootPackageContext;
    private final List<Diagnostic> diagnosticList;
    private DiagnosticResult diagnosticResult;
    private final List<PlatformLibrary> providedPlatformLibs;

    private ClassLoader classLoaderWithAllJars;
    public Set<Path> optimizedJarLibraryPaths = new HashSet<>();

    JarResolver(JBallerinaBackend jBalBackend, PackageResolution pkgResolution) {
        this.jBalBackend = jBalBackend;
        this.pkgResolution = pkgResolution;
        this.rootPackageContext = pkgResolution.packageContext();
        this.diagnosticList = new ArrayList<>();
        this.providedPlatformLibs = new ArrayList<>();
    }

    DiagnosticResult diagnosticResult() {
        if (this.diagnosticResult == null) {
            this.diagnosticResult = new DefaultDiagnosticResult(this.diagnosticList);
        }
        return diagnosticResult;
    }

    /**
     * Returns a list of platform libraries with provided scope used in dependencies.
     *
     * @return list of platform libraries with provided scope
     */
    public List<PlatformLibrary> providedPlatformLibs() {
        return providedPlatformLibs;
    }

    public Collection<JarLibrary> getJarFilePathsRequiredForExecution() {
        return getJarFilePathsRequiredForExecution(false);
    }

    // TODO These method names are too long. Refactor them soon
    public Collection<JarLibrary> getJarFilePathsRequiredForExecution(boolean optimizeFinalExecutable) {
        // 1) Add this root package related jar files
        Set<JarLibrary> jarFiles = new HashSet<>();
        addCodeGeneratedLibraryPaths(rootPackageContext, PlatformLibraryScope.DEFAULT, jarFiles);
        addPlatformLibraryPaths(rootPackageContext, PlatformLibraryScope.DEFAULT, jarFiles, optimizeFinalExecutable);
        addPlatformLibraryPaths(rootPackageContext, PlatformLibraryScope.PROVIDED, jarFiles, optimizeFinalExecutable);

        // 2) Get all the dependencies of the root package including transitives.
        // Filter out PackageDependencyScope.TEST_ONLY scope dependencies and lang libs
        pkgResolution.allDependencies()
                .stream()
                .filter(pkgDep -> pkgDep.scope() != PackageDependencyScope.TEST_ONLY)
                .filter(pkgDep -> !pkgDep.packageInstance().descriptor().isLangLibPackage())
                .filter(pkgDep -> !optimizeFinalExecutable || !isUnusedPkgDependency(pkgDep))
                .map(pkgDep -> pkgDep.packageInstance().packageContext())
                .forEach(pkgContext -> {
                    // Add generated thin jar of every module in the package represented by the packageContext
                    addCodeGeneratedLibraryPaths(pkgContext, PlatformLibraryScope.DEFAULT, jarFiles);
                    // All platform-specific libraries(specified in Ballerina.toml) having the default scope
                    addPlatformLibraryPaths(pkgContext, PlatformLibraryScope.DEFAULT, jarFiles,
                            optimizeFinalExecutable);
                    addPlatformLibraryPaths(pkgContext, PlatformLibraryScope.PROVIDED, jarFiles, true,
                            optimizeFinalExecutable);
                });

        // 3) Add the runtime library path
        jarFiles.add(new JarLibrary(jBalBackend.runtimeLibrary().path(),
                PlatformLibraryScope.DEFAULT,
                getPackageName(rootPackageContext)));

        // TODO Filter out duplicate jar entries
        return jarFiles;
    }

    private boolean isUnusedPkgDependency(ResolvedPackageDependency pkgDependency) {
        if (!jBalBackend.unusedProjectLevelPackageIds.contains(pkgDependency.packageId())) {
            return false;
        }
        for (ModuleId moduleId : pkgDependency.packageInstance().packageContext().moduleIds()) {
            if (!jBalBackend.unusedModuleIds.contains(moduleId)) {
                return false;
            }
        }
        return true;
    }

    private void addCodeGeneratedLibraryPaths(PackageContext packageContext, PlatformLibraryScope scope,
                                              Set<JarLibrary> libraryPaths) {
        for (ModuleId moduleId : packageContext.moduleIds()) {
            if (packageContext.project().buildOptions().optimizeCodegen() &&
                    jBalBackend.unusedModuleIds.contains(moduleId)) {
                continue;
            }
            ModuleContext moduleContext = packageContext.moduleContext(moduleId);
            PackageID pkgID = moduleContext.descriptor().moduleCompilationId();

            if (packageContext.project().buildOptions().optimizeCodegen() &&
                    !this.rootPackageContext.project().buildOptions().skipTests() &&
                    this.jBalBackend.getOptimizedPackageIDs().contains(pkgID)) {
                addOptimizedLibraryPaths(packageContext, scope, libraryPaths, moduleContext, pkgID);
            }

            PlatformLibrary generatedJarLibrary = jBalBackend.codeGeneratedLibrary(
                    packageContext.packageId(), moduleContext.moduleName());
            libraryPaths.add(new JarLibrary(generatedJarLibrary.path(), scope, getPackageName(packageContext)));
        }
    }

    private void addOptimizedLibraryPaths(PackageContext packageContext, PlatformLibraryScope scope,
                                          Set<JarLibrary> libraryPaths, ModuleContext moduleContext,
                                          PackageID pkgID) {
        PlatformLibrary generatedOptimizedLibrary = jBalBackend.codeGeneratedOptimizedLibrary(
                packageContext.packageId(), moduleContext.moduleName());
        Path optimizedJarLibraryPath = Paths.get(generatedOptimizedLibrary.path().toAbsolutePath().toString());

        if (JvmCodeGenUtil.duplicatePkgsMap.containsKey(pkgID.orgName + pkgID.getNameComps().toString())) {
            // Package is an optimized duplicated pkg.
            // This means the package is a common dependency of both testable and build projects.
            optimizedJarLibraryPath =
                    Path.of(optimizedJarLibraryPath.toString().replace(ProjectConstants.BLANG_COMPILED_JAR_EXT,
                            ProjectConstants.BYTECODE_OPTIMIZED_JAR_SUFFIX));
        }

        libraryPaths.add(new JarLibrary(optimizedJarLibraryPath, scope,
                getPackageName(packageContext) + ProjectConstants.BYTECODE_OPTIMIZED_JAR_SUFFIX));
        optimizedJarLibraryPaths.add(optimizedJarLibraryPath);
    }

    private void addPlatformLibraryPaths(PackageContext packageContext,
                                         PlatformLibraryScope scope,
                                         Set<JarLibrary> libraryPaths, boolean addOnlyUsedLibraries) {
        addPlatformLibraryPaths(packageContext, scope, libraryPaths, false, addOnlyUsedLibraries);
    }

    private void addPlatformLibraryPaths(PackageContext packageContext,
                                         PlatformLibraryScope scope,
                                         Set<JarLibrary> libraryPaths,
                                         boolean addProvidedJars, boolean addOnlyUsedLibraries) {
        // Add all the jar library dependencies of current package (packageId)
        PackageId packageId = packageContext.packageId();
        Collection<PlatformLibrary> otherJarDependencies = jBalBackend.platformLibraryDependencies(packageId, scope);
        Set<String> usedNativeClassPaths = getUsedNativeClassPaths(addOnlyUsedLibraries, packageId);

        if (addProvidedJars) {
            providedPlatformLibs.addAll(otherJarDependencies);
        }

        for (PlatformLibrary otherJarDependency : otherJarDependencies) {
            JarLibrary newEntry = (JarLibrary) otherJarDependency;

            // If there are more than one platform dependency, there could be secondary dependencies
            if (addOnlyUsedLibraries && otherJarDependencies.size() == 1 &&
                    !isUsedDependency(newEntry, usedNativeClassPaths)) {
                continue;
            }
            if (hasEmptyIdOrVersion(newEntry)) {
                libraryPaths.add(new JarLibrary(otherJarDependency.path(), scope, getPackageName(packageContext)));
                continue;
            }
            if (libraryPaths.contains(newEntry)) {
                JarLibrary existingEntry = libraryPaths.stream().filter(jarLibrary1 ->
                        jarLibrary1.equals(newEntry)).findAny().orElseThrow();
                if (hasEmptyIdOrVersion(existingEntry)) {
                    continue;
                }
                ComparableVersion existingVersion = new ComparableVersion(existingEntry.version().orElseThrow());
                ComparableVersion newVersion = new ComparableVersion(newEntry.version().get());

                if (existingVersion.compareTo(newVersion) >= 0) {
                    if (existingVersion.compareTo(newVersion) != 0) {
                        reportDiagnostic(newEntry, existingEntry);
                    }
                    continue;
                }
                reportDiagnostic(existingEntry, newEntry);
                libraryPaths.remove(existingEntry);
            }
            libraryPaths.add(new JarLibrary(
                    newEntry.path(),
                    scope,
                    newEntry.artifactId().orElseThrow(),
                    newEntry.groupId().orElseThrow(),
                    newEntry.version().orElseThrow(),
                    newEntry.packageName().orElseThrow()));
        }
    }

    private Set<String> getUsedNativeClassPaths(boolean addOnlyUsedLibraries, PackageId packageID) {
        if (addOnlyUsedLibraries) {
            return jBalBackend.pkgWiseUsedNativeClassPaths.getOrDefault(packageID, Collections.emptySet());
        }
        return Collections.emptySet();
    }

    private static boolean hasEmptyIdOrVersion(JarLibrary entry) {
        return entry.groupId().isEmpty() || entry.artifactId().isEmpty() || entry.version().isEmpty();
    }

    private boolean isUsedDependency(JarLibrary otherJarDependency, Set<String> usedNativeClassPaths) {
        String pkgName = otherJarDependency.packageName().orElseThrow();
        if (CodegenOptimizationUtils.isWhiteListedModule(pkgName)) {
            return true;
        }
        if (usedNativeClassPaths.isEmpty()) {
            return false;
        }
        try (JarFile jarFile = new JarFile(otherJarDependency.path().toFile())) {
            for (String classPath : usedNativeClassPaths) {
                ZipEntry usedClassEntry = jarFile.getJarEntry(classPath);
                if (usedClassEntry != null) {
                    return true;
                }
            }
            return false;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void reportDiagnostic(JarLibrary existingEntry, JarLibrary newEntry) {
        // Report diagnostic only for non ballerina dependencies
        if (!existingEntry.packageName().orElseThrow().startsWith(ProjectConstants.BALLERINA_ORG)
                || !newEntry.packageName().orElseThrow().startsWith(ProjectConstants.BALLERINA_ORG)) {
            var diagnosticInfo = new DiagnosticInfo(
                    ProjectDiagnosticErrorCode.CONFLICTING_PLATFORM_JAR_FILES.diagnosticId(),
                    "detected conflicting jar files. '" + newEntry.path().getFileName() + "' dependency of '" +
                            newEntry.packageName().get() + "' conflicts with '" + existingEntry.path().getFileName() +
                            "' dependency of '" + existingEntry.packageName().get() + "'. Picking '" +
                            newEntry.path().getFileName() + "' over '" + existingEntry.path().getFileName() + "'.",
                    DiagnosticSeverity.WARNING);
            diagnosticList.add(new PackageDiagnostic(diagnosticInfo,
                    this.jBalBackend.packageContext().descriptor().name().toString()));
        }
    }

    public Collection<JarLibrary> getJarFilePathsRequiredForTestExecution(ModuleName moduleName) {
        // 1) Get all the jars excepts for test scope package and platform-specific dependencies
        Set<JarLibrary> allJarFileForTestExec = new HashSet<>(getJarFilePathsRequiredForExecution());

        // 2) Replace given modules thin jar with it's test-thin jar
        if (!rootPackageContext.packageManifest().org().anonymous()) {
            PackageId rootPackageId = rootPackageContext.packageId();

            // Add the test-thin jar of the specified module
            PlatformLibrary generatedTestJar = jBalBackend.codeGeneratedTestLibrary(rootPackageId, moduleName);
            allJarFileForTestExec.add(new JarLibrary(generatedTestJar.path(),
                    PlatformLibraryScope.DEFAULT,
                    getPackageName(rootPackageContext)));
        }

        // 3) Add platform-specific libraries with test scope defined in the root package's Ballerina.toml
        addPlatformLibraryPaths(rootPackageContext, PlatformLibraryScope.TEST_ONLY, allJarFileForTestExec, false);

        // Get all the dependencies of the root package including transitives.
        // 4) Include only the dependencies with PackageDependencyScope.TEST_ONLY scope
        // 5) All generated jars and platform-specific libraries of test scope dependencies of this rootPackage.
        pkgResolution.allDependencies()
                .stream()
                .filter(pkgDep -> pkgDep.scope() == PackageDependencyScope.TEST_ONLY)
                .filter(pkgDep -> !pkgDep.packageInstance().descriptor().isLangLibPackage())    //filter out lang libs
                .map(pkgDep -> pkgDep.packageInstance().packageContext())
                .forEach(pkgContext -> {
                    // Add generated thin jar of every module in the package represented by the packageContext
                    addCodeGeneratedLibraryPaths(pkgContext, PlatformLibraryScope.DEFAULT, allJarFileForTestExec);
                    // All platform-specific libraries(specified in Ballerina.toml) having the default scope
                    addPlatformLibraryPaths(pkgContext, PlatformLibraryScope.DEFAULT, allJarFileForTestExec, false);
                });

        // 6 Add other dependencies required to run Ballerina test cases
        allJarFileForTestExec.addAll(ProjectUtils.testDependencies());

        // TODO Filter out duplicate jar entries
        return allJarFileForTestExec;
    }

    public ClassLoader getClassLoaderWithRequiredJarFilesForExecution() {
        if (classLoaderWithAllJars != null) {
            return classLoaderWithAllJars;
        }

        classLoaderWithAllJars = createClassLoader(getJarFilePathsRequiredForExecution());
        return classLoaderWithAllJars;
    }

    public ClassLoader getClassLoaderWithRequiredJarFilesForTestExecution(ModuleName moduleName) {
        return createClassLoader(getJarFilePathsRequiredForTestExecution(moduleName));
    }

    private URLClassLoader createClassLoader(Collection<JarLibrary> jarFiles) {
        if (jBalBackend.diagnosticResult().hasErrors()) {
            throw new IllegalStateException("Cannot create a ClassLoader: this compilation has errors.");
        }

        List<URL> urlList = new ArrayList<>(jarFiles.size());
        for (JarLibrary jarFile : jarFiles) {
            try {
                urlList.add(jarFile.path().toUri().toURL());
            } catch (MalformedURLException e) {
                // This path cannot get executed
                throw new RuntimeException("Failed to create classloader with all jar files", e);
            }
        }

        // TODO use the ClassLoader.getPlatformClassLoader() here
        return AccessController.doPrivileged(
                (PrivilegedAction<URLClassLoader>) () -> new URLClassLoader(urlList.toArray(new URL[0]),
                        ClassLoader.getSystemClassLoader())
        );
    }

    private String getPackageName(PackageContext packageContext) {
        return packageContext.packageOrg().value() + "/" + packageContext.packageName().value();
    }

    /**
     * Provides Qualified Class Name.
     *
     * @param orgName     Org name
     * @param packageName Package name
     * @param version     Package version
     * @param className   Class name
     * @return Qualified class name
     */
    public static String getQualifiedClassName(String orgName, String packageName, String version, String className) {
        if (!DOT.equals(packageName)) {
            className = encodeNonFunctionIdentifier(packageName) + "." +
                    CompilerUtils.getMajorVersion(version) + "." + className;
        }
        if (!ANON_ORG.equals(orgName)) {
            className = encodeNonFunctionIdentifier(orgName) + "." + className;
        }
        return className;
    }
}
