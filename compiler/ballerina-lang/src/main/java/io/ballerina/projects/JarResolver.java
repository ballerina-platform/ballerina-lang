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

import io.ballerina.projects.util.ProjectUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static io.ballerina.projects.util.ProjectConstants.ANON_ORG;
import static io.ballerina.projects.util.ProjectConstants.DOT;
import static io.ballerina.runtime.api.utils.IdentifierUtils.encodeNonFunctionIdentifier;

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

    private ClassLoader classLoaderWithAllJars;

    JarResolver(JBallerinaBackend jBalBackend, PackageResolution pkgResolution) {
        this.jBalBackend = jBalBackend;
        this.pkgResolution = pkgResolution;
        this.rootPackageContext = pkgResolution.packageContext();
    }

    // TODO These method names are too long. Refactor them soon
    public Collection<Path> getJarFilePathsRequiredForExecution() {
        // 1) Add this root package related jar files
        List<Path> jarFilePaths = new ArrayList<>();
        addCodeGeneratedLibraryPaths(rootPackageContext, jarFilePaths);
        addPlatformLibraryPaths(rootPackageContext, PlatformLibraryScope.DEFAULT, jarFilePaths);

        // 2) Get all the dependencies of the root package including transitives.
        // Filter out PackageDependencyScope.TEST_ONLY scope dependencies
        pkgResolution.allDependencies()
                .stream()
                .filter(pkgDep -> pkgDep.scope() != PackageDependencyScope.TEST_ONLY)
                .map(pkgDep -> pkgDep.packageInstance().packageContext())
                .forEach(pkgContext -> {
                    // Add generated thin jar of every module in the package represented by the packageContext
                    addCodeGeneratedLibraryPaths(pkgContext, jarFilePaths);
                    // All platform-specific libraries(specified in Ballerina.toml) having the default scope
                    addPlatformLibraryPaths(pkgContext, PlatformLibraryScope.DEFAULT, jarFilePaths);
                });

        // 3) Add the runtime library path
        jarFilePaths.add(jBalBackend.runtimeLibrary().path());

        // TODO Filter out duplicate jar entries
        return jarFilePaths;
    }

    private void addCodeGeneratedLibraryPaths(PackageContext packageContext, List<Path> libraryPaths) {
        for (ModuleId moduleId : packageContext.moduleIds()) {
            ModuleContext moduleContext = packageContext.moduleContext(moduleId);
            PlatformLibrary generatedJarLibrary = jBalBackend.codeGeneratedLibrary(
                    packageContext.packageId(), moduleContext.moduleName());
            libraryPaths.add(generatedJarLibrary.path());
        }
    }

    private void addPlatformLibraryPaths(PackageContext packageContext,
                                         PlatformLibraryScope scope,
                                         List<Path> libraryPaths) {
        // Add all the jar library dependencies of current package (packageId)
        Collection<PlatformLibrary> otherJarDependencies = jBalBackend.platformLibraryDependencies(
                packageContext.packageId(), scope);
        for (PlatformLibrary otherJarDependency : otherJarDependencies) {
            libraryPaths.add(otherJarDependency.path());
        }
    }


    public Collection<Path> getJarFilePathsRequiredForTestExecution(ModuleName moduleName) {
        // 1) Get all the jars excepts for test scope package and platform-specific dependencies
        List<Path> allJarFileForTestExec = new ArrayList<>(getJarFilePathsRequiredForExecution());

        // 2) Replace given modules thin jar with it's test-thin jar
        if (!rootPackageContext.manifest().org().anonymous()) {
            PackageId rootPackageId = rootPackageContext.packageId();

            // Add the test-thin jar of the specified module
            PlatformLibrary generatedTestJar = jBalBackend.codeGeneratedTestLibrary(rootPackageId, moduleName);
            allJarFileForTestExec.add(generatedTestJar.path());

            // Remove the generated jar without test code.
            PlatformLibrary generatedJar = jBalBackend.codeGeneratedLibrary(rootPackageId, moduleName);
            allJarFileForTestExec.remove(generatedJar.path());
        }

        // 3) Add platform-specific libraries with test scope defined in the root package's Ballerina.toml
        addPlatformLibraryPaths(rootPackageContext, PlatformLibraryScope.TEST_ONLY, allJarFileForTestExec);

        // Get all the dependencies of the root package including transitives.
        // 4) Include only the dependencies with PackageDependencyScope.TEST_ONLY scope
        // 5) All generated jars and platform-specific libraries of test scope dependencies of this rootPackage.
        pkgResolution.allDependencies()
                .stream()
                .filter(pkgDep -> pkgDep.scope() == PackageDependencyScope.TEST_ONLY)
                .map(pkgDep -> pkgDep.packageInstance().packageContext())
                .forEach(pkgContext -> {
                    // Add generated thin jar of every module in the package represented by the packageContext
                    addCodeGeneratedLibraryPaths(pkgContext, allJarFileForTestExec);
                    // All platform-specific libraries(specified in Ballerina.toml) having the default scope
                    addPlatformLibraryPaths(pkgContext, PlatformLibraryScope.DEFAULT, allJarFileForTestExec);
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

    private URLClassLoader createClassLoader(Collection<Path> jarFilePaths) {
        if (jBalBackend.diagnosticResult().hasErrors()) {
            throw new IllegalStateException("Cannot create a ClassLoader: this compilation has errors.");
        }

        List<URL> urlList = new ArrayList<>(jarFilePaths.size());
        for (Path jarFilePath : jarFilePaths) {
            try {
                urlList.add(jarFilePath.toUri().toURL());
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
            className = encodeNonFunctionIdentifier(packageName) + "." + version.replace('.', '_') + "." + className;
        }
        if (!ANON_ORG.equals(orgName)) {
            className = encodeNonFunctionIdentifier(orgName) + "." + className;
        }
        return className;
    }
}
