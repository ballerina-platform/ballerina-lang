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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.ballerina.projects.PackageName;
import io.ballerina.projects.PackageOrg;
import io.ballerina.projects.ProjectException;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.TextProgressMonitor;
import org.eclipse.jgit.merge.ContentMergeStrategy;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static io.ballerina.projects.util.ProjectConstants.INDEX_JSON_FILE_NAME;
import static org.wso2.ballerinalang.util.RepoUtils.SET_BALLERINA_DEV_CENTRAL;

/**
 * This class is responsible for updating the package index repository cache and loading the packages to the index.
 *
 * @since 2201.12.0
 */
class PackageIndexUpdater {
    private final Path indexDirectory;
    private final String indexGitRepoName;
    private final String remoteRepoUrl;
    private final CredentialsProvider credentialsProvider;
    private boolean isIndexRepoUpdated = false;
    private PackageIndex packageIndex;
    private boolean offline = false;
    private final PrintStream outStream = System.out;

    PackageIndexUpdater(Path indexPath, String indexGitRepoName,
                        String remoteRepoUrl, CredentialsProvider credentialsProvider) {
        this.indexDirectory = indexPath;
        this.indexGitRepoName = indexGitRepoName;
        this.remoteRepoUrl = remoteRepoUrl;
        this.credentialsProvider = credentialsProvider;
    }

    private void updateIndexRepoCache() {
        if (isIndexRepoUpdated) {
            return;
        }
        if (!isIndexFetched()) {
            if (offline) {
                outStream.println("Index repository is not available. " +
                        "Try again without the 'offline' flag set to false");
                return;
            }
            fetchIndex();
        }
        checkoutToBranch();
        if (!offline) {
            fetchIndexHead();
        }
        isIndexRepoUpdated = true;
    }

    void setOffline(boolean offline) {
        this.offline = offline;
    }

    void setPackageIndex(PackageIndex packageIndex) {
        this.packageIndex = packageIndex;
    }

    public void loadOrg(PackageOrg packageOrg) {
        Path orgDir = indexDirectory.resolve(indexGitRepoName).resolve(packageOrg.value());
        if (!Files.exists(orgDir)) {
            return;
        }
        try (Stream<Path> files = Files.list(orgDir)) {
            for (Path packageFile : files.toList()) {
                loadPackage(packageOrg, PackageName.from(packageFile.getFileName().toString().replace(".json", "")));
            }
        } catch (IOException e) {
            throw new ProjectException("Error reading index files: " + e.getMessage(), e);
        }
    }

    // TODO: Possible performance improvements. Can research on this area more if needed.
    //  1. load all ballerina* packages to the index prematurely.
    //  2. load all the packages of an org when a package of that org is requested.
    public void loadPackage(PackageOrg packageOrg, PackageName packageName) {
        updateIndexRepoCache();
        Path orgDir = indexDirectory.resolve(indexGitRepoName).resolve(packageOrg.value());
        Path packageFile = orgDir.resolve(packageName.value()).resolve(INDEX_JSON_FILE_NAME);
        if (!Files.isRegularFile(packageFile)) {
            return;
        }
        try {
            List<String> content = Files.readAllLines(packageFile);
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(IndexPackage.class, new IndexPackageAdapter())
                    .create();
            List<IndexPackage> packagesOfOrg = new ArrayList<>();
            for (String jsonString : content) {
                packagesOfOrg.add(gson.fromJson(jsonString, IndexPackage.class));
            }
            packageIndex.addPackage(packagesOfOrg);
        } catch (IOException e) {
            throw new ProjectException("Error reading index file: " + packageFile + ": " + e.getMessage(), e);
        }
    }

    private boolean isIndexFetched() {
        if (!indexDirectory.resolve(indexGitRepoName).toFile().isDirectory()) {
            return false;
        }
        File gitDir = new File(indexDirectory.resolve(indexGitRepoName).toFile(), ".git");
        return gitDir.isDirectory();
    }

    private void fetchIndex() {
        // TODO: add a progress bar
        try {
            Files.createDirectories(indexDirectory);
        } catch (IOException e) {
            throw new ProjectException("Error while creating the package index directory: " + e.getMessage());
        }
        try (Git git = Git.cloneRepository()
                .setURI(remoteRepoUrl)
                .setDirectory(indexDirectory.resolve(indexGitRepoName).toFile())
                .setBranchesToClone(Arrays.asList("refs/heads/prod", "refs/heads/dev", "refs/heads/stage"))
                .setCredentialsProvider(credentialsProvider)
                .call()) {
            git.checkout().setCreateBranch(true).setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK)
                    .setStartPoint("origin/prod").setName("prod").call();
            git.checkout().setCreateBranch(true).setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK)
                    .setStartPoint("origin/dev").setName("dev").call();
            git.checkout().setCreateBranch(true).setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK)
                    .setStartPoint("origin/stage").setName("stage").call();
        } catch (GitAPIException e) {
            throw new ProjectException("Error while cloning the package index repository: " + e.getMessage(), e);
        }
    }

    private void checkoutToBranch() {
        Branch branch = Branch.PROD;
        if (RepoUtils.SET_BALLERINA_STAGE_CENTRAL) {
            branch = Branch.STAGE;
        } else if (SET_BALLERINA_DEV_CENTRAL) {
            branch = Branch.DEV;
        }
        try (Git git = Git.open(indexDirectory.resolve(indexGitRepoName).toFile())) {
            git.checkout().setName(branch.branchName()).call();
        } catch (IOException | GitAPIException e) {
            throw new ProjectException("Error while checking out to the " + branch + " branch: " + e.getMessage(), e);
        }
    }

    private void fetchIndexHead() {
        // TODO: add a progress bar
        try (Git git = Git.open(indexDirectory.resolve(indexGitRepoName).toFile())) {
            git.pull()
                    .setContentMergeStrategy(ContentMergeStrategy.THEIRS)
                    .setProgressMonitor(new TextProgressMonitor(new PrintWriter(System.out))) // TODO: customize
                    .setCredentialsProvider(credentialsProvider).call();
        } catch (IOException | GitAPIException e) {
            throw new ProjectException("Error while pulling the latest changes from the upstream: "
                    + e.getMessage(), e);
        }
    }

    // TODO: this implicitly assumes that the index always has these 3 branches. Need to discuss.
    private enum Branch {
        PROD("prod"),
        DEV("dev"),
        STAGE("stage");
        private final String branchName;
        Branch(String branchName) {
            this.branchName = branchName;
        }
        public String branchName() {
            return branchName;
        }
    }
}
