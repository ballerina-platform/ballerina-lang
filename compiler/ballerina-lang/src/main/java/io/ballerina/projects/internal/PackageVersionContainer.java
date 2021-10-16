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
import io.ballerina.projects.PackageVersion;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A container for various package representation that provides efficient access to packages.
 *
 * @param <T> element type
 */
public class PackageVersionContainer<T> {
    private final Map<PackageOrg, Map<PackageName, Map<PackageVersion, T>>> pkgOrgMap;

    public PackageVersionContainer() {
        this.pkgOrgMap = new HashMap<>();
    }

    public void add(PackageOrg pkgOrg, PackageName pkgName, PackageVersion pkgVersion, T t) {
        Map<PackageName, Map<PackageVersion, T>> pkgNameMap = pkgOrgMap.computeIfAbsent(pkgOrg,
                orgName -> new HashMap<>());
        Map<PackageVersion, T> pkgVersionMap = pkgNameMap.computeIfAbsent(pkgName, name -> new HashMap<>());
        pkgVersionMap.put(pkgVersion, t);
    }

    public Optional<T> get(PackageOrg pkgOrg, PackageName pkgName, PackageVersion pkgVersion) {
        Map<PackageName, Map<PackageVersion, T>> pkgNameMap = pkgOrgMap.get(pkgOrg);
        if (pkgNameMap == null) {
            return Optional.empty();
        }

        Map<PackageVersion, T> pkgVersionMap = pkgNameMap.get(pkgName);
        if (pkgVersionMap == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(pkgVersionMap.get(pkgVersion));
    }

    public Collection<T> get(PackageOrg pkgOrg, PackageName pkgName) {
        Map<PackageName, Map<PackageVersion, T>> pkgNameMap = pkgOrgMap.get(pkgOrg);
        if (pkgNameMap == null) {
            return Collections.emptyList();
        }

        Map<PackageVersion, T> pkgVersionMap = pkgNameMap.get(pkgName);
        if (pkgVersionMap == null) {
            return Collections.emptyList();
        }

        return pkgVersionMap.values();
    }

    public Collection<T> getAll() {
        return pkgOrgMap.values()
                .stream()
                .map(Map::values)
                .flatMap(Collection::stream)
                .map(Map::values)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }
}
