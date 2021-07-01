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

import org.wso2.ballerinalang.compiler.util.Name;

import java.nio.file.Paths;

/**
 * This represents a local file system based {@link org.ballerinalang.repository.PackageRepository}.
 *
 * @since 0.94
 */
public class LocalFSPackageRepository extends GeneralFSPackageRepository {

    public LocalFSPackageRepository(String basePath) {
        super(Paths.get(basePath));
    }

    public LocalFSPackageRepository(String basePath, String orgName) {
        super(Paths.get(basePath), new Name(orgName));
    }
}
