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
package org.ballerinalang.compiler;

import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.nio.file.Path;
import java.util.HashSet;

/**
 * Contains methods to resolve the module jars and native libraries.
 *
 * @since 2.0.0
 */
public interface JarResolver {

    CompilerContext.Key<JarResolver> JAR_RESOLVER_KEY = new CompilerContext.Key<>();

    Path moduleJar(PackageID packageID);

    Path moduleTestJar(BLangPackage bLangPackage);

    HashSet<Path> nativeDependencies(PackageID packageID);

    HashSet<Path> nativeDependenciesForTests(PackageID packageID);

    HashSet<Path> allDependencies(BLangPackage bLangPackage);

    HashSet<Path> allTestDependencies(BLangPackage bLangPackage);

    Path getRuntimeJar();
}
