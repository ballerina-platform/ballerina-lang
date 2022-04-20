package io.ballerina.semver.checker;

import io.ballerina.projects.Package;
import io.ballerina.projects.Project;
import io.ballerina.projects.SemanticVersion;
import io.ballerina.semver.checker.central.CentralClientWrapper;
import io.ballerina.semver.checker.comparator.PackageComparator;
import io.ballerina.semver.checker.diff.PackageDiff;
import io.ballerina.semver.checker.exception.BallerinaSemverToolException;
import io.ballerina.semver.checker.util.PackageUtils;

import java.nio.file.Path;
import java.util.Optional;

public class SemverChecker {

    private final Path projectPath;
    private Package currentPackage;

    public SemverChecker(Path projectPath) {
        this.projectPath = projectPath;
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

    public Optional<PackageDiff> computeDiff() throws BallerinaSemverToolException {
        if (this.currentPackage == null) {
            this.currentPackage = loadPackage(projectPath);
        }

        PackageComparator packageComparator = new PackageComparator(currentPackage, currentPackage);
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
