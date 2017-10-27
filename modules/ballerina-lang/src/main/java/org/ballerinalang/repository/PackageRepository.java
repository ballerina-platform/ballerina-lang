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
 * This represents a package repository. For example, implementation can be used to
 * fetch source code / compiled objects from the file system, network etc...
 * 
 * @since 0.94
 */
public interface PackageRepository {
    
    /**
     * Looks up and returns a {@link PackageEntity} given the package identifier.
     * 
     * @param pkgId the package identifier
     * @return the package entity is returned if it's available, or else, null will be returned.
     */
    PackageEntity loadPackage(PackageID pkgId);
    
    /**
     * Looks up and return a {@link PackageEntity} given the package identifier,
     * and its specific entry.
     *
     * @param pkgId the package identifier
     * @param entryName the entry name
     * @return the package entity
     */
    PackageEntity loadPackage(PackageID pkgId, String entryName);
    
    /**
     * Lists all the packages that are available in this repository.
     * @param maxDepth the maximum depth of directories to search in
     * @return the list of packages
     */
    Set<PackageID> listPackages(int maxDepth);
    
}
