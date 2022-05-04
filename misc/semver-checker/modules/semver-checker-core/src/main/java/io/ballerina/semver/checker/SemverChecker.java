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
import io.ballerina.semver.checker.diff.PackageDiff;
import io.ballerina.semver.checker.exception.SemverToolException;
import io.ballerina.semver.checker.util.DiffUtils;
import io.ballerina.semver.checker.util.PackageUtils;
import io.ballerina.tools.diagnostics.Diagnostic;

import java.io.PrintStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.ballerina.semver.checker.util.SemverUtils.BAL_FILE_EXT;

/**
 * Semver checker API.
 *
 * @since 2201.2.0
 */
public class SemverChecker {

    private final Path projectPath;
    private Package currentPackage;
    private SemanticVersion releasedVersion;
    private final PrintStream outStream;
    private final PrintStream errStream;

    public SemverChecker(Path projectPath) {
        this(projectPath, null, System.out, System.err);
    }

    public SemverChecker(Path projectPath, SemanticVersion releasedVersion) {
        this(projectPath, releasedVersion, System.out, System.err);
    }

    public SemverChecker(Path projectPath, SemanticVersion releasedVersion, PrintStream out, PrintStream err) {
        this.projectPath = projectPath;
        this.releasedVersion = releasedVersion;
        this.outStream = out;
        this.errStream = err;
    }

    /**
     * Returns the suggested version information, which is derived based on the source code compatibility between
     * the local version and the user-provided release version. (If the released version is not provided, the local
     * changes will be compared against the latest compatible published version available in the central.)
     *
     * @return suggested version information in string format
     * @throws SemverToolException If execution is failed
     */
    public String getVersionSuggestionSummary() throws SemverToolException {
        Optional<PackageDiff> packageDiff = computeDiff();
        return DiffUtils.getVersionSuggestion(packageDiff.orElse(null), getLocalVersion(), releasedVersion);
    }

    /**
     * Returns the list of source-code differences which has compatibility impacts between the local version and
     * the user-provided release version.
     * (If the released version is not provided, the local changes will be compared against the latest compatible
     * published version available in the central.)
     *
     * @return Returns the list of source-code differences in string format
     * @throws SemverToolException If execution is failed
     */
    public String getDiffSummary() throws SemverToolException {
        StringBuilder sb = new StringBuilder();
        Optional<PackageDiff> packageDiff = computeDiff();
        sb.append(DiffUtils.getDiffSummary(packageDiff.orElse(null), getLocalVersion(), releasedVersion));
        sb.append(DiffUtils.getVersionSuggestion(packageDiff.orElse(null), getLocalVersion(), releasedVersion));
        return sb.toString();
    }

    /**
     * Calculates and returns the {@link PackageDiff} object containing the change type, compatibility information,
     * and all the sub-level (i.e. module, function, service) changes of the provided Ballerina package instance.
     *
     * @return calculated {@link PackageDiff}
     * @throws SemverToolException If execution is failed
     */
    public Optional<PackageDiff> computeDiff() throws SemverToolException {
        loadCurrentPackage();

        String orgName = currentPackage.packageOrg().value();
        String pkgName = currentPackage.packageName().value();
        SemanticVersion pkgVersion = currentPackage.packageVersion().value();

        CentralClientWrapper clientWrapper = new CentralClientWrapper(outStream, errStream);
        if (releasedVersion == null) {
            errStream.println("checking for the latest compatible release version available in central...");
            releasedVersion = clientWrapper.getLatestCompatibleVersion(orgName, pkgName, pkgVersion);
        }
        Path balaPath = clientWrapper.pullPackage(orgName, pkgName, releasedVersion);
        Package balaPackage = PackageUtils.loadPackage(balaPath);

        PackageComparator packageComparator = new PackageComparator(currentPackage, balaPackage);
        return packageComparator.computeDiff();
    }

    private SemanticVersion getLocalVersion() throws SemverToolException {
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
