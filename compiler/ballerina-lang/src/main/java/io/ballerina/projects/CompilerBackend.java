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

import java.util.Collection;

/**
 * An abstract class that represents a Ballerina compiler backend.
 *
 * @see JBallerinaBackend
 * @since 2.0.0
 */
public abstract class CompilerBackend {

    /**
     * Returns all the platform-specific library dependencies of a given Package.
     * <p>
     * Typically these library dependencies are specified in Ballerina.toml file.
     *
     * @param packageId the {@code PackageId} of the package.
     * @return a collection of required platform-specific library dependencies of a given Package
     */
    public abstract Collection<PlatformLibrary> platformLibraryDependencies(PackageId packageId);

    /**
     * Returns a collection of platform-specific library dependencies of a given Package
     * specified with the given {@code PlatformLibraryScope}.
     * <p>
     * Typically these library dependencies are specified in Ballerina.toml file.
     *
     * @param packageId the {@code PackageId} of the package.
     * @param scope     the scope of the dependency
     * @return a collection of required platform-specific library dependencies of a given Package
     */
    public abstract Collection<PlatformLibrary> platformLibraryDependencies(PackageId packageId,
                                                                            PlatformLibraryScope scope);

    /**
     * Returns the generated platform library of the specified module.
     *
     * @param packageId  the {@code PackageId} of the package
     * @param moduleName the name of the module in the package
     * @return the generated platform library of the specified module
     */
    public abstract PlatformLibrary codeGeneratedLibrary(PackageId packageId, ModuleName moduleName);

    /**
     * Returns the generated optimized platform library of the specified module.
     *
     * @param packageId  the {@code PackageId} of the package
     * @param moduleName the name of the module in the package
     * @return the generated optimized platform library of the specified module
     */
    public abstract PlatformLibrary codeGeneratedOptimizedLibrary(PackageId packageId, ModuleName moduleName);

    /**
     * Returns the generated platform library of the specified module required to run tests.
     *
     * @param packageId  the {@code PackageId} of the package
     * @param moduleName the name of the module in the package
     * @return the generated platform library of the specified module
     */
    public abstract PlatformLibrary codeGeneratedTestLibrary(PackageId packageId, ModuleName moduleName);

    public abstract PlatformLibrary codeGeneratedResourcesLibrary(PackageId packageId);

    /**
     * Returns the platform-specific runtime library.
     *
     * @return the platform-specific runtime library
     */
    public abstract PlatformLibrary runtimeLibrary();

    /**
     * Returns the supported target platform of this compiler backend.
     *
     * @return the supported target platform of this compiler backend
     */
    public abstract TargetPlatform targetPlatform();

    // TODO this method should be moved to some other class owned by the CompilerBackend
    public abstract void performCodeGen(ModuleContext moduleContext, CompilationCache compilationCache);

    public abstract String libraryFileExtension();

    /**
     * Represent the unique name of a supported compiler backed target.
     *
     * @see JvmTarget
     * @since 2.0.0
     */
    public interface TargetPlatform {
        String code();
    }
}
