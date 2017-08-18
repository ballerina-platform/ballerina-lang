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

/**
 * User package source repository class, which contains the most common 
 * used features of a package source repository as a hierarchical lookup structure,
 * starting initially from a system package source repository.
 * 
 * @since 0.94
 */
public abstract class UserPackageSourceRepository implements PackageSourceRepository {

    private static final String BALLERINA_SYSTEM_PKG_PREFIX = "ballerina";

    private PackageSourceRepository systemRepo;
    
    private PackageSourceRepository parentRepo;
    
    public UserPackageSourceRepository(PackageSourceRepository systemRepo, PackageSourceRepository parentRepo) {
        this.systemRepo = systemRepo;
        this.parentRepo = parentRepo;
    }
    
    public abstract PackageSource lookupPackageSource(PackageID pkgID);
    
    @Override
    public PackageSource getPackageSource(PackageID pkgID) {
        PackageSource result;
        if (this.isSystemPackage(pkgID)) {
            result = this.systemRepo.getPackageSource(pkgID);
        } else {
            result = this.lookupPackageSource(pkgID);
            if (result == null && this.parentRepo != null) {
                result = this.parentRepo.getPackageSource(pkgID);
            }
        }
        return result;
    }
    
    private boolean isSystemPackage(PackageID pkgID) {
        return pkgID.getNameCompCount() > 0 && pkgID.getNameComponent(0).equals(BALLERINA_SYSTEM_PKG_PREFIX);
    }
    
}
