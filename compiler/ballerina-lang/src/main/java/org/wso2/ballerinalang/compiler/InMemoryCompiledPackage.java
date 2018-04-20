/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler;

import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.repository.CompiledPackage;
import org.ballerinalang.repository.CompilerOutputEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code InMemoryCompiledPackage} represents a in-memory version of a compiled Ballerina package.
 *
 * @since 0.970.0
 */
public class InMemoryCompiledPackage implements CompiledPackage {
    public PackageID pkgID;
    public List<CompilerOutputEntry> srcEntries;
    public CompilerOutputEntry pkgMDEntry;
    public CompilerOutputEntry pkgBinaryEntry;

    public InMemoryCompiledPackage(PackageID pkgID) {
        this.pkgID = pkgID;
        this.srcEntries = new ArrayList<>();
    }

    @Override
    public PackageID getPackageID() {
        return this.pkgID;
    }

    @Override
    public List<CompilerOutputEntry> getSourceEntries() {
        return this.srcEntries;
    }

    @Override
    public void addSourceEntry(CompilerOutputEntry compiledPackageEntry) {
        this.srcEntries.add(compiledPackageEntry);
    }

    @Override
    public CompilerOutputEntry getPackageMDEntry() {
        return pkgMDEntry;
    }

    @Override
    public CompilerOutputEntry getPackageBinaryEntry() {
        return pkgBinaryEntry;
    }

    @Override
    public void setPackageBinaryEntry(CompilerOutputEntry entry) {
        this.pkgBinaryEntry = entry;
    }

    @Override
    public List<CompilerOutputEntry> getAllEntries() {
        List<CompilerOutputEntry> allEntries = new ArrayList<>(srcEntries);
        allEntries.add(pkgBinaryEntry);
        if (pkgMDEntry != null) {
            allEntries.add(pkgMDEntry);
        }
        return allEntries;
    }

    @Override
    public Kind getKind() {
        return Kind.FROM_SOURCE;
    }
}
