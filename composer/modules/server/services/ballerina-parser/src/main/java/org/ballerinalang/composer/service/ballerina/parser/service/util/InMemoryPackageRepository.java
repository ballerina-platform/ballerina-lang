/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.composer.service.ballerina.parser.service.util;

import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.repository.CompilerInput;
import org.ballerinalang.repository.PackageEntity;
import org.ballerinalang.repository.PackageSource;
import org.ballerinalang.repository.fs.GeneralFSPackageRepository;

import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * InMemoryPackageRepository.
 */
public class InMemoryPackageRepository extends GeneralFSPackageRepository {

    private byte[] code;
    private PackageID pkgID;
    private String name;

    public InMemoryPackageRepository(PackageID pkgID, String basePath, String name, byte[] code) {
        super(Paths.get(basePath));
        this.name = name;
        this.code = code.clone();
        this.pkgID = pkgID;

    }

    private PackageSource lookupPackageSource(PackageID pkgID, byte[] code) {
        // This seems a wrong logic. But file name is received with "pkgID.name.value".
        if (this.name.equals(pkgID.name.value) || this.pkgID.name.value.equals(pkgID.name.value)) {
            return new InMemoryPackageSource(pkgID, name, code);
        } else {
            return null;
        }
    }

    @Override
    public PackageEntity loadPackage(PackageID pkgID) {
        //TODO check compiled packages first
        PackageEntity result = this.lookupPackageSource(pkgID, code);
        return result;
    }

    @Override
    public PackageEntity loadPackage(PackageID pkgID, String entryName) {
        PackageEntity result = this.lookupPackageSource(pkgID, code);
        return result;
    }

    /**
     * InMemoryPackageSource.
     */
    public class InMemoryPackageSource extends FSPackageSource {

        private byte[] code;
        private String name;

        public InMemoryPackageSource(PackageID pkgID, String name, byte[] code) {
            super(pkgID, null);
            this.code = code.clone();
            this.name = name;
        }

        @Override
        public List<CompilerInput> getPackageSourceEntries() {
            return Stream.of(new InMemoryPackageRepository.InMemoryPackageSource.InMemorySourceEntry(name, code))
                    .collect(Collectors.toList());
        }

        @Override
        public List<String> getEntryNames() {
            return Stream.of(name).collect(Collectors.toList());
        }

        /**
         * InMemorySourceEntry.
         */
        public class InMemorySourceEntry implements CompilerInput {

            private String name;
            private byte[] code;

            public InMemorySourceEntry(String name, byte[] code) {
                this.name = name;
                this.code = code.clone();
            }

            @Override
            public String getEntryName() {
                return name;
            }

            @Override
            public byte[] getCode() {
                return code.clone();
            }
        }
    }

    public Set<PackageID> listPackages(int maxDepth) {
        return new LinkedHashSet<>();
    }
}

