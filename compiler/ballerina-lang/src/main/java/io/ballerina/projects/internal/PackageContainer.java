/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.projects.internal;

import io.ballerina.projects.PackageName;
import io.ballerina.projects.PackageOrg;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A container for various package representation that provides efficient access to packages.
 *
 * @param <T> element type
 */
public class PackageContainer<T> {
    private final Map<PackageOrg, Map<PackageName, T>> pkgOrgMap;

    public PackageContainer() {
        this.pkgOrgMap = new HashMap<>();
    }

    public void add(PackageOrg pkgOrg, PackageName pkgName, T t) {
        Map<PackageName, T> pkgNameMap = pkgOrgMap.computeIfAbsent(pkgOrg, orgName -> new HashMap<>());
        pkgNameMap.put(pkgName, t);
        pkgOrgMap.put(pkgOrg, pkgNameMap);
    }

    public Optional<T> get(PackageOrg pkgOrg, PackageName pkgName) {
        Map<PackageName, T> pkgNameMap = pkgOrgMap.get(pkgOrg);
        if (pkgNameMap == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(pkgNameMap.get(pkgName));
    }

    public T getOrElseThrow(PackageOrg pkgOrg, PackageName pkgName) {
        T value = null;
        Map<PackageName, T> pkgNameMap = pkgOrgMap.get(pkgOrg);
        if (pkgNameMap != null) {
            value = pkgNameMap.get(pkgName);
        }

        if (value == null) {
            throw new NoSuchElementException("No value present");
        }
        return value;
    }

    public boolean contains(PackageOrg pkgOrg, PackageName pkgName) {
        Map<PackageName, T> pkgNameMap = pkgOrgMap.get(pkgOrg);
        if (pkgNameMap == null) {
            return false;
        }

        return pkgNameMap.containsKey(pkgName);
    }

    public Collection<T> getAll() {
        return pkgOrgMap.values()
                .stream()
                .map(Map::values)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }
}
