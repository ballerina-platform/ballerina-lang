/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.toml.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Defines the repositories object which is defines the remote repositories that can be used for package management.
 *
 * @since 0.971
 */
public class Repositories {
    private List<String> repoOrder = new ArrayList<>();
    private List<Repository> repositories = new ArrayList<>();

    /**
     * Get default repositories.
     *
     * @return list of default repositories
     */
    public List<String> getRepoOrder() {
        return repoOrder;
    }

    /**
     * Set default repositories.
     *
     * @param repoOrder list of default repositories
     */
    public void setRepoOrder(List<String> repoOrder) {
        this.repoOrder = repoOrder;
    }

    /**
     * Get list of repositories.
     *
     * @return repositories list
     */
    public List<Repository> getRepositoryList() {
        return repositories;
    }

    /**
     * Add a repository to the repositories list.
     *
     * @param repository repository object
     */
    public void addRepositories(Repository repository) {
        repositories.add(repository);
        repositories = removeDuplicates(repositories);
    }

    /**
     * Remove duplicates from repositories list.
     *
     * @param list list of elements
     * @return repositories list without duplicates
     */
    private List<Repository> removeDuplicates(List<Repository> list) {
        return list.stream().distinct().collect(Collectors.toList());
    }
}
