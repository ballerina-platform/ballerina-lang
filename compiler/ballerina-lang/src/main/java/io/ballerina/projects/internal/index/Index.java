/*
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.projects.internal.index;

import io.ballerina.projects.PackageVersion;
import io.ballerina.projects.SemanticVersion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

// TODO: index should merge the central index and the distribution index.
// TODO: define a set of proper APIs in the index
public class Index {
    private final Map<String, Map<String, List<IndexPackage>>> packageMap;
    public static final Index EMPTY_INDEX = new Index();

    public Index() {
        this.packageMap = new HashMap<>();
    }

    public Optional<IndexPackage> getVersion(String orgName, String packageName, PackageVersion version) {
        return getVersion(orgName, packageName, version, null);
    }

    public Optional<IndexPackage> getVersion(String orgName, String packageName, PackageVersion version,
                                             String repository) {
        if (this.packageMap.get(orgName) == null) {
            return Optional.empty();
        }
        List<IndexPackage> packageMap = this.packageMap.get(orgName).get(packageName);
        if (packageMap == null) {
            return Optional.empty();
        }
        Stream<IndexPackage> versions = packageMap.stream().filter(pkg -> pkg.version().equals(version));
        if (repository != null) {
            return versions.filter(pkg -> repository.equals(pkg.repository())).findFirst();
        }
        return versions.findFirst();
    }

    public void putVersion(IndexPackage pkg) {
        List<IndexPackage> packageMap = this.packageMap.computeIfAbsent(
                pkg.org().toString(), k -> new HashMap<>()).computeIfAbsent(pkg.name().toString(), k -> new ArrayList<>());
        packageMap.add(pkg);
    }

    public List<IndexPackage> getPackage(String orgName, String packageName) {
        if (this.packageMap.get(orgName) == null) {
            return new ArrayList<>();
        }
        return packageMap.get(orgName).get(packageName);
    }

    public List<IndexPackage> getPackage(String orgName,
                                         String packageName,
                                         String supportedPlatform,
                                         SemanticVersion ballerinaVersion) {
        if (this.packageMap.get(orgName) == null) {
            return new ArrayList<>();
        }
        return packageMap.get(orgName).get(packageName).stream().filter(pkg ->
                pkg.supportedPlatform().equals(supportedPlatform) && pkg.ballerinaVersion().equals(ballerinaVersion)
        ).toList();
    }

    public List<IndexPackage> getPackageMatchingModule(String orgName, String moduleName, String ballerinaVersionStr) {
        SemanticVersion ballerinaVersion = SemanticVersion.from(ballerinaVersionStr);
        if (this.packageMap.get(orgName) == null) {
            return new ArrayList<>();
        }
        return packageMap.get(orgName).values().stream().flatMap(List::stream)
                .filter(pkg -> pkg.modules().stream().anyMatch(module -> module.name().equals(moduleName)) &&
                        ballerinaVersion.greaterThanOrEqualTo(pkg.ballerinaVersion()))
                .toList();
    }

    public void putPackages(List<IndexPackage> pkgs) {
        for (IndexPackage descriptor : pkgs) {
            putVersion(descriptor);
        }
    }
}
