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
package io.ballerina.projects.repos;

import io.ballerina.projects.environment.Environment;
import io.ballerina.projects.internal.repositories.FileSystemRepository;
import io.ballerina.projects.utils.ProjectConstants;

import java.nio.file.Path;

/**
 * Distribution cache.
 *
 * @since 2.0.0
 */
public class BallerinaDistributionRepository extends FileSystemRepository {

    public BallerinaDistributionRepository(Environment environment, Path distributionPath) {
        super(environment, distributionPath.resolve(ProjectConstants.DIST_CACHE_DIRECTORY));
    }
}
