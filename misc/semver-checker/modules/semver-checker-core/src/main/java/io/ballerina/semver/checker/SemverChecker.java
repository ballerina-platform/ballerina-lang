/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.semver.checker;

import io.ballerina.projects.Package;
import io.ballerina.projects.Project;
import io.ballerina.projects.SemanticVersion;
import io.ballerina.semver.checker.central.CentralClientWrapper;
import io.ballerina.semver.checker.comparator.PackageComparator;
import io.ballerina.semver.checker.diff.PackageDiff;
import io.ballerina.semver.checker.exception.BallerinaSemverToolException;
import io.ballerina.semver.checker.util.PackageUtils;

import java.io.PrintStream;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Semver checker API.
 *
 * @since 2201.2.0
 */
public class SemverChecker {

    private final Path projectPath;
    private Package currentPackage;
    private final PrintStream outStream;
    private final PrintStream errStream;

    public SemverChecker(Path projectPath) {
        this.projectPath = projectPath;
        this.outStream = System.out;
        this.errStream = System.err;
    }

    public SemanticVersion getCurrentVersion() throws BallerinaSemverToolException {
        if (this.currentPackage == null) {
            this.currentPackage = loadPackage(projectPath);
        }
        return SemanticVersion.from(currentPackage.packageVersion().value().toString());
    }

    public Optional<PackageDiff> getSuggestedVersion() throws BallerinaSemverToolException {
        if (this.currentPackage == null) {
            this.currentPackage = loadPackage(projectPath);
        }

        String orgName = currentPackage.packageOrg().value();
        String pkgName = currentPackage.packageName().value();
        SemanticVersion pkgVersion = currentPackage.packageVersion().value();

        CentralClientWrapper clientWrapper = new CentralClientWrapper();
        SemanticVersion compatibleVersion = clientWrapper.getLatestCompatibleVersion(orgName, pkgName, pkgVersion);
        Path balaPath = clientWrapper.pullPackage(orgName, pkgName, compatibleVersion);
        Package balaPackage = loadPackage(balaPath);

        PackageComparator packageComparator = new PackageComparator(currentPackage, balaPackage);
        return packageComparator.computeDiff();
    }

    private Package loadPackage(Path projectPath) throws BallerinaSemverToolException {
        Project project = PackageUtils.loadProject(projectPath);
        switch (project.kind()) {
            case BUILD_PROJECT:
            case BALA_PROJECT:
                return project.currentPackage();
            case SINGLE_FILE_PROJECT:
                throw new BallerinaSemverToolException("semver checker tool is not applicable for single file " +
                        "projects.");
            default:
                throw new BallerinaSemverToolException("semver checker tool is not applicable for " +
                        project.kind().name());
        }
    }
}
