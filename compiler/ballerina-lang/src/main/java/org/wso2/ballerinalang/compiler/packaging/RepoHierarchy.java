/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.ballerinalang.compiler.packaging;

import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.repository.CompilerInput;
import org.wso2.ballerinalang.compiler.packaging.converters.Converter;
import org.wso2.ballerinalang.compiler.packaging.repo.Repo;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * Keeps list of n Repos in lookup order.
 * Also has a list of (n-1) child RepoHierarchy objects.
 * <p>
 * (n-1) because, each Repo is matched with one child RepoHierarchy,
 * except for the fist one, it matches with this object.
 */
public class RepoHierarchy {

    private final Repo[] repos;
    private final RepoHierarchy[] dags;
    private static final boolean verbose = Boolean.parseBoolean(System.getenv("BALLERINA_REPO_LOOKUP_VERBOSE"));
    private static final PrintStream out = System.out;

    RepoHierarchy(Repo[] repos, RepoHierarchy[] dags) {
        this.repos = repos;
        this.dags = dags;
    }

    public Resolution resolve(PackageID pkg) {
        log1(pkg);
        for (int i = 0; i < repos.length; i++) {
            Repo repo = repos[i];
            Patten patten = repo.calculate(pkg);
            if (patten != Patten.NULL) {
                List<CompilerInput> paths = new ArrayList<>();
                Converter converter = repo.getConverterInstance();
                try {
                    paths = patten.convertToSources(converter, pkg).collect(Collectors.toList());
                } catch (NoSuchElementException e) {
                    //TODO: This happens on some operating system intermittently when an unknown module is imported
                    //TODO: Fix this properly issue #20215
                }
                log2(repo, patten, paths);
                if (!paths.isEmpty()) {
                    return new Resolution(getChildHierarchyForRepo(i), paths);
                }
            } else {
                log3(repo);
            }
        }
        log4();
        return Resolution.NOT_FOUND;
    }

    private void log1(PackageID pkg) {
        if (verbose) {
            out.println("Searching " + pkg);
        }
    }

    private void log2(Repo repo, Patten patten, List<CompilerInput> paths) {
        if (verbose) {
            out.println("\t looking in " + repo + " for patten\n\t\t" +
                                patten + " and found \n\t\t\t" +
                                paths);
        }
    }

    private void log3(Repo repo) {
        if (verbose) {
            out.println("\t skipping " + repo);
        }
    }

    private void log4() {
        if (verbose) {
            out.println("\t could not find");
        }
    }

    private RepoHierarchy getChildHierarchyForRepo(int repoIndex) {
        if (repoIndex > 0) {
            return dags[repoIndex - 1];
        } else {
            return this;
        }
    }

    @Override
    public String toString() {
        return "{r:" + Arrays.toString(repos) +
                ", d:" + Arrays.toString(dags) + "}";
    }

}
