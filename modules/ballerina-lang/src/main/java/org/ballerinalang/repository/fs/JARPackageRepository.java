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

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This represents a jar file based {@link PackageRepository}.
 *
 * @since 0.94
 */
public class JARPackageRepository extends GeneralFSPackageRepository {

    public JARPackageRepository(String basePath) {
        super(generatePath(basePath));
    }
    
    private static Path generatePath(String basePath) {
        try {
            return Paths.get(JARPackageRepository.class.getResource(basePath).toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }        
    }

}
