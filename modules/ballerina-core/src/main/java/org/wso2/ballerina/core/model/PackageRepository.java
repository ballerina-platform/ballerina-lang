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
package org.wso2.ballerina.core.model;


import java.io.InputStream;
import java.nio.file.Path;
import java.util.Map;

/**
 * @since 0.8.0
 */
public class PackageRepository {

    public PackageSource loadPackage(Path packageDirPath) {
        return null;
    }

    /**
     *
     * @since 0.8.0
     */
    public class PackageSource {
        private Path packagePath;
        private Map<String, InputStream> sourceFileStreamMap;
        private PackageRepository packageRepository;

        public PackageSource(Path packagePath,
                             Map<String, InputStream> sourceFileStreamMap,
                             PackageRepository packageRepository) {
            this.packagePath = packagePath;
            this.sourceFileStreamMap = sourceFileStreamMap;
            this.packageRepository = packageRepository;
        }

        public PackageRepository getPackageRepository() {
            return packageRepository;
        }

        public Path getPackagePath() {
            return packagePath;
        }

        public Map<String, InputStream> getSourceFileStreamMap() {
            return sourceFileStreamMap;
        }
    }
}
