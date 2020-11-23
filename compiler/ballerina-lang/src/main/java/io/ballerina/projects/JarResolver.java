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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static io.ballerina.projects.util.ProjectConstants.ANON_ORG;
import static io.ballerina.projects.util.ProjectConstants.DOT;
import static io.ballerina.runtime.internal.IdentifierUtils.encodeNonFunctionIdentifier;

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
    private final PackageContext packageContext;

    private List<Path> allJarFilePaths;
    private ClassLoader classLoaderWithAllJars;

    JarResolver(JBallerinaBackend jBalBackend, PackageResolution pkgResolution) {
        this.jBalBackend = jBalBackend;
        this.pkgResolution = pkgResolution;
        this.packageContext = pkgResolution.packageContext();
    }

    // TODO These method names are too long. Refactor them soon
    public Collection<Path> getJarFilePathsRequiredForExecution() {
        if (allJarFilePaths != null) {
            return allJarFilePaths;
        }

        allJarFilePaths = new ArrayList<>();
        List<Package> sortedPackageIds = pkgResolution.dependencyGraph().toTopologicallySortedList();
        for (Package pkg : sortedPackageIds) {
            PackageContext packageContext = pkg.packageContext();
            for (ModuleId moduleId : packageContext.moduleIds()) {
                ModuleContext moduleContext = packageContext.moduleContext(moduleId);
                PlatformLibrary generatedJarLibrary = jBalBackend.codeGeneratedLibrary(
                        pkg.packageId(), moduleContext.moduleName());
                allJarFilePaths.add(generatedJarLibrary.path());
            }

            // Add all the jar library dependencies of current package (packageId)
            // TODO Filter our test scope dependencies
            Collection<PlatformLibrary> otherJarDependencies =
                    jBalBackend.platformLibraryDependencies(pkg.packageId());
            for (PlatformLibrary otherJarDependency : otherJarDependencies) {
                allJarFilePaths.add(otherJarDependency.path());
            }
        }

        // Add the runtime library path
        allJarFilePaths.add(jBalBackend.runtimeLibrary().path());
        return allJarFilePaths;
    }

    public Collection<Path> getJarFilePathsRequiredForTestExecution(ModuleName moduleName) {
        Collection<Path> allJarFiles = getJarFilePathsRequiredForExecution();
        Set<Path> allJarFileForTestExec = new HashSet<>(allJarFiles);
        if (!packageContext.manifest().org().anonymous()) {
            PlatformLibrary generatedTestJar = jBalBackend.codeGeneratedTestLibrary(
                    packageContext.packageId(), moduleName);
            allJarFileForTestExec.add(generatedTestJar.path());

            // Remove the generated jar without test code.
            PlatformLibrary generatedJar = jBalBackend.codeGeneratedLibrary(
                    packageContext.packageId(), moduleName);
            allJarFileForTestExec.remove(generatedJar.path());
        }
        allJarFileForTestExec.addAll(ProjectUtils.testDependencies());

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
