package io.ballerina.cli.cmd;

import io.ballerina.cli.task.Task;
import io.ballerina.projects.Project;
import io.ballerina.projects.internal.model.Target;
import io.ballerina.projects.util.ProjectConstants;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

import static io.ballerina.cli.cmd.CommandUtil.JAR;
import static io.ballerina.cli.cmd.Constants.BACKUP;
import static io.ballerina.cli.cmd.Constants.BUILD_COMMAND;
import static io.ballerina.cli.cmd.Constants.RUN_COMMAND;
import static io.ballerina.cli.cmd.Constants.TEST_COMMAND;
import static io.ballerina.cli.launcher.LauncherUtils.createLauncherException;

public class CacheArtifactsTask implements Task {
    private final String currTask;
    private boolean skipExecutable = false;
    public CacheArtifactsTask(String currTask) {
        this.currTask = currTask;
    }

    public CacheArtifactsTask(String currTask, boolean skipExecutable) {
        this.currTask = currTask;
        this.skipExecutable = skipExecutable;
    }

    @Override
    public void execute(Project project) {
        try {
            Path targetDir = project.targetDir();
            Path backupDir = project.targetDir().resolve(BACKUP);
            Target target = new Target(targetDir);
            if ((this.currTask.equals(BUILD_COMMAND) || this.currTask.equals(RUN_COMMAND)) && !skipExecutable) {
                copyFile(target.getExecutablePath(project.currentPackage()), targetDir, backupDir);
            }else if (this.currTask.equals(TEST_COMMAND)) {
                Path testSuitePath = target.getTestsCachePath().resolve(ProjectConstants.TEST_SUITE_JSON);
                String packageOrg = project.currentPackage().packageOrg().toString();
                String packageName = project.currentPackage().packageName().toString();
                String packageVersion = project.currentPackage().packageVersion().toString();
                List<Path> testArtifactsPaths =
                        Files.walk(target.cachesPath().resolve(packageOrg).resolve(packageName).resolve(packageVersion))
                                .filter(Files::isRegularFile)
                                .filter(path -> path.toString().endsWith(JAR))
                                .toList();
                copyFile(testSuitePath, targetDir, backupDir);
                for (Path testArtifactPath : testArtifactsPaths) {
                    copyFile(testArtifactPath, targetDir, backupDir);
                }
            }
        } catch (IOException e) {
            throw createLauncherException("unable to create the cache: " + e.getMessage());
        }


    }

    private static void copyFile(Path sourceFilePath , Path sourceRootPath, Path destRootPath) throws IOException {
        Path relativePath = sourceRootPath.relativize(sourceFilePath);
        Path destFile = destRootPath.resolve(relativePath);
        Files.createDirectories(destFile.getParent());
        Files.copy(sourceFilePath, destFile, StandardCopyOption.REPLACE_EXISTING);
    }
}
