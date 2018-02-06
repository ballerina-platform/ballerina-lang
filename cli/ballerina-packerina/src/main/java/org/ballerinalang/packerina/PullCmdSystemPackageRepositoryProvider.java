/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.packerina;

import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.repository.PackageRepository;
import org.ballerinalang.repository.fs.ClasspathPackageRepository;
import org.ballerinalang.spi.SystemPackageRepositoryProvider;

/**
 * This represents the Ballerina pull system package repository provider.
 */
@JavaSPIService("org.ballerinalang.spi.SystemPackageRepositoryProvider")
public class PullCmdSystemPackageRepositoryProvider implements SystemPackageRepositoryProvider {

    private static final String JAR_SYSTEM_LIB_LOCATION = "/META-INF/ballerina/";

    /**
     * Loads the package repository
     *
     * @return loaded package repository
     */
    @Override
    public PackageRepository loadRepository() {
        return new ClasspathPackageRepository(this.getClass(), JAR_SYSTEM_LIB_LOCATION);
    }

}