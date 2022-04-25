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
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.SemanticVersion;
import io.ballerina.semver.checker.central.CentralClientWrapper;
import io.ballerina.semver.checker.comparator.PackageComparator;
import io.ballerina.semver.checker.diff.CompatibilityLevel;
import io.ballerina.semver.checker.diff.PackageDiff;
import io.ballerina.semver.checker.exception.SemverToolException;
import io.ballerina.semver.checker.util.PackageUtils;
import io.ballerina.tools.diagnostics.Diagnostic;

import java.io.PrintStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.ballerina.semver.checker.util.SemverUtils.BAL_FILE_EXT;
import static io.ballerina.semver.checker.util.SemverUtils.calculateSuggestedVersion;

/**
 * Semver checker API.
 *
 * @since 2201.2.0
 */
public class SemverChecker {

    private final Path projectPath;
    private Package currentPackage;
    private SemanticVersion resolvedReleaseVersion;
    private final PrintStream outStream;
    private final PrintStream errStream;

    public SemverChecker(Path projectPath) {
        this(projectPath, System.out, System.err);
    }

    public SemverChecker(Path projectPath, PrintStream outStream, PrintStream errStream) {
        this.projectPath = projectPath;
        this.outStream = outStream;
        this.errStream = errStream;
    }

    public String suggestVersion() throws SemverToolException {
        Optional<PackageDiff> packageDiff = computeDiff();
        StringBuilder sb = new StringBuilder();
        SemanticVersion currentVersion = getCurrentVersion();
        sb.append("current version: ").append(currentVersion).append(System.lineSeparator());
        sb.append("compatibility status with the latest release (").append(resolvedReleaseVersion).append("): ");
        if (packageDiff.isEmpty()) {
            sb.append("no changes detected");
        } else {
            switch (packageDiff.get().getCompatibilityLevel()) {
                case MAJOR:
                    sb.append("backward-incompatible changes detected.").append(System.lineSeparator());
                    break;
                case MINOR:
                    sb.append("patch-incompatible changes detected.").append(System.lineSeparator());
                    break;
                case PATCH:
                    sb.append("patch-compatible changes detected.").append(System.lineSeparator());
                    break;
                case AMBIGUOUS:
                    sb.append("one or more changes detected with ambiguous level of impact. the developer is expected" +
                            " to manually review the changes below and choose an appropriate version");
                    sb.append(System.lineSeparator());
                    packageDiff.get().getChildDiffs(CompatibilityLevel.AMBIGUOUS)
                            .forEach(diff -> sb.append(diff.getAsString()));
                    break;
                case UNKNOWN:
                default:
                    sb.append("one or more changes detected with unknown level of impact. the developer is expected " +
                            "to manually review the changes below and choose an appropriate version");
                    sb.append(System.lineSeparator());
                    packageDiff.get().getChildDiffs(CompatibilityLevel.UNKNOWN);
                    break;
            }
        }
        sb.append("suggested version: ").append(calculateSuggestedVersion(currentVersion, packageDiff.orElse(null)));
        return sb.toString();
    }

    public String getCompatibilitySummary() throws SemverToolException {
        Optional<PackageDiff> packageDiff = computeDiff();
        StringBuilder sb = new StringBuilder();
        sb.append(System.lineSeparator());
        if (packageDiff.isEmpty()) {
            sb.append("no changes detected");
        } else {
            sb.append(packageDiff.get().getAsString());
        }

        return sb.toString();
    }

    private Optional<PackageDiff> computeDiff() throws SemverToolException {
        loadCurrentPackage();

        String orgName = currentPackage.packageOrg().value();
        String pkgName = currentPackage.packageName().value();
        SemanticVersion pkgVersion = currentPackage.packageVersion().value();

        CentralClientWrapper clientWrapper = new CentralClientWrapper();
        outStream.println("checking for latest compatible release version available in central...");
        resolvedReleaseVersion = clientWrapper.getLatestCompatibleVersion(orgName, pkgName, pkgVersion);
        outStream.println("pulling package version '" + resolvedReleaseVersion + "' from central...");
        outStream.println();
        Path balaPath = clientWrapper.pullPackage(orgName, pkgName, resolvedReleaseVersion);
        Package balaPackage = PackageUtils.loadPackage(balaPath);

        PackageComparator packageComparator = new PackageComparator(currentPackage, balaPackage);
        return packageComparator.computeDiff();
    }

    private SemanticVersion getCurrentVersion() throws SemverToolException {
        loadCurrentPackage();
        return SemanticVersion.from(currentPackage.packageVersion().value().toString());
    }

    private void loadCurrentPackage() throws SemverToolException {
        if (this.currentPackage == null) {
            this.currentPackage = PackageUtils.loadPackage(projectPath);
            PackageCompilation compilation = currentPackage.getCompilation();
            if (!compilation.diagnosticResult().hasErrors()) {
                return;
            }

            // Ignores .toml diagnostics as we consider source code compatibility.
            // Todo - support toml changes validation
            List<Diagnostic> srcErrors = compilation.diagnosticResult().errors().stream()
                    .filter(diagnostic -> diagnostic.location().lineRange().filePath().endsWith(BAL_FILE_EXT))
                    .collect(Collectors.toList());
            if (srcErrors.isEmpty()) {
                return;
            }

            StringBuilder sb = new StringBuilder();
            sb.append("semver checker execution failed due to compilation errors:").append(System.lineSeparator());
            srcErrors.forEach(diagnostic -> sb.append(diagnostic.toString()).append(System.lineSeparator()));
            throw new SemverToolException(sb.toString());
        }
    }
}
