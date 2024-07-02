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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This represents an aggregated package repository, which is a container for multiple {@link PackageRepository}s
 * in the same level.
 * 
 * @since 0.94
 *
 */
public class AggregatedPackageRepository implements PackageRepository {

    private final List<PackageRepository> repos = new ArrayList<>();
    
    public void addRepository(PackageRepository repo) {
        this.repos.add(repo);
    }
    
    public List<PackageRepository> getRepositories() {
        return repos;
    }
    
    @Override
    public PackageEntity loadPackage(PackageID pkgId) {
        PackageEntity result = null;
        for (PackageRepository repo : this.repos) {
            result = repo.loadPackage(pkgId);
            if (result != null) {
                break;
            }
        }
        return result;
    }

    @Override
    public PackageEntity loadPackage(PackageID pkgId, String entryName) {
        PackageEntity result = null;
        for (PackageRepository repo : this.repos) {
            result = repo.loadPackage(pkgId, entryName);
            if (result != null) {
                break;
            }
        }
        return result;
    }

    @Override
    public Set<PackageID> listPackages(int maxDepth) {
        return this.repos.stream().flatMap(e -> e.listPackages(maxDepth).stream()).collect(
                Collectors.toCollection(LinkedHashSet::new));
    }

}
