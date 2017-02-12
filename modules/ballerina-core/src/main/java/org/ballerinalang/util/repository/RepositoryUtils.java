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
package org.ballerinalang.util.repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class RepositoryUtils {

    public static ProgramPackageRepository initializeProgramRepository(Path programDirPath) {
        // TODO Refactor this method
        // 1) Create System repository
        String ballerinaHome = System.getProperty("ballerina.home");
        if (ballerinaHome == null || ballerinaHome.isEmpty()) {
            throw new IllegalStateException("ballerina.home is not set");
        }

        Path systemRepoPath = Paths.get(ballerinaHome);
        if (Files.notExists(systemRepoPath)) {
            throw new IllegalStateException("ballerina installation directory does not exists");
        }

        SystemPackageRepository systemPkgRepo = new SystemPackageRepository(systemRepoPath);

        // 2) Create user repository
        Path userPkgRepoPath;
        String ballerinaRepoProp = System.getenv("BALLERINA_REPOSITORY");
        if (ballerinaRepoProp == null || ballerinaRepoProp.isEmpty()) {
            String userHome = System.getProperty("user.home");
            userPkgRepoPath = Paths.get(userHome, ".ballerina");
            if (Files.notExists(userPkgRepoPath)) {
                try {
                    Files.createDirectory(userPkgRepoPath);
                } catch (IOException e) {
                    throw new IllegalStateException("failed to create user repository at '" +
                            userPkgRepoPath.toString() + "'");
                }
            }
        } else {
            userPkgRepoPath = Paths.get(ballerinaRepoProp);
        }

        UserPackageRepository userPkgRepo = new UserPackageRepository(userPkgRepoPath, systemPkgRepo);
        return new ProgramPackageRepository(programDirPath, systemPkgRepo, userPkgRepo);
    }

}
