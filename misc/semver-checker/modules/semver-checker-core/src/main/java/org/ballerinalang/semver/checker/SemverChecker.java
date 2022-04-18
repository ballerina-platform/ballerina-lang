package org.ballerinalang.semver.checker;

import io.ballerina.projects.Package;
import io.ballerina.projects.Project;
import io.ballerina.projects.SemanticVersion;
import org.ballerinalang.semver.checker.comparator.PackageComparator;
import org.ballerinalang.semver.checker.diff.PackageDiff;
import org.ballerinalang.semver.checker.exceptions.BallerinaSemverToolException;
import org.ballerinalang.semver.checker.util.PackageUtils;

import java.nio.file.Path;
import java.util.Optional;

public class SemverChecker {

    private final Path projectPath;
    private Package loadedPackage;

    public SemverChecker(Path projectPath) throws BallerinaSemverToolException {
        this.projectPath = projectPath;
    }

    public SemanticVersion getCurrentVersion() throws BallerinaSemverToolException {
        if (this.loadedPackage == null) {
            loadPackage();
        }
        return loadedPackage.packageVersion().value();
    }

    public SemanticVersion getSuggestedVersion() throws BallerinaSemverToolException {
        if (this.loadedPackage == null) {
            loadPackage();
        }

        // new CentralAPIClient();
        return null;
    }

    public Optional<PackageDiff> computeDiff() throws BallerinaSemverToolException {
        if (this.loadedPackage == null) {
            loadPackage();
        }

        PackageComparator packageComparator = new PackageComparator(loadedPackage, loadedPackage);
        return packageComparator.computeDiff();
    }
//
//    public Diff getPatchVersionCompatibleChanges() {
//
//    }
//
//    public Diff getMinorVersionCompatibleChanges() {
//
//    }
//
//    public Diff getMajorVersionCompatibleChanges() {
//
//    }

    private void loadPackage() throws BallerinaSemverToolException {
        Project project = PackageUtils.loadProject(projectPath);
        switch (project.kind()) {
            case BUILD_PROJECT:
            case BALA_PROJECT:
                loadedPackage = project.currentPackage();
            case SINGLE_FILE_PROJECT:
                throw new BallerinaSemverToolException("semver checker tool is not applicable for single file " +
                        "projects.");
            default:
                throw new BallerinaSemverToolException("semver checker tool is not applicable for " +
                        project.kind().name());
        }
    }

}
