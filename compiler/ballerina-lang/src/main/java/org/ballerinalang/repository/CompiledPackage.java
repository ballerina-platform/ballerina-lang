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
package org.ballerinalang.repository;

import org.ballerinalang.model.elements.PackageID;

import java.util.List;

/**
 * {@code CompiledPackage} represents a compiled Ballerina package.
 *
 * @since 0.970.0
 */
public interface CompiledPackage {

    /**
     * Specify the kinds of the compiled packages.
     *
     * @since 0.970.0
     */
    enum Kind {
        FROM_SOURCE,
        FROM_BINARY
    }

    PackageID getPackageID();

    List<CompilerOutputEntry> getSourceEntries();

    void addSourceEntry(CompilerOutputEntry compiledPackageEntry);

    CompilerOutputEntry getPackageMDEntry();

    CompilerOutputEntry getPackageBinaryEntry();

    void setPackageBinaryEntry(CompilerOutputEntry entry);

    List<CompilerOutputEntry> getAllEntries();

    Kind getKind();
}
