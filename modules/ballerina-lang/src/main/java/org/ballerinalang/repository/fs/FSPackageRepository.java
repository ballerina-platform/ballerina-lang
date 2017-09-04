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
package org.ballerinalang.repository.fs;

import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.repository.PackageEntity;
import org.ballerinalang.repository.PackageRepository;
import org.ballerinalang.repository.PackageSource;
import org.ballerinalang.repository.PackageSourceEntry;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This represents a local file system based {@link PackageRepository}.
 *
 * @since 0.94
 */
public class FSPackageRepository implements PackageRepository {

    private Path basePath;

    public FSPackageRepository(Path basePath) {
        this.basePath = basePath;
    }

    private PackageSource lookupPackageSource(PackageID pkgID) {
        Path path = this.generatePath(pkgID);
        if (!Files.isDirectory(path)) {
            return null;
        }
        return new FSPackageSource(pkgID, path);
    }

    private PackageSource lookupPackageSource(PackageID pkgID, String entryName) {
        Path path = this.generatePath(pkgID);
        if (!Files.isDirectory(path)) {
            return null;
        }
        return new FSPackageSource(pkgID, path, entryName);
    }

    @Override
    public PackageEntity loadPackage(PackageID pkgID) {
        PackageEntity result = null;
        //TODO check compiled packages first
        if (result == null) {
            result = this.lookupPackageSource(pkgID);
        }
        return result;
    }

    @Override
    public PackageEntity loadPackage(PackageID pkgID, String entryName) {
        PackageEntity result = null;
        //TODO check compiled packages first
        if (result == null) {
            result = this.lookupPackageSource(pkgID, entryName);
        }
        return result;
    }

    private Path generatePath(PackageID pkgID) {
        return this.basePath.resolve(pkgID.getPackageName().getValue().replace('.', File.separatorChar));
    }

    /**
     * This represents a local file system based {@link FSPackageSource}.
     *
     * @since 0.94
     */
    public class FSPackageSource implements PackageSource {

        private static final String BAL_SOURCE_EXT = ".bal";

        private PackageID pkgID;

        private Path pkgPath;

        private List<String> cachedEntryNames;

        public FSPackageSource(PackageID pkgID, Path pkgPath) {
            this.pkgID = pkgID;
            this.pkgPath = pkgPath;
        }

        public FSPackageSource(PackageID pkgID, Path pkgPath, String entryName) {
            this.pkgID = pkgID;
            this.pkgPath = pkgPath;
            if (!Files.exists(pkgPath.resolve(entryName))) {
                throw new RuntimeException("The entry name: " + entryName + " does not exist at package: " + pkgID);
            }
            this.cachedEntryNames = Arrays.asList(entryName);
        }

        @Override
        public PackageID getPackageId() {
            return pkgID;
        }

        @Override
        public List<String> getEntryNames() {
            if (this.cachedEntryNames == null) {
                try {
                    List<Path> files = Files.walk(this.pkgPath).filter(
                            Files::isRegularFile).filter(e -> e.getFileName().toString().endsWith(BAL_SOURCE_EXT)).
                            collect(Collectors.toList());
                    this.cachedEntryNames = new ArrayList<>(files.size());
                    files.stream().forEach(e -> this.cachedEntryNames.add(e.getFileName().toString()));
                } catch (IOException e) {
                    throw new RuntimeException("Error in listing packages at '" + this.pkgID +
                            "': " + e.getMessage(), e);
                }
            }
            return this.cachedEntryNames;
        }

        @Override
        public PackageSourceEntry getPackageSourceEntry(String name) {
            return new FSPackageSourceEntry(name);
        }

        @Override
        public List<PackageSourceEntry> getPackageSourceEntries() {
            return this.getEntryNames().stream().map(e -> new FSPackageSourceEntry(e)).collect(Collectors.toList());
        }

        /**
         * This represents local file system based {@link PackageSourceEntry}.
         *
         * @since 0.94
         */
        public class FSPackageSourceEntry implements PackageSourceEntry {

            private String name;

            private byte[] code;

            public FSPackageSourceEntry(String name) {
                this.name = name;
                Path filePath = basePath.resolve(name);
                try {
                    this.code = Files.readAllBytes(basePath.resolve(pkgPath).resolve(name));
                } catch (IOException e) {
                    throw new RuntimeException("Error in loading package source entry '" + filePath +
                            "': " + e.getMessage(), e);
                }
            }

            @Override
            public PackageID getPackageID() {
                return pkgID;
            }

            @Override
            public String getEntryName() {
                return name;
            }

            @Override
            public byte[] getCode() {
                return code;
            }

        }

        @Override
        public PackageRepository getPackageRepository() {
            return FSPackageRepository.this;
        }

        @Override
        public Kind getKind() {
            return Kind.SOURCE;
        }

        @Override
        public String getName() {
            return this.getPackageId().toString();
        }

    }

}
