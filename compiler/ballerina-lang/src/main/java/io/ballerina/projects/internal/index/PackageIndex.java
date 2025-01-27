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

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import io.ballerina.projects.PackageName;
import io.ballerina.projects.PackageOrg;
import io.ballerina.projects.PackageVersion;
import io.ballerina.projects.util.ProjectUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents the package index as an in-memory table of organization names and package names.
 *
 * @since 2201.12.0
 */
public class PackageIndex {
    private final Table<PackageOrg, PackageName, List<IndexPackage>> packageTable = HashBasedTable.create();
    private final PackageIndexUpdater updater;

    PackageIndex(PackageIndexUpdater updater) {
        this.updater = updater;
    }

    void addPackage(List<IndexPackage> packages) {
        for (IndexPackage pkg : packages) {
            if (!this.packageTable.contains(pkg.org(), pkg.name())) {
                this.packageTable.put(pkg.org(), pkg.name(), new ArrayList<>());
            }
            Objects.requireNonNull(this.packageTable.get(pkg.org(), pkg.name())).add(pkg);
        }
    }

    /**
     * Set the offline flag.
     *
     * @param offline offline flag
     */
    public void setOffline(boolean offline) {
        this.updater.setOffline(offline);
    }

    private void loadPackage(PackageOrg packageOrg, PackageName packageName) {
        if (!this.packageTable.contains(packageOrg, packageName)) {
            updater.loadPackage(packageOrg, packageName);
        }
    }

    private void loadOrg(PackageOrg packageOrg) {
        updater.loadOrg(packageOrg);
    }

    /**
     * Get the specified version of a package.
     *
     * @param packageOrg organization name
     * @param packageName package name
     * @param version package version
     * @return the matching package recorded in the index
     */
    public IndexPackage getVersion(PackageOrg packageOrg, PackageName packageName, PackageVersion version) {
        loadPackage(packageOrg, packageName);
        List<IndexPackage> packageMap = this.packageTable.get(packageOrg, packageName);
        if (packageMap == null) {
            return null;
        }
        return packageMap.stream().filter(pkg -> pkg.version().equals(version)).findAny().orElse(null);
    }

    /**
     * Get the all available versions of a package.
     *
     * @param orgName organization name
     * @param packageName package name
     * @return package versions of the matching package recorded in the index
     */
    public List<IndexPackage> getPackage(PackageOrg orgName, PackageName packageName) {
        loadPackage(orgName, packageName);
        return this.packageTable.get(orgName, packageName);
    }

    /**
     * Get the all modules matching a module name in an organization
     * @param orgName organization name
     * @param moduleName module name
     * @return modules of the matching package recorded in the index
     */
    public List<IndexPackage> getPackageContainingModule(PackageOrg orgName, String moduleName) {
        loadOrg(orgName);
        return packageTable.row(orgName).values().stream().flatMap(List::stream)
                .filter(pkg -> pkg.modules().stream().anyMatch(module -> module.name().equals(moduleName)) &&
                        ProjectUtils.isDistributionSupported(pkg.ballerinaVersion()))
                .toList();
    }
}
