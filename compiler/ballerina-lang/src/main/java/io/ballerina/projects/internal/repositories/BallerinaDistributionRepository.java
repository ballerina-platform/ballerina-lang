/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.projects.internal.repositories;

import io.ballerina.projects.ProjectException;
import io.ballerina.projects.environment.Environment;
import io.ballerina.projects.util.ProjectConstants;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * This class represents the package respository available in the Ballerina distribution.
 *
 * @since 2.0.0
 */
public class BallerinaDistributionRepository extends FileSystemRepository {

    private BallerinaDistributionRepository(Environment environment, Path distributionRepoPath) {
        super(environment, distributionRepoPath);
    }

    public static BallerinaDistributionRepository from(Environment environment, Path distributionPath) {
        Path distributionRepoPath = distributionPath.resolve(ProjectConstants.DIST_CACHE_DIRECTORY);
        if (Files.notExists(distributionPath)) {
            throw new ProjectException("Ballerina distribution repository does not exists: " +
                    distributionRepoPath);
        }
        return new BallerinaDistributionRepository(environment, distributionRepoPath);
    }
}
