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
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.wso2.ballerinalang.compiler.packaging.Patten.path;

/**
 * Resolve a balo using the path given in the Ballerina.toml.
 */
public class PathBaloRepo implements Repo<Path> {
    private static final Pattern semVerPattern = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)");
    
    private final Manifest manifest;
    private Map<PackageID, Manifest> dependencyManifests;
    private ZipConverter zipConverter;
    
    public PathBaloRepo(Manifest manifest, Map<PackageID, Manifest> dependencyManifests) {
        this.manifest = manifest;
        this.dependencyManifests = dependencyManifests;
        // path value for zip converter does'nt matter
        this.zipConverter = new ZipConverter(Paths.get(""));
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
        Path baloPath = dep.getMetadata().getPath();
        
        // if balo file does not exists
        if (Files.notExists(baloPath)) {
            throw new BLangCompilerException("balo file for dependency [" + dep.getModuleID() + "] does not exists: " +
                                             dep.getMetadata().getPath().toAbsolutePath().normalize());
        }
    
        // if balo file is not a file
        if (!Files.isRegularFile(baloPath)) {
            throw new BLangCompilerException("balo file for dependency [" + dep.getModuleID() + "] is not a file: " +
                                             dep.getMetadata().getPath().toAbsolutePath().normalize());
        }
        
        // update version of the dependency from the current(root) project
        if (moduleID.version.value.isEmpty() && null != dep.getMetadata().getVersion()) {
            Matcher semverMatcher = semVerPattern.matcher(dep.getMetadata().getVersion());
            if (semverMatcher.matches()) {
                moduleID.version = new Name(dep.getMetadata().getVersion());
            }
        }

        Manifest manifestFromBalo = RepoUtils.getManifestFromBalo(baloPath.toAbsolutePath());
        // if version is not set, then resolve by the balo path's manifest
        if (moduleID.version.value.isEmpty()) {
            moduleID.version = new Name(manifestFromBalo.getProject().getVersion());
        }
    
        // update dependency manifests map for imports of this moduleID.
        this.dependencyManifests.put(moduleID, manifestFromBalo);
    
        // resolve to balo path
        return new Patten(
                path(baloPath.toAbsolutePath().toString(), ProjectDirConstants.SOURCE_DIR_NAME, dep.getModuleName()),
                Patten.WILDCARD_SOURCE);
    }
    
    @Override
    public Converter<Path> getConverterInstance() {
        return this.zipConverter;
    }
    
    @Override
    public String toString() {
        return "{t:'PathBaloRepo', c:'" + this.zipConverter + "'}";
    }
}
