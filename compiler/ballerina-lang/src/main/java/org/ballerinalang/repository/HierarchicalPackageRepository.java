/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.repository;

import org.ballerinalang.model.elements.PackageID;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Hierarchical package repository class, which contains the most common
 * used features of a package repository as a hierarchical lookup structure,
 * starting initially from a system package repository.
 *
 * @since 0.94
 */
public abstract class HierarchicalPackageRepository implements PackageRepository {

    private static final String BALLERINA_SYSTEM_PKG_PREFIX = "ballerina";

    private final PackageRepository systemRepo;

    private final PackageRepository parentRepo;
    
    private Set<PackageID> cachedPackageIds;

    public HierarchicalPackageRepository(PackageRepository systemRepo, PackageRepository parentRepo) {
        this.systemRepo = systemRepo;
        this.parentRepo = parentRepo;
    }

    public abstract PackageEntity lookupPackage(PackageID pkgId);

    public abstract PackageEntity lookupPackage(PackageID pkgId, String entryName);
    
    public abstract Set<PackageID> lookupPackageIDs(int maxDepth);

    @Nullable
    @Override
    public PackageEntity loadPackage(PackageID pkgId) {
        PackageEntity result = null;
        if (this.isSystemPackage(pkgId)) {
            result = this.systemRepo.loadPackage(pkgId);
        }

        if (result != null) {
            return result;
        }

        result = this.lookupPackage(pkgId);
        if (result == null && this.parentRepo != null) {
            result = this.parentRepo.loadPackage(pkgId);
        }
        return result;
    }

    @Nullable
    @Override
    public PackageEntity loadPackage(PackageID pkgId, String entryName) {
        PackageEntity result = null;
        if (this.isSystemPackage(pkgId)) {
            result = this.systemRepo.loadPackage(pkgId, entryName);
        }

        if (result != null) {
            return result;
        }

        result = this.lookupPackage(pkgId, entryName);
        if (result == null && this.parentRepo != null) {
            result = this.parentRepo.loadPackage(pkgId, entryName);
        }
        return result;
    }

    private boolean isSystemPackage(PackageID pkgID) {
        return pkgID.getNameComp(0).getValue().equals(BALLERINA_SYSTEM_PKG_PREFIX);
    }
    
    @Override
    public Set<PackageID> listPackages(int maxDepth) {
        if (this.cachedPackageIds == null) {
            this.cachedPackageIds = new LinkedHashSet<>(this.systemRepo.listPackages(maxDepth));
            if (this.parentRepo != null) {
                this.cachedPackageIds.addAll(this.parentRepo.listPackages(maxDepth));
            }
            this.cachedPackageIds.addAll(this.lookupPackageIDs(maxDepth));
        }
        return this.cachedPackageIds;
    }

}
