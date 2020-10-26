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

package io.ballerina.projects.environment;

import io.ballerina.projects.Project;
import org.ballerinalang.compiler.JarResolver;
import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;

import java.nio.file.Path;
import java.util.HashSet;


/**
 * Use by the code gen to resolve dependent netive jar libraries.
 *
 * @since 2.0.0
 */
public class ProjectJarResolver implements JarResolver {

    Project project;

    public ProjectJarResolver(Project project, GlobalPackageCache globalPackageCache) {
        this.project = project;
    }

    @Override
    public Path moduleJar(PackageID packageID) {
        return null;
    }

    @Override
    public Path moduleTestJar(BLangPackage bLangPackage) {
        return null;
    }

    @Override
    public HashSet<Path> nativeDependencies(PackageID packageID) {
        return new HashSet<>();
    }

    @Override
    public HashSet<Path> nativeDependenciesForTests(PackageID packageID) {
        return new HashSet<>();
    }

    @Override
    public HashSet<Path> allDependencies(BLangPackage bLangPackage) {
        return null;
    }

    @Override
    public HashSet<Path> allTestDependencies(BLangPackage bLangPackage) {
        return null;
    }

    @Override
    public Path getRuntimeJar() {
        return null;
    }
}
