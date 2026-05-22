package io.ballerina.cli.cmd;

import io.ballerina.cli.task.Task;
import io.ballerina.projects.Project;
import io.ballerina.projects.util.ProjectConstants;

import java.io.IOException;
import java.nio.file.NoSuchFileException;

import static io.ballerina.cli.launcher.LauncherUtils.createLauncherException;

public class RestoreCachedArtifactsTask implements Task {
    private final boolean isWorkspace;

    public RestoreCachedArtifactsTask() {
        this.isWorkspace = false;
    }

    public RestoreCachedArtifactsTask(boolean isWorkspace) {
        this.isWorkspace = isWorkspace;
    }
    @Override
    public void execute(Project project) {
        try {
            CommandUtil.copyOneDirectoryUp(project.targetDir().resolve(ProjectConstants.EXEC_BACKUP_DIR_NAME));
        } catch (NoSuchFileException e) {
            if (isWorkspace) {
                return;
            }
            throw createLauncherException("unable to restore the cache: " + e.getMessage());
        } catch (IOException e) {
            throw createLauncherException("unable to restore the cache: " + e.getMessage());
        }
    }
}
