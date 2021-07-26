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
package org.ballerinalang.repository;

import org.ballerinalang.model.elements.PackageID;

/**
 * This represents a package file, which is the artifact being loaded from a {@link PackageRepository}.
 * 
 * @since 0.94
 */
public interface PackageEntity {

    /**
     * @since 0.94
     */
    enum Kind {
        SOURCE(".bal"),
        COMPILED(".bala"),
        COMPILED_BIR(".bir"); //TODO rajith, are we going to change the extention to bir?

        private final String extension;

        Kind(String extension) {
            this.extension = extension;
        }

        public String getExtension() {
            return extension;
        }
    }

    /**
     * Returns the package identifier package source belongs to.
     *
     * @return the package ID
     */
    PackageID getPackageId();

    Kind getKind();

    String getName();
}
