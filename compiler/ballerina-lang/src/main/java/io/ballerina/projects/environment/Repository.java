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

import io.ballerina.projects.Module;
import io.ballerina.projects.ModuleName;
import io.ballerina.projects.Package;
import io.ballerina.projects.SemanticVersion;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * This interface represent a repository of packages.
 *
 * @since 2.0.0
 */
public interface Repository {

    public Optional<Package> getPackage(PackageLoadRequest packageLoadRequest);

    public List<SemanticVersion> getPackageVersions(PackageLoadRequest packageLoadRequest);

    public byte[] getCachedBir(ModuleName moduleName);

    public void cacheBir(ModuleName moduleName, byte[] bir);

    public Path getCachedJar(Module aPackage);

}
