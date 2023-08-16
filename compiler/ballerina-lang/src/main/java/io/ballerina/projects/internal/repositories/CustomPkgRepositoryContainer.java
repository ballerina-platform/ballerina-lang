/*
 * Copyright (c) 2023, WSO2 LLC. (http://wso2.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.ballerina.projects.internal.repositories;

import java.util.Map;

/**
 * This class acts as a custom package repositories' container.
 *
 * @since 2201.8.0
 */

public class CustomPkgRepositoryContainer {
    Map<String, MavenPackageRepository> customPackageRepositories;
    public CustomPkgRepositoryContainer(Map<String, MavenPackageRepository> customPackageRepositories) {
        this.customPackageRepositories = customPackageRepositories;
    }

    public Map<String, MavenPackageRepository> getCustomPackageRepositories() {
        return customPackageRepositories;
    }
}
