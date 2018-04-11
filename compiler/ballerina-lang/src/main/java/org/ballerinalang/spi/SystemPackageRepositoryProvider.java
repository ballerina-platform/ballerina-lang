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
package org.ballerinalang.spi;

import org.ballerinalang.repository.PackageRepository;
import org.wso2.ballerinalang.compiler.packaging.repo.Repo;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * This represents the Java SPI interface for a Ballerina system package repository provider.
 *
 * @since 0.94
 */
public interface SystemPackageRepositoryProvider {

    /**
     * Temp workaround to order the jar repos. until we have ship system code as balo
     *
     * @return more dependent jars should have higher dependentLevel
     */
    int dependentLevel();

    static URI getClassUri(Object obj) {
        try {
            return obj.getClass().getProtectionDomain().getCodeSource().getLocation().toURI();
        } catch (URISyntaxException ignore) {
            return null;
        }
    }

    /**
     * Loads the system package repository exposed by this service.
     *
     * @return the loaded {@link PackageRepository} object
     */
    Repo loadRepository();

}
