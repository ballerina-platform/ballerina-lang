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

import java.util.Set;

/**
 * This represents a composite {@link HierarchicalPackageRepository} which will encapsulate a given
 * {@link PackageRepository}.
 * 
 * @since 0.94
 */
public class CompositePackageRepository extends HierarchicalPackageRepository {
    
    private final PackageRepository myRepo;

    public CompositePackageRepository(PackageRepository systemRepo,
                                      PackageRepository parentRepo, PackageRepository myRepo) {
        super(systemRepo, parentRepo);
        this.myRepo = myRepo;
    }

    @Override
    public PackageEntity lookupPackage(PackageID pkgId) {
        return this.myRepo.loadPackage(pkgId);
    }

    @Override
    public PackageEntity lookupPackage(PackageID pkgId, String entryName) {
        return this.myRepo.loadPackage(pkgId, entryName);
    }

    @Override
    public Set<PackageID> lookupPackageIDs(int maxDepth) {
        return this.myRepo.listPackages(maxDepth);
    }

}
