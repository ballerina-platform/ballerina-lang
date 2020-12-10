package io.ballerina.projects.directory;

import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.util.ProjectConstants;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Consists of static methods that may be used to obtain {@link Project}
 * information using file paths.
 */
public class ProjectPaths {

    /**
     * Finds the root directory of a Ballerina package using the filepath provided.
     *
     * @param filepath ballerina file that belongs to a package
     * @return path to the package root directory
     * @throws ProjectException if the provided path is invalid or if it is a standalone ballerina file
     */
    public static Path findPackageRoot(Path filepath) throws ProjectException {
        if (!Files.exists(filepath)) {
            throw new ProjectException("provided path does not exist");
        }
        if (!Files.isRegularFile(filepath) || !filepath.toString().endsWith(ProjectConstants.BLANG_SOURCE_EXT)) {
            throw new ProjectException("provided path is not a valid Ballerina file");
        }
        Path absFilePath = filepath.toAbsolutePath().normalize();

        // check if the file is a source file in the default module
        if (isDefaultModuleSrcFile(absFilePath)) {
            return absFilePath.getParent();
        }
        // check if the file is a test file in the default module
        if (isDefaultModuleTestFile(absFilePath)) {
            Path testsRoot = Optional.of(absFilePath.getParent()).get();
            return testsRoot.getParent();
        }
        // check if the file is a source file in a non-default module
        if (isNonDefaultModuleSrcFile(filepath)) {
            Path modulesRoot = Optional.of(Optional.of(absFilePath.getParent()).get().getParent()).get();
            return modulesRoot.getParent();
        }
        // check if the file is a test file in a non-default module
        if (isNonDefaultModuleTestFile(filepath)) {
            Path testsRoot = Optional.of(absFilePath.getParent()).get();
            Path modulesRoot = Optional.of(Optional.of(testsRoot.getParent()).get().getParent()).get();
            return modulesRoot.getParent();
        }

        throw new ProjectException("provided file path does not belong to a Ballerina package");
    }


    /**
     * Checks if the path is a standalone file.
     *
     * @param file path to bal file
     * @return true if the file is a standalone bal file
     */
    public static boolean isBallerinaStandaloneFile(Path file) {
        // Check if the file is a regular file
        if (!Files.isRegularFile(file)) {
            return false;
        }
        // Check if it is a file with bal extention.
        if (!file.toString().endsWith(ProjectDirConstants.BLANG_SOURCE_EXT)) {
            return false;
        }
        // check if the file is a source file in the default module
        if (isDefaultModuleSrcFile(file)) {
            return false;
        }
        // check if the file is a test file in the default module
        if (isDefaultModuleTestFile(file)) {
            return false;
        }
        // check if the file is a source file in a non-default module
        if (isNonDefaultModuleSrcFile(file)) {
            return false;
        }
        // check if the file is a test file in a non-default module
        if (isNonDefaultModuleTestFile(file)) {
            return false;
        }

        return true;
    }

    static boolean isDefaultModuleSrcFile(Path filePath) {
        Path absFilePath = filePath.toAbsolutePath().normalize();
        return hasBallerinaToml(Optional.of(absFilePath.getParent()).get());
    }

    static boolean isDefaultModuleTestFile(Path filePath) {
        Path absFilePath = filePath.toAbsolutePath().normalize();
        Path testsRoot = Optional.of(absFilePath.getParent()).get();
        Path projectRoot = Optional.of(testsRoot.getParent()).get();
        return ProjectConstants.TEST_DIR_NAME.equals(testsRoot.toFile().getName())
                && hasBallerinaToml(projectRoot);
    }

    static boolean isNonDefaultModuleSrcFile(Path filePath) {
        Path absFilePath = filePath.toAbsolutePath().normalize();
        Path modulesRoot = Optional.of(Optional.of(absFilePath.getParent()).get().getParent()).get();
        Path projectRoot = modulesRoot.getParent();
        return ProjectConstants.MODULES_ROOT.equals(modulesRoot.toFile().getName())
                && hasBallerinaToml(projectRoot);
    }

    static boolean isNonDefaultModuleTestFile(Path filePath) {
        Path absFilePath = filePath.toAbsolutePath().normalize();
        Path testsRoot = Optional.of(absFilePath.getParent()).get();
        Path modulesRoot = Optional.of(Optional.of(testsRoot.getParent()).get().getParent()).get();
        Path projectRoot = modulesRoot.getParent();
        return ProjectConstants.MODULES_ROOT.equals(modulesRoot.toFile().getName())
                && hasBallerinaToml(projectRoot);
    }

    private static boolean hasBallerinaToml(Path filePath) {
        Path absFilePath = filePath.toAbsolutePath().normalize();
        return absFilePath.resolve(ProjectConstants.BALLERINA_TOML).toFile().exists();
    }
}
