/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.projects.internal.index;

import org.eclipse.jgit.transport.CredentialsProvider;
import org.wso2.ballerinalang.util.RepoUtils;

import java.nio.file.Path;

import static io.ballerina.projects.util.ProjectConstants.INDEX_DIR_NAME;

/**
 * Builder for creating a {@code PackageIndex}.
 *
 * @since 2201.12.0
 */
public class PackageIndexBuilder {
    // TODO: add correct values for the below static variables
    // TODO: "central.ballerina.io-index" similar to rust's "crates.io-index"
    private static final String DEFAULT_INDEX_GIT_REPO_NAME = "ballerina-index-test";
    private static final String DEFAULT_REMOTE_REPO_URL
            = "https://github.com/gayaldassanayake/ballerina-index-test.git";

    private Path indexPath;
    private String indexGitRepoName;
    private String remoteRepoUrl;
    private CredentialsProvider credentialsProvider;

    /**
     * Creates a new {@code PackageIndexBuilder} instance.
     * The fields are set to default values pointing to the index of the Ballerina central.
     *
     */
    public PackageIndexBuilder() {
        this.indexPath = RepoUtils.createAndGetHomeReposPath().resolve(INDEX_DIR_NAME);
        this.indexGitRepoName = DEFAULT_INDEX_GIT_REPO_NAME;
        this.remoteRepoUrl = DEFAULT_REMOTE_REPO_URL;
        this.credentialsProvider = CredentialsProvider.getDefault();
    }

    /**
     * Sets the path to the index.
     *
     * @param indexPath the path to the index
     * @return the updated {@code PackageIndexBuilder} instance
     */
    public PackageIndexBuilder indexPath(Path indexPath) {
        this.indexPath = indexPath;
        return this;
    }

    /**
     * Sets the name of the git repository where the index is stored.
     *
     * @param indexGitRepoName the name of the git repository
     * @return the updated {@code PackageIndexBuilder} instance
     */
    public PackageIndexBuilder indexGitRepoName(String indexGitRepoName) {
        this.indexGitRepoName = indexGitRepoName;
        return this;
    }

    /**
     * Sets the URL of the remote repository where the index is stored.
     *
     * @param remoteRepoUrl the URL of the remote repository
     * @return the updated {@code PackageIndexBuilder} instance
     */
    public PackageIndexBuilder remoteRepoUrl(String remoteRepoUrl) {
        this.remoteRepoUrl = remoteRepoUrl;
        return this;
    }

    /**
     * Sets the credentials provider for the remote repository.
     *
     * @param credentialsProvider the credentials provider
     * @return the updated {@code PackageIndexBuilder} instance
     */
    public PackageIndexBuilder credentialsProvider(CredentialsProvider credentialsProvider) {
        this.credentialsProvider = credentialsProvider;
        return this;
    }

    /**
     * Builds a new {@code PackageIndex} instance.
     *
     * @return the new {@code PackageIndex} instance
     */
    public PackageIndex build() {
        PackageIndexUpdater indexUpdater = new PackageIndexUpdater(
                indexPath, indexGitRepoName, remoteRepoUrl, credentialsProvider);
        PackageIndex packageIndex = new PackageIndex(indexUpdater);
        indexUpdater.setPackageIndex(packageIndex);
        return packageIndex;
    }
}
