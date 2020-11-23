/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.projects.internal.repositories;

import io.ballerina.projects.Package;
import io.ballerina.projects.PackageVersion;
import io.ballerina.projects.environment.PackageLoadRequest;
import io.ballerina.projects.environment.PackageRepository;
import org.ballerinalang.central.client.CentralAPIClient;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Remote Repository stored in remote central.
 *
 * @since 2.0.0
 */
public class RemoteRepository implements PackageRepository {

    private FileSystemRepository fileSystemRepo;
    private CentralAPIClient client;


    public RemoteRepository(FileSystemRepository fileSystemRepo) {
        this.fileSystemRepo = fileSystemRepo;
        this.client = new CentralAPIClient();
    }

    @Override
    public Optional<Package> getPackage(PackageLoadRequest packageLoadRequest) {
        String packageName = packageLoadRequest.packageName().value();
        String orgName = packageLoadRequest.orgName().value();
        String version = packageLoadRequest.version().isPresent() ?
                packageLoadRequest.version().get().toString() : null;

        Path packagePathInBaloCache = this.fileSystemRepo.balo.resolve(orgName).resolve(packageName);
        this.client.pullPackage(orgName, packageName, version, packagePathInBaloCache, "any", false);

        return this.fileSystemRepo.getPackage(packageLoadRequest);
    }

    @Override
    public List<PackageVersion> getPackageVersions(PackageLoadRequest packageLoadRequest) {
        String orgName = packageLoadRequest.orgName().value();
        String packageName = packageLoadRequest.packageName().value();

        List<PackageVersion> packageVersions = new ArrayList<>();

        for (String version : this.client.getPackageVersions(orgName, packageName)) {
            packageVersions.add(PackageVersion.from(version));
        }
        return packageVersions;
    }

    @Override
    public Map<String, List<String>> getPackages() {
        throw new UnsupportedOperationException();
    }
}
