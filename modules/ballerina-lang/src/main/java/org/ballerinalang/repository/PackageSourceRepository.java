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
 * This represents a package source repository. For example, implementation can be used to
 * fetch source code from the file system, network etc...
 * 
 * @since 0.94
 */
public interface PackageSourceRepository {
    
    /**
     * Looks up and returns a {@link PackageSource} given the package identifier.
     * 
     * @param pkgID the package ID
     * @return The package source is returned if it's available, or else, null will be returned.
     */
    PackageSource getPackageSource(PackageID pkgID);

}
