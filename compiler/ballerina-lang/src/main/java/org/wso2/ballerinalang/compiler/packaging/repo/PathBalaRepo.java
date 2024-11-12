/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.ballerinalang.compiler.packaging.repo;

import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.toml.model.Dependency;
import org.ballerinalang.toml.model.Manifest;
import org.wso2.ballerinalang.compiler.packaging.Patten;
import org.wso2.ballerinalang.compiler.packaging.converters.Converter;
import org.wso2.ballerinalang.compiler.packaging.converters.ZipConverter;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;
import org.wso2.ballerinalang.util.RepoUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.wso2.ballerinalang.compiler.packaging.Patten.path;

/**
 * Resolve a bala using the path given in the Ballerina.toml.
 */
public class PathBalaRepo implements Repo<Path> {
    private static final Pattern SEM_VER_PATTERN = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)");
    
    private final Manifest manifest;
    private final Map<PackageID, Manifest> dependencyManifests;
    private final ZipConverter zipConverter;
    
    public PathBalaRepo(Manifest manifest, Map<PackageID, Manifest> dependencyManifests) {
        this.manifest = manifest;
        this.dependencyManifests = dependencyManifests;
        // path value for zip converter does'nt matter
        this.zipConverter = new ZipConverter(Path.of(""));
    }
    
    @Override
    public Patten calculate(PackageID moduleID) {
        // if manifest does not exists
        if (null == this.manifest) {
            return Patten.NULL;
        }
        
        Optional<Dependency> tomlDependency =
                this.manifest.getDependencies().stream()
                        .filter(dep -> dep.getOrgName().equals(moduleID.orgName.value) &&
                                       dep.getModuleName().equals(moduleID.name.value) &&
                                       null != dep.getMetadata().getPath())
                        .findFirst();
        
        // if dependency is not found in toml
        if (!tomlDependency.isPresent()) {
            return Patten.NULL;
        }
        
        Dependency dep = tomlDependency.get();
        Path balaPath = dep.getMetadata().getPath();
        
        // if bala file does not exists
        if (Files.notExists(balaPath)) {
            throw new BLangCompilerException("bala file for dependency [" + dep.getModuleID() + "] does not exists: " +
                                             dep.getMetadata().getPath().toAbsolutePath().normalize());
        }
    
        // if bala file is not a file
        if (!Files.isRegularFile(balaPath)) {
            throw new BLangCompilerException("bala file for dependency [" + dep.getModuleID() + "] is not a file: " +
                                             dep.getMetadata().getPath().toAbsolutePath().normalize());
        }
        
        // update version of the dependency from the current(root) project
        if (moduleID.version.value.isEmpty() && null != dep.getMetadata().getVersion()) {
            Matcher semverMatcher = SEM_VER_PATTERN.matcher(dep.getMetadata().getVersion());
            if (semverMatcher.matches()) {
                moduleID.version = new Name(dep.getMetadata().getVersion());
            }
        }

        Manifest manifestFromBala = RepoUtils.getManifestFromBala(balaPath.toAbsolutePath());
        // if version is not set, then resolve by the bala path's manifest
        if (moduleID.version.value.isEmpty()) {
            moduleID.version = new Name(manifestFromBala.getProject().getVersion());
        }
    
        // update dependency manifests map for imports of this moduleID.
        this.dependencyManifests.put(moduleID, manifestFromBala);
    
        // resolve to bala path
        return new Patten(
                path(balaPath.toAbsolutePath().toString(), ProjectDirConstants.SOURCE_DIR_NAME, dep.getModuleName()),
                Patten.WILDCARD_SOURCE);
    }
    
    @Override
    public Converter<Path> getConverterInstance() {
        return this.zipConverter;
    }
    
    @Override
    public String toString() {
        return "{t:'PathBalaRepo', c:'" + this.zipConverter + "'}";
    }
}
